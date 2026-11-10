package neoguide.project.engine;

import neoguide.project.engine.model.CommercialCalculationInput;
import neoguide.project.engine.model.CommercialCalculationResult;
import neoguide.project.engine.model.CommercialUnitInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommercialCalculationEngineTest {

    private CommercialCalculationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CommercialCalculationEngine();
    }

    @Test
    @DisplayName("Sem comercial deve retornar 0.00 kVA")
    void semComercial_deveRetornarZeroKva() {
        CommercialCalculationInput inputNulo = null;
        CommercialCalculationResult resNulo = engine.calcularDemandaComercial(inputNulo);
        assertEquals(new BigDecimal("0.00"), resNulo.demandaComercialDcKva());

        CommercialCalculationInput inputFalso = new CommercialCalculationInput(false, Collections.emptyList());
        CommercialCalculationResult resFalso = engine.calcularDemandaComercial(inputFalso);
        assertEquals(new BigDecimal("0.00"), resFalso.demandaComercialDcKva());
        assertEquals(new BigDecimal("0.00"), resFalso.potenciaTotalInstaladaKw());

        CommercialCalculationInput inputVazio = new CommercialCalculationInput(true, Collections.emptyList());
        CommercialCalculationResult resVazio = engine.calcularDemandaComercial(inputVazio);
        assertEquals(new BigDecimal("0.00"), resVazio.demandaComercialDcKva());
    }

    @Test
    @DisplayName("Potência total menor ou igual a 20 kW deve aplicar 100% de fator de demanda")
    void potenciaMenorOuIgual20kW_deveAplicar100PorCento() {
        CommercialUnitInput u1 = new CommercialUnitInput("Loja 01", "Boutique", new BigDecimal("35.00"), new BigDecimal("8.00"));
        CommercialUnitInput u2 = new CommercialUnitInput("Sala 101", "Escritório", new BigDecimal("25.00"), new BigDecimal("8.00"));
        CommercialCalculationInput input = new CommercialCalculationInput(true, List.of(u1, u2));

        CommercialCalculationResult result = engine.calcularDemandaComercial(input);

        assertNotNull(result);
        assertEquals(new BigDecimal("16.00"), result.potenciaTotalInstaladaKw());
        assertEquals(new BigDecimal("16.00"), result.potenciaPrimeiraFaixaKw());
        assertEquals(new BigDecimal("0.00"), result.potenciaExcedenteKw());
        assertEquals(new BigDecimal("16.00"), result.demandaAtivaPonderadaKw());
        // 16.00 / 0.92 = 17.3913... -> 17.39 kVA
        assertEquals(new BigDecimal("17.39"), result.demandaComercialDcKva());
        assertEquals(2, result.unidadesDetalhadas().size());
        assertEquals(new BigDecimal("1.0000"), result.unidadesDetalhadas().get(0).fatorDemandaPonderado());
        // 8.00 * 1.0 / 0.92 = 8.6956... -> 8.70 kVA
        assertEquals(new BigDecimal("8.70"), result.unidadesDetalhadas().get(0).demandaCalculadaKva());
    }

    @Test
    @DisplayName("Potência total superior a 20 kW deve aplicar regressão de 70% sobre o excedente")
    void potenciaMaiorQue20kW_deveAplicarRegressaoExcedente() {
        // Exemplo da norma com 25 kW totais
        CommercialUnitInput u1 = new CommercialUnitInput("Loja 01", "Padaria", new BigDecimal("80.00"), new BigDecimal("25.00"));
        CommercialCalculationInput input = new CommercialCalculationInput(true, List.of(u1));

        CommercialCalculationResult result = engine.calcularDemandaComercial(input);

        assertNotNull(result);
        assertEquals(new BigDecimal("25.00"), result.potenciaTotalInstaladaKw());
        assertEquals(new BigDecimal("20.00"), result.potenciaPrimeiraFaixaKw());
        assertEquals(new BigDecimal("5.00"), result.potenciaExcedenteKw());
        // 20.00 * 1.00 + 5.00 * 0.70 = 23.50 kW
        assertEquals(new BigDecimal("23.50"), result.demandaAtivaPonderadaKw());
        // 23.50 / 0.92 = 25.5434... -> 25.54 kVA
        assertEquals(new BigDecimal("25.54"), result.demandaComercialDcKva());
        assertTrue(result.memoriaCalculo().contains("25.54 kVA"));
    }

    @Test
    @DisplayName("Potência negativa deve lançar IllegalArgumentException")
    void potenciaNegativa_deveLancarIllegalArgumentException() {
        CommercialUnitInput u1 = new CommercialUnitInput("Loja 01", "Farmácia", new BigDecimal("40.00"), new BigDecimal("-5.00"));
        CommercialCalculationInput input = new CommercialCalculationInput(true, List.of(u1));

        assertThrows(IllegalArgumentException.class, () -> engine.calcularDemandaComercial(input));
    }
}
