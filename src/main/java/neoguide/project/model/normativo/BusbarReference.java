package neoguide.project.model.normativo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "catalogo_barramentos_qdg")
public class BusbarReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "max_demanda_kva", nullable = false, precision = 6, scale = 2)
    private BigDecimal maxDemandaKva;

    @Column(name = "barra_dimensao_mm", nullable = false, length = 30)
    private String barraDimensaoMm;

    @Column(name = "barra_dimensao_pol", length = 30)
    private String barraDimensaoPol;

    @Column(name = "corrente_nominal_a", nullable = false)
    private Integer correnteNominalA;

    @Column(name = "norma_origem", nullable = false, length = 60)
    private String normaOrigem;

    public BusbarReference() {
    }

    public BusbarReference(BigDecimal maxDemandaKva, String barraDimensaoMm, String barraDimensaoPol,
                           Integer correnteNominalA, String normaOrigem) {
        this.maxDemandaKva = maxDemandaKva;
        this.barraDimensaoMm = barraDimensaoMm;
        this.barraDimensaoPol = barraDimensaoPol;
        this.correnteNominalA = correnteNominalA;
        this.normaOrigem = normaOrigem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMaxDemandaKva() {
        return maxDemandaKva;
    }

    public void setMaxDemandaKva(BigDecimal maxDemandaKva) {
        this.maxDemandaKva = maxDemandaKva;
    }

    public String getBarraDimensaoMm() {
        return barraDimensaoMm;
    }

    public void setBarraDimensaoMm(String barraDimensaoMm) {
        this.barraDimensaoMm = barraDimensaoMm;
    }

    public String getBarraDimensaoPol() {
        return barraDimensaoPol;
    }

    public void setBarraDimensaoPol(String barraDimensaoPol) {
        this.barraDimensaoPol = barraDimensaoPol;
    }

    public Integer getCorrenteNominalA() {
        return correnteNominalA;
    }

    public void setCorrenteNominalA(Integer correnteNominalA) {
        this.correnteNominalA = correnteNominalA;
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
        if (!(o instanceof BusbarReference that)) return false;
        return Objects.equals(id, that.id) || Objects.equals(maxDemandaKva, that.maxDemandaKva);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxDemandaKva);
    }
}
