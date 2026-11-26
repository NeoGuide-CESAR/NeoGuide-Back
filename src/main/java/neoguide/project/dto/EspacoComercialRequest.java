package neoguide.project.dto;

import java.math.BigDecimal;

/** Espaço comercial enviado pelo front. A posição na lista define a ordem. */
public class EspacoComercialRequest {

    private String ocupacaoCodigo;
    private BigDecimal areaM2;
    private BigDecimal cargaInstaladaKw;

    public EspacoComercialRequest() {
    }

    public EspacoComercialRequest(String ocupacaoCodigo, BigDecimal areaM2, BigDecimal cargaInstaladaKw) {
        this.ocupacaoCodigo = ocupacaoCodigo;
        this.areaM2 = areaM2;
        this.cargaInstaladaKw = cargaInstaladaKw;
    }

    public String getOcupacaoCodigo() { return ocupacaoCodigo; }
    public void setOcupacaoCodigo(String ocupacaoCodigo) { this.ocupacaoCodigo = ocupacaoCodigo; }
    public BigDecimal getAreaM2() { return areaM2; }
    public void setAreaM2(BigDecimal areaM2) { this.areaM2 = areaM2; }
    public BigDecimal getCargaInstaladaKw() { return cargaInstaladaKw; }
    public void setCargaInstaladaKw(BigDecimal cargaInstaladaKw) { this.cargaInstaladaKw = cargaInstaladaKw; }
}
