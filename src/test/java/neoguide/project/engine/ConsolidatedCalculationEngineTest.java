package neoguide.project.engine;

import neoguide.project.engine.enums.SupplyVoltageLevel;
import neoguide.project.engine.enums.VoltageSystem;
import neoguide.project.engine.model.ConsolidatedCalculationInput;
import neoguide.project.engine.model.ConsolidatedCalculationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ConsolidatedCalculationEngineTest {

    private ConsolidatedCalculationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ConsolidatedCalculationEngine();
    }

    @Test
    @DisplayName("Deve consolidar demandas do caso canônico do memorial descritivo (100 apartamentos)")
    void deveConsolidarDemandasCasoCanonicoMemorial100Apartamentos() {
        ConsolidatedCalculationInput input = new ConsolidatedCalculationInput(
                new BigDecimal("114.08"),
                new BigDecimal("69.45"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("2.70"),
                VoltageSystem.TRIFASICO_380_220V,
                null
        );

        ConsolidatedCalculationResult result = engine.calculate(input);

        assertNotNull(result);
        assertEquals(new BigDecimal("186.23"), result.demandaTotalEdificacaoDed());
        assertEquals(new BigDecimal("183.53"), result.demandaQdgDqdg());
        assertEquals(new BigDecimal("2.70"), result.demandaIncendioDeduzidaDmbi());
        assertEquals(SupplyVoltageLevel.BAIXA_TENSAO_SECUNDARIA, result.nivelFornecimento());
        assertEquals(VoltageSystem.TRIFASICO_380_220V, result.sistemaTensao());

        // Corrente de projeto Ip aproximada de 278.84 A
        assertEquals(new BigDecimal("278.84"), result.correnteProjetoIpA());

        // Dimensionamento Padrão de Entrada (faixa até 211 kVA)
        assertNotNull(result.padraoEntrada());
        assertEquals(320, result.padraoEntrada().disjuntorCorrenteInA());
        assertEquals("400 A", result.padraoEntrada().disjuntorCarcaca());
        assertEquals("1 x 240 mm²", result.padraoEntrada().secaoCondutorFase());
        assertEquals("1 x 240 mm²", result.padraoEntrada().secaoCondutorNeutro());
        assertEquals("1 x 110 mm", result.padraoEntrada().eletrodutoPeadMm());
        assertEquals("1 x 100 mm", result.padraoEntrada().eletrodutoAcoMm());

        // Dimensionamento Barramento QDG (faixa até 207 kVA)
        assertNotNull(result.barramentoQdg());
        assertEquals("30 x 5 mm", result.barramentoQdg().barraDimensaoMm());
        assertEquals("1 1/4\" x 3/16\"", result.barramentoQdg().barraDimensaoPol());
        assertEquals(449, result.barramentoQdg().correnteNominalA());

        // Auditorias
        assertTrue(result.disjuntorAdequado());
        assertTrue(result.barramentoAdequado());
        assertFalse(result.notasNormativas().isEmpty());
    }

    @Test
    @DisplayName("Deve classificar corretamente o limite de 300 kVA (BT vs MT)")
    void deveClassificarCorretamenteLimite300Kva() {
        // Exatamente 300.00 kVA -> Baixa Tensão
        ConsolidatedCalculationInput inputBt = new ConsolidatedCalculationInput(
                new BigDecimal("300.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                VoltageSystem.TRIFASICO_380_220V,
                null
        );
        ConsolidatedCalculationResult resultBt = engine.calculate(inputBt);
        assertEquals(SupplyVoltageLevel.BAIXA_TENSAO_SECUNDARIA, resultBt.nivelFornecimento());
        assertNotNull(resultBt.padraoEntrada());
        assertEquals(450, resultBt.padraoEntrada().disjuntorCorrenteInA());
        assertEquals("2 x (1 x 185 mm²)", resultBt.padraoEntrada().secaoCondutorFase());

        // 300.10 kVA -> Média Tensão
        ConsolidatedCalculationInput inputMt = new ConsolidatedCalculationInput(
                new BigDecimal("300.10"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                VoltageSystem.TRIFASICO_380_220V,
                null
        );
        ConsolidatedCalculationResult resultMt = engine.calculate(inputMt);
        assertEquals(SupplyVoltageLevel.MEDIA_TENSAO_PRIMARIA, resultMt.nivelFornecimento());
        assertNull(resultMt.padraoEntrada());
        assertTrue(resultMt.notasNormativas().stream().anyMatch(n -> n.contains("Média Tensão Primária (13,8 kV)")));
    }

    @Test
    @DisplayName("Deve dimensionar entrada em 220/127 V corretamente")
    void deveDimensionarEntrada220VCorretamente() {
        ConsolidatedCalculationInput input45 = new ConsolidatedCalculationInput(
                new BigDecimal("45.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                VoltageSystem.TRIFASICO_220_127V,
                null
        );
        ConsolidatedCalculationResult result45 = engine.calculate(input45);
        assertNotNull(result45.padraoEntrada());
        assertEquals(125, result45.padraoEntrada().disjuntorCorrenteInA());
        assertEquals("1 x 50 mm²", result45.padraoEntrada().secaoCondutorFase());

        ConsolidatedCalculationInput input110 = new ConsolidatedCalculationInput(
                new BigDecimal("110.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                VoltageSystem.TRIFASICO_220_127V,
                null
        );
        ConsolidatedCalculationResult result110 = engine.calculate(input110);
        assertNotNull(result110.padraoEntrada());
        assertEquals(320, result110.padraoEntrada().disjuntorCorrenteInA());
        assertEquals("1 x 240 mm²", result110.padraoEntrada().secaoCondutorFase());
    }

    @Test
    @DisplayName("Deve deduzir motobomba de incêndio apenas do QDG")
    void deveDeduzirMotobombaIncendioApenasDoQdg() {
        ConsolidatedCalculationInput input = new ConsolidatedCalculationInput(
                new BigDecimal("100.00"),
                new BigDecimal("20.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("5.00"),
                VoltageSystem.TRIFASICO_380_220V,
                null
        );

        ConsolidatedCalculationResult result = engine.calculate(input);

        assertEquals(new BigDecimal("125.00"), result.demandaTotalEdificacaoDed());
        assertEquals(new BigDecimal("120.00"), result.demandaQdgDqdg());
        assertEquals(new BigDecimal("5.00"), result.demandaIncendioDeduzidaDmbi());
        assertTrue(result.notasNormativas().stream().anyMatch(n -> n.contains("motobomba de incêndio")));
    }

    @Test
    @DisplayName("Deve dimensionar barramentos do QDG para faixas superiores")
    void deveDimensionarBarramentosParaFaixasSuperiores() {
        ConsolidatedCalculationInput input220 = new ConsolidatedCalculationInput(
                new BigDecimal("220.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                VoltageSystem.TRIFASICO_380_220V,
                null
        );
        ConsolidatedCalculationResult res220 = engine.calculate(input220);
        assertEquals("40 x 5 mm", res220.barramentoQdg().barraDimensaoMm());
        assertEquals(460, res220.barramentoQdg().correnteNominalA());

        ConsolidatedCalculationInput input300 = new ConsolidatedCalculationInput(
                new BigDecimal("300.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                VoltageSystem.TRIFASICO_380_220V,
                null
        );
        ConsolidatedCalculationResult res300 = engine.calculate(input300);
        assertEquals("50 x 5 mm", res300.barramentoQdg().barraDimensaoMm());
        assertEquals(595, res300.barramentoQdg().correnteNominalA());

        ConsolidatedCalculationInput input500 = new ConsolidatedCalculationInput(
                new BigDecimal("500.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                VoltageSystem.TRIFASICO_380_220V,
                null
        );
        ConsolidatedCalculationResult res500 = engine.calculate(input500);
        assertEquals("50 x 10 mm", res500.barramentoQdg().barraDimensaoMm());
        assertEquals(1312, res500.barramentoQdg().correnteNominalA());
    }

    @Test
    @DisplayName("Deve validar entradas inválidas e negativas")
    void deveValidarEntradasInvalidasENegativas() {
        assertThrows(IllegalArgumentException.class, () -> engine.calculate(null));

        assertThrows(IllegalArgumentException.class, () -> new ConsolidatedCalculationInput(
                new BigDecimal("-10.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                VoltageSystem.TRIFASICO_380_220V,
                null
        ));

        ConsolidatedCalculationInput allZeros = new ConsolidatedCalculationInput(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                VoltageSystem.TRIFASICO_380_220V,
                null
        );
        assertThrows(IllegalArgumentException.class, () -> engine.calculate(allZeros));
    }
}
