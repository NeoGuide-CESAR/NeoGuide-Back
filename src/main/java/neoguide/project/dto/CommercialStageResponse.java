package neoguide.project.dto;

import java.math.BigDecimal;
import java.util.List;

public class CommercialStageResponse {

    private boolean sucesso;
    private Long projectId;
    private Integer etapaAtual;
    private int totalEspacos;
    private BigDecimal potenciaTotalInstaladaKw;
    private BigDecimal demandaComercialDcKva;
    private String memoriaCalculo;
    private List<CommercialUnitItemResponse> espacos;
    private String tipoEnquadramento;

    public CommercialStageResponse() {
    }

    public CommercialStageResponse(boolean sucesso, Long projectId, Integer etapaAtual, int totalEspacos,
                                   BigDecimal potenciaTotalInstaladaKw, BigDecimal demandaComercialDcKva,
                                   String memoriaCalculo, List<CommercialUnitItemResponse> espacos) {
        this.sucesso = sucesso;
        this.projectId = projectId;
        this.etapaAtual = etapaAtual;
        this.totalEspacos = totalEspacos;
        this.potenciaTotalInstaladaKw = potenciaTotalInstaladaKw;
        this.demandaComercialDcKva = demandaComercialDcKva;
        this.memoriaCalculo = memoriaCalculo;
        this.espacos = espacos;
    }

    public boolean isSucesso() { return sucesso; }
    public Long getProjectId() { return projectId; }
    public Integer getEtapaAtual() { return etapaAtual; }
    public int getTotalEspacos() { return totalEspacos; }
    public BigDecimal getPotenciaTotalInstaladaKw() { return potenciaTotalInstaladaKw; }
    public BigDecimal getDemandaComercialDcKva() { return demandaComercialDcKva; }
    public String getMemoriaCalculo() { return memoriaCalculo; }
    public List<CommercialUnitItemResponse> getEspacos() { return espacos; }
    /** Tipo usado no enquadramento BT/MT (residencial com espaços comerciais vira MISTO). */
    public String getTipoEnquadramento() { return tipoEnquadramento; }
    public void setTipoEnquadramento(String tipoEnquadramento) { this.tipoEnquadramento = tipoEnquadramento; }
}
