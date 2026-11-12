package com.sensoritoys.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import com.sensoritoys.gateway.security.JwtUtil;

import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private enum Acceso { PUBLICO, AUTENTICADO, PROPIO_O_ADMIN, ADMIN }

    private record Regla(HttpMethod metodo, PathPattern patron, Acceso acceso) {
    }

    private static final PathPatternParser PARSER = new PathPatternParser();
    private static final String PREFIJO_BEARER = "Bearer ";
    private static final String ROL_ADMIN = "ADMIN";

    private static final List<Regla> REGLAS = List.of(
            regla(HttpMethod.POST, "/api/v1/usuarios/login", Acceso.PUBLICO),
            regla(HttpMethod.POST, "/api/v1/usuarios/registro", Acceso.PUBLICO),
            regla(HttpMethod.POST, "/api/v1/usuarios/logout", Acceso.AUTENTICADO),
            regla(HttpMethod.GET, "/api/v1/usuarios", Acceso.ADMIN),
            regla(HttpMethod.PUT, "/api/v1/usuarios/{id}/rol/{idRol}", Acceso.ADMIN),
            regla(null, "/api/v1/usuarios/{id}", Acceso.PROPIO_O_ADMIN),

            regla(HttpMethod.GET, "/api/v1/productos", Acceso.PUBLICO),
            regla(HttpMethod.GET, "/api/v1/productos/todos", Acceso.ADMIN),
            regla(HttpMethod.GET, "/api/v1/productos/dashboard/stock", Acceso.ADMIN),
            regla(HttpMethod.GET, "/api/v1/productos/{id}", Acceso.PUBLICO),

            regla(HttpMethod.GET, "/api/v1/categorias", Acceso.PUBLICO),

            regla(HttpMethod.POST, "/api/v1/pedidos", Acceso.AUTENTICADO),
            regla(HttpMethod.POST, "/api/v1/pedidos", Acceso.AUTENTICADO),
            regla(HttpMethod.GET, "/api/v1/pedidos/usuario/{id}", Acceso.PROPIO_O_ADMIN));

    private final JwtUtil jwtUtil;
    private final String origenPermitido;

    public JwtAuthFilter(JwtUtil jwtUtil, @Value("${sensoritoys.cors.origen-permitido}") String origenPermitido) {
        this.jwtUtil = jwtUtil;
        this.origenPermitido = origenPermitido;
    }

    private static Regla regla(HttpMethod metodo, String patron, Acceso acceso) {
        return new Regla(metodo, PARSER.parse(patron), acceso);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        PathContainer ruta = request.getPath().pathWithinApplication();
        Optional<Regla> regla = buscarRegla(request.getMethod(), ruta);
        Acceso acceso = regla.map(Regla::acceso).orElse(Acceso.ADMIN);

        if (acceso == Acceso.PUBLICO) {
            return chain.filter(exchange);
        }

        Optional<Claims> claims = leerClaims(request);
        if (claims.isEmpty()) {
            return rechazar(exchange, HttpStatus.UNAUTHORIZED, "Token ausente o inválido");
        }

        boolean esAdmin = ROL_ADMIN.equals(claims.get().get("rol", String.class));

        boolean autorizado = switch (acceso) {
            case AUTENTICADO -> true;
            case ADMIN -> esAdmin;
            case PROPIO_O_ADMIN -> esAdmin || esPropietario(regla.get(), ruta, claims.get());
            case PUBLICO -> true;
        };

        if (!autorizado) {
            return rechazar(exchange, HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta acción");
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private Optional<Regla> buscarRegla(HttpMethod metodo, PathContainer ruta) {
        return REGLAS.stream()
                .filter(regla -> regla.metodo() == null || regla.metodo().equals(metodo))
                .filter(regla -> regla.patron().matches(ruta))
                .findFirst();
    }

    private Optional<Claims> leerClaims(ServerHttpRequest request) {
        String cabecera = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (cabecera == null || !cabecera.startsWith(PREFIJO_BEARER)) {
            return Optional.empty();
        }
        return jwtUtil.leerClaims(cabecera.substring(PREFIJO_BEARER.length()).trim());
    }

    private boolean esPropietario(Regla regla, PathContainer ruta, Claims claims) {
        PathPattern.PathMatchInfo info = regla.patron().matchAndExtract(ruta);
        if (info == null) {
            return false;
        }
        String idRecurso = info.getUriVariables().get("id");
        String idUsuario = claims.get("uid", String.class);
        return idRecurso != null && idRecurso.equals(idUsuario);
    }

    private Mono<Void> rechazar(ServerWebExchange exchange, HttpStatus estado, String mensaje) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(estado);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        if (origenPermitido.equals(exchange.getRequest().getHeaders().getOrigin())) {
            response.getHeaders().setAccessControlAllowOrigin(origenPermitido);
            response.getHeaders().add(HttpHeaders.VARY, HttpHeaders.ORIGIN);
        }
        byte[] cuerpo = ("{\"error\":\"" + mensaje + "\"}").getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(cuerpo);
        return response.writeWith(Mono.just(buffer));
    }
}
