package neoguide.project.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Última demanda calculada de um projeto (tabela demand_results). Guarda as parcelas usadas no cálculo e a
 * impressão digital dos insumos: se os insumos atuais mudarem, o resultado vira DESATUALIZADO.
 */
@Entity
@Table(name = "demand_results")
public class DemandResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "calculado_em", nullable = false)
    private LocalDateTime calculadoEm;

    @Column(name = "fingerprint", nullable = false, length = 64)
    private String fingerprint;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_edificacao", nullable = false, length = 40)
    private TipoEdificacao tipoEdificacao;

    @Column(name = "drf_kva", nullable = false, precision = 20, scale = 10)
    private BigDecimal drf;

    @Column(name = "ds_kva", nullable = false, precision = 20, scale = 10)
    private BigDecimal ds;

    @Column(name = "dc_kva", nullable = false, precision = 20, scale = 10)
    private BigDecimal dc;

    @Column(name = "dve_kva", nullable = false, precision = 20, scale = 10)
    private BigDecimal dve;

    @Column(name = "dmbi_kva", nullable = false, precision = 20, scale = 10)
    private BigDecimal dmbi;

    public DemandResult() {
    }

    public DemandResult(Project project, LocalDateTime calculadoEm, String fingerprint, TipoEdificacao tipoEdificacao,
                        BigDecimal drf, BigDecimal ds, BigDecimal dc, BigDecimal dve, BigDecimal dmbi) {
        this.project = project;
        this.calculadoEm = calculadoEm;
        this.fingerprint = fingerprint;
        this.tipoEdificacao = tipoEdificacao;
        this.drf = drf;
        this.ds = ds;
        this.dc = dc;
        this.dve = dve;
        this.dmbi = dmbi;
    }

    public Long getId() { return id; }
    public Project getProject() { return project; }
    public LocalDateTime getCalculadoEm() { return calculadoEm; }
    public String getFingerprint() { return fingerprint; }
    public TipoEdificacao getTipoEdificacao() { return tipoEdificacao; }
    public BigDecimal getDrf() { return drf; }
    public BigDecimal getDs() { return ds; }
    public BigDecimal getDc() { return dc; }
    public BigDecimal getDve() { return dve; }
    public BigDecimal getDmbi() { return dmbi; }
}
