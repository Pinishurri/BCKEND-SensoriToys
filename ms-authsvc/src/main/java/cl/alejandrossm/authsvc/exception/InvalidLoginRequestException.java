package cl.alejandrossm.authsvc.exception;

public class InvalidLoginRequestException extends RuntimeException {
    public InvalidLoginRequestException(String message) {
        super(message);
    }
}
