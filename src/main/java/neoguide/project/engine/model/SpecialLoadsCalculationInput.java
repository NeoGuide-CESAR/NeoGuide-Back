package neoguide.project.engine.model;

import java.math.BigDecimal;

public record SpecialLoadsCalculationInput(
        ElectricVehicleLoadInput recargaVeicular,
        FirePumpLoadInput motobombaIncendio,
        BigDecimal potenciaTransformadorKva,
        BigDecimal demandaServicosDsKva
) {
    public static SpecialLoadsCalculationInput apenas(ElectricVehicleLoadInput ve, FirePumpLoadInput mbi) {
        return new SpecialLoadsCalculationInput(ve, mbi, null, null);
    }
}
