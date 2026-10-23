package neoguide.project.dto.catalogo;

import java.math.BigDecimal;
import java.util.List;

public record AcDemandFactorWrapperResponse(
        String tabela,
        BigDecimal fatorPotencia,
        List<AcDemandFactorItemResponse> faixas
) {}
