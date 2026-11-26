package cl.alejandrossm.authsvc.exception;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<Map<String, Object>> invalidCredentials() {
        return error(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
    }

    @ExceptionHandler({InvalidLoginRequestException.class, HttpMessageNotReadableException.class})
    ResponseEntity<Map<String, Object>> invalidRequest(Exception exception) {
        String message = exception instanceof InvalidLoginRequestException
                ? exception.getMessage()
                : "El cuerpo de la solicitud no es valido";
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validationError(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("La solicitud no es valida");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ResourceAccessException.class)
    ResponseEntity<Map<String, Object>> usuariosNoDisponible() {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "El servicio ms_usuarios no está disponible");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", message));
    }
}
