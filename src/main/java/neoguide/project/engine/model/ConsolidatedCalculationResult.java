package neoguide.project.engine.model;

import java.math.BigDecimal;
import java.util.List;
import neoguide.project.engine.enums.SupplyVoltageLevel;
import neoguide.project.engine.enums.VoltageSystem;

public record ConsolidatedCalculationResult(
    BigDecimal demandaTotalEdificacaoDed,
    BigDecimal demandaQdgDqdg,
    BigDecimal demandaIncendioDeduzidaDmbi,
    BigDecimal correnteProjetoIpA,
    VoltageSystem sistemaTensao,
    SupplyVoltageLevel nivelFornecimento,
    ServiceEntranceDimensioning padraoEntrada,
    BusbarDimensioning barramentoQdg,
    boolean disjuntorAdequado,
    boolean barramentoAdequado,
    List<String> notasNormativas
) {}
