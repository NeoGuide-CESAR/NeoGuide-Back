package neoguide.project.exception;

/**
 * Conflito com o estado atual do recurso (ex.: recurso já existente, HTTP 409).
 */
public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(message);
    }
}
