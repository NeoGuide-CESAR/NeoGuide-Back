package neoguide.project.engine.model.commonarea;

import java.math.BigDecimal;

public record CooktopItem(
        String descricao,
        BigDecimal potenciaKw,
        int quantidade
) {}
