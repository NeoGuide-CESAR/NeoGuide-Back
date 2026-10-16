package neoguide.project.dto;

import java.math.BigDecimal;

public class ResidentialTypologyItemRequest {
    private String nome;
    private Long catalogoFaixaId;
    private BigDecimal areaUtilM2;
    private Integer quantidadeUnidades;
    private boolean isSmartStudio;

    public ResidentialTypologyItemRequest() {
    }

    public ResidentialTypologyItemRequest(String nome, Long catalogoFaixaId, BigDecimal areaUtilM2,
                                          Integer quantidadeUnidades, boolean isSmartStudio) {
        this.nome = nome;
        this.catalogoFaixaId = catalogoFaixaId;
        this.areaUtilM2 = areaUtilM2;
        this.quantidadeUnidades = quantidadeUnidades;
        this.isSmartStudio = isSmartStudio;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Long getCatalogoFaixaId() {
        return catalogoFaixaId;
    }

    public void setCatalogoFaixaId(Long catalogoFaixaId) {
        this.catalogoFaixaId = catalogoFaixaId;
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
        return isSmartStudio;
    }

    public boolean getIsSmartStudio() {
        return isSmartStudio;
    }

    public void setSmartStudio(boolean smartStudio) {
        this.isSmartStudio = smartStudio;
    }
}
