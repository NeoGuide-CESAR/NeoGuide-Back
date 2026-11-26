package neoguide.project.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import neoguide.project.model.User;
import neoguide.project.repository.DemandResultRepository;
import neoguide.project.repository.ProjectRepository;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Meu perfil: GET/PATCH /auth/me e PUT /auth/password. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PerfilControllerTest {

    private static final String SENHA = "SenhaForte123";

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private DemandResultRepository resultadoRepository;
    @Autowired private neoguide.project.repository.CommercialUnitRepository espacoRepository;
    @Autowired private neoguide.project.repository.ResidentialTypologyRepository tipologiaRepository;
    @Autowired private neoguide.project.repository.CommonAreaLoadRepository cargaRepository;
    @Autowired private neoguide.project.repository.SpecialLoadsRepository especiaisRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private final ObjectMapper json = new ObjectMapper();
    private User maria;
    private User joao;
    private String token;

    @BeforeEach
    void setUp() {
        resultadoRepository.deleteAll();
        espacoRepository.deleteAll();
        tipologiaRepository.deleteAll();
        cargaRepository.deleteAll();
        especiaisRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        maria = new User("Maria Souza", "maria@perfil.com", passwordEncoder.encode(SENHA));
        maria.setCpfCnpj("12345678909");
        maria.setTelefone("81999998888");
        maria.setCrea("CREA-PE 123456");
        maria = userRepository.save(maria);
        joao = userRepository.save(new User("Joao Silva", "joao@perfil.com", passwordEncoder.encode(SENHA)));
        token = jwtService.generateToken(maria);
    }

    private JsonNode corpo(MvcResult r) throws Exception {
        return json.readTree(r.getResponse().getContentAsString());
    }

    private MvcResult alterar(String corpo, int esperado) throws Exception {
        return mockMvc.perform(patch("/api/v1/auth/me").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado)).andReturn();
    }

    @Test
    @DisplayName("GET /auth/me devolve os dados do usuário logado, com documento formatado e sem hash")
    void obterPerfil() throws Exception {
        MvcResult r = mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token)).andExpect(status().isOk()).andReturn();
        JsonNode p = corpo(r);
        assertEquals("Maria Souza", p.get("nome").asText());
        assertEquals("123.456.789-09", p.get("cpfCnpj").asText());
        assertEquals("81999998888", p.get("telefone").asText());
        assertEquals("CREA-PE 123456", p.get("registroProfissional").asText());
        assertEquals("PROJETISTA", p.get("perfil").asText());
        assertFalse(r.getResponse().getContentAsString().toLowerCase().contains("password"));
        assertFalse(r.getResponse().getContentAsString().contains("$2"));
    }

    @Test
    @DisplayName("Sem token, /auth/me não é público: 401")
    void semTokenNaoEPublico() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/v1/auth/password").contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH altera nome, telefone e registro; ignora cpfCnpj e perfil enviados")
    void atualizarIgnoraDocumentoEPerfil() throws Exception {
        MvcResult r = alterar("{\"nome\":\"Maria de Souza\",\"telefone\":\"(81) 98888-7777\",\"email\":\"maria@perfil.com\","
                + "\"registroProfissional\":\"CREA-PE 999\",\"cpfCnpj\":\"00000000000\",\"perfil\":\"ANALISTA\"}", 200);
        JsonNode p = corpo(r);
        assertEquals("Maria de Souza", p.get("nome").asText());
        assertEquals("81988887777", p.get("telefone").asText());
        assertTrue(p.get("token").isNull(), "sem troca de e-mail não há novo token");
        User salvo = userRepository.findById(maria.getId()).orElseThrow();
        assertEquals("12345678909", salvo.getCpfCnpj());
        assertEquals("ROLE_PROJETISTA", salvo.getRole().name());
        assertEquals("CREA-PE 999", salvo.getCrea());
    }

    @Test
    @DisplayName("Validações com o campo: nome sem sobrenome, telefone, e-mail e registro obrigatório do projetista")
    void validacoes() throws Exception {
        String ok = "\"email\":\"maria@perfil.com\",\"registroProfissional\":\"CREA-PE 1\"";
        assertEquals("nome", corpo(alterar("{\"nome\":\"Maria\"," + ok + "}", 422)).get("campo").asText());
        assertEquals("telefone", corpo(alterar("{\"nome\":\"Maria Souza\",\"telefone\":\"123\"," + ok + "}", 422)).get("campo").asText());
        assertEquals("email", corpo(alterar("{\"nome\":\"Maria Souza\",\"email\":\"invalido\",\"registroProfissional\":\"x\"}", 422)).get("campo").asText());
        JsonNode registro = corpo(alterar("{\"nome\":\"Maria Souza\",\"email\":\"maria@perfil.com\",\"registroProfissional\":\"\"}", 422));
        assertEquals("registroProfissional", registro.get("campo").asText());
        assertEquals("Informe seu registro profissional.", registro.get("detail").asText());
    }

    @Test
    @DisplayName("Trocar o e-mail exige a senha atual; e-mail em uso é 409; com a senha certa salva e devolve novo token")
    void trocaDeEmail() throws Exception {
        String base = "\"nome\":\"Maria Souza\",\"registroProfissional\":\"CREA-PE 1\"";
        JsonNode semSenha = corpo(alterar("{" + base + ",\"email\":\"nova@perfil.com\"}", 422));
        assertEquals("senhaAtual", semSenha.get("campo").asText());
        assertEquals("senha_incorreta", semSenha.get("regra").asText());
        assertEquals("senha_incorreta", corpo(alterar("{" + base + ",\"email\":\"nova@perfil.com\",\"senhaAtual\":\"errada123\"}", 422)).get("regra").asText());

        JsonNode emUso = corpo(alterar("{" + base + ",\"email\":\"joao@perfil.com\",\"senhaAtual\":\"" + SENHA + "\"}", 409));
        assertEquals("email_em_uso", emUso.get("regra").asText());
        assertEquals("email", emUso.get("campo").asText());

        JsonNode ok = corpo(alterar("{" + base + ",\"email\":\"Nova@Perfil.com\",\"senhaAtual\":\"" + SENHA + "\"}", 200));
        assertEquals("nova@perfil.com", ok.get("email").asText());
        assertFalse(ok.get("token").isNull());
        assertEquals("nova@perfil.com", userRepository.findById(maria.getId()).orElseThrow().getEmail());
    }

    @Test
    @DisplayName("Usuário A não altera B: o id vem do token, não do corpo")
    void naoAlteraOutroUsuario() throws Exception {
        alterar("{\"id\":" + joao.getId() + ",\"nome\":\"Maria Nova\",\"email\":\"maria@perfil.com\",\"registroProfissional\":\"CREA-PE 1\"}", 200);
        assertEquals("Joao Silva", userRepository.findById(joao.getId()).orElseThrow().getName());
        assertEquals("Maria Nova", userRepository.findById(maria.getId()).orElseThrow().getName());
    }

    @Test
    @DisplayName("PUT /auth/password: atual errada, nova fraca, nova igual à atual; sucesso é 204 sem corpo")
    void trocarSenha() throws Exception {
        java.util.function.BiFunction<String, Integer, MvcResult> enviar = (corpo, esperado) -> {
            try {
                return mockMvc.perform(put("/api/v1/auth/password").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado)).andReturn();
            } catch (Exception e) { throw new RuntimeException(e); }
        };
        assertEquals("senhaAtual", corpo(enviar.apply("{\"senhaAtual\":\"errada123\",\"novaSenha\":\"Outra1234\"}", 422)).get("campo").asText());
        JsonNode fraca = corpo(enviar.apply("{\"senhaAtual\":\"" + SENHA + "\",\"novaSenha\":\"curta1\"}", 422));
        assertEquals("novaSenha", fraca.get("campo").asText());
        assertEquals("senha_fraca", fraca.get("regra").asText());
        assertEquals("senha_fraca", corpo(enviar.apply("{\"senhaAtual\":\"" + SENHA + "\",\"novaSenha\":\"somenteletras\"}", 422)).get("regra").asText());
        assertEquals("senha_igual", corpo(enviar.apply("{\"senhaAtual\":\"" + SENHA + "\",\"novaSenha\":\"" + SENHA + "\"}", 422)).get("regra").asText());

        MvcResult ok = enviar.apply("{\"senhaAtual\":\"" + SENHA + "\",\"novaSenha\":\"Outra1234\"}", 204);
        assertEquals("", ok.getResponse().getContentAsString());
        assertTrue(passwordEncoder.matches("Outra1234", userRepository.findById(maria.getId()).orElseThrow().getPassword()));
    }
}
