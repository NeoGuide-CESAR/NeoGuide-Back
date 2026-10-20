package neoguide.project.engine.model;

import java.math.BigDecimal;

public record FirePumpLoadInput(
        BigDecimal potenciaCv,
        BigDecimal demandaKvaCustom,
        boolean derivadoAntesDoQdg
) {
    public FirePumpLoadInput {
        if (potenciaCv != null && potenciaCv.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("A potência da motobomba em CV não pode ser negativa.");
        }
        if (demandaKvaCustom != null && demandaKvaCustom.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("A demanda customizada em kVA não pode ser negativa.");
        }
    }

    public static FirePumpLoadInput padrao(BigDecimal potenciaCv) {
        return new FirePumpLoadInput(potenciaCv, null, true);
    }
}
