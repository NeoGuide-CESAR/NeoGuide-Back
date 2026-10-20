package neoguide.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import neoguide.project.dto.CreateProjectRequest;
import neoguide.project.dto.ResidentialStageRequest;
import neoguide.project.dto.ResidentialTypologyItemRequest;
import neoguide.project.model.Project;
import neoguide.project.model.ProjectStatus;
import neoguide.project.model.ProjectType;
import neoguide.project.model.ResidentialTypology;
import neoguide.project.model.User;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    private ResidentialTypologyRepository residentialTypologyRepository;

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
        residentialTypologyRepository.deleteAll();
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
    @DisplayName("GET /api/v1/projects autenticado deve retornar 200 OK apenas com os projetos do usuário logado")
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
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].protocolo", is("PRJ-2026-0001")))
                .andExpect(jsonPath("$[0].titulo", is("Projeto do Dono")));
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
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/projects/" + saved.getId())
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.protocolo", is("PRJ-2026-0030")));
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id}/etapa-residencial com dados válidos deve retornar 200 OK e resposta consolidada enxuta")
    void shouldSaveResidentialStageSuccessfully() throws Exception {
        Project proj = new Project();
        proj.setProtocolo("PRJ-2026-0040");
        proj.setTitulo("Edifício Solar dos Pinheiros");
        proj.setUser(dono);
        proj.setTensaoFornecimento("380_220V");
        proj.setMunicipio("Recife");
        proj.setEtapaAtual(1);
        proj.setCreatedAt(LocalDateTime.now());
        Project saved = projectRepository.save(proj);

        ResidentialStageRequest request = new ResidentialStageRequest(List.of(
                new ResidentialTypologyItemRequest("Tipo A (2Q)", 6L, new BigDecimal("65.00"), 50, false),
                new ResidentialTypologyItemRequest("Tipo B (3Q)", 8L, new BigDecimal("78.00"), 50, false)
        ));

        mockMvc.perform(put("/api/v1/projects/" + saved.getId() + "/etapa-residencial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso", is(true)))
                .andExpect(jsonPath("$.projectId", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.demandaResidencialFinalDrfKva", is(122.72)));

        List<ResidentialTypology> typologies = residentialTypologyRepository.findByProjectIdOrderByIdAsc(saved.getId());
        assertEquals(2, typologies.size());
        assertEquals("Tipo A (2Q)", typologies.get(0).getNome());
        assertEquals(new BigDecimal("1.47"), typologies.get(0).getDemandaUnitariaKva());
        assertEquals(new BigDecimal("73.50"), typologies.get(0).getDemandaTotalTipologiaKva());

        assertEquals("Tipo B (3Q)", typologies.get(1).getNome());
        assertEquals(new BigDecimal("1.80"), typologies.get(1).getDemandaUnitariaKva());
        assertEquals(new BigDecimal("90.00"), typologies.get(1).getDemandaTotalTipologiaKva());

        Project updated = projectRepository.findById(saved.getId()).orElseThrow();
        assertEquals(2, updated.getEtapaAtual());
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id}/etapa-residencial com contrato enxuto sem nome deve salvar e retornar 200 OK")
    void shouldSaveResidentialStageWithLeanContractWithoutNome() throws Exception {
        Project proj = new Project();
        proj.setProtocolo("PRJ-2026-0045");
        proj.setTitulo("Edifício Contrato Enxuto");
        proj.setUser(dono);
        proj.setTensaoFornecimento("380_220V");
        proj.setMunicipio("Recife");
        proj.setEtapaAtual(1);
        proj.setCreatedAt(LocalDateTime.now());
        Project saved = projectRepository.save(proj);

        String jsonPayload = """
                {
                  "tipologias": [
                    {
                      "areaUtilM2": 65.00,
                      "quantidadeUnidades": 50,
                      "isSmartStudio": false
                    },
                    {
                      "areaUtilM2": 78.00,
                      "quantidadeUnidades": 50,
                      "isSmartStudio": false
                    }
                  ]
                }
                """;

        mockMvc.perform(put("/api/v1/projects/" + saved.getId() + "/etapa-residencial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso", is(true)))
                .andExpect(jsonPath("$.projectId", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.demandaResidencialFinalDrfKva", is(122.72)));

        List<ResidentialTypology> typologies = residentialTypologyRepository.findByProjectIdOrderByIdAsc(saved.getId());
        assertEquals(2, typologies.size());
        assertEquals("Tipologia 65.00m²", typologies.get(0).getNome());
        assertEquals("Tipologia 78.00m²", typologies.get(1).getNome());
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id}/etapa-residencial sucessivo deve ser idempotente e sobrescrever tipologias")
    void shouldBeIdempotentOnConsecutiveCalls() throws Exception {
        Project proj = new Project();
        proj.setProtocolo("PRJ-2026-0041");
        proj.setTitulo("Edifício Idempotente");
        proj.setUser(dono);
        proj.setTensaoFornecimento("380_220V");
        proj.setMunicipio("Recife");
        proj.setEtapaAtual(1);
        proj.setCreatedAt(LocalDateTime.now());
        Project saved = projectRepository.save(proj);

        ResidentialStageRequest firstRequest = new ResidentialStageRequest(List.of(
                new ResidentialTypologyItemRequest("Tipo A", null, new BigDecimal("65.00"), 50, false),
                new ResidentialTypologyItemRequest("Tipo B", null, new BigDecimal("78.00"), 50, false)
        ));

        mockMvc.perform(put("/api/v1/projects/" + saved.getId() + "/etapa-residencial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso", is(true)))
                .andExpect(jsonPath("$.demandaResidencialFinalDrfKva", is(122.72)));

        assertEquals(2, residentialTypologyRepository.findByProjectIdOrderByIdAsc(saved.getId()).size());

        // Segunda chamada com nova composição (ex: apenas 1 studio)
        ResidentialStageRequest secondRequest = new ResidentialStageRequest(List.of(
                new ResidentialTypologyItemRequest("Studio Compacto", null, new BigDecimal("30.00"), 15, true)
        ));

        mockMvc.perform(put("/api/v1/projects/" + saved.getId() + "/etapa-residencial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso", is(true)))
                .andExpect(jsonPath("$.demandaResidencialFinalDrfKva", notNullValue()));

        List<ResidentialTypology> typologiesAfter = residentialTypologyRepository.findByProjectIdOrderByIdAsc(saved.getId());
        assertEquals(1, typologiesAfter.size());
        assertEquals("Studio Compacto", typologiesAfter.get(0).getNome());
        assertEquals(15, typologiesAfter.get(0).getQuantidadeUnidades());
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id}/etapa-residencial com payload inválido deve retornar 400 Bad Request RFC 7807")
    void shouldReturn400WhenPayloadIsInvalid() throws Exception {
        Project proj = new Project();
        proj.setProtocolo("PRJ-2026-0042");
        proj.setTitulo("Projeto Teste Erro");
        proj.setUser(dono);
        proj.setTensaoFornecimento("380_220V");
        proj.setMunicipio("Recife");
        proj.setCreatedAt(LocalDateTime.now());
        Project saved = projectRepository.save(proj);

        // Lista de tipologias vazia
        ResidentialStageRequest reqVazia = new ResidentialStageRequest(List.of());
        mockMvc.perform(put("/api/v1/projects/" + saved.getId() + "/etapa-residencial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqVazia)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors.tipologias", notNullValue()));

        // Item com área negativa e quantidade zero
        ResidentialStageRequest reqCamposInvalidos = new ResidentialStageRequest(List.of(
                new ResidentialTypologyItemRequest("Tipo Ruim", null, new BigDecimal("-10.00"), 0, false)
        ));
        mockMvc.perform(put("/api/v1/projects/" + saved.getId() + "/etapa-residencial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqCamposInvalidos)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors['tipologias[0].areaUtilM2']", notNullValue()))
                .andExpect(jsonPath("$.errors['tipologias[0].quantidadeUnidades']", notNullValue()));
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id}/etapa-residencial por outro projetista deve retornar 403 Forbidden")
    void shouldReturn403WhenOtherUserAttemptsToSaveResidentialStage() throws Exception {
        Project proj = new Project();
        proj.setProtocolo("PRJ-2026-0043");
        proj.setTitulo("Projeto Dono Exclusivo");
        proj.setUser(dono);
        proj.setTensaoFornecimento("380_220V");
        proj.setMunicipio("Recife");
        proj.setCreatedAt(LocalDateTime.now());
        Project saved = projectRepository.save(proj);

        ResidentialStageRequest request = new ResidentialStageRequest(List.of(
                new ResidentialTypologyItemRequest("Tipo A", null, new BigDecimal("65.00"), 20, false)
        ));

        mockMvc.perform(put("/api/v1/projects/" + saved.getId() + "/etapa-residencial")
                        .header("Authorization", "Bearer " + tokenOutro)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.message", containsString("Acesso negado")));
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id}/etapa-residencial sem token deve retornar 401 Unauthorized")
    void shouldReturn401WhenNoTokenProvided() throws Exception {
        ResidentialStageRequest request = new ResidentialStageRequest(List.of(
                new ResidentialTypologyItemRequest("Tipo A", null, new BigDecimal("65.00"), 20, false)
        ));

        mockMvc.perform(put("/api/v1/projects/1/etapa-residencial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id}/etapa-residencial com ID inexistente deve retornar 404 Not Found")
    void shouldReturn404WhenProjectDoesNotExist() throws Exception {
        ResidentialStageRequest request = new ResidentialStageRequest(List.of(
                new ResidentialTypologyItemRequest("Tipo A", null, new BigDecimal("65.00"), 20, false)
        ));

        mockMvc.perform(put("/api/v1/projects/99999/etapa-residencial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("PUT /projects/{id}/etapa-residencial rota legada deve funcionar com 200 OK")
    void shouldSupportLegacyEtapaResidencialEndpoint() throws Exception {
        Project proj = new Project();
        proj.setProtocolo("PRJ-2026-0044");
        proj.setTitulo("Projeto Rota Legada Etapa 2");
        proj.setUser(dono);
        proj.setTensaoFornecimento("380_220V");
        proj.setMunicipio("Recife");
        proj.setCreatedAt(LocalDateTime.now());
        Project saved = projectRepository.save(proj);

        ResidentialStageRequest request = new ResidentialStageRequest(List.of(
                new ResidentialTypologyItemRequest("Tipo A", null, new BigDecimal("65.00"), 10, false)
        ));

        mockMvc.perform(put("/projects/" + saved.getId() + "/etapa-residencial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso", is(true)))
                .andExpect(jsonPath("$.demandaResidencialFinalDrfKva", notNullValue()));
    }
}
