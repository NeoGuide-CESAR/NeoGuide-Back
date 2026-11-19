package neoguide.project.dto.catalogo;

import neoguide.project.model.normativo.MinimumOutletsReference;
import java.math.BigDecimal;

public class MinimumOutletsCatalogResponse {

    private Long id;
    private BigDecimal minAreaM2;
    private BigDecimal maxAreaM2;
    private String faixaDescricao;
    private Integer minTug100w;
    private Integer minTug600w;
    private String normaOrigem;

    public MinimumOutletsCatalogResponse() {
    }

    public MinimumOutletsCatalogResponse(Long id, BigDecimal minAreaM2, BigDecimal maxAreaM2,
                                         String faixaDescricao, Integer minTug100w,
                                         Integer minTug600w, String normaOrigem) {
        this.id = id;
        this.minAreaM2 = minAreaM2;
        this.maxAreaM2 = maxAreaM2;
        this.faixaDescricao = faixaDescricao;
        this.minTug100w = minTug100w;
        this.minTug600w = minTug600w;
        this.normaOrigem = normaOrigem;
    }

    public static MinimumOutletsCatalogResponse from(MinimumOutletsReference entity) {
        if (entity == null) {
            return null;
        }
        return new MinimumOutletsCatalogResponse(
                entity.getId(),
                entity.getMinAreaM2(),
                entity.getMaxAreaM2(),
                entity.getFaixaDescricao(),
                entity.getMinTug100w(),
                entity.getMinTug600w(),
                entity.getNormaOrigem()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getFaixaDescricao() {
        return faixaDescricao;
    }

    public void setFaixaDescricao(String faixaDescricao) {
        this.faixaDescricao = faixaDescricao;
    }

    public Integer getMinTug100w() {
        return minTug100w;
    }

    public void setMinTug100w(Integer minTug100w) {
        this.minTug100w = minTug100w;
    }

    public Integer getMinTug600w() {
        return minTug600w;
    }

    public void setMinTug600w(Integer minTug600w) {
        this.minTug600w = minTug600w;
    }

    public String getNormaOrigem() {
        return normaOrigem;
    }

    public void setNormaOrigem(String normaOrigem) {
        this.normaOrigem = normaOrigem;
    }
}
