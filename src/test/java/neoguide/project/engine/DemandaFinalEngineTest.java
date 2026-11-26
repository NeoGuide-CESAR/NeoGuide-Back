package neoguide.project.engine;

import neoguide.project.engine.model.EntradaDemandaFinal;
import neoguide.project.engine.model.ResultadoDemandaFinal;
import neoguide.project.model.TipoEdificacao;
import neoguide.project.model.normativo.BusbarReference;
import neoguide.project.model.normativo.ServiceEntranceReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Cálculo final: Ded, DQDG, enquadramento, entrada (Tabela 2), barramento (Tabela 3), Ip e alertas. */
class DemandaFinalEngineTest {

    private static final String NORMA_ENTRADA = "DIS-NOR-053 Anexo II Tabela 2";
    private static final String NORMA_BARRAMENTO = "DIS-NOR-053 Anexo II Tabela 3";

    private DemandaFinalEngine engine;
    private List<ServiceEntranceReference> entradas;
    private List<BusbarReference> barramentos;

    @BeforeEach
    void setUp() {
        engine = new DemandaFinalEngine();
        // Trechos da Tabela 2 (380/220 V) e da Tabela 3, na mesma forma da semente do back.
        entradas = List.of(
                entrada("46.00", "16 mm²", 70, 70),
                entrada("53.00", "25 mm²", 80, 80),
                entrada("165.00", "150 mm²", 250, 250),
                entrada("211.00", "240 mm²", 320, 320),
                entrada("263.00", "2 x 150 mm²", 400, 400),
                entrada("300.00", "2 x 185 mm²", 450, 630));
        barramentos = List.of(
                barra("40.00", "19 x 3", 208),
                barra("104.00", "25 x 5", 340),
                barra("207.00", "30 x 5", 449),
                barra("259.00", "40 x 5", 460));
    }

    private static ServiceEntranceReference entrada(String max, String secao, int correnteTotal, int disjuntor) {
        return new ServiceEntranceReference("380_220V", new BigDecimal(max), 1, secao, "1 x 110", "1 x 100",
                correnteTotal, "160 A", disjuntor, NORMA_ENTRADA);
    }

    private static BusbarReference barra(String max, String dimensao, int corrente) {
        return new BusbarReference(new BigDecimal(max), dimensao, null, corrente, NORMA_BARRAMENTO);
    }

    private static EntradaDemandaFinal entrada(TipoEdificacao tipo, String drf, String ds, String dc, String dve, String dmbi) {
        return new EntradaDemandaFinal(tipo, bd(drf), bd(ds), bd(dc), bd(dve), bd(dmbi));
    }

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }

    private ResultadoDemandaFinal calcular(EntradaDemandaFinal e) {
        return engine.calcular(e, entradas, barramentos);
    }

    @Test
    @DisplayName("Ded = Drf + Ds + Dc + Dve + Dmbi; DQDG = Ded − Dmbi")
    void dedESomaDasParcelasEDqdgSemMotobomba() {
        ResultadoDemandaFinal r = calcular(entrada(TipoEdificacao.MISTO, "100", "20", "21.74", "29.60", "2.70"));
        assertEquals(0, bd("174.04").compareTo(r.ded()));
        assertEquals(0, bd("171.34").compareTo(r.dqdg()));
    }

    @Test
    @DisplayName("Motobomba de incêndio não entra no DQDG")
    void motobombaForaDoDqdg() {
        ResultadoDemandaFinal semBomba = calcular(entrada(TipoEdificacao.COMERCIAL, "0", "0", "100", "0", "0"));
        ResultadoDemandaFinal comBomba = calcular(entrada(TipoEdificacao.COMERCIAL, "0", "0", "100", "0", "2.70"));
        assertEquals(0, semBomba.dqdg().compareTo(comBomba.dqdg()));
        assertEquals(0, bd("102.70").compareTo(comBomba.ded()));
    }

    @Test
    @DisplayName("Exemplo da VE e motobomba: Dve 29,60 kVA e motobomba 2 cv 2,70 kVA ficam fora do DQDG")
    void exemploVeEMotobomba() {
        ResultadoDemandaFinal r = calcular(entrada(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, "0", "0", "0", "29.60", "2.70"));
        assertEquals(0, bd("29.60").compareTo(r.dqdg()));
        assertEquals(0, bd("32.30").compareTo(r.ded()));
    }

    @Test
    @DisplayName("Entrada: 165 < Ded ≤ 211 → 240 mm² e disjuntor geral 320 A")
    void entradaFaixa165a211() {
        ResultadoDemandaFinal r = calcular(entrada(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, "200", "0", "0", "0", "0"));
        assertEquals(Integer.valueOf(320), r.entrada().disjuntorGeralA());
        assertEquals("240 mm²", r.entrada().caboFaseMm2());
        assertEquals("DIS-NOR-053 Anexo II Tabela 2", r.entrada().tabelaReferencia());
    }

    @Test
    @DisplayName("Entrada: Ded exatamente 165 kVA fica na faixa anterior (150 mm²)")
    void entradaLimiteSuperior165() {
        ResultadoDemandaFinal r = calcular(entrada(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, "165", "0", "0", "0", "0"));
        assertEquals("150 mm²", r.entrada().caboFaseMm2());
    }

    @Test
    @DisplayName("Barramento: 104 < DQDG ≤ 207 → 30 x 5 mm, 449 A")
    void barramentoFaixa104a207() {
        ResultadoDemandaFinal r = calcular(entrada(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, "150", "0", "0", "0", "0"));
        assertEquals("30 x 5", r.barramento().secao());
        assertEquals(Integer.valueOf(449), r.barramento().capacidadeA());
    }

    @Test
    @DisplayName("Barramento: DQDG exatamente 104 fica em 25 x 5 mm, 340 A")
    void barramentoLimite104() {
        ResultadoDemandaFinal r = calcular(entrada(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, "104", "0", "0", "0", "0"));
        assertEquals("25 x 5", r.barramento().secao());
    }

    @Test
    @DisplayName("Ip = DQDG × 1000 / (√3 × 380); 200 kVA dá 303,87 A")
    void correnteDeProjeto() {
        ResultadoDemandaFinal r = calcular(entrada(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, "200", "0", "0", "0", "0"));
        assertEquals(0, bd("303.87").compareTo(r.ipA()));
    }

    @Test
    @DisplayName("Residencial acima de 300 kVA: atendimento em MT, sem entrada nem barramento, com alerta")
    void residencialAcimaDe300EMt() {
        ResultadoDemandaFinal r = calcular(entrada(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, "301", "0", "0", "0", "0"));
        assertTrue(r.atendimentoMt());
        assertNull(r.entrada());
        assertNull(r.barramento());
        assertNull(r.ipA());
        assertEquals(0, bd("300").compareTo(r.limiteBtKva()));
        assertTrue(r.alertas().stream().anyMatch(a -> "atendimento_mt".equals(a.codigo()) && "ALERTA".equals(a.nivel())));
    }

    @Test
    @DisplayName("Comercial acima de 225 kVA já é MT, mesmo estando dentro da Tabela 2")
    void comercialAcimaDe225EMt() {
        ResultadoDemandaFinal r = calcular(entrada(TipoEdificacao.COMERCIAL, "0", "0", "226", "0", "0"));
        assertTrue(r.atendimentoMt());
        assertEquals(0, bd("225").compareTo(r.limiteBtKva()));
    }

    @Test
    @DisplayName("Misto usa o limite comercial de 225 kVA")
    void mistoUsaLimiteComercial() {
        assertTrue(calcular(entrada(TipoEdificacao.MISTO, "0", "0", "226", "0", "0")).atendimentoMt());
        assertFalse(calcular(entrada(TipoEdificacao.MISTO, "0", "0", "225", "0", "0")).atendimentoMt());
    }

    @Test
    @DisplayName("Até 50 kVA: INFO de critério simplificado")
    void ateCinquentaKvaInfo() {
        ResultadoDemandaFinal r = calcular(entrada(TipoEdificacao.COMERCIAL, "0", "0", "40", "0", "0"));
        assertTrue(r.alertas().stream().anyMatch(a -> "demanda_ate_50_kva".equals(a.codigo()) && "INFO".equals(a.nivel())));
    }

    @Test
    @DisplayName("Disjuntor de 250 A ou mais: INFO sobre relé ajustável 0,9 a 1,2")
    void releAjustavel() {
        ResultadoDemandaFinal r = calcular(entrada(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, "200", "0", "0", "0", "0"));
        assertTrue(r.alertas().stream().anyMatch(a -> "rele_ajustavel".equals(a.codigo()) && "INFO".equals(a.nivel())));
    }

    @Test
    @DisplayName("Corrente de projeto acima do disjuntor geral gera ALERTA (não bloqueia)")
    void correnteAcimaDoDisjuntor() {
        // 53 kVA → entrada de 25 mm² com disjuntor 80 A; Ip = 53000 / 658,18 = 80,53 A > 80 A
        ResultadoDemandaFinal r = calcular(entrada(TipoEdificacao.COMERCIAL, "0", "0", "53", "0", "0"));
        assertFalse(r.atendimentoMt());
        assertTrue(r.alertas().stream().anyMatch(a -> "corrente_projeto_acima_disjuntor".equals(a.codigo()) && "ALERTA".equals(a.nivel())));
    }

    @Test
    @DisplayName("Sem alertas quando Ip está dentro do disjuntor e a demanda é intermediária")
    void semAlertaEmCasoComum() {
        ResultadoDemandaFinal r = calcular(entrada(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, "120", "0", "0", "0", "0"));
        assertTrue(r.alertas().stream().noneMatch(a -> "ALERTA".equals(a.nivel())));
    }
}
