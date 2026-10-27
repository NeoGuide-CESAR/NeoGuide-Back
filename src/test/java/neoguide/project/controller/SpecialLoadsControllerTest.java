package neoguide.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import neoguide.project.dto.SpecialLoadsStageRequest;
import neoguide.project.model.Project;
import neoguide.project.model.User;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.SpecialLoadsRepository;
import neoguide.project.repository.UserRepository;
import neoguide.project.security.jwt.JwtService;
import org.junit.jupiter.api.AfterEach;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SpecialLoadsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SpecialLoadsRepository specialLoadsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User dono;
    private User outroUsuario;
    private String tokenDono;
    private String tokenOutro;
    private Project projetoDono;

    @BeforeEach
    void setUp() {
        specialLoadsRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        dono = userRepository.save(new User("Projetista Dono", "dono@special.com", passwordEncoder.encode("SenhaForte@123")));
        outroUsuario = userRepository.save(new User("Outro Projetista", "outro@special.com", passwordEncoder.encode("SenhaForte@123")));

        tokenDono = jwtService.generateToken(dono);
        tokenOutro = jwtService.generateToken(outroUsuario);

        projetoDono = new Project();
        projetoDono.setUser(dono);
        projetoDono.setProtocolo("PRJ-2026-0002");
        projetoDono.setTitulo("Condomínio Eletroposto");
        projetoDono.setMunicipio("Recife");
        projetoDono.setTensaoFornecimento("380_220V");
        projetoDono.setEtapaAtual(4);
        projetoDono = projectRepository.save(projetoDono);
    }


    @AfterEach
    void tearDown() {
        specialLoadsRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id}/etapa-cargas-especiais com dados válidos deve persistir e retornar 200 OK")
    void saveSpecialLoadsStage_comSucesso_retorna200() throws Exception {
        SpecialLoadsStageRequest request = new SpecialLoadsStageRequest(
                4, new BigDecimal("7.40"), new BigDecimal("15.00"), new BigDecimal("10.00")
        );

        mockMvc.perform(put("/api/v1/projects/" + projetoDono.getId() + "/etapa-cargas-especiais")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.projectId").value(projetoDono.getId()))
                .andExpect(jsonPath("$.etapaAtual").value(5))
                .andExpect(jsonPath("$.fatorSimultaneidadeVe").value(0.70))
                .andExpect(jsonPath("$.demandaVeDveKva").value(20.72))
                .andExpect(jsonPath("$.demandaIncendioDmbiKva").value(14.40))
                .andExpect(jsonPath("$.demandaEspecialTotalKva").value(35.12));

        assertEquals(1, specialLoadsRepository.count());
    }

    @Test
    @DisplayName("PUT /projects/{id}/etapa-cargas-especiais alias deve retornar 200 OK")
    void saveSpecialLoadsStage_alias_retorna200() throws Exception {
        SpecialLoadsStageRequest request = new SpecialLoadsStageRequest(
                2, new BigDecimal("7.40"), new BigDecimal("10.00"), null
        );

        mockMvc.perform(put("/projects/" + projetoDono.getId() + "/etapa-cargas-especiais")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.demandaVeDveKva").value(12.58))
                .andExpect(jsonPath("$.demandaIncendioDmbiKva").value(9.95));
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id}/etapa-cargas-especiais sem cargas especiais deve retornar 200 OK com zeros")
    void saveSpecialLoadsStage_semCargas_retorna200ComZero() throws Exception {
        SpecialLoadsStageRequest request = new SpecialLoadsStageRequest(0, null, null, null);

        mockMvc.perform(put("/api/v1/projects/" + projetoDono.getId() + "/etapa-cargas-especiais")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.etapaAtual").value(5))
                .andExpect(jsonPath("$.demandaVeDveKva").value(0.00))
                .andExpect(jsonPath("$.demandaIncendioDmbiKva").value(0.00));
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id}/etapa-cargas-especiais repetido deve executar upsert sem duplicar registro")
    void saveSpecialLoadsStage_upsertIdempotente_naoDuplica() throws Exception {
        SpecialLoadsStageRequest r1 = new SpecialLoadsStageRequest(2, new BigDecimal("7.40"), null, null);
        mockMvc.perform(put("/api/v1/projects/" + projetoDono.getId() + "/etapa-cargas-especiais")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r1)))
                .andExpect(status().isOk());

        assertEquals(1, specialLoadsRepository.count());

        SpecialLoadsStageRequest r2 = new SpecialLoadsStageRequest(4, new BigDecimal("7.40"), new BigDecimal("10.00"), null);
        mockMvc.perform(put("/api/v1/projects/" + projetoDono.getId() + "/etapa-cargas-especiais")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.demandaVeDveKva").value(20.72));

        assertEquals(1, specialLoadsRepository.count());
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id}/etapa-cargas-especiais com vagas sem potência deve retornar 400 Bad Request (RFC 7807)")
    void saveSpecialLoadsStage_vagasSemPotencia_retorna400() throws Exception {
        SpecialLoadsStageRequest request = new SpecialLoadsStageRequest(4, null, null, null);

        mockMvc.perform(put("/api/v1/projects/" + projetoDono.getId() + "/etapa-cargas-especiais")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id}/etapa-cargas-especiais sem token deve retornar 401 Unauthorized")
    void saveSpecialLoadsStage_semToken_retorna401() throws Exception {
        SpecialLoadsStageRequest request = new SpecialLoadsStageRequest();

        mockMvc.perform(put("/api/v1/projects/" + projetoDono.getId() + "/etapa-cargas-especiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id}/etapa-cargas-especiais com outro usuário deve retornar 403 Forbidden")
    void saveSpecialLoadsStage_outroUsuario_retorna403() throws Exception {
        SpecialLoadsStageRequest request = new SpecialLoadsStageRequest();

        mockMvc.perform(put("/api/v1/projects/" + projetoDono.getId() + "/etapa-cargas-especiais")
                        .header("Authorization", "Bearer " + tokenOutro)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id}/etapa-cargas-especiais com id inexistente deve retornar 404 Not Found")
    void saveSpecialLoadsStage_projetoInexistente_retorna404() throws Exception {
        SpecialLoadsStageRequest request = new SpecialLoadsStageRequest();

        mockMvc.perform(put("/api/v1/projects/999999/etapa-cargas-especiais")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
