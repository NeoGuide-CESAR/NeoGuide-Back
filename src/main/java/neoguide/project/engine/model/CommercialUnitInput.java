package neoguide.project.engine.model;

import java.math.BigDecimal;

/** Espaço comercial já resolvido contra a Tabela 22: carga mínima, FD e FP vêm do catálogo. */
public record CommercialUnitInput(
        String ocupacaoCodigo,
        String rotulo,
        BigDecimal areaM2,
        BigDecimal potenciaInstaladaKw,
        BigDecimal cargaMinimaWm2,
        BigDecimal fatorDemandaIlum,
        BigDecimal fatorPotencia,
        boolean escalonado
) {
}
