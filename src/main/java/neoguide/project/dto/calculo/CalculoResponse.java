package neoguide.project.dto.calculo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Resultado do cálculo (POST /calcular, GET /resultado). Valores em kVA com 2 casas, arredondados só na apresentação.
 *
 * <p>{@code situacaoCalculo}: CALCULADO (insumos iguais aos do cálculo) ou DESATUALIZADO (houve alteração depois).
 * {@code seloConformidade}: ATENDIMENTO_MT, COM_ALERTAS ou CONFORME.
 */
public record CalculoResponse(
        Long projectId,
        String protocolo,
        String status,
        String situacaoCalculo,
        LocalDateTime calculadoEm,
        ResumoDemandas resumoDemandas,
        Enquadramento enquadramento,
        DimensionamentoEntrada dimensionamentoEntrada,
        BarramentoQdg barramentoQdg,
        List<AlertaResponse> alertas,
        String seloConformidade
) {
    public CalculoResponse {
        alertas = alertas != null ? List.copyOf(alertas) : List.of();
    }

    public record ResumoDemandas(BigDecimal drf, BigDecimal ds, BigDecimal dc, BigDecimal dve, BigDecimal dmbi,
                                 BigDecimal ded, BigDecimal dqdg) {
    }

    /** {@code tipoEdificacao} é o escolhido; {@code tipoEnquadramento} é o derivado, usado no limite de BT. */
    public record Enquadramento(String tipoFornecimento, BigDecimal limiteKva, boolean atendimentoMT,
                                String tipoEdificacao, String tipoEnquadramento, String motivoEnquadramento) {
    }

    public record DimensionamentoEntrada(Integer disjuntorGeralA, Integer circuitos, String caboFaseMm2, String caboNeutroMm2,
                                         String eletrodutoEntrada, String tabelaReferencia, BigDecimal correnteProjetoA) {
    }

    public record BarramentoQdg(String secao, Integer capacidadeA, String tabelaReferencia) {
    }

    public record AlertaResponse(String codigo, String nivel, String mensagem, String campo) {
    }
}
