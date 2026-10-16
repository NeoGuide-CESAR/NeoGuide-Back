package neoguide.project.service;

import neoguide.project.dto.CreateProjectRequest;
import neoguide.project.dto.CreateProjectResponse;
import neoguide.project.dto.ProjectResponse;
import neoguide.project.dto.ResidentialStageRequest;
import neoguide.project.dto.ResidentialStageResponse;
import neoguide.project.dto.ResidentialTypologyItemRequest;
import neoguide.project.engine.ResidentialCalculationEngine;
import neoguide.project.engine.model.ResidentialCalculationResult;
import neoguide.project.engine.model.ResidentialTypologyItem;
import neoguide.project.engine.model.ResidentialTypologyOutput;
import neoguide.project.exception.BusinessException;
import neoguide.project.exception.ForbiddenException;
import neoguide.project.exception.ResourceNotFoundException;
import neoguide.project.model.Project;
import neoguide.project.model.ProjectStatus;
import neoguide.project.model.ProjectType;
import neoguide.project.model.ResidentialTypology;
import neoguide.project.model.User;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.ResidentialTypologyRepository;
import neoguide.project.repository.UserRepository;
import neoguide.project.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ResidentialTypologyRepository residentialTypologyRepository;
    private final ResidentialCalculationEngine residentialCalculationEngine;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository,
                          ResidentialTypologyRepository residentialTypologyRepository,
                          ResidentialCalculationEngine residentialCalculationEngine) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.residentialTypologyRepository = residentialTypologyRepository;
        this.residentialCalculationEngine = residentialCalculationEngine;
    }

    /**
     * Gera sequencial anual de protocolo no formato PRJ-AAAA-XXXX.
     */
    public synchronized String generateNextProtocolo() {
        return generateNextProtocolo(LocalDate.now().getYear());
    }

    public synchronized String generateNextProtocolo(int year) {
        String prefix = String.format("PRJ-%d-", year);
        Optional<String> lastOpt = projectRepository.findLastProtocoloByPrefix(prefix + "%");

        int nextSeq = 1;
        if (lastOpt.isPresent()) {
            String last = lastOpt.get();
            try {
                int dashIndex = last.lastIndexOf('-');
                if (dashIndex != -1 && dashIndex + 1 < last.length()) {
                    nextSeq = Integer.parseInt(last.substring(dashIndex + 1)) + 1;
                }
            } catch (NumberFormatException ignored) {
                nextSeq = 1;
            }
        }

        return String.format("%s%04d", prefix, nextSeq);
    }

    /**
     * Cria um novo projeto associado ao usuário logado.
     */
    @Transactional
    public CreateProjectResponse createProject(CreateProjectRequest request, Long currentUserId) {
        if (request == null) {
            throw new BusinessException("Requisição de criação de projeto não informada");
        }

        Validator.create()
                .required("titulo", request.getTitulo())
                .length("titulo", request.getTitulo(), 3, 150)
                .validVoltage("tensaoFornecimento", request.getTensaoFornecimento())
                .required("municipio", request.getMunicipio())
                .validate();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado: " + currentUserId));

        Project project = new Project();
        project.setUser(user);
        project.setProtocolo(generateNextProtocolo());
        project.setTitulo(request.getTitulo().trim());
        project.setDescricao(request.getDescricao());
        project.setTensaoFornecimento(request.getTensaoFornecimento().trim());
        project.setPossuiComercial(request.isPossuiComercial());
        project.setPossuiVe(request.isPossuiVe());
        project.setPossuiIncendio(request.isPossuiIncendio());
        project.setEtapaAtual(1);
        project.setStatus(ProjectStatus.RASCUNHO);

        ProjectType tipo = parseProjectType(request.getTipo());
        project.setTipo(tipo);

        String responsavel = request.getResponsavel();
        if (responsavel == null || responsavel.isBlank()) {
            responsavel = user.getName();
        }
        project.setResponsavel(responsavel);
        project.setCrea(request.getCrea());
        project.setMunicipio(request.getMunicipio().trim());
        project.setCep(request.getCep());
        project.setEndereco(request.getEndereco());
        project.setBairro(request.getBairro());
        project.setNormaTecnica("NBR 5410");

        LocalDateTime now = LocalDateTime.now();
        project.setCreatedAt(now);
        project.setDataSubmissao(now);

        Project saved = projectRepository.save(project);
        return CreateProjectResponse.from(saved);
    }

    /**
     * Lista todos os projetos do usuário logado ordenados pela data de criação decrescente.
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> listByUser(Long currentUserId) {
        List<Project> projects = projectRepository.findAllByUserIdOrderByCreatedAtDesc(currentUserId);
        List<ProjectResponse> responses = new ArrayList<>();
        for (Project project : projects) {
            responses.add(ProjectResponse.from(project));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjectsByUser(Long currentUserId) {
        return listByUser(currentUserId);
    }

    /**
     * Consulta detalhes do projeto garantindo que pertence ao usuário logado.
     */
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId, Long currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com id " + projectId));

        if (project.getUser() == null || !project.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Acesso negado: o projeto pertence a outro projetista");
        }

        return ProjectResponse.from(project);
    }

    /**
     * Mantido para compatibilidade com rotas legadas administrativas.
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> listAll() {
        List<Project> projects = projectRepository.findAllByOrderByDataSubmissaoDesc();
        List<ProjectResponse> response = new ArrayList<>();
        for (Project project : projects) {
            response.add(ProjectResponse.from(project));
        }
        return response;
    }

    private ProjectType parseProjectType(String tipoStr) {
        if (tipoStr == null || tipoStr.isBlank()) {
            return ProjectType.LIGACAO_NOVA;
        }
        for (ProjectType pt : ProjectType.values()) {
            if (pt.name().equalsIgnoreCase(tipoStr.trim()) || pt.getDescricao().equalsIgnoreCase(tipoStr.trim())) {
                return pt;
            }
        }
        return ProjectType.LIGACAO_NOVA;
    }

    /**
     * Salva as tipologias residenciais da Etapa 2 de forma atômica e idempotente.
     */
    @Transactional
    public ResidentialStageResponse saveResidentialStage(Long projectId, ResidentialStageRequest request, Long currentUserId) {
        validateResidentialStageRequest(request);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com id " + projectId));

        if (project.getUser() == null || !project.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Acesso negado: o projeto pertence a outro projetista");
        }

        List<ResidentialTypologyItem> engineItems = new ArrayList<>();
        for (ResidentialTypologyItemRequest item : request.getTipologias()) {
            engineItems.add(new ResidentialTypologyItem(
                    item.getNome() != null ? item.getNome().trim() : "",
                    item.getAreaUtilM2(),
                    item.getQuantidadeUnidades(),
                    null,
                    item.isSmartStudio()
            ));
        }
        ResidentialCalculationResult calcResult = residentialCalculationEngine.calculate(engineItems);

        residentialTypologyRepository.deleteByProjectId(projectId);
        residentialTypologyRepository.flush();

        List<ResidentialTypologyOutput> outputs = calcResult.getTipologias();
        List<ResidentialTypology> entitiesToSave = new ArrayList<>();
        for (int i = 0; i < request.getTipologias().size(); i++) {
            ResidentialTypologyItemRequest item = request.getTipologias().get(i);
            ResidentialTypologyOutput out = outputs.get(i);
            BigDecimal demandaUnitaria = out.demandaUnitariaDi();
            BigDecimal demandaTotal = out.demandaSubtotal();

            ResidentialTypology entity = new ResidentialTypology(
                    project,
                    item.getCatalogoFaixaId(),
                    item.getNome().trim(),
                    item.getAreaUtilM2(),
                    item.getQuantidadeUnidades(),
                    item.isSmartStudio(),
                    demandaUnitaria,
                    demandaTotal
            );
            entitiesToSave.add(entity);
        }
        residentialTypologyRepository.saveAll(entitiesToSave);

        int currentStage = project.getEtapaAtual() != null ? project.getEtapaAtual() : 1;
        project.setEtapaAtual(Math.max(currentStage, 2));
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);

        return new ResidentialStageResponse(
                true,
                project.getId(),
                project.getEtapaAtual(),
                calcResult.getTotalApartamentos(),
                calcResult.getDemandaBrutaDr(),
                calcResult.getFatorCoincidenciaFc(),
                calcResult.getFatorSegurancaFr(),
                calcResult.getDemandaFinalDrf()
        );
    }

    private void validateResidentialStageRequest(ResidentialStageRequest request) {
        Validator validator = Validator.create();
        if (request == null || request.getTipologias() == null || request.getTipologias().isEmpty()) {
            validator.custom("tipologias", false, "A lista de tipologias residenciais não pode ser vazia");
        } else {
            for (int i = 0; i < request.getTipologias().size(); i++) {
                ResidentialTypologyItemRequest item = request.getTipologias().get(i);
                String prefix = "tipologias[" + i + "].";
                if (item == null) {
                    validator.custom(prefix, false, "Item de tipologia não pode ser nulo");
                    continue;
                }
                validator.required(prefix + "nome", item.getNome());
                if (item.getAreaUtilM2() == null || item.getAreaUtilM2().compareTo(BigDecimal.ZERO) <= 0) {
                    validator.custom(prefix + "areaUtilM2", false, "A área útil privativa deve ser estritamente maior que zero");
                }
                if (item.getQuantidadeUnidades() == null || item.getQuantidadeUnidades() <= 0) {
                    validator.custom(prefix + "quantidadeUnidades", false, "A quantidade de unidades deve ser um número positivo maior que zero");
                }
            }
        }
        validator.validate();
    }
}