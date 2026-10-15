package neoguide.project.model.normativo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "catalogo_cargas_ocupacao")
public class OccupancyLoadReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ocupacao", nullable = false, unique = true, length = 50)
    private String ocupacao;

    @Column(name = "descricao", nullable = false, length = 100)
    private String descricao;

    @Column(name = "carga_minima_w_m2", nullable = false, precision = 6, scale = 2)
    private BigDecimal cargaMinimaWm2;

    @Column(name = "vitrine_w_metro", precision = 6, scale = 2)
    private BigDecimal vitrineWMetro;

    @Column(name = "fator_demanda_ilum", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorDemandaIlum;

    @Column(name = "fator_demanda_tug", precision = 4, scale = 2)
    private BigDecimal fatorDemandaTug;

    @Column(name = "fator_potencia", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorPotencia;

    @Column(name = "regra_normativa", length = 150)
    private String regraNormativa;

    @Column(name = "norma_origem", nullable = false, length = 60)
    private String normaOrigem;

    public OccupancyLoadReference() {
    }

    public OccupancyLoadReference(String ocupacao, String descricao, BigDecimal cargaMinimaWm2,
                                  BigDecimal vitrineWMetro, BigDecimal fatorDemandaIlum,
                                  BigDecimal fatorDemandaTug, BigDecimal fatorPotencia,
                                  String regraNormativa, String normaOrigem) {
        this.ocupacao = ocupacao;
        this.descricao = descricao;
        this.cargaMinimaWm2 = cargaMinimaWm2;
        this.vitrineWMetro = vitrineWMetro;
        this.fatorDemandaIlum = fatorDemandaIlum;
        this.fatorDemandaTug = fatorDemandaTug;
        this.fatorPotencia = fatorPotencia;
        this.regraNormativa = regraNormativa;
        this.normaOrigem = normaOrigem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOcupacao() {
        return ocupacao;
    }

    public void setOcupacao(String ocupacao) {
        this.ocupacao = ocupacao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getCargaMinimaWm2() {
        return cargaMinimaWm2;
    }

    public void setCargaMinimaWm2(BigDecimal cargaMinimaWm2) {
        this.cargaMinimaWm2 = cargaMinimaWm2;
    }

    public BigDecimal getVitrineWMetro() {
        return vitrineWMetro;
    }

    public void setVitrineWMetro(BigDecimal vitrineWMetro) {
        this.vitrineWMetro = vitrineWMetro;
    }

    public BigDecimal getFatorDemandaIlum() {
        return fatorDemandaIlum;
    }

    public void setFatorDemandaIlum(BigDecimal fatorDemandaIlum) {
        this.fatorDemandaIlum = fatorDemandaIlum;
    }

    public BigDecimal getFatorDemandaTug() {
        return fatorDemandaTug;
    }

    public void setFatorDemandaTug(BigDecimal fatorDemandaTug) {
        this.fatorDemandaTug = fatorDemandaTug;
    }

    public BigDecimal getFatorPotencia() {
        return fatorPotencia;
    }

    public void setFatorPotencia(BigDecimal fatorPotencia) {
        this.fatorPotencia = fatorPotencia;
    }

    public String getRegraNormativa() {
        return regraNormativa;
    }

    public void setRegraNormativa(String regraNormativa) {
        this.regraNormativa = regraNormativa;
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
        if (!(o instanceof OccupancyLoadReference that)) return false;
        return Objects.equals(id, that.id) || Objects.equals(ocupacao, that.ocupacao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ocupacao);
    }
}
