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
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "demand_results_motor")
public class DemandResults {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private Project project;

    @Column(name = "demanda_residencial_kva", nullable = false, precision = 10, scale = 2)
    private BigDecimal demandaResidencialKva;

    @Column(name = "demanda_servico_kva", nullable = false, precision = 10, scale = 2)
    private BigDecimal demandaServicoKva;

    @Column(name = "demanda_comercial_kva", nullable = false, precision = 10, scale = 2)
    private BigDecimal demandaComercialKva;

    @Column(name = "demanda_ve_kva", nullable = false, precision = 10, scale = 2)
    private BigDecimal demandaVeKva;

    @Column(name = "demanda_incendio_kva", nullable = false, precision = 10, scale = 2)
    private BigDecimal demandaIncendioKva;

    @Column(name = "demanda_global_kva", nullable = false, precision = 10, scale = 2)
    private BigDecimal demandaGlobalKva;

    @Column(name = "demanda_qdg_kva", nullable = false, precision = 10, scale = 2)
    private BigDecimal demandaQdgKva;

    @Column(name = "tipo_fornecimento", nullable = false, length = 50)
    private String tipoFornecimento;

    @Column(name = "disjuntor_geral_a", nullable = false)
    private Integer disjuntorGeralA;

    @Column(name = "cabo_fase_mm2", nullable = false, length = 100)
    private String caboFaseMm2;

    @Column(name = "cabo_neutro_mm2", nullable = false, length = 100)
    private String caboNeutroMm2;

    @Column(name = "barramento_cobre", nullable = false, length = 100)
    private String barramentoCobre;

    @Column(name = "capacidade_barramento_a", nullable = false)
    private Integer capacidadeBarramentoA;

    @Column(name = "memoria_calculo_json", columnDefinition = "TEXT")
    private String memoriaCalculoJson;

    @Column(name = "calculado_em", nullable = false)
    private LocalDateTime calculadoEm;

    public DemandResults() {
        this.calculadoEm = LocalDateTime.now();
    }

    public DemandResults(Project project, BigDecimal demandaResidencialKva, BigDecimal demandaServicoKva,
                         BigDecimal demandaComercialKva, BigDecimal demandaVeKva, BigDecimal demandaIncendioKva,
                         BigDecimal demandaGlobalKva, BigDecimal demandaQdgKva, String tipoFornecimento,
                         Integer disjuntorGeralA, String caboFaseMm2, String caboNeutroMm2,
                         String barramentoCobre, Integer capacidadeBarramentoA,
                         String memoriaCalculoJson, LocalDateTime calculadoEm) {
        this.project = project;
        this.demandaResidencialKva = demandaResidencialKva;
        this.demandaServicoKva = demandaServicoKva;
        this.demandaComercialKva = demandaComercialKva;
        this.demandaVeKva = demandaVeKva;
        this.demandaIncendioKva = demandaIncendioKva;
        this.demandaGlobalKva = demandaGlobalKva;
        this.demandaQdgKva = demandaQdgKva;
        this.tipoFornecimento = tipoFornecimento;
        this.disjuntorGeralA = disjuntorGeralA;
        this.caboFaseMm2 = caboFaseMm2;
        this.caboNeutroMm2 = caboNeutroMm2;
        this.barramentoCobre = barramentoCobre;
        this.capacidadeBarramentoA = capacidadeBarramentoA;
        this.memoriaCalculoJson = memoriaCalculoJson;
        this.calculadoEm = calculadoEm != null ? calculadoEm : LocalDateTime.now();
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

    public BigDecimal getDemandaResidencialKva() {
        return demandaResidencialKva;
    }

    public void setDemandaResidencialKva(BigDecimal demandaResidencialKva) {
        this.demandaResidencialKva = demandaResidencialKva;
    }

    public BigDecimal getDemandaServicoKva() {
        return demandaServicoKva;
    }

    public void setDemandaServicoKva(BigDecimal demandaServicoKva) {
        this.demandaServicoKva = demandaServicoKva;
    }

    public BigDecimal getDemandaComercialKva() {
        return demandaComercialKva;
    }

    public void setDemandaComercialKva(BigDecimal demandaComercialKva) {
        this.demandaComercialKva = demandaComercialKva;
    }

    public BigDecimal getDemandaVeKva() {
        return demandaVeKva;
    }

    public void setDemandaVeKva(BigDecimal demandaVeKva) {
        this.demandaVeKva = demandaVeKva;
    }

    public BigDecimal getDemandaIncendioKva() {
        return demandaIncendioKva;
    }

    public void setDemandaIncendioKva(BigDecimal demandaIncendioKva) {
        this.demandaIncendioKva = demandaIncendioKva;
    }

    public BigDecimal getDemandaGlobalKva() {
        return demandaGlobalKva;
    }

    public void setDemandaGlobalKva(BigDecimal demandaGlobalKva) {
        this.demandaGlobalKva = demandaGlobalKva;
    }

    public BigDecimal getDemandaQdgKva() {
        return demandaQdgKva;
    }

    public void setDemandaQdgKva(BigDecimal demandaQdgKva) {
        this.demandaQdgKva = demandaQdgKva;
    }

    public String getTipoFornecimento() {
        return tipoFornecimento;
    }

    public void setTipoFornecimento(String tipoFornecimento) {
        this.tipoFornecimento = tipoFornecimento;
    }

    public Integer getDisjuntorGeralA() {
        return disjuntorGeralA;
    }

    public void setDisjuntorGeralA(Integer disjuntorGeralA) {
        this.disjuntorGeralA = disjuntorGeralA;
    }

    public String getCaboFaseMm2() {
        return caboFaseMm2;
    }

    public void setCaboFaseMm2(String caboFaseMm2) {
        this.caboFaseMm2 = caboFaseMm2;
    }

    public String getCaboNeutroMm2() {
        return caboNeutroMm2;
    }

    public void setCaboNeutroMm2(String caboNeutroMm2) {
        this.caboNeutroMm2 = caboNeutroMm2;
    }

    public String getBarramentoCobre() {
        return barramentoCobre;
    }

    public void setBarramentoCobre(String barramentoCobre) {
        this.barramentoCobre = barramentoCobre;
    }

    public Integer getCapacidadeBarramentoA() {
        return capacidadeBarramentoA;
    }

    public void setCapacidadeBarramentoA(Integer capacidadeBarramentoA) {
        this.capacidadeBarramentoA = capacidadeBarramentoA;
    }

    public String getMemoriaCalculoJson() {
        return memoriaCalculoJson;
    }

    public void setMemoriaCalculoJson(String memoriaCalculoJson) {
        this.memoriaCalculoJson = memoriaCalculoJson;
    }

    public LocalDateTime getCalculadoEm() {
        return calculadoEm;
    }

    public void setCalculadoEm(LocalDateTime calculadoEm) {
        this.calculadoEm = calculadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DemandResults that = (DemandResults) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
