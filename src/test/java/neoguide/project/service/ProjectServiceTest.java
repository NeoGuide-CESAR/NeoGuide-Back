package neoguide.project.service;

import neoguide.project.dto.SpecialLoadsStageRequest;
import neoguide.project.dto.SpecialLoadsStageResponse;
import neoguide.project.engine.SpecialLoadsCalculationEngine;
import neoguide.project.engine.model.ElectricVehicleLoadOutput;
import neoguide.project.engine.model.FirePumpLoadOutput;
import neoguide.project.engine.model.SpecialLoadsCalculationResult;
import neoguide.project.model.SpecialLoads;
import neoguide.project.repository.SpecialLoadsRepository;

import neoguide.project.dto.CommercialStageRequest;
import neoguide.project.dto.CommercialStageResponse;
import neoguide.project.dto.EspacoComercialRequest;
import neoguide.project.engine.model.CommercialUnitResult;
import neoguide.project.engine.model.CommercialCalculationResult;
import neoguide.project.model.CommercialUnit;
import neoguide.project.model.TipoEdificacao;
import neoguide.project.repository.CommercialUnitRepository;

import neoguide.project.dto.CommonAreaLoadItemRequest;
import neoguide.project.dto.CommonAreaStageRequest;
import neoguide.project.dto.CommonAreaStageResponse;
import neoguide.project.dto.CreateProjectRequest;
import neoguide.project.dto.CreateProjectResponse;
import neoguide.project.dto.ProjectResponse;
import neoguide.project.dto.ProjectUpdatePatchRequest;
import neoguide.project.dto.ResidentialStageRequest;
import neoguide.project.dto.ResidentialStageResponse;
import neoguide.project.dto.ResidentialTypologyItemRequest;
import neoguide.project.engine.CommonAreaCalculationEngine;
import neoguide.project.engine.ResidentialCalculationEngine;
import neoguide.project.engine.model.commonarea.CommonAreaCalculationResult;
import neoguide.project.engine.model.commonarea.CommonAreaSubtotal;
import neoguide.project.model.CommonAreaLoad;
import neoguide.project.repository.CommonAreaLoadRepository;
import neoguide.project.engine.model.ResidentialCalculationResult;
import neoguide.project.engine.model.ResidentialTypologyOutput;
import neoguide.project.exception.ForbiddenException;
import neoguide.project.exception.ResourceNotFoundException;
import neoguide.project.exception.ValidationException;
import neoguide.project.model.Project;
import neoguide.project.model.ProjectStatus;
import neoguide.project.model.ProjectType;
import neoguide.project.model.ResidentialTypology;
import neoguide.project.model.User;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.ResidentialTypologyRepository;
import neoguide.project.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResidentialTypologyRepository residentialTypologyRepository;

    @Mock
    private ResidentialCalculationEngine residentialCalculationEngine;

    @Mock
    private CommonAreaLoadRepository commonAreaLoadRepository;

    @Mock
    private CommonAreaCalculationEngine commonAreaCalculationEngine;

    @Mock
    private CommercialUnitRepository commercialUnitRepository;

    @Mock
    private CommercialStageService commercialStageService;

    @Mock
    private SpecialLoadsRepository specialLoadsRepository;

    @Mock
    private SpecialLoadsCalculationEngine specialLoadsCalculationEngine;

    @Mock
    private EstadoEtapasService estadoEtapasService;

    @InjectMocks
    private ProjectService projectService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Engenheiro Teste", "eng@teste.com", "senha123");
        user.setId(10L);
    }

    @Test
    @DisplayName("Deve gerar o primeiro protocolo do ano quando não houver projetos anteriores")
    void shouldGenerateFirstProtocolOfYear() {
        int year = LocalDate.now().getYear();
        when(projectRepository.findLastProtocoloByPrefix("PRJ-" + year + "-%")).thenReturn(Optional.empty());

        String protocolo = projectService.generateNextProtocolo(year);

        assertEquals(String.format("PRJ-%d-0001", year), protocolo);
        verify(projectRepository).findLastProtocoloByPrefix("PRJ-" + year + "-%");
    }

    @Test
    @DisplayName("Deve incrementar protocolo sequencial anual a partir do último existente")
    void shouldIncrementExistingProtocol() {
        int year = 2026;
        when(projectRepository.findLastProtocoloByPrefix("PRJ-2026-%"))
                .thenReturn(Optional.of("PRJ-2026-0001"));

        String protocolo = projectService.generateNextProtocolo(year);

        assertEquals("PRJ-2026-0002", protocolo);

        when(projectRepository.findLastProtocoloByPrefix("PRJ-2026-%"))
                .thenReturn(Optional.of("PRJ-2026-0099"));

        String proximo = projectService.generateNextProtocolo(year);
        assertEquals("PRJ-2026-0100", proximo);
    }

    @Test
    @DisplayName("Deve criar projeto com sucesso, persistir dados do DNA e retornar CreateProjectResponse")
    void shouldCreateProjectSuccessfully() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setTipoEdificacao("RESIDENCIAL_MULTIFAMILIAR");
        request.setTitulo("Edifício Solar Central");
        request.setDescricao("Edifício comercial de 12 pavimentos");
        request.setTipo("LIGACAO_NOVA");
        request.setTensaoFornecimento("380_220V");
        request.setPossuiComercial(true);
        request.setPossuiVe(true);
        request.setPossuiIncendio(false);
        request.setResponsavel("Engenheiro Teste");
        request.setCrea("12345678-D");
        request.setMunicipio("Recife");
        request.setCep("50030-000");
        request.setEndereco("Cais do Apolo, 100");
        request.setBairro("Bairro do Recife");

        int year = LocalDate.now().getYear();
        when(projectRepository.findLastProtocoloByPrefix("PRJ-" + year + "-%"))
                .thenReturn(Optional.of(String.format("PRJ-%d-0003", year)));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project p = invocation.getArgument(0);
            p.setId(100L);
            return p;
        });

        CreateProjectResponse response = projectService.createProject(request, 10L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(String.format("PRJ-%d-0004", year), response.getProtocolo());
        assertEquals("Edifício Solar Central", response.getTitulo());
        assertEquals(ProjectStatus.RASCUNHO.name(), response.getStatus());
        assertEquals(1, response.getEtapaAtual());
        assertNotNull(response.getCreatedAt());

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(captor.capture());
        Project saved = captor.getValue();

        assertEquals(user, saved.getUser());
        assertEquals("380_220V", saved.getTensaoFornecimento());
        assertTrue(saved.isPossuiComercial());
        assertTrue(saved.isPossuiVe());
        assertFalse(saved.isPossuiIncendio());
        assertEquals("Recife", saved.getMunicipio());
        assertEquals("50030-000", saved.getCep());
    }

    @Test
    @DisplayName("Deve listar apenas os projetos pertencentes ao usuário autenticado")
    void shouldListOnlyProjectsOfAuthenticatedUser() {
        Project p1 = new Project();
        p1.setId(1L);
        p1.setProtocolo("PRJ-2026-0001");
        p1.setTitulo("Projeto 1");
        p1.setUser(user);
        p1.setTensaoFornecimento("380_220V");
        p1.setCreatedAt(LocalDateTime.now().minusHours(1));

        Project p2 = new Project();
        p2.setId(2L);
        p2.setProtocolo("PRJ-2026-0002");
        p2.setTitulo("Projeto 2");
        p2.setUser(user);
        p2.setTensaoFornecimento("220_127V");
        p2.setCreatedAt(LocalDateTime.now());

        when(projectRepository.findAllByUserIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(p2, p1));

        List<ProjectResponse> result = projectService.listByUser(10L);

        assertEquals(2, result.size());
        assertEquals("PRJ-2026-0002", result.get(0).getProtocolo());
        assertEquals("PRJ-2026-0001", result.get(1).getProtocolo());
        verify(projectRepository).findAllByUserIdOrderByCreatedAtDesc(10L);
    }

    @Test
    @DisplayName("Deve listar projetos com paginação, filtro de status e termo de busca")
    void shouldListProjectsWithPaginationAndFilters() {
        Project p = new Project();
        p.setId(1L);
        p.setProtocolo("PRJ-2026-0001");
        p.setTitulo("Edifício Solar Central");
        p.setUser(user);
        p.setStatus(ProjectStatus.RASCUNHO);
        p.setMunicipio("Recife");
        p.setTensaoFornecimento("380_220V");
        p.setCreatedAt(LocalDateTime.now());

        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        Page<Project> page = new PageImpl<>(List.of(p), pageable, 1);

        when(projectRepository.findByUserWithFilters(eq(10L), eq(List.of(ProjectStatus.RASCUNHO)), eq("Solar"), eq(pageable)))
                .thenReturn(page);

        Page<ProjectResponse> result = projectService.listByUserWithFilters(10L, List.of(ProjectStatus.RASCUNHO), "Solar", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("PRJ-2026-0001", result.getContent().get(0).getProtocolo());
        assertEquals("Edifício Solar Central", result.getContent().get(0).getTitulo());
        verify(projectRepository).findByUserWithFilters(10L, List.of(ProjectStatus.RASCUNHO), "Solar", pageable);
    }

    @Test
    @DisplayName("Deve listar projetos com termo de busca em branco passando null para o repository")
    void shouldListProjectsWithBlankSearchPassingNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> page = new PageImpl<>(List.of(), pageable, 0);

        when(projectRepository.findByUserWithFilters(eq(10L), any(), isNull(), eq(pageable)))
                .thenReturn(page);

        Page<ProjectResponse> result = projectService.listByUserWithFilters(10L, null, "   ", pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(projectRepository).findByUserWithFilters(eq(10L), any(), isNull(), eq(pageable));
    }

    @Test
    @DisplayName("Deve duplicar projeto com sucesso gerando novo protocolo e sufixo (cópia)")
    void shouldDuplicateProjectSuccessfully() {
        Project original = new Project();
        original.setId(50L);
        original.setProtocolo("PRJ-2026-0001");
        original.setTitulo("Projeto Matriz");
        original.setDescricao("Descrição detalhada");
        original.setTipo(ProjectType.LIGACAO_NOVA);
        original.setStatus(ProjectStatus.APROVADO);
        original.setEtapaAtual(4);
        original.setUser(user);
        original.setNormaTecnica("NBR 5410");
        original.setResponsavel("Engenheiro Teste");
        original.setMunicipio("Recife");
        original.setTensaoFornecimento("380_220V");
        original.setPossuiComercial(true);
        original.setPossuiVe(true);
        original.setPossuiIncendio(false);
        original.setCep("50000-000");
        original.setEndereco("Rua do Sol, 50");
        original.setBairro("Boa Vista");
        original.setCrea("CREA-12345");

        when(projectRepository.findById(50L)).thenReturn(Optional.of(original));
        int year = LocalDate.now().getYear();
        when(projectRepository.findLastProtocoloByPrefix("PRJ-" + year + "-%"))
                .thenReturn(Optional.of(String.format("PRJ-%d-0005", year)));

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project p = invocation.getArgument(0);
            p.setId(51L);
            return p;
        });

        ProjectResponse response = projectService.duplicateProject(50L, 10L);

        assertNotNull(response);
        assertEquals(51L, response.getId());
        assertEquals("Projeto Matriz (cópia)", response.getTitulo());
        assertEquals(String.format("PRJ-%d-0006", year), response.getProtocolo());
        assertEquals(ProjectStatus.RASCUNHO.name(), response.getStatus());
        assertEquals(1, response.getEtapaAtual());
        assertEquals("380_220V", response.getTensaoFornecimento());
        assertTrue(response.isPossuiComercial());
        assertTrue(response.isPossuiVe());
        assertFalse(response.isPossuiIncendio());
        assertEquals("Recife", response.getMunicipio());

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(captor.capture());
        Project saved = captor.getValue();
        assertEquals(user, saved.getUser());
        assertEquals("Projeto Matriz (cópia)", saved.getTitulo());
        assertEquals(ProjectStatus.RASCUNHO, saved.getStatus());
        assertEquals(1, saved.getEtapaAtual());
    }

    @Test
    @DisplayName("Deve lançar ForbiddenException ao tentar duplicar projeto pertencente a outro usuário")
    void shouldThrowForbiddenWhenDuplicatingOtherUsersProject() {
        User outroUser = new User("Outro Engenheiro", "outro@teste.com", "senha123");
        outroUser.setId(99L);

        Project original = new Project();
        original.setId(50L);
        original.setUser(outroUser);

        when(projectRepository.findById(50L)).thenReturn(Optional.of(original));

        ForbiddenException ex = assertThrows(ForbiddenException.class, () ->
                projectService.duplicateProject(50L, 10L)
        );

        assertTrue(ex.getMessage().contains("Acesso negado"));
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar duplicar projeto inexistente")
    void shouldThrowNotFoundWhenDuplicatingNonExistentProject() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                projectService.duplicateProject(999L, 10L)
        );
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Deve retornar detalhes do projeto quando pertencer ao usuário logado")
    void shouldReturnProjectDetailsWhenOwnedByUser() {
        Project project = new Project();
        project.setId(50L);
        project.setProtocolo("PRJ-2026-0050");
        project.setTitulo("Projeto Solar");
        project.setUser(user);
        project.setTensaoFornecimento("380_220V");
        project.setMunicipio("Olinda");

        when(projectRepository.findById(50L)).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.getProjectById(50L, 10L);

        assertNotNull(response);
        assertEquals(50L, response.getId());
        assertEquals("PRJ-2026-0050", response.getProtocolo());
        assertEquals("Projeto Solar", response.getTitulo());
    }

    @Test
    @DisplayName("Deve lançar ForbiddenException ao tentar consultar projeto de outro usuário")
    void shouldThrowForbiddenWhenAccessingOtherUsersProject() {
        User outroUser = new User("Outro Engenheiro", "outro@teste.com", "senha123");
        outroUser.setId(99L);

        Project project = new Project();
        project.setId(50L);
        project.setUser(outroUser);

        when(projectRepository.findById(50L)).thenReturn(Optional.of(project));

        ForbiddenException ex = assertThrows(ForbiddenException.class, () ->
                projectService.getProjectById(50L, 10L)
        );

        assertTrue(ex.getMessage().contains("Acesso negado"));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando projeto não existir")
    void shouldThrowNotFoundWhenProjectDoesNotExist() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                projectService.getProjectById(999L, 10L)
        );
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando título for ausente ou inválido")
    void shouldValidateTitulo() {
        CreateProjectRequest reqSemTitulo = new CreateProjectRequest();
        reqSemTitulo.setTipoEdificacao("RESIDENCIAL_MULTIFAMILIAR");
        reqSemTitulo.setTensaoFornecimento("380_220V");
        reqSemTitulo.setMunicipio("Recife");

        ValidationException ex1 = assertThrows(ValidationException.class, () ->
                projectService.createProject(reqSemTitulo, 10L)
        );
        assertTrue(ex1.getErrors().containsKey("titulo"));

        CreateProjectRequest reqTituloCurto = new CreateProjectRequest();
        reqTituloCurto.setTipoEdificacao("RESIDENCIAL_MULTIFAMILIAR");
        reqTituloCurto.setTitulo("AB");
        reqTituloCurto.setTensaoFornecimento("380_220V");
        reqTituloCurto.setMunicipio("Recife");

        ValidationException ex2 = assertThrows(ValidationException.class, () ->
                projectService.createProject(reqTituloCurto, 10L)
        );
        assertTrue(ex2.getErrors().containsKey("titulo"));
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando tensão de fornecimento for inválida")
    void shouldValidateTensaoFornecimento() {
        CreateProjectRequest reqTensaoInvalida = new CreateProjectRequest();
        reqTensaoInvalida.setTipoEdificacao("RESIDENCIAL_MULTIFAMILIAR");
        reqTensaoInvalida.setTitulo("Projeto Residencial");
        reqTensaoInvalida.setTensaoFornecimento("110V_INVALIDA");
        reqTensaoInvalida.setMunicipio("Recife");

        ValidationException ex = assertThrows(ValidationException.class, () ->
                projectService.createProject(reqTensaoInvalida, 10L)
        );
        assertTrue(ex.getErrors().containsKey("tensaoFornecimento"));
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando município for ausente")
    void shouldValidateMunicipio() {
        CreateProjectRequest reqSemMunicipio = new CreateProjectRequest();
        reqSemMunicipio.setTipoEdificacao("RESIDENCIAL_MULTIFAMILIAR");
        reqSemMunicipio.setTitulo("Projeto Residencial");
        reqSemMunicipio.setTensaoFornecimento("380_220V");

        ValidationException ex = assertThrows(ValidationException.class, () ->
                projectService.createProject(reqSemMunicipio, 10L)
        );
        assertTrue(ex.getErrors().containsKey("municipio"));
    }

    @Test
    @DisplayName("Deve salvar etapa residencial com sucesso, excluir registros anteriores, avançar etapa e retornar resposta consolidada")
    void shouldSaveResidentialStageSuccessfully() {
        Project project = new Project();
        project.setId(100L);
        project.setUser(user);
        project.setEtapaAtual(1);

        ResidentialTypologyItemRequest item1 = new ResidentialTypologyItemRequest(new BigDecimal("65.00"), 50, false);
        ResidentialTypologyItemRequest item2 = new ResidentialTypologyItemRequest(new BigDecimal("78.00"), 50, false);
        ResidentialStageRequest request = new ResidentialStageRequest(List.of(item1, item2));

        ResidentialTypologyOutput out1 = new ResidentialTypologyOutput("Tipologia 65.00m²", new BigDecimal("65.00"), 50, new BigDecimal("1.47"), new BigDecimal("73.50"));
        ResidentialTypologyOutput out2 = new ResidentialTypologyOutput("Tipologia 78.00m²", new BigDecimal("78.00"), 50, new BigDecimal("1.80"), new BigDecimal("90.00"));
        ResidentialCalculationResult calcResult = new ResidentialCalculationResult(
                100,
                new BigDecimal("163.50"),
                new BigDecimal("0.6823"),
                new BigDecimal("111.56"),
                new BigDecimal("1.10"),
                new BigDecimal("122.72"),
                false,
                List.of(out1, out2)
        );

        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(residentialCalculationEngine.calculate(anyList())).thenReturn(calcResult);

        ResidentialStageResponse response = projectService.saveResidentialStage(100L, request, 10L);

        assertNotNull(response);
        assertTrue(response.isSucesso());
        assertEquals(100L, response.getProjectId());
        assertEquals(new BigDecimal("122.72"), response.getDemandaResidencialFinalDrfKva());

        verify(residentialTypologyRepository).deleteByProjectId(100L);
        verify(residentialTypologyRepository).flush();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ResidentialTypology>> captor = ArgumentCaptor.forClass(List.class);
        verify(residentialTypologyRepository).saveAll(captor.capture());
        List<ResidentialTypology> savedList = captor.getValue();
        assertEquals(2, savedList.size());

        ResidentialTypology saved1 = savedList.get(0);
        assertEquals(project, saved1.getProject());
        assertEquals("Tipologia 65.00m²", saved1.getNome());
        assertNull(saved1.getCatalogoFaixaId());
        assertEquals(new BigDecimal("65.00"), saved1.getAreaUtilM2());
        assertEquals(50, saved1.getQuantidadeUnidades());
        assertFalse(saved1.isSmartStudio());
        assertEquals(new BigDecimal("1.47"), saved1.getDemandaUnitariaKva());
        assertEquals(new BigDecimal("73.50"), saved1.getDemandaTotalTipologiaKva());

        ResidentialTypology saved2 = savedList.get(1);
        assertEquals("Tipologia 78.00m²", saved2.getNome());
        assertEquals(new BigDecimal("1.80"), saved2.getDemandaUnitariaKva());
        assertEquals(new BigDecimal("90.00"), saved2.getDemandaTotalTipologiaKva());

        assertEquals(2, project.getEtapaAtual());
        assertNotNull(project.getUpdatedAt());
        verify(projectRepository).save(project);
    }

    @Test
    @DisplayName("Deve preservar etapaAtual superior quando projeto já estiver em etapa mais avançada")
    void shouldPreserveHigherStageWhenUpdatingResidentialStage() {
        Project project = new Project();
        project.setId(100L);
        project.setUser(user);
        project.setEtapaAtual(4);

        ResidentialTypologyItemRequest item = new ResidentialTypologyItemRequest(new BigDecimal("35.00"), 20, true);
        ResidentialStageRequest request = new ResidentialStageRequest(List.of(item));

        ResidentialTypologyOutput out = new ResidentialTypologyOutput("Tipologia 35.00m²", new BigDecimal("35.00"), 20, new BigDecimal("1.20"), new BigDecimal("24.00"));
        ResidentialCalculationResult calcResult = new ResidentialCalculationResult(
                20,
                new BigDecimal("24.00"),
                new BigDecimal("0.9000"),
                new BigDecimal("21.60"),
                new BigDecimal("1.50"),
                new BigDecimal("32.40"),
                true,
                List.of(out)
        );

        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(residentialCalculationEngine.calculate(anyList())).thenReturn(calcResult);

        ResidentialStageResponse response = projectService.saveResidentialStage(100L, request, 10L);

        assertNotNull(response);
        assertTrue(response.isSucesso());
        assertEquals(4, project.getEtapaAtual());
        verify(projectRepository).save(project);
    }

    @Test
    @DisplayName("Deve lançar ForbiddenException ao tentar salvar tipologias em projeto de outro usuário")
    void shouldThrowForbiddenWhenUserIsNotOwnerOnResidentialStage() {
        User outro = new User("Outro Projetista", "outro@teste.com", "senha");
        outro.setId(99L);

        Project project = new Project();
        project.setId(100L);
        project.setUser(outro);

        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));

        ResidentialStageRequest request = new ResidentialStageRequest(List.of(
                new ResidentialTypologyItemRequest(new BigDecimal("50.00"), 10, false)
        ));

        ForbiddenException ex = assertThrows(ForbiddenException.class, () ->
                projectService.saveResidentialStage(100L, request, 10L)
        );
        assertTrue(ex.getMessage().contains("Acesso negado"));
        verify(residentialTypologyRepository, never()).deleteByProjectId(anyLong());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando projeto não existir na etapa residencial")
    void shouldThrowNotFoundWhenProjectDoesNotExistOnResidentialStage() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        ResidentialStageRequest request = new ResidentialStageRequest(List.of(
                new ResidentialTypologyItemRequest(new BigDecimal("50.00"), 10, false)
        ));

        assertThrows(ResourceNotFoundException.class, () ->
                projectService.saveResidentialStage(999L, request, 10L)
        );
        verify(residentialTypologyRepository, never()).deleteByProjectId(anyLong());
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando payload da etapa residencial for inválido")
    void shouldThrowValidationExceptionWhenPayloadIsInvalidOnResidentialStage() {
        // 1. Request nula / lista vazia
        ResidentialStageRequest reqVazia = new ResidentialStageRequest(List.of());
        ValidationException exVazia = assertThrows(ValidationException.class, () ->
                projectService.saveResidentialStage(100L, reqVazia, 10L)
        );
        assertTrue(exVazia.getErrors().containsKey("tipologias"));

        // 2. Área útil negativa ou zero
        ResidentialStageRequest reqAreaInvalida = new ResidentialStageRequest(List.of(
                new ResidentialTypologyItemRequest(new BigDecimal("-5.00"), 10, false)
        ));
        ValidationException exArea = assertThrows(ValidationException.class, () ->
                projectService.saveResidentialStage(100L, reqAreaInvalida, 10L)
        );
        assertTrue(exArea.getErrors().containsKey("tipologias[0].areaUtilM2"));

        // 3. Quantidade zero ou negativa
        ResidentialStageRequest reqQuantInvalida = new ResidentialStageRequest(List.of(
                new ResidentialTypologyItemRequest(new BigDecimal("50.00"), 0, false)
        ));
        ValidationException exQuant = assertThrows(ValidationException.class, () ->
                projectService.saveResidentialStage(100L, reqQuantInvalida, 10L)
        );
        assertTrue(exQuant.getErrors().containsKey("tipologias[0].quantidadeUnidades"));
    }

    @Test
    @DisplayName("updateProjectPatch deve atualizar dados cadastrais mantendo o status")
    void updateProjectPatch_dadosCadastrais_sucesso() {
        Project proj = new Project();
        proj.setId(100L);
        proj.setUser(user);
        proj.setTitulo("Titulo Antigo");
        proj.setMunicipio("Recife");
        proj.setTensaoFornecimento("380_220V");
        proj.setStatus(ProjectStatus.REPROVADO);
        proj.setAtivo(true);

        when(projectRepository.findById(100L)).thenReturn(Optional.of(proj));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectUpdatePatchRequest request = new ProjectUpdatePatchRequest();
        request.setTitulo("Titulo Novo Atualizado");
        request.setEndereco("Rua Nova, 123");

        ProjectResponse response = projectService.updateProjectPatch(100L, request, 10L);

        assertEquals("Titulo Novo Atualizado", response.getTitulo());
        assertEquals("Rua Nova, 123", response.getEndereco());
        assertEquals(ProjectStatus.REPROVADO.name(), response.getStatus());
        verify(projectRepository).save(proj);
    }

    @Test
    @DisplayName("updateProjectPatch com alteração de carga deve invalidar status para RASCUNHO")
    void updateProjectPatch_invalidaStatus_quandoCargaAlterada() {
        Project proj = new Project();
        proj.setId(100L);
        proj.setUser(user);
        proj.setTitulo("Projeto Inicial");
        proj.setMunicipio("Recife");
        proj.setTensaoFornecimento("380_220V");
        proj.setStatus(ProjectStatus.REPROVADO);
        proj.setPossuiVe(false);
        proj.setAtivo(true);

        when(projectRepository.findById(100L)).thenReturn(Optional.of(proj));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectUpdatePatchRequest request = new ProjectUpdatePatchRequest();
        request.setPossuiVe(true);

        ProjectResponse response = projectService.updateProjectPatch(100L, request, 10L);

        assertTrue(response.isPossuiVe());
        assertEquals(ProjectStatus.RASCUNHO.name(), response.getStatus());
        verify(projectRepository).save(proj);
    }

    @Test
    @DisplayName("updateProjectPatch por outro projetista deve lançar ForbiddenException")
    void updateProjectPatch_outroUsuario_lancaForbidden() {
        Project proj = new Project();
        proj.setId(100L);
        proj.setUser(user);
        proj.setAtivo(true);

        when(projectRepository.findById(100L)).thenReturn(Optional.of(proj));

        ProjectUpdatePatchRequest request = new ProjectUpdatePatchRequest();
        request.setTitulo("Tentativa Invasiva");

        assertThrows(ForbiddenException.class, () -> projectService.updateProjectPatch(100L, request, 999L));
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProjectPatch com projeto inativo deve lançar ResourceNotFoundException")
    void updateProjectPatch_projetoInativo_lancaNotFound() {
        Project proj = new Project();
        proj.setId(100L);
        proj.setUser(user);
        proj.setAtivo(false);

        when(projectRepository.findById(100L)).thenReturn(Optional.of(proj));

        ProjectUpdatePatchRequest request = new ProjectUpdatePatchRequest();
        request.setTitulo("Projeto Inativo");

        assertThrows(ResourceNotFoundException.class, () -> projectService.updateProjectPatch(100L, request, 10L));
    }

    @Test
    @DisplayName("deleteProject deve marcar ativo = false")
    void deleteProject_sucesso() {
        Project proj = new Project();
        proj.setId(100L);
        proj.setUser(user);
        proj.setAtivo(true);

        when(projectRepository.findById(100L)).thenReturn(Optional.of(proj));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        projectService.deleteProject(100L, 10L);

        assertFalse(proj.isAtivo());
        verify(projectRepository).save(proj);
    }

    @Test
    @DisplayName("deleteProject por outro usuário deve lançar ForbiddenException")
    void deleteProject_outroUsuario_lancaForbidden() {
        Project proj = new Project();
        proj.setId(100L);
        proj.setUser(user);
        proj.setAtivo(true);

        when(projectRepository.findById(100L)).thenReturn(Optional.of(proj));

        assertThrows(ForbiddenException.class, () -> projectService.deleteProject(100L, 999L));
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("B-EP-10 (D1): projeto EM_ANALISE não pode ser excluído (somente RASCUNHO)")
    void deleteProject_emAnalise_lancaConflito() {
        Project proj = new Project();
        proj.setId(100L);
        proj.setUser(user);
        proj.setAtivo(true);
        proj.setStatus(neoguide.project.model.ProjectStatus.EM_ANALISE);

        when(projectRepository.findById(100L)).thenReturn(Optional.of(proj));

        assertThrows(neoguide.project.exception.ConflictException.class, () -> projectService.deleteProject(100L, 10L));
        assertTrue(proj.isAtivo());
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("B-EP-10 (D1): projeto APROVADO e REPROVADO também não podem ser excluídos")
    void deleteProject_aprovadoOuReprovado_lancaConflito() {
        for (neoguide.project.model.ProjectStatus status : List.of(
                neoguide.project.model.ProjectStatus.APROVADO,
                neoguide.project.model.ProjectStatus.REPROVADO)) {
            Project proj = new Project();
            proj.setId(100L);
            proj.setUser(user);
            proj.setAtivo(true);
            proj.setStatus(status);
            when(projectRepository.findById(100L)).thenReturn(Optional.of(proj));

            assertThrows(neoguide.project.exception.ConflictException.class, () -> projectService.deleteProject(100L, 10L));
            assertTrue(proj.isAtivo());
        }
    }

    @Test
    @DisplayName("Deve salvar etapa de áreas comuns com sucesso para payload híbrido")
    void deveSalvarEtapaAreaComumComSucesso_PayloadHibrido() {
        Project proj = new Project();
        proj.setId(100L);
        proj.setUser(user);
        proj.setAtivo(true);
        proj.setEtapaAtual(1);

        when(projectRepository.findById(100L)).thenReturn(Optional.of(proj));

        CommonAreaLoadItemRequest motor = new CommonAreaLoadItemRequest(
                "CATALOGO", "MOTORES", 1L, null, null,
                "Bomba de Recalque", "Bomba 5cv", 1,
                new BigDecimal("3.68"), new BigDecimal("0.85"), new BigDecimal("1.00"), true
        );
        CommonAreaLoadItemRequest cooktop = new CommonAreaLoadItemRequest(
                "CATALOGO", "COCCAO", null, null, 2L,
                "Cooktop de Indução", "Cooktop 4 bocas", 2,
                new BigDecimal("7.00"), new BigDecimal("1.00"), new BigDecimal("0.60"), false
        );
        CommonAreaLoadItemRequest exaustor = new CommonAreaLoadItemRequest(
                "PERSONALIZADO", "CLIMATIZACAO", null, null, null,
                "Exaustor Especial", "Exaustor customizado", 1,
                new BigDecimal("2.50"), new BigDecimal("0.85"), new BigDecimal("0.80"), false
        );

        CommonAreaStageRequest request = new CommonAreaStageRequest(List.of(motor, cooktop, exaustor));

        CommonAreaSubtotal mockSub = new CommonAreaSubtotal(
                new BigDecimal("10.50"), BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ONE, "Memória"
        );
        CommonAreaCalculationResult calcResult = new CommonAreaCalculationResult(
                new BigDecimal("10.50"),
                mockSub, mockSub, mockSub, mockSub, mockSub, mockSub, mockSub, mockSub, mockSub, mockSub,
                List.of(BigDecimal.ZERO, new BigDecimal("8.40"), new BigDecimal("2.35"))
        );
        when(commonAreaCalculationEngine.calculate(anyList())).thenReturn(calcResult);

        CommonAreaStageResponse response = projectService.saveCommonAreaStage(100L, request, 10L);

        assertTrue(response.isSucesso());
        assertEquals(100L, response.getProjectId());
        assertEquals(3, response.getEtapaAtual());
        assertEquals(3, response.getTotalCargas());
        assertEquals(new BigDecimal("10.50"), response.getDemandaServicosDsKva());

        verify(commonAreaLoadRepository).deleteByProjectId(100L);
        verify(commonAreaLoadRepository).flush();
        verify(commonAreaLoadRepository).saveAll(anyList());
        verify(projectRepository).save(proj);
    }

    @Test
    @DisplayName("Deve garantir idempotência ao salvar duas vezes consecutivas")
    void deveGarantirIdempotenciaAoSalvarDuasVezes() {
        Project proj = new Project();
        proj.setId(100L);
        proj.setUser(user);
        proj.setAtivo(true);
        proj.setEtapaAtual(2);

        when(projectRepository.findById(100L)).thenReturn(Optional.of(proj));

        CommonAreaLoadItemRequest item = new CommonAreaLoadItemRequest(
                "PERSONALIZADO", "MOTORES", null, null, null,
                "Motor", "Motor teste", 1,
                new BigDecimal("1.50"), new BigDecimal("0.85"), new BigDecimal("1.00"), false
        );
        CommonAreaStageRequest request = new CommonAreaStageRequest(List.of(item));

        CommonAreaSubtotal mockSub = new CommonAreaSubtotal(
                new BigDecimal("1.76"), BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ONE, "Memória"
        );
        CommonAreaCalculationResult calcResult = new CommonAreaCalculationResult(
                new BigDecimal("1.76"),
                mockSub, mockSub, mockSub, mockSub, mockSub, mockSub, mockSub, mockSub, mockSub, mockSub,
                List.of(new BigDecimal("1.76"))
        );
        when(commonAreaCalculationEngine.calculate(anyList())).thenReturn(calcResult);

        projectService.saveCommonAreaStage(100L, request, 10L);
        projectService.saveCommonAreaStage(100L, request, 10L);

        verify(commonAreaLoadRepository, times(2)).deleteByProjectId(100L);
        verify(commonAreaLoadRepository, times(2)).flush();
        verify(commonAreaLoadRepository, times(2)).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando item personalizado tiver potência inválida")
    void deveLancarValidationExceptionQuandoItemPersonalizadoComPotenciaInvalida() {
        CommonAreaLoadItemRequest item = new CommonAreaLoadItemRequest(
                "PERSONALIZADO", "MOTORES", null, null, null,
                "Motor", "Desc", 1,
                BigDecimal.ZERO, new BigDecimal("0.85"), new BigDecimal("1.00"), false
        );
        CommonAreaStageRequest request = new CommonAreaStageRequest(List.of(item));

        assertThrows(ValidationException.class, () -> projectService.saveCommonAreaStage(100L, request, 10L));
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando fator de potência estiver fora dos limites")
    void deveLancarValidationExceptionQuandoFatorPotenciaForaDosLimites() {
        CommonAreaLoadItemRequest item = new CommonAreaLoadItemRequest(
                "PERSONALIZADO", "MOTORES", null, null, null,
                "Motor", "Desc", 1,
                new BigDecimal("5.0"), new BigDecimal("1.50"), new BigDecimal("1.00"), false
        );
        CommonAreaStageRequest request = new CommonAreaStageRequest(List.of(item));

        assertThrows(ValidationException.class, () -> projectService.saveCommonAreaStage(100L, request, 10L));
    }

    @Test
    @DisplayName("Deve lançar ForbiddenException quando projeto pertencer a outro usuário")
    void deveLancarForbiddenExceptionQuandoProjetoNaoPertenceAoUsuario() {
        Project proj = new Project();
        proj.setId(100L);
        User outro = new User("Outro", "outro@teste.com", "123");
        outro.setId(99L);
        proj.setUser(outro);
        proj.setAtivo(true);

        when(projectRepository.findById(100L)).thenReturn(Optional.of(proj));

        CommonAreaLoadItemRequest item = new CommonAreaLoadItemRequest(
                "PERSONALIZADO", "MOTORES", null, null, null,
                "Motor", "Desc", 1,
                new BigDecimal("1.0"), new BigDecimal("0.85"), new BigDecimal("1.00"), false
        );
        CommonAreaStageRequest request = new CommonAreaStageRequest(List.of(item));

        assertThrows(ForbiddenException.class, () -> projectService.saveCommonAreaStage(100L, request, 10L));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando projeto não existir")
    void deveLancarResourceNotFoundExceptionQuandoProjetoNaoExiste() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        CommonAreaLoadItemRequest item = new CommonAreaLoadItemRequest(
                "PERSONALIZADO", "MOTORES", null, null, null,
                "Motor", "Desc", 1,
                new BigDecimal("1.0"), new BigDecimal("0.85"), new BigDecimal("1.00"), false
        );
        CommonAreaStageRequest request = new CommonAreaStageRequest(List.of(item));

        assertThrows(ResourceNotFoundException.class, () -> projectService.saveCommonAreaStage(999L, request, 10L));
    }

    @Test
    @DisplayName("deleteProject com projeto inexistente deve lançar ResourceNotFoundException")
    void deleteProject_inexistente_lancaNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.deleteProject(999L, 10L));
    }

    @Test
    @DisplayName("saveCommercialStage com sucesso deve gravar os espaços e avançar o projeto para etapa 4")
    void saveCommercialStage_comSucesso_salvaEspacosEAtualizaEtapa() {
        Project project = new Project();
        project.setId(1L);
        project.setUser(user);
        project.setEtapaAtual(2);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        CommercialUnitResult unidade = new CommercialUnitResult("LOJAS", "Lojas e Vitrines", new BigDecimal("50.00"),
                new BigDecimal("25.00"), new BigDecimal("1.0000"), new BigDecimal("27.17"), new BigDecimal("1.50"), null);
        when(commercialStageService.calcular(anyList())).thenReturn(
                new CommercialCalculationResult(new BigDecimal("25.00"), new BigDecimal("27.17"), List.of(unidade), "memória"));
        when(commercialUnitRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        CommercialStageRequest request = new CommercialStageRequest(
                List.of(new EspacoComercialRequest("LOJAS", new BigDecimal("50.00"), new BigDecimal("25.00"))));

        CommercialStageResponse response = projectService.saveCommercialStage(1L, request, 10L);

        assertTrue(response.isSucesso());
        assertEquals(4, response.getEtapaAtual());
        assertEquals(1, response.getTotalEspacos());
        assertEquals(new BigDecimal("27.17"), response.getDemandaComercialDcKva());
        assertEquals("LOJAS", response.getEspacos().get(0).getOcupacaoCodigo());
        assertEquals(1, response.getEspacos().get(0).getOrdem());
        assertTrue(project.isPossuiComercial());
        verify(commercialStageService).validarEspacos(anyList());
        verify(commercialUnitRepository).deleteByProjectId(1L);
        verify(commercialUnitRepository).saveAll(anyList());
        verify(projectRepository).save(project);
    }

    @Test
    @DisplayName("saveCommercialStage sem espaços deve zerar o comercial e manter etapa 4")
    void saveCommercialStage_semEspacos_zeraComercial() {
        Project project = new Project();
        project.setId(1L);
        project.setUser(user);
        project.setEtapaAtual(1);
        project.setPossuiComercial(true);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(commercialStageService.calcular(anyList())).thenReturn(
                new CommercialCalculationResult(BigDecimal.ZERO, BigDecimal.ZERO, List.of(), "Sem espaços"));
        when(commercialUnitRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        CommercialStageResponse response = projectService.saveCommercialStage(1L,
                new CommercialStageRequest(List.of()), 10L);

        assertEquals(4, response.getEtapaAtual());
        assertEquals(0, response.getTotalEspacos());
        assertEquals(BigDecimal.ZERO, response.getDemandaComercialDcKva());
        assertFalse(project.isPossuiComercial());
        verify(commercialUnitRepository).deleteByProjectId(1L);
    }

    @Test
    @DisplayName("saveCommercialStage para projeto de outro usuário deve lançar ForbiddenException")
    void saveCommercialStage_projetoOutroUsuario_lancaForbidden() {
        User outro = new User("Outro", "outro@teste.com", "senha");
        outro.setId(99L);
        Project project = new Project();
        project.setId(1L);
        project.setUser(outro);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        CommercialStageRequest request = new CommercialStageRequest(List.of());

        assertThrows(ForbiddenException.class, () -> projectService.saveCommercialStage(1L, request, 10L));
    }

    @Test
    @DisplayName("saveCommercialStage para projeto inexistente deve lançar ResourceNotFoundException")
    void saveCommercialStage_projetoInexistente_lancaNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        CommercialStageRequest request = new CommercialStageRequest(List.of());

        assertThrows(ResourceNotFoundException.class, () -> projectService.saveCommercialStage(999L, request, 10L));
    }

    @Test
    @DisplayName("saveCommercialStage com dados inválidos deve lançar ValidationException antes de gravar")
    void saveCommercialStage_dadosInvalidos_lancaValidationException() {
        doThrow(new ValidationException(Map.of("espacos[0].cargaInstaladaKw", "inválida")))
                .when(commercialStageService).validarEspacos(anyList());
        CommercialStageRequest request = new CommercialStageRequest(
                List.of(new EspacoComercialRequest("LOJAS", new BigDecimal("50.00"), new BigDecimal("-5.00"))));

        assertThrows(ValidationException.class, () -> projectService.saveCommercialStage(1L, request, 10L));
        verify(commercialUnitRepository, never()).deleteByProjectId(anyLong());
    }

    @Test
    @DisplayName("saveSpecialLoadsStage criando novo registro deve salvar SpecialLoads e avançar projeto para etapa 5")
    void saveSpecialLoadsStage_novoRegistro_sucesso() {
        Project project = new Project();
        project.setId(1L);
        project.setUser(user);
        project.setEtapaAtual(3);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(specialLoadsRepository.findByProjectId(1L)).thenReturn(Optional.empty());

        ElectricVehicleLoadOutput veOut = new ElectricVehicleLoadOutput(
                4, new BigDecimal("7.40"), new BigDecimal("29.60"),
                new BigDecimal("0.70"), new BigDecimal("1.00"), new BigDecimal("20.72")
        );
        FirePumpLoadOutput mbiOut = new FirePumpLoadOutput(
                new BigDecimal("15.00"), new BigDecimal("14.40"), true, "ESTRELA_TRIANGULO", "DIS-NOR-030"
        );
        when(specialLoadsCalculationEngine.calculate(any())).thenReturn(
                new SpecialLoadsCalculationResult(
                        veOut, mbiOut, new BigDecimal("35.12"), new BigDecimal("14.40"),
                        new BigDecimal("20.72"), false, null, "Auditoria teste"
                )
        );

        SpecialLoadsStageRequest request = new SpecialLoadsStageRequest(4, new BigDecimal("7.40"), new BigDecimal("15.00"), null);

        SpecialLoadsStageResponse response = projectService.saveSpecialLoadsStage(1L, request, 10L);

        assertNotNull(response);
        assertTrue(response.isSucesso());
        assertEquals(5, response.getEtapaAtual());
        assertEquals(new BigDecimal("20.72"), response.getDemandaVeDveKva());
        assertEquals(new BigDecimal("14.40"), response.getDemandaIncendioDmbiKva());
        assertTrue(project.isPossuiVe());
        assertTrue(project.isPossuiIncendio());

        ArgumentCaptor<SpecialLoads> captor = ArgumentCaptor.forClass(SpecialLoads.class);
        verify(specialLoadsRepository).save(captor.capture());
        SpecialLoads saved = captor.getValue();
        assertEquals(4, saved.getQtdPontosVe());
        assertEquals(new BigDecimal("20.72"), saved.getDemandaVeKva());
        verify(projectRepository).save(project);
    }

    @Test
    @DisplayName("saveSpecialLoadsStage atualizando registro existente (upsert) deve manter integridade e avançar etapa 5")
    void saveSpecialLoadsStage_atualizandoExistente_upsertSucesso() {
        Project project = new Project();
        project.setId(1L);
        project.setUser(user);
        project.setEtapaAtual(4);

        SpecialLoads existente = new SpecialLoads();
        existente.setId(50L);
        existente.setProject(project);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(specialLoadsRepository.findByProjectId(1L)).thenReturn(Optional.of(existente));

        ElectricVehicleLoadOutput veOut = new ElectricVehicleLoadOutput(
                0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO
        );
        FirePumpLoadOutput mbiOut = new FirePumpLoadOutput(
                BigDecimal.ZERO, BigDecimal.ZERO, true, "N/A", "DIS-NOR-030"
        );
        when(specialLoadsCalculationEngine.calculate(any())).thenReturn(
                new SpecialLoadsCalculationResult(
                        veOut, mbiOut, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, false, null, "Sem cargas"
                )
        );

        SpecialLoadsStageRequest request = new SpecialLoadsStageRequest(0, null, null, null);

        SpecialLoadsStageResponse response = projectService.saveSpecialLoadsStage(1L, request, 10L);

        assertNotNull(response);
        assertTrue(response.isSucesso());
        assertEquals(5, response.getEtapaAtual());
        assertFalse(project.isPossuiVe());
        assertFalse(project.isPossuiIncendio());
        verify(specialLoadsRepository).save(existente);
    }

    @Test
    @DisplayName("saveSpecialLoadsStage para projeto de outro usuário deve lançar ForbiddenException")
    void saveSpecialLoadsStage_outroUsuario_lancaForbidden() {
        User outro = new User("Outro", "outro@teste.com", "senha");
        outro.setId(99L);
        Project project = new Project();
        project.setId(1L);
        project.setUser(outro);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        SpecialLoadsStageRequest request = new SpecialLoadsStageRequest();

        assertThrows(ForbiddenException.class, () -> projectService.saveSpecialLoadsStage(1L, request, 10L));
    }

    @Test
    @DisplayName("saveSpecialLoadsStage com vagas de VE sem potência deve lançar ValidationException")
    void saveSpecialLoadsStage_vagasSemPotencia_lancaValidationException() {
        SpecialLoadsStageRequest request = new SpecialLoadsStageRequest(4, null, null, null);
        assertThrows(ValidationException.class, () -> projectService.saveSpecialLoadsStage(1L, request, 10L));
    }

    @Test
    @DisplayName("PATCH em projeto EM_ANALISE deve ser recusado (somente leitura, D1)")
    void updateProjectPatch_emAnalise_lancaConflito() {
        Project proj = new Project();
        proj.setId(100L);
        proj.setUser(user);
        proj.setTitulo("Projeto em análise");
        proj.setAtivo(true);
        proj.setStatus(ProjectStatus.EM_ANALISE);
        when(projectRepository.findById(100L)).thenReturn(Optional.of(proj));

        ProjectUpdatePatchRequest patch = new ProjectUpdatePatchRequest();
        patch.setTitulo("Novo nome");

        assertThrows(neoguide.project.exception.ConflictException.class,
                () -> projectService.updateProjectPatch(100L, patch, 10L));
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Totais: contam por situação; a soma é o total de todos")
    void totais_contaPorSituacao() {
        when(projectRepository.contarPorStatus(eq(10L), isNull())).thenReturn(List.of(
                new Object[]{ProjectStatus.RASCUNHO, 3L},
                new Object[]{ProjectStatus.EM_ANALISE, 2L},
                new Object[]{ProjectStatus.AGUARDANDO_CORRECAO, 1L},
                new Object[]{ProjectStatus.APROVADO, 4L},
                new Object[]{ProjectStatus.REPROVADO, 1L}));

        neoguide.project.dto.TotaisProjetosResponse totais = projectService.totais(10L, "  ");

        assertEquals(11L, totais.todos());
        assertEquals(3L, totais.rascunho());
        assertEquals(2L, totais.emAnalise());
        assertEquals(1L, totais.aguardandoCorrecao());
        assertEquals(4L, totais.aprovado());
        assertEquals(1L, totais.reprovado());
    }

    @Test
    @DisplayName("Duplicação copia as etapas do projeto original (B-EP-11)")
    void duplicateProject_copiaEtapas() {
        Project original = new Project();
        original.setId(50L);
        original.setUser(user);
        original.setTitulo("Original");
        original.setTipoEdificacao(neoguide.project.model.TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR);
        original.setStatus(ProjectStatus.APROVADO);
        original.setAtivo(true);
        when(projectRepository.findById(50L)).thenReturn(Optional.of(original));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> {
            Project p = inv.getArgument(0);
            if (p.getId() == null) p.setId(51L);
            return p;
        });
        neoguide.project.model.ResidentialTypology tipologia = new neoguide.project.model.ResidentialTypology(
                original, "Tipo A", new BigDecimal("63.00"), 50, false, new BigDecimal("1.47"), new BigDecimal("73.50"));
        when(residentialTypologyRepository.findByProjectIdOrderByIdAsc(50L)).thenReturn(List.of(tipologia));

        ProjectResponse copia = projectService.duplicateProject(50L, 10L);

        verify(residentialTypologyRepository, times(1)).save(any(neoguide.project.model.ResidentialTypology.class));
        assertEquals("RESIDENCIAL_MULTIFAMILIAR", copia.getTipoEdificacao());
        assertEquals(ProjectStatus.RASCUNHO.name(), copia.getStatus());
    }
}
