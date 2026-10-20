package neoguide.project.engine.model.commonarea;

import java.math.BigDecimal;

public record BoilerItem(
        String descricao,
        BigDecimal potenciaKw,
        int quantidade
) {}
