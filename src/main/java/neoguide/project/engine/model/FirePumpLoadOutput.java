package neoguide.project.engine.model;

import java.math.BigDecimal;

public record FirePumpLoadOutput(
        BigDecimal potenciaCv,
        BigDecimal demandaDmbiKva,
        boolean derivadoAntesDoQdg,
        String chavePartidaSugerida,
        String normaOrigem
) {
}
