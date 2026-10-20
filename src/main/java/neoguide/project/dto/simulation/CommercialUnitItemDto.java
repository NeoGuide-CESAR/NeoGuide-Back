package neoguide.project.dto.simulation;

import java.math.BigDecimal;

public record CommercialUnitItemDto(
    String identificacao,
    String atividadeComercial,
    BigDecimal areaM2,
    BigDecimal potenciaInstaladaKw,
    BigDecimal fatorDemanda
) {
    public CommercialUnitItemDto {
        identificacao = (identificacao != null) ? identificacao : "Unidade Comercial";
        areaM2 = (areaM2 != null) ? areaM2 : BigDecimal.ZERO;
        potenciaInstaladaKw = (potenciaInstaladaKw != null) ? potenciaInstaladaKw : BigDecimal.ZERO;
        fatorDemanda = (fatorDemanda != null) ? fatorDemanda : new BigDecimal("1.00");
    }
}
