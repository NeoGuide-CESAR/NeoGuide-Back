package neoguide.project.dto.validation;

public record NormativeValidationMessage(
    NormativeMessageType tipo,
    String regra,
    String texto,
    String detalheTecnico
) {
    public NormativeValidationMessage(NormativeMessageType tipo, String regra, String texto) {
        this(tipo, regra, texto, null);
    }
}
