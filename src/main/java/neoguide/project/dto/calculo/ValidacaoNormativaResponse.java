package neoguide.project.dto.calculo;

import java.util.List;

/**
 * Situação: ERRO (há itens que impedem o cálculo), ATENCAO (só alertas) ou OK. INFO não altera a situação.
 */
public record ValidacaoNormativaResponse(
        Long projectId,
        String situacao,
        int quantidadeErros,
        int quantidadeAlertas,
        Totais totais,
        List<ItemValidacaoResponse> itens
) {
    /** Contagem por nível; INFO não conta como ponto de atenção. */
    public record Totais(int erros, int alertas, int infos) {
    }

    public ValidacaoNormativaResponse {
        itens = itens != null ? List.copyOf(itens) : List.of();
    }
}
