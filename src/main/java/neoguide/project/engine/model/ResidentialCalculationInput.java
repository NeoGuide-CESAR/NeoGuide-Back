package neoguide.project.engine.model;

import java.util.Collections;
import java.util.List;

/**
 * Payload agrupador de parâmetros para execução do cálculo residencial.
 */
public record ResidentialCalculationInput(
        List<ResidentialTypologyItem> tipologias,
        boolean isSmartStudio
) {

    public ResidentialCalculationInput {
        tipologias = tipologias != null ? List.copyOf(tipologias) : Collections.emptyList();
    }

    public ResidentialCalculationInput(List<ResidentialTypologyItem> tipologias) {
        this(tipologias, false);
    }

    // Getters de compatibilidade
    public List<ResidentialTypologyItem> getTipologias() {
        return tipologias;
    }

    public boolean getIsSmartStudio() {
        return isSmartStudio;
    }
}
