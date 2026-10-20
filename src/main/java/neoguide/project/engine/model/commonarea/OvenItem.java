package neoguide.project.engine.model.commonarea;

import java.math.BigDecimal;

public record OvenItem(
        String descricao,
        BigDecimal potenciaKw,
        int quantidade
) {}
