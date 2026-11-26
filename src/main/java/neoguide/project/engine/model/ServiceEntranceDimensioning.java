package neoguide.project.engine.model;

import java.math.BigDecimal;

public record ServiceEntranceDimensioning(
    String tensaoFornecimento,
    BigDecimal limiteDemandaKva,
    int circuitosPorFase,
    String secaoCondutorFase,
    String secaoCondutorNeutro,
    String eletrodutoPeadMm,
    String eletrodutoAcoMm,
    int correnteTotalAdmissivelA,
    String disjuntorCarcaca,
    int disjuntorCorrenteInA,
    int capacidadeInterrupcaoKa,
    String normaReferencia
) {}
