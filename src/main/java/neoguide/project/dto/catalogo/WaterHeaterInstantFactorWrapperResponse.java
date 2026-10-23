package neoguide.project.dto.catalogo;

import java.math.BigDecimal;
import java.util.List;

public record WaterHeaterInstantFactorWrapperResponse(
        String tabela,
        String descricao,
        BigDecimal fatorPotencia,
        List<WaterHeaterInstantFactorItemResponse> faixas
) {}
