package neoguide.project.exception;

/**
 * Exceção lançada quando a requisição é inválida para o estado atual do recurso (HTTP 400).
 */
public class BadRequestException extends BusinessException {

    public BadRequestException(String message) {
        super(message);
    }
}
