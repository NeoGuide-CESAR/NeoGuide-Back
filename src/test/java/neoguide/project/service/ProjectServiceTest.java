package neoguide.project.service;

import neoguide.project.dto.CreateProjectRequest;
import neoguide.project.dto.CreateProjectResponse;
import neoguide.project.dto.ProjectResponse;
import neoguide.project.dto.ResidentialStageRequest;
import neoguide.project.dto.ResidentialStageResponse;
import neoguide.project.dto.ResidentialTypologyItemRequest;
import neoguide.project.engine.ResidentialCalculationEngine;
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

        when(projectRepository.findByUserWithFilters(eq(10L), eq(ProjectStatus.RASCUNHO), eq("Solar"), eq(pageable)))
                .thenReturn(page);

        Page<ProjectResponse> result = projectService.listByUserWithFilters(10L, ProjectStatus.RASCUNHO, "Solar", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("PRJ-2026-0001", result.getContent().get(0).getProtocolo());
        assertEquals("Edifício Solar Central", result.getContent().get(0).getTitulo());
        verify(projectRepository).findByUserWithFilters(10L, ProjectStatus.RASCUNHO, "Solar", pageable);
    }

    @Test
    @DisplayName("Deve listar projetos com termo de busca em branco passando null para o repository")
    void shouldListProjectsWithBlankSearchPassingNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> page = new PageImpl<>(List.of(), pageable, 0);

        when(projectRepository.findByUserWithFilters(eq(10L), isNull(), isNull(), eq(pageable)))
                .thenReturn(page);

        Page<ProjectResponse> result = projectService.listByUserWithFilters(10L, null, "   ", pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(projectRepository).findByUserWithFilters(10L, null, null, pageable);
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
        reqSemTitulo.setTensaoFornecimento("380_220V");
        reqSemTitulo.setMunicipio("Recife");

        ValidationException ex1 = assertThrows(ValidationException.class, () ->
                projectService.createProject(reqSemTitulo, 10L)
        );
        assertTrue(ex1.getErrors().containsKey("titulo"));

        CreateProjectRequest reqTituloCurto = new CreateProjectRequest();
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
}
