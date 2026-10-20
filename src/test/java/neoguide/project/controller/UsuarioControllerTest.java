package neoguide.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import neoguide.project.dto.AlterarSenhaDTO;
import neoguide.project.dto.UsuarioUpdateDTO;
import neoguide.project.model.User;
import neoguide.project.model.UserRole;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.ResidentialTypologyRepository;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ResidentialTypologyRepository residentialTypologyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User usuario;
    private String token;

    @BeforeEach
    void setUp() {
        residentialTypologyRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        usuario = new User(
                "Engenheiro Teste",
                "eng@teste.com",
                passwordEncoder.encode("SenhaAntiga123"),
                "11122233344",
                UserRole.ROLE_PROJETISTA,
                "81987654321",
                "NeoGuide Engenharia",
                "CREA-PE 54321"
        );
        usuario = userRepository.save(usuario);
        token = jwtService.generateToken(usuario);
    }

    @Test
    @DisplayName("GET /api/v1/usuarios/me autenticado retorna 200 com perfil do usuário")
    void getPerfil_sucesso() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/me")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(usuario.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Engenheiro Teste")))
                .andExpect(jsonPath("$.email", is("eng@teste.com")))
                .andExpect(jsonPath("$.telefone", is("81987654321")))
                .andExpect(jsonPath("$.empresa", is("NeoGuide Engenharia")))
                .andExpect(jsonPath("$.crea", is("CREA-PE 54321")))
                .andExpect(jsonPath("$.role", is("ROLE_PROJETISTA")));
    }

    @Test
    @DisplayName("GET /api/v1/usuarios/me sem token retorna 401 Unauthorized")
    void getPerfil_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /api/v1/usuarios/me autenticado atualiza dados cadastrais e retorna 200")
    void atualizarPerfil_sucesso() throws Exception {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO("Engenheiro Atualizado", "81999998888", "Nova Firma SA", "CREA-PE 99887");

        mockMvc.perform(patch("/api/v1/usuarios/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Engenheiro Atualizado")))
                .andExpect(jsonPath("$.telefone", is("81999998888")))
                .andExpect(jsonPath("$.empresa", is("Nova Firma SA")))
                .andExpect(jsonPath("$.crea", is("CREA-PE 99887")));
    }

    @Test
    @DisplayName("PATCH /api/v1/usuarios/me com nome menor que 3 caracteres retorna 400 Bad Request")
    void atualizarPerfil_nomeCurto_retorna400() throws Exception {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO("Ab", null, null, null);

        mockMvc.perform(patch("/api/v1/usuarios/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @DisplayName("PUT /api/v1/usuarios/me/senha com credenciais válidas retorna 204 No Content")
    void alterarSenha_sucesso() throws Exception {
        AlterarSenhaDTO dto = new AlterarSenhaDTO("SenhaAntiga123", "NovaSenhaSuper123", "NovaSenhaSuper123");

        mockMvc.perform(put("/api/v1/usuarios/me/senha")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /api/v1/usuarios/me/senha com senha atual incorreta retorna 400 Bad Request")
    void alterarSenha_senhaAtualIncorreta_retorna400() throws Exception {
        AlterarSenhaDTO dto = new AlterarSenhaDTO("SenhaErrada123", "NovaSenhaSuper123", "NovaSenhaSuper123");

        mockMvc.perform(put("/api/v1/usuarios/me/senha")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.senhaAtual").exists());
    }

    @Test
    @DisplayName("PUT /api/v1/usuarios/me/senha com confirmação divergente retorna 400 Bad Request")
    void alterarSenha_senhasDivergentes_retorna400() throws Exception {
        AlterarSenhaDTO dto = new AlterarSenhaDTO("SenhaAntiga123", "NovaSenhaSuper123", "OutraSenha123");

        mockMvc.perform(put("/api/v1/usuarios/me/senha")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.confirmacaoNovaSenha").exists());
    }
}
