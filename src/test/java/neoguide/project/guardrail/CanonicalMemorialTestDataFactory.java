package neoguide.project.guardrail;

import neoguide.project.dto.CommercialStageRequest;
import neoguide.project.dto.EspacoComercialRequest;
import neoguide.project.dto.CommonAreaLoadItemRequest;
import neoguide.project.dto.CommonAreaStageRequest;
import neoguide.project.dto.CreateProjectRequest;
import neoguide.project.dto.ResidentialStageRequest;
import neoguide.project.dto.ResidentialTypologyItemRequest;
import neoguide.project.dto.SpecialLoadsStageRequest;
import neoguide.project.engine.model.CommercialUnitInput;
import neoguide.project.engine.model.ConsolidatedCalculationResult;
import neoguide.project.engine.model.ElectricVehicleLoadInput;
import neoguide.project.engine.model.FirePumpLoadInput;
import neoguide.project.engine.model.ResidentialCalculationInput;
import neoguide.project.engine.model.ResidentialTypologyItem;
import neoguide.project.engine.model.SpecialLoadsCalculationInput;
import neoguide.project.engine.model.commonarea.AirConditioningItem;
import neoguide.project.engine.model.commonarea.CommonAreaCalculationInput;
import neoguide.project.engine.model.commonarea.CustomLoadItem;
import neoguide.project.engine.model.commonarea.LightingTugInput;
import neoguide.project.engine.model.commonarea.MotorItem;
import neoguide.project.model.DemandResults;
import neoguide.project.model.Project;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Fixtures imutáveis (zero Lombok) para os 4 cenários canônicos de validação cruzada
 * aderentes às normas oficiais DIS-NOR-053 (Rev. 06) e DIS-NOR-030 (Rev. 07) da Neoenergia Pernambuco.
 */
public final class CanonicalMemorialTestDataFactory {

    public static final String TENSAO_380_220V = "380_220V";

    private CanonicalMemorialTestDataFactory() {
    }

    // =========================================================================
    // CENÁRIO 1 — Memorial Canônico 100 Apartamentos (DIS-NOR-053 Rev. 06)
    // =========================================================================

    public static ResidentialCalculationInput cenario1ResidencialInput() {
        return new ResidentialCalculationInput(
                List.of(
                        new ResidentialTypologyItem("Apartamento Tipo A (63 m²)", new BigDecimal("63.00"), 50),
                        new ResidentialTypologyItem("Apartamento Tipo B (68 m²)", new BigDecimal("68.00"), 50)
                ),
                false
        );
    }

    /**
     * Composição analítica de áreas comuns que totaliza exatamente Ds = 69,32 kVA:
     * - Parcela A (Iluminação 12 kW / 0,80 = 15,00 + TUG 10 kW * 0,50 = 5,00): 20,00 kVA
     * - Parcela F (Climatização 9,20 kW / 0,92): 10,00 kVA
     * - Parcela G (Motor 10 cv ativo = 11,54 + 10 cv reserva = 0,00 + 5 cv ativo * 0,50 = 3,01): 14,55 kVA
     * - Elevadores / Cargas operacionais personalizadas: 24,77 kVA
     */
    public static CommonAreaCalculationInput cenario1AreaComumDomainInput() {
        return new CommonAreaCalculationInput(
                new LightingTugInput(new BigDecimal("12.00"), new BigDecimal("10.00")),
                List.of(),
                List.of(),
                List.of(),
                null,
                List.of(new AirConditioningItem("Split Salão de Festas", null, new BigDecimal("9200.00"), null, 1)),
                List.of(
                        new MotorItem("Bomba Recalque Principal 10 cv", new BigDecimal("10.00"), null, false, true),
                        new MotorItem("Bomba Recalque Reserva 10 cv", new BigDecimal("10.00"), null, true, true),
                        new MotorItem("Bomba Esgoto 5 cv", new BigDecimal("5.00"), null, false, true)
                ),
                List.of(),
                List.of(),
                List.of(new CustomLoadItem("Elevadores Sociais e Serviço", new BigDecimal("24.77"), BigDecimal.ONE, BigDecimal.ONE, 1))
        );
    }

    public static CommonAreaStageRequest cenario1AreaComumStageRequest() {
        return new CommonAreaStageRequest(List.of(
                new CommonAreaLoadItemRequest("CATALOGO", "ILUMINACAO_TUG", null, null, null,
                        "ILUMINACAO", "Iluminação áreas comuns", 1, new BigDecimal("12.00"), new BigDecimal("0.80"), BigDecimal.ONE, false),
                new CommonAreaLoadItemRequest("CATALOGO", "ILUMINACAO_TUG", null, null, null,
                        "TUG", "Tomadas de uso geral", 1, new BigDecimal("10.00"), BigDecimal.ONE, new BigDecimal("0.50"), false),
                new CommonAreaLoadItemRequest("CATALOGO", "CLIMATIZACAO", null, null, null,
                        "SPLIT", "Climatização salão de festas", 1, new BigDecimal("9.20"), new BigDecimal("0.92"), BigDecimal.ONE, false),
                new CommonAreaLoadItemRequest("CATALOGO", "MOTORES", null, null, null,
                        "BOMBA_RECALQUE", "Bomba recalque 10 cv ativa", 1, new BigDecimal("7.355"), new BigDecimal("0.77"), BigDecimal.ONE, false),
                new CommonAreaLoadItemRequest("CATALOGO", "MOTORES", null, null, null,
                        "BOMBA_RECALQUE", "Bomba recalque 10 cv reserva", 1, new BigDecimal("7.355"), new BigDecimal("0.77"), BigDecimal.ZERO, true),
                new CommonAreaLoadItemRequest("CATALOGO", "MOTORES", null, null, null,
                        "BOMBA_ESGOTO", "Bomba esgoto 5 cv ativa", 1, new BigDecimal("3.6775"), new BigDecimal("0.75"), new BigDecimal("0.50"), false),
                new CommonAreaLoadItemRequest("PERSONALIZADO", "ILUMINACAO_TUG", null, null, null,
                        "ELEVADORES", "Elevadores sociais e serviço", 1, new BigDecimal("24.77"), BigDecimal.ONE, BigDecimal.ONE, false)
        ));
    }

    public static SpecialLoadsCalculationInput cenario1CargasEspeciaisDomainInput() {
        return SpecialLoadsCalculationInput.apenas(
                ElectricVehicleLoadInput.padrao(0, BigDecimal.ZERO),
                FirePumpLoadInput.padrao(new BigDecimal("2.00"))
        );
    }

    public static CreateProjectRequest cenario1CreateProjectRequest() {
        CreateProjectRequest req = new CreateProjectRequest();
        req.setTitulo("Edifício Memorial Canônico 100 Apartamentos");
        req.setDescricao("Validação cruzada DIS-NOR-053 Rev. 06 (50x63m² + 50x68m²)");
        req.setTipo("LIGACAO_NOVA");
        req.setTensaoFornecimento(TENSAO_380_220V);
        req.setPossuiComercial(false);
        req.setPossuiVe(false);
        req.setPossuiIncendio(true);
        req.setMunicipio("Recife");
        req.setCrea("PE-12345/D");
        req.setCep("52011-000");
        req.setEndereco("Av. Rui Barbosa, 1000");
        req.setBairro("Graças");
        return req;
    }

    public static ResidentialStageRequest cenario1ResidentialStageRequest() {
        return new ResidentialStageRequest(List.of(
                new ResidentialTypologyItemRequest(new BigDecimal("63.00"), 50, false),
                new ResidentialTypologyItemRequest(new BigDecimal("68.00"), 50, false)
        ));
    }

    public static SpecialLoadsStageRequest cenario1SpecialLoadsStageRequest() {
        return new SpecialLoadsStageRequest(0, null, new BigDecimal("2.00"), null);
    }

    // =========================================================================
    // CENÁRIO 2 — Smart Studios (Item 6.25 DIS-NOR-053 Rev. 06)
    // =========================================================================

    public static ResidentialCalculationInput cenario2SmartStudiosInput(boolean isSmartStudio) {
        return new ResidentialCalculationInput(
                List.of(new ResidentialTypologyItem("Smart Studio 28 m²", new BigDecimal("28.00"), 40, isSmartStudio)),
                isSmartStudio
        );
    }

    public static ResidentialStageRequest cenario2ResidentialStageRequest(boolean isSmartStudio) {
        return new ResidentialStageRequest(List.of(
                new ResidentialTypologyItemRequest(new BigDecimal("28.00"), 40, isSmartStudio)
        ));
    }

    // =========================================================================
    // CENÁRIO 3 — Trava de VE / 50% do Transformador (Item 6.26.2 DIS-NOR-053)
    // =========================================================================

    public static ResidentialCalculationInput cenario3ResidencialViolacaoInput() {
        return new ResidentialCalculationInput(
                List.of(new ResidentialTypologyItem("Apartamento 63 m²", new BigDecimal("63.00"), 40)),
                false
        );
    }

    public static ResidentialStageRequest cenario3ResidentialViolacaoStageRequest() {
        return new ResidentialStageRequest(List.of(
                new ResidentialTypologyItemRequest(new BigDecimal("63.00"), 40, false)
        ));
    }

    public static CommonAreaStageRequest areaComumPersonalizadaStageRequest(BigDecimal dsKva) {
        return new CommonAreaStageRequest(List.of(
                new CommonAreaLoadItemRequest("PERSONALIZADO", "ILUMINACAO_TUG", null, null, null,
                        "SERVICOS_CONDOMINIO", "Serviços gerais de áreas comuns", 1, dsKva, BigDecimal.ONE, BigDecimal.ONE, false)
        ));
    }

    /**
     * 15 vagas × 7,40 kW -> Quadro 33 (11–20 vagas: Fve = 0,86) -> Dve = 95,46 kVA.
     * Com Ds = 30,00 kVA e Strafo = 225,0 kVA -> Ds + Dve = 125,46 kVA (55,76% > 50%).
     */
    public static SpecialLoadsCalculationInput cenario3VeViolacaoDomainInput(BigDecimal dsKva, BigDecimal sTrafoKva) {
        return new SpecialLoadsCalculationInput(
                ElectricVehicleLoadInput.padrao(15, new BigDecimal("7.40")),
                FirePumpLoadInput.padrao(new BigDecimal("2.00")),
                sTrafoKva,
                dsKva
        );
    }

    /**
     * 8 vagas × 3,70 kW -> Quadro 33 (1–10 vagas: Fve = 1,00) -> Dve = 29,60 kVA.
     * Com Ds = 30,00 kVA e Strafo = 225,0 kVA -> Ds + Dve = 59,60 kVA (26,49% <= 40%).
     */
    public static SpecialLoadsCalculationInput cenario3VeConformeDomainInput(BigDecimal dsKva, BigDecimal sTrafoKva) {
        return new SpecialLoadsCalculationInput(
                ElectricVehicleLoadInput.padrao(8, new BigDecimal("3.70")),
                FirePumpLoadInput.padrao(new BigDecimal("2.00")),
                sTrafoKva,
                dsKva
        );
    }

    public static SpecialLoadsStageRequest cenario3VeViolacaoStageRequest() {
        return new SpecialLoadsStageRequest(15, new BigDecimal("7.40"), new BigDecimal("2.00"), null);
    }

    public static SpecialLoadsStageRequest cenario3VeConformeStageRequest() {
        return new SpecialLoadsStageRequest(8, new BigDecimal("3.70"), new BigDecimal("2.00"), null);
    }

    // =========================================================================
    // CENÁRIO 4 — Grande Porte / Média Tensão (Item 6.1 DIS-NOR-053 Rev. 06)
    // =========================================================================

    public static ResidentialCalculationInput cenario4ResidencialInput() {
        return new ResidentialCalculationInput(
                List.of(new ResidentialTypologyItem("Apartamento 85 m²", new BigDecimal("85.00"), 200)),
                false
        );
    }

    public static ResidentialStageRequest cenario4ResidentialStageRequest() {
        return new ResidentialStageRequest(List.of(
                new ResidentialTypologyItemRequest(new BigDecimal("85.00"), 200, false)
        ));
    }

    public static CommonAreaCalculationInput cenario4AreaComumDomainInput() {
        return new CommonAreaCalculationInput(
                new LightingTugInput(new BigDecimal("16.00"), new BigDecimal("10.00")),
                List.of(),
                List.of(),
                List.of(),
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(new CustomLoadItem("Elevadores e Serviços Centrais", new BigDecimal("20.00"), BigDecimal.ONE, BigDecimal.ONE, 1))
        );
    }

    /** Cenário 4: 40 kW de escritórios/salas comerciais (Tabela 22: 100% até 20 kW, 70% no excedente, FP 0,92). */
    public static List<CommercialUnitInput> cenario4ComercialDomainInput() {
        return List.of(new CommercialUnitInput("ESCRITORIOS", "Escritórios e Salas Comerciais",
                new BigDecimal("200.00"), new BigDecimal("40.00"),
                new BigDecimal("30.00"), new BigDecimal("1.00"), new BigDecimal("0.92"), true));
    }

    public static CommercialStageRequest cenario4CommercialStageRequest() {
        return new CommercialStageRequest(List.of(
                new EspacoComercialRequest("ESCRITORIOS", new BigDecimal("200.00"), new BigDecimal("40.00"))
        ));
    }

    public static SpecialLoadsCalculationInput cenario4CargasEspeciaisDomainInput() {
        return SpecialLoadsCalculationInput.apenas(
                ElectricVehicleLoadInput.padrao(0, BigDecimal.ZERO),
                FirePumpLoadInput.padrao(new BigDecimal("5.00"))
        );
    }

    public static SpecialLoadsStageRequest cenario4SpecialLoadsStageRequest() {
        return new SpecialLoadsStageRequest(0, null, new BigDecimal("5.00"), null);
    }

    public static CreateProjectRequest cenario4CreateProjectRequest() {
        CreateProjectRequest req = new CreateProjectRequest();
        req.setTitulo("Torre Empresarial e Residencial Grande Porte 200 Aptos");
        req.setDescricao("Validação cruzada Média Tensão Primária 13,8 kV (Ded > 300 kVA)");
        req.setTipo("LIGACAO_NOVA");
        req.setTensaoFornecimento(TENSAO_380_220V);
        req.setPossuiComercial(true);
        req.setPossuiVe(false);
        req.setPossuiIncendio(true);
        req.setMunicipio("Recife");
        req.setCrea("PE-99887/D");
        return req;
    }

    // =========================================================================
    // HELPERS PARA VALIDAÇÃO NORMATIVA EM DOMÍNIO PURO
    // =========================================================================

    public static Project buildDomainProject(Long id, String protocolo, String tensao) {
        Project project = new Project();
        project.setId(id);
        project.setProtocolo(protocolo);
        project.setTensaoFornecimento(tensao);
        return project;
    }

    public static DemandResults buildDomainDemandResults(Project project,
                                                         BigDecimal drf,
                                                         BigDecimal ds,
                                                         BigDecimal dc,
                                                         BigDecimal dve,
                                                         BigDecimal dmbi,
                                                         ConsolidatedCalculationResult consolidated) {
        DemandResults results = new DemandResults();
        results.setProject(project);
        results.setDemandaResidencialKva(drf);
        results.setDemandaServicoKva(ds);
        results.setDemandaComercialKva(dc);
        results.setDemandaVeKva(dve);
        results.setDemandaIncendioKva(dmbi);
        results.setDemandaGlobalKva(consolidated.demandaTotalEdificacaoDed());
        results.setDemandaQdgKva(consolidated.demandaQdgDqdg());
        results.setDisjuntorGeralA(consolidated.padraoEntrada() != null
                ? consolidated.padraoEntrada().disjuntorCorrenteInA()
                : null);
        results.setCapacidadeBarramentoA(consolidated.barramentoQdg() != null
                ? consolidated.barramentoQdg().correnteNominalA()
                : null);
        results.setCalculadoEm(LocalDateTime.now());
        return results;
    }
}
