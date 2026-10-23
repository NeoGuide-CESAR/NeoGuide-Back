package neoguide.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "common_area_loads")
public class CommonAreaLoad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 20)
    private String origem; // "CATALOGO" ou "PERSONALIZADO"

    @Column(nullable = false, length = 30)
    private String submodulo; // "ILUMINACAO_TUG", "CLIMATIZACAO", "MOTORES", "COCCAO", "AQUECIMENTO"

    @Column(name = "catalogo_motor_id")
    private Long catalogoMotorId;

    @Column(name = "catalogo_clima_id")
    private Long catalogoClimaId;

    @Column(name = "catalogo_equipamento_id")
    private Long catalogoEquipamentoId;

    @Column(name = "tipo_equipamento", nullable = false, length = 100)
    private String tipoEquipamento;

    @Column(length = 255)
    private String descricao;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "potencia_nominal_kw", precision = 8, scale = 2)
    private BigDecimal potenciaNominalKw;

    @Column(name = "fator_potencia", precision = 4, scale = 2)
    private BigDecimal fatorPotencia;

    @Column(name = "fator_demanda", precision = 4, scale = 2)
    private BigDecimal fatorDemanda;

    @Column(name = "demanda_calculada_kva", precision = 8, scale = 2)
    private BigDecimal demandaCalculadaKva;

    @Column(name = "is_reserva", nullable = false)
    private boolean isReserva = false;

    public CommonAreaLoad() {
    }

    public CommonAreaLoad(Project project, String origem, String submodulo, Long catalogoMotorId,
                          Long catalogoClimaId, Long catalogoEquipamentoId, String tipoEquipamento,
                          String descricao, Integer quantidade, BigDecimal potenciaNominalKw,
                          BigDecimal fatorPotencia, BigDecimal fatorDemanda,
                          BigDecimal demandaCalculadaKva, boolean isReserva) {
        this.project = project;
        this.origem = origem;
        this.submodulo = submodulo;
        this.catalogoMotorId = catalogoMotorId;
        this.catalogoClimaId = catalogoClimaId;
        this.catalogoEquipamentoId = catalogoEquipamentoId;
        this.tipoEquipamento = tipoEquipamento;
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.potenciaNominalKw = potenciaNominalKw;
        this.fatorPotencia = fatorPotencia;
        this.fatorDemanda = fatorDemanda;
        this.demandaCalculadaKva = demandaCalculadaKva;
        this.isReserva = isReserva;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getSubmodulo() {
        return submodulo;
    }

    public void setSubmodulo(String submodulo) {
        this.submodulo = submodulo;
    }

    public Long getCatalogoMotorId() {
        return catalogoMotorId;
    }

    public void setCatalogoMotorId(Long catalogoMotorId) {
        this.catalogoMotorId = catalogoMotorId;
    }

    public Long getCatalogoClimaId() {
        return catalogoClimaId;
    }

    public void setCatalogoClimaId(Long catalogoClimaId) {
        this.catalogoClimaId = catalogoClimaId;
    }

    public Long getCatalogoEquipamentoId() {
        return catalogoEquipamentoId;
    }

    public void setCatalogoEquipamentoId(Long catalogoEquipamentoId) {
        this.catalogoEquipamentoId = catalogoEquipamentoId;
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

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPotenciaNominalKw() {
        return potenciaNominalKw;
    }

    public void setPotenciaNominalKw(BigDecimal potenciaNominalKw) {
        this.potenciaNominalKw = potenciaNominalKw;
    }

    public BigDecimal getFatorPotencia() {
        return fatorPotencia;
    }

    public void setFatorPotencia(BigDecimal fatorPotencia) {
        this.fatorPotencia = fatorPotencia;
    }

    public BigDecimal getFatorDemanda() {
        return fatorDemanda;
    }

    public void setFatorDemanda(BigDecimal fatorDemanda) {
        this.fatorDemanda = fatorDemanda;
    }

    public BigDecimal getDemandaCalculadaKva() {
        return demandaCalculadaKva;
    }

    public void setDemandaCalculadaKva(BigDecimal demandaCalculadaKva) {
        this.demandaCalculadaKva = demandaCalculadaKva;
    }

    public boolean isReserva() {
        return isReserva;
    }

    public void setReserva(boolean reserva) {
        isReserva = reserva;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommonAreaLoad that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
