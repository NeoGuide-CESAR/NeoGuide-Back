package neoguide.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "special_loads")
public class SpecialLoads {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private Project project;

    @Column(name = "qtd_pontos_ve")
    private Integer qtdPontosVe;

    @Column(name = "potencia_ve_kw", precision = 8, scale = 2)
    private BigDecimal potenciaEstacaoVeKw;

    @Column(name = "fator_simultaneidade_ve", precision = 4, scale = 2)
    private BigDecimal fatorSimultaneidadeVe;

    @Column(name = "demanda_ve_kva", precision = 8, scale = 2)
    private BigDecimal demandaVeKva;

    @Column(name = "potencia_bomba_incendio_cv", precision = 8, scale = 2)
    private BigDecimal potenciaBombaIncendioCv;

    @Column(name = "demanda_bomba_incendio_kva", precision = 8, scale = 2)
    private BigDecimal demandaBombaIncendioKva;

    @Column(name = "potencia_solar_inversor_kw", precision = 8, scale = 2)
    private BigDecimal potenciaSolarInversorKw;

    public SpecialLoads() {
    }

    public SpecialLoads(Project project, Integer qtdPontosVe, BigDecimal potenciaEstacaoVeKw,
                        BigDecimal fatorSimultaneidadeVe, BigDecimal demandaVeKva,
                        BigDecimal potenciaBombaIncendioCv, BigDecimal demandaBombaIncendioKva,
                        BigDecimal potenciaSolarInversorKw) {
        this.project = project;
        this.qtdPontosVe = qtdPontosVe;
        this.potenciaEstacaoVeKw = potenciaEstacaoVeKw;
        this.fatorSimultaneidadeVe = fatorSimultaneidadeVe;
        this.demandaVeKva = demandaVeKva;
        this.potenciaBombaIncendioCv = potenciaBombaIncendioCv;
        this.demandaBombaIncendioKva = demandaBombaIncendioKva;
        this.potenciaSolarInversorKw = potenciaSolarInversorKw;
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

    public Integer getQtdPontosVe() {
        return qtdPontosVe;
    }

    public void setQtdPontosVe(Integer qtdPontosVe) {
        this.qtdPontosVe = qtdPontosVe;
    }

    public BigDecimal getPotenciaEstacaoVeKw() {
        return potenciaEstacaoVeKw;
    }

    public void setPotenciaEstacaoVeKw(BigDecimal potenciaEstacaoVeKw) {
        this.potenciaEstacaoVeKw = potenciaEstacaoVeKw;
    }

    public BigDecimal getFatorSimultaneidadeVe() {
        return fatorSimultaneidadeVe;
    }

    public void setFatorSimultaneidadeVe(BigDecimal fatorSimultaneidadeVe) {
        this.fatorSimultaneidadeVe = fatorSimultaneidadeVe;
    }

    public BigDecimal getDemandaVeKva() {
        return demandaVeKva;
    }

    public void setDemandaVeKva(BigDecimal demandaVeKva) {
        this.demandaVeKva = demandaVeKva;
    }

    public BigDecimal getPotenciaBombaIncendioCv() {
        return potenciaBombaIncendioCv;
    }

    public void setPotenciaBombaIncendioCv(BigDecimal potenciaBombaIncendioCv) {
        this.potenciaBombaIncendioCv = potenciaBombaIncendioCv;
    }

    public BigDecimal getDemandaBombaIncendioKva() {
        return demandaBombaIncendioKva;
    }

    public void setDemandaBombaIncendioKva(BigDecimal demandaBombaIncendioKva) {
        this.demandaBombaIncendioKva = demandaBombaIncendioKva;
    }

    public BigDecimal getPotenciaSolarInversorKw() {
        return potenciaSolarInversorKw;
    }

    public void setPotenciaSolarInversorKw(BigDecimal potenciaSolarInversorKw) {
        this.potenciaSolarInversorKw = potenciaSolarInversorKw;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpecialLoads that = (SpecialLoads) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "SpecialLoads{" +
                "id=" + id +
                ", qtdPontosVe=" + qtdPontosVe +
                ", potenciaEstacaoVeKw=" + potenciaEstacaoVeKw +
                ", fatorSimultaneidadeVe=" + fatorSimultaneidadeVe +
                ", demandaVeKva=" + demandaVeKva +
                ", potenciaBombaIncendioCv=" + potenciaBombaIncendioCv +
                ", demandaBombaIncendioKva=" + demandaBombaIncendioKva +
                ", potenciaSolarInversorKw=" + potenciaSolarInversorKw +
                '}';
    }
}
