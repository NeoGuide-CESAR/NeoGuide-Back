package neoguide.project.engine.model;

import java.math.BigDecimal;

public record CommercialUnitResult(
        String ocupacaoCodigo,
        String rotulo,
        BigDecimal areaM2,
        BigDecimal potenciaInstaladaKw,
        BigDecimal fatorDemandaEfetivo,
        BigDecimal demandaCalculadaKva,
        BigDecimal cargaMinimaKw,
        String alerta
) {
}
