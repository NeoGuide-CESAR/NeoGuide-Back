package neoguide.project.engine;

import neoguide.project.engine.model.commonarea.AirConditioningItem;
import neoguide.project.engine.model.commonarea.ApplianceItem;
import neoguide.project.engine.model.commonarea.BoilerItem;
import neoguide.project.engine.model.commonarea.CommonAreaCalculationInput;
import neoguide.project.engine.model.commonarea.CommonAreaCalculationResult;
import neoguide.project.engine.model.commonarea.CommonAreaSubtotal;
import neoguide.project.engine.model.commonarea.CookingInput;
import neoguide.project.engine.model.commonarea.CooktopItem;
import neoguide.project.engine.model.commonarea.CustomLoadItem;
import neoguide.project.engine.model.commonarea.ElevatorItem;
import neoguide.project.engine.model.commonarea.LightingTugInput;
import neoguide.project.engine.model.commonarea.MotorItem;
import neoguide.project.engine.model.commonarea.OvenItem;
import neoguide.project.engine.model.commonarea.PoolPumpItem;
import neoguide.project.engine.model.commonarea.WaterHeaterItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonAreaCalculationEngineTest {

    private CommonAreaCalculationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CommonAreaCalculationEngine();
    }

    @Test
    @DisplayName("Teste Unitário 1: Parcela a - Iluminação e TUG (Tabela 22)")
    void testParcelaAIluminacaoETug() {
        LightingTugInput input = new LightingTugInput(new BigDecimal("10.0"), new BigDecimal("6.0"));

        CommonAreaSubtotal subtotal = engine.calculateParcelaA(input);

        // Pilum = 10 / 0.80 = 12.50 kVA
        // Ptug = (6 * 0.50) / 1.00 = 3.00 kVA
        // Total = 15.50 kVA
        assertEquals(new BigDecimal("15.50"), subtotal.demandaKva());
        assertEquals(new BigDecimal("16.00"), subtotal.potenciaAtivaKw());
    }

    @Test
    @DisplayName("Teste Unitário 2: Parcela b - Aquecedores Rápidos de Passagem (Tabela 7)")
    void testParcelaBAquecedoresPassagemTabela7() {
        List<WaterHeaterItem> items = List.of(
                new WaterHeaterItem("Chuveiro Elétrico Rápido", new BigDecimal("5.5"), 4)
        );

        CommonAreaSubtotal subtotal = engine.calculateParcelaB(items);

        // N = 4 => FD = 0.76 (Tabela 7)
        // Ptotal = 4 * 5.5 = 22.0 kW
        // Demanda = (22.0 * 0.76) / 1.00 = 16.72 kVA
        assertEquals(new BigDecimal("16.72"), subtotal.demandaKva());
        assertEquals(new BigDecimal("22.00"), subtotal.potenciaAtivaKw());
        assertEquals(new BigDecimal("0.76"), subtotal.fatorDemandaMedio());
    }

    @Test
    @DisplayName("Teste Unitário 3: Parcela c - Boilers de Acumulação (Tabela 8)")
    void testParcelaCBoilersAcumulacaoTabela8() {
        List<BoilerItem> items = List.of(
                new BoilerItem("Boiler Central 500L", new BigDecimal("6.0"), 2)
        );

        CommonAreaSubtotal subtotal = engine.calculateParcelaC(items);

        // N = 2 => FD = 0.72 (Tabela 8)
        // Ptotal = 2 * 6.0 = 12.0 kW
        // Demanda = (12.0 * 0.72) / 1.00 = 8.64 kVA
        assertEquals(new BigDecimal("8.64"), subtotal.demandaKva());
        assertEquals(new BigDecimal("12.00"), subtotal.potenciaAtivaKw());
        assertEquals(new BigDecimal("0.72"), subtotal.fatorDemandaMedio());
    }

    @Test
    @DisplayName("Teste Unitário 4: Parcela d - Eletrodomésticos Coletivos (Tabela 9)")
    void testParcelaDEletrodomesticosTabela9() {
        List<ApplianceItem> items = List.of(
                new ApplianceItem("Lavadora Industrial", new BigDecimal("2.0"), 3)
        );

        CommonAreaSubtotal subtotal = engine.calculateParcelaD(items);

        // N = 3 => FD = 0.70 (Tabela 9)
        // Ptotal = 6.0 kW
        // Demanda = (6.0 * 0.70) / 0.92 = 4.20 / 0.92 = 4.5652... => 4.57 kVA
        assertEquals(new BigDecimal("4.57"), subtotal.demandaKva());
        assertEquals(new BigDecimal("6.00"), subtotal.potenciaAtivaKw());
        assertEquals(new BigDecimal("0.70"), subtotal.fatorDemandaMedio());
    }

    @Test
    @DisplayName("Teste Unitário 5: Parcela e - Desmembramento Gourmet (Fornos T9 x Cooktops T10)")
    void testParcelaEDesmembramentoFornosECooktops() {
        CookingInput input = new CookingInput(
                List.of(new OvenItem("Forno Elétrico Embutir", new BigDecimal("3.0"), 2)),
                List.of(new CooktopItem("Cooktop de Indução 4 bocas", new BigDecimal("7.0"), 4))
        );

        CommonAreaSubtotal subtotal = engine.calculateParcelaE(input);

        // Fornos: N=2, P=6.0 kW, FD=0.70 (T9) => 4.20 kVA
        // Cooktops: N=4, P=28.0 kW, FD=0.40 (T10) => 11.20 kVA
        // (Nota: se cooktop usasse T9, seria FD=0.70 => 19.60 kVA, gerando superdimensionamento de 43%)
        // Total = 4.20 + 11.20 = 15.40 kVA
        assertEquals(new BigDecimal("15.40"), subtotal.demandaKva());
        assertEquals(new BigDecimal("34.00"), subtotal.potenciaAtivaKw());
    }

    @Test
    @DisplayName("Teste Unitário 6: Parcela f - Climatização (Tabelas 11 e 12)")
    void testParcelaFClimatizacaoTabela11ETabela12() {
        List<AirConditioningItem> items = List.of(
                new AirConditioningItem("Split Salão de Festas", 12000, null, null, 3)
        );

        CommonAreaSubtotal subtotal = engine.calculateParcelaF(items);

        // 12.000 BTU => Tabela 11: 1.600 W (1.6 kW) por aparelho
        // 3 un => Ptotal = 4.8 kW
        // FD = 1.00, FP = 0.92 => 4.8 / 0.92 = 5.2173... => 5.22 kVA
        assertEquals(new BigDecimal("5.22"), subtotal.demandaKva());
        assertEquals(new BigDecimal("4.80"), subtotal.potenciaAtivaKw());
    }

    @Test
    @DisplayName("Teste Unitário 7: Parcela g - Força Motriz com Revezamento de Bombas")
    void testParcelaGMotoresComRevezamentoDeBombas() {
        List<MotorItem> items = List.of(
                new MotorItem("Bomba de Recalque 1 (Ativa)", new BigDecimal("5.0"), null, false, true),
                new MotorItem("Bomba de Recalque 2 (Reserva)", new BigDecimal("5.0"), null, true, true),
                new MotorItem("Exaustor Pressurização", new BigDecimal("3.0"), null, false, true)
        );

        CommonAreaSubtotal subtotal = engine.calculateParcelaG(items);

        // Tabela 19:
        // 5.0 cv => 5.27 kVA
        // 3.0 cv => 3.37 kVA
        // Bomba Reserva: FD = 0.00 => 0.00 kVA
        // Motores Ativos: 5.0 cv (5.27 kVA) e 3.0 cv (3.37 kVA)
        // Maior ativo (5.0 cv): FD = 1.00 => 5.27 kVA
        // Demais ativos (3.0 cv): FD = 0.50 => 3.37 * 0.50 = 1.685 kVA
        // Total = 5.27 + 1.685 = 6.955 => 6.96 kVA
        assertEquals(new BigDecimal("6.96"), subtotal.demandaKva());
    }

    @Test
    @DisplayName("Teste Unitário 8: Parcela h - Elevadores (Tabela 15)")
    void testParcelaHElevadoresTabela15() {
        List<ElevatorItem> items = List.of(
                new ElevatorItem("Elevador Social", new BigDecimal("11.0"), 1),
                new ElevatorItem("Elevador Serviço", new BigDecimal("11.0"), 1)
        );

        CommonAreaSubtotal subtotal = engine.calculateParcelaH(items);

        // 2 elevadores de 11.0 kW
        // Maior: 11.0 * 1.00 = 11.0 kW
        // Demais: 11.0 * 0.60 = 6.6 kW
        // Ponderado = 17.6 kW
        // FP = 0.92 => 17.6 / 0.92 = 19.1304... => 19.13 kVA
        assertEquals(new BigDecimal("19.13"), subtotal.demandaKva());
        assertEquals(new BigDecimal("22.00"), subtotal.potenciaAtivaKw());
    }

    @Test
    @DisplayName("Teste Unitário 9: Parcela i - Bombas de Piscina (Tabela 16)")
    void testParcelaIBombasPiscinaTabela16() {
        List<PoolPumpItem> items = List.of(
                new PoolPumpItem("Bomba Filtro Piscina", new BigDecimal("1.5"), 2)
        );

        CommonAreaSubtotal subtotal = engine.calculateParcelaI(items);

        // N = 2 => FD = 0.56 (Tabela 16)
        // Ptotal = 3.0 kW
        // Demanda = (3.0 * 0.56) / 0.80 = 1.68 / 0.80 = 2.10 kVA
        assertEquals(new BigDecimal("2.10"), subtotal.demandaKva());
        assertEquals(new BigDecimal("3.00"), subtotal.potenciaAtivaKw());
    }

    @Test
    @DisplayName("Teste Unitário 10: Cargas Personalizadas Manuais")
    void testCargasCustomizadas() {
        List<CustomLoadItem> items = List.of(
                new CustomLoadItem("Carga Especial", new BigDecimal("10.0"), new BigDecimal("0.85"), new BigDecimal("0.80"), 1)
        );

        CommonAreaSubtotal subtotal = engine.calculateCustomLoads(items);

        // Demanda = (10.0 * 0.80) / 0.85 = 8.00 / 0.85 = 9.4117... => 9.41 kVA
        assertEquals(new BigDecimal("9.41"), subtotal.demandaKva());
        assertEquals(new BigDecimal("10.00"), subtotal.potenciaAtivaKw());
    }

    @Test
    @DisplayName("Teste Unitário 11: Consolidado Completo Ds de Todas as Parcelas")
    void testConsolidadoCasoCompletoDs() {
        CommonAreaCalculationInput input = new CommonAreaCalculationInput(
                new LightingTugInput(new BigDecimal("10.0"), new BigDecimal("6.0")), // a = 15.50
                List.of(new WaterHeaterItem("Chuveiro", new BigDecimal("5.5"), 4)), // b = 16.72
                List.of(new BoilerItem("Boiler", new BigDecimal("6.0"), 2)), // c = 8.64
                List.of(new ApplianceItem("Lavadora", new BigDecimal("2.0"), 3)), // d = 4.57
                new CookingInput( // e = 15.40
                        List.of(new OvenItem("Forno", new BigDecimal("3.0"), 2)),
                        List.of(new CooktopItem("Cooktop", new BigDecimal("7.0"), 4))
                ),
                List.of(new AirConditioningItem("Split", 12000, null, null, 3)), // f = 5.22
                List.of( // g = 6.96
                        new MotorItem("Bomba 1", new BigDecimal("5.0"), null, false, true),
                        new MotorItem("Bomba 2", new BigDecimal("5.0"), null, true, true),
                        new MotorItem("Exaustor", new BigDecimal("3.0"), null, false, true)
                ),
                List.of(new ElevatorItem("Elevador", new BigDecimal("11.0"), 2)), // h = 19.13
                List.of(new PoolPumpItem("Bomba Piscina", new BigDecimal("1.5"), 2)), // i = 2.10
                List.of(new CustomLoadItem("Carga Especial", new BigDecimal("10.0"), new BigDecimal("0.85"), new BigDecimal("0.80"), 1)) // custom = 9.41
        );

        CommonAreaCalculationResult result = engine.calculate(input);

        // Soma esperada: 15.50 + 16.72 + 8.64 + 4.57 + 15.40 + 5.22 + 6.96 + 19.13 + 2.10 + 9.41 = 103.65 kVA
        assertEquals(new BigDecimal("103.65"), result.demandaTotalDsKva());
        assertEquals(new BigDecimal("103.65"), result.getDemandaServicosDsKva());
        assertEquals(new BigDecimal("15.50"), result.parcelaA_IluminacaoTug().demandaKva());
        assertEquals(new BigDecimal("16.72"), result.parcelaB_AquecedoresPassagem().demandaKva());
        assertEquals(new BigDecimal("8.64"), result.parcelaC_Boilers().demandaKva());
        assertEquals(new BigDecimal("4.57"), result.parcelaD_Eletrodomesticos().demandaKva());
        assertEquals(new BigDecimal("15.40"), result.parcelaE_CoccaoGourmet().demandaKva());
        assertEquals(new BigDecimal("5.22"), result.parcelaF_Climatizacao().demandaKva());
        assertEquals(new BigDecimal("6.96"), result.parcelaG_Motores().demandaKva());
        assertEquals(new BigDecimal("19.13"), result.parcelaH_Elevadores().demandaKva());
        assertEquals(new BigDecimal("2.10"), result.parcelaI_BombasPiscina().demandaKva());
        assertEquals(new BigDecimal("9.41"), result.parcelasCustomizadas().demandaKva());
    }

    @Test
    @DisplayName("Teste Unitário 12: Validações de Entrada Inválida")
    void testValidacoesDeEntradaInvalida() {
        assertThrows(IllegalArgumentException.class, () -> engine.calculate((CommonAreaCalculationInput) null));

        // Potência negativa
        assertThrows(IllegalArgumentException.class, () ->
                engine.calculateParcelaA(new LightingTugInput(new BigDecimal("-1.0"), BigDecimal.ZERO))
        );

        // FP fora do limite (0, 1.0]
        assertThrows(IllegalArgumentException.class, () ->
                engine.calculateCustomLoads(List.of(
                        new CustomLoadItem("Inválido", new BigDecimal("5.0"), new BigDecimal("1.2"), new BigDecimal("0.8"), 1)
                ))
        );

        // FD fora do limite [0, 1.0]
        assertThrows(IllegalArgumentException.class, () ->
                engine.calculateCustomLoads(List.of(
                        new CustomLoadItem("Inválido", new BigDecimal("5.0"), new BigDecimal("0.8"), new BigDecimal("-0.1"), 1)
                ))
        );
    }
}
