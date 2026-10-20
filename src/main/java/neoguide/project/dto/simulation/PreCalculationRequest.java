package neoguide.project.dto.simulation;

import java.util.Collections;
import java.util.List;

public record PreCalculationRequest(
    String tensaoFornecimento,
    List<ResidentialTypologyItemDto> tipologias,
    List<CommonAreaLoadItemDto> cargasServicos,
    List<CommercialUnitItemDto> unidadesComerciais,
    SpecialLoadsItemDto cargasEspeciais
) {
    public PreCalculationRequest {
        tensaoFornecimento = (tensaoFornecimento != null && !tensaoFornecimento.isBlank()) 
            ? tensaoFornecimento : "380_220V";
        tipologias = (tipologias != null) ? List.copyOf(tipologias) : Collections.emptyList();
        cargasServicos = (cargasServicos != null) ? List.copyOf(cargasServicos) : Collections.emptyList();
        unidadesComerciais = (unidadesComerciais != null) ? List.copyOf(unidadesComerciais) : Collections.emptyList();
        cargasEspeciais = (cargasEspeciais != null) ? cargasEspeciais : SpecialLoadsItemDto.empty();
    }
}
