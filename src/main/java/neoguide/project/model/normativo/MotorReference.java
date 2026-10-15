package neoguide.project.model.normativo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "catalogo_motores")
public class MotorReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "potencia_cv", nullable = false, unique = true, precision = 6, scale = 2)
    private BigDecimal potenciaCv;

    @Column(name = "potencia_kw", nullable = false, precision = 6, scale = 2)
    private BigDecimal potenciaKw;

    @Column(name = "rendimento_eta", nullable = false, precision = 4, scale = 2)
    private BigDecimal rendimentoEta;

    @Column(name = "fator_potencia", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorPotencia;

    @Column(name = "demanda_unitaria_kva", nullable = false, precision = 6, scale = 2)
    private BigDecimal demandaUnitariaKva;

    @Column(name = "corrente_nominal_380v_a", nullable = false, precision = 6, scale = 2)
    private BigDecimal correnteNominal380vA;

    @Column(name = "chave_partida_recomendada", length = 50)
    private String chavePartidaRecomendada;

    @Column(name = "norma_origem", nullable = false, length = 60)
    private String normaOrigem;

    public MotorReference() {
    }

    public MotorReference(BigDecimal potenciaCv, BigDecimal potenciaKw, BigDecimal rendimentoEta,
                          BigDecimal fatorPotencia, BigDecimal demandaUnitariaKva,
                          BigDecimal correnteNominal380vA, String chavePartidaRecomendada,
                          String normaOrigem) {
        this.potenciaCv = potenciaCv;
        this.potenciaKw = potenciaKw;
        this.rendimentoEta = rendimentoEta;
        this.fatorPotencia = fatorPotencia;
        this.demandaUnitariaKva = demandaUnitariaKva;
        this.correnteNominal380vA = correnteNominal380vA;
        this.chavePartidaRecomendada = chavePartidaRecomendada;
        this.normaOrigem = normaOrigem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getPotenciaCv() {
        return potenciaCv;
    }

    public void setPotenciaCv(BigDecimal potenciaCv) {
        this.potenciaCv = potenciaCv;
    }

    public BigDecimal getPotenciaKw() {
        return potenciaKw;
    }

    public void setPotenciaKw(BigDecimal potenciaKw) {
        this.potenciaKw = potenciaKw;
    }

    public BigDecimal getRendimentoEta() {
        return rendimentoEta;
    }

    public void setRendimentoEta(BigDecimal rendimentoEta) {
        this.rendimentoEta = rendimentoEta;
    }

    public BigDecimal getFatorPotencia() {
        return fatorPotencia;
    }

    public void setFatorPotencia(BigDecimal fatorPotencia) {
        this.fatorPotencia = fatorPotencia;
    }

    public BigDecimal getDemandaUnitariaKva() {
        return demandaUnitariaKva;
    }

    public void setDemandaUnitariaKva(BigDecimal demandaUnitariaKva) {
        this.demandaUnitariaKva = demandaUnitariaKva;
    }

    public BigDecimal getCorrenteNominal380vA() {
        return correnteNominal380vA;
    }

    public void setCorrenteNominal380vA(BigDecimal correnteNominal380vA) {
        this.correnteNominal380vA = correnteNominal380vA;
    }

    public String getChavePartidaRecomendada() {
        return chavePartidaRecomendada;
    }

    public void setChavePartidaRecomendada(String chavePartidaRecomendada) {
        this.chavePartidaRecomendada = chavePartidaRecomendada;
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
        if (!(o instanceof MotorReference that)) return false;
        return Objects.equals(id, that.id) || Objects.equals(potenciaCv, that.potenciaCv);
    }

    @Override
    public int hashCode() {
        return Objects.hash(potenciaCv);
    }
}
