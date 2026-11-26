package neoguide.project.dto.calculo;

/**
 * Item da validação normativa. {@code etapa} indica a etapa a corrigir (residencial, area-comum, comercial,
 * cargas-especiais, projeto). {@code aba} (área comum) e {@code item} (índice, base 0, em listas) levam ao ponto exato.
 */
public record ItemValidacaoResponse(
        String codigo,
        String nivel,
        String mensagem,
        String campo,
        String etapa,
        String aba,
        Integer item,
        String rotuloAcao,
        Alternativa alternativa
) {
    /** Segunda ação possível para o mesmo item (ex.: alterar o tipo de edificação). */
    public record Alternativa(String rotulo, String etapa, String campo) {
    }
}
