package neoguide.project.dto;

import java.math.BigDecimal;

public class SpecialLoadsStageResponse {

    private boolean sucesso;
    private Long projectId;
    private Integer etapaAtual;
    private BigDecimal fatorSimultaneidadeVe;
    private BigDecimal demandaVeDveKva;
    private BigDecimal demandaIncendioDmbiKva;
    private BigDecimal demandaEspecialTotalKva;
    private boolean alertaTrafo50Excedido;
    private String memoriaCalculo;

    public SpecialLoadsStageResponse() {
    }

    public SpecialLoadsStageResponse(boolean sucesso, Long projectId, Integer etapaAtual,
                                    BigDecimal fatorSimultaneidadeVe, BigDecimal demandaVeDveKva,
                                    BigDecimal demandaIncendioDmbiKva, BigDecimal demandaEspecialTotalKva,
                                    boolean alertaTrafo50Excedido, String memoriaCalculo) {
        this.sucesso = sucesso;
        this.projectId = projectId;
        this.etapaAtual = etapaAtual;
        this.fatorSimultaneidadeVe = fatorSimultaneidadeVe;
        this.demandaVeDveKva = demandaVeDveKva;
        this.demandaIncendioDmbiKva = demandaIncendioDmbiKva;
        this.demandaEspecialTotalKva = demandaEspecialTotalKva;
        this.alertaTrafo50Excedido = alertaTrafo50Excedido;
        this.memoriaCalculo = memoriaCalculo;
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

    public BigDecimal getFatorSimultaneidadeVe() {
        return fatorSimultaneidadeVe;
    }

    public void setFatorSimultaneidadeVe(BigDecimal fatorSimultaneidadeVe) {
        this.fatorSimultaneidadeVe = fatorSimultaneidadeVe;
    }

    public BigDecimal getDemandaVeDveKva() {
        return demandaVeDveKva;
    }

    public void setDemandaVeDveKva(BigDecimal demandaVeDveKva) {
        this.demandaVeDveKva = demandaVeDveKva;
    }

    public BigDecimal getDemandaIncendioDmbiKva() {
        return demandaIncendioDmbiKva;
    }

    public void setDemandaIncendioDmbiKva(BigDecimal demandaIncendioDmbiKva) {
        this.demandaIncendioDmbiKva = demandaIncendioDmbiKva;
    }

    public BigDecimal getDemandaEspecialTotalKva() {
        return demandaEspecialTotalKva;
    }

    public void setDemandaEspecialTotalKva(BigDecimal demandaEspecialTotalKva) {
        this.demandaEspecialTotalKva = demandaEspecialTotalKva;
    }

    public boolean isAlertaTrafo50Excedido() {
        return alertaTrafo50Excedido;
    }

    public void setAlertaTrafo50Excedido(boolean alertaTrafo50Excedido) {
        this.alertaTrafo50Excedido = alertaTrafo50Excedido;
    }

    public String getMemoriaCalculo() {
        return memoriaCalculo;
    }

    public void setMemoriaCalculo(String memoriaCalculo) {
        this.memoriaCalculo = memoriaCalculo;
    }
}
