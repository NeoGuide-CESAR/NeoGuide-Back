package neoguide.project.model.normativo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "catalogo_fatores_eletrodomesticos")
public class AppliancesDemandFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "min_aparelhos", nullable = false)
    private Integer minAparelhos;

    @Column(name = "max_aparelhos", nullable = false)
    private Integer maxAparelhos;

    @Column(name = "descricao_faixa", length = 50)
    private String descricaoFaixa;

    @Column(name = "fator_demanda", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorDemanda;

    @Column(name = "fator_potencia", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorPotencia;

    @Column(name = "norma_origem", nullable = false, length = 60)
    private String normaOrigem;

    public AppliancesDemandFactor() {
    }

    public AppliancesDemandFactor(Integer minAparelhos, Integer maxAparelhos, String descricaoFaixa,
                                  BigDecimal fatorDemanda, BigDecimal fatorPotencia, String normaOrigem) {
        this.minAparelhos = minAparelhos;
        this.maxAparelhos = maxAparelhos;
        this.descricaoFaixa = descricaoFaixa;
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

    public Integer getMinAparelhos() {
        return minAparelhos;
    }

    public void setMinAparelhos(Integer minAparelhos) {
        this.minAparelhos = minAparelhos;
    }

    public Integer getMaxAparelhos() {
        return maxAparelhos;
    }

    public void setMaxAparelhos(Integer maxAparelhos) {
        this.maxAparelhos = maxAparelhos;
    }

    public String getDescricaoFaixa() {
        return descricaoFaixa;
    }

    public void setDescricaoFaixa(String descricaoFaixa) {
        this.descricaoFaixa = descricaoFaixa;
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
        if (!(o instanceof AppliancesDemandFactor that)) return false;
        return Objects.equals(id, that.id) || (Objects.equals(minAparelhos, that.minAparelhos) && Objects.equals(maxAparelhos, that.maxAparelhos));
    }

    @Override
    public int hashCode() {
        return Objects.hash(minAparelhos, maxAparelhos);
    }
}
