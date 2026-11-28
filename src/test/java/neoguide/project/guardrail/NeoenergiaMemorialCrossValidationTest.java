package neoguide.project.guardrail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import neoguide.project.dto.CreateProjectRequest;
import neoguide.project.dto.LoginRequest;
import neoguide.project.dto.RegisterRequest;
import neoguide.project.dto.validation.NormativeMessageType;
import neoguide.project.dto.validation.NormativeValidationResponse;
import neoguide.project.engine.CommercialCalculationEngine;
import neoguide.project.engine.CommonAreaCalculationEngine;
import neoguide.project.engine.ConsolidatedCalculationEngine;
import neoguide.project.engine.ResidentialCalculationEngine;
import neoguide.project.engine.SpecialLoadsCalculationEngine;
import neoguide.project.engine.enums.SupplyVoltageLevel;
import neoguide.project.engine.enums.VoltageSystem;
import neoguide.project.engine.model.CommercialCalculationResult;
import neoguide.project.engine.model.ConsolidatedCalculationInput;
import neoguide.project.engine.model.ConsolidatedCalculationResult;
import neoguide.project.engine.model.ResidentialCalculationResult;
import neoguide.project.engine.model.ResidentialTypologyItem;
import neoguide.project.engine.model.SpecialLoadsCalculationResult;
import neoguide.project.engine.model.commonarea.CommonAreaCalculationResult;
import neoguide.project.engine.normativo.TabelasNormativas;
import neoguide.project.model.DemandResults;
import neoguide.project.model.Project;
import neoguide.project.repository.CommercialUnitRepository;
import neoguide.project.repository.CommonAreaLoadRepository;
import neoguide.project.repository.DemandResultsRepository;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.ResidentialTypologyRepository;
import neoguide.project.repository.SpecialLoadsRepository;
import neoguide.project.repository.UserRepository;
import neoguide.project.validator.NormativeRuleValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Guardrail GR-01 — Validações Cruzadas com Memoriais Históricos da Neoenergia Pernambuco.
 * Aderência integral às normas oficiais DIS-NOR-053 (Rev. 06) e DIS-NOR-030 (Rev. 07)
 * conforme {@link TabelasNormativas}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("GR-01 — Guardrail de Validações Cruzadas com Memoriais Canônicos da Neoenergia")
class NeoenergiaMemorialCrossValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ResidentialTypologyRepository residentialTypologyRepository;

    @Autowired
    private CommonAreaLoadRepository commonAreaLoadRepository;

    @Autowired
    private CommercialUnitRepository commercialUnitRepository;

    @Autowired
    private SpecialLoadsRepository specialLoadsRepository;

    @Autowired
    private DemandResultsRepository demandResultsRepository;

    @Autowired
    private neoguide.project.repository.DemandResultRepository demandResultRepository;

    private static BigDecimal round2(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    // =========================================================================
    // NÍVEL 1 — VALIDAÇÃO DE DOMÍNIO PURO EM RAM (MOTORES + VALIDADOR NORMATIVO)
    // =========================================================================

    @Nested
    @DisplayName("Nível 1 — Domínio Puro em RAM (Motores de Cálculo e NormativeRuleValidator)")
    class DominioPuroGuardrailTests {

        private ResidentialCalculationEngine residentialEngine;
        private CommonAreaCalculationEngine commonAreaEngine;
        private CommercialCalculationEngine commercialEngine;
        private SpecialLoadsCalculationEngine specialLoadsEngine;
        private ConsolidatedCalculationEngine consolidatedEngine;
        private NormativeRuleValidator normativeValidator;

        @BeforeEach
        void setUpEngines() {
            residentialEngine = new ResidentialCalculationEngine();
            commonAreaEngine = new CommonAreaCalculationEngine();
            commercialEngine = new CommercialCalculationEngine();
            specialLoadsEngine = new SpecialLoadsCalculationEngine();
            consolidatedEngine = new ConsolidatedCalculationEngine();
            normativeValidator = new NormativeRuleValidator();
        }

        @Test
        @DisplayName("Cenário 1 — Memorial Canônico 100 Apartamentos (50x63m² + 50x68m², Ds=69,32, Dmbi=2,70, BT 380/220V)")
        void cenario1_memorialCanonico100Apartamentos_dominioPuro() {
            // 1. Residencial (Quadros 35, 36 e 37)
            ResidentialCalculationResult res = residentialEngine.calculate(
                    CanonicalMemorialTestDataFactory.cenario1ResidencialInput()
            );

            assertThat(res.totalApartamentos()).isEqualTo(100);
            assertThat(res.tipologias().get(0).demandaUnitariaDi()).isEqualByComparingTo("1.47");
            assertThat(res.tipologias().get(0).demandaSubtotal()).isEqualByComparingTo("73.50");
            assertThat(res.tipologias().get(1).demandaUnitariaDi()).isEqualByComparingTo("1.57");
            assertThat(res.tipologias().get(1).demandaSubtotal()).isEqualByComparingTo("78.50");
            assertThat(res.demandaBrutaDr()).isEqualByComparingTo("152.00");
            assertThat(res.fatorCoincidenciaFc()).isEqualByComparingTo("0.6823");
            assertThat(res.demandaProvisoriaDtr()).isEqualByComparingTo("103.7096");
            assertThat(round2(res.demandaProvisoriaDtr())).isEqualByComparingTo("103.71");
            assertThat(res.fatorSegurancaFr()).isEqualByComparingTo("1.10");
            assertThat(res.demandaFinalDrf()).isEqualByComparingTo("114.08056");
            BigDecimal drfApresentacao = round2(res.demandaFinalDrf());
            assertThat(drfApresentacao).isEqualByComparingTo("114.08");

            // 2. Serviços e Áreas Comuns (DIS-NOR-030)
            CommonAreaCalculationResult common = commonAreaEngine.calculate(
                    CanonicalMemorialTestDataFactory.cenario1AreaComumDomainInput()
            );
            assertThat(common.parcelaA_IluminacaoTug().demandaKva()).isEqualByComparingTo("20.00");
            assertThat(common.parcelaF_Climatizacao().demandaKva()).isEqualByComparingTo("10.00");
            assertThat(common.parcelaG_Motores().demandaKva()).isEqualByComparingTo("14.55");
            assertThat(common.parcelasCustomizadas().demandaKva()).isEqualByComparingTo("24.77");
            assertThat(common.demandaTotalDsKva()).isEqualByComparingTo("69.32");

            // 3. Cargas Especiais — Motobomba de Incêndio 2 cv isolada antes do QDG
            SpecialLoadsCalculationResult special = specialLoadsEngine.calculate(
                    CanonicalMemorialTestDataFactory.cenario1CargasEspeciaisDomainInput()
            );
            assertThat(special.recargaVeicular().demandaDveKva()).isEqualByComparingTo("0.00");
            assertThat(special.motobombaIncendio().demandaDmbiKva()).isEqualByComparingTo("2.70");
            assertThat(special.motobombaIncendio().derivadoAntesDoQdg()).isTrue();
            assertThat(special.parcelaDedutivelQdgKva()).isEqualByComparingTo("2.70");

            // 4. Consolidação Geral, Padrão de Entrada (Tabela 2) e Barramento QDG (Tabela 3)
            ConsolidatedCalculationResult consolidated = consolidatedEngine.calculate(new ConsolidatedCalculationInput(
                    drfApresentacao,
                    common.demandaTotalDsKva(),
                    BigDecimal.ZERO,
                    special.recargaVeicular().demandaDveKva(),
                    special.motobombaIncendio().demandaDmbiKva(),
                    VoltageSystem.TRIFASICO_380_220V,
                    null
            ));

            assertThat(consolidated.demandaTotalEdificacaoDed()).isEqualByComparingTo("186.10");
            assertThat(consolidated.demandaQdgDqdg()).isEqualByComparingTo("183.40");
            assertThat(consolidated.demandaIncendioDeduzidaDmbi()).isEqualByComparingTo("2.70");
            assertThat(consolidated.nivelFornecimento()).isEqualTo(SupplyVoltageLevel.BAIXA_TENSAO_SECUNDARIA);
            assertThat(consolidated.sistemaTensao()).isEqualTo(VoltageSystem.TRIFASICO_380_220V);
            assertThat(consolidated.correnteProjetoIpA()).isEqualByComparingTo("278.65");

            assertThat(consolidated.padraoEntrada()).isNotNull();
            assertThat(consolidated.padraoEntrada().disjuntorCorrenteInA()).isEqualTo(320);
            assertThat(consolidated.padraoEntrada().disjuntorCarcaca()).isEqualTo("400 A");
            assertThat(consolidated.padraoEntrada().secaoCondutorFase()).contains("240 mm²");
            assertThat(consolidated.padraoEntrada().secaoCondutorNeutro()).contains("240 mm²");

            assertThat(consolidated.barramentoQdg()).isNotNull();
            assertThat(consolidated.barramentoQdg().barraDimensaoMm()).isEqualTo("30 x 5 mm");
            assertThat(consolidated.barramentoQdg().correnteNominalA()).isEqualTo(449);

            // Verificação Ip <= In e Ip <= Ibarramento
            assertThat(consolidated.correnteProjetoIpA())
                    .isLessThanOrEqualTo(BigDecimal.valueOf(consolidated.padraoEntrada().disjuntorCorrenteInA()));
            assertThat(consolidated.disjuntorAdequado()).isTrue();
            assertThat(consolidated.barramentoAdequado()).isTrue();

            // 5. Auditoria Normativa (NormativeRuleValidator)
            Project project = CanonicalMemorialTestDataFactory.buildDomainProject(
                    1L, "PRJ-GR01-0001", CanonicalMemorialTestDataFactory.TENSAO_380_220V
            );
            DemandResults demandResults = CanonicalMemorialTestDataFactory.buildDomainDemandResults(
                    project, drfApresentacao, common.demandaTotalDsKva(), BigDecimal.ZERO,
                    BigDecimal.ZERO, special.motobombaIncendio().demandaDmbiKva(), consolidated
            );

            NormativeValidationResponse audit = normativeValidator.validate(project, demandResults);
            assertThat(audit.conforme()).isTrue();
            assertThat(audit.seloConformidade()).isEqualTo("CONFORME_DIS_NOR_053");
            assertThat(audit.totalErrosNormativos()).isZero();
            assertThat(audit.detalhesTecnicos().correnteProjetoIpA()).isEqualByComparingTo("278.65");
            assertThat(audit.detalhesTecnicos().potenciaNominalTrafoKva()).isEqualByComparingTo("225.0");
            assertThat(audit.detalhesTecnicos().nivelTensaoEnquadrado()).isEqualTo("BAIXA_TENSAO");
        }

        @Test
        @DisplayName("Cenário 2 — Smart Studios (Item 6.25 DIS-NOR-053): 40x28m² impõe Fc=0,9000 vs Quadro 36 (0,7380)")
        void cenario2_smartStudiosItem625_dominioPuro() {
            // Valida Quadro 35 e Quadro 36 oficiais em TabelasNormativas
            assertThat(TabelasNormativas.faixaArea(new BigDecimal("28.00")).demandaKva())
                    .isEqualByComparingTo("1.00");
            assertThat(TabelasNormativas.fatorCoincidencia(40))
                    .isEqualByComparingTo("0.7380");

            // Modo Smart Studio (N = 40 > 15 -> Item 6.25 impõe Fc = 0,9000)
            ResidentialCalculationResult smartStudio = residentialEngine.calculate(
                    CanonicalMemorialTestDataFactory.cenario2SmartStudiosInput(true)
            );
            // Modo Convencional (N = 40 -> Quadro 36 oficial = 0,7380)
            ResidentialCalculationResult convencional = residentialEngine.calculate(
                    CanonicalMemorialTestDataFactory.cenario2SmartStudiosInput(false)
            );

            // Smart Studio: Dr = 40,00; Fc = 0,9000; Dtr = 36,0000; Fr = 1,30; Drf = 46,80000
            assertThat(smartStudio.isSmartStudio()).isTrue();
            assertThat(smartStudio.totalApartamentos()).isEqualTo(40);
            assertThat(smartStudio.demandaBrutaDr()).isEqualByComparingTo("40.00");
            assertThat(smartStudio.fatorCoincidenciaFc()).isEqualByComparingTo("0.9000");
            assertThat(smartStudio.demandaProvisoriaDtr()).isEqualByComparingTo("36.0000");
            assertThat(smartStudio.fatorSegurancaFr()).isEqualByComparingTo("1.30");
            assertThat(smartStudio.demandaFinalDrf()).isEqualByComparingTo("46.80000");
            assertThat(round2(smartStudio.demandaFinalDrf())).isEqualByComparingTo("46.80");

            // Convencional: Dr = 40,00; Fc = 0,7380; Dtr = 29,5200; Fr = 1,30; Drf = 38,37600 (apresentação 38,38)
            assertThat(convencional.isSmartStudio()).isFalse();
            assertThat(convencional.demandaBrutaDr()).isEqualByComparingTo("40.00");
            assertThat(convencional.fatorCoincidenciaFc()).isEqualByComparingTo("0.7380");
            assertThat(convencional.demandaProvisoriaDtr()).isEqualByComparingTo("29.5200");
            assertThat(convencional.fatorSegurancaFr()).isEqualByComparingTo("1.30");
            assertThat(convencional.demandaFinalDrf()).isEqualByComparingTo("38.37600");
            assertThat(round2(convencional.demandaFinalDrf())).isEqualByComparingTo("38.38");

            // Majoração normativa imposta pelo Item 6.25
            assertThat(smartStudio.demandaFinalDrf()).isGreaterThan(convencional.demandaFinalDrf());
            assertThat(smartStudio.demandaFinalDrf().subtract(convencional.demandaFinalDrf()))
                    .isEqualByComparingTo("8.42400");

            // Fronteira N = 15: ainda segue Quadro 36 (0,9027); apenas N > 15 ativa Fc = 0,9000
            ResidentialCalculationResult fronteira15 = residentialEngine.calculate(
                    List.of(new ResidentialTypologyItem("Studio 28 m²", new BigDecimal("28.00"), 15, true)),
                    true
            );
            assertThat(fronteira15.fatorCoincidenciaFc()).isEqualByComparingTo("0.9027");
        }

        @Test
        @DisplayName("Cenário 3 — Trava de VE / 50% do Transformador (Item 6.26.2 DIS-NOR-053): violação e conformidade")
        void cenario3_travaVe50PorcentoTrafoItem6262_dominioPuro() {
            // Verifica fatores oficiais do Quadro 33 em TabelasNormativas
            assertThat(TabelasNormativas.fatorVe(8)).isEqualByComparingTo("1.00");
            assertThat(TabelasNormativas.fatorVe(15)).isEqualByComparingTo("0.86");

            BigDecimal ds = new BigDecimal("30.00");
            BigDecimal sTrafo = new BigDecimal("225.0");

            // --- Sub-caso 3A: VIOLAÇÃO (Ds + Dve = 30,00 + 95,46 = 125,46 kVA = 55,76% > 50% de 225 kVA) ---
            ResidentialCalculationResult resViolacao = residentialEngine.calculate(
                    CanonicalMemorialTestDataFactory.cenario3ResidencialViolacaoInput()
            );
            BigDecimal drfViolacao = round2(resViolacao.demandaFinalDrf());
            assertThat(drfViolacao).isEqualByComparingTo("52.07");

            SpecialLoadsCalculationResult specialViolacao = specialLoadsEngine.calculate(
                    CanonicalMemorialTestDataFactory.cenario3VeViolacaoDomainInput(ds, sTrafo)
            );
            assertThat(specialViolacao.recargaVeicular().fatorSimultaneidadeFve()).isEqualByComparingTo("0.86");
            assertThat(specialViolacao.recargaVeicular().demandaDveKva()).isEqualByComparingTo("95.46");
            assertThat(specialViolacao.motobombaIncendio().demandaDmbiKva()).isEqualByComparingTo("2.70");
            assertThat(specialViolacao.alertaTrafo50Excedido()).isTrue();
            assertThat(specialViolacao.percentualUsoTrafoVeDs()).isEqualByComparingTo("55.76");

            ConsolidatedCalculationResult consViolacao = consolidatedEngine.calculate(new ConsolidatedCalculationInput(
                    drfViolacao, ds, BigDecimal.ZERO,
                    specialViolacao.recargaVeicular().demandaDveKva(),
                    specialViolacao.motobombaIncendio().demandaDmbiKva(),
                    VoltageSystem.TRIFASICO_380_220V, sTrafo
            ));
            assertThat(consViolacao.demandaTotalEdificacaoDed()).isEqualByComparingTo("180.23");
            assertThat(consViolacao.demandaQdgDqdg()).isEqualByComparingTo("177.53");

            Project projViolacao = CanonicalMemorialTestDataFactory.buildDomainProject(
                    31L, "PRJ-GR01-VE-ERR", CanonicalMemorialTestDataFactory.TENSAO_380_220V
            );
            DemandResults resultsViolacao = CanonicalMemorialTestDataFactory.buildDomainDemandResults(
                    projViolacao, drfViolacao, ds, BigDecimal.ZERO,
                    specialViolacao.recargaVeicular().demandaDveKva(),
                    specialViolacao.motobombaIncendio().demandaDmbiKva(),
                    consViolacao
            );

            NormativeValidationResponse auditViolacao = normativeValidator.validate(projViolacao, resultsViolacao);
            assertThat(auditViolacao.conforme()).isFalse();
            assertThat(auditViolacao.seloConformidade()).isEqualTo("PENDENTE_AJUSTES");
            assertThat(auditViolacao.totalErrosNormativos()).isEqualTo(1);
            assertThat(auditViolacao.detalhesTecnicos().potenciaNominalTrafoKva()).isEqualByComparingTo("225.0");
            assertThat(auditViolacao.detalhesTecnicos().percentualOcupacaoVeServico()).isEqualByComparingTo("55.76");
            assertThat(auditViolacao.mensagens())
                    .anySatisfy(msg -> {
                        assertThat(msg.tipo()).isEqualTo(NormativeMessageType.ERRO_NORMATIVO);
                        assertThat(msg.regra()).contains("6.26.2");
                        assertThat(msg.detalheTecnico()).isEqualTo("Ds + Dve > 50% Strafo");
                    });

            // --- Sub-caso 3B: CONFORMIDADE (Ds + Dve = 30,00 + 29,60 = 59,60 kVA = 26,49% <= 40% de 225 kVA) ---
            ResidentialCalculationResult resConforme = residentialEngine.calculate(
                    CanonicalMemorialTestDataFactory.cenario1ResidencialInput()
            );
            BigDecimal drfConforme = round2(resConforme.demandaFinalDrf());
            assertThat(drfConforme).isEqualByComparingTo("114.08");

            SpecialLoadsCalculationResult specialConforme = specialLoadsEngine.calculate(
                    CanonicalMemorialTestDataFactory.cenario3VeConformeDomainInput(ds, sTrafo)
            );
            assertThat(specialConforme.recargaVeicular().fatorSimultaneidadeFve()).isEqualByComparingTo("1.00");
            assertThat(specialConforme.recargaVeicular().demandaDveKva()).isEqualByComparingTo("29.60");
            assertThat(specialConforme.alertaTrafo50Excedido()).isFalse();
            assertThat(specialConforme.percentualUsoTrafoVeDs()).isEqualByComparingTo("26.49");

            ConsolidatedCalculationResult consConforme = consolidatedEngine.calculate(new ConsolidatedCalculationInput(
                    drfConforme, ds, BigDecimal.ZERO,
                    specialConforme.recargaVeicular().demandaDveKva(),
                    specialConforme.motobombaIncendio().demandaDmbiKva(),
                    VoltageSystem.TRIFASICO_380_220V, sTrafo
            ));
            assertThat(consConforme.demandaTotalEdificacaoDed()).isEqualByComparingTo("176.38");

            Project projConforme = CanonicalMemorialTestDataFactory.buildDomainProject(
                    32L, "PRJ-GR01-VE-OK", CanonicalMemorialTestDataFactory.TENSAO_380_220V
            );
            DemandResults resultsConforme = CanonicalMemorialTestDataFactory.buildDomainDemandResults(
                    projConforme, drfConforme, ds, BigDecimal.ZERO,
                    specialConforme.recargaVeicular().demandaDveKva(),
                    specialConforme.motobombaIncendio().demandaDmbiKva(),
                    consConforme
            );

            NormativeValidationResponse auditConforme = normativeValidator.validate(projConforme, resultsConforme);
            assertThat(auditConforme.conforme()).isTrue();
            assertThat(auditConforme.seloConformidade()).isEqualTo("CONFORME_DIS_NOR_053");
            assertThat(auditConforme.totalErrosNormativos()).isZero();
            assertThat(auditConforme.totalAlertas()).isZero();
            assertThat(auditConforme.detalhesTecnicos().potenciaNominalTrafoKva()).isEqualByComparingTo("225.0");
            assertThat(auditConforme.detalhesTecnicos().percentualOcupacaoVeServico()).isEqualByComparingTo("26.49");
            assertThat(auditConforme.mensagens())
                    .anySatisfy(msg -> {
                        assertThat(msg.tipo()).isEqualTo(NormativeMessageType.INFO);
                        assertThat(msg.regra()).contains("6.26.2");
                        assertThat(msg.detalheTecnico()).isEqualTo("Ds + Dve <= 40% Strafo");
                    });
        }

        @Test
        @DisplayName("Cenário 4 — Grande Porte / Média Tensão (Item 6.1 DIS-NOR-053): Ded > 300 kVA -> 13,8 kV e Barramento Tabela 3")
        void cenario4_grandePorteMediaTensaoItem61_dominioPuro() {
            // 1. Residencial: 200 aptos × 85 m² (Quadro 35: Di = 1,86 kVA)
            ResidentialCalculationResult res = residentialEngine.calculate(
                    CanonicalMemorialTestDataFactory.cenario4ResidencialInput()
            );
            assertThat(res.totalApartamentos()).isEqualTo(200);
            assertThat(res.tipologias().get(0).demandaUnitariaDi()).isEqualByComparingTo("1.86");
            assertThat(res.demandaBrutaDr()).isEqualByComparingTo("372.00");
            assertThat(res.fatorCoincidenciaFc()).isEqualByComparingTo("0.6823");
            assertThat(res.demandaProvisoriaDtr()).isEqualByComparingTo("253.8156");
            assertThat(res.fatorSegurancaFr()).isEqualByComparingTo("1.10");
            assertThat(res.demandaFinalDrf()).isEqualByComparingTo("279.19716");
            BigDecimal drfApresentacao = round2(res.demandaFinalDrf());
            assertThat(drfApresentacao).isEqualByComparingTo("279.20");

            // 2. Serviços (Ds = 45,00 kVA)
            CommonAreaCalculationResult common = commonAreaEngine.calculate(
                    CanonicalMemorialTestDataFactory.cenario4AreaComumDomainInput()
            );
            assertThat(common.demandaTotalDsKva()).isEqualByComparingTo("45.00");

            // 3. Comercial (25 + 15 = 40 kW -> Pdem = 34 kW / 0,92 = 36,96 kVA)
            CommercialCalculationResult commercial = commercialEngine.calcular(
                    CanonicalMemorialTestDataFactory.cenario4ComercialDomainInput()
            );
            assertThat(commercial.potenciaTotalInstaladaKw()).isEqualByComparingTo("40.00");
            assertThat(commercial.demandaComercialDcKva()).isEqualByComparingTo("36.96");

            // 4. Motobomba de Incêndio 5 cv (Tabela 19 -> Dmbi = 6,02 kVA)
            SpecialLoadsCalculationResult special = specialLoadsEngine.calculate(
                    CanonicalMemorialTestDataFactory.cenario4CargasEspeciaisDomainInput()
            );
            assertThat(special.motobombaIncendio().demandaDmbiKva()).isEqualByComparingTo("6.02");

            // 5. Consolidação: Ded = 367,18 kVA > 300 kVA -> Média Tensão Primária (13,8 kV)
            ConsolidatedCalculationResult consolidated = consolidatedEngine.calculate(new ConsolidatedCalculationInput(
                    drfApresentacao,
                    common.demandaTotalDsKva(),
                    commercial.demandaComercialDcKva(),
                    BigDecimal.ZERO,
                    special.motobombaIncendio().demandaDmbiKva(),
                    VoltageSystem.TRIFASICO_380_220V,
                    null
            ));

            assertThat(consolidated.demandaTotalEdificacaoDed()).isEqualByComparingTo("367.18");
            assertThat(consolidated.demandaQdgDqdg()).isEqualByComparingTo("361.16");
            assertThat(consolidated.nivelFornecimento()).isEqualTo(SupplyVoltageLevel.MEDIA_TENSAO_PRIMARIA);
            assertThat(consolidated.padraoEntrada()).isNull();
            assertThat(consolidated.notasNormativas())
                    .anySatisfy(nota -> assertThat(nota).contains("13,8 kV").contains("6.1"));

            // Barramento QDG pela Tabela 3 para DQDG = 361,16 kVA (faixa <= 432 kVA -> 40 x 10 mm, 994 A)
            assertThat(consolidated.barramentoQdg()).isNotNull();
            assertThat(consolidated.barramentoQdg().barraDimensaoMm()).isEqualTo("40 x 10 mm");
            assertThat(consolidated.barramentoQdg().correnteNominalA()).isEqualTo(994);
            assertThat(consolidated.correnteProjetoIpA()).isEqualByComparingTo("548.73");
            assertThat(consolidated.barramentoAdequado()).isTrue();

            // 6. Validador Normativo: impedimento para BT e exigência de MT (13,8 kV)
            Project project = CanonicalMemorialTestDataFactory.buildDomainProject(
                    4L, "PRJ-GR01-MT", CanonicalMemorialTestDataFactory.TENSAO_380_220V
            );
            DemandResults results = CanonicalMemorialTestDataFactory.buildDomainDemandResults(
                    project, drfApresentacao, common.demandaTotalDsKva(),
                    commercial.demandaComercialDcKva(), BigDecimal.ZERO,
                    special.motobombaIncendio().demandaDmbiKva(), consolidated
            );

            NormativeValidationResponse audit = normativeValidator.validate(project, results);
            assertThat(audit.conforme()).isFalse();
            assertThat(audit.seloConformidade()).isEqualTo("PENDENTE_AJUSTES");
            assertThat(audit.detalhesTecnicos().nivelTensaoEnquadrado()).isEqualTo("MEDIA_TENSAO_OBRIGATORIA");
            assertThat(audit.detalhesTecnicos().potenciaNominalTrafoKva()).isEqualByComparingTo("500.0");
            assertThat(audit.mensagens())
                    .anySatisfy(msg -> {
                        assertThat(msg.tipo()).isEqualTo(NormativeMessageType.ERRO_NORMATIVO);
                        assertThat(msg.detalheTecnico()).isEqualTo("Ded > 300 kVA");
                        assertThat(msg.texto()).contains("13,8 kV").contains("Subestação");
                    });
        }
    }

    // =========================================================================
    // NÍVEL 2 — FLUXO E2E INTEGRADO VIA MOCKMVC + SPRING BOOT + H2 ISOLADO
    // =========================================================================

    @Nested
    @DisplayName("Nível 2 — Fluxo E2E Integrado via MockMvc (JWT -> Etapas -> Calcular -> Validação -> PDF)")
    class FluxoE2EIntegradoTests {

        @BeforeEach
        void cleanUpBefore() {
            limparBanco();
        }

        @AfterEach
        void cleanUpAfter() {
            limparBanco();
        }

        private void limparBanco() {
            demandResultsRepository.deleteAll();
            demandResultRepository.deleteAll();
            residentialTypologyRepository.deleteAll();
            commonAreaLoadRepository.deleteAll();
            commercialUnitRepository.deleteAll();
            specialLoadsRepository.deleteAll();
            projectRepository.deleteAll();
            userRepository.deleteAll();
        }

        private String registrarELogarJwt(String email, String nome) throws Exception {
            RegisterRequest register = new RegisterRequest(nome, email, "Guardrail123", "Guardrail123");
            register.setCrea("PE-54321/D");
            register.setEmpresa("NeoGuide Engenharia");

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(register)))
                    .andExpect(status().isCreated());

            LoginRequest login = new LoginRequest(email, "Guardrail123");
            MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
            String token = loginJson.get("token").asText();
            assertThat(token).isNotBlank();
            return token;
        }

        private Long criarProjeto(String token, CreateProjectRequest req) throws Exception {
            MvcResult result = mockMvc.perform(post("/api/v1/projects")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            Long projectId = json.get("id").asLong();
            assertThat(projectId).isPositive();
            return projectId;
        }

        @Test
        @DisplayName("Cenário 1 E2E — Memorial 100 Aptos: registro/JWT -> etapas -> calcular -> demand_results -> validação -> PDF")
        void cenario1_memorialCanonico100Apartamentos_fluxoE2ECompleto() throws Exception {
            // 1. Registro + Login JWT
            String token = registrarELogarJwt("gr01.cenario1@neoguide.com", "Engenheiro Guardrail C1");

            // 2. Criação do Projeto
            Long projectId = criarProjeto(token, CanonicalMemorialTestDataFactory.cenario1CreateProjectRequest());

            // 3. Etapa 2 — Residencial (50x63m² + 50x68m² -> Drf = 114,08 kVA)
            MvcResult resStage = mockMvc.perform(put("/api/v1/projects/" + projectId + "/etapa-residencial")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    CanonicalMemorialTestDataFactory.cenario1ResidentialStageRequest())))
                    .andExpect(status().isOk())
                    .andReturn();
            JsonNode resJson = objectMapper.readTree(resStage.getResponse().getContentAsString(StandardCharsets.UTF_8));
            assertThat(new BigDecimal(resJson.get("demandaResidencialFinalDrfKva").asText()))
                    .isEqualByComparingTo("114.08");

            // 4. Etapa 3 — Áreas Comuns (Ds = 69,32 kVA)
            MvcResult commonStage = mockMvc.perform(put("/api/v1/projects/" + projectId + "/etapa-area-comum")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    CanonicalMemorialTestDataFactory.cenario1AreaComumStageRequest())))
                    .andExpect(status().isOk())
                    .andReturn();
            JsonNode commonJson = objectMapper.readTree(commonStage.getResponse().getContentAsString(StandardCharsets.UTF_8));
            assertThat(new BigDecimal(commonJson.get("demandaServicosDsKva").asText()))
                    .isEqualByComparingTo("69.32");

            // 5. Etapa 5 — Cargas Especiais (MBI 2 cv -> Dmbi = 2,70 kVA)
            MvcResult specialStage = mockMvc.perform(put("/api/v1/projects/" + projectId + "/etapa-cargas-especiais")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    CanonicalMemorialTestDataFactory.cenario1SpecialLoadsStageRequest())))
                    .andExpect(status().isOk())
                    .andReturn();
            JsonNode specialJson = objectMapper.readTree(specialStage.getResponse().getContentAsString(StandardCharsets.UTF_8));
            assertThat(new BigDecimal(specialJson.get("demandaIncendioDmbiKva").asText()))
                    .isEqualByComparingTo("2.70");

            // 6. POST /api/v1/projects/{id}/calcular (200 CONCLUIDO)
            MvcResult calcResult = mockMvc.perform(post("/api/v1/projects/" + projectId + "/motor/calcular")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andReturn();
            JsonNode calcJson = objectMapper.readTree(calcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
            assertThat(calcJson.get("status").asText()).isEqualTo("CONCLUIDO");

            JsonNode resumo = calcJson.get("resumoDemandas");
            assertThat(new BigDecimal(resumo.get("demandaResidencialFinalDrfKva").asText())).isEqualByComparingTo("114.08");
            assertThat(new BigDecimal(resumo.get("demandaServicosDsKva").asText())).isEqualByComparingTo("69.32");
            assertThat(new BigDecimal(resumo.get("demandaComercialDcKva").asText())).isEqualByComparingTo("0.00");
            assertThat(new BigDecimal(resumo.get("demandaVeDveKva").asText())).isEqualByComparingTo("0.00");
            assertThat(new BigDecimal(resumo.get("demandaIncendioDmbiKva").asText())).isEqualByComparingTo("2.70");
            assertThat(new BigDecimal(resumo.get("demandaGlobalDedKva").asText())).isEqualByComparingTo("186.10");
            assertThat(new BigDecimal(resumo.get("demandaQdgKva").asText())).isEqualByComparingTo("183.40");

            JsonNode entrada = calcJson.get("dimensionamentoEntrada");
            assertThat(entrada.get("tipoFornecimento").asText()).isEqualTo("TRIFASICO_380_220V_BT");
            assertThat(entrada.get("disjuntorGeralA").asInt()).isEqualTo(320);
            assertThat(entrada.get("caboFaseMm2").asText()).contains("240 mm²");
            assertThat(entrada.get("barramentoCobreQdg").asText()).isEqualTo("30 x 5 mm");
            assertThat(entrada.get("capacidadeBarramentoA").asInt()).isEqualTo(449);

            // Conferência direta na tabela demand_results
            DemandResults persisted = demandResultsRepository.findByProjectId(projectId).orElseThrow();
            assertThat(persisted.getDemandaResidencialKva()).isEqualByComparingTo("114.08");
            assertThat(persisted.getDemandaServicoKva()).isEqualByComparingTo("69.32");
            assertThat(persisted.getDemandaIncendioKva()).isEqualByComparingTo("2.70");
            assertThat(persisted.getDemandaGlobalKva()).isEqualByComparingTo("186.10");
            assertThat(persisted.getDemandaQdgKva()).isEqualByComparingTo("183.40");
            assertThat(persisted.getDisjuntorGeralA()).isEqualTo(320);
            assertThat(persisted.getBarramentoCobre()).isEqualTo("30 x 5 mm");
            assertThat(persisted.getCapacidadeBarramentoA()).isEqualTo(449);

            // 7. GET /api/v1/projects/{id}/validacao-normativa (200 CONFORME_DIS_NOR_053)
            MvcResult auditResult = mockMvc.perform(get("/api/v1/projects/" + projectId + "/motor/validacao-normativa")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andReturn();
            JsonNode auditJson = objectMapper.readTree(auditResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
            assertThat(auditJson.get("conforme").asBoolean()).isTrue();
            assertThat(auditJson.get("seloConformidade").asText()).isEqualTo("CONFORME_DIS_NOR_053");
            assertThat(auditJson.get("totalErrosNormativos").asInt()).isZero();
            assertThat(new BigDecimal(auditJson.get("detalhesTecnicos").get("correnteProjetoIpA").asText()))
                    .isEqualByComparingTo("278.65");
            assertThat(auditJson.get("detalhesTecnicos").get("nivelTensaoEnquadrado").asText())
                    .isEqualTo("BAIXA_TENSAO");

            // 8. GET /api/v1/projects/{id}/memorial-pdf (200 application/pdf iniciando com %PDF-)
            MvcResult pdfResult = mockMvc.perform(get("/api/v1/projects/" + projectId + "/motor/memorial-pdf")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                    .andReturn();
            byte[] pdfBytes = pdfResult.getResponse().getContentAsByteArray();
            assertThat(pdfBytes.length).isGreaterThan(100);
            assertThat(new String(pdfBytes, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        }

        @Test
        @DisplayName("Cenário 2 E2E — Smart Studios (40x28m²): compara Drf=46,80 (Smart Studio) vs Drf=38,38 (Convencional)")
        void cenario2_smartStudios_fluxoE2E() throws Exception {
            String token = registrarELogarJwt("gr01.cenario2@neoguide.com", "Engenheiro Guardrail C2");

            CreateProjectRequest reqStudio = CanonicalMemorialTestDataFactory.cenario1CreateProjectRequest();
            reqStudio.setTitulo("Residencial Smart Studios 40 Unidades");
            Long studioId = criarProjeto(token, reqStudio);

            mockMvc.perform(put("/api/v1/projects/" + studioId + "/etapa-residencial")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    CanonicalMemorialTestDataFactory.cenario2ResidentialStageRequest(true))))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/v1/projects/" + studioId + "/motor/calcular")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            DemandResults studioResults = demandResultsRepository.findByProjectId(studioId).orElseThrow();
            assertThat(studioResults.getDemandaResidencialKva()).isEqualByComparingTo("46.80");

            CreateProjectRequest reqConv = CanonicalMemorialTestDataFactory.cenario1CreateProjectRequest();
            reqConv.setTitulo("Residencial Convencional 40 Unidades");
            Long convId = criarProjeto(token, reqConv);

            mockMvc.perform(put("/api/v1/projects/" + convId + "/etapa-residencial")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    CanonicalMemorialTestDataFactory.cenario2ResidentialStageRequest(false))))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/v1/projects/" + convId + "/motor/calcular")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            DemandResults convResults = demandResultsRepository.findByProjectId(convId).orElseThrow();
            assertThat(convResults.getDemandaResidencialKva()).isEqualByComparingTo("38.38");
        }

        @Test
        @DisplayName("Cenário 3 E2E — Trava de VE 50% do Transformador (Item 6.26.2): reprova acima e aprova abaixo do limite")
        void cenario3_travaVe50Porcento_fluxoE2E() throws Exception {
            String token = registrarELogarJwt("gr01.cenario3@neoguide.com", "Engenheiro Guardrail C3");

            // Caso 3A: Violação (40x63m² + Ds=30 + 15 vagas VE x 7,4kW -> Dve=95,46 + MBI 2cv -> Ded=180,23, Strafo=225, uso=55,76%)
            CreateProjectRequest req = CanonicalMemorialTestDataFactory.cenario1CreateProjectRequest();
            req.setTitulo("Edifício Recarga VE Trava 50%");
            req.setPossuiVe(true);
            Long projectId = criarProjeto(token, req);

            mockMvc.perform(put("/api/v1/projects/" + projectId + "/etapa-residencial")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    CanonicalMemorialTestDataFactory.cenario3ResidentialViolacaoStageRequest())))
                    .andExpect(status().isOk());

            mockMvc.perform(put("/api/v1/projects/" + projectId + "/etapa-area-comum")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    CanonicalMemorialTestDataFactory.areaComumPersonalizadaStageRequest(new BigDecimal("30.00")))))
                    .andExpect(status().isOk());

            mockMvc.perform(put("/api/v1/projects/" + projectId + "/etapa-cargas-especiais")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    CanonicalMemorialTestDataFactory.cenario3VeViolacaoStageRequest())))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/v1/projects/" + projectId + "/motor/calcular")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            DemandResults violacaoPersisted = demandResultsRepository.findByProjectId(projectId).orElseThrow();
            assertThat(violacaoPersisted.getDemandaVeKva()).isEqualByComparingTo("95.46");
            assertThat(violacaoPersisted.getDemandaGlobalKva()).isEqualByComparingTo("180.23");

            MvcResult auditViolacaoRes = mockMvc.perform(get("/api/v1/projects/" + projectId + "/motor/validacao-normativa")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andReturn();
            JsonNode auditViolacao = objectMapper.readTree(auditViolacaoRes.getResponse().getContentAsString(StandardCharsets.UTF_8));
            assertThat(auditViolacao.get("conforme").asBoolean()).isFalse();
            assertThat(auditViolacao.get("seloConformidade").asText()).isEqualTo("PENDENTE_AJUSTES");
            assertThat(auditViolacao.get("totalErrosNormativos").asInt()).isEqualTo(1);
            assertThat(new BigDecimal(auditViolacao.get("detalhesTecnicos").get("percentualOcupacaoVeServico").asText()))
                    .isEqualByComparingTo("55.76");

            // Caso 3B: Adequação abaixo do limite (100 aptos + Ds=30 + 8 vagas VE x 3,7kW -> Dve=29,60 -> uso=26,49% <= 40%)
            // Projeto concluído é somente leitura: reabre para ajustar as etapas, como o usuário faria ao duplicar/editar.
            projectRepository.findById(projectId).ifPresent(p -> {
                p.setStatus(neoguide.project.model.ProjectStatus.RASCUNHO);
                projectRepository.save(p);
            });

            mockMvc.perform(put("/api/v1/projects/" + projectId + "/etapa-residencial")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    CanonicalMemorialTestDataFactory.cenario1ResidentialStageRequest())))
                    .andExpect(status().isOk());

            mockMvc.perform(put("/api/v1/projects/" + projectId + "/etapa-cargas-especiais")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    CanonicalMemorialTestDataFactory.cenario3VeConformeStageRequest())))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/v1/projects/" + projectId + "/motor/calcular")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            MvcResult auditConformeRes = mockMvc.perform(get("/api/v1/projects/" + projectId + "/motor/validacao-normativa")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andReturn();
            JsonNode auditConforme = objectMapper.readTree(auditConformeRes.getResponse().getContentAsString(StandardCharsets.UTF_8));
            assertThat(auditConforme.get("conforme").asBoolean()).isTrue();
            assertThat(auditConforme.get("seloConformidade").asText()).isEqualTo("CONFORME_DIS_NOR_053");
            assertThat(auditConforme.get("totalErrosNormativos").asInt()).isZero();
            assertThat(new BigDecimal(auditConforme.get("detalhesTecnicos").get("percentualOcupacaoVeServico").asText()))
                    .isEqualByComparingTo("26.49");
        }

        @Test
        @DisplayName("Cenário 4 E2E — Grande Porte / Média Tensão (200x85m² + serviços + comercial + MBI 5cv -> Ded=367,18 kVA)")
        void cenario4_grandePorteMediaTensao_fluxoE2E() throws Exception {
            String token = registrarELogarJwt("gr01.cenario4@neoguide.com", "Engenheiro Guardrail C4");
            Long projectId = criarProjeto(token, CanonicalMemorialTestDataFactory.cenario4CreateProjectRequest());

            mockMvc.perform(put("/api/v1/projects/" + projectId + "/etapa-residencial")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    CanonicalMemorialTestDataFactory.cenario4ResidentialStageRequest())))
                    .andExpect(status().isOk());

            mockMvc.perform(put("/api/v1/projects/" + projectId + "/etapa-area-comum")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    CanonicalMemorialTestDataFactory.areaComumPersonalizadaStageRequest(new BigDecimal("45.00")))))
                    .andExpect(status().isOk());

            mockMvc.perform(put("/api/v1/projects/" + projectId + "/etapa-comercial")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    CanonicalMemorialTestDataFactory.cenario4CommercialStageRequest())))
                    .andExpect(status().isOk());

            mockMvc.perform(put("/api/v1/projects/" + projectId + "/etapa-cargas-especiais")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    CanonicalMemorialTestDataFactory.cenario4SpecialLoadsStageRequest())))
                    .andExpect(status().isOk());

            MvcResult calcResult = mockMvc.perform(post("/api/v1/projects/" + projectId + "/motor/calcular")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andReturn();
            JsonNode calcJson = objectMapper.readTree(calcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
            assertThat(calcJson.get("status").asText()).isEqualTo("CONCLUIDO");
            assertThat(calcJson.get("dimensionamentoEntrada").get("tipoFornecimento").asText())
                    .isEqualTo("SUBESTACAO_ABRIGADA_13_8KV");
            assertThat(calcJson.get("dimensionamentoEntrada").get("barramentoCobreQdg").asText())
                    .isEqualTo("40 x 10 mm");
            assertThat(calcJson.get("dimensionamentoEntrada").get("capacidadeBarramentoA").asInt())
                    .isEqualTo(994);

            DemandResults persisted = demandResultsRepository.findByProjectId(projectId).orElseThrow();
            assertThat(persisted.getDemandaResidencialKva()).isEqualByComparingTo("279.20");
            assertThat(persisted.getDemandaServicoKva()).isEqualByComparingTo("45.00");
            assertThat(persisted.getDemandaComercialKva()).isEqualByComparingTo("36.96");
            assertThat(persisted.getDemandaIncendioKva()).isEqualByComparingTo("6.02");
            assertThat(persisted.getDemandaGlobalKva()).isEqualByComparingTo("367.18");
            assertThat(persisted.getDemandaQdgKva()).isEqualByComparingTo("361.16");

            MvcResult auditResult = mockMvc.perform(get("/api/v1/projects/" + projectId + "/motor/validacao-normativa")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andReturn();
            JsonNode auditJson = objectMapper.readTree(auditResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
            assertThat(auditJson.get("conforme").asBoolean()).isFalse();
            assertThat(auditJson.get("detalhesTecnicos").get("nivelTensaoEnquadrado").asText())
                    .isEqualTo("MEDIA_TENSAO_OBRIGATORIA");
            assertThat(new BigDecimal(auditJson.get("detalhesTecnicos").get("potenciaNominalTrafoKva").asText()))
                    .isEqualByComparingTo("500.0");

            MvcResult pdfResult = mockMvc.perform(get("/api/v1/projects/" + projectId + "/motor/memorial-pdf")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                    .andReturn();
            byte[] pdfBytes = pdfResult.getResponse().getContentAsByteArray();
            assertThat(pdfBytes.length).isGreaterThan(100);
            assertThat(new String(pdfBytes, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        }
    }
}
