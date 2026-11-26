package neoguide.project.engine;

import neoguide.project.engine.model.CommercialCalculationResult;
import neoguide.project.engine.model.CommercialUnitInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Casos da DIS-NOR-030 Tabela 22 para a etapa comercial: Dc por espaço e soma dos espaços. */
class CommercialCalculationEngineTest {

    private static final BigDecimal FP = new BigDecimal("0.92");
    private static final BigDecimal FD_UM = new BigDecimal("1.00");
    private static final BigDecimal CARGA_MINIMA_LOJA = new BigDecimal("30.00");

    private CommercialCalculationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CommercialCalculationEngine();
    }

    private static CommercialUnitInput loja(String area, String potencia) {
        return new CommercialUnitInput("LOJAS", "Lojas e Vitrines", bd(area), bd(potencia), CARGA_MINIMA_LOJA, FD_UM, FP, false);
    }

    private static CommercialUnitInput escritorio(String area, String potencia) {
        return new CommercialUnitInput("ESCRITORIOS", "Escritórios e Salas Comerciais", bd(area), bd(potencia),
                new BigDecimal("30.00"), FD_UM, FP, true);
    }

    private static BigDecimal bd(String valor) {
        return valor == null ? null : new BigDecimal(valor);
    }

    @Test
    @DisplayName("Lista vazia: Dc = 0,00 kVA")
    void listaVazia_dcZero() {
        CommercialCalculationResult resultado = engine.calcular(List.of());
        assertEquals(new BigDecimal("0.00"), resultado.demandaComercialDcKva());
        assertEquals(new BigDecimal("0.00"), resultado.potenciaTotalInstaladaKw());
        assertTrue(resultado.unidadesDetalhadas().isEmpty());
    }

    @Test
    @DisplayName("Loja de 20 kW (sem escalonamento): Dc = 20 / 0,92 = 21,74 kVA")
    void loja20kw_dc2174() {
        CommercialCalculationResult resultado = engine.calcular(List.of(loja("120", "20")));
        assertEquals(new BigDecimal("21.74"), resultado.demandaComercialDcKva());
        assertEquals(new BigDecimal("21.74"), resultado.unidadesDetalhadas().get(0).demandaCalculadaKva());
    }

    @Test
    @DisplayName("Escritório de 30 kW (escalonado): Dc = [20 × 1,00 + 10 × 0,70] / 0,92 = 29,35 kVA")
    void escritorio30kw_dc2935() {
        CommercialCalculationResult resultado = engine.calcular(List.of(escritorio("85", "30")));
        assertEquals(new BigDecimal("29.35"), resultado.demandaComercialDcKva());
    }

    @Test
    @DisplayName("Escritório de 20 kW: sem excedente, Dc = 21,74 kVA")
    void escritorio20kw_semExcedente() {
        CommercialCalculationResult resultado = engine.calcular(List.of(escritorio("85", "20")));
        assertEquals(new BigDecimal("21.74"), resultado.demandaComercialDcKva());
    }

    @Test
    @DisplayName("Loja de 50 kW não usa o escalonamento de escritórios: 50 / 0,92 = 54,35 kVA")
    void lojaGrande_naoEscalona() {
        CommercialCalculationResult resultado = engine.calcular(List.of(loja("200", "50")));
        assertEquals(new BigDecimal("54.35"), resultado.demandaComercialDcKva());
    }

    @Test
    @DisplayName("Dc total é a soma dos Dc dos espaços: 21,74 + 29,35 = 51,09 kVA")
    void somaDosEspacos() {
        CommercialCalculationResult resultado = engine.calcular(List.of(loja("120", "20"), escritorio("85", "30")));
        assertEquals(new BigDecimal("51.09"), resultado.demandaComercialDcKva());
        assertEquals(new BigDecimal("50.00"), resultado.potenciaTotalInstaladaKw());
        assertEquals(2, resultado.unidadesDetalhadas().size());
    }

    @Test
    @DisplayName("Espaço sem carga: Dc = 0 e fator de demanda efetivo = 0")
    void espacoSemCarga_dcZero() {
        CommercialCalculationResult resultado = engine.calcular(List.of(loja("50", "0")));
        assertEquals(new BigDecimal("0.00"), resultado.demandaComercialDcKva());
        assertEquals(0, BigDecimal.ZERO.compareTo(resultado.unidadesDetalhadas().get(0).fatorDemandaEfetivo()));
    }

    @Test
    @DisplayName("Carga abaixo do mínimo da ocupação gera alerta, sem bloquear o cálculo")
    void cargaAbaixoDoMinimo_geraAlerta() {
        // 120 m² × 30 W/m² = 3,60 kW de carga mínima; instalado 2 kW
        CommercialCalculationResult resultado = engine.calcular(List.of(loja("120", "2")));
        assertEquals("3,60", resultado.unidadesDetalhadas().get(0).cargaMinimaKw().toPlainString().replace('.', ','));
        assertEquals("Carga abaixo do mínimo da ocupação (3,60 kW para 120 m²).",
                resultado.unidadesDetalhadas().get(0).alerta());
        assertEquals(new BigDecimal("2.17"), resultado.demandaComercialDcKva());
    }

    @Test
    @DisplayName("Carga igual ou acima do mínimo não gera alerta")
    void cargaAcimaDoMinimo_semAlerta() {
        CommercialCalculationResult resultado = engine.calcular(List.of(loja("120", "3.6")));
        assertNull(resultado.unidadesDetalhadas().get(0).alerta());
    }

    @Test
    @DisplayName("Sem área informada não há carga mínima nem alerta")
    void semArea_semCargaMinima() {
        CommercialCalculationResult resultado = engine.calcular(List.of(loja(null, "1")));
        assertNull(resultado.unidadesDetalhadas().get(0).cargaMinimaKw());
        assertNull(resultado.unidadesDetalhadas().get(0).alerta());
    }
}
