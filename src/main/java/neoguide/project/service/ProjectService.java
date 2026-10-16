package neoguide.project.service;

import neoguide.project.dto.CreateProjectRequest;
import neoguide.project.dto.CreateProjectResponse;
import neoguide.project.dto.ProjectResponse;
import neoguide.project.exception.BusinessException;
import neoguide.project.exception.ForbiddenException;
import neoguide.project.exception.ResourceNotFoundException;
import neoguide.project.model.Project;
import neoguide.project.model.ProjectStatus;
import neoguide.project.model.ProjectType;
import neoguide.project.model.User;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.UserRepository;
import neoguide.project.validation.Validator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
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

        LocalDateTime now = LocalDateTime.now();
        copy.setCreatedAt(now);
        copy.setDataSubmissao(now);

        Project saved = projectRepository.save(copy);
        return ProjectResponse.from(saved);
    }

    /**
     * Lista todos os projetos do usuário logado ordenados pela data de criação decrescente (compatibilidade).
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
}