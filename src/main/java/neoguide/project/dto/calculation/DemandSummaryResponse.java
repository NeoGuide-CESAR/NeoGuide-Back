package neoguide.project.dto.calculation;

import java.math.BigDecimal;

public record DemandSummaryResponse(
    BigDecimal demandaResidencialFinalDrfKva,
    BigDecimal demandaServicosDsKva,
    BigDecimal demandaComercialDcKva,
    BigDecimal demandaVeDveKva,
    BigDecimal demandaIncendioDmbiKva,
    BigDecimal demandaGlobalDedKva,
    BigDecimal demandaQdgKva
) {}
