package neoguide.project.model.normativo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "catalogo_fatores_boilers")
public class BoilerDemandFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantidade_boilers", nullable = false)
    private Integer quantidadeBoilers;

    @Column(name = "fator_demanda", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorDemanda;

    @Column(name = "fator_potencia", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorPotencia;

    @Column(name = "norma_origem", nullable = false, length = 60)
    private String normaOrigem;

    public BoilerDemandFactor() {
    }

    public BoilerDemandFactor(Integer quantidadeBoilers, BigDecimal fatorDemanda,
                              BigDecimal fatorPotencia, String normaOrigem) {
        this.quantidadeBoilers = quantidadeBoilers;
        this.fatorDemanda = fatorDemanda;
        this.fatorPotencia = fatorPotencia;
        this.normaOrigem = normaOrigem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantidadeBoilers() {
        return quantidadeBoilers;
    }

    public void setQuantidadeBoilers(Integer quantidadeBoilers) {
        this.quantidadeBoilers = quantidadeBoilers;
    }

    public BigDecimal getFatorDemanda() {
        return fatorDemanda;
    }

    public void setFatorDemanda(BigDecimal fatorDemanda) {
        this.fatorDemanda = fatorDemanda;
    }

    public BigDecimal getFatorPotencia() {
        return fatorPotencia;
    }

    public void setFatorPotencia(BigDecimal fatorPotencia) {
        this.fatorPotencia = fatorPotencia;
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
        if (!(o instanceof BoilerDemandFactor that)) return false;
        return Objects.equals(id, that.id) || Objects.equals(quantidadeBoilers, that.quantidadeBoilers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantidadeBoilers);
    }
}
