package neoguide.project.dto.calculation;

import java.math.BigDecimal;
import java.util.Map;

public record CalculationMemoryDto(
    String tensaoFornecimento,
    int totalApartamentos,
    BigDecimal drKva,
    BigDecimal fc,
    BigDecimal fr,
    BigDecimal drfKva,
    BigDecimal dsKva,
    Map<String, BigDecimal> parcelasServicoKva,
    BigDecimal dcKva,
    BigDecimal dveKva,
    BigDecimal dmbiKva,
    BigDecimal dedKva,
    BigDecimal dqdgKva,
    BigDecimal correnteProjetoIpA,
    String enquadramentoTensao,
    String normaReferencia
) {}
