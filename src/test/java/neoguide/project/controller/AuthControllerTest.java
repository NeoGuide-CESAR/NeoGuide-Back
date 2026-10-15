package neoguide.project.controller;

import com.jayway.jsonpath.JsonPath;
import neoguide.project.model.User;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.UserRepository;
import neoguide.project.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void cleanUp() {
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register deve cadastrar usuário com sucesso (201 Created)")
    void shouldRegisterUserSuccessfully() throws Exception {
        String body = """
                {
                    "name": "Carlos Projetista",
                    "email": "carlos@empresa.com",
                    "password": "SenhaForte@123",
                    "confirmPassword": "SenhaForte@123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Carlos Projetista")))
                .andExpect(jsonPath("$.email", is("carlos@empresa.com")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login com credenciais corretas deve retornar 200 OK e LoginResponse com JWT")
    void shouldLoginSuccessfullyAndReturnToken() throws Exception {
        User user = new User("Ana Projetista", "ana@empresa.com", passwordEncoder.encode("SenhaForte@123"));
        userRepository.save(user);

        String loginBody = """
                {
                    "email": "ana@empresa.com",
                    "password": "SenhaForte@123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tipo", is("Bearer")))
                .andExpect(jsonPath("$.usuario.name", is("Ana Projetista")))
                .andExpect(jsonPath("$.usuario.email", is("ana@empresa.com")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login com senha errada deve retornar 401 Unauthorized")
    void shouldReturn401WhenPasswordIsIncorrect() throws Exception {
        User user = new User("Ana Projetista", "ana@empresa.com", passwordEncoder.encode("SenhaForte@123"));
        userRepository.save(user);

        String loginBody = """
                {
                    "email": "ana@empresa.com",
                    "password": "SenhaIncorreta@999"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("E-mail ou senha inválidos")));
    }

    @Test
    @DisplayName("Acesso a rota protegida sem token deve retornar 401 Unauthorized com ErrorResponse padronizado")
    void shouldReturn401OnProtectedRouteWithoutToken() throws Exception {
        mockMvc.perform(get("/test"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Acesso não autorizado. Token ausente ou inválido.")));
    }

    @Test
    @DisplayName("Acesso a rota protegida com Bearer token válido deve retornar 200 OK")
    void shouldAllowAccessToProtectedRouteWithValidToken() throws Exception {
        User user = new User("Lucas Engenheiro", "lucas@empresa.com", passwordEncoder.encode("SenhaForte@123"));
        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);

        mockMvc.perform(get("/test")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /auth/login e /auth/register devem manter compatibilidade com rota legada")
    void shouldSupportLegacyAuthRoutes() throws Exception {
        String body = """
                {
                    "name": "Beatriz Projetista",
                    "email": "beatriz@empresa.com",
                    "password": "SenhaForte@123",
                    "confirmPassword": "SenhaForte@123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("beatriz@empresa.com")));

        String loginBody = """
                {
                    "email": "beatriz@empresa.com",
                    "password": "SenhaForte@123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }
}
