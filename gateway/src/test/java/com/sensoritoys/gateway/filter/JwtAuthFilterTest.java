package com.sensoritoys.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import com.sensoritoys.gateway.security.JwtUtil;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

class JwtAuthFilterTest {

    private static final String ORIGEN = "http://localhost:5173";
    private static final String SECRET = "sensoritoys-clave-secreta-muy-segura-para-jwt-2026-xyz";

    private final JwtAuthFilter filtro = new JwtAuthFilter(new JwtUtil(SECRET), ORIGEN);

    private String token(String email, String uid, String rol) {
        return Jwts.builder()
                .subject(email)
                .claim("uid", uid)
                .claim("rol", rol)
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private MockServerWebExchange solicitar(MockServerHttpRequest.BaseBuilder<?> builder, String token) {
        MockServerHttpRequest request = token == null
                ? builder.build()
                : builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + token).build();
        return MockServerWebExchange.from(request);
    }

    private boolean pasaPorElFiltro(MockServerWebExchange exchange) {
        AtomicBoolean continuo = new AtomicBoolean(false);
        GatewayFilterChain cadena = ex -> {
            continuo.set(true);
            return Mono.empty();
        };
        filtro.filter(exchange, cadena).block();
        return continuo.get();
    }

    @Test
    void catalogoEsPublico() {
        MockServerWebExchange exchange = solicitar(MockServerHttpRequest.get("/api/v1/productos"), null);

        assertTrue(pasaPorElFiltro(exchange));
    }

    @Test
    void loginYRegistroSonPublicos() {
        assertTrue(pasaPorElFiltro(solicitar(MockServerHttpRequest.post("/api/v1/usuarios/login"), null)));
        assertTrue(pasaPorElFiltro(solicitar(MockServerHttpRequest.post("/api/v1/usuarios/registro"), null)));
    }

    @Test
    void crearProductoSinTokenDevuelve401() {
        MockServerWebExchange exchange = solicitar(MockServerHttpRequest.post("/api/v1/productos"), null);

        assertFalse(pasaPorElFiltro(exchange));
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void crearProductoConTokenInvalidoDevuelve401() {
        MockServerWebExchange exchange = solicitar(MockServerHttpRequest.post("/api/v1/productos"), "token-falso");

        assertFalse(pasaPorElFiltro(exchange));
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void crearProductoComoClienteDevuelve403() {
        MockServerWebExchange exchange = solicitar(MockServerHttpRequest.post("/api/v1/productos"),
                token("ana@test.com", "5", "CLIENTE"));

        assertFalse(pasaPorElFiltro(exchange));
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void crearProductoComoAdminPasa() {
        MockServerWebExchange exchange = solicitar(MockServerHttpRequest.post("/api/v1/productos"),
                token("admin@sensoritoys.cl", "1", "ADMIN"));

        assertTrue(pasaPorElFiltro(exchange));
    }

    @Test
    void listarUsuariosSoloAdmin() {
        MockServerWebExchange comoCliente = solicitar(MockServerHttpRequest.get("/api/v1/usuarios"),
                token("ana@test.com", "5", "CLIENTE"));
        MockServerWebExchange comoAdmin = solicitar(MockServerHttpRequest.get("/api/v1/usuarios"),
                token("admin@sensoritoys.cl", "1", "ADMIN"));

        assertFalse(pasaPorElFiltro(comoCliente));
        assertEquals(HttpStatus.FORBIDDEN, comoCliente.getResponse().getStatusCode());
        assertTrue(pasaPorElFiltro(comoAdmin));
    }

    @Test
    void clienteSoloAccedeASuPropioUsuario() {
        String tokenCliente = token("ana@test.com", "5", "CLIENTE");
        MockServerWebExchange propio = solicitar(MockServerHttpRequest.put("/api/v1/usuarios/5"), tokenCliente);
        MockServerWebExchange ajeno = solicitar(MockServerHttpRequest.put("/api/v1/usuarios/6"), tokenCliente);

        assertTrue(pasaPorElFiltro(propio));
        assertFalse(pasaPorElFiltro(ajeno));
        assertEquals(HttpStatus.FORBIDDEN, ajeno.getResponse().getStatusCode());
    }

    @Test
    void clienteNoPuedeAsignarRoles() {
        MockServerWebExchange exchange = solicitar(MockServerHttpRequest.put("/api/v1/usuarios/5/rol/1"),
                token("ana@test.com", "5", "CLIENTE"));

        assertFalse(pasaPorElFiltro(exchange));
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void clientePuedeCrearPedidoPeroNoListarlos() {
        String tokenCliente = token("ana@test.com", "5", "CLIENTE");

        assertTrue(pasaPorElFiltro(solicitar(MockServerHttpRequest.post("/api/v1/pedidos"), tokenCliente)));
        assertFalse(pasaPorElFiltro(solicitar(MockServerHttpRequest.get("/api/v1/pedidos"), tokenCliente)));
    }

    @Test
    void rutaSinReglaExigeAdmin() {
        MockServerWebExchange exchange = solicitar(MockServerHttpRequest.get("/api/v1/otra-cosa"),
                token("ana@test.com", "5", "CLIENTE"));

        assertFalse(pasaPorElFiltro(exchange));
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void preflightCorsPasaSinToken() {
        MockServerWebExchange exchange = solicitar(MockServerHttpRequest.options("/api/v1/productos"), null);

        assertTrue(pasaPorElFiltro(exchange));
    }

    @Test
    void rechazoIncluyeCabeceraCorsParaElOrigenPermitido() {
        MockServerWebExchange exchange = solicitar(
                MockServerHttpRequest.post("/api/v1/productos").header(HttpHeaders.ORIGIN, ORIGEN), null);

        assertFalse(pasaPorElFiltro(exchange));
        assertEquals(ORIGEN, exchange.getResponse().getHeaders().getAccessControlAllowOrigin());
    }

    @Test
    void rechazoNoIncluyeCabeceraCorsParaOtroOrigen() {
        MockServerWebExchange exchange = solicitar(
                MockServerHttpRequest.post("/api/v1/productos").header(HttpHeaders.ORIGIN, "http://otro-sitio.com"), null);

        assertFalse(pasaPorElFiltro(exchange));
        assertEquals(null, exchange.getResponse().getHeaders().getAccessControlAllowOrigin());
    }
    @Test
    void clienteSoloVeSusPropiosPedidos() {
        String tokenCliente = token("ana@test.com", "5", "CLIENTE");
        MockServerWebExchange propios = solicitar(MockServerHttpRequest.get("/api/v1/pedidos/usuario/5"), tokenCliente);
        MockServerWebExchange ajenos = solicitar(MockServerHttpRequest.get("/api/v1/pedidos/usuario/6"), tokenCliente);

        assertTrue(pasaPorElFiltro(propios));
        assertFalse(pasaPorElFiltro(ajenos));
        assertEquals(HttpStatus.FORBIDDEN, ajenos.getResponse().getStatusCode());
}
}
