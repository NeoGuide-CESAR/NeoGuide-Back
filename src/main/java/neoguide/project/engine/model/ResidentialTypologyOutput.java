package neoguide.project.engine.model;

import java.math.BigDecimal;

/**
 * Detalhamento analítico individualizado da tipologia calculada.
 */
public record ResidentialTypologyOutput(
        String nome,
        BigDecimal areaM2,
        int quantidade,
        BigDecimal demandaUnitariaDi,
        BigDecimal demandaSubtotal
) {

    // Getters de compatibilidade
    public String getNome() {
        return nome;
    }

    public BigDecimal getAreaM2() {
        return areaM2;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getDemandaUnitariaDi() {
        return demandaUnitariaDi;
    }

    public BigDecimal getDemandaSubtotal() {
        return demandaSubtotal;
    }
}
