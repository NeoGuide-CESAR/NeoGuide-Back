package neoguide.project.dto.catalogo;

import neoguide.project.model.normativo.BusbarReference;
import java.math.BigDecimal;

public record BusbarCatalogResponse(
        Long id,
        BigDecimal maxDemandaKva,
        String barraMm,
        String barraPol,
        Integer correnteNominalA,
        String normaOrigem
) {
    public static BusbarCatalogResponse fromEntity(BusbarReference entity) {
        if (entity == null) return null;
        return new BusbarCatalogResponse(
                entity.getId(),
                entity.getMaxDemandaKva(),
                entity.getBarraDimensaoMm(),
                entity.getBarraDimensaoPol(),
                entity.getCorrenteNominalA(),
                entity.getNormaOrigem()
        );
    }
}
