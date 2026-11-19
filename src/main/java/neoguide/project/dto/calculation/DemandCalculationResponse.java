package neoguide.project.dto.calculation;

import neoguide.project.model.DemandResults;
import neoguide.project.model.Project;

import java.time.LocalDateTime;

public record DemandCalculationResponse(
    Long projectId,
    String protocolo,
    String status,
    DemandSummaryResponse resumoDemandas,
    ServiceEntranceSizingResponse dimensionamentoEntrada,
    String seloConformidade,
    LocalDateTime calculadoEm
) {
    public static DemandCalculationResponse of(Project project, DemandResults results,
                                               String disjuntorCarcaca, String eletroduto) {
        DemandSummaryResponse resumo = new DemandSummaryResponse(
            results.getDemandaResidencialKva(),
            results.getDemandaServicoKva(),
            results.getDemandaComercialKva(),
            results.getDemandaVeKva(),
            results.getDemandaIncendioKva(),
            results.getDemandaGlobalKva(),
            results.getDemandaQdgKva()
        );

        ServiceEntranceSizingResponse entrada = new ServiceEntranceSizingResponse(
            results.getTipoFornecimento(),
            results.getDisjuntorGeralA(),
            disjuntorCarcaca,
            results.getCaboFaseMm2(),
            results.getCaboNeutroMm2(),
            eletroduto,
            results.getBarramentoCobre(),
            results.getCapacidadeBarramentoA()
        );

        return new DemandCalculationResponse(
            project.getId(),
            project.getProtocolo(),
            project.getStatus().name(),
            resumo,
            entrada,
            "CONFORME_DIS_NOR_053",
            results.getCalculadoEm()
        );
    }
}
