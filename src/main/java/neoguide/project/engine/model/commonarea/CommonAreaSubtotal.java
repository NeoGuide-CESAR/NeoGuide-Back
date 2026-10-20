package neoguide.project.engine.model.commonarea;

import java.math.BigDecimal;

public record CommonAreaSubtotal(
        BigDecimal demandaKva,
        BigDecimal potenciaAtivaKw,
        BigDecimal fatorDemandaMedio,
        BigDecimal fatorPotenciaAdotado,
        String memoriaCalculo
) {}
