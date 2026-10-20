package neoguide.project.service;

import neoguide.project.dto.CreateProjectRequest;
import neoguide.project.dto.CreateProjectResponse;
import neoguide.project.dto.ProjectResponse;
import neoguide.project.dto.ProjectUpdatePatchRequest;
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
import neoguide.project.dto.CommonAreaLoadItemRequest;
import neoguide.project.dto.CommonAreaStageRequest;
import neoguide.project.dto.CommonAreaStageResponse;
import neoguide.project.engine.CommonAreaCalculationEngine;
import neoguide.project.engine.model.commonarea.CommonAreaCalculationResult;
import neoguide.project.model.CommonAreaLoad;
import neoguide.project.repository.CommonAreaLoadRepository;
import neoguide.project.validation.Validator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final CommonAreaLoadRepository commonAreaLoadRepository;
    private final CommonAreaCalculationEngine commonAreaCalculationEngine;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository,
                          ResidentialTypologyRepository residentialTypologyRepository,
                          ResidentialCalculationEngine residentialCalculationEngine,
                          CommonAreaLoadRepository commonAreaLoadRepository,
                          CommonAreaCalculationEngine commonAreaCalculationEngine) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.residentialTypologyRepository = residentialTypologyRepository;
        this.residentialCalculationEngine = residentialCalculationEngine;
        this.commonAreaLoadRepository = commonAreaLoadRepository;
        this.commonAreaCalculationEngine = commonAreaCalculationEngine;
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
        project.setAtivo(true);

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
     * Lista projetos paginados com filtros opcionais de status e busca textual.
     */
    @Transactional(readOnly = true)
    public Page<ProjectResponse> listByUserWithFilters(Long userId, ProjectStatus status, String search, Pageable pageable) {
        String termo = (search != null && !search.isBlank()) ? search.trim() : null;
        Page<Project> page = projectRepository.findByUserWithFilters(userId, status, termo, pageable);
        return page.map(ProjectResponse::from);
    }

    /**
     * Duplica um projeto existente para o usuário logado com novo protocolo e status RASCUNHO.
     */
    @Transactional
    public ProjectResponse duplicateProject(Long projectId, Long currentUserId) {
        Project original = projectRepository.findById(projectId)
                .filter(Project::isAtivo)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com id " + projectId));

        if (original.getUser() == null || !original.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Acesso negado: o projeto pertence a outro projetista");
        }

        Project copy = new Project();
        copy.setUser(original.getUser());
        copy.setProtocolo(generateNextProtocolo());
        copy.setTitulo(original.getTitulo() + " (cópia)");
        copy.setDescricao(original.getDescricao());
        copy.setTipo(original.getTipo());
        copy.setStatus(ProjectStatus.RASCUNHO);
        copy.setNormaTecnica(original.getNormaTecnica());
        copy.setResponsavel(original.getResponsavel());
        copy.setMunicipio(original.getMunicipio());
        copy.setTensaoFornecimento(original.getTensaoFornecimento());
        copy.setPossuiComercial(original.isPossuiComercial());
        copy.setPossuiVe(original.isPossuiVe());
        copy.setPossuiIncendio(original.isPossuiIncendio());
        copy.setEtapaAtual(1);
        copy.setCep(original.getCep());
        copy.setEndereco(original.getEndereco());
        copy.setBairro(original.getBairro());
        copy.setCrea(original.getCrea());
        copy.setAtivo(true);

        LocalDateTime now = LocalDateTime.now();
        copy.setCreatedAt(now);
        copy.setDataSubmissao(now);

        Project saved = projectRepository.save(copy);
        return ProjectResponse.from(saved);
    }

    /**
     * Atualiza parcialmente os dados cadastrais da Etapa 1 do projeto (PATCH).
     * Caso atributos conceituais ou de carga sejam alterados, reseta o status para RASCUNHO.
     */
    @Transactional
    public ProjectResponse updateProjectPatch(Long projectId, ProjectUpdatePatchRequest request, Long currentUserId) {
        if (request == null) {
            throw new BusinessException("Corpo da requisição de atualização é obrigatório");
        }

        Project project = projectRepository.findById(projectId)
                .filter(Project::isAtivo)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com id " + projectId));

        if (project.getUser() == null || !project.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Acesso negado: o projeto pertence a outro projetista");
        }

        boolean structuralChanged = false;

        if (request.getTipo() != null && !request.getTipo().isBlank()) {
            ProjectType newTipo = parseProjectType(request.getTipo());
            if (newTipo != project.getTipo()) {
                project.setTipo(newTipo);
                structuralChanged = true;
            }
        }

        if (request.getTensaoFornecimento() != null && !request.getTensaoFornecimento().isBlank()) {
            Validator.create().validVoltage("tensaoFornecimento", request.getTensaoFornecimento()).validate();
            if (!request.getTensaoFornecimento().trim().equals(project.getTensaoFornecimento())) {
                project.setTensaoFornecimento(request.getTensaoFornecimento().trim());
                structuralChanged = true;
            }
        }

        if (request.getPossuiComercial() != null && request.getPossuiComercial() != project.isPossuiComercial()) {
            project.setPossuiComercial(request.getPossuiComercial());
            structuralChanged = true;
        }

        if (request.getPossuiVe() != null && request.getPossuiVe() != project.isPossuiVe()) {
            project.setPossuiVe(request.getPossuiVe());
            structuralChanged = true;
        }

        if (request.getPossuiIncendio() != null && request.getPossuiIncendio() != project.isPossuiIncendio()) {
            project.setPossuiIncendio(request.getPossuiIncendio());
            structuralChanged = true;
        }

        if (structuralChanged) {
            project.setStatus(ProjectStatus.RASCUNHO);
        }

        String titulo = request.getTitulo();
        if (titulo != null && !titulo.isBlank()) {
            Validator.create().length("titulo", titulo, 3, 150).validate();
            project.setTitulo(titulo.trim());
        }

        if (request.getDescricao() != null) {
            project.setDescricao(request.getDescricao().trim());
        }
        if (request.getNormaTecnica() != null) {
            project.setNormaTecnica(request.getNormaTecnica().trim());
        }
        if (request.getResponsavel() != null) {
            project.setResponsavel(request.getResponsavel().trim());
        }
        if (request.getMunicipio() != null && !request.getMunicipio().isBlank()) {
            project.setMunicipio(request.getMunicipio().trim());
        }
        if (request.getCep() != null) {
            project.setCep(request.getCep().trim());
        }
        String endereco = request.getEndereco();
        if (endereco != null) {
            project.setEndereco(endereco.trim());
        }
        if (request.getBairro() != null) {
            project.setBairro(request.getBairro().trim());
        }
        if (request.getCrea() != null) {
            project.setCrea(request.getCrea().trim());
        }

        project.setUpdatedAt(LocalDateTime.now());
        Project saved = projectRepository.save(project);
        return ProjectResponse.from(saved);
    }

    /**
     * Executa a exclusão lógica do projeto (DELETE / soft delete), marcando ativo = false.
     */
    @Transactional
    public void deleteProject(Long projectId, Long currentUserId) {
        Project project = projectRepository.findById(projectId)
                .filter(Project::isAtivo)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com id " + projectId));

        if (project.getUser() == null || !project.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Acesso negado: o projeto pertence a outro projetista");
        }

        project.setAtivo(false);
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    /**
     * Lista todos os projetos ativos do usuário logado ordenados pela data de criação decrescente.
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> listByUser(Long currentUserId) {
        List<Project> projects = projectRepository.findAllByUserIdOrderByCreatedAtDesc(currentUserId);
        List<ProjectResponse> responses = new ArrayList<>();
        for (Project project : projects) {
            if (project.isAtivo()) {
                responses.add(ProjectResponse.from(project));
            }
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjectsByUser(Long currentUserId) {
        return listByUser(currentUserId);
    }

    /**
     * Consulta detalhes do projeto garantindo que pertence ao usuário logado e está ativo.
     */
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId, Long currentUserId) {
        Project project = projectRepository.findById(projectId)
                .filter(Project::isAtivo)
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
            if (project.isAtivo()) {
                response.add(ProjectResponse.from(project));
            }
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
                .filter(Project::isAtivo)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com id " + projectId));

        if (project.getUser() == null || !project.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Acesso negado: o projeto pertence a outro projetista");
        }

        List<ResidentialTypologyItem> engineItems = new ArrayList<>();
        for (ResidentialTypologyItemRequest item : request.getTipologias()) {
            String nome = "Tipologia " + item.getAreaUtilM2() + "m²";
            engineItems.add(new ResidentialTypologyItem(
                    nome,
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
            String nome = "Tipologia " + item.getAreaUtilM2() + "m²";

            ResidentialTypology entity = new ResidentialTypology(
                    project,
                    nome,
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
                calcResult.getDemandaFinalDrf()
        );
    }

    /**
     * Salva as cargas de áreas comuns e serviços da Etapa 3 de forma atômica e idempotente.
     */
    @Transactional
    public CommonAreaStageResponse saveCommonAreaStage(Long projectId, CommonAreaStageRequest request, Long currentUserId) {
        validateCommonAreaStageRequest(request);

        Project project = projectRepository.findById(projectId)
                .filter(Project::isAtivo)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com id " + projectId));

        if (project.getUser() == null || !project.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Acesso negado: o projeto pertence a outro projetista");
        }

        CommonAreaCalculationResult calcResult = commonAreaCalculationEngine.calculate(request.getCargas());

        commonAreaLoadRepository.deleteByProjectId(projectId);
        commonAreaLoadRepository.flush();

        List<CommonAreaLoad> entities = new ArrayList<>();
        for (int i = 0; i < request.getCargas().size(); i++) {
            CommonAreaLoadItemRequest item = request.getCargas().get(i);
            BigDecimal demandaCalculada = (calcResult.getDemandasPorItem() != null && i < calcResult.getDemandasPorItem().size())
                    ? calcResult.getDemandasPorItem().get(i)
                    : BigDecimal.ZERO;

            CommonAreaLoad load = new CommonAreaLoad();
            load.setProject(project);
            load.setOrigem(item.getOrigem() != null ? item.getOrigem().toUpperCase() : "PERSONALIZADO");
            load.setSubmodulo(item.getSubmodulo() != null ? item.getSubmodulo().toUpperCase() : "MOTORES");
            load.setCatalogoMotorId(item.getCatalogoMotorId());
            load.setCatalogoClimaId(item.getCatalogoClimaId());
            load.setCatalogoEquipamentoId(item.getCatalogoEquipamentoId());
            load.setTipoEquipamento(item.getTipoEquipamento());
            load.setDescricao(item.getDescricao());
            load.setQuantidade(item.getQuantidade());
            load.setPotenciaNominalKw(item.getPotenciaNominalKw());
            load.setFatorPotencia(item.getFatorPotencia());
            load.setFatorDemanda(item.getFatorDemanda());
            load.setDemandaCalculadaKva(demandaCalculada);
            load.setReserva(item.isReserva());

            entities.add(load);
        }
        commonAreaLoadRepository.saveAll(entities);

        int currentStage = project.getEtapaAtual() != null ? project.getEtapaAtual() : 1;
        int novaEtapa = Math.max(currentStage, 3);
        project.setEtapaAtual(novaEtapa);
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);

        return new CommonAreaStageResponse(
                true,
                project.getId(),
                novaEtapa,
                entities.size(),
                calcResult.getDemandaServicosDsKva()
        );
    }

    private void validateCommonAreaStageRequest(CommonAreaStageRequest request) {
        Validator validator = Validator.create();

        if (request == null || request.getCargas() == null || request.getCargas().isEmpty()) {
            validator.custom("cargas", false, "A lista de cargas de áreas comuns não pode ser vazia");
            validator.validate();
            return;
        }

        List<String> submodulosValidos = List.of("ILUMINACAO_TUG", "CLIMATIZACAO", "MOTORES", "COCCAO", "AQUECIMENTO");

        for (int i = 0; i < request.getCargas().size(); i++) {
            CommonAreaLoadItemRequest item = request.getCargas().get(i);
            String prefix = "cargas[" + i + "].";

            if (item == null) {
                validator.custom(prefix, false, "Item de carga não pode ser nulo");
                continue;
            }

            // Validação da Origem
            if (item.getOrigem() == null || (!item.getOrigem().equalsIgnoreCase("CATALOGO") && !item.getOrigem().equalsIgnoreCase("PERSONALIZADO"))) {
                validator.custom(prefix + "origem", false, "Origem deve ser 'CATALOGO' ou 'PERSONALIZADO'");
            }

            // Validação do Submódulo
            if (item.getSubmodulo() == null || !submodulosValidos.contains(item.getSubmodulo().toUpperCase())) {
                validator.custom(prefix + "submodulo", false, "Submódulo inválido. Valores aceitos: ILUMINACAO_TUG, CLIMATIZACAO, MOTORES, COCCAO, AQUECIMENTO");
            }

            // Tipo de Equipamento
            validator.required(prefix + "tipoEquipamento", item.getTipoEquipamento());

            // Quantidade
            if (item.getQuantidade() == null || item.getQuantidade() <= 0) {
                validator.custom(prefix + "quantidade", false, "A quantidade deve ser um número inteiro maior que zero");
            }

            // Regras para item PERSONALIZADO
            if ("PERSONALIZADO".equalsIgnoreCase(item.getOrigem())) {
                if (item.getPotenciaNominalKw() == null || item.getPotenciaNominalKw().compareTo(BigDecimal.ZERO) <= 0) {
                    validator.custom(prefix + "potenciaNominalKw", false, "A potência nominal em kW deve ser maior que zero para cargas personalizadas");
                }
                if (item.getFatorPotencia() == null || item.getFatorPotencia().compareTo(BigDecimal.ZERO) <= 0 || item.getFatorPotencia().compareTo(BigDecimal.ONE) > 0) {
                    validator.custom(prefix + "fatorPotencia", false, "O fator de potência deve estar entre 0.01 e 1.00");
                }
                if (item.getFatorDemanda() == null || item.getFatorDemanda().compareTo(BigDecimal.ZERO) < 0 || item.getFatorDemanda().compareTo(BigDecimal.ONE) > 0) {
                    validator.custom(prefix + "fatorDemanda", false, "O fator de demanda deve estar entre 0.00 e 1.00");
                }
            }

            // Regras para item CATALOGO
            if ("CATALOGO".equalsIgnoreCase(item.getOrigem())) {
                boolean hasCatalogId = item.getCatalogoMotorId() != null
                        || item.getCatalogoClimaId() != null
                        || item.getCatalogoEquipamentoId() != null;
                if (!hasCatalogId && (item.getTipoEquipamento() == null || item.getTipoEquipamento().isBlank())) {
                    validator.custom(prefix + "catalogo", false, "Para itens de catálogo, informe ao menos um ID de referência normativa ou tipo de equipamento");
                }
            }
        }

        validator.validate();
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
