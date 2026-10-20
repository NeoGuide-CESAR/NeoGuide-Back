package neoguide.project.dto.simulation;

import java.math.BigDecimal;

public record ResidentialTypologyItemDto(
    String nome,
    BigDecimal areaUtilM2,
    Integer quantidadeUnidades,
    Boolean isSmartStudio
) {
    public ResidentialTypologyItemDto {
        nome = (nome != null) ? nome : "Tipologia Residencial";
        areaUtilM2 = (areaUtilM2 != null) ? areaUtilM2 : BigDecimal.ZERO;
        quantidadeUnidades = (quantidadeUnidades != null) ? quantidadeUnidades : 0;
        isSmartStudio = (isSmartStudio != null) ? isSmartStudio : Boolean.FALSE;
    }
}
