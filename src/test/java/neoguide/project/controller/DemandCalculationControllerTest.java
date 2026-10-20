package neoguide.project.controller;

import neoguide.project.model.Project;
import neoguide.project.model.ProjectStatus;
import neoguide.project.model.ProjectType;
import neoguide.project.model.ResidentialTypology;
import neoguide.project.model.User;
import neoguide.project.repository.DemandResultsRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DemandCalculationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ResidentialTypologyRepository residentialTypologyRepository;

    @Autowired
    private DemandResultsRepository demandResultsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User dono;
    private User outroUsuario;
    private String tokenDono;
    private String tokenOutro;
    private Project projetoDono;
    private Project projetoSemTipologia;

    @BeforeEach
    void setUp() {
        demandResultsRepository.deleteAll();
        residentialTypologyRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        dono = userRepository.save(new User("Projetista Dono", "dono@empresa.com", passwordEncoder.encode("SenhaForte@123")));
        outroUsuario = userRepository.save(new User("Outro Projetista", "outro@empresa.com", passwordEncoder.encode("SenhaForte@123")));

        tokenDono = jwtService.generateToken(dono);
        tokenOutro = jwtService.generateToken(outroUsuario);

        projetoDono = new Project();
        projetoDono.setUser(dono);
        projetoDono.setProtocolo("PRJ-2026-0001");
        projetoDono.setTitulo("Condomínio Residencial Parque Central");
        projetoDono.setTensaoFornecimento("380_220V");
        projetoDono.setTipo(ProjectType.LIGACAO_NOVA);
        projetoDono.setStatus(ProjectStatus.RASCUNHO);
        projetoDono.setEtapaAtual(2);
        projetoDono.setMunicipio("Recife");
        projetoDono.setAtivo(true);
        projetoDono = projectRepository.save(projetoDono);

        ResidentialTypology tipologia = new ResidentialTypology(
                projetoDono,
                "Apartamento Padrão 70m²",
                new BigDecimal("70.00"),
                50,
                false,
                new BigDecimal("1.50"),
                new BigDecimal("75.00")
        );
        residentialTypologyRepository.save(tipologia);

        projetoSemTipologia = new Project();
        projetoSemTipologia.setUser(dono);
        projetoSemTipologia.setProtocolo("PRJ-2026-0002");
        projetoSemTipologia.setTitulo("Projeto Incompleto");
        projetoSemTipologia.setTensaoFornecimento("380_220V");
        projetoSemTipologia.setTipo(ProjectType.LIGACAO_NOVA);
        projetoSemTipologia.setStatus(ProjectStatus.RASCUNHO);
        projetoSemTipologia.setEtapaAtual(1);
        projetoSemTipologia.setMunicipio("Recife");
        projetoSemTipologia.setAtivo(true);
        projetoSemTipologia = projectRepository.save(projetoSemTipologia);
    }

    @Test
    @DisplayName("POST /api/v1/projects/{id}/calcular autenticado com dados válidos deve retornar 200 OK com payload completo")
    void shouldCalculateProjectSuccessfully() throws Exception {
        mockMvc.perform(post("/api/v1/projects/" + projetoDono.getId() + "/calcular")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId", is(projetoDono.getId().intValue())))
                .andExpect(jsonPath("$.protocolo", is("PRJ-2026-0001")))
                .andExpect(jsonPath("$.status", is("CONCLUIDO")))
                .andExpect(jsonPath("$.seloConformidade", is("CONFORME_DIS_NOR_053")))
                .andExpect(jsonPath("$.resumoDemandas.demandaResidencialFinalDrfKva", notNullValue()))
                .andExpect(jsonPath("$.resumoDemandas.demandaGlobalDedKva", notNullValue()))
                .andExpect(jsonPath("$.resumoDemandas.demandaQdgKva", notNullValue()))
                .andExpect(jsonPath("$.dimensionamentoEntrada.tipoFornecimento", is("TRIFASICO_380_220V_BT")))
                .andExpect(jsonPath("$.dimensionamentoEntrada.disjuntorGeralA", notNullValue()))
                .andExpect(jsonPath("$.dimensionamentoEntrada.barramentoCobreQdg", notNullValue()))
                .andExpect(jsonPath("$.calculadoEm", notNullValue()));
    }

    @Test
    @DisplayName("POST /projects/{id}/calcular (alias retrocompatível) deve retornar 200 OK")
    void shouldCalculateProjectUsingAliasRouteSuccessfully() throws Exception {
        mockMvc.perform(post("/projects/" + projetoDono.getId() + "/calcular")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONCLUIDO")));
    }

    @Test
    @DisplayName("POST /api/v1/projects/{id}/calcular sem tipologias residenciais deve retornar 422 Unprocessable Entity")
    void shouldReturn422WhenNoResidentialTypologies() throws Exception {
        mockMvc.perform(post("/api/v1/projects/" + projetoSemTipologia.getId() + "/calcular")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/projects/{id}/calcular por usuário que não é proprietário deve retornar 403 Forbidden")
    void shouldReturn403WhenUserNotOwner() throws Exception {
        mockMvc.perform(post("/api/v1/projects/" + projetoDono.getId() + "/calcular")
                        .header("Authorization", "Bearer " + tokenOutro))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("POST /api/v1/projects/999/calcular para projeto inexistente deve retornar 404 Not Found")
    void shouldReturn404WhenProjectNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/projects/999/calcular")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("POST /api/v1/projects/{id}/calcular sem autenticação deve retornar 401 Unauthorized")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/projects/" + projetoDono.getId() + "/calcular"))
                .andExpect(status().isUnauthorized());
    }
}
