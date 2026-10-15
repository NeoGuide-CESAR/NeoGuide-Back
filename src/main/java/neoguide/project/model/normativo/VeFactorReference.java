package neoguide.project.model.normativo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "catalogo_fatores_ve")
public class VeFactorReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "max_vagas", nullable = false)
    private Integer maxVagas;

    @Column(name = "fator_simultaneidade", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorSimultaneidade;

    @Column(name = "faixa_descricao", length = 50)
    private String faixaDescricao;

    @Column(name = "norma_origem", nullable = false, length = 60)
    private String normaOrigem;

    public VeFactorReference() {
    }

    public VeFactorReference(Integer maxVagas, BigDecimal fatorSimultaneidade, String faixaDescricao,
                             String normaOrigem) {
        this.maxVagas = maxVagas;
        this.fatorSimultaneidade = fatorSimultaneidade;
        this.faixaDescricao = faixaDescricao;
        this.normaOrigem = normaOrigem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMaxVagas() {
        return maxVagas;
    }

    public void setMaxVagas(Integer maxVagas) {
        this.maxVagas = maxVagas;
    }

    public BigDecimal getFatorSimultaneidade() {
        return fatorSimultaneidade;
    }

    public void setFatorSimultaneidade(BigDecimal fatorSimultaneidade) {
        this.fatorSimultaneidade = fatorSimultaneidade;
    }

    public String getFaixaDescricao() {
        return faixaDescricao;
    }

    public void setFaixaDescricao(String faixaDescricao) {
        this.faixaDescricao = faixaDescricao;
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
        if (!(o instanceof VeFactorReference that)) return false;
        return Objects.equals(id, that.id) || Objects.equals(maxVagas, that.maxVagas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxVagas);
    }
}
