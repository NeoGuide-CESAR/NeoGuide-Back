package neoguide.project.engine.model;

import java.math.BigDecimal;
import neoguide.project.engine.enums.VoltageSystem;

public record ConsolidatedCalculationInput(
    BigDecimal demandaResidencialDrf,
    BigDecimal demandaServicosDs,
    BigDecimal demandaComercialDc,
    BigDecimal demandaVeDve,
    BigDecimal demandaIncendioDmbi,
    VoltageSystem sistemaTensao,
    BigDecimal potenciaTrafoKva
) {
    public ConsolidatedCalculationInput {
        demandaResidencialDrf = (demandaResidencialDrf != null) ? demandaResidencialDrf : BigDecimal.ZERO;
        demandaServicosDs = (demandaServicosDs != null) ? demandaServicosDs : BigDecimal.ZERO;
        demandaComercialDc = (demandaComercialDc != null) ? demandaComercialDc : BigDecimal.ZERO;
        demandaVeDve = (demandaVeDve != null) ? demandaVeDve : BigDecimal.ZERO;
        demandaIncendioDmbi = (demandaIncendioDmbi != null) ? demandaIncendioDmbi : BigDecimal.ZERO;
        sistemaTensao = (sistemaTensao != null) ? sistemaTensao : VoltageSystem.TRIFASICO_380_220V;

        if (demandaResidencialDrf.compareTo(BigDecimal.ZERO) < 0 ||
            demandaServicosDs.compareTo(BigDecimal.ZERO) < 0 ||
            demandaComercialDc.compareTo(BigDecimal.ZERO) < 0 ||
            demandaVeDve.compareTo(BigDecimal.ZERO) < 0 ||
            demandaIncendioDmbi.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("As parcelas de demanda não podem ser negativas.");
        }
    }
}
