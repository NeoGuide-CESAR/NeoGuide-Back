package neoguide.project.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import neoguide.project.model.CommercialUnit;
import neoguide.project.model.CommonAreaLoad;
import neoguide.project.model.Project;
import neoguide.project.model.ResidentialTypology;
import neoguide.project.model.SpecialLoads;
import neoguide.project.model.TipoEdificacao;
import neoguide.project.model.User;
import neoguide.project.repository.CommercialUnitRepository;
import neoguide.project.repository.CommonAreaLoadRepository;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.ResidentialTypologyRepository;
import neoguide.project.repository.SpecialLoadsRepository;
import neoguide.project.repository.UserRepository;
import neoguide.project.security.jwt.JwtService;
import neoguide.project.support.TestDatabaseCleaner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Catálogo de regras da Revisão: um caso positivo e um negativo por regra, sempre lendo o que está no banco. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ValidacaoNormativaTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private ResidentialTypologyRepository tipologiaRepository;
    @Autowired private CommonAreaLoadRepository cargaRepository;
    @Autowired private CommercialUnitRepository espacoRepository;
    @Autowired private SpecialLoadsRepository especiaisRepository;
    @Autowired private TestDatabaseCleaner testDatabaseCleaner;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private final ObjectMapper json = new ObjectMapper();
    private String token;
    private int sequencia;

    @BeforeEach
    void setUp() {
        testDatabaseCleaner.cleanOperationalTables();
        User dono = userRepository.save(new User("Dono", "dono@regras.com", passwordEncoder.encode("SenhaForte@123")));
        token = jwtService.generateToken(dono);
    }

    @AfterEach
    void tearDown() {
        testDatabaseCleaner.cleanOperationalTables();
    }

    private Project projeto(TipoEdificacao tipo, boolean ve, boolean incendio) {
        Project p = new Project();
        p.setUser(userRepository.findAll().get(0));
        p.setProtocolo("PRJ-2026-" + (9000 + ++sequencia));
        p.setTitulo("Edifício");
        p.setTipoEdificacao(tipo);
        p.setTensaoFornecimento("380_220V");
        p.setMunicipio("Recife");
        p.setCep("50050000");
        p.setEndereco("Rua do Exemplo, 120");
        p.setPossuiVe(ve);
        p.setPossuiIncendio(incendio);
        return projectRepository.save(p);
    }

    private void tipologia(Project p, String area, int unidades, boolean smart) {
        tipologiaRepository.save(new ResidentialTypology(p, "T", new BigDecimal(area), unidades, smart, null, null));
    }

    private void carga(Project p, String submodulo, Integer quantidade, String potencia, Long catalogoMotorId) {
        cargaRepository.save(new CommonAreaLoad(p, catalogoMotorId == null ? "PERSONALIZADO" : "CATALOGO", submodulo, catalogoMotorId, null, null,
                "EQUIP", "Equipamento", quantidade, potencia == null ? null : new BigDecimal(potencia), BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO, false));
    }

    private void espaco(Project p, int ordem, String area, String carga) {
        espacoRepository.save(new CommercialUnit(p, ordem, "LOJAS", "Loja", "Loja", new BigDecimal(area), new BigDecimal(carga), BigDecimal.ONE, BigDecimal.ZERO));
    }

    private void especiais(Project p, Integer pontos, String potenciaVe, String cv, Boolean medidor) {
        SpecialLoads s = new SpecialLoads(p, pontos, potenciaVe == null ? null : new BigDecimal(potenciaVe), BigDecimal.ONE, BigDecimal.ZERO,
                cv == null ? null : new BigDecimal(cv), BigDecimal.ZERO, null);
        s.setMedidorExclusivo(medidor);
        especiaisRepository.save(s);
    }

    private JsonNode validar(Project p) throws Exception {
        String corpo = mockMvc.perform(get("/api/v1/projects/" + p.getId() + "/validacao-normativa").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(corpo);
    }

    private List<String> codigos(JsonNode v) {
        return v.get("itens").findValuesAsText("codigo");
    }

    private JsonNode item(JsonNode v, String codigo) {
        for (JsonNode i : v.get("itens")) if (codigo.equals(i.get("codigo").asText())) return i;
        return null;
    }

    // ---------------------------------------------------------------- ABA_FORNOS_VAZIA

    @Test
    @DisplayName("ABA_FORNOS_VAZIA: sem fornos nem fogões nas cargas gravadas → dispara; com qualquer um > 0 → não")
    void abaFornosVazia() throws Exception {
        Project p = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, false, false);
        tipologia(p, "65", 24, false);
        carga(p, "AQUECIMENTO", 6, "5.5", null);

        JsonNode v = validar(p);
        assertTrue(codigos(v).contains("ABA_FORNOS_VAZIA"));
        assertEquals("ALERTA", item(v, "ABA_FORNOS_VAZIA").get("nivel").asText());
        assertEquals("fornos", item(v, "ABA_FORNOS_VAZIA").get("aba").asText());
        assertEquals("area-comum", item(v, "ABA_FORNOS_VAZIA").get("etapa").asText());

        carga(p, "COCCAO", 1, "4.0", null); // um forno elétrico
        assertFalse(codigos(validar(p)).contains("ABA_FORNOS_VAZIA"));
    }

    @Test
    @DisplayName("ABA_FORNOS_VAZIA: quantidade 0 em cocção continua vazia; só cooktops > 0 também resolve")
    void abaFornosVaziaQuantidadeZeroOuNula() throws Exception {
        Project p = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, false, false);
        tipologia(p, "65", 24, false);
        carga(p, "COCCAO", 0, "4.0", null);
        assertTrue(codigos(validar(p)).contains("ABA_FORNOS_VAZIA"));

        carga(p, "COCCAO", 2, "5.0", null); // cooktops
        assertFalse(codigos(validar(p)).contains("ABA_FORNOS_VAZIA"));
    }

    @Test
    @DisplayName("Área comum sem nenhuma carga gravada não gera alerta nenhum (a etapa ainda não foi preenchida)")
    void areaComumNaoGravadaNaoAlerta() throws Exception {
        Project p = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, false, false);
        tipologia(p, "65", 24, false);
        JsonNode v = validar(p);
        assertEquals(0, v.get("totais").get("alertas").asInt());
        assertEquals("OK", v.get("situacao").asText());
    }

    // ---------------------------------------------------------------- MOTOBOMBA

    @Test
    @DisplayName("MOTOBOMBA_SEM_MEDIDOR_EXCLUSIVO: sem incêndio nunca dispara; com incêndio dispara até o medidor ser marcado")
    void motobombaSemMedidor() throws Exception {
        Project sem = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, false, false);
        tipologia(sem, "65", 24, false);
        assertFalse(codigos(validar(sem)).contains("MOTOBOMBA_SEM_MEDIDOR_EXCLUSIVO"));
        especiais(sem, null, null, "15", null);
        assertFalse(codigos(validar(sem)).contains("MOTOBOMBA_SEM_MEDIDOR_EXCLUSIVO"));

        Project com = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, false, true);
        tipologia(com, "65", 24, false);
        especiais(com, null, null, "15", false);
        JsonNode v = validar(com);
        assertEquals("ALERTA", item(v, "MOTOBOMBA_SEM_MEDIDOR_EXCLUSIVO").get("nivel").asText());
        assertEquals("medidorExclusivo", item(v, "MOTOBOMBA_SEM_MEDIDOR_EXCLUSIVO").get("campo").asText());

        SpecialLoads s = especiaisRepository.findByProjectId(com.getId()).orElseThrow();
        s.setMedidorExclusivo(true);
        especiaisRepository.save(s);
        assertFalse(codigos(validar(com)).contains("MOTOBOMBA_SEM_MEDIDOR_EXCLUSIVO"));
    }

    @Test
    @DisplayName("MOTOBOMBA_INCOMPLETA só vale com incêndio marcado e cv dentro de 1,0 a 50,0")
    void motobombaIncompleta() throws Exception {
        Project p = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, false, true);
        tipologia(p, "65", 24, false);
        assertTrue(codigos(validar(p)).contains("MOTOBOMBA_INCOMPLETA"));
        especiais(p, null, null, "60", true);
        assertTrue(codigos(validar(p)).contains("MOTOBOMBA_INCOMPLETA"));
        SpecialLoads s = especiaisRepository.findByProjectId(p.getId()).orElseThrow();
        s.setPotenciaBombaIncendioCv(new BigDecimal("15"));
        especiaisRepository.save(s);
        assertFalse(codigos(validar(p)).contains("MOTOBOMBA_INCOMPLETA"));
    }

    // ---------------------------------------------------------------- VE

    @Test
    @DisplayName("VE_INCOMPLETO: só com VE marcado; quantidade 1–500 e potência 3,70–22,0 kW")
    void veIncompleto() throws Exception {
        Project sem = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, false, false);
        tipologia(sem, "65", 24, false);
        assertFalse(codigos(validar(sem)).contains("VE_INCOMPLETO"));

        Project p = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, true, false);
        tipologia(p, "65", 24, false);
        assertEquals(2, codigos(validar(p)).stream().filter("VE_INCOMPLETO"::equals).count());
        especiais(p, 6, "25.0", null, null);
        JsonNode v = validar(p);
        assertEquals(1, codigos(v).stream().filter("VE_INCOMPLETO"::equals).count());
        assertEquals("potenciaVeKw", item(v, "VE_INCOMPLETO").get("campo").asText());
        SpecialLoads s = especiaisRepository.findByProjectId(p.getId()).orElseThrow();
        s.setPotenciaEstacaoVeKw(new BigDecimal("7.4"));
        especiaisRepository.save(s);
        assertFalse(codigos(validar(p)).contains("VE_INCOMPLETO"));
    }

    // ---------------------------------------------------------------- ÁREA COMUM

    @Test
    @DisplayName("AREA_COMUM_POTENCIA_AUSENTE: quantidade 0 sem potência não dispara; quantidade 2 sem potência é ERRO na aba e no item")
    void areaComumPotenciaAusente() throws Exception {
        Project p = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, false, false);
        tipologia(p, "65", 24, false);
        carga(p, "COCCAO", 1, "4.0", null);
        carga(p, "AQUECIMENTO", 0, null, null);
        assertFalse(codigos(validar(p)).contains("AREA_COMUM_POTENCIA_AUSENTE"));

        carga(p, "MOTORES", 2, null, null);
        JsonNode v = validar(p);
        JsonNode i = item(v, "AREA_COMUM_POTENCIA_AUSENTE");
        assertEquals("ERRO", i.get("nivel").asText());
        assertEquals("motores", i.get("aba").asText());
        assertEquals(2, i.get("item").asInt());
        assertEquals("potenciaNominalKw", i.get("campo").asText());

        carga(p, "MOTORES", 2, null, 7L); // item vindo do catálogo traz a potência do próprio catálogo
        assertEquals(1, codigos(validar(p)).stream().filter("AREA_COMUM_POTENCIA_AUSENTE"::equals).count());
    }

    // ---------------------------------------------------------------- COMERCIAL

    @Test
    @DisplayName("COMERCIAL_ESPACO_INCOMPLETO aponta o índice do espaço e o campo")
    void comercialEspacoIncompleto() throws Exception {
        Project p = projeto(TipoEdificacao.MISTO, false, false);
        tipologia(p, "65", 24, false);
        espaco(p, 1, "120", "20");
        espaco(p, 2, "0", "20");
        JsonNode v = validar(p);
        JsonNode i = item(v, "COMERCIAL_ESPACO_INCOMPLETO");
        assertEquals("Espaço 2 está sem área.", i.get("mensagem").asText());
        assertEquals(1, i.get("item").asInt());
        assertEquals("areaM2", i.get("campo").asText());
        assertEquals("ERRO", i.get("nivel").asText());
    }

    @Test
    @DisplayName("COMERCIAL_CARGA_ABAIXO_MINIMO: ALERTA com os valores no texto; espaço sem problema não dispara")
    void comercialCargaAbaixoDoMinimo() throws Exception {
        Project p = projeto(TipoEdificacao.MISTO, false, false);
        tipologia(p, "65", 24, false);
        espaco(p, 1, "120", "20");
        assertFalse(codigos(validar(p)).contains("COMERCIAL_CARGA_ABAIXO_MINIMO"));

        espaco(p, 2, "85", "1");
        JsonNode i = item(validar(p), "COMERCIAL_CARGA_ABAIXO_MINIMO");
        assertEquals("ALERTA", i.get("nivel").asText());
        assertEquals(1, i.get("item").asInt());
        assertEquals("Espaço 2: carga de 1,0 kW abaixo do mínimo da ocupação (2,6 kW para 85 m²).", i.get("mensagem").asText());
    }

    @Test
    @DisplayName("Residencial com espaço comercial gera ALERTA TIPO_ENQUADRAMENTO_MISTO (sem erro); misto e residencial sem espaço não")
    void tipoEnquadramentoMisto() throws Exception {
        Project r = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, false, false);
        tipologia(r, "65", 24, false);
        espaco(r, 1, "120", "20");
        JsonNode v = validar(r);
        JsonNode i = item(v, "TIPO_ENQUADRAMENTO_MISTO");
        assertEquals("ALERTA", i.get("nivel").asText());
        assertEquals("comercial", i.get("etapa").asText());
        assertEquals("Revisar espaços", i.get("rotuloAcao").asText());
        assertEquals("Alterar tipo", i.get("alternativa").get("rotulo").asText());
        assertEquals("projeto", i.get("alternativa").get("etapa").asText());
        assertEquals("tipoEdificacao", i.get("alternativa").get("campo").asText());
        assertEquals(0, v.get("totais").get("erros").asInt());
        assertFalse(codigos(v).contains("COMERCIAL_TIPO_INCOMPATIVEL"));

        Project m = projeto(TipoEdificacao.MISTO, false, false);
        tipologia(m, "65", 24, false);
        espaco(m, 1, "120", "20");
        assertFalse(codigos(validar(m)).contains("TIPO_ENQUADRAMENTO_MISTO"));

        Project semEspaco = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, false, false);
        tipologia(semEspaco, "65", 24, false);
        assertFalse(codigos(validar(semEspaco)).contains("TIPO_ENQUADRAMENTO_MISTO"));
    }

    // ---------------------------------------------------------------- RESIDENCIAL E PROJETO

    @Test
    @DisplayName("Residencial: sem grupos é ERRO; grupo fora da faixa aponta o item; smart studio com mais de 15 unidades é ALERTA")
    void residencial() throws Exception {
        Project p = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, false, false);
        assertTrue(codigos(validar(p)).contains("RESIDENCIAL_SEM_GRUPOS"));

        tipologia(p, "65", 24, false);
        tipologia(p, "5", 16, true);
        JsonNode v = validar(p);
        JsonNode grupo = item(v, "RESIDENCIAL_GRUPO_INCOMPLETO");
        assertEquals(1, grupo.get("item").asInt());
        assertEquals("areaUtilM2", grupo.get("campo").asText());
        assertEquals("ALERTA", item(v, "SMART_STUDIO_FC_90").get("nivel").asText());

        Project pequeno = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, false, false);
        tipologia(pequeno, "65", 10, true);
        assertFalse(codigos(validar(pequeno)).contains("SMART_STUDIO_FC_90"));
    }

    @Test
    @DisplayName("PROJETO_CAMPO_OBRIGATORIO: um item por campo vazio, na etapa 1")
    void projetoCampoObrigatorio() throws Exception {
        Project p = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, false, false);
        tipologia(p, "65", 24, false);
        p.setCep(null);
        p.setEndereco(" ");
        projectRepository.save(p);
        JsonNode v = validar(p);
        assertEquals(List.of("cep", "endereco"), v.get("itens").findValuesAsText("campo"));
        assertEquals("projeto", item(v, "PROJETO_CAMPO_OBRIGATORIO").get("etapa").asText());
    }

    // ---------------------------------------------------------------- TOTAIS E FONTE DOS DADOS

    @Test
    @DisplayName("Totais batem com a lista de itens e a validação sempre lê o banco: alterar e validar de novo muda o resultado")
    void totaisEFonteDosDados() throws Exception {
        Project p = projeto(TipoEdificacao.MISTO, false, false);
        tipologia(p, "65", 24, false);
        carga(p, "AQUECIMENTO", 6, "5.5", null);
        espaco(p, 1, "85", "1");

        JsonNode v = validar(p);
        long erros = v.get("itens").findValues("nivel").stream().filter(n -> "ERRO".equals(n.asText())).count();
        long alertas = v.get("itens").findValues("nivel").stream().filter(n -> "ALERTA".equals(n.asText())).count();
        assertEquals(erros, v.get("totais").get("erros").asInt());
        assertEquals(alertas, v.get("totais").get("alertas").asInt());
        assertEquals(2, alertas); // fornos vazios + carga abaixo do mínimo
        assertEquals("ATENCAO", v.get("situacao").asText());

        CommercialUnit e = espacoRepository.findAll().get(0);
        espacoRepository.delete(e);
        carga(p, "COCCAO", 1, "4.0", null);
        JsonNode depois = validar(p);
        assertEquals(0, depois.get("totais").get("alertas").asInt());
        assertEquals("OK", depois.get("situacao").asText());
    }

    @Test
    @DisplayName("Mensagens do resultado (até 50 kVA, média tensão, Ip) não aparecem na Revisão")
    void semMensagensDeResultado() throws Exception {
        Project p = projeto(TipoEdificacao.COMERCIAL, false, false);
        espaco(p, 1, "100", "10");
        List<String> codigos = codigos(validar(p));
        assertFalse(codigos.contains("demanda_ate_50_kva"));
        assertFalse(codigos.contains("atendimento_mt"));
        assertFalse(codigos.contains("rele_ajustavel"));
        assertFalse(codigos.contains("corrente_projeto_acima_disjuntor"));
    }

    @Test
    @DisplayName("Os campos citados pelas regras existem nos DTOs das etapas")
    void camposExistemNosDtos() throws Exception {
        List<Class<?>> dtos = List.of(neoguide.project.dto.CreateProjectRequest.class, neoguide.project.dto.ResidentialStageRequest.class,
                neoguide.project.dto.ResidentialTypologyItemRequest.class, neoguide.project.dto.CommonAreaLoadItemRequest.class,
                neoguide.project.dto.EspacoComercialRequest.class, neoguide.project.dto.CommercialStageRequest.class,
                neoguide.project.dto.SpecialLoadsStageRequest.class);
        for (java.lang.reflect.Field constante : neoguide.project.service.RegrasValidacaoNormativa.Campos.class.getFields()) {
            String campo = (String) constante.get(null);
            boolean existe = dtos.stream().anyMatch(c -> java.util.Arrays.stream(c.getDeclaredFields()).anyMatch(f -> f.getName().equals(campo)));
            assertTrue(existe, "Campo '" + campo + "' não existe em nenhum DTO de etapa");
        }
    }
}
