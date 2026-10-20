package neoguide.project.engine.model;

import java.math.BigDecimal;

public record BusbarDimensioning(
    BigDecimal limiteDemandaKva,
    String barraDimensaoMm,
    String barraDimensaoPol,
    int correnteNominalA,
    String normaReferencia
) {}
