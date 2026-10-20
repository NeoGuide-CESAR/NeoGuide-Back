package neoguide.project.dto.simulation;

import java.math.BigDecimal;

public record SpecialLoadsItemDto(
    Integer qtdPontosVe,
    BigDecimal potenciaVeKw,
    BigDecimal potenciaBombaIncendioCv,
    BigDecimal potenciaSolarInversorKw
) {
    public SpecialLoadsItemDto {
        qtdPontosVe = (qtdPontosVe != null) ? qtdPontosVe : 0;
        potenciaVeKw = (potenciaVeKw != null) ? potenciaVeKw : BigDecimal.ZERO;
        potenciaBombaIncendioCv = (potenciaBombaIncendioCv != null) ? potenciaBombaIncendioCv : BigDecimal.ZERO;
        potenciaSolarInversorKw = (potenciaSolarInversorKw != null) ? potenciaSolarInversorKw : BigDecimal.ZERO;
    }

    public static SpecialLoadsItemDto empty() {
        return new SpecialLoadsItemDto(0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
