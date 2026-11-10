package neoguide.project.engine.model;

import java.math.BigDecimal;

public record CommercialUnitResult(
        String identificacao,
        String atividadeComercial,
        BigDecimal areaM2,
        BigDecimal potenciaInstaladaKw,
        BigDecimal fatorDemandaPonderado,
        BigDecimal demandaCalculadaKva
) {
}
