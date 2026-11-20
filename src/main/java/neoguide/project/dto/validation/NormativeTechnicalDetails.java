package neoguide.project.dto.validation;

import java.math.BigDecimal;

public record NormativeTechnicalDetails(
    BigDecimal demandaTotalEdificacaoDedKva,
    BigDecimal demandaQdgDqdgKva,
    BigDecimal correnteProjetoIpA,
    Integer disjuntorGeralInA,
    Integer capacidadeBarramentoA,
    BigDecimal potenciaNominalTrafoKva,
    BigDecimal percentualOcupacaoVeServico,
    String nivelTensaoEnquadrado
) {}
