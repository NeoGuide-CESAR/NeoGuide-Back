package neoguide.project.service;

import java.math.BigDecimal;
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

import neoguide.project.dto.validation.NormativeValidationResponse;
import neoguide.project.exception.ForbiddenException;
import neoguide.project.exception.ResourceNotFoundException;
import neoguide.project.exception.UnprocessableEntityException;
import neoguide.project.model.DemandResults;
import neoguide.project.model.Project;
import neoguide.project.model.User;
import neoguide.project.repository.DemandResultsRepository;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.validator.NormativeRuleValidator;

class NormativeValidationServiceTest {

    private ProjectRepository projectRepository;
    private DemandResultsRepository demandResultsRepository;
    private NormativeRuleValidator normativeRuleValidator;
    private NormativeValidationService service;

    @BeforeEach
    void setUp() {
        projectRepository = mock(ProjectRepository.class);
        demandResultsRepository = mock(DemandResultsRepository.class);
        normativeRuleValidator = new NormativeRuleValidator();

        service = new NormativeValidationService(
                projectRepository,
                demandResultsRepository,
                normativeRuleValidator
        );
    }

    @Test
    @DisplayName("Deve validar projeto com sucesso e emitir selo de conformidade")
    void deveValidarComSucesso() {
        Long projectId = 1L;
        Long userId = 2L;

        User user = new User();
        user.setId(userId);

        Project project = new Project();
        project.setId(projectId);
        project.setProtocolo("PRJ-2026-0001");
        project.setUser(user);
        project.setTensaoFornecimento("380_220V");
        project.setAtivo(true);

        DemandResults demandResults = new DemandResults();
        demandResults.setDemandaGlobalKva(new BigDecimal("120.00"));
        demandResults.setDemandaQdgKva(new BigDecimal("120.00"));
        demandResults.setDemandaServicoKva(new BigDecimal("20.00"));
        demandResults.setDemandaVeKva(BigDecimal.ZERO);
        demandResults.setDisjuntorGeralA(200);
        demandResults.setCapacidadeBarramentoA(340);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(demandResultsRepository.findByProjectId(projectId)).thenReturn(Optional.of(demandResults));

        NormativeValidationResponse response = service.validateProject(projectId, userId);

        assertNotNull(response);
        assertTrue(response.conforme());
        assertEquals("CONFORME_DIS_NOR_053", response.seloConformidade());
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException quando projeto nao existe")
    void deveLancarNotFoundQuandoProjetoInexistente() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.validateProject(99L, 1L));
    }

    @Test
    @DisplayName("Deve lancar ForbiddenException quando usuario nao for proprietario")
    void deveLancarForbiddenQuandoNaoEhDono() {
        User dono = new User();
        dono.setId(10L);

        Project project = new Project();
        project.setId(1L);
        project.setUser(dono);
        project.setAtivo(true);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(ForbiddenException.class, () -> service.validateProject(1L, 999L));
    }

    @Test
    @DisplayName("Deve lancar UnprocessableEntityException quando calculo oficial ainda nao foi processado")
    void deveLancarUnprocessableEntityQuandoSemCalculoOficial() {
        User dono = new User();
        dono.setId(1L);

        Project project = new Project();
        project.setId(1L);
        project.setUser(dono);
        project.setAtivo(true);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(demandResultsRepository.findByProjectId(1L)).thenReturn(Optional.empty());

        assertThrows(UnprocessableEntityException.class, () -> service.validateProject(1L, 1L));
    }
}
