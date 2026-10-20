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
@Table(name = "commercial_units")
public class CommercialUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 100)
    private String identificacao;

    @Column(name = "atividade_comercial", nullable = false, length = 100)
    private String atividadeComercial;

    @Column(name = "area_m2", precision = 10, scale = 2)
    private BigDecimal areaM2;

    @Column(name = "potencia_instalada_kw", nullable = false, precision = 10, scale = 2)
    private BigDecimal potenciaInstaladaKw;

    @Column(name = "fator_demanda", nullable = false, precision = 5, scale = 4)
    private BigDecimal fatorDemanda;

    @Column(name = "demanda_calculada_kva", nullable = false, precision = 10, scale = 2)
    private BigDecimal demandaCalculadaKva;

    public CommercialUnit() {
    }

    public CommercialUnit(Project project, String identificacao, String atividadeComercial,
                          BigDecimal areaM2, BigDecimal potenciaInstaladaKw,
                          BigDecimal fatorDemanda, BigDecimal demandaCalculadaKva) {
        this.project = project;
        this.identificacao = identificacao;
        this.atividadeComercial = atividadeComercial;
        this.areaM2 = areaM2;
        this.potenciaInstaladaKw = potenciaInstaladaKw;
        this.fatorDemanda = fatorDemanda;
        this.demandaCalculadaKva = demandaCalculadaKva;
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

    public String getIdentificacao() {
        return identificacao;
    }

    public void setIdentificacao(String identificacao) {
        this.identificacao = identificacao;
    }

    public String getAtividadeComercial() {
        return atividadeComercial;
    }

    public void setAtividadeComercial(String atividadeComercial) {
        this.atividadeComercial = atividadeComercial;
    }

    public BigDecimal getAreaM2() {
        return areaM2;
    }

    public void setAreaM2(BigDecimal areaM2) {
        this.areaM2 = areaM2;
    }

    public BigDecimal getPotenciaInstaladaKw() {
        return potenciaInstaladaKw;
    }

    public void setPotenciaInstaladaKw(BigDecimal potenciaInstaladaKw) {
        this.potenciaInstaladaKw = potenciaInstaladaKw;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommercialUnit that = (CommercialUnit) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "CommercialUnit{" +
                "id=" + id +
                ", identificacao='" + identificacao + '\'' +
                ", atividadeComercial='" + atividadeComercial + '\'' +
                ", areaM2=" + areaM2 +
                ", potenciaInstaladaKw=" + potenciaInstaladaKw +
                ", fatorDemanda=" + fatorDemanda +
                ", demandaCalculadaKva=" + demandaCalculadaKva +
                '}';
    }
}
