package neoguide.project.model.normativo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "catalogo_fatores_seguranca")
public class SafetyFactorReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "min_dr_kva", nullable = false, precision = 8, scale = 2)
    private BigDecimal minDrKva;

    @Column(name = "max_dr_kva", nullable = false, precision = 8, scale = 2)
    private BigDecimal maxDrKva;

    @Column(name = "fator_seguranca_fr", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorSegurancaFr;

    @Column(name = "norma_origem", nullable = false, length = 60)
    private String normaOrigem;

    public SafetyFactorReference() {
    }

    public SafetyFactorReference(BigDecimal minDrKva, BigDecimal maxDrKva, BigDecimal fatorSegurancaFr,
                                 String normaOrigem) {
        this.minDrKva = minDrKva;
        this.maxDrKva = maxDrKva;
        this.fatorSegurancaFr = fatorSegurancaFr;
        this.normaOrigem = normaOrigem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMinDrKva() {
        return minDrKva;
    }

    public void setMinDrKva(BigDecimal minDrKva) {
        this.minDrKva = minDrKva;
    }

    public BigDecimal getMaxDrKva() {
        return maxDrKva;
    }

    public void setMaxDrKva(BigDecimal maxDrKva) {
        this.maxDrKva = maxDrKva;
    }

    public BigDecimal getFatorSegurancaFr() {
        return fatorSegurancaFr;
    }

    public void setFatorSegurancaFr(BigDecimal fatorSegurancaFr) {
        this.fatorSegurancaFr = fatorSegurancaFr;
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
        if (!(o instanceof SafetyFactorReference that)) return false;
        return Objects.equals(id, that.id) || (Objects.equals(minDrKva, that.minDrKva) && Objects.equals(maxDrKva, that.maxDrKva));
    }

    @Override
    public int hashCode() {
        return Objects.hash(minDrKva, maxDrKva);
    }
}
