package neoguide.project.dto.catalogo;

import neoguide.project.model.normativo.SafetyFactorReference;

import java.math.BigDecimal;

public class SafetyFactorResponse {

    private BigDecimal minDrKva;
    private BigDecimal maxDrKva;
    private BigDecimal fatorSegurancaFr;

    public SafetyFactorResponse() {
    }

    public SafetyFactorResponse(BigDecimal minDrKva, BigDecimal maxDrKva, BigDecimal fatorSegurancaFr) {
        this.minDrKva = minDrKva;
        this.maxDrKva = maxDrKva;
        this.fatorSegurancaFr = fatorSegurancaFr;
    }

    public static SafetyFactorResponse from(SafetyFactorReference ref) {
        if (ref == null) return null;
        return new SafetyFactorResponse(
                ref.getMinDrKva(),
                ref.getMaxDrKva(),
                ref.getFatorSegurancaFr()
        );
    }

    public BigDecimal getMinDrKva() {
        return minDrKva;
    }

    public void setMinDrKva(BigDecimal minDrKva) {
        this.minDrKva = minDrKva;
    }

    public BigDecimal getMaxDrKva() {
        return maxDrKva;
    }

    public void setMaxDrKva(BigDecimal maxDrKva) {
        this.maxDrKva = maxDrKva;
    }

    public BigDecimal getFatorSegurancaFr() {
        return fatorSegurancaFr;
    }

    public void setFatorSegurancaFr(BigDecimal fatorSegurancaFr) {
        this.fatorSegurancaFr = fatorSegurancaFr;
    }
}
