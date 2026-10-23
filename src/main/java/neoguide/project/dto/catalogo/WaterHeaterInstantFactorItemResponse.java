package neoguide.project.dto.catalogo;

import neoguide.project.model.normativo.WaterHeaterInstantFactor;

import java.math.BigDecimal;

public record WaterHeaterInstantFactorItemResponse(
        Integer quantidadeAparelhos,
        BigDecimal fatorDemanda
) {
    public static WaterHeaterInstantFactorItemResponse from(WaterHeaterInstantFactor entity) {
        return new WaterHeaterInstantFactorItemResponse(
                entity.getQuantidadeAparelhos(),
                entity.getFatorDemanda()
        );
    }
}
