package neoguide.project.engine.model.commonarea;

import java.math.BigDecimal;

public record WaterHeaterItem(
        String descricao,
        BigDecimal potenciaKw,
        int quantidade
) {}
