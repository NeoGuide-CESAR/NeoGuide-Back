package neoguide.project.engine.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Resultado consolidado do cálculo da demanda residencial final (Drf) segundo a DIS-NOR-053.
 */
public record ResidentialCalculationResult(
        int totalApartamentos,
        BigDecimal demandaBrutaDr,
        BigDecimal fatorCoincidenciaFc,
        BigDecimal demandaProvisoriaDtr,
        BigDecimal fatorSegurancaFr,
        BigDecimal demandaFinalDrf,
        boolean isSmartStudio,
        List<ResidentialTypologyOutput> tipologias
) {

    public ResidentialCalculationResult {
        tipologias = tipologias != null ? List.copyOf(tipologias) : Collections.emptyList();
    }

    // Getters de compatibilidade
    public int getTotalApartamentos() {
        return totalApartamentos;
    }

    public BigDecimal getDemandaBrutaDr() {
        return demandaBrutaDr;
    }

    public BigDecimal getFatorCoincidenciaFc() {
        return fatorCoincidenciaFc;
    }

    public BigDecimal getDemandaProvisoriaDtr() {
        return demandaProvisoriaDtr;
    }

    public BigDecimal getFatorSegurancaFr() {
        return fatorSegurancaFr;
    }

    public BigDecimal getDemandaFinalDrf() {
        return demandaFinalDrf;
    }

    public boolean getIsSmartStudio() {
        return isSmartStudio;
    }

    public List<ResidentialTypologyOutput> getTipologias() {
        return tipologias;
    }
}
