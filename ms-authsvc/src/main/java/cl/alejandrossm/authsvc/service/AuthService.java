package cl.alejandrossm.authsvc.service;

import java.time.Instant;
import java.util.Map;

import cl.alejandrossm.authsvc.dto.LoginRequest;
import cl.alejandrossm.authsvc.dto.LoginResponse;
import cl.alejandrossm.authsvc.exception.InvalidLoginRequestException;
import cl.alejandrossm.authsvc.model.AppUser;
import cl.alejandrossm.authsvc.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final AppUserRepository appUserRepository;
    private final RestClient usuariosClient;
    private final long expirationSeconds;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            AppUserRepository appUserRepository,
            @Value("${authsvc.usuarios-url}") String usuariosUrl,
            @Value("${jwt.expiration-seconds}") long expirationSeconds) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.appUserRepository = appUserRepository;
        this.usuariosClient = RestClient.create(usuariosUrl);
        this.expirationSeconds = expirationSeconds;
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null || isBlank(request.effectiveUsername()) || isBlank(request.effectivePassword())) {
            throw new InvalidLoginRequestException("username/email y password/contrasenia son obligatorios");
        }

        String username = request.effectiveUsername().trim();

        // Users from the reference guide are stored locally in H2.
        AppUser demoUser = appUserRepository.findByUsername(username).orElse(null);
        if (demoUser != null) {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, request.effectivePassword()));
            String role = authentication.getAuthorities().iterator().next().getAuthority()
                    .replaceFirst("^ROLE_", "");
            String displayName = username.equals("admin") ? "Administrador" : "Cliente";
            String email = username.equals("admin")
                    ? "admin@sensoritoys.cl"
                    : "cliente@sensoritoys.cl";
            return emitirToken(username, role, demoUser.getId(), displayName, email);
        }

        // Store accounts remain in ms_usuarios/MySQL. It validates their credentials;
        // this service issues the token consumed by the gateway.
        try {
            StoreLoginResponse user = usuariosClient.post()
                    .uri("/api/v1/usuarios/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("email", username, "contrasenia", request.effectivePassword()))
                    .retrieve()
                    .body(StoreLoginResponse.class);

            if (user == null || user.id() == null || isBlank(user.rol())) {
                throw new IllegalStateException("ms_usuarios devolvió una respuesta de login incompleta");
            }
            return emitirToken(user.email(), user.rol(), user.id(), user.nombre(), user.email());
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 401 || exception.getStatusCode().value() == 403) {
                throw new BadCredentialsException("Credenciales inválidas");
            }
            throw exception;
        }
    }

    private LoginResponse emitirToken(String username, String role, Long id, String nombre, String email) {
        Instant issuedAt = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("authsvc")
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(expirationSeconds))
                .subject(username)
                .claim("role", role)
                .claim("uid", String.valueOf(id))
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new LoginResponse(token, "Bearer", expirationSeconds,
                username, role, id, nombre, email, role);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record StoreLoginResponse(Long id, String nombre, String email, String rol) {
    }
}
