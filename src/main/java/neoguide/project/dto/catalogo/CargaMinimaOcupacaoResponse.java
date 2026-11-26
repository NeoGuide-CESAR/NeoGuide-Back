package neoguide.project.dto.catalogo;

import neoguide.project.model.normativo.OccupancyLoadReference;

import java.math.BigDecimal;

/** Uma ocupação da DIS-NOR-030 Tabela 22, usada para montar a lista de finalidades da etapa comercial. */
public record CargaMinimaOcupacaoResponse(
        String codigo,
        String rotulo,
        BigDecimal cargaMinimaWm2,
        BigDecimal fatorDemandaIlum,
        BigDecimal fatorPotencia,
        boolean escalonado,
        String regraNormativa
) {
    public static CargaMinimaOcupacaoResponse from(OccupancyLoadReference ref) {
        return new CargaMinimaOcupacaoResponse(
                ref.getOcupacao(),
                ref.getDescricao(),
                ref.getCargaMinimaWm2(),
                ref.getFatorDemandaIlum(),
                ref.getFatorPotencia(),
                "ESCRITORIOS".equals(ref.getOcupacao()),
                ref.getRegraNormativa()
        );
    }
}
