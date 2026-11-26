package neoguide.project.dto.simulation;

import java.math.BigDecimal;
import java.util.List;

public record PreCalculationResponse(
    long tempoProcessamentoMs,
    BigDecimal demandaResidencialBrutaDrKva,
    BigDecimal fatorCoincidenciaFc,
    BigDecimal fatorSegurancaFr,
    BigDecimal demandaResidencialFinalDrfKva,
    BigDecimal demandaServicosDsKva,
    BigDecimal demandaComercialDcKva,
    BigDecimal demandaVeDveKva,
    BigDecimal demandaIncendioDmbiKva,
    BigDecimal demandaGlobalDedKva,
    BigDecimal demandaQdgKva,
    String faixaAtendimento,
    Integer disjuntorGeralSugeridoA,
    String caboFaseSugeridoMm2,
    String caboNeutroSugeridoMm2,
    String barramentoQdgSugeridoMm,
    Integer capacidadeBarramentoA,
    String alertaNormativo,
    List<String> alertasNormativosDetalhados
) {}
