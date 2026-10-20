package neoguide.project.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import neoguide.project.dto.validation.NormativeValidationResponse;
import neoguide.project.exception.ForbiddenException;
import neoguide.project.exception.ResourceNotFoundException;
import neoguide.project.exception.UnprocessableEntityException;
import neoguide.project.model.DemandResults;
import neoguide.project.model.Project;
import neoguide.project.repository.DemandResultsRepository;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.validator.NormativeRuleValidator;

@Service
public class NormativeValidationService {

    private final ProjectRepository projectRepository;
    private final DemandResultsRepository demandResultsRepository;
    private final NormativeRuleValidator normativeRuleValidator;

    public NormativeValidationService(ProjectRepository projectRepository,
                                      DemandResultsRepository demandResultsRepository,
                                      NormativeRuleValidator normativeRuleValidator) {
        this.projectRepository = projectRepository;
        this.demandResultsRepository = demandResultsRepository;
        this.normativeRuleValidator = normativeRuleValidator;
    }

    @Transactional(readOnly = true)
    public NormativeValidationResponse validateProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .filter(Project::isAtivo)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com id: " + projectId));

        if (project.getUser() == null || !project.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Usuário não possui permissão para acessar o projeto informado");
        }

        DemandResults demandResults = demandResultsRepository.findByProjectId(projectId)
                .orElseThrow(() -> new UnprocessableEntityException(
                        "O cálculo oficial da demanda ainda não foi processado para este projeto. Execute POST /api/v1/projects/"
                                + projectId + "/calcular antes da auditoria normativa."));

        return normativeRuleValidator.validate(project, demandResults);
    }
}
