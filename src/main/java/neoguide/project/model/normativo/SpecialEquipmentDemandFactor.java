package neoguide.project.model.normativo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "catalogo_fatores_especiais")
public class SpecialEquipmentDemandFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_equipamento", nullable = false, length = 50)
    private String tipoEquipamento;

    @Column(name = "descricao", nullable = false, length = 150)
    private String descricao;

    @Column(name = "fator_demanda", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorDemanda;

    @Column(name = "fator_potencia", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorPotencia;

    @Column(name = "norma_origem", nullable = false, length = 60)
    private String normaOrigem;

    public SpecialEquipmentDemandFactor() {
    }

    public SpecialEquipmentDemandFactor(String tipoEquipamento, String descricao,
                                        BigDecimal fatorDemanda, BigDecimal fatorPotencia,
                                        String normaOrigem) {
        this.tipoEquipamento = tipoEquipamento;
        this.descricao = descricao;
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

    public String getTipoEquipamento() {
        return tipoEquipamento;
    }

    public void setTipoEquipamento(String tipoEquipamento) {
        this.tipoEquipamento = tipoEquipamento;
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
        if (!(o instanceof SpecialEquipmentDemandFactor that)) return false;
        return Objects.equals(id, that.id) || Objects.equals(tipoEquipamento, that.tipoEquipamento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipoEquipamento);
    }
}
