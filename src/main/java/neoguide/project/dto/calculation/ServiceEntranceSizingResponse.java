package neoguide.project.dto.calculation;

public record ServiceEntranceSizingResponse(
    String tipoFornecimento,
    Integer disjuntorGeralA,
    String disjuntorCarcaca,
    String caboFaseMm2,
    String caboNeutroMm2,
    String eletrodutoEntrada,
    String barramentoCobreQdg,
    Integer capacidadeBarramentoA
) {}
