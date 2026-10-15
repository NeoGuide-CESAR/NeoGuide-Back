package neoguide.project.model.normativo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "catalogo_faixas_area")
public class AreaRangeReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "faixa", nullable = false, unique = true, length = 50)
    private String faixa;

    @Column(name = "rotulo", nullable = false, length = 50)
    private String rotulo;

    @Column(name = "min_area_m2", nullable = false, precision = 8, scale = 2)
    private BigDecimal minAreaM2;

    @Column(name = "max_area_m2", nullable = false, precision = 8, scale = 2)
    private BigDecimal maxAreaM2;

    @Column(name = "demanda_unitaria_kva", nullable = false, precision = 6, scale = 2)
    private BigDecimal demandaUnitariaKva;

    @Column(name = "norma_origem", nullable = false, length = 60)
    private String normaOrigem;

    public AreaRangeReference() {
    }

    public AreaRangeReference(String faixa, String rotulo, BigDecimal minAreaM2, BigDecimal maxAreaM2,
                              BigDecimal demandaUnitariaKva, String normaOrigem) {
        this.faixa = faixa;
        this.rotulo = rotulo;
        this.minAreaM2 = minAreaM2;
        this.maxAreaM2 = maxAreaM2;
        this.demandaUnitariaKva = demandaUnitariaKva;
        this.normaOrigem = normaOrigem;
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

    public BigDecimal getDemandaUnitariaKva() {
        return demandaUnitariaKva;
    }

    public void setDemandaUnitariaKva(BigDecimal demandaUnitariaKva) {
        this.demandaUnitariaKva = demandaUnitariaKva;
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
        if (!(o instanceof AreaRangeReference that)) return false;
        return Objects.equals(id, that.id) || Objects.equals(faixa, that.faixa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(faixa);
    }
}
