package neoguide.project.engine.model;

import java.util.List;

public record CommercialCalculationInput(
        boolean possuiComercial,
        List<CommercialUnitInput> unidades
) {
}
