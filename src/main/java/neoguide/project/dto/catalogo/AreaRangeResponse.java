package neoguide.project.dto.catalogo;

import com.fasterxml.jackson.annotation.JsonProperty;
import neoguide.project.model.normativo.AreaRangeReference;

import java.math.BigDecimal;

public class AreaRangeResponse {

    private Long id;
    private String faixa;
    private String rotulo;
    private BigDecimal minAreaM2;
    private BigDecimal maxAreaM2;

    @JsonProperty("demandaUnitariakVa")
    private BigDecimal demandaUnitariakVa;

    private String normaOrigem;

    public AreaRangeResponse() {
    }

    public AreaRangeResponse(Long id, String faixa, String rotulo, BigDecimal minAreaM2,
                             BigDecimal maxAreaM2, BigDecimal demandaUnitariakVa, String normaOrigem) {
        this.id = id;
        this.faixa = faixa;
        this.rotulo = rotulo;
        this.minAreaM2 = minAreaM2;
        this.maxAreaM2 = maxAreaM2;
        this.demandaUnitariakVa = demandaUnitariakVa;
        this.normaOrigem = normaOrigem;
    }

    public static AreaRangeResponse from(AreaRangeReference ref) {
        if (ref == null) return null;
        return new AreaRangeResponse(
                ref.getId(),
                ref.getFaixa(),
                ref.getRotulo(),
                ref.getMinAreaM2(),
                ref.getMaxAreaM2(),
                ref.getDemandaUnitariaKva(),
                ref.getNormaOrigem()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFaixa() {
        return faixa;
    }

    public void setFaixa(String faixa) {
        this.faixa = faixa;
    }

    public String getRotulo() {
        return rotulo;
    }

    public void setRotulo(String rotulo) {
        this.rotulo = rotulo;
    }

    public BigDecimal getMinAreaM2() {
        return minAreaM2;
    }

    public void setMinAreaM2(BigDecimal minAreaM2) {
        this.minAreaM2 = minAreaM2;
    }

    public BigDecimal getMaxAreaM2() {
        return maxAreaM2;
    }

    public void setMaxAreaM2(BigDecimal maxAreaM2) {
        this.maxAreaM2 = maxAreaM2;
    }

    public BigDecimal getDemandaUnitariakVa() {
        return demandaUnitariakVa;
    }

    public void setDemandaUnitariakVa(BigDecimal demandaUnitariakVa) {
        this.demandaUnitariakVa = demandaUnitariakVa;
    }

    public String getNormaOrigem() {
        return normaOrigem;
    }

    public void setNormaOrigem(String normaOrigem) {
        this.normaOrigem = normaOrigem;
    }
}
