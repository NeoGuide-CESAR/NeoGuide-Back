package neoguide.project.engine.model.commonarea;

import java.math.BigDecimal;

public record ApplianceItem(
        String descricao,
        BigDecimal potenciaKw,
        int quantidade
) {}
