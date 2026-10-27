package neoguide.project.engine.model;

import java.math.BigDecimal;

public record CommercialUnitInput(
        String identificacao,
        String atividadeComercial,
        BigDecimal areaM2,
        BigDecimal potenciaInstaladaKw
) {
}
