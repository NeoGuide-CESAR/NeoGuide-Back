package neoguide.project.engine.model.commonarea;

import java.math.BigDecimal;

public record PoolPumpItem(
        String descricao,
        BigDecimal potenciaKw,
        int quantidade
) {}
