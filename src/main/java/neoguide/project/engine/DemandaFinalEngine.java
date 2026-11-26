package neoguide.project.engine;

import neoguide.project.engine.model.EntradaDemandaFinal;
import neoguide.project.engine.model.ResultadoDemandaFinal;
import neoguide.project.engine.model.ResultadoDemandaFinal.AlertaCalculo;
import neoguide.project.engine.model.ResultadoDemandaFinal.BarramentoQdg;
import neoguide.project.engine.model.ResultadoDemandaFinal.DimensionamentoEntrada;
import neoguide.project.model.TipoEdificacao;
import neoguide.project.model.normativo.BusbarReference;
import neoguide.project.model.normativo.ServiceEntranceReference;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Cálculo final: Ded, DQDG, enquadramento BT/MT, entrada de serviço (Tabela 2), barramento do QDG (Tabela 3),
 * corrente de projeto Ip e alertas. Não acessa banco: as tabelas normativas chegam por parâmetro.
 */
@Component
public class DemandaFinalEngine {

    public static final BigDecimal LIMITE_BT_RESIDENCIAL_KVA = new BigDecimal("300");
    public static final BigDecimal LIMITE_BT_COMERCIAL_KVA = new BigDecimal("225");
    public static final BigDecimal LIMITE_CRITERIO_SIMPLIFICADO_KVA = new BigDecimal("50");
    public static final BigDecimal TENSAO_FASE_NEUTRO_380 = new BigDecimal("380");
    public static final int LIMITE_RELE_AJUSTAVEL_A = 250;
    static final BigDecimal RAIZ_DE_TRES = new BigDecimal("1.7320508075688772935");
    private static final BigDecimal MIL = new BigDecimal("1000");

    public ResultadoDemandaFinal calcular(EntradaDemandaFinal entrada,
                                          List<ServiceEntranceReference> tabelaEntrada,
                                          List<BusbarReference> tabelaBarramento) {
        BigDecimal ded = entrada.drf().add(entrada.ds()).add(entrada.dc()).add(entrada.dve()).add(entrada.dmbi());
        // A motobomba de incêndio não entra no DQDG (regra da especificação: DQDG = Ded − Dmbi).
        BigDecimal dqdg = ded.subtract(entrada.dmbi());
        BigDecimal limite = entrada.tipo() == TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR
                ? LIMITE_BT_RESIDENCIAL_KVA
                : LIMITE_BT_COMERCIAL_KVA;

        List<AlertaCalculo> alertas = new ArrayList<>();
        if (ded.compareTo(LIMITE_CRITERIO_SIMPLIFICADO_KVA) <= 0) {
            alertas.add(new AlertaCalculo("demanda_ate_50_kva", "INFO",
                    "Projetos até 50 kVA seguem critério simplificado de ligação.", "tipoEdificacao"));
        }

        if (ded.compareTo(limite) > 0) {
            alertas.add(new AlertaCalculo("atendimento_mt", "ALERTA",
                    "Demanda acima de " + limite.toPlainString() + " kVA: atendimento em média tensão. "
                            + "Memorial e dimensionamento de baixa tensão não são emitidos.", "tipoEdificacao"));
            return new ResultadoDemandaFinal(entrada.drf(), entrada.ds(), entrada.dc(), entrada.dve(), entrada.dmbi(),
                    ded, dqdg, limite, true, null, null, null, alertas);
        }

        ServiceEntranceReference linhaEntrada = tabelaEntrada.stream()
                .sorted(Comparator.comparing(ServiceEntranceReference::getMaxDemandaKva))
                .filter(l -> l.getMaxDemandaKva().compareTo(ded) >= 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Tabela de entrada de serviço não cobre a Ded de " + ded.toPlainString() + " kVA"));

        BusbarReference linhaBarramento = tabelaBarramento.stream()
                .sorted(Comparator.comparing(BusbarReference::getMaxDemandaKva))
                .filter(l -> l.getMaxDemandaKva().compareTo(dqdg) >= 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Tabela de barramento não cobre o DQDG de " + dqdg.toPlainString() + " kVA"));

        DimensionamentoEntrada dimensionamento = new DimensionamentoEntrada(
                linhaEntrada.getDisjuntorCorrenteA(),
                linhaEntrada.getQuantidadeCircuitos(),
                linhaEntrada.getSecaoCondutorMm2(),
                // A Tabela 2 traz uma seção por faixa; o neutro segue a mesma seção (confirmar com a norma).
                linhaEntrada.getSecaoCondutorMm2(),
                "PEAD " + linhaEntrada.getDutoPeadMm() + " mm / aço " + linhaEntrada.getDutoAcoMm() + " mm",
                linhaEntrada.getNormaOrigem());
        BarramentoQdg barramento = new BarramentoQdg(
                linhaBarramento.getBarraDimensaoMm(),
                linhaBarramento.getCorrenteNominalA(),
                linhaBarramento.getNormaOrigem());

        BigDecimal ip = dqdg.multiply(MIL)
                .divide(RAIZ_DE_TRES.multiply(TENSAO_FASE_NEUTRO_380), 2, RoundingMode.HALF_UP);
        if (ip.compareTo(BigDecimal.valueOf(linhaEntrada.getDisjuntorCorrenteA())) > 0) {
            alertas.add(new AlertaCalculo("corrente_projeto_acima_disjuntor", "ALERTA",
                    "A corrente de projeto (" + ip.toPlainString().replace('.', ',') + " A) supera o disjuntor geral ("
                            + linhaEntrada.getDisjuntorCorrenteA() + " A).", "disjuntorGeral"));
        }
        if (linhaEntrada.getDisjuntorCorrenteA() >= LIMITE_RELE_AJUSTAVEL_A) {
            alertas.add(new AlertaCalculo("rele_ajustavel", "INFO",
                    "Disjuntor de 250 A ou mais: relé de sobrecorrente ajustável entre 0,9 e 1,2 da corrente nominal "
                            + "(DIS-NOR-053, Tabela 2, nota 6).", "disjuntorGeral"));
        }

        return new ResultadoDemandaFinal(entrada.drf(), entrada.ds(), entrada.dc(), entrada.dve(), entrada.dmbi(),
                ded, dqdg, limite, false, ip, dimensionamento, barramento, alertas);
    }
}
