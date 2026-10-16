package neoguide.project.engine.model;

import java.math.BigDecimal;

/**
 * Item de entrada representando uma tipologia habitacional privativa.
 */
public record ResidentialTypologyItem(
        String nome,
        BigDecimal areaM2,
        int quantidade,
        BigDecimal demandaUnitariaDi,
        Boolean isSmartStudio
) {

    public ResidentialTypologyItem(String nome, BigDecimal areaM2, int quantidade, BigDecimal demandaUnitariaDi) {
        this(nome, areaM2, quantidade, demandaUnitariaDi, false);
    }

    public ResidentialTypologyItem(String nome, BigDecimal areaM2, int quantidade) {
        this(nome, areaM2, quantidade, null, false);
    }

    public ResidentialTypologyItem(String nome, BigDecimal areaM2, int quantidade, boolean isSmartStudio) {
        this(nome, areaM2, quantidade, null, isSmartStudio);
    }

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

    public Boolean getIsSmartStudio() {
        return isSmartStudio;
    }
}
