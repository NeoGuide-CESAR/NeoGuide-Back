package neoguide.project.dto;

import java.util.ArrayList;
import java.util.List;

/** Corpo do PUT /api/v1/projects/{id}/etapa-comercial: a lista inteira de espaços (substitui a anterior). */
public class CommercialStageRequest {

    private List<EspacoComercialRequest> espacos = new ArrayList<>();

    public CommercialStageRequest() {
    }

    public CommercialStageRequest(List<EspacoComercialRequest> espacos) {
        this.espacos = espacos != null ? espacos : new ArrayList<>();
    }

    public List<EspacoComercialRequest> getEspacos() { return espacos; }
    public void setEspacos(List<EspacoComercialRequest> espacos) { this.espacos = espacos; }
}
