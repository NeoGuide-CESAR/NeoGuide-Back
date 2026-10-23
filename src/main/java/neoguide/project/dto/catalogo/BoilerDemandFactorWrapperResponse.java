package neoguide.project.dto.catalogo;

import java.math.BigDecimal;
import java.util.List;

public record BoilerDemandFactorWrapperResponse(
        String tabela,
        String descricao,
        BigDecimal fatorPotencia,
        List<BoilerDemandFactorItemResponse> faixas
) {}
