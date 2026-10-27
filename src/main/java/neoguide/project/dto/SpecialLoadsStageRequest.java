package neoguide.project.dto;

import java.math.BigDecimal;

public class SpecialLoadsStageRequest {

    private Integer qtdPontosVe;
    private BigDecimal potenciaVeKw;
    private BigDecimal potenciaBombaIncendioCv;
    private BigDecimal potenciaSolarInversorKw;
    private BigDecimal fatorSimultaneidadeCustom;
    private BigDecimal demandaKvaCustom;

    public SpecialLoadsStageRequest() {
    }

    public SpecialLoadsStageRequest(Integer qtdPontosVe, BigDecimal potenciaVeKw,
                                   BigDecimal potenciaBombaIncendioCv, BigDecimal potenciaSolarInversorKw) {
        this.qtdPontosVe = qtdPontosVe;
        this.potenciaVeKw = potenciaVeKw;
        this.potenciaBombaIncendioCv = potenciaBombaIncendioCv;
        this.potenciaSolarInversorKw = potenciaSolarInversorKw;
    }

    public Integer getQtdPontosVe() {
        return qtdPontosVe;
    }

    public void setQtdPontosVe(Integer qtdPontosVe) {
        this.qtdPontosVe = qtdPontosVe;
    }

    public BigDecimal getPotenciaVeKw() {
        return potenciaVeKw;
    }

    public void setPotenciaVeKw(BigDecimal potenciaVeKw) {
        this.potenciaVeKw = potenciaVeKw;
    }

    // Alias para interoperabilidade com o nome potenciaEstacaoVeKw
    public BigDecimal getPotenciaEstacaoVeKw() {
        return potenciaVeKw;
    }

    public void setPotenciaEstacaoVeKw(BigDecimal potenciaEstacaoVeKw) {
        this.potenciaVeKw = potenciaEstacaoVeKw;
    }

    public BigDecimal getPotenciaBombaIncendioCv() {
        return potenciaBombaIncendioCv;
    }

    public void setPotenciaBombaIncendioCv(BigDecimal potenciaBombaIncendioCv) {
        this.potenciaBombaIncendioCv = potenciaBombaIncendioCv;
    }

    public BigDecimal getPotenciaSolarInversorKw() {
        return potenciaSolarInversorKw;
    }

    public void setPotenciaSolarInversorKw(BigDecimal potenciaSolarInversorKw) {
        this.potenciaSolarInversorKw = potenciaSolarInversorKw;
    }

    public BigDecimal getFatorSimultaneidadeCustom() {
        return fatorSimultaneidadeCustom;
    }

    public void setFatorSimultaneidadeCustom(BigDecimal fatorSimultaneidadeCustom) {
        this.fatorSimultaneidadeCustom = fatorSimultaneidadeCustom;
    }

    public BigDecimal getDemandaKvaCustom() {
        return demandaKvaCustom;
    }

    public void setDemandaKvaCustom(BigDecimal demandaKvaCustom) {
        this.demandaKvaCustom = demandaKvaCustom;
    }
}
