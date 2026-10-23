package neoguide.project.engine.model.commonarea;

import java.math.BigDecimal;

public record ElevatorItem(
        String descricao,
        BigDecimal potenciaKw,
        int quantidade
) {}
