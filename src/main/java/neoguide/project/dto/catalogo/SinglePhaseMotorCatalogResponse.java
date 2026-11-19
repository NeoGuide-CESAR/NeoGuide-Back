package neoguide.project.dto.catalogo;

import neoguide.project.model.normativo.SinglePhaseMotorReference;
import java.math.BigDecimal;

public class SinglePhaseMotorCatalogResponse {

    private Long id;
    private String potenciaCv;
    private BigDecimal potenciaCvNumerica;
    private BigDecimal potenciaW;
    private BigDecimal potenciaVa;
    private BigDecimal corrente110vA;
    private BigDecimal corrente220vA;
    private BigDecimal fatorPotencia;
    private String normaOrigem;

    public SinglePhaseMotorCatalogResponse() {
    }

    public SinglePhaseMotorCatalogResponse(Long id, String potenciaCv, BigDecimal potenciaCvNumerica,
                                           BigDecimal potenciaW, BigDecimal potenciaVa,
                                           BigDecimal corrente110vA, BigDecimal corrente220vA,
                                           BigDecimal fatorPotencia, String normaOrigem) {
        this.id = id;
        this.potenciaCv = potenciaCv;
        this.potenciaCvNumerica = potenciaCvNumerica;
        this.potenciaW = potenciaW;
        this.potenciaVa = potenciaVa;
        this.corrente110vA = corrente110vA;
        this.corrente220vA = corrente220vA;
        this.fatorPotencia = fatorPotencia;
        this.normaOrigem = normaOrigem;
    }

    public static SinglePhaseMotorCatalogResponse from(SinglePhaseMotorReference entity) {
        if (entity == null) {
            return null;
        }
        return new SinglePhaseMotorCatalogResponse(
                entity.getId(),
                entity.getPotenciaCv(),
                entity.getPotenciaCvNumerica(),
                entity.getPotenciaW(),
                entity.getPotenciaVa(),
                entity.getCorrente110vA(),
                entity.getCorrente220vA(),
                entity.getFatorPotencia(),
                entity.getNormaOrigem()
        );
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
}
