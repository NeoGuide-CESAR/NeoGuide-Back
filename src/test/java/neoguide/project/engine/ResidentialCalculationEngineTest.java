package neoguide.project.engine;

import neoguide.project.engine.model.ResidentialCalculationInput;
import neoguide.project.engine.model.ResidentialCalculationResult;
import neoguide.project.engine.model.ResidentialTypologyItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ResidentialCalculationEngine — Motor de Cálculo da Demanda Residencial (DIS-NOR-053 Rev. 06)")
class ResidentialCalculationEngineTest {

    private ResidentialCalculationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ResidentialCalculationEngine();
    }

    /** Apresentação com 2 casas (regra: arredondar só na apresentação, B-MT-07). */
    private static BigDecimal apresentado(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    @Nested
    @DisplayName("1. Caso Canônico do Memorial Descritivo (100 unidades)")
    class MemorialCanonicoTests {

        @Test
        @DisplayName("Deve calcular o memorial de 100 unidades (50x63m² e 50x68m²) sem arredondamento intermediário")
        void testMemorialCanonico100Apartamentos() {
            // GIVEN: 50 unidades de 63m² (Di=1.47 kVA) + 50 unidades de 68m² (Di=1.57 kVA)
            var tipoA = new ResidentialTypologyItem("Tipo A - 63m²", new BigDecimal("63.00"), 50, new BigDecimal("1.47"));
            var tipoB = new ResidentialTypologyItem("Tipo B - 68m²", new BigDecimal("68.00"), 50, new BigDecimal("1.57"));

            var input = new ResidentialCalculationInput(List.of(tipoA, tipoB), false);

            // WHEN
            ResidentialCalculationResult result = engine.calculate(input);

            // THEN
            assertThat(result.getTotalApartamentos()).isEqualTo(100);
            assertThat(result.getDemandaBrutaDr()).isEqualByComparingTo("152.00");
            assertThat(result.getFatorCoincidenciaFc()).isEqualByComparingTo("0.6823");
            // Valores brutos: Dtr = 152.00 * 0.6823 = 103.7096; Drf = 103.7096 * 1.10 = 114.08056
            assertThat(result.getDemandaProvisoriaDtr()).isEqualByComparingTo("103.7096");
            assertThat(result.getFatorSegurancaFr()).isEqualByComparingTo("1.10");
            assertThat(result.getDemandaFinalDrf()).isEqualByComparingTo("114.08056");
            // Apresentação (2 casas)
            assertThat(apresentado(result.getDemandaProvisoriaDtr())).isEqualByComparingTo("103.71");
            assertThat(apresentado(result.getDemandaFinalDrf())).isEqualByComparingTo("114.08");
            assertThat(result.isSmartStudio()).isFalse();

            // Subtotais analíticos das tipologias
            assertThat(result.getTipologias()).hasSize(2);

            var outA = result.getTipologias().get(0);
            assertThat(outA.getNome()).isEqualTo("Tipo A - 63m²");
            assertThat(outA.getAreaM2()).isEqualByComparingTo("63.00");
            assertThat(outA.getQuantidade()).isEqualTo(50);
            assertThat(outA.getDemandaUnitariaDi()).isEqualByComparingTo("1.47");
            assertThat(outA.getDemandaSubtotal()).isEqualByComparingTo("73.50");

            var outB = result.getTipologias().get(1);
            assertThat(outB.getNome()).isEqualTo("Tipo B - 68m²");
            assertThat(outB.getAreaM2()).isEqualByComparingTo("68.00");
            assertThat(outB.getQuantidade()).isEqualTo(50);
            assertThat(outB.getDemandaUnitariaDi()).isEqualByComparingTo("1.57");
            assertThat(outB.getDemandaSubtotal()).isEqualByComparingTo("78.50");
        }

        @Test
        @DisplayName("Deve inferir Di do Quadro 35 caso a demanda unitária não seja informada no memorial de 100 unidades")
        void testMemorialCanonicoComInferenciaDi() {
            var tipoA = new ResidentialTypologyItem("Tipo A", new BigDecimal("63.00"), 50);
            var tipoB = new ResidentialTypologyItem("Tipo B", new BigDecimal("68.00"), 50);

            ResidentialCalculationResult result = engine.calculate(List.of(tipoA, tipoB), false);

            assertThat(result.getDemandaBrutaDr()).isEqualByComparingTo("152.00");
            assertThat(result.getFatorCoincidenciaFc()).isEqualByComparingTo("0.6823");
            assertThat(apresentado(result.getDemandaProvisoriaDtr())).isEqualByComparingTo("103.71");
            assertThat(result.getFatorSegurancaFr()).isEqualByComparingTo("1.10");
            assertThat(apresentado(result.getDemandaFinalDrf())).isEqualByComparingTo("114.08");
        }
    }

    @Nested
    @DisplayName("2. Regra Regulatória de Smart Studios (Item 6.25 DIS-NOR-053)")
    class SmartStudiosTests {

        @Test
        @DisplayName("Deve fixar Fc = 0.9000 para Smart Studios com N > 15 (ex.: 20 unidades de 35m², Di = 1.00 pelo Quadro 35)")
        void testSmartStudiosAcimaDe15Unidades() {
            // GIVEN: 20 unidades x 35m² (Di = 1.00 kVA, Quadro 35 "Até 40 m²"), isSmartStudio = true
            var studio = new ResidentialTypologyItem("Studio", new BigDecimal("35.00"), 20);

            // WHEN
            ResidentialCalculationResult result = engine.calculate(List.of(studio), true);

            // THEN
            assertThat(result.getTotalApartamentos()).isEqualTo(20);
            assertThat(result.isSmartStudio()).isTrue();
            // Dr = 20 * 1.00 = 20.00 kVA
            assertThat(result.getDemandaBrutaDr()).isEqualByComparingTo("20.00");
            // Fc fixado em 0.9000 (Item 6.25) ao invés do valor de tabela 0.8720 (N = 20)
            assertThat(result.getFatorCoincidenciaFc()).isEqualByComparingTo("0.9000");
            // Dtr = 20.00 * 0.9000 = 18.00 kVA
            assertThat(result.getDemandaProvisoriaDtr()).isEqualByComparingTo("18.00");
            // Dr <= 25.00 => Fr = 1.50
            assertThat(result.getFatorSegurancaFr()).isEqualByComparingTo("1.50");
            // Drf = 18.00 * 1.50 = 27.00 kVA
            assertThat(result.getDemandaFinalDrf()).isEqualByComparingTo("27.00");
        }

        @Test
        @DisplayName("Deve manter curva convencional do Quadro 36 para Smart Studios com N <= 15 (ex.: 10 unidades)")
        void testSmartStudiosAte15Unidades() {
            // GIVEN: 10 unidades x 35m², isSmartStudio = true
            var studio = new ResidentialTypologyItem("Studio", new BigDecimal("35.00"), 10);

            // WHEN
            ResidentialCalculationResult result = engine.calculate(List.of(studio), true);

            // THEN
            assertThat(result.getTotalApartamentos()).isEqualTo(10);
            assertThat(result.isSmartStudio()).isTrue();
            // Para N = 10, o Quadro 36 prescreve Fc = 0.9640
            assertThat(result.getFatorCoincidenciaFc()).isEqualByComparingTo("0.9640");
        }

        @Test
        @DisplayName("Deve inferir isSmartStudio = true através do método sobrecarregado caso item contenha a flag")
        void testInferenciaSmartStudioNaSobrecarga() {
            var itemStudio = new ResidentialTypologyItem("Studio Compacto", new BigDecimal("32.00"), 18, true);

            ResidentialCalculationResult result = engine.calculate(List.of(itemStudio));

            assertThat(result.isSmartStudio()).isTrue();
            assertThat(result.getFatorCoincidenciaFc()).isEqualByComparingTo("0.9000");
        }

        @Test
        @DisplayName("Deve assumir isSmartStudio = false por padrão caso nenhum item possua flag ativa")
        void testPadraoNaoSmartStudioNaSobrecarga() {
            var itemNormal = new ResidentialTypologyItem("Normal", new BigDecimal("60.00"), 20);

            ResidentialCalculationResult result = engine.calculate(List.of(itemNormal));

            assertThat(result.isSmartStudio()).isFalse();
            assertThat(result.getFatorCoincidenciaFc()).isEqualByComparingTo("0.8720");
        }
    }

    @Nested
    @DisplayName("3. Quadro 37 — Escalonamento e Limites do Fator de Segurança (Fr)")
    class FatorSegurancaQuadro37Tests {

        @ParameterizedTest(name = "Dr = {0} kVA deve resultar em Fr = {1}")
        @CsvSource({
                "0.00,   1.50",
                "10.00,  1.50",
                "25.00,  1.50",
                "25.01,  1.30",
                "40.00,  1.30",
                "50.00,  1.30",
                "50.01,  1.20",
                "75.00,  1.20",
                "100.00, 1.20",
                "100.01, 1.10",
                "500.00, 1.10"
        })
        void testFronteirasFatorSegurancaQuadro37(String drString, String frEsperado) {
            BigDecimal dr = new BigDecimal(drString);
            BigDecimal fr = engine.resolveFatorSeguranca(dr);
            assertThat(fr).isEqualByComparingTo(frEsperado);
        }
    }

    @Nested
    @DisplayName("4. Quadro 35 — Inferência da Demanda Unitária (Di) por Área Útil (valores da norma)")
    class DemandaUnitariaQuadro35Tests {

        @ParameterizedTest(name = "Área = {0} m² deve resultar em Di = {1} kVA")
        @CsvSource({
                "25.00,   1.00",
                "40.00,   1.00",
                "40.01,   1.05",
                "45.00,   1.05",
                "45.01,   1.16",
                "50.00,   1.16",
                "50.01,   1.26",
                "55.00,   1.26",
                "55.01,   1.36",
                "60.00,   1.36",
                "60.01,   1.47",
                "63.00,   1.47",
                "65.00,   1.47",
                "65.01,   1.57",
                "68.00,   1.57",
                "70.00,   1.57",
                "70.01,   1.67",
                "80.00,   1.76",
                "80.01,   1.86",
                "90.00,   1.96",
                "90.01,   2.06",
                "100.00,  2.16",
                "100.01,  2.35",
                "110.00,  2.35",
                "120.00,  2.54",
                "120.01,  2.73",
                "140.00,  2.91",
                "140.01,  3.06",
                "170.00,  3.47",
                "170.01,  3.65",
                "200.00,  4.01",
                "200.01,  4.36",
                "300.00,  5.76",
                "300.01,  6.61",
                "500.00,  9.10",
                "500.01,  9.91",
                "1000.00, 16.93"
        })
        void testInferenciaAutomaticaDiQuadro35(String areaString, String diEsperado) {
            BigDecimal area = new BigDecimal(areaString);
            BigDecimal di = engine.resolveDemandaUnitaria(area);
            assertThat(di).isEqualByComparingTo(diEsperado);
        }

        @Test
        @DisplayName("Área acima de 1000 m² não é tabelada e deve ser rejeitada")
        void testAreaAcimaDoQuadro() {
            assertThatThrownBy(() -> engine.resolveDemandaUnitaria(new BigDecimal("1000.01")))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("5. Quadro 36 — Tabela discreta do Fator de Coincidência (Fc), sem interpolação")
    class FatorCoincidenciaQuadro36Tests {

        @ParameterizedTest(name = "N = {0} apartamentos deve resultar em Fc = {1}")
        @CsvSource({
                "1,   1.0000",
                "2,   0.9800",
                "5,   0.9680",
                "8,   0.9650",
                "10,  0.9640",
                "15,  0.9027",
                "20,  0.8720",
                "22,  0.8477",
                "25,  0.8184",
                "30,  0.7827",
                "35,  0.7571",
                "40,  0.7380",
                "45,  0.7231",
                "50,  0.7068",
                "55,  0.6935",
                "59,  0.6844",
                "60,  0.6823",
                "120, 0.6823"
        })
        void testTabelaDiscretaQuadro36(int n, String fcEsperado) {
            assertThat(engine.resolveFatorCoincidencia(n, false)).isEqualByComparingTo(fcEsperado);
        }

        @Test
        @DisplayName("N não tabelado não é interpolado: cada N tem o seu valor (ex.: 21 = 0.8590, nunca a média de 20 e 22)")
        void testSemInterpolacao() {
            assertThat(engine.resolveFatorCoincidencia(21, false)).isEqualByComparingTo("0.8590");
        }
    }

    @Nested
    @DisplayName("6. Validações Defensivas e Tratamento de Erros")
    class ValidacoesEntradaTests {

        @Test
        @DisplayName("Deve rejeitar lista de tipologias nula ou vazia")
        void testListaNulaOuVazia() {
            assertThatThrownBy(() -> engine.calculate(null, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não pode ser nula ou vazia");

            assertThatThrownBy(() -> engine.calculate(Collections.emptyList(), false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não pode ser nula ou vazia");
        }

        @Test
        @DisplayName("Deve rejeitar payload unificado nulo")
        void testInputNulo() {
            assertThatThrownBy(() -> engine.calculate((ResidentialCalculationInput) null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não podem ser nulos");
        }

        @Test
        @DisplayName("Deve rejeitar tipologia individual nula")
        void testTipologiaNula() {
            List<ResidentialTypologyItem> listaComNulo = Collections.singletonList(null);
            assertThatThrownBy(() -> engine.calculate(listaComNulo, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não pode ser nulo");
        }

        @Test
        @DisplayName("Deve rejeitar quantidade de apartamentos menor ou igual a zero")
        void testQuantidadeInvalida() {
            var itemZero = new ResidentialTypologyItem("Zero", new BigDecimal("50.00"), 0);
            assertThatThrownBy(() -> engine.calculate(List.of(itemZero), false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("quantidade de apartamentos deve ser maior que zero");

            var itemNegativo = new ResidentialTypologyItem("Negativo", new BigDecimal("50.00"), -5);
            assertThatThrownBy(() -> engine.calculate(List.of(itemNegativo), false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("quantidade de apartamentos deve ser maior que zero");
        }

        @Test
        @DisplayName("Deve rejeitar área privativa nula, zero ou negativa")
        void testAreaInvalida() {
            var itemAreaNula = new ResidentialTypologyItem("Nula", null, 10);
            assertThatThrownBy(() -> engine.calculate(List.of(itemAreaNula), false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("área útil privativa deve ser maior que zero");

            var itemAreaZero = new ResidentialTypologyItem("Zero", BigDecimal.ZERO, 10);
            assertThatThrownBy(() -> engine.calculate(List.of(itemAreaZero), false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("área útil privativa deve ser maior que zero");

            var itemAreaNegativa = new ResidentialTypologyItem("Negativa", new BigDecimal("-10.00"), 10);
            assertThatThrownBy(() -> engine.calculate(List.of(itemAreaNegativa), false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("área útil privativa deve ser maior que zero");

            assertThatThrownBy(() -> engine.resolveDemandaUnitaria(null))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> engine.resolveDemandaUnitaria(BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar demanda unitária Di explícita menor ou igual a zero")
        void testDiExplicitoInvalido() {
            var itemDiZero = new ResidentialTypologyItem("Zero Di", new BigDecimal("50.00"), 10, BigDecimal.ZERO);
            assertThatThrownBy(() -> engine.calculate(List.of(itemDiZero), false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("demanda unitária Di informada deve ser maior que zero");
        }

        @Test
        @DisplayName("Deve rejeitar total de apartamentos <= 0 na resolução de Fc")
        void testTotalApartamentosInvalidoEmFc() {
            assertThatThrownBy(() -> engine.resolveFatorCoincidencia(0, false))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> engine.resolveFatorCoincidencia(-1, false))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve rejeitar demanda bruta Dr negativa ou nula na resolução de Fr")
        void testDrInvalidoEmFr() {
            assertThatThrownBy(() -> engine.resolveFatorSeguranca(null))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> engine.resolveFatorSeguranca(new BigDecimal("-1.00")))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
