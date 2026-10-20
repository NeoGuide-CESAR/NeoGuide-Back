package neoguide.project.dto.catalogo;

import com.fasterxml.jackson.annotation.JsonProperty;
import neoguide.project.model.normativo.MotorReference;

import java.math.BigDecimal;

public class MotorCatalogResponse {

    private Long id;
    private BigDecimal potenciaCv;
    private BigDecimal potenciaKw;
    private BigDecimal rendimentoEta;
    private BigDecimal fatorPotencia;

    @JsonProperty("demandaUnitariakVa")
    private BigDecimal demandaUnitariakVa;

    private BigDecimal correnteNominal380vA;
    private String chavePartidaRecomendada;
    private String normaOrigem;

    public MotorCatalogResponse() {
    }

    public MotorCatalogResponse(Long id, BigDecimal potenciaCv, BigDecimal potenciaKw,
                                BigDecimal rendimentoEta, BigDecimal fatorPotencia,
                                BigDecimal demandaUnitariakVa, BigDecimal correnteNominal380vA,
                                String chavePartidaRecomendada, String normaOrigem) {
        this.id = id;
        this.potenciaCv = potenciaCv;
        this.potenciaKw = potenciaKw;
        this.rendimentoEta = rendimentoEta;
        this.fatorPotencia = fatorPotencia;
        this.demandaUnitariakVa = demandaUnitariakVa;
        this.correnteNominal380vA = correnteNominal380vA;
        this.chavePartidaRecomendada = chavePartidaRecomendada;
        this.normaOrigem = normaOrigem;
    }

    public static MotorCatalogResponse from(MotorReference entity) {
        return new MotorCatalogResponse(
                entity.getId(),
                entity.getPotenciaCv(),
                entity.getPotenciaKw(),
                entity.getRendimentoEta(),
                entity.getFatorPotencia(),
                entity.getDemandaUnitariaKva(),
                entity.getCorrenteNominal380vA(),
                entity.getChavePartidaRecomendada(),
                entity.getNormaOrigem()
        );
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

    @JsonProperty("demandaUnitariakVa")
    public BigDecimal getDemandaUnitariakVa() {
        return demandaUnitariakVa;
    }

    @JsonProperty("demandaUnitariakVa")
    public void setDemandaUnitariakVa(BigDecimal demandaUnitariakVa) {
        this.demandaUnitariakVa = demandaUnitariakVa;
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
}
