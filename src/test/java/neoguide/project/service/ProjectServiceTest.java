package neoguide.project.service;

import neoguide.project.dto.CreateProjectRequest;
import neoguide.project.dto.CreateProjectResponse;
import neoguide.project.dto.ProjectResponse;
import neoguide.project.exception.ForbiddenException;
import neoguide.project.exception.ResourceNotFoundException;
import neoguide.project.exception.ValidationException;
import neoguide.project.model.Project;
import neoguide.project.model.ProjectStatus;
import neoguide.project.model.ProjectType;
import neoguide.project.model.User;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

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
}
