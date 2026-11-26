package neoguide.project.service;

import neoguide.project.model.TipoEdificacao;

/**
 * Tipo de edificação usado no enquadramento BT/MT, derivado do que o usuário escolheu e da existência de espaços
 * comerciais (DIS-NOR-053, itens 6.2.4 e 6.2.6). Não altera o tipo escolhido.
 *
 * <ul>
 *   <li>Residencial sem espaços comerciais: residencial (limite de BT de 300 kVA).</li>
 *   <li>Residencial com espaços comerciais: misto (225 kVA).</li>
 *   <li>Misto e comercial: o próprio tipo (225 kVA).</li>
 * </ul>
 */
public final class EnquadramentoEdificacao {

    private EnquadramentoEdificacao() {
    }

    public static TipoEdificacao derivar(TipoEdificacao escolhido, boolean temEspacosComerciais) {
        if (escolhido == TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR && temEspacosComerciais) {
            return TipoEdificacao.MISTO;
        }
        return escolhido;
    }

    /** Razão do enquadramento quando ele difere do tipo escolhido; nulo quando coincidem. */
    public static String motivo(TipoEdificacao escolhido, TipoEdificacao derivado) {
        if (escolhido == null || escolhido == derivado) {
            return null;
        }
        return "edificação residencial com espaços comerciais, tratada como mista";
    }
}
