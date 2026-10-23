package neoguide.project.engine.model.commonarea;

import java.math.BigDecimal;

public record CustomLoadItem(
        String descricao,
        BigDecimal potenciaKw,
        BigDecimal fatorPotencia,
        BigDecimal fatorDemanda,
        int quantidade
) {}
