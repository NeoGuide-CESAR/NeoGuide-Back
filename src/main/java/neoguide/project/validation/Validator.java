package neoguide.project.validation;

import neoguide.project.exception.ValidationException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validador manual, sem dependências externas.
 * Acumula os erros por campo e, ao final, lança {@link ValidationException} se houver algum.
 *
 * Uso:
 * <pre>
 * Validator.create()
 *     .required("name", name)
 *     .length("name", name, 3, 100)
 *     .validate();
 * </pre>
 */
public class Validator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern HAS_LETTER = Pattern.compile(".*[A-Za-zÀ-ÿ].*");
    private static final Pattern HAS_DIGIT = Pattern.compile(".*\\d.*");

    private final Map<String, String> errors = new LinkedHashMap<>();

    private Validator() {
    }

    public static Validator create() {
        return new Validator();
    }

    /** Campo obrigatório: não pode ser nulo nem conter apenas espaços. */
    public Validator required(String field, String value) {
        if (isBlank(value)) {
            addError(field, "O campo é obrigatório");
        }
        return this;
    }

    /** Tamanho mínimo e máximo (ignora valores em branco, que são tratados por required). */
    public Validator length(String field, String value, int min, int max) {
        if (isBlank(value)) {
            return this;
        }
        int size = value.trim().length();
        if (size < min) {
            addError(field, "Deve ter no mínimo " + min + " caracteres");
        } else if (size > max) {
            addError(field, "Deve ter no máximo " + max + " caracteres");
        }
        return this;
    }

    /** Formato de e-mail. */
    public Validator email(String field, String value) {
        if (isBlank(value)) {
            return this;
        }
        if (!EMAIL_PATTERN.matcher(value.trim()).matches()) {
            addError(field, "E-mail inválido");
        }
        return this;
    }

    /** Senha forte o suficiente: ao menos uma letra e ao menos um número. */
    public Validator strongPassword(String field, String value) {
        if (isBlank(value)) {
            return this;
        }
        if (!HAS_LETTER.matcher(value).matches() || !HAS_DIGIT.matcher(value).matches()) {
            addError(field, "Deve conter ao menos uma letra e um número");
        }
        return this;
    }

    /** Verifica se dois campos possuem o mesmo valor (ex.: senha e confirmação). */
    public Validator sameAs(String field, String value, String other, String message) {
        if (value == null ? other != null : !value.equals(other)) {
            addError(field, message);
        }
        return this;
    }

    /** Lança {@link ValidationException} se algum erro tiver sido acumulado. */
    public void validate() {
        if (!errors.isEmpty()) {
            throw new ValidationException(Map.copyOf(errors));
        }
    }

    private void addError(String field, String message) {
        errors.putIfAbsent(field, message);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}