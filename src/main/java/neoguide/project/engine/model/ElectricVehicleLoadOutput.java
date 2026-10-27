package neoguide.project.engine.model;

import java.math.BigDecimal;

public record ElectricVehicleLoadOutput(
        int vagas,
        BigDecimal potenciaUnitáriaKw,
        BigDecimal potenciaTotalInstaladaKw,
        BigDecimal fatorSimultaneidadeFve,
        BigDecimal fatorPotencia,
        BigDecimal demandaDveKva
) {
}
