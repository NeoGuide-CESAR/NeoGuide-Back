package neoguide.project.dto.catalogo;

import neoguide.project.model.normativo.AcDemandFactor;

import java.math.BigDecimal;

public record AcDemandFactorItemResponse(
        String faixaAparelhos,
        Integer minAparelhos,
        Integer maxAparelhos,
        BigDecimal fatorComercial,
        BigDecimal fatorResidencial
) {
    public static AcDemandFactorItemResponse from(AcDemandFactor entity) {
        return new AcDemandFactorItemResponse(
                entity.getFaixaAparelhos(),
                entity.getMinAparelhos(),
                entity.getMaxAparelhos(),
                entity.getFatorComercial(),
                entity.getFatorResidencial()
        );
    }
}
