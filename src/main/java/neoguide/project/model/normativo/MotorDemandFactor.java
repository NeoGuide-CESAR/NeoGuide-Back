package neoguide.project.model.normativo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "catalogo_fatores_motores")
public class MotorDemandFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_enquadramento", nullable = false, length = 50)
    private String tipoEnquadramento;

    @Column(name = "descricao", nullable = false, length = 150)
    private String descricao;

    @Column(name = "fator_demanda", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorDemanda;

    @Column(name = "norma_origem", nullable = false, length = 60)
    private String normaOrigem;

    public MotorDemandFactor() {
    }

    public MotorDemandFactor(String tipoEnquadramento, String descricao,
                             BigDecimal fatorDemanda, String normaOrigem) {
        this.tipoEnquadramento = tipoEnquadramento;
        this.descricao = descricao;
        this.fatorDemanda = fatorDemanda;
        this.normaOrigem = normaOrigem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipoEnquadramento() {
        return tipoEnquadramento;
    }

    public void setTipoEnquadramento(String tipoEnquadramento) {
        this.tipoEnquadramento = tipoEnquadramento;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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
        if (!(o instanceof MotorDemandFactor that)) return false;
        return Objects.equals(id, that.id) || Objects.equals(tipoEnquadramento, that.tipoEnquadramento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipoEnquadramento);
    }
}
