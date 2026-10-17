package neoguide.project.model.normativo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "catalogo_motores_monofasicos")
public class SinglePhaseMotorReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "potencia_cv", nullable = false, unique = true, length = 20)
    private String potenciaCv;

    @Column(name = "potencia_cv_numerica", nullable = false, precision = 6, scale = 2)
    private BigDecimal potenciaCvNumerica;

    @Column(name = "potencia_w", nullable = false, precision = 8, scale = 2)
    private BigDecimal potenciaW;

    @Column(name = "potencia_va", nullable = false, precision = 8, scale = 2)
    private BigDecimal potenciaVa;

    @Column(name = "corrente_110v_a", precision = 6, scale = 2)
    private BigDecimal corrente110vA;

    @Column(name = "corrente_220v_a", nullable = false, precision = 6, scale = 2)
    private BigDecimal corrente220vA;

    @Column(name = "fator_potencia", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorPotencia;

    @Column(name = "norma_origem", nullable = false, length = 60)
    private String normaOrigem;

    public SinglePhaseMotorReference() {
    }

    public SinglePhaseMotorReference(String potenciaCv, BigDecimal potenciaCvNumerica,
                                     BigDecimal potenciaW, BigDecimal potenciaVa,
                                     BigDecimal corrente110vA, BigDecimal corrente220vA,
                                     BigDecimal fatorPotencia, String normaOrigem) {
        this.potenciaCv = potenciaCv;
        this.potenciaCvNumerica = potenciaCvNumerica;
        this.potenciaW = potenciaW;
        this.potenciaVa = potenciaVa;
        this.corrente110vA = corrente110vA;
        this.corrente220vA = corrente220vA;
        this.fatorPotencia = fatorPotencia;
        this.normaOrigem = normaOrigem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPotenciaCv() {
        return potenciaCv;
    }

    public void setPotenciaCv(String potenciaCv) {
        this.potenciaCv = potenciaCv;
    }

    public BigDecimal getPotenciaCvNumerica() {
        return potenciaCvNumerica;
    }

    public void setPotenciaCvNumerica(BigDecimal potenciaCvNumerica) {
        this.potenciaCvNumerica = potenciaCvNumerica;
    }

    public BigDecimal getPotenciaW() {
        return potenciaW;
    }

    public void setPotenciaW(BigDecimal potenciaW) {
        this.potenciaW = potenciaW;
    }

    public BigDecimal getPotenciaVa() {
        return potenciaVa;
    }

    public void setPotenciaVa(BigDecimal potenciaVa) {
        this.potenciaVa = potenciaVa;
    }

    public BigDecimal getCorrente110vA() {
        return corrente110vA;
    }

    public void setCorrente110vA(BigDecimal corrente110vA) {
        this.corrente110vA = corrente110vA;
    }

    public BigDecimal getCorrente220vA() {
        return corrente220vA;
    }

    public void setCorrente220vA(BigDecimal corrente220vA) {
        this.corrente220vA = corrente220vA;
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
        if (!(o instanceof SinglePhaseMotorReference that)) return false;
        return Objects.equals(id, that.id) || Objects.equals(potenciaCv, that.potenciaCv);
    }

    @Override
    public int hashCode() {
        return Objects.hash(potenciaCv);
    }
}
