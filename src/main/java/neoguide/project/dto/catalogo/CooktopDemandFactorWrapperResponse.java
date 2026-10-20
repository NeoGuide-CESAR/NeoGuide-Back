package neoguide.project.dto.catalogo;

import java.math.BigDecimal;
import java.util.List;

public record CooktopDemandFactorWrapperResponse(
        String tabela,
        String descricao,
        BigDecimal fatorPotencia,
        List<CooktopDemandFactorItemResponse> faixas
) {}
