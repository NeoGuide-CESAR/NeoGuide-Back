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
@Table(name = "residential_typologies")
public class ResidentialTypology {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "catalogo_faixa_id")
    private Long catalogoFaixaId;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "area_util_m2", nullable = false, precision = 8, scale = 2)
    private BigDecimal areaUtilM2;

    @Column(name = "quantidade_unidades", nullable = false)
    private Integer quantidadeUnidades;

    @Column(name = "is_smart_studio", nullable = false)
    private boolean isSmartStudio = false;

    @Column(name = "demanda_unitaria_kva", precision = 6, scale = 2)
    private BigDecimal demandaUnitariaKva;

    @Column(name = "demanda_total_tipologia_kva", precision = 8, scale = 2)
    private BigDecimal demandaTotalTipologiaKva;

    public ResidentialTypology() {
    }

    public ResidentialTypology(Project project, String nome, BigDecimal areaUtilM2,
                               Integer quantidadeUnidades, boolean isSmartStudio,
                               BigDecimal demandaUnitariaKva, BigDecimal demandaTotalTipologiaKva) {
        this(project, null, nome, areaUtilM2, quantidadeUnidades, isSmartStudio, demandaUnitariaKva, demandaTotalTipologiaKva);
    }

    public ResidentialTypology(Project project, Long catalogoFaixaId, String nome, BigDecimal areaUtilM2,
                               Integer quantidadeUnidades, boolean isSmartStudio,
                               BigDecimal demandaUnitariaKva, BigDecimal demandaTotalTipologiaKva) {
        this.project = project;
        this.catalogoFaixaId = catalogoFaixaId;
        this.nome = nome;
        this.areaUtilM2 = areaUtilM2;
        this.quantidadeUnidades = quantidadeUnidades;
        this.isSmartStudio = isSmartStudio;
        this.demandaUnitariaKva = demandaUnitariaKva;
        this.demandaTotalTipologiaKva = demandaTotalTipologiaKva;
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

    public Long getCatalogoFaixaId() {
        return catalogoFaixaId;
    }

    public void setCatalogoFaixaId(Long catalogoFaixaId) {
        this.catalogoFaixaId = catalogoFaixaId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getAreaUtilM2() {
        return areaUtilM2;
    }

    public void setAreaUtilM2(BigDecimal areaUtilM2) {
        this.areaUtilM2 = areaUtilM2;
    }

    public Integer getQuantidadeUnidades() {
        return quantidadeUnidades;
    }

    public void setQuantidadeUnidades(Integer quantidadeUnidades) {
        this.quantidadeUnidades = quantidadeUnidades;
    }

    public boolean isSmartStudio() {
        return isSmartStudio;
    }

    public void setSmartStudio(boolean smartStudio) {
        isSmartStudio = smartStudio;
    }

    public BigDecimal getDemandaUnitariaKva() {
        return demandaUnitariaKva;
    }

    public void setDemandaUnitariaKva(BigDecimal demandaUnitariaKva) {
        this.demandaUnitariaKva = demandaUnitariaKva;
    }

    public BigDecimal getDemandaTotalTipologiaKva() {
        return demandaTotalTipologiaKva;
    }

    public void setDemandaTotalTipologiaKva(BigDecimal demandaTotalTipologiaKva) {
        this.demandaTotalTipologiaKva = demandaTotalTipologiaKva;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResidentialTypology that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
