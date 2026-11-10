package neoguide.project.dto;

import java.util.ArrayList;
import java.util.List;

public class CommercialStageRequest {

    private List<CommercialUnitItemRequest> unidadesComerciais = new ArrayList<>();

    public CommercialStageRequest() {
    }

    public CommercialStageRequest(List<CommercialUnitItemRequest> unidadesComerciais) {
        this.unidadesComerciais = unidadesComerciais != null ? unidadesComerciais : new ArrayList<>();
    }

    public List<CommercialUnitItemRequest> getUnidadesComerciais() {
        return unidadesComerciais;
    }

    public void setUnidadesComerciais(List<CommercialUnitItemRequest> unidadesComerciais) {
        this.unidadesComerciais = unidadesComerciais;
    }
}
