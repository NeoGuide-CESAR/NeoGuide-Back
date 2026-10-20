package neoguide.project.engine.model;

import java.math.BigDecimal;
import java.util.List;

public record CommercialCalculationResult(
        BigDecimal potenciaTotalInstaladaKw,
        BigDecimal potenciaPrimeiraFaixaKw,
        BigDecimal potenciaExcedenteKw,
        BigDecimal demandaAtivaPonderadaKw,
        BigDecimal fatorPotenciaAdotado,
        BigDecimal demandaComercialDcKva,
        List<CommercialUnitResult> unidadesDetalhadas,
        String memoriaCalculo
) {
}
