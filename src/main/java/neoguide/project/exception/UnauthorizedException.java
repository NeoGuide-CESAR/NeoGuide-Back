package neoguide.project.exception;

/**
 * Credenciais inválidas no login.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}