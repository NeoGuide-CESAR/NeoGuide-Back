package neoguide.project.controller;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
class MemorialPdfControllerTest {

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
    private Project projetoConsolidado;
    private Project projetoRascunho;

    @BeforeEach
    void setUp() {
        limparBanco();

        dono = userRepository.save(new User("Engenheiro Dono", "dono.pdf@empresa.com", passwordEncoder.encode("SenhaForte@123")));
        outroUsuario = userRepository.save(new User("Outro Projetista", "outro.pdf@empresa.com", passwordEncoder.encode("SenhaForte@123")));

        tokenDono = jwtService.generateToken(dono);
        tokenOutro = jwtService.generateToken(outroUsuario);

        projetoConsolidado = new Project();
        projetoConsolidado.setUser(dono);
        projetoConsolidado.setProtocolo("PRJ-2026-0004");
        projetoConsolidado.setTitulo("Edifício Jardins da Jaqueira");
        projetoConsolidado.setTensaoFornecimento("380_220V");
        projetoConsolidado.setTipo(ProjectType.LIGACAO_NOVA);
        projetoConsolidado.setStatus(ProjectStatus.CONCLUIDO);
        projetoConsolidado.setEtapaAtual(6);
        projetoConsolidado.setMunicipio("Recife");
        projetoConsolidado.setAtivo(true);
        projetoConsolidado = projectRepository.save(projetoConsolidado);

        DemandResults results = new DemandResults();
        results.setProject(projetoConsolidado);
        results.setDemandaResidencialKva(new BigDecimal("122.71"));
        results.setDemandaServicoKva(new BigDecimal("13.97"));
        results.setDemandaComercialKva(BigDecimal.ZERO);
        results.setDemandaVeKva(new BigDecimal("23.68"));
        results.setDemandaIncendioKva(new BigDecimal("2.70"));
        results.setDemandaGlobalKva(new BigDecimal("163.06"));
        results.setDemandaQdgKva(new BigDecimal("160.36"));
        results.setTipoFornecimento("TRIFASICO_380_220V_BT");
        results.setDisjuntorGeralA(250);
        results.setCaboFaseMm2("150 mm² XLPE");
        results.setCaboNeutroMm2("150 mm² XLPE");
        results.setBarramentoCobre("30 x 5 mm (449 A)");
        results.setCapacidadeBarramentoA(449);
        results.setCalculadoEm(LocalDateTime.now());
        demandResultsRepository.save(results);

        projetoRascunho = new Project();
        projetoRascunho.setUser(dono);
        projetoRascunho.setProtocolo("PRJ-2026-0005");
        projetoRascunho.setTitulo("Edifício em Rascunho");
        projetoRascunho.setTensaoFornecimento("380_220V");
        projetoRascunho.setTipo(ProjectType.LIGACAO_NOVA);
        projetoRascunho.setStatus(ProjectStatus.RASCUNHO);
        projetoRascunho.setEtapaAtual(2);
        projetoRascunho.setMunicipio("Recife");
        projetoRascunho.setAtivo(true);
        projetoRascunho = projectRepository.save(projetoRascunho);
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
    @DisplayName("GET /api/v1/projects/{id}/memorial-pdf deve retornar 200 OK com PDF valido e headers de anexo")
    void getMemorialPdf_deveRetornar200ComHeadersCorretos() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/projects/" + projetoConsolidado.getId() + "/motor/memorial-pdf")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"Memorial_Descritivo_PRJ-2026-0004.pdf\"")))
                .andExpect(header().string("Cache-Control", containsString("no-cache")))
                .andReturn();

        byte[] pdfBytes = result.getResponse().getContentAsByteArray();
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1000);
        String magicHeader = new String(pdfBytes, 0, 5, StandardCharsets.US_ASCII);
        assertEquals("%PDF-", magicHeader);
    }

    @Test
    @DisplayName("GET deve responder também no alias /projects/{id}/motor/memorial-pdf")
    void deveResponderNoAliasComSucesso() throws Exception {
        mockMvc.perform(get("/projects/" + projetoConsolidado.getId() + "/motor/memorial-pdf")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE));
    }

    @Test
    @DisplayName("GET deve retornar 400 Bad Request quando projeto estiver em RASCUNHO (mesmo com Accept application/pdf)")
    void getMemorialPdf_deveRetornar400QuandoProjetoNaoCalculado() throws Exception {
        mockMvc.perform(get("/api/v1/projects/" + projetoRascunho.getId() + "/motor/memorial-pdf")
                        .header("Authorization", "Bearer " + tokenDono)
                        .accept(MediaType.APPLICATION_PDF))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_JSON_VALUE)));
    }

    @Test
    @DisplayName("GET deve retornar 401 Unauthorized quando sem autenticacao")
    void getMemorialPdf_deveRetornar401QuandoNaoAutenticado() throws Exception {
        mockMvc.perform(get("/api/v1/projects/" + projetoConsolidado.getId() + "/motor/memorial-pdf"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET deve retornar 403 Forbidden quando consultado por outro usuario")
    void getMemorialPdf_deveRetornar403QuandoNaoEhDono() throws Exception {
        mockMvc.perform(get("/api/v1/projects/" + projetoConsolidado.getId() + "/motor/memorial-pdf")
                        .header("Authorization", "Bearer " + tokenOutro))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET deve retornar 404 Not Found quando projeto nao existir")
    void getMemorialPdf_deveRetornar404QuandoInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/projects/99999/motor/memorial-pdf")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isNotFound());
    }
}
