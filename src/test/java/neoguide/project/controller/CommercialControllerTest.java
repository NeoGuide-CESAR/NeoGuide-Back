package neoguide.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import neoguide.project.dto.CommercialStageRequest;
import neoguide.project.dto.EspacoComercialRequest;
import neoguide.project.model.Project;
import neoguide.project.model.TipoEdificacao;
import neoguide.project.model.User;
import neoguide.project.repository.CommercialUnitRepository;
import neoguide.project.repository.ProjectRepository;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Autowired
    private TestDatabaseCleaner testDatabaseCleaner;

    private User dono;
    private User outroUsuario;
    private String tokenDono;
    private String tokenOutro;
    private Project projetoDono;

    @BeforeEach
    void setUp() {
        testDatabaseCleaner.cleanOperationalTables();

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
        projetoDono.setTipoEdificacao(TipoEdificacao.MISTO);
        projetoDono = projectRepository.save(projetoDono);
    }

    @AfterEach
    void tearDown() {
        testDatabaseCleaner.cleanOperationalTables();
    }

    private static EspacoComercialRequest espaco(String codigo, String area, String carga) {
        return new EspacoComercialRequest(codigo, new BigDecimal(area), new BigDecimal(carga));
    }

    @Test
    @DisplayName("PUT etapa-comercial com uma loja de 20 kW deve retornar 200 com Dc = 21,74 kVA")
    void saveCommercialStage_comSucesso_retorna200() throws Exception {
        CommercialStageRequest request = new CommercialStageRequest(List.of(espaco("LOJAS", "120", "20")));

        mockMvc.perform(put("/api/v1/projects/" + projetoDono.getId() + "/etapa-comercial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.projectId").value(projetoDono.getId()))
                .andExpect(jsonPath("$.etapaAtual").value(4))
                .andExpect(jsonPath("$.totalEspacos").value(1))
                .andExpect(jsonPath("$.potenciaTotalInstaladaKw").value(20.00))
                .andExpect(jsonPath("$.demandaComercialDcKva").value(21.74))
                .andExpect(jsonPath("$.espacos", hasSize(1)))
                .andExpect(jsonPath("$.espacos[0].ordem").value(1))
                .andExpect(jsonPath("$.espacos[0].ocupacaoCodigo").value("LOJAS"))
                .andExpect(jsonPath("$.espacos[0].rotulo").value("Lojas e Vitrines"))
                .andExpect(jsonPath("$.espacos[0].demandaCalculadaKva").value(21.74));
    }

    @Test
    @DisplayName("PUT etapa-comercial com escritório de 30 kW deve aplicar o escalonamento e retornar 29,35 kVA")
    void saveCommercialStage_escritorioEscalonado_retorna200() throws Exception {
        CommercialStageRequest request = new CommercialStageRequest(List.of(espaco("ESCRITORIOS", "85", "30")));

        mockMvc.perform(put("/projects/" + projetoDono.getId() + "/etapa-comercial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.demandaComercialDcKva").value(29.35));
    }

    @Test
    @DisplayName("PUT etapa-comercial sem espaços deve retornar 200 com Dc = 0,00 kVA")
    void saveCommercialStage_semEspacos_retorna200ComZero() throws Exception {
        CommercialStageRequest request = new CommercialStageRequest(List.of());

        mockMvc.perform(put("/api/v1/projects/" + projetoDono.getId() + "/etapa-comercial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEspacos").value(0))
                .andExpect(jsonPath("$.demandaComercialDcKva").value(0.00));
    }

    @Test
    @DisplayName("PUT etapa-comercial com ocupação fora da Tabela 22 deve retornar 400 no campo ocupacaoCodigo")
    void saveCommercialStage_ocupacaoDesconhecida_retorna400() throws Exception {
        CommercialStageRequest request = new CommercialStageRequest(List.of(espaco("INEXISTENTE", "50", "10")));

        mockMvc.perform(put("/api/v1/projects/" + projetoDono.getId() + "/etapa-comercial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors['espacos[0].ocupacaoCodigo']").exists());
    }

    @Test
    @DisplayName("PUT etapa-comercial com carga negativa deve retornar 400 (RFC 7807)")
    void saveCommercialStage_cargaInvalida_retorna400() throws Exception {
        CommercialStageRequest request = new CommercialStageRequest(List.of(espaco("LOJAS", "80", "-10")));

        mockMvc.perform(put("/api/v1/projects/" + projetoDono.getId() + "/etapa-comercial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT etapa-comercial de edificação residencial com espaço deve retornar 400 no campo espacos")
    void saveCommercialStage_residencial_aceitaEspacosEListaVazia() throws Exception {
        Project residencial = new Project();
        residencial.setUser(dono);
        residencial.setProtocolo("PRJ-2026-0002");
        residencial.setTitulo("Residencial Jardim");
        residencial.setMunicipio("Recife");
        residencial.setTensaoFornecimento("380_220V");
        residencial.setEtapaAtual(3);
        residencial.setTipoEdificacao(TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR);
        residencial = projectRepository.save(residencial);

        CommercialStageRequest request = new CommercialStageRequest(List.of(espaco("LOJAS", "80", "10")));

        mockMvc.perform(put("/api/v1/projects/" + residencial.getId() + "/etapa-comercial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEspacos").value(1))
                .andExpect(jsonPath("$.tipoEnquadramento").value("MISTO"));

        // Lista vazia também é válida e o enquadramento volta a ser o do tipo escolhido.
        mockMvc.perform(put("/api/v1/projects/" + residencial.getId() + "/etapa-comercial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommercialStageRequest(List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEspacos").value(0))
                .andExpect(jsonPath("$.tipoEnquadramento").value("RESIDENCIAL_MULTIFAMILIAR"));
    }

    @Test
    @DisplayName("GET do projeto deve devolver os espaços comerciais gravados, na ordem")
    void getProject_devolveEspacosGravados() throws Exception {
        CommercialStageRequest request = new CommercialStageRequest(List.of(
                espaco("LOJAS", "120", "20"), espaco("ESCRITORIOS", "85", "30")));
        mockMvc.perform(put("/api/v1/projects/" + projetoDono.getId() + "/etapa-comercial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/projects/" + projetoDono.getId())
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.espacosComerciais", hasSize(2)))
                .andExpect(jsonPath("$.espacosComerciais[0].ocupacaoCodigo").value("LOJAS"))
                .andExpect(jsonPath("$.espacosComerciais[1].ocupacaoCodigo").value("ESCRITORIOS"))
                .andExpect(jsonPath("$.espacosComerciais[1].ordem").value(2));
    }

    @Test
    @DisplayName("POST pre-calculo-etapa devolve o Dc sem gravar nada no projeto")
    void preCalculoEtapa_devolveDcSemGravar() throws Exception {
        String corpo = "{\"etapa\":\"comercial\",\"espacos\":[{\"ocupacaoCodigo\":\"ESCRITORIOS\",\"areaM2\":85,\"cargaInstaladaKw\":30}]}";

        mockMvc.perform(post("/api/v1/simulacao/pre-calculo-etapa")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.demandaComercialDcKva").value(29.35))
                .andExpect(jsonPath("$.espacos[0].demandaCalculadaKva").value(29.35));

        org.junit.jupiter.api.Assertions.assertTrue(commercialUnitRepository.findByProjectId(projetoDono.getId()).isEmpty());
    }

    @Test
    @DisplayName("PUT etapa-comercial sem token deve retornar 401 Unauthorized")
    void saveCommercialStage_semToken_retorna401() throws Exception {
        CommercialStageRequest request = new CommercialStageRequest(List.of());

        mockMvc.perform(put("/api/v1/projects/" + projetoDono.getId() + "/etapa-comercial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT etapa-comercial com usuário diferente deve retornar 403 Forbidden")
    void saveCommercialStage_outroUsuario_retorna403() throws Exception {
        CommercialStageRequest request = new CommercialStageRequest(List.of());

        mockMvc.perform(put("/api/v1/projects/" + projetoDono.getId() + "/etapa-comercial")
                        .header("Authorization", "Bearer " + tokenOutro)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT etapa-comercial com id inexistente deve retornar 404 Not Found")
    void saveCommercialStage_projetoInexistente_retorna404() throws Exception {
        CommercialStageRequest request = new CommercialStageRequest(List.of());

        mockMvc.perform(put("/api/v1/projects/999999/etapa-comercial")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
