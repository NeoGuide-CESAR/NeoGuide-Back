package neoguide.project.engine.model.commonarea;

import java.math.BigDecimal;

public record AirConditioningItem(
        String descricao,
        Integer capacidadeBtu,
        BigDecimal potenciaW,
        BigDecimal potenciaVa,
        int quantidade
) {}
