package neoguide.project.model.normativo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "catalogo_tomadas_minimas")
public class MinimumOutletsReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "min_area_m2", nullable = false, precision = 8, scale = 2)
    private BigDecimal minAreaM2;

    @Column(name = "max_area_m2", nullable = false, precision = 8, scale = 2)
    private BigDecimal maxAreaM2;

    @Column(name = "faixa_descricao", length = 50)
    private String faixaDescricao;

    @Column(name = "min_tug_100w", nullable = false)
    private Integer minTug100w;

    @Column(name = "min_tug_600w", nullable = false)
    private Integer minTug600w;

    @Column(name = "norma_origem", nullable = false, length = 60)
    private String normaOrigem;

    public MinimumOutletsReference() {
    }

    public MinimumOutletsReference(BigDecimal minAreaM2, BigDecimal maxAreaM2, String faixaDescricao,
                                   Integer minTug100w, Integer minTug600w, String normaOrigem) {
        this.minAreaM2 = minAreaM2;
        this.maxAreaM2 = maxAreaM2;
        this.faixaDescricao = faixaDescricao;
        this.minTug100w = minTug100w;
        this.minTug600w = minTug600w;
        this.normaOrigem = normaOrigem;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MinimumOutletsReference that)) return false;
        return Objects.equals(id, that.id) || (Objects.equals(minAreaM2, that.minAreaM2) && Objects.equals(maxAreaM2, that.maxAreaM2));
    }

    @Override
    public int hashCode() {
        return Objects.hash(minAreaM2, maxAreaM2);
    }
}
