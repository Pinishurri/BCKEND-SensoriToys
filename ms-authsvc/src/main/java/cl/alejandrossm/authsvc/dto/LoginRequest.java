package cl.alejandrossm.authsvc.dto;

/** Accepts the Authenticator contract and the existing SensoriToys login contract. */
public record LoginRequest(String username, String password, String email, String contrasenia) {

    public String effectiveUsername() {
        return username != null ? username : email;
    }

    public String effectivePassword() {
        return password != null ? password : contrasenia;
    }
}
