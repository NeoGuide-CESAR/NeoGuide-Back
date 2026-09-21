package neoguide.project.exception;

import java.util.Map;

/**
 * Lançada quando os dados enviados na requisição não passam nas validações.
 * Carrega um mapa de campo -> mensagem de erro.
 */
public class ValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super("Dados inválidos");
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}