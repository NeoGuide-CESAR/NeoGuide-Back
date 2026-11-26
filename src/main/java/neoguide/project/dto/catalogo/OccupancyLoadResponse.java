package neoguide.project.dto.catalogo;

import neoguide.project.model.normativo.OccupancyLoadReference;
import java.math.BigDecimal;

public record OccupancyLoadResponse(
        Long id,
        String ocupacao,
        String descricao,
        BigDecimal cargaMinimaWm2,
        BigDecimal vitrineWMetro,
        BigDecimal fatorDemandaIlum,
        BigDecimal fatorDemandaTug,
        BigDecimal fatorPotencia,
        String regraNormativa,
        String normaOrigem
) {
    public static OccupancyLoadResponse fromEntity(OccupancyLoadReference entity) {
        if (entity == null) {
            return null;
        }
        return new OccupancyLoadResponse(
                entity.getId(),
                entity.getOcupacao(),
                entity.getDescricao(),
                entity.getCargaMinimaWm2(),
                entity.getVitrineWMetro(),
                entity.getFatorDemandaIlum(),
                entity.getFatorDemandaTug(),
                entity.getFatorPotencia(),
                entity.getRegraNormativa(),
                entity.getNormaOrigem()
        );
    }
}
