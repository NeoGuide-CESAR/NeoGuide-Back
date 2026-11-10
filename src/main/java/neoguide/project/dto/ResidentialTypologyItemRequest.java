package neoguide.project.dto;

import java.math.BigDecimal;

public class ResidentialTypologyItemRequest {
    private BigDecimal areaUtilM2;
    private Integer quantidadeUnidades;
    private Boolean isSmartStudio = false;

    public ResidentialTypologyItemRequest() {
    }

    public ResidentialTypologyItemRequest(BigDecimal areaUtilM2, Integer quantidadeUnidades) {
        this(areaUtilM2, quantidadeUnidades, false);
    }

    public ResidentialTypologyItemRequest(BigDecimal areaUtilM2, Integer quantidadeUnidades, Boolean isSmartStudio) {
        this.areaUtilM2 = areaUtilM2;
        this.quantidadeUnidades = quantidadeUnidades;
        this.isSmartStudio = isSmartStudio != null ? isSmartStudio : false;
    }

    public BigDecimal getAreaUtilM2() {
        return areaUtilM2;
    }

    public void setAreaUtilM2(BigDecimal areaUtilM2) {
        this.areaUtilM2 = areaUtilM2;
    }

    public Integer getQuantidadeUnidades() {
        return quantidadeUnidades;
    }

    public void setQuantidadeUnidades(Integer quantidadeUnidades) {
        this.quantidadeUnidades = quantidadeUnidades;
    }

    public boolean isSmartStudio() {
        return Boolean.TRUE.equals(isSmartStudio);
    }

    public void setSmartStudio(Boolean smartStudio) {
        this.isSmartStudio = smartStudio != null ? smartStudio : false;
    }

    public void setIsSmartStudio(Boolean isSmartStudio) {
        this.isSmartStudio = isSmartStudio != null ? isSmartStudio : false;
    }
}
