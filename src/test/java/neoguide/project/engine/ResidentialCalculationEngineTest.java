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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ResidentialCalculationEngine — Motor de Cálculo da Demanda Residencial (DIS-NOR-053)")
class ResidentialCalculationEngineTest {

    private ResidentialCalculationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ResidentialCalculationEngine();
    }

    @Nested
    @DisplayName("1. Caso Canônico do Memorial Descritivo (100 unidades)")
    class MemorialCanonicoTests {

        @Test
        @DisplayName("Deve calcular com precisão exata o memorial de 100 unidades (50x63m² e 50x68m²)")
        void testMemorialCanonico100Apartamentos() {
            // GIVEN: 50 unidades de 63m² (Di=1.47 kVA) + 50 unidades de 68m² (Di=1.57 kVA)
            var tipoA = new ResidentialTypologyItem("Tipo A - 63m²", new BigDecimal("63.00"), 50, new BigDecimal("1.47"));
            var tipoB = new ResidentialTypologyItem("Tipo B - 68m²", new BigDecimal("68.00"), 50, new BigDecimal("1.57"));

            var input = new ResidentialCalculationInput(List.of(tipoA, tipoB), false);

            // WHEN
            ResidentialCalculationResult result = engine.calculate(input);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getTotalApartamentos()).isEqualTo(100);
            assertThat(result.getDemandaBrutaDr()).isEqualByComparingTo("152.00");
            assertThat(result.getFatorCoincidenciaFc()).isEqualByComparingTo("0.6823");
            assertThat(result.getDemandaProvisoriaDtr()).isEqualByComparingTo("103.71");
            assertThat(result.getFatorSegurancaFr()).isEqualByComparingTo("1.10");
            assertThat(result.getDemandaFinalDrf()).isEqualByComparingTo("114.08");
            assertThat(result.isSmartStudio()).isFalse();

            // Verificação dos subtotais analíticos das tipologias
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
            assertThat(result.getDemandaProvisoriaDtr()).isEqualByComparingTo("103.71");
            assertThat(result.getFatorSegurancaFr()).isEqualByComparingTo("1.10");
            assertThat(result.getDemandaFinalDrf()).isEqualByComparingTo("114.08");
        }
    }

    @Nested
    @DisplayName("2. Regra Regulatória de Smart Studios (Item 6.25 DIS-NOR-053)")
    class SmartStudiosTests {

        @Test
        @DisplayName("Deve fixar Fc = 0.9000 para Smart Studios com N > 15 (ex.: 20 unidades de 35m²)")
        void testSmartStudiosAcimaDe15Unidades() {
            // GIVEN: 20 unidades x 35m² (Di = 1.20 kVA), isSmartStudio = true
            var studio = new ResidentialTypologyItem("Studio", new BigDecimal("35.00"), 20);

            // WHEN
            ResidentialCalculationResult result = engine.calculate(List.of(studio), true);

            // THEN
            assertThat(result.getTotalApartamentos()).isEqualTo(20);
            assertThat(result.isSmartStudio()).isTrue();
            // Dr = 20 * 1.20 = 24.00 kVA
            assertThat(result.getDemandaBrutaDr()).isEqualByComparingTo("24.00");
            // Fc fixado em 0.9000 (Item 6.25) ao invés do valor de tabela 0.8560
            assertThat(result.getFatorCoincidenciaFc()).isEqualByComparingTo("0.9000");
            // Dtr = 24.00 * 0.9000 = 21.60 kVA
            assertThat(result.getDemandaProvisoriaDtr()).isEqualByComparingTo("21.60");
            // Dr <= 25.00 => Fr = 1.50
            assertThat(result.getFatorSegurancaFr()).isEqualByComparingTo("1.50");
            // Drf = 21.60 * 1.50 = 32.40 kVA
            assertThat(result.getDemandaFinalDrf()).isEqualByComparingTo("32.40");
        }

        @Test
        @DisplayName("Deve manter curva convencional do Quadro 36 para Smart Studios com N <= 15 (ex.: 10 unidades)")
        void testSmartStudiosAte15Unidades() {
            // GIVEN: 10 unidades x 35m² (Di = 1.20 kVA), isSmartStudio = true
            var studio = new ResidentialTypologyItem("Studio", new BigDecimal("35.00"), 10);

            // WHEN
            ResidentialCalculationResult result = engine.calculate(List.of(studio), true);

            // THEN
            assertThat(result.getTotalApartamentos()).isEqualTo(10);
            assertThat(result.isSmartStudio()).isTrue();
            // Para N = 10, tabela regular prescreve Fc = 0.9640
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
            assertThat(result.getFatorCoincidenciaFc()).isEqualByComparingTo("0.8560");
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
    @DisplayName("4. Quadro 35 — Inferência da Demanda Unitária (Di) por Área Útil")
    class DemandaUnitariaQuadro35Tests {

        @ParameterizedTest(name = "Área = {0} m² deve resultar em Di = {1} kVA")
        @CsvSource({
                "25.00,  1.20",
                "40.00,  1.20",
                "40.01,  1.28",
                "45.00,  1.28",
                "45.01,  1.35",
                "50.00,  1.35",
                "50.01,  1.40",
                "55.00,  1.40",
                "55.01,  1.43",
                "60.00,  1.43",
                "60.01,  1.47",
                "63.00,  1.47",
                "65.00,  1.47",
                "65.01,  1.57",
                "68.00,  1.57",
                "70.00,  1.57",
                "70.01,  1.80",
                "80.00,  1.80",
                "80.01,  1.95",
                "90.00,  1.95",
                "90.01,  2.15",
                "100.00, 2.15",
                "100.01, 2.45",
                "110.00, 2.45",
                "120.00, 2.45",
                "120.01, 2.80",
                "140.00, 2.80",
                "140.01, 3.30",
                "170.00, 3.30",
                "170.01, 3.90",
                "200.00, 3.90",
                "200.01, 5.40",
                "300.00, 5.40",
                "300.01, 7.20",
                "500.00, 7.20"
        })
        void testInferenciaAutomaticaDiQuadro35(String areaString, String diEsperado) {
            BigDecimal area = new BigDecimal(areaString);
            BigDecimal di = engine.resolveDemandaUnitaria(area);
            assertThat(di).isEqualByComparingTo(diEsperado);
        }
    }

    @Nested
    @DisplayName("5. Quadro 36 — Interpolação e Pontos Discretos do Fator de Coincidência (Fc)")
    class FatorCoincidenciaQuadro36Tests {

        @Test
        @DisplayName("Deve retornar valores exatos dos nós tabelados")
        void testPontosExatosTabelados() {
            assertThat(engine.resolveFatorCoincidencia(10, false)).isEqualByComparingTo("0.9640");
            assertThat(engine.resolveFatorCoincidencia(15, false)).isEqualByComparingTo("0.9027");
            assertThat(engine.resolveFatorCoincidencia(20, false)).isEqualByComparingTo("0.8560");
            assertThat(engine.resolveFatorCoincidencia(25, false)).isEqualByComparingTo("0.8184");
            assertThat(engine.resolveFatorCoincidencia(30, false)).isEqualByComparingTo("0.7827");
            assertThat(engine.resolveFatorCoincidencia(35, false)).isEqualByComparingTo("0.7580");
            assertThat(engine.resolveFatorCoincidencia(40, false)).isEqualByComparingTo("0.7380");
            assertThat(engine.resolveFatorCoincidencia(45, false)).isEqualByComparingTo("0.7231");
            assertThat(engine.resolveFatorCoincidencia(50, false)).isEqualByComparingTo("0.7080");
            assertThat(engine.resolveFatorCoincidencia(55, false)).isEqualByComparingTo("0.6935");
            assertThat(engine.resolveFatorCoincidencia(60, false)).isEqualByComparingTo("0.6823");
            assertThat(engine.resolveFatorCoincidencia(120, false)).isEqualByComparingTo("0.6823");
        }

        @Test
        @DisplayName("Deve interpolar linearmente para N entre 1 e 10 (1=1.0000 e 10=0.9640)")
        void testInterpolacaoAbaixoDe10() {
            assertThat(engine.resolveFatorCoincidencia(1, false)).isEqualByComparingTo("1.0000");
            // Para N = 5: deltaN = 4, step = 9, deltaFc = 0.9640 - 1.0000 = -0.0360
            // Fc = 1.0000 + (4 * -0.0360 / 9) = 1.0000 - 0.0160 = 0.9840
            assertThat(engine.resolveFatorCoincidencia(5, false)).isEqualByComparingTo("0.9840");
        }

        @Test
        @DisplayName("Deve interpolar linearmente para nós intermediários (ex.: N = 22)")
        void testInterpolacaoNosIntermediarios() {
            // N1 = 20 (0.8560), N2 = 25 (0.8184)
            // step = 5, deltaN = 2, deltaFc = -0.0376
            // Fc = 0.8560 + (2 * -0.0376 / 5) = 0.8560 - 0.01504 = 0.84096 => 0.8410
            assertThat(engine.resolveFatorCoincidencia(22, false)).isEqualByComparingTo("0.8410");
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
