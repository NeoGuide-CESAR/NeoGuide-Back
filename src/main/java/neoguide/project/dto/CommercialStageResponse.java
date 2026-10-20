package neoguide.project.dto;

import java.math.BigDecimal;
import java.util.List;

public class CommercialStageResponse {

    private boolean sucesso;
    private Long projectId;
    private Integer etapaAtual;
    private int totalUnidades;
    private BigDecimal potenciaTotalInstaladaKw;
    private BigDecimal demandaComercialDcKva;
    private String memoriaCalculo;
    private List<CommercialUnitItemResponse> unidades;

    public CommercialStageResponse() {
    }

    public CommercialStageResponse(boolean sucesso, Long projectId, Integer etapaAtual, int totalUnidades,
                                   BigDecimal potenciaTotalInstaladaKw, BigDecimal demandaComercialDcKva,
                                   String memoriaCalculo, List<CommercialUnitItemResponse> unidades) {
        this.sucesso = sucesso;
        this.projectId = projectId;
        this.etapaAtual = etapaAtual;
        this.totalUnidades = totalUnidades;
        this.potenciaTotalInstaladaKw = potenciaTotalInstaladaKw;
        this.demandaComercialDcKva = demandaComercialDcKva;
        this.memoriaCalculo = memoriaCalculo;
        this.unidades = unidades;
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

    public int getTotalUnidades() {
        return totalUnidades;
    }

    public void setTotalUnidades(int totalUnidades) {
        this.totalUnidades = totalUnidades;
    }

    public BigDecimal getPotenciaTotalInstaladaKw() {
        return potenciaTotalInstaladaKw;
    }

    public void setPotenciaTotalInstaladaKw(BigDecimal potenciaTotalInstaladaKw) {
        this.potenciaTotalInstaladaKw = potenciaTotalInstaladaKw;
    }

    public BigDecimal getDemandaComercialDcKva() {
        return demandaComercialDcKva;
    }

    public void setDemandaComercialDcKva(BigDecimal demandaComercialDcKva) {
        this.demandaComercialDcKva = demandaComercialDcKva;
    }

    public String getMemoriaCalculo() {
        return memoriaCalculo;
    }

    public void setMemoriaCalculo(String memoriaCalculo) {
        this.memoriaCalculo = memoriaCalculo;
    }

    public List<CommercialUnitItemResponse> getUnidades() {
        return unidades;
    }

    public void setUnidades(List<CommercialUnitItemResponse> unidades) {
        this.unidades = unidades;
    }
}
