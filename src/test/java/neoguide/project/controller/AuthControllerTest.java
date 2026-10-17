package neoguide.project.controller;

import com.jayway.jsonpath.JsonPath;
import neoguide.project.model.User;
import neoguide.project.model.UserRole;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
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
                .andExpect(jsonPath("$.email", is("carlos@empresa.com")))
                .andExpect(jsonPath("$.role", is("ROLE_PROJETISTA")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register deve cadastrar usuário com CPF e role ANALISTA")
    void shouldRegisterUserWithCpfAndRoleSuccessfully() throws Exception {
        String body = """
                {
                    "name": "Mariana Analista",
                    "email": "mariana@concessionaria.com",
                    "cpfCnpj": "123.456.789-01",
                    "role": "ROLE_ANALISTA",
                    "password": "SenhaForte@123",
                    "confirmPassword": "SenhaForte@123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Mariana Analista")))
                .andExpect(jsonPath("$.email", is("mariana@concessionaria.com")))
                .andExpect(jsonPath("$.cpfCnpj", is("12345678901")))
                .andExpect(jsonPath("$.role", is("ROLE_ANALISTA")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register deve retornar 409 Conflict ao registrar CPF/CNPJ duplicado")
    void shouldReturn409ConflictWhenRegisteringDuplicateCpf() throws Exception {
        User existingUser = new User("Existente", "existente@empresa.com", passwordEncoder.encode("SenhaForte@123"), "12345678901", UserRole.ROLE_PROJETISTA);
        userRepository.save(existingUser);

        String body = """
                {
                    "name": "Novo Usuário",
                    "email": "novo@empresa.com",
                    "cpfCnpj": "123.456.789-01",
                    "password": "SenhaForte@123",
                    "confirmPassword": "SenhaForte@123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", is("CPF/CNPJ já cadastrado")));
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
                .andExpect(jsonPath("$.role", is("ROLE_PROJETISTA")))
                .andExpect(jsonPath("$.usuario.name", is("Ana Projetista")))
                .andExpect(jsonPath("$.usuario.email", is("ana@empresa.com")))
                .andExpect(jsonPath("$.usuario.role", is("ROLE_PROJETISTA")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login flexível via campo 'login' como e-mail")
    void shouldLoginSuccessfullyWithLoginFieldAsEmail() throws Exception {
        User user = new User("Bruno Projetista", "bruno@empresa.com", passwordEncoder.encode("SenhaForte@123"));
        userRepository.save(user);

        String loginBody = """
                {
                    "login": "bruno@empresa.com",
                    "password": "SenhaForte@123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.usuario.email", is("bruno@empresa.com")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login flexível via CPF sem máscara e com validação da claim role no token")
    void shouldLoginSuccessfullyWithCpfWithoutMask() throws Exception {
        User user = new User("Clara Analista", "clara@concessionaria.com", passwordEncoder.encode("SenhaForte@123"), "98765432100", UserRole.ROLE_ANALISTA);
        userRepository.save(user);

        String loginBody = """
                {
                    "login": "98765432100",
                    "password": "SenhaForte@123"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.role", is("ROLE_ANALISTA")))
                .andExpect(jsonPath("$.usuario.cpfCnpj", is("98765432100")))
                .andExpect(jsonPath("$.usuario.role", is("ROLE_ANALISTA")))
                .andReturn();

        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
        assertEquals("ROLE_ANALISTA", jwtService.extractRole(token));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login flexível via CPF com máscara")
    void shouldLoginSuccessfullyWithCpfWithMask() throws Exception {
        User user = new User("Clara Analista", "clara@concessionaria.com", passwordEncoder.encode("SenhaForte@123"), "98765432100", UserRole.ROLE_ANALISTA);
        userRepository.save(user);

        String loginBody = """
                {
                    "login": "987.654.321-00",
                    "password": "SenhaForte@123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.usuario.email", is("clara@concessionaria.com")));
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
