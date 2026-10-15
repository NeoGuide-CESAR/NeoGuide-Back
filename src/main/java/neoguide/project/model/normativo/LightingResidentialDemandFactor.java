package neoguide.project.model.normativo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "catalogo_fatores_iluminacao_res")
public class LightingResidentialDemandFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "min_potencia_kw", nullable = false, precision = 6, scale = 2)
    private BigDecimal minPotenciaKw;

    @Column(name = "max_potencia_kw", nullable = false, precision = 6, scale = 2)
    private BigDecimal maxPotenciaKw;

    @Column(name = "fator_demanda", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorDemanda;

    @Column(name = "norma_origem", nullable = false, length = 60)
    private String normaOrigem;

    public LightingResidentialDemandFactor() {
    }

    public LightingResidentialDemandFactor(BigDecimal minPotenciaKw, BigDecimal maxPotenciaKw,
                                          BigDecimal fatorDemanda, String normaOrigem) {
        this.minPotenciaKw = minPotenciaKw;
        this.maxPotenciaKw = maxPotenciaKw;
        this.fatorDemanda = fatorDemanda;
        this.normaOrigem = normaOrigem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMinPotenciaKw() {
        return minPotenciaKw;
    }

    public void setMinPotenciaKw(BigDecimal minPotenciaKw) {
        this.minPotenciaKw = minPotenciaKw;
    }

    public BigDecimal getMaxPotenciaKw() {
        return maxPotenciaKw;
    }

    public void setMaxPotenciaKw(BigDecimal maxPotenciaKw) {
        this.maxPotenciaKw = maxPotenciaKw;
    }

    public BigDecimal getFatorDemanda() {
        return fatorDemanda;
    }

    public void setFatorDemanda(BigDecimal fatorDemanda) {
        this.fatorDemanda = fatorDemanda;
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
        if (!(o instanceof LightingResidentialDemandFactor that)) return false;
        return Objects.equals(id, that.id) || (Objects.equals(minPotenciaKw, that.minPotenciaKw) && Objects.equals(maxPotenciaKw, that.maxPotenciaKw));
    }

    @Override
    public int hashCode() {
        return Objects.hash(minPotenciaKw, maxPotenciaKw);
    }
}
