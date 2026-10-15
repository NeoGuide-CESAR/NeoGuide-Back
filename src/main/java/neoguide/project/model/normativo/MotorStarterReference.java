package neoguide.project.model.normativo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "catalogo_dispositivos_partida")
public class MotorStarterReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_partida", nullable = false, length = 50)
    private String tipoPartida;

    @Column(name = "min_cv_220v", precision = 6, scale = 2)
    private BigDecimal minCv220v;

    @Column(name = "max_cv_220v", precision = 6, scale = 2)
    private BigDecimal maxCv220v;

    @Column(name = "min_cv_380v", precision = 6, scale = 2)
    private BigDecimal minCv380v;

    @Column(name = "max_cv_380v", precision = 6, scale = 2)
    private BigDecimal maxCv380v;

    @Column(name = "descricao", nullable = false, length = 255)
    private String descricao;

    @Column(name = "norma_origem", nullable = false, length = 60)
    private String normaOrigem;

    public MotorStarterReference() {
    }

    public MotorStarterReference(String tipoPartida, BigDecimal minCv220v, BigDecimal maxCv220v,
                                 BigDecimal minCv380v, BigDecimal maxCv380v, String descricao,
                                 String normaOrigem) {
        this.tipoPartida = tipoPartida;
        this.minCv220v = minCv220v;
        this.maxCv220v = maxCv220v;
        this.minCv380v = minCv380v;
        this.maxCv380v = maxCv380v;
        this.descricao = descricao;
        this.normaOrigem = normaOrigem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipoPartida() {
        return tipoPartida;
    }

    public void setTipoPartida(String tipoPartida) {
        this.tipoPartida = tipoPartida;
    }

    public BigDecimal getMinCv220v() {
        return minCv220v;
    }

    public void setMinCv220v(BigDecimal minCv220v) {
        this.minCv220v = minCv220v;
    }

    public BigDecimal getMaxCv220v() {
        return maxCv220v;
    }

    public void setMaxCv220v(BigDecimal maxCv220v) {
        this.maxCv220v = maxCv220v;
    }

    public BigDecimal getMinCv380v() {
        return minCv380v;
    }

    public void setMinCv380v(BigDecimal minCv380v) {
        this.minCv380v = minCv380v;
    }

    public BigDecimal getMaxCv380v() {
        return maxCv380v;
    }

    public void setMaxCv380v(BigDecimal maxCv380v) {
        this.maxCv380v = maxCv380v;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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
        if (!(o instanceof MotorStarterReference that)) return false;
        return Objects.equals(id, that.id) || Objects.equals(tipoPartida, that.tipoPartida);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipoPartida);
    }
}
