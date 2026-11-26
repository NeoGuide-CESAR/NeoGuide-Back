package neoguide.project.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import neoguide.project.model.CommercialUnit;
import neoguide.project.model.Project;
import neoguide.project.model.ResidentialTypology;
import neoguide.project.model.SpecialLoads;
import neoguide.project.model.TipoEdificacao;
import neoguide.project.model.User;
import neoguide.project.repository.CommercialUnitRepository;
import neoguide.project.repository.DemandResultRepository;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.ResidentialTypologyRepository;
import neoguide.project.repository.SpecialLoadsRepository;
import neoguide.project.repository.UserRepository;
import neoguide.project.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Etapa comercial sempre opcional, enquadramento derivado e estados das etapas calculados no servidor. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EtapasEEnquadramentoTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private ResidentialTypologyRepository tipologiaRepository;
    @Autowired private CommercialUnitRepository espacoRepository;
    @Autowired private SpecialLoadsRepository especiaisRepository;
    @Autowired private DemandResultRepository resultadoRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private final ObjectMapper json = new ObjectMapper();
    private String token;
    private String tokenOutro;
    private int sequencia;

    @BeforeEach
    void setUp() {
        resultadoRepository.deleteAll();
        tipologiaRepository.deleteAll();
        espacoRepository.deleteAll();
        especiaisRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        User dono = userRepository.save(new User("Dono", "dono@etapas.com", passwordEncoder.encode("SenhaForte@123")));
        User outro = userRepository.save(new User("Outro", "outro@etapas.com", passwordEncoder.encode("SenhaForte@123")));
        token = jwtService.generateToken(dono);
        tokenOutro = jwtService.generateToken(outro);
    }

    private Project projeto(TipoEdificacao tipo, int etapaAtual) {
        Project p = new Project();
        p.setUser(userRepository.findAll().stream().filter(u -> u.getEmail().equals("dono@etapas.com")).findFirst().orElseThrow());
        p.setProtocolo("PRJ-2026-" + (8000 + ++sequencia));
        p.setTitulo("Edifício");
        p.setTipoEdificacao(tipo);
        p.setTensaoFornecimento("380_220V");
        p.setMunicipio("Recife");
        p.setCep("50050000");
        p.setEndereco("Rua do Exemplo, 120");
        p.setEtapaAtual(etapaAtual);
        return projectRepository.save(p);
    }

    private void tipologia(Project p) {
        tipologiaRepository.save(new ResidentialTypology(p, "T", new BigDecimal("65"), 10, false, null, null));
    }

    private JsonNode projetoJson(Project p, String tk) throws Exception {
        return json.readTree(mockMvc.perform(get("/api/v1/projects/" + p.getId()).header("Authorization", "Bearer " + tk))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    private List<String> estados(Project p) throws Exception {
        List<String> r = new ArrayList<>();
        for (JsonNode e : projetoJson(p, token).get("etapas")) r.add(e.get("estado").asText());
        return r;
    }

    private JsonNode calcular(Project p) throws Exception {
        return json.readTree(mockMvc.perform(post("/api/v1/projects/" + p.getId() + "/calcular").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    // ---------------------------------------------------------------- enquadramento

    @Test
    @DisplayName("Residencial com espaço comercial e Ded de 230 kVA é tratado como misto: MT (limite 225)")
    void residencialComEspacoComercialVaiParaMt() throws Exception {
        Project p = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, 5);
        tipologia(p);
        espacoRepository.save(new CommercialUnit(p, 1, "LOJAS", "Loja", "Loja", new BigDecimal("500"), new BigDecimal("250"), BigDecimal.ONE, new BigDecimal("230")));

        JsonNode c = calcular(p);
        assertTrue(c.get("enquadramento").get("atendimentoMT").asBoolean());
        assertEquals(225, c.get("enquadramento").get("limiteKva").asInt());
        assertEquals("RESIDENCIAL_MULTIFAMILIAR", c.get("enquadramento").get("tipoEdificacao").asText());
        assertEquals("MISTO", c.get("enquadramento").get("tipoEnquadramento").asText());
        assertEquals("edificação residencial com espaços comerciais, tratada como mista", c.get("enquadramento").get("motivoEnquadramento").asText());
        assertEquals("MISTO", projetoJson(p, token).get("tipoEnquadramento").asText());
    }

    @Test
    @DisplayName("Residencial sem espaço comercial e Ded de 230 kVA continua em BT (limite 300)")
    void residencialSemEspacoFicaEmBt() throws Exception {
        Project p = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, 5);
        tipologia(p);
        especiaisRepository.save(new SpecialLoads(p, 1, new BigDecimal("7.4"), BigDecimal.ONE, new BigDecimal("230"), null, BigDecimal.ZERO, null));

        JsonNode c = calcular(p);
        assertFalse(c.get("enquadramento").get("atendimentoMT").asBoolean());
        assertEquals(300, c.get("enquadramento").get("limiteKva").asInt());
        assertEquals("RESIDENCIAL_MULTIFAMILIAR", c.get("enquadramento").get("tipoEnquadramento").asText());
        assertTrue(c.get("enquadramento").get("motivoEnquadramento").isNull());
        assertEquals("RESIDENCIAL_MULTIFAMILIAR", projetoJson(p, token).get("tipoEnquadramento").asText());
    }

    @Test
    @DisplayName("Misto usa o próprio tipo no enquadramento")
    void mistoEnquadraComoMisto() throws Exception {
        Project p = projeto(TipoEdificacao.MISTO, 5);
        tipologia(p);
        espacoRepository.save(new CommercialUnit(p, 1, "LOJAS", "Loja", "Loja", new BigDecimal("120"), new BigDecimal("20"), BigDecimal.ONE, new BigDecimal("21.74")));
        assertEquals("MISTO", calcular(p).get("enquadramento").get("tipoEnquadramento").asText());
    }

    // ---------------------------------------------------------------- estados das etapas

    @Test
    @DisplayName("Projeto novo: só a etapa 1 está aberta; as demais bloqueadas; nenhuma com check sem dado")
    void projetoNovoSoTemEtapa1() throws Exception {
        Project p = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, 1);
        assertEquals(List.of("ATUAL", "BLOQUEADA", "BLOQUEADA", "BLOQUEADA", "NAO_SE_APLICA"), estados(p));
    }

    @Test
    @DisplayName("Etapa 4 (Comercial) fica disponível após as etapas 1 a 3, para todos os tipos de edificação")
    void comercialDisponivelParaTodosOsTipos() throws Exception {
        for (TipoEdificacao tipo : TipoEdificacao.values()) {
            Project p = projeto(tipo, 4); // o usuário já alcançou a etapa 4
            if (tipo != TipoEdificacao.COMERCIAL) tipologia(p);
            List<String> estados = estados(p);
            assertEquals("ATUAL", estados.get(3), "tipo " + tipo);
            assertNotEquals("BLOQUEADA", estados.get(3), "tipo " + tipo);
            assertEquals("CONCLUIDA", estados.get(0));
            assertEquals(tipo == TipoEdificacao.COMERCIAL ? "NAO_SE_APLICA" : "CONCLUIDA", estados.get(1));
            assertEquals("CONCLUIDA", estados.get(2));
        }
    }

    @Test
    @DisplayName("Sem os dados exigidos, a etapa não conclui e as seguintes ficam bloqueadas (mesmo com etapaAtual alta)")
    void semDadoNaoConclui() throws Exception {
        Project p = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, 5); // sem tipologias gravadas
        List<String> e = estados(p);
        assertEquals("CONCLUIDA", e.get(0));
        assertNotEquals("CONCLUIDA", e.get(1));
        assertEquals("BLOQUEADA", e.get(2));
        assertEquals("BLOQUEADA", e.get(3));
    }

    @Test
    @DisplayName("Registrar a etapa 5 com lista comercial vazia conclui a etapa 4; etapa 5 sem VE nem bomba é NAO_SE_APLICA")
    void comercialVaziaConcluiAoAvancar() throws Exception {
        Project p = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, 4);
        tipologia(p);
        mockMvc.perform(put("/api/v1/projects/" + p.getId() + "/etapa-comercial").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"espacos\":[]}")).andExpect(status().isOk());
        assertEquals("ATUAL", estados(p).get(3));
        mockMvc.perform(put("/api/v1/projects/" + p.getId() + "/etapa-atual").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"etapa\":5}")).andExpect(status().isOk());
        List<String> e = estados(p);
        assertEquals("CONCLUIDA", e.get(3));
        assertEquals("NAO_SE_APLICA", e.get(4));
    }

    @Test
    @DisplayName("Etapa 5 com VE marcado e sem dados fica aberta; com dados e depois da revisão, conclui")
    void etapa5ComVe() throws Exception {
        Project p = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, 5);
        p.setPossuiVe(true);
        projectRepository.save(p);
        tipologia(p);
        assertEquals("ATUAL", estados(p).get(4));
        especiaisRepository.save(new SpecialLoads(p, 6, new BigDecimal("7.4"), BigDecimal.ONE, BigDecimal.ZERO, null, BigDecimal.ZERO, null));
        assertEquals("ATUAL", estados(p).get(4)); // ainda não passou dela
        mockMvc.perform(put("/api/v1/projects/" + p.getId() + "/etapa-atual").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"etapa\":6}")).andExpect(status().isOk());
        assertEquals("CONCLUIDA", estados(p).get(4));
    }

    // ---------------------------------------------------------------- etapa alcançada

    @Test
    @DisplayName("etapa-atual nunca recua, recusa valores inválidos e é do dono do projeto")
    void etapaAtual() throws Exception {
        Project p = projeto(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, 3);
        mockMvc.perform(put("/api/v1/projects/" + p.getId() + "/etapa-atual").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"etapa\":2}")).andExpect(status().isOk());
        assertEquals(3, projectRepository.findById(p.getId()).orElseThrow().getEtapaAtual());
        mockMvc.perform(put("/api/v1/projects/" + p.getId() + "/etapa-atual").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"etapa\":9}")).andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/v1/projects/" + p.getId() + "/etapa-atual").header("Authorization", "Bearer " + tokenOutro)
                .contentType(MediaType.APPLICATION_JSON).content("{\"etapa\":4}")).andExpect(status().isForbidden());
    }
}
