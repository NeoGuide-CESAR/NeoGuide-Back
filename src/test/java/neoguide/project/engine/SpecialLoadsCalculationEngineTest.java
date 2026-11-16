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
    @DisplayName("Recarga VE deve aplicar o Quadro 33 (DIS-NOR-053 Rev. 06) sem interpolação, com P = 7,40 kW")
    void recargaVeicular_faixasQuadro33_deveAplicarFatoresCorretos() {
        BigDecimal pEstacao = new BigDecimal("7.40");

        // 1 vaga -> Fve = 1.00 -> 7.40 kVA
        ElectricVehicleLoadOutput r1 = engine.calcularRecargaVeicular(new ElectricVehicleLoadInput(1, pEstacao, null, null));
        assertEquals(new BigDecimal("1.00"), r1.fatorSimultaneidadeFve());
        assertEquals(new BigDecimal("7.40"), r1.demandaDveKva());

        // 2 vagas (1–10) -> Fve = 1.00 -> 2 * 7.40 = 14.80 kVA
        ElectricVehicleLoadOutput r2 = engine.calcularRecargaVeicular(new ElectricVehicleLoadInput(2, pEstacao, null, null));
        assertEquals(new BigDecimal("1.00"), r2.fatorSimultaneidadeFve());
        assertEquals(new BigDecimal("14.80"), r2.demandaDveKva());

        // 4 pontos -> Fve = 1.00 (1–10) -> 4 * 7.40 = 29.60 kVA (caso R3)
        ElectricVehicleLoadOutput r4 = engine.calcularRecargaVeicular(new ElectricVehicleLoadInput(4, pEstacao, null, null));
        assertEquals(new BigDecimal("1.00"), r4.fatorSimultaneidadeFve());
        assertEquals(new BigDecimal("29.60"), r4.demandaDveKva());

        // 10 pontos -> Fve = 1.00 -> 74.00 kVA
        ElectricVehicleLoadOutput r10 = engine.calcularRecargaVeicular(new ElectricVehicleLoadInput(10, pEstacao, null, null));
        assertEquals(new BigDecimal("1.00"), r10.fatorSimultaneidadeFve());
        assertEquals(new BigDecimal("74.00"), r10.demandaDveKva());

        // 20 pontos (11–20) -> Fve = 0.86 -> 20 * 7.40 * 0.86 = 127.28 kVA
        ElectricVehicleLoadOutput r20 = engine.calcularRecargaVeicular(new ElectricVehicleLoadInput(20, pEstacao, null, null));
        assertEquals(new BigDecimal("0.86"), r20.fatorSimultaneidadeFve());
        assertEquals(new BigDecimal("127.28"), r20.demandaDveKva());

        // 25 pontos (21–30) -> Fve = 0.80 -> 25 * 7.40 * 0.80 = 148.00 kVA
        ElectricVehicleLoadOutput r25 = engine.calcularRecargaVeicular(new ElectricVehicleLoadInput(25, pEstacao, null, null));
        assertEquals(new BigDecimal("0.80"), r25.fatorSimultaneidadeFve());
        assertEquals(new BigDecimal("148.00"), r25.demandaDveKva());

        // 35 pontos (31–40) -> Fve = 0.78 -> 35 * 7.40 * 0.78 = 202.02 kVA
        ElectricVehicleLoadOutput r35 = engine.calcularRecargaVeicular(new ElectricVehicleLoadInput(35, pEstacao, null, null));
        assertEquals(new BigDecimal("0.78"), r35.fatorSimultaneidadeFve());
        assertEquals(new BigDecimal("202.02"), r35.demandaDveKva());
    }

    @Test
    @DisplayName("Regressão: valores antigos do Quadro 33 (2 vagas = 0,85; 4 = 0,80; 8 = 0,70) não devem voltar")
    void recargaVeicular_regressaoValoresAntigos() {
        assertNotEquals(new BigDecimal("0.85"), engine.resolverFatorSimultaneidadeVe(2));
        assertNotEquals(new BigDecimal("0.80"), engine.resolverFatorSimultaneidadeVe(4));
        assertNotEquals(new BigDecimal("0.70"), engine.resolverFatorSimultaneidadeVe(8));
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
    @DisplayName("Motobomba de incêndio deve usar a Tabela 19 da DIS-NOR-030 Rev. 07 (coluna VA)")
    void motobombaIncendio_tabela19ValoresNormativos() {
        // R9: 1 cv -> 1.52 kVA; 5 cv -> 6.02 kVA; 0.50 cv -> 0.87 kVA (DIRETA)
        assertEquals(new BigDecimal("1.52"), engine.calcularMotobombaIncendio(FirePumpLoadInput.padrao(new BigDecimal("1.00"))).demandaDmbiKva());
        assertEquals(new BigDecimal("6.02"), engine.calcularMotobombaIncendio(FirePumpLoadInput.padrao(new BigDecimal("5.00"))).demandaDmbiKva());

        // 0.50 CV -> 0.87 kVA, DIRETA
        FirePumpLoadOutput b05 = engine.calcularMotobombaIncendio(FirePumpLoadInput.padrao(new BigDecimal("0.50")));
        assertEquals(new BigDecimal("0.87"), b05.demandaDmbiKva());
        assertEquals("DIRETA", b05.chavePartidaSugerida());

        // R7: 2 cv -> 2.70 kVA (não entra no DQDG)
        FirePumpLoadOutput b2 = engine.calcularMotobombaIncendio(FirePumpLoadInput.padrao(new BigDecimal("2.00")));
        assertEquals(new BigDecimal("2.70"), b2.demandaDmbiKva());

        // 10.00 CV -> 11.54 kVA, ESTRELA_TRIANGULO
        FirePumpLoadOutput b10 = engine.calcularMotobombaIncendio(FirePumpLoadInput.padrao(new BigDecimal("10.00")));
        assertEquals(new BigDecimal("11.54"), b10.demandaDmbiKva());
        assertEquals("ESTRELA_TRIANGULO", b10.chavePartidaSugerida());

        // 15.00 CV -> 16.65 kVA, ESTRELA_TRIANGULO
        FirePumpLoadOutput b15 = engine.calcularMotobombaIncendio(FirePumpLoadInput.padrao(new BigDecimal("15.00")));
        assertEquals(new BigDecimal("16.65"), b15.demandaDmbiKva());
        assertEquals("ESTRELA_TRIANGULO", b15.chavePartidaSugerida());

        // 25.00 CV -> 25.83 kVA, COMPENSADORA
        FirePumpLoadOutput b25 = engine.calcularMotobombaIncendio(FirePumpLoadInput.padrao(new BigDecimal("25.00")));
        assertEquals(new BigDecimal("25.83"), b25.demandaDmbiKva());
        assertEquals("COMPENSADORA", b25.chavePartidaSugerida());

        // 30.00 CV -> 30.52 kVA, SOFT_STARTER
        FirePumpLoadOutput b30 = engine.calcularMotobombaIncendio(FirePumpLoadInput.padrao(new BigDecimal("30.00")));
        assertEquals(new BigDecimal("30.52"), b30.demandaDmbiKva());
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
        ElectricVehicleLoadInput ve = new ElectricVehicleLoadInput(4, new BigDecimal("7.40"), null, null); // 29.60 kVA (FD 1.00)
        FirePumpLoadInput mbi = FirePumpLoadInput.padrao(new BigDecimal("15.00")); // 16.65 kVA, derivada antes do QDG

        SpecialLoadsCalculationInput input = SpecialLoadsCalculationInput.apenas(ve, mbi);
        SpecialLoadsCalculationResult result = engine.calculate(input);

        // Ded = Dve (29.60) + Dmbi (16.65) = 46.25 kVA
        assertEquals(new BigDecimal("46.25"), result.demandaEspecialTotalKva());
        // Parcela dedutível = Dmbi = 16.65 kVA
        assertEquals(new BigDecimal("16.65"), result.parcelaDedutivelQdgKva());
        // Contribuição ao QDG = Ded - Dmbi = 29.60 kVA (apenas VE)
        assertEquals(new BigDecimal("29.60"), result.contribuicaoQdgKva());
        assertTrue(result.notaAuditoria().contains("DQDG = Ded - Dmbi"));
    }

    @Test
    @DisplayName("Quando bomba não for derivada a montante do QDG, não há dedução da barra")
    void isolamentoQdg_naoDerivado_somaNoQdg() {
        ElectricVehicleLoadInput ve = new ElectricVehicleLoadInput(2, new BigDecimal("7.40"), null, null); // 14.80 kVA (FD 1.00)
        FirePumpLoadInput mbi = new FirePumpLoadInput(new BigDecimal("10.00"), null, false); // 11.54 kVA, interna ao QDG

        SpecialLoadsCalculationInput input = SpecialLoadsCalculationInput.apenas(ve, mbi);
        SpecialLoadsCalculationResult result = engine.calculate(input);

        assertEquals(new BigDecimal("26.34"), result.demandaEspecialTotalKva());
        assertEquals(new BigDecimal("0.00"), result.parcelaDedutivelQdgKva());
        assertEquals(new BigDecimal("26.34"), result.contribuicaoQdgKva());
    }

    @Test
    @DisplayName("Alerta Preventivo de 50% do Trafo quando Ds + Dve > 0.50 * Strafo")
    void alertaTrafo50_quandoUltrapassa_deveSinalizarTrueEPercentual() {
        ElectricVehicleLoadInput ve = new ElectricVehicleLoadInput(4, new BigDecimal("7.40"), null, null); // 29.60 kVA
        FirePumpLoadInput mbi = FirePumpLoadInput.padrao(new BigDecimal("10.00"));

        // Strafo = 100 kVA (limite 50% = 50 kVA). Ds = 35 kVA. Ds + Dve = 35 + 29.60 = 64.60 kVA > 50 kVA
        SpecialLoadsCalculationInput input = new SpecialLoadsCalculationInput(
                ve, mbi, new BigDecimal("100.00"), new BigDecimal("35.00")
        );

        SpecialLoadsCalculationResult result = engine.calculate(input);

        assertTrue(result.alertaTrafo50Excedido());
        assertEquals(new BigDecimal("64.60"), result.percentualUsoTrafoVeDs());
        assertTrue(result.notaAuditoria().contains("ATENÇÃO NORMATIVA"));
    }

    @Test
    @DisplayName("Alerta Preventivo de 50% do Trafo quando Ds + Dve <= 0.50 * Strafo deve ser false")
    void alertaTrafo50_quandoDentroDoLimite_deveSinalizarFalse() {
        ElectricVehicleLoadInput ve = new ElectricVehicleLoadInput(2, new BigDecimal("7.40"), null, null); // 14.80 kVA
        FirePumpLoadInput mbi = FirePumpLoadInput.padrao(new BigDecimal("10.00"));

        // Strafo = 150 kVA (limite 50% = 75 kVA). Ds = 40 kVA. Ds + Dve = 40 + 14.80 = 54.80 kVA (36.53%)
        SpecialLoadsCalculationInput input = new SpecialLoadsCalculationInput(
                ve, mbi, new BigDecimal("150.00"), new BigDecimal("40.00")
        );

        SpecialLoadsCalculationResult result = engine.calculate(input);

        assertFalse(result.alertaTrafo50Excedido());
        assertEquals(new BigDecimal("36.53"), result.percentualUsoTrafoVeDs());
    }
}
