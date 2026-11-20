package neoguide.project.dto.simulation;

import java.math.BigDecimal;

public record CommonAreaLoadItemDto(
    String origem,               // "CATALOGO" ou "PERSONALIZADO"
    String submodulo,            // "ILUMINACAO_TUG", "CLIMATIZACAO", "COCCAO", "MOTORES", etc.
    String tipoEquipamento,
    BigDecimal potenciaNominalKw,
    Integer quantidade,
    BigDecimal fatorPotencia,
    BigDecimal fatorDemanda,
    Boolean isReserva,
    Long catalogoMotorId,
    Long catalogoClimaId,
    Long catalogoEquipamentoId
) {
    public CommonAreaLoadItemDto {
        origem = (origem != null) ? origem : "PERSONALIZADO";
        potenciaNominalKw = (potenciaNominalKw != null) ? potenciaNominalKw : BigDecimal.ZERO;
        quantidade = (quantidade != null) ? quantidade : 1;
        fatorPotencia = (fatorPotencia != null) ? fatorPotencia : new BigDecimal("1.00");
        fatorDemanda = (fatorDemanda != null) ? fatorDemanda : new BigDecimal("1.00");
        isReserva = (isReserva != null) ? isReserva : Boolean.FALSE;
    }
}
