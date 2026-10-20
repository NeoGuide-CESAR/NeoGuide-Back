package neoguide.project.service.pdf;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import neoguide.project.exception.BadRequestException;
import neoguide.project.exception.ForbiddenException;
import neoguide.project.exception.ResourceNotFoundException;
import neoguide.project.model.DemandResults;
import neoguide.project.model.Project;
import neoguide.project.model.ProjectStatus;
import neoguide.project.model.User;
import neoguide.project.repository.CommercialUnitRepository;
import neoguide.project.repository.CommonAreaLoadRepository;
import neoguide.project.repository.DemandResultsRepository;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.ResidentialTypologyRepository;
import neoguide.project.repository.SpecialLoadsRepository;

class MemorialPdfServiceTest {

    private ProjectRepository projectRepository;
    private DemandResultsRepository demandResultsRepository;
    private ResidentialTypologyRepository residentialTypologyRepository;
    private CommonAreaLoadRepository commonAreaLoadRepository;
    private CommercialUnitRepository commercialUnitRepository;
    private SpecialLoadsRepository specialLoadsRepository;
    private MemorialSectionRenderer sectionRenderer;
    private MemorialPdfService service;

    @BeforeEach
    void setUp() {
        projectRepository = mock(ProjectRepository.class);
        demandResultsRepository = mock(DemandResultsRepository.class);
        residentialTypologyRepository = mock(ResidentialTypologyRepository.class);
        commonAreaLoadRepository = mock(CommonAreaLoadRepository.class);
        commercialUnitRepository = mock(CommercialUnitRepository.class);
        specialLoadsRepository = mock(SpecialLoadsRepository.class);
        sectionRenderer = new MemorialSectionRenderer();

        service = new MemorialPdfService(
                projectRepository,
                demandResultsRepository,
                residentialTypologyRepository,
                commonAreaLoadRepository,
                commercialUnitRepository,
                specialLoadsRepository,
                sectionRenderer
        );
    }

    private Project criarProjeto(Long id, String email, ProjectStatus status) {
        User user = new User();
        user.setId(1L);
        user.setName("Eng. Roberto Albuquerque");
        user.setEmail(email);
        user.setCrea("123456-D/PE");

        Project p = new Project();
        p.setId(id);
        p.setProtocolo("PRJ-2026-0042");
        p.setTitulo("Edifício Jardins da Jaqueira");
        p.setUser(user);
        p.setStatus(status);
        p.setTensaoFornecimento("380_220V");
        p.setMunicipio("Recife");
        p.setBairro("Jaqueira");
        p.setEndereco("Rua do Futuro, 1200");
        p.setCep("52050110");
        p.setAtivo(true);
        return p;
    }

    private DemandResults criarResultados(Project p) {
        DemandResults r = new DemandResults();
        r.setProject(p);
        r.setDemandaResidencialKva(new BigDecimal("114.08"));
        r.setDemandaServicoKva(new BigDecimal("20.27"));
        r.setDemandaComercialKva(new BigDecimal("19.02"));
        r.setDemandaVeKva(new BigDecimal("23.68"));
        r.setDemandaIncendioKva(new BigDecimal("2.70"));
        r.setDemandaGlobalKva(new BigDecimal("179.75"));
        r.setDemandaQdgKva(new BigDecimal("177.05"));
        r.setTipoFornecimento("TRIFASICO_380_220V_BT");
        r.setDisjuntorGeralA(320);
        r.setCaboFaseMm2("1 x 240 mm² XLPE 90ºC");
        r.setCaboNeutroMm2("1 x 240 mm² XLPE 90ºC");
        r.setBarramentoCobre("30 x 5 mm (1 1/4\" x 3/16\")");
        r.setCapacidadeBarramentoA(449);
        r.setCalculadoEm(LocalDateTime.now());
        return r;
    }

    @Test
    @DisplayName("Deve gerar PDF formal com sucesso quando projeto concluido e consolidado")
    void deveGerarPdfComSucessoQuandoProjetoConsolidado() {
        Long projectId = 42L;
        String email = "roberto@eng.com";
        Project project = criarProjeto(projectId, email, ProjectStatus.CONCLUIDO);
        DemandResults results = criarResultados(project);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(demandResultsRepository.findByProjectId(projectId)).thenReturn(Optional.of(results));
        when(residentialTypologyRepository.findByProjectIdOrderByIdAsc(projectId)).thenReturn(Collections.emptyList());
        when(commonAreaLoadRepository.findByProjectId(projectId)).thenReturn(Collections.emptyList());
        when(commercialUnitRepository.findByProjectId(projectId)).thenReturn(Collections.emptyList());
        when(specialLoadsRepository.findByProjectId(projectId)).thenReturn(Optional.empty());

        byte[] pdfBytes = service.generateMemorialPdf(projectId, email);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1000, "O tamanho do PDF gerado deve ser superior a 1000 bytes");

        // Asserta Magic Number do PDF (%PDF-)
        String header = new String(pdfBytes, 0, 5, StandardCharsets.US_ASCII);
        assertEquals("%PDF-", header);
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando projeto estiver com status RASCUNHO")
    void deveLancarBadRequestExceptionQuandoProjetoEmRascunho() {
        Long projectId = 42L;
        String email = "roberto@eng.com";
        Project project = criarProjeto(projectId, email, ProjectStatus.RASCUNHO);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.generateMemorialPdf(projectId, email));
        assertTrue(ex.getMessage().contains("Projeto não consolidado"));
    }

    @Test
    @DisplayName("Deve lancar BadRequestException quando demand_results nao existir")
    void deveLancarBadRequestExceptionQuandoDemandResultsNaoExiste() {
        Long projectId = 42L;
        String email = "roberto@eng.com";
        Project project = criarProjeto(projectId, email, ProjectStatus.CONCLUIDO);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(demandResultsRepository.findByProjectId(projectId)).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.generateMemorialPdf(projectId, email));
        assertTrue(ex.getMessage().contains("Projeto não consolidado"));
    }

    @Test
    @DisplayName("Deve lancar ForbiddenException quando email nao pertencer ao proprietario")
    void deveLancarForbiddenExceptionQuandoUsuarioNaoEhProprietario() {
        Long projectId = 42L;
        Project project = criarProjeto(projectId, "proprietario@eng.com", ProjectStatus.CONCLUIDO);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThrows(ForbiddenException.class,
                () -> service.generateMemorialPdf(projectId, "invasor@empresa.com"));
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException quando projeto nao for encontrado")
    void deveLancarNotFoundQuandoProjetoInexistente() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.generateMemorialPdf(999L, "usuario@empresa.com"));
    }
}
