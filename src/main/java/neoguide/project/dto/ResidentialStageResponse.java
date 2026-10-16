package neoguide.project.dto;

import java.math.BigDecimal;

public class ResidentialStageResponse {
    private boolean sucesso;
    private Long projectId;
    private Integer etapaAtual;
    private Integer totalApartamentos;
    private BigDecimal demandaResidencialBrutaDrKva;
    private BigDecimal fatorCoincidenciaFc;
    private BigDecimal fatorSegurancaFr;
    private BigDecimal demandaResidencialFinalDrfKva;

    public ResidentialStageResponse() {
    }

    public ResidentialStageResponse(boolean sucesso, Long projectId, Integer etapaAtual, Integer totalApartamentos,
                                    BigDecimal demandaResidencialBrutaDrKva, BigDecimal fatorCoincidenciaFc,
                                    BigDecimal fatorSegurancaFr, BigDecimal demandaResidencialFinalDrfKva) {
        this.sucesso = sucesso;
        this.projectId = projectId;
        this.etapaAtual = etapaAtual;
        this.totalApartamentos = totalApartamentos;
        this.demandaResidencialBrutaDrKva = demandaResidencialBrutaDrKva;
        this.fatorCoincidenciaFc = fatorCoincidenciaFc;
        this.fatorSegurancaFr = fatorSegurancaFr;
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

    public Integer getEtapaAtual() {
        return etapaAtual;
    }

    public void setEtapaAtual(Integer etapaAtual) {
        this.etapaAtual = etapaAtual;
    }

    public Integer getTotalApartamentos() {
        return totalApartamentos;
    }

    public void setTotalApartamentos(Integer totalApartamentos) {
        this.totalApartamentos = totalApartamentos;
    }

    public BigDecimal getDemandaResidencialBrutaDrKva() {
        return demandaResidencialBrutaDrKva;
    }

    public void setDemandaResidencialBrutaDrKva(BigDecimal demandaResidencialBrutaDrKva) {
        this.demandaResidencialBrutaDrKva = demandaResidencialBrutaDrKva;
    }

    public BigDecimal getFatorCoincidenciaFc() {
        return fatorCoincidenciaFc;
    }

    public void setFatorCoincidenciaFc(BigDecimal fatorCoincidenciaFc) {
        this.fatorCoincidenciaFc = fatorCoincidenciaFc;
    }

    public BigDecimal getFatorSegurancaFr() {
        return fatorSegurancaFr;
    }

    public void setFatorSegurancaFr(BigDecimal fatorSegurancaFr) {
        this.fatorSegurancaFr = fatorSegurancaFr;
    }

    public BigDecimal getDemandaResidencialFinalDrfKva() {
        return demandaResidencialFinalDrfKva;
    }

    public void setDemandaResidencialFinalDrfKva(BigDecimal demandaResidencialFinalDrfKva) {
        this.demandaResidencialFinalDrfKva = demandaResidencialFinalDrfKva;
    }
}
