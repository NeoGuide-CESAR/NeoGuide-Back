package neoguide.project.dto;

import java.math.BigDecimal;

public class CommonAreaStageResponse {

    private boolean sucesso;
    private Long projectId;
    private Integer etapaAtual;
    private Integer totalCargas;
    private BigDecimal demandaServicosDsKva;

    public CommonAreaStageResponse() {
    }

    public CommonAreaStageResponse(boolean sucesso, Long projectId, Integer etapaAtual,
                                   Integer totalCargas, BigDecimal demandaServicosDsKva) {
        this.sucesso = sucesso;
        this.projectId = projectId;
        this.etapaAtual = etapaAtual;
        this.totalCargas = totalCargas;
        this.demandaServicosDsKva = demandaServicosDsKva;
    }

    public boolean isSucesso() {
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

    public Integer getEtapaAtual() {
        return etapaAtual;
    }

    public void setEtapaAtual(Integer etapaAtual) {
        this.etapaAtual = etapaAtual;
    }

    public Integer getTotalCargas() {
        return totalCargas;
    }

    public void setTotalCargas(Integer totalCargas) {
        this.totalCargas = totalCargas;
    }

    public BigDecimal getDemandaServicosDsKva() {
        return demandaServicosDsKva;
    }

    public void setDemandaServicosDsKva(BigDecimal demandaServicosDsKva) {
        this.demandaServicosDsKva = demandaServicosDsKva;
    }
}
