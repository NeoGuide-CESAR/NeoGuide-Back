package neoguide.project.model.normativo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "catalogo_fatores_cooktops")
public class CooktopDemandFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantidade_aparelhos", nullable = false)
    private Integer quantidadeAparelhos;

    @Column(name = "fator_demanda", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorDemanda;

    @Column(name = "fator_potencia", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorPotencia;

    @Column(name = "norma_origem", nullable = false, length = 60)
    private String normaOrigem;

    public CooktopDemandFactor() {
    }

    public CooktopDemandFactor(Integer quantidadeAparelhos, BigDecimal fatorDemanda,
                               BigDecimal fatorPotencia, String normaOrigem) {
        this.quantidadeAparelhos = quantidadeAparelhos;
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

    public Integer getQuantidadeAparelhos() {
        return quantidadeAparelhos;
    }

    public void setQuantidadeAparelhos(Integer quantidadeAparelhos) {
        this.quantidadeAparelhos = quantidadeAparelhos;
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
        if (!(o instanceof CooktopDemandFactor that)) return false;
        return Objects.equals(id, that.id) || Objects.equals(quantidadeAparelhos, that.quantidadeAparelhos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantidadeAparelhos);
    }
}
