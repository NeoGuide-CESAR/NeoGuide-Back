package neoguide.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import neoguide.project.dto.CreateProjectRequest;
import neoguide.project.model.Project;
import neoguide.project.model.ProjectStatus;
import neoguide.project.model.ProjectType;
import neoguide.project.model.User;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User dono;
    private User outroUsuario;
    private String tokenDono;
    private String tokenOutro;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
        userRepository.deleteAll();

        dono = userRepository.save(new User("Projetista Dono", "dono@empresa.com", passwordEncoder.encode("SenhaForte@123")));
        outroUsuario = userRepository.save(new User("Outro Projetista", "outro@empresa.com", passwordEncoder.encode("SenhaForte@123")));

        tokenDono = jwtService.generateToken(dono);
        tokenOutro = jwtService.generateToken(outroUsuario);
    }

    @Test
    @DisplayName("POST /api/v1/projects autenticado com dados válidos deve retornar 201 Created com protocolo gerado")
    void shouldCreateProjectSuccessfully() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setTitulo("Condomínio Residencial Jardins");
        request.setDescricao("Edificação multifamiliar de 8 pavimentos");
        request.setTipo("LIGACAO_NOVA");
        request.setTensaoFornecimento("380_220V");
        request.setPossuiComercial(true);
        request.setPossuiVe(true);
        request.setPossuiIncendio(false);
        request.setResponsavel("Projetista Dono");
        request.setCrea("CREA-12345");
        request.setMunicipio("Recife");
        request.setCep("50000-000");
        request.setEndereco("Rua das Flores, 123");
        request.setBairro("Boa Viagem");

        mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.protocolo", matchesPattern("^PRJ-\\d{4}-\\d{4}$")))
                .andExpect(jsonPath("$.titulo", is("Condomínio Residencial Jardins")))
                .andExpect(jsonPath("$.status", is("RASCUNHO")))
                .andExpect(jsonPath("$.etapaAtual", is(1)))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/projects com campos booleanos opcionais omitidos deve deserializar corretamente com default false e retornar 201")
    void shouldCreateProjectSuccessfullyWhenOptionalBooleanFieldsAreOmitted() throws Exception {
        String jsonPayload = """
                {
                  "titulo": "Projeto X",
                  "municipio": "Recife",
                  "tensaoFornecimento": "380_220V"
                }
                """;

        mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.protocolo", matchesPattern("^PRJ-\\d{4}-\\d{4}$")))
                .andExpect(jsonPath("$.titulo", is("Projeto X")))
                .andExpect(jsonPath("$.status", is("RASCUNHO")));
    }

    @Test
    @DisplayName("POST /api/v1/projects sem token de autorização deve retornar 401 Unauthorized")
    void shouldReturn401WhenCreatingWithoutToken() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setTitulo("Projeto Sem Token");
        request.setTensaoFornecimento("380_220V");
        request.setMunicipio("Recife");

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/projects com validação inválida deve retornar 400 Bad Request")
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setTitulo("AB"); // Menor que 3 caracteres
        request.setTensaoFornecimento("VOLTAGEM_INVALIDA");
        // Municipio ausente

        mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors.titulo", notNullValue()))
                .andExpect(jsonPath("$.errors.tensaoFornecimento", notNullValue()))
                .andExpect(jsonPath("$.errors.municipio", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/v1/projects autenticado deve retornar 200 OK com paginação e apenas projetos do usuário logado")
    void shouldListOnlyAuthenticatedUserProjects() throws Exception {
        Project projDono = new Project();
        projDono.setProtocolo("PRJ-2026-0001");
        projDono.setTitulo("Projeto do Dono");
        projDono.setUser(dono);
        projDono.setTensaoFornecimento("380_220V");
        projDono.setMunicipio("Recife");
        projDono.setCreatedAt(LocalDateTime.now());
        projectRepository.save(projDono);

        Project projOutro = new Project();
        projOutro.setProtocolo("PRJ-2026-0002");
        projOutro.setTitulo("Projeto de Outro Usuário");
        projOutro.setUser(outroUsuario);
        projOutro.setTensaoFornecimento("220_127V");
        projOutro.setMunicipio("Olinda");
        projOutro.setCreatedAt(LocalDateTime.now());
        projectRepository.save(projOutro);

        mockMvc.perform(get("/api/v1/projects")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].protocolo", is("PRJ-2026-0001")))
                .andExpect(jsonPath("$.content[0].titulo", is("Projeto do Dono")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("GET /api/v1/projects com paginação e ordenação deve respeitar size=2 e sort=createdAt,desc")
    void shouldListProjectsWithPaginationSizeAndSorting() throws Exception {
        LocalDateTime baseTime = LocalDateTime.now();

        Project p1 = new Project();
        p1.setProtocolo("PRJ-2026-0001");
        p1.setTitulo("Projeto Alfa");
        p1.setUser(dono);
        p1.setTensaoFornecimento("380_220V");
        p1.setMunicipio("Recife");
        p1.setCreatedAt(baseTime.minusHours(3));
        projectRepository.save(p1);

        Project p2 = new Project();
        p2.setProtocolo("PRJ-2026-0002");
        p2.setTitulo("Projeto Beta");
        p2.setUser(dono);
        p2.setTensaoFornecimento("380_220V");
        p2.setMunicipio("Recife");
        p2.setCreatedAt(baseTime.minusHours(2));
        projectRepository.save(p2);

        Project p3 = new Project();
        p3.setProtocolo("PRJ-2026-0003");
        p3.setTitulo("Projeto Gama");
        p3.setUser(dono);
        p3.setTensaoFornecimento("380_220V");
        p3.setMunicipio("Recife");
        p3.setCreatedAt(baseTime.minusHours(1));
        projectRepository.save(p3);

        mockMvc.perform(get("/api/v1/projects?page=0&size=2&sort=createdAt,desc")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].titulo", is("Projeto Gama")))
                .andExpect(jsonPath("$.content[1].titulo", is("Projeto Beta")))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(2)));
    }

    @Test
    @DisplayName("GET /api/v1/projects com filtro de status deve retornar apenas projetos correspondentes")
    void shouldFilterProjectsByStatus() throws Exception {
        Project rascunho = new Project();
        rascunho.setProtocolo("PRJ-2026-0001");
        rascunho.setTitulo("Projeto Rascunho");
        rascunho.setUser(dono);
        rascunho.setStatus(ProjectStatus.RASCUNHO);
        rascunho.setTensaoFornecimento("380_220V");
        rascunho.setMunicipio("Recife");
        projectRepository.save(rascunho);

        Project aprovado = new Project();
        aprovado.setProtocolo("PRJ-2026-0002");
        aprovado.setTitulo("Projeto Aprovado");
        aprovado.setUser(dono);
        aprovado.setStatus(ProjectStatus.APROVADO);
        aprovado.setTensaoFornecimento("380_220V");
        aprovado.setMunicipio("Recife");
        projectRepository.save(aprovado);

        mockMvc.perform(get("/api/v1/projects?status=RASCUNHO")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].titulo", is("Projeto Rascunho")))
                .andExpect(jsonPath("$.content[0].status", is("RASCUNHO")));
    }

    @Test
    @DisplayName("GET /api/v1/projects com termo search deve buscar por título, protocolo ou município")
    void shouldFilterProjectsBySearchTerm() throws Exception {
        Project p1 = new Project();
        p1.setProtocolo("PRJ-2026-0100");
        p1.setTitulo("Edifício Solar Central");
        p1.setUser(dono);
        p1.setTensaoFornecimento("380_220V");
        p1.setMunicipio("Recife");
        projectRepository.save(p1);

        Project p2 = new Project();
        p2.setProtocolo("PRJ-2026-0200");
        p2.setTitulo("Galpão Logístico");
        p2.setUser(dono);
        p2.setTensaoFornecimento("380_220V");
        p2.setMunicipio("Caruaru");
        projectRepository.save(p2);

        // Busca por título parcial minúsculo
        mockMvc.perform(get("/api/v1/projects?search=solar")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].titulo", is("Edifício Solar Central")));

        // Busca por município
        mockMvc.perform(get("/api/v1/projects?search=Caruaru")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].titulo", is("Galpão Logístico")));

        // Busca por protocolo
        mockMvc.perform(get("/api/v1/projects?search=0100")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].protocolo", is("PRJ-2026-0100")));
    }

    @Test
    @DisplayName("POST /api/v1/projects/{id}/duplicate autenticado deve retornar 201 Created com projeto clonado")
    void shouldDuplicateProjectSuccessfully() throws Exception {
        Project original = new Project();
        original.setProtocolo("PRJ-2026-0001");
        original.setTitulo("Condomínio Jardins");
        original.setDescricao("Memorial descritivo elétrico");
        original.setUser(dono);
        original.setStatus(ProjectStatus.APROVADO);
        original.setEtapaAtual(3);
        original.setTensaoFornecimento("380_220V");
        original.setPossuiComercial(true);
        original.setPossuiVe(true);
        original.setPossuiIncendio(false);
        original.setMunicipio("Recife");
        original.setCep("50000-000");
        original.setEndereco("Av. Boa Viagem, 1000");
        original.setCrea("CREA-PE-1234");
        original.setCreatedAt(LocalDateTime.now().minusDays(2));
        Project savedOriginal = projectRepository.save(original);

        mockMvc.perform(post("/api/v1/projects/" + savedOriginal.getId() + "/duplicate")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id", not(is(savedOriginal.getId().intValue()))))
                .andExpect(jsonPath("$.titulo", is("Condomínio Jardins (cópia)")))
                .andExpect(jsonPath("$.protocolo", matchesPattern("^PRJ-\\d{4}-\\d{4}$")))
                .andExpect(jsonPath("$.status", is("RASCUNHO")))
                .andExpect(jsonPath("$.etapaAtual", is(1)))
                .andExpect(jsonPath("$.tensaoFornecimento", is("380_220V")))
                .andExpect(jsonPath("$.possuiComercial", is(true)))
                .andExpect(jsonPath("$.possuiVe", is(true)))
                .andExpect(jsonPath("$.municipio", is("Recife")));

        assertEquals(2, projectRepository.findAllByUserIdOrderByCreatedAtDesc(dono.getId()).size());
    }

    @Test
    @DisplayName("POST /api/v1/projects/{id}/duplicate por outro usuário deve retornar 403 Forbidden")
    void shouldReturn403WhenOtherUserTriesToDuplicateProject() throws Exception {
        Project projDono = new Project();
        projDono.setProtocolo("PRJ-2026-0010");
        projDono.setTitulo("Projeto Exclusivo do Dono");
        projDono.setUser(dono);
        projDono.setTensaoFornecimento("380_220V");
        projDono.setMunicipio("Recife");
        Project saved = projectRepository.save(projDono);

        mockMvc.perform(post("/api/v1/projects/" + saved.getId() + "/duplicate")
                        .header("Authorization", "Bearer " + tokenOutro))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.message", containsString("Acesso negado")));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{id} pelo criador deve retornar 200 OK com os detalhes do projeto")
    void shouldReturnProjectDetailsWhenUserIsOwner() throws Exception {
        Project proj = new Project();
        proj.setProtocolo("PRJ-2026-0010");
        proj.setTitulo("Projeto Residencial Dono");
        proj.setUser(dono);
        proj.setTensaoFornecimento("380_220V");
        proj.setMunicipio("Jaboatão");
        proj.setCep("54000-000");
        proj.setPossuiComercial(true);
        proj.setCreatedAt(LocalDateTime.now());
        Project saved = projectRepository.save(proj);

        mockMvc.perform(get("/api/v1/projects/" + saved.getId())
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.protocolo", is("PRJ-2026-0010")))
                .andExpect(jsonPath("$.tensaoFornecimento", is("380_220V")))
                .andExpect(jsonPath("$.possuiComercial", is(true)))
                .andExpect(jsonPath("$.municipio", is("Jaboatão")));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{id} por outro usuário deve retornar 403 Forbidden")
    void shouldReturn403WhenOtherUserTriesToAccessProject() throws Exception {
        Project proj = new Project();
        proj.setProtocolo("PRJ-2026-0020");
        proj.setTitulo("Projeto Protegido do Dono");
        proj.setUser(dono);
        proj.setTensaoFornecimento("380_220V");
        proj.setMunicipio("Recife");
        proj.setCreatedAt(LocalDateTime.now());
        Project saved = projectRepository.save(proj);

        mockMvc.perform(get("/api/v1/projects/" + saved.getId())
                        .header("Authorization", "Bearer " + tokenOutro))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.message", containsString("Acesso negado")));
    }

    @Test
    @DisplayName("Rotas legadas /projects e /projects/{id} devem funcionar com compatibilidade")
    void shouldSupportLegacyProjectEndpoints() throws Exception {
        Project proj = new Project();
        proj.setProtocolo("PRJ-2026-0030");
        proj.setTitulo("Projeto Rota Legada");
        proj.setUser(dono);
        proj.setTensaoFornecimento("220_127V");
        proj.setMunicipio("Caruaru");
        proj.setCreatedAt(LocalDateTime.now());
        Project saved = projectRepository.save(proj);

        mockMvc.perform(get("/projects")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        mockMvc.perform(get("/projects/" + saved.getId())
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.protocolo", is("PRJ-2026-0030")));
    }
}
