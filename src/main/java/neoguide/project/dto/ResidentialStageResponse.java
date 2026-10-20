package neoguide.project.dto;

import java.math.BigDecimal;

public class ResidentialStageResponse {
    private boolean sucesso;
    private Long projectId;
    private BigDecimal demandaResidencialFinalDrfKva;

    public ResidentialStageResponse() {
    }

    public ResidentialStageResponse(boolean sucesso, Long projectId, BigDecimal demandaResidencialFinalDrfKva) {
        this.sucesso = sucesso;
        this.projectId = projectId;
        this.demandaResidencialFinalDrfKva = demandaResidencialFinalDrfKva;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public boolean getSucesso() {
        return sucesso;
    }

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public BigDecimal getDemandaResidencialFinalDrfKva() {
        return demandaResidencialFinalDrfKva;
    }

    public void setDemandaResidencialFinalDrfKva(BigDecimal demandaResidencialFinalDrfKva) {
        this.demandaResidencialFinalDrfKva = demandaResidencialFinalDrfKva;
    }
}
