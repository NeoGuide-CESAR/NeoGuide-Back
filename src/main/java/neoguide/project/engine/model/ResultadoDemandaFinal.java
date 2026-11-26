package neoguide.project.engine.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado do cálculo final (DIS-NOR-053 e DIS-NOR-030). Valores sem arredondamento; a apresentação usa 2 casas.
 * Em atendimento em média tensão, {@code entrada}, {@code barramento} e {@code ipA} ficam nulos.
 */
public record ResultadoDemandaFinal(
        BigDecimal drf,
        BigDecimal ds,
        BigDecimal dc,
        BigDecimal dve,
        BigDecimal dmbi,
        BigDecimal ded,
        BigDecimal dqdg,
        BigDecimal limiteBtKva,
        boolean atendimentoMt,
        BigDecimal ipA,
        DimensionamentoEntrada entrada,
        BarramentoQdg barramento,
        List<AlertaCalculo> alertas
) {
    public ResultadoDemandaFinal {
        alertas = alertas != null ? List.copyOf(alertas) : List.of();
    }

    /** Entrada de serviço (Tabela 2, 380/220 V), escolhida pela Ded. */
    public record DimensionamentoEntrada(
            Integer disjuntorGeralA,
            Integer circuitos,
            String caboFaseMm2,
            String caboNeutroMm2,
            String eletrodutoEntrada,
            String tabelaReferencia
    ) {
    }

    /** Barramento do QDG (Tabela 3), escolhido pelo DQDG. */
    public record BarramentoQdg(
            String secao,
            Integer capacidadeA,
            String tabelaReferencia
    ) {
    }

    /** Alerta do cálculo. Nível: INFO, ALERTA ou ERRO. */
    public record AlertaCalculo(
            String codigo,
            String nivel,
            String mensagem,
            String campo
    ) {
    }
}
