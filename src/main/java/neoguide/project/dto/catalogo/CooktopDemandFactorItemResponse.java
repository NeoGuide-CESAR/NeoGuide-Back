package neoguide.project.dto.catalogo;

import neoguide.project.model.normativo.CooktopDemandFactor;

import java.math.BigDecimal;

public record CooktopDemandFactorItemResponse(
        Integer quantidade,
        BigDecimal fatorDemanda
) {
    public static CooktopDemandFactorItemResponse from(CooktopDemandFactor entity) {
        return new CooktopDemandFactorItemResponse(
                entity.getQuantidadeAparelhos(),
                entity.getFatorDemanda()
        );
    }
}
