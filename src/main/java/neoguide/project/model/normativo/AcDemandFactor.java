package neoguide.project.model.normativo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "catalogo_fatores_ar_condicionado")
public class AcDemandFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "min_aparelhos", nullable = false)
    private Integer minAparelhos;

    @Column(name = "max_aparelhos", nullable = false)
    private Integer maxAparelhos;

    @Column(name = "faixa_aparelhos", nullable = false, length = 30)
    private String faixaAparelhos;

    @Column(name = "fator_comercial", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorComercial;

    @Column(name = "fator_residencial", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorResidencial;

    @Column(name = "fator_potencia", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorPotencia;

    @Column(name = "norma_origem", nullable = false, length = 60)
    private String normaOrigem;

    public AcDemandFactor() {
    }

    public AcDemandFactor(Integer minAparelhos, Integer maxAparelhos, String faixaAparelhos,
                          BigDecimal fatorComercial, BigDecimal fatorResidencial,
                          BigDecimal fatorPotencia, String normaOrigem) {
        this.minAparelhos = minAparelhos;
        this.maxAparelhos = maxAparelhos;
        this.faixaAparelhos = faixaAparelhos;
        this.fatorComercial = fatorComercial;
        this.fatorResidencial = fatorResidencial;
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

    public String getFaixaAparelhos() {
        return faixaAparelhos;
    }

    public void setFaixaAparelhos(String faixaAparelhos) {
        this.faixaAparelhos = faixaAparelhos;
    }

    public BigDecimal getFatorComercial() {
        return fatorComercial;
    }

    public void setFatorComercial(BigDecimal fatorComercial) {
        this.fatorComercial = fatorComercial;
    }

    public BigDecimal getFatorResidencial() {
        return fatorResidencial;
    }

    public void setFatorResidencial(BigDecimal fatorResidencial) {
        this.fatorResidencial = fatorResidencial;
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
        if (!(o instanceof AcDemandFactor that)) return false;
        return Objects.equals(id, that.id) || (Objects.equals(minAparelhos, that.minAparelhos) && Objects.equals(maxAparelhos, that.maxAparelhos));
    }

    @Override
    public int hashCode() {
        return Objects.hash(minAparelhos, maxAparelhos);
    }
}
