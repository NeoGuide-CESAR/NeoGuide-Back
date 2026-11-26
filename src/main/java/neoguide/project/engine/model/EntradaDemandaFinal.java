package neoguide.project.engine.model;

import neoguide.project.model.TipoEdificacao;

import java.math.BigDecimal;

/**
 * Parcelas da demanda do projeto já calculadas pelos motores de cada etapa (kVA).
 * Drf: residencial; Ds: áreas comuns e serviços; Dc: espaços comerciais; Dve: recarga de VE; Dmbi: motobomba de incêndio.
 */
public record EntradaDemandaFinal(
        TipoEdificacao tipo,
        BigDecimal drf,
        BigDecimal ds,
        BigDecimal dc,
        BigDecimal dve,
        BigDecimal dmbi
) {
}
