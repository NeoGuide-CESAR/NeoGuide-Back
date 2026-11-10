package neoguide.project.dto;

import java.math.BigDecimal;

public class CommercialUnitItemResponse {

    private Long id;
    private String identificacao;
    private String atividadeComercial;
    private BigDecimal areaM2;
    private BigDecimal potenciaInstaladaKw;
    private BigDecimal fatorDemanda;
    private BigDecimal demandaCalculadaKva;

    public CommercialUnitItemResponse() {
    }

    public CommercialUnitItemResponse(Long id, String identificacao, String atividadeComercial,
                                      BigDecimal areaM2, BigDecimal potenciaInstaladaKw,
                                      BigDecimal fatorDemanda, BigDecimal demandaCalculadaKva) {
        this.id = id;
        this.identificacao = identificacao;
        this.atividadeComercial = atividadeComercial;
        this.areaM2 = areaM2;
        this.potenciaInstaladaKw = potenciaInstaladaKw;
        this.fatorDemanda = fatorDemanda;
        this.demandaCalculadaKva = demandaCalculadaKva;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentificacao() {
        return identificacao;
    }

    public void setIdentificacao(String identificacao) {
        this.identificacao = identificacao;
    }

    public String getAtividadeComercial() {
        return atividadeComercial;
    }

    public void setAtividadeComercial(String atividadeComercial) {
        this.atividadeComercial = atividadeComercial;
    }

    public BigDecimal getAreaM2() {
        return areaM2;
    }

    public void setAreaM2(BigDecimal areaM2) {
        this.areaM2 = areaM2;
    }

    public BigDecimal getPotenciaInstaladaKw() {
        return potenciaInstaladaKw;
    }

    public void setPotenciaInstaladaKw(BigDecimal potenciaInstaladaKw) {
        this.potenciaInstaladaKw = potenciaInstaladaKw;
    }

    public BigDecimal getFatorDemanda() {
        return fatorDemanda;
    }

    public void setFatorDemanda(BigDecimal fatorDemanda) {
        this.fatorDemanda = fatorDemanda;
    }

    public BigDecimal getDemandaCalculadaKva() {
        return demandaCalculadaKva;
    }

    public void setDemandaCalculadaKva(BigDecimal demandaCalculadaKva) {
        this.demandaCalculadaKva = demandaCalculadaKva;
    }
}
