package neoguide.project.exception;

/**
 * Regra de negócio violada (ex.: e-mail já cadastrado).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}