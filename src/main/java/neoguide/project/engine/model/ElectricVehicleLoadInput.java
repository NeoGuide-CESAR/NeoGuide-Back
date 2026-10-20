package neoguide.project.engine.model;

import java.math.BigDecimal;

public record ElectricVehicleLoadInput(
        int vagas,
        BigDecimal potenciaEstacaoKw,
        BigDecimal fatorSimultaneidadeCustom,
        BigDecimal fatorPotenciaCustom
) {
    public ElectricVehicleLoadInput {
        if (vagas < 0) {
            throw new IllegalArgumentException("O número de vagas não pode ser negativo.");
        }
        if (potenciaEstacaoKw != null && potenciaEstacaoKw.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("A potência da estação não pode ser negativa.");
        }
        if (fatorPotenciaCustom != null &&
                (fatorPotenciaCustom.compareTo(BigDecimal.ZERO) <= 0 || fatorPotenciaCustom.compareTo(BigDecimal.ONE) > 0)) {
            throw new IllegalArgumentException("O fator de potência deve estar entre 0 (exclusivo) e 1.00 (inclusivo).");
        }
    }

    public static ElectricVehicleLoadInput padrao(int vagas, BigDecimal potenciaEstacaoKw) {
        return new ElectricVehicleLoadInput(vagas, potenciaEstacaoKw, null, null);
    }
}
