package neoguide.project.dto;

import java.util.ArrayList;
import java.util.List;

public class ResidentialStageRequest {
    private List<ResidentialTypologyItemRequest> tipologias = new ArrayList<>();

    public ResidentialStageRequest() {
    }

    public ResidentialStageRequest(List<ResidentialTypologyItemRequest> tipologias) {
        this.tipologias = tipologias != null ? tipologias : new ArrayList<>();
    }

    public List<ResidentialTypologyItemRequest> getTipologias() {
        return tipologias;
    }

    public void setTipologias(List<ResidentialTypologyItemRequest> tipologias) {
        this.tipologias = tipologias;
    }
}
