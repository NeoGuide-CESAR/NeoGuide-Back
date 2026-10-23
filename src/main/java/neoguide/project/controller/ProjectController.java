package neoguide.project.controller;

import neoguide.project.dto.CreateProjectRequest;
import neoguide.project.dto.CreateProjectResponse;
import neoguide.project.dto.ProjectResponse;
import neoguide.project.dto.ProjectUpdatePatchRequest;
import neoguide.project.dto.ResidentialStageRequest;
import neoguide.project.dto.ResidentialStageResponse;
import neoguide.project.exception.UnauthorizedException;
import neoguide.project.model.ProjectStatus;
import neoguide.project.model.User;
import neoguide.project.repository.UserRepository;
import neoguide.project.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/projects", "/projects"})
public class ProjectController {

    private final ProjectService projectService;
    private final UserRepository userRepository;

    public ProjectController(ProjectService projectService, UserRepository userRepository) {
        this.projectService = projectService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<CreateProjectResponse> create(@RequestBody CreateProjectRequest request,
                                                        Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        CreateProjectResponse response = projectService.createProject(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> list(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        return ResponseEntity.ok(projectService.listByUserWithFilters(userId, status, search, pageable));
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<ProjectResponse> duplicate(@PathVariable("id") Long id,
                                                     Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.duplicateProject(id, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getById(@PathVariable("id") Long id,
                                                   Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        return ResponseEntity.ok(projectService.getProjectById(id, userId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> updatePatch(
            @PathVariable("id") Long id,
            @RequestBody(required = false) ProjectUpdatePatchRequest request,
            Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        return ResponseEntity.ok(projectService.updateProjectPatch(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id,
            Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        projectService.deleteProject(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/etapa-residencial")
    public ResponseEntity<ResidentialStageResponse> saveResidentialStage(
            @PathVariable("id") Long id,
            @RequestBody ResidentialStageRequest request,
            Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        ResidentialStageResponse response = projectService.saveResidentialStage(id, request, userId);
        return ResponseEntity.ok(response);
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Usuário não autenticado");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user.getId();
        }
        if (principal instanceof String email) {
            return userRepository.findByEmail(email)
                    .map(User::getId)
                    .orElseThrow(() -> new UnauthorizedException("Usuário autenticado não encontrado"));
        }
        if (principal instanceof UserDetails userDetails) {
            return userRepository.findByEmail(userDetails.getUsername())
                    .map(User::getId)
                    .orElseThrow(() -> new UnauthorizedException("Usuário autenticado não encontrado"));
        }
        throw new UnauthorizedException("Credenciais inválidas");
    }
}
