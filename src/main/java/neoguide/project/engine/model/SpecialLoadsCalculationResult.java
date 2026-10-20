package neoguide.project.engine.model;

import java.math.BigDecimal;

public record SpecialLoadsCalculationResult(
        ElectricVehicleLoadOutput recargaVeicular,
        FirePumpLoadOutput motobombaIncendio,
        BigDecimal demandaEspecialTotalKva,
        BigDecimal parcelaDedutivelQdgKva,
        BigDecimal contribuicaoQdgKva,
        boolean alertaTrafo50Excedido,
        BigDecimal percentualUsoTrafoVeDs,
        String notaAuditoria
) {
}
