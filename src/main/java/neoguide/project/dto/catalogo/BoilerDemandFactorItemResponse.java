package neoguide.project.dto.catalogo;

import neoguide.project.model.normativo.BoilerDemandFactor;

import java.math.BigDecimal;

public record BoilerDemandFactorItemResponse(
        Integer quantidadeBoilers,
        BigDecimal fatorDemanda
) {
    public static BoilerDemandFactorItemResponse from(BoilerDemandFactor entity) {
        return new BoilerDemandFactorItemResponse(
                entity.getQuantidadeBoilers(),
                entity.getFatorDemanda()
        );
    }
}
