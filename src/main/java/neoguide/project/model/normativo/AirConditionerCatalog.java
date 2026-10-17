package neoguide.project.model.normativo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "catalogo_aparelhos_ar_condicionado")
public class AirConditionerCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "capacidade_btu", nullable = false, unique = true)
    private Integer capacidadeBtu;

    @Column(name = "potencia_ativa_w", nullable = false, precision = 8, scale = 2)
    private BigDecimal potenciaAtivaW;

    @Column(name = "potencia_aparente_va", nullable = false, precision = 8, scale = 2)
    private BigDecimal potenciaAparenteVa;

    @Column(name = "tensao_v", nullable = false)
    private Integer tensaoV;

    @Column(name = "corrente_nominal_a", nullable = false, precision = 6, scale = 2)
    private BigDecimal correnteNominalA;

    @Column(name = "fator_potencia", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorPotencia;

    @Column(name = "norma_origem", nullable = false, length = 60)
    private String normaOrigem;

    public AirConditionerCatalog() {
    }

    public AirConditionerCatalog(Integer capacidadeBtu, BigDecimal potenciaAtivaW,
                                 BigDecimal potenciaAparenteVa, Integer tensaoV,
                                 BigDecimal correnteNominalA, BigDecimal fatorPotencia,
                                 String normaOrigem) {
        this.capacidadeBtu = capacidadeBtu;
        this.potenciaAtivaW = potenciaAtivaW;
        this.potenciaAparenteVa = potenciaAparenteVa;
        this.tensaoV = tensaoV;
        this.correnteNominalA = correnteNominalA;
        this.fatorPotencia = fatorPotencia;
        this.normaOrigem = normaOrigem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCapacidadeBtu() {
        return capacidadeBtu;
    }

    public void setCapacidadeBtu(Integer capacidadeBtu) {
        this.capacidadeBtu = capacidadeBtu;
    }

    public BigDecimal getPotenciaAtivaW() {
        return potenciaAtivaW;
    }

    public void setPotenciaAtivaW(BigDecimal potenciaAtivaW) {
        this.potenciaAtivaW = potenciaAtivaW;
    }

    public BigDecimal getPotenciaAparenteVa() {
        return potenciaAparenteVa;
    }

    public void setPotenciaAparenteVa(BigDecimal potenciaAparenteVa) {
        this.potenciaAparenteVa = potenciaAparenteVa;
    }

    public Integer getTensaoV() {
        return tensaoV;
    }

    public void setTensaoV(Integer tensaoV) {
        this.tensaoV = tensaoV;
    }

    public BigDecimal getCorrenteNominalA() {
        return correnteNominalA;
    }

    public void setCorrenteNominalA(BigDecimal correnteNominalA) {
        this.correnteNominalA = correnteNominalA;
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
        if (!(o instanceof AirConditionerCatalog that)) return false;
        return Objects.equals(id, that.id) || Objects.equals(capacidadeBtu, that.capacidadeBtu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(capacidadeBtu);
    }
}
