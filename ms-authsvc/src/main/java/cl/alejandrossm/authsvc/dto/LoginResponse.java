package cl.alejandrossm.authsvc.dto;

/** Includes the guide's fields and aliases compatible with the SensoriToys frontend. */
public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        String username,
        String role,
        Long id,
        String nombre,
        String email,
        String rol) {
}
