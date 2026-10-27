package neoguide.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import neoguide.project.dto.CommercialStageRequest;
import neoguide.project.dto.CommercialUnitItemRequest;
import neoguide.project.model.Project;
import neoguide.project.model.User;
import neoguide.project.repository.CommercialUnitRepository;
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

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommercialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CommercialUnitRepository commercialUnitRepository;

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
        commercialUnitRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        dono = userRepository.save(new User("Projetista Dono", "dono@comercial.com", passwordEncoder.encode("SenhaForte@123")));
        outroUsuario = userRepository.save(new User("Outro Projetista", "outro@comercial.com", passwordEncoder.encode("SenhaForte@123")));

        tokenDono = jwtService.generateToken(dono);
        tokenOutro = jwtService.generateToken(outroUsuario);

        projetoDono = new Project();
        projetoDono.setUser(dono);
        projetoDono.setProtocolo("PRJ-2026-0001");
        projetoDono.setTitulo("Edifício Misto Central");
        projetoDono.setMunicipio("Recife");
        projetoDono.setTensaoFornecimento("380_220V");
        projetoDono.setEtapaAtual(3);
        projetoDono = projectRepository.save(projetoDono);
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id}/etapa-comercial com unidades comerciais deve retornar 200 OK com Dc calculado")
    void saveCommercialStage_comSucesso_retorna200() throws Exception {
        CommercialUnitItemRequest u1 = new CommercialUnitItemRequest("Loja 01", "Padaria", new BigDecimal("80.00"), new BigDecimal("25.00"));
        CommercialStageRequest request = new CommercialStageRequest(List.of(u1));

        mockMvc.perform(put("/api/v1/projects/" + projetoDono.getId() + "/etapa-comercial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.projectId").value(projetoDono.getId()))
                .andExpect(jsonPath("$.etapaAtual").value(4))
                .andExpect(jsonPath("$.totalUnidades").value(1))
                .andExpect(jsonPath("$.potenciaTotalInstaladaKw").value(25.00))
                .andExpect(jsonPath("$.demandaComercialDcKva").value(25.54))
                .andExpect(jsonPath("$.unidades", hasSize(1)))
                .andExpect(jsonPath("$.unidades[0].identificacao").value("Loja 01"))
                .andExpect(jsonPath("$.unidades[0].atividadeComercial").value("Padaria"))
                .andExpect(jsonPath("$.unidades[0].demandaCalculadaKva").value(25.54));
    }

    @Test
    @DisplayName("PUT /projects/{id}/etapa-comercial alias deve responder 200 OK")
    void saveCommercialStage_alias_retorna200() throws Exception {
        CommercialUnitItemRequest u1 = new CommercialUnitItemRequest("Sala 101", "Consultório", new BigDecimal("40.00"), new BigDecimal("10.00"));
        CommercialStageRequest request = new CommercialStageRequest(List.of(u1));

        mockMvc.perform(put("/projects/" + projetoDono.getId() + "/etapa-comercial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.demandaComercialDcKva").value(10.87));
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id}/etapa-comercial sem unidades comerciais deve retornar 200 OK e Dc = 0.00 kVA")
    void saveCommercialStage_semUnidades_retorna200ComZero() throws Exception {
        CommercialStageRequest request = new CommercialStageRequest(List.of());

        mockMvc.perform(put("/api/v1/projects/" + projetoDono.getId() + "/etapa-comercial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.totalUnidades").value(0))
                .andExpect(jsonPath("$.demandaComercialDcKva").value(0.00));
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id}/etapa-comercial com potência inválida deve retornar 400 Bad Request (RFC 7807)")
    void saveCommercialStage_potenciaInvalida_retorna400() throws Exception {
        CommercialUnitItemRequest u1 = new CommercialUnitItemRequest("Loja 01", "Padaria", new BigDecimal("80.00"), new BigDecimal("-10.00"));
        CommercialStageRequest request = new CommercialStageRequest(List.of(u1));

        mockMvc.perform(put("/api/v1/projects/" + projetoDono.getId() + "/etapa-comercial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id}/etapa-comercial sem token deve retornar 401 Unauthorized")
    void saveCommercialStage_semToken_retorna401() throws Exception {
        CommercialStageRequest request = new CommercialStageRequest(List.of());

        mockMvc.perform(put("/api/v1/projects/" + projetoDono.getId() + "/etapa-comercial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id}/etapa-comercial com usuário diferente deve retornar 403 Forbidden")
    void saveCommercialStage_outroUsuario_retorna403() throws Exception {
        CommercialStageRequest request = new CommercialStageRequest(List.of());

        mockMvc.perform(put("/api/v1/projects/" + projetoDono.getId() + "/etapa-comercial")
                        .header("Authorization", "Bearer " + tokenOutro)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id}/etapa-comercial com id inexistente deve retornar 404 Not Found")
    void saveCommercialStage_projetoInexistente_retorna404() throws Exception {
        CommercialStageRequest request = new CommercialStageRequest(List.of());

        mockMvc.perform(put("/api/v1/projects/999999/etapa-comercial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
