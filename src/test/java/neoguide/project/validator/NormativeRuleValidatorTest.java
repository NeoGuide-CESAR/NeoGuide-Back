package neoguide.project.validator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import neoguide.project.dto.validation.NormativeMessageType;
import neoguide.project.dto.validation.NormativeValidationResponse;
import neoguide.project.model.DemandResults;
import neoguide.project.model.Project;

class NormativeRuleValidatorTest {

    private NormativeRuleValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NormativeRuleValidator();
    }

    private Project criarProjeto(String tensao) {
        Project p = new Project();
        p.setId(10L);
        p.setProtocolo("PRJ-2026-0010");
        p.setTensaoFornecimento(tensao);
        return p;
    }

    @Test
    @DisplayName("Cenario Canonico 1: Projeto Conforme em Baixa Tensao (Ded=209.91 kVA, In=320A, Barra=449A)")
    void deveConcederSeloConformeEmBaixaTensao() {
        Project project = criarProjeto("380_220V");
        DemandResults results = new DemandResults();
        results.setDemandaResidencialKva(new BigDecimal("114.08"));
        results.setDemandaServicoKva(new BigDecimal("69.45"));
        results.setDemandaComercialKva(BigDecimal.ZERO);
        results.setDemandaVeKva(new BigDecimal("23.68"));
        results.setDemandaIncendioKva(new BigDecimal("2.70"));
        results.setDemandaGlobalKva(new BigDecimal("209.91"));
        results.setDemandaQdgKva(new BigDecimal("207.21"));
        results.setDisjuntorGeralA(320);
        results.setCapacidadeBarramentoA(449);
        results.setCalculadoEm(LocalDateTime.now());

        NormativeValidationResponse response = validator.validate(project, results);

        assertNotNull(response);
        assertTrue(response.conforme());
        assertEquals("CONFORME_DIS_NOR_053", response.seloConformidade());
        assertEquals(0, response.totalErrosNormativos());
        assertTrue(response.totalInformacoes() >= 3);
        assertEquals(new BigDecimal("225.0"), response.detalhesTecnicos().potenciaNominalTrafoKva());
        assertEquals("BAIXA_TENSAO", response.detalhesTecnicos().nivelTensaoEnquadrado());
    }

    @Test
    @DisplayName("Cenario Canonico 2: Impedimento por Media Tensao Obrigatoria (Ded=345.50 kVA > 300 kVA)")
    void deveReprovarQuandoDemandaExcede300kVa() {
        Project project = criarProjeto("380_220V");
        DemandResults results = new DemandResults();
        results.setDemandaGlobalKva(new BigDecimal("345.50"));
        results.setDemandaQdgKva(new BigDecimal("340.00"));
        results.setDemandaServicoKva(new BigDecimal("50.00"));
        results.setDemandaVeKva(BigDecimal.ZERO);
        results.setDisjuntorGeralA(630);
        results.setCapacidadeBarramentoA(650);

        NormativeValidationResponse response = validator.validate(project, results);

        assertNotNull(response);
        assertFalse(response.conforme());
        assertEquals("PENDENTE_AJUSTES", response.seloConformidade());
        assertEquals(1, response.totalErrosNormativos());
        assertEquals("MEDIA_TENSAO_OBRIGATORIA", response.detalhesTecnicos().nivelTensaoEnquadrado());
        assertTrue(response.mensagens().stream().anyMatch(m -> m.tipo() == NormativeMessageType.ERRO_NORMATIVO && m.regra().contains("6.2")));
    }

    @Test
    @DisplayName("Cenario Canonico 3: Sobrecarga Termica por Recarga VE (Ds + Dve > 50% Strafo)")
    void deveReprovarQuandoVeMaisServicosUltrapassa50PorcentoDoTrafo() {
        Project project = criarProjeto("380_220V");
        // Strafo para 145.20 kVA é 150 kVA. 50% = 75 kVA. Ds + Dve = 50 + 35 = 85 kVA (> 75 kVA).
        DemandResults results = new DemandResults();
        results.setDemandaGlobalKva(new BigDecimal("145.20"));
        results.setDemandaQdgKva(new BigDecimal("142.50"));
        results.setDemandaServicoKva(new BigDecimal("50.00"));
        results.setDemandaVeKva(new BigDecimal("35.00"));
        results.setDisjuntorGeralA(250);
        results.setCapacidadeBarramentoA(340);

        NormativeValidationResponse response = validator.validate(project, results);

        assertNotNull(response);
        assertFalse(response.conforme());
        assertEquals("PENDENTE_AJUSTES", response.seloConformidade());
        assertTrue(response.totalErrosNormativos() >= 1);
        assertTrue(response.mensagens().stream().anyMatch(m -> m.tipo() == NormativeMessageType.ERRO_NORMATIVO && m.regra().contains("6.26.2")));
    }

    @Test
    @DisplayName("Cenario Borda 4: Subdimensionamento do disjuntor geral (Ip > In)")
    void deveReprovarQuandoIpMaiorQueIn() {
        Project project = criarProjeto("380_220V");
        // D_QDG = 207.21 kVA => Ip = 314.82 A. Disjuntor configurado = 250 A.
        DemandResults results = new DemandResults();
        results.setDemandaGlobalKva(new BigDecimal("209.91"));
        results.setDemandaQdgKva(new BigDecimal("207.21"));
        results.setDemandaServicoKva(new BigDecimal("20.00"));
        results.setDemandaVeKva(BigDecimal.ZERO);
        results.setDisjuntorGeralA(250); // Subdimensionado
        results.setCapacidadeBarramentoA(449);

        NormativeValidationResponse response = validator.validate(project, results);

        assertNotNull(response);
        assertFalse(response.conforme());
        assertEquals("PENDENTE_AJUSTES", response.seloConformidade());
        assertTrue(response.mensagens().stream().anyMatch(m -> m.tipo() == NormativeMessageType.ERRO_NORMATIVO && m.texto().contains("Incompatibilidade de proteção geral")));
    }

    @Test
    @DisplayName("Cenario Borda 5: Edificacao simplificada com Ded <= 50 kVA")
    void deveEmitirNotaDeLigacaoSimplificadaParaDedMenorOuIgual50kVa() {
        Project project = criarProjeto("380_220V");
        DemandResults results = new DemandResults();
        results.setDemandaGlobalKva(new BigDecimal("35.00"));
        results.setDemandaQdgKva(new BigDecimal("35.00"));
        results.setDemandaServicoKva(new BigDecimal("5.00"));
        results.setDemandaVeKva(BigDecimal.ZERO);
        results.setDisjuntorGeralA(70);
        results.setCapacidadeBarramentoA(208);

        NormativeValidationResponse response = validator.validate(project, results);

        assertNotNull(response);
        assertTrue(response.conforme());
        assertTrue(response.mensagens().stream().anyMatch(m -> m.detalheTecnico().equals("Ded <= 50 kVA")));
    }
}
