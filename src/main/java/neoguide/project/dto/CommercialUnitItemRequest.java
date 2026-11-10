package neoguide.project.dto;

import java.math.BigDecimal;

public class CommercialUnitItemRequest {

    private String identificacao;
    private String atividadeComercial;
    private BigDecimal areaM2;
    private BigDecimal potenciaInstaladaKw;

    public CommercialUnitItemRequest() {
    }

    public CommercialUnitItemRequest(String identificacao, String atividadeComercial, BigDecimal areaM2, BigDecimal potenciaInstaladaKw) {
        this.identificacao = identificacao;
        this.atividadeComercial = atividadeComercial;
        this.areaM2 = areaM2;
        this.potenciaInstaladaKw = potenciaInstaladaKw;
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
}
