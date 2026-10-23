package neoguide.project.engine.model.commonarea;

import java.math.BigDecimal;

public record LightingTugInput(
        BigDecimal potenciaIluminacaoKw,
        BigDecimal potenciaTugKw
) {}
