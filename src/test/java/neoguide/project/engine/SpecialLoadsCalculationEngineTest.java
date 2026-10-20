package neoguide.project.engine;

import neoguide.project.engine.model.ElectricVehicleLoadInput;
import neoguide.project.engine.model.ElectricVehicleLoadOutput;
import neoguide.project.engine.model.FirePumpLoadInput;
import neoguide.project.engine.model.FirePumpLoadOutput;
import neoguide.project.engine.model.SpecialLoadsCalculationInput;
import neoguide.project.engine.model.SpecialLoadsCalculationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SpecialLoadsCalculationEngineTest {

    private SpecialLoadsCalculationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new SpecialLoadsCalculationEngine();
    }

    @Test
    @DisplayName("Recarga VE sem vagas ou com potência zero deve retornar 0.00 kVA")
    void recargaVeicular_semVagas_deveRetornarZero() {
        ElectricVehicleLoadOutput out1 = engine.calcularRecargaVeicular(null);
        assertEquals(new BigDecimal("0.00"), out1.demandaDveKva());

        ElectricVehicleLoadInput inZero = new ElectricVehicleLoadInput(0, new BigDecimal("7.40"), null, null);
        ElectricVehicleLoadOutput out2 = engine.calcularRecargaVeicular(inZero);
        assertEquals(new BigDecimal("0.00"), out2.demandaDveKva());
        assertEquals(0, out2.vagas());
    }

    @Test
    @DisplayName("Recarga VE deve aplicar os fatores de simultaneidade do Quadro 33 da DIS-NOR-053")
    void recargaVeicular_faixasQuadro33_deveAplicarFatoresCorretos() {
        BigDecimal pEstacao = new BigDecimal("7.40");

        // 1 vaga -> Fve = 1.00 -> 7.40 kVA
        ElectricVehicleLoadOutput r1 = engine.calcularRecargaVeicular(new ElectricVehicleLoadInput(1, pEstacao, null, null));
        assertEquals(new BigDecimal("1.00"), r1.fatorSimultaneidadeFve());
        assertEquals(new BigDecimal("7.40"), r1.demandaDveKva());

        // 2 vagas -> Fve = 0.85 -> 2 * 7.40 * 0.85 = 12.58 kVA
        ElectricVehicleLoadOutput r2 = engine.calcularRecargaVeicular(new ElectricVehicleLoadInput(2, pEstacao, null, null));
        assertEquals(new BigDecimal("0.85"), r2.fatorSimultaneidadeFve());
        assertEquals(new BigDecimal("12.58"), r2.demandaDveKva());

        // 3 vagas -> Fve = 0.75 -> 3 * 7.40 * 0.75 = 16.65 kVA
        ElectricVehicleLoadOutput r3 = engine.calcularRecargaVeicular(new ElectricVehicleLoadInput(3, pEstacao, null, null));
        assertEquals(new BigDecimal("0.75"), r3.fatorSimultaneidadeFve());
        assertEquals(new BigDecimal("16.65"), r3.demandaDveKva());

        // 4 vagas -> Fve = 0.70 -> 4 * 7.40 * 0.70 = 20.72 kVA
        ElectricVehicleLoadOutput r4 = engine.calcularRecargaVeicular(new ElectricVehicleLoadInput(4, pEstacao, null, null));
        assertEquals(new BigDecimal("0.70"), r4.fatorSimultaneidadeFve());
        assertEquals(new BigDecimal("20.72"), r4.demandaDveKva());

        // 8 vagas -> Fve = 0.70 -> 8 * 7.40 * 0.70 = 41.44 kVA
        ElectricVehicleLoadOutput r8 = engine.calcularRecargaVeicular(new ElectricVehicleLoadInput(8, pEstacao, null, null));
        assertEquals(new BigDecimal("0.70"), r8.fatorSimultaneidadeFve());
        assertEquals(new BigDecimal("41.44"), r8.demandaDveKva());

        // 10 vagas -> Fve = 0.60 -> 10 * 7.40 * 0.60 = 44.40 kVA
        ElectricVehicleLoadOutput r10 = engine.calcularRecargaVeicular(new ElectricVehicleLoadInput(10, pEstacao, null, null));
        assertEquals(new BigDecimal("0.60"), r10.fatorSimultaneidadeFve());
        assertEquals(new BigDecimal("44.40"), r10.demandaDveKva());

        // 20 vagas -> Fve = 0.50 -> 20 * 7.40 * 0.50 = 74.00 kVA
        ElectricVehicleLoadOutput r20 = engine.calcularRecargaVeicular(new ElectricVehicleLoadInput(20, pEstacao, null, null));
        assertEquals(new BigDecimal("0.50"), r20.fatorSimultaneidadeFve());
        assertEquals(new BigDecimal("74.00"), r20.demandaDveKva());

        // 25 vagas -> Fve = 0.45 -> 25 * 7.40 * 0.45 = 83.25 kVA
        ElectricVehicleLoadOutput r25 = engine.calcularRecargaVeicular(new ElectricVehicleLoadInput(25, pEstacao, null, null));
        assertEquals(new BigDecimal("0.45"), r25.fatorSimultaneidadeFve());
        assertEquals(new BigDecimal("83.25"), r25.demandaDveKva());

        // 35 vagas -> Fve = 0.35 -> 35 * 7.40 * 0.35 = 90.65 kVA
        ElectricVehicleLoadOutput r35 = engine.calcularRecargaVeicular(new ElectricVehicleLoadInput(35, pEstacao, null, null));
        assertEquals(new BigDecimal("0.35"), r35.fatorSimultaneidadeFve());
        assertEquals(new BigDecimal("90.65"), r35.demandaDveKva());
    }

    @Test
    @DisplayName("Fator de simultaneidade customizado deve ter prioridade")
    void recargaVeicular_fatorCustomizado_devePrevalecer() {
        ElectricVehicleLoadInput custom = new ElectricVehicleLoadInput(10, new BigDecimal("10.00"), new BigDecimal("0.80"), new BigDecimal("0.95"));
        ElectricVehicleLoadOutput result = engine.calcularRecargaVeicular(custom);

        assertEquals(new BigDecimal("0.80"), result.fatorSimultaneidadeFve());
        assertEquals(new BigDecimal("0.95"), result.fatorPotencia());
        // 10 * 10 * 0.80 / 0.95 = 84.2105... -> 84.21 kVA
        assertEquals(new BigDecimal("84.21"), result.demandaDveKva());
    }

    @Test
    @DisplayName("Validações defensivas de entrada para recarga veicular")
    void recargaVeicular_validacoesDefensivas() {
        assertThrows(IllegalArgumentException.class, () -> new ElectricVehicleLoadInput(-1, new BigDecimal("7.40"), null, null));
        assertThrows(IllegalArgumentException.class, () -> new ElectricVehicleLoadInput(2, new BigDecimal("-7.40"), null, null));
        assertThrows(IllegalArgumentException.class, () -> new ElectricVehicleLoadInput(2, new BigDecimal("7.40"), null, new BigDecimal("0.00")));
        assertThrows(IllegalArgumentException.class, () -> new ElectricVehicleLoadInput(2, new BigDecimal("7.40"), null, new BigDecimal("1.05")));
    }

    @Test
    @DisplayName("Motobomba de incêndio deve resolver lookup da Tabela 19 da DIS-NOR-030")
    void motobombaIncendio_tabela19ValoresNormativos() {
        // 0.50 CV -> 0.75 kVA, DIRETA
        FirePumpLoadOutput b05 = engine.calcularMotobombaIncendio(FirePumpLoadInput.padrao(new BigDecimal("0.50")));
        assertEquals(new BigDecimal("0.75"), b05.demandaDmbiKva());
        assertEquals("DIRETA", b05.chavePartidaSugerida());

        // 10.00 CV -> 9.95 kVA, ESTRELA_TRIANGULO
        FirePumpLoadOutput b10 = engine.calcularMotobombaIncendio(FirePumpLoadInput.padrao(new BigDecimal("10.00")));
        assertEquals(new BigDecimal("9.95"), b10.demandaDmbiKva());
        assertEquals("ESTRELA_TRIANGULO", b10.chavePartidaSugerida());

        // 15.00 CV -> 14.40 kVA, ESTRELA_TRIANGULO
        FirePumpLoadOutput b15 = engine.calcularMotobombaIncendio(FirePumpLoadInput.padrao(new BigDecimal("15.00")));
        assertEquals(new BigDecimal("14.40"), b15.demandaDmbiKva());
        assertEquals("ESTRELA_TRIANGULO", b15.chavePartidaSugerida());

        // 25.00 CV -> 23.50 kVA, COMPENSADORA
        FirePumpLoadOutput b25 = engine.calcularMotobombaIncendio(FirePumpLoadInput.padrao(new BigDecimal("25.00")));
        assertEquals(new BigDecimal("23.50"), b25.demandaDmbiKva());
        assertEquals("COMPENSADORA", b25.chavePartidaSugerida());

        // 30.00 CV -> 28.00 kVA, SOFT_STARTER
        FirePumpLoadOutput b30 = engine.calcularMotobombaIncendio(FirePumpLoadInput.padrao(new BigDecimal("30.00")));
        assertEquals(new BigDecimal("28.00"), b30.demandaDmbiKva());
        assertEquals("SOFT_STARTER", b30.chavePartidaSugerida());
    }

    @Test
    @DisplayName("Demanda customizada da motobomba deve prevalecer sobre o lookup da tabela")
    void motobombaIncendio_demandaCustomizada_devePrevalecer() {
        FirePumpLoadInput custom = new FirePumpLoadInput(new BigDecimal("15.00"), new BigDecimal("18.50"), true);
        FirePumpLoadOutput output = engine.calcularMotobombaIncendio(custom);

        assertEquals(new BigDecimal("18.50"), output.demandaDmbiKva());
        assertTrue(output.derivadoAntesDoQdg());
    }

    @Test
    @DisplayName("Regra de Isolamento do QDG: Bomba de incêndio derivada a montante é deduzida do QDG")
    void isolamentoQdg_derivadoAntesDoQdg_deduzTotalmenteDaBarraQdg() {
        ElectricVehicleLoadInput ve = new ElectricVehicleLoadInput(4, new BigDecimal("7.40"), null, null); // 20.72 kVA
        FirePumpLoadInput mbi = FirePumpLoadInput.padrao(new BigDecimal("15.00")); // 14.40 kVA, derivada antes do QDG

        SpecialLoadsCalculationInput input = SpecialLoadsCalculationInput.apenas(ve, mbi);
        SpecialLoadsCalculationResult result = engine.calculate(input);

        // Ded = Dve (20.72) + Dmbi (14.40) = 35.12 kVA
        assertEquals(new BigDecimal("35.12"), result.demandaEspecialTotalKva());
        // Parcela dedutível = Dmbi = 14.40 kVA
        assertEquals(new BigDecimal("14.40"), result.parcelaDedutivelQdgKva());
        // Contribuição ao QDG = Ded - Dmbi = 20.72 kVA (apenas VE)
        assertEquals(new BigDecimal("20.72"), result.contribuicaoQdgKva());
        assertTrue(result.notaAuditoria().contains("DQDG = Ded - Dmbi"));
    }

    @Test
    @DisplayName("Quando bomba não for derivada a montante do QDG, não há dedução da barra")
    void isolamentoQdg_naoDerivado_somaNoQdg() {
        ElectricVehicleLoadInput ve = new ElectricVehicleLoadInput(2, new BigDecimal("7.40"), null, null); // 12.58 kVA
        FirePumpLoadInput mbi = new FirePumpLoadInput(new BigDecimal("10.00"), null, false); // 9.95 kVA, interna ao QDG

        SpecialLoadsCalculationInput input = SpecialLoadsCalculationInput.apenas(ve, mbi);
        SpecialLoadsCalculationResult result = engine.calculate(input);

        assertEquals(new BigDecimal("22.53"), result.demandaEspecialTotalKva());
        assertEquals(new BigDecimal("0.00"), result.parcelaDedutivelQdgKva());
        assertEquals(new BigDecimal("22.53"), result.contribuicaoQdgKva());
    }

    @Test
    @DisplayName("Alerta Preventivo de 50% do Trafo quando Ds + Dve > 0.50 * Strafo")
    void alertaTrafo50_quandoUltrapassa_deveSinalizarTrueEPercentual() {
        ElectricVehicleLoadInput ve = new ElectricVehicleLoadInput(4, new BigDecimal("7.40"), null, null); // 20.72 kVA
        FirePumpLoadInput mbi = FirePumpLoadInput.padrao(new BigDecimal("10.00"));

        // Strafo = 100 kVA (limite 50% = 50 kVA). Ds = 35 kVA. Ds + Dve = 35 + 20.72 = 55.72 kVA > 50 kVA
        SpecialLoadsCalculationInput input = new SpecialLoadsCalculationInput(
                ve, mbi, new BigDecimal("100.00"), new BigDecimal("35.00")
        );

        SpecialLoadsCalculationResult result = engine.calculate(input);

        assertTrue(result.alertaTrafo50Excedido());
        assertEquals(new BigDecimal("55.72"), result.percentualUsoTrafoVeDs());
        assertTrue(result.notaAuditoria().contains("ATENÇÃO NORMATIVA"));
    }

    @Test
    @DisplayName("Alerta Preventivo de 50% do Trafo quando Ds + Dve <= 0.50 * Strafo deve ser false")
    void alertaTrafo50_quandoDentroDoLimite_deveSinalizarFalse() {
        ElectricVehicleLoadInput ve = new ElectricVehicleLoadInput(2, new BigDecimal("7.40"), null, null); // 12.58 kVA
        FirePumpLoadInput mbi = FirePumpLoadInput.padrao(new BigDecimal("10.00"));

        // Strafo = 150 kVA (limite 50% = 75 kVA). Ds = 40 kVA. Ds + Dve = 40 + 12.58 = 52.58 kVA <= 75 kVA (35.05%)
        SpecialLoadsCalculationInput input = new SpecialLoadsCalculationInput(
                ve, mbi, new BigDecimal("150.00"), new BigDecimal("40.00")
        );

        SpecialLoadsCalculationResult result = engine.calculate(input);

        assertFalse(result.alertaTrafo50Excedido());
        assertEquals(new BigDecimal("35.05"), result.percentualUsoTrafoVeDs());
    }
}
