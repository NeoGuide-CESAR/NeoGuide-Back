package neoguide.project.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import neoguide.project.model.DemandResults;
import neoguide.project.model.Project;
import neoguide.project.model.ProjectStatus;
import neoguide.project.model.ProjectType;
import neoguide.project.model.User;
import neoguide.project.repository.CommercialUnitRepository;
import neoguide.project.repository.CommonAreaLoadRepository;
import neoguide.project.repository.DemandResultsRepository;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.ResidentialTypologyRepository;
import neoguide.project.repository.SpecialLoadsRepository;
import neoguide.project.repository.UserRepository;
import neoguide.project.security.jwt.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NormativeValidationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DemandResultsRepository demandResultsRepository;

    @Autowired
    private ResidentialTypologyRepository residentialTypologyRepository;

    @Autowired
    private CommonAreaLoadRepository commonAreaLoadRepository;

    @Autowired
    private CommercialUnitRepository commercialUnitRepository;

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
    private Project projetoComCalculo;
    private Project projetoSemCalculo;

    @BeforeEach
    void setUp() {
        limparBanco();

        dono = userRepository.save(new User("Projetista Dono", "dono.val@empresa.com", passwordEncoder.encode("SenhaForte@123")));
        outroUsuario = userRepository.save(new User("Outro Projetista", "outro.val@empresa.com", passwordEncoder.encode("SenhaForte@123")));

        tokenDono = jwtService.generateToken(dono);
        tokenOutro = jwtService.generateToken(outroUsuario);

        projetoComCalculo = new Project();
        projetoComCalculo.setUser(dono);
        projetoComCalculo.setProtocolo("PRJ-2026-0042");
        projetoComCalculo.setTitulo("Edifício Boa Viagem");
        projetoComCalculo.setTensaoFornecimento("380_220V");
        projetoComCalculo.setTipo(ProjectType.LIGACAO_NOVA);
        projetoComCalculo.setStatus(ProjectStatus.CONCLUIDO);
        projetoComCalculo.setEtapaAtual(6);
        projetoComCalculo.setMunicipio("Recife");
        projetoComCalculo.setAtivo(true);
        projetoComCalculo = projectRepository.save(projetoComCalculo);

        DemandResults results = new DemandResults();
        results.setProject(projetoComCalculo);
        results.setDemandaResidencialKva(new BigDecimal("114.08"));
        results.setDemandaServicoKva(new BigDecimal("69.45"));
        results.setDemandaComercialKva(BigDecimal.ZERO);
        results.setDemandaVeKva(new BigDecimal("23.68"));
        results.setDemandaIncendioKva(new BigDecimal("2.70"));
        results.setDemandaGlobalKva(new BigDecimal("209.91"));
        results.setDemandaQdgKva(new BigDecimal("207.21"));
        results.setTipoFornecimento("TRIFASICO_380_220V_BT");
        results.setDisjuntorGeralA(320);
        results.setCaboFaseMm2("1 x 240 mm²");
        results.setCaboNeutroMm2("1 x 240 mm²");
        results.setBarramentoCobre("30 x 5 mm");
        results.setCapacidadeBarramentoA(449);
        results.setCalculadoEm(LocalDateTime.now());
        demandResultsRepository.save(results);

        projetoSemCalculo = new Project();
        projetoSemCalculo.setUser(dono);
        projetoSemCalculo.setProtocolo("PRJ-2026-0099");
        projetoSemCalculo.setTitulo("Edifício Sem Cálculo");
        projetoSemCalculo.setTensaoFornecimento("380_220V");
        projetoSemCalculo.setTipo(ProjectType.LIGACAO_NOVA);
        projetoSemCalculo.setStatus(ProjectStatus.RASCUNHO);
        projetoSemCalculo.setEtapaAtual(2);
        projetoSemCalculo.setMunicipio("Recife");
        projetoSemCalculo.setAtivo(true);
        projetoSemCalculo = projectRepository.save(projetoSemCalculo);
    }

    @AfterEach
    void tearDown() {
        limparBanco();
    }

    private void limparBanco() {
        demandResultsRepository.deleteAll();
        specialLoadsRepository.deleteAll();
        commercialUnitRepository.deleteAll();
        commonAreaLoadRepository.deleteAll();
        residentialTypologyRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/v1/projects/{id}/validacao-normativa deve retornar 200 OK com selo de conformidade")
    void deveRetornar200ComSeloQuandoProjetoConsolidado() throws Exception {
        mockMvc.perform(get("/api/v1/projects/" + projetoComCalculo.getId() + "/motor/validacao-normativa")
                        .header("Authorization", "Bearer " + tokenDono)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId", is(projetoComCalculo.getId().intValue())))
                .andExpect(jsonPath("$.protocolo", is("PRJ-2026-0042")))
                .andExpect(jsonPath("$.conforme", is(true)))
                .andExpect(jsonPath("$.seloConformidade", is("CONFORME_DIS_NOR_053")))
                .andExpect(jsonPath("$.totalErrosNormativos", is(0)))
                .andExpect(jsonPath("$.mensagens", notNullValue()))
                .andExpect(jsonPath("$.detalhesTecnicos.correnteProjetoIpA", notNullValue()))
                .andExpect(jsonPath("$.detalhesTecnicos.disjuntorGeralInA", is(320)));
    }

    @Test
    @DisplayName("GET deve responder também no alias /projects/{id}/motor/validacao-normativa")
    void deveResponderNoAliasComSucesso() throws Exception {
        mockMvc.perform(get("/projects/" + projetoComCalculo.getId() + "/motor/validacao-normativa")
                        .header("Authorization", "Bearer " + tokenDono)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conforme", is(true)))
                .andExpect(jsonPath("$.seloConformidade", is("CONFORME_DIS_NOR_053")));
    }

    @Test
    @DisplayName("GET deve retornar 401 Unauthorized quando sem token JWT")
    void deveRetornar401QuandoSemToken() throws Exception {
        mockMvc.perform(get("/api/v1/projects/" + projetoComCalculo.getId() + "/motor/validacao-normativa")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET deve retornar 403 Forbidden quando consultado por outro usuário")
    void deveRetornar403QuandoUsuarioNaoEhDono() throws Exception {
        mockMvc.perform(get("/api/v1/projects/" + projetoComCalculo.getId() + "/motor/validacao-normativa")
                        .header("Authorization", "Bearer " + tokenOutro)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET deve retornar 404 Not Found para projeto inexistente")
    void deveRetornar404QuandoProjetoInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/projects/99999/motor/validacao-normativa")
                        .header("Authorization", "Bearer " + tokenDono)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET deve retornar 422 Unprocessable Entity quando o projeto ainda não possui cálculo oficial")
    void deveRetornar422QuandoSemCalculoOficial() throws Exception {
        mockMvc.perform(get("/api/v1/projects/" + projetoSemCalculo.getId() + "/motor/validacao-normativa")
                        .header("Authorization", "Bearer " + tokenDono)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity());
    }
}
