package neoguide.project.controller;

import neoguide.project.dto.perfil.PerfilResponse;
import neoguide.project.dto.perfil.PerfilUpdateRequest;
import neoguide.project.dto.perfil.SenhaRequest;
import neoguide.project.exception.PerfilRecusadoException;
import neoguide.project.exception.UnauthorizedException;
import neoguide.project.model.User;
import neoguide.project.repository.UserRepository;
import neoguide.project.service.PerfilService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Meu perfil: GET/PATCH /auth/me e PUT /auth/password. Exigem login (a regra de segurança está antes do /auth/**
 * público). Erros em RFC 7807 com {@code regra} e {@code campo}.
 */
@RestController
@RequestMapping({"/api/v1/auth", "/auth"})
public class PerfilController {

    private final PerfilService perfilService;
    private final UserRepository userRepository;

    public PerfilController(PerfilService perfilService, UserRepository userRepository) {
        this.perfilService = perfilService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<PerfilResponse> obter(Authentication authentication) {
        return ResponseEntity.ok(perfilService.obter(userId(authentication)));
    }

    @PatchMapping("/me")
    public ResponseEntity<PerfilResponse> atualizar(@RequestBody(required = false) PerfilUpdateRequest request,
                                                    Authentication authentication) {
        return ResponseEntity.ok(perfilService.atualizar(userId(authentication), request));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> alterarSenha(@RequestBody(required = false) SenhaRequest request, Authentication authentication) {
        perfilService.alterarSenha(userId(authentication), request);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(PerfilRecusadoException.class)
    public ResponseEntity<ProblemDetail> recusado(PerfilRecusadoException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatus());
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problema.setTitle("Alteração não permitida");
        problema.setProperty("regra", ex.getRegra());
        problema.setProperty("campo", ex.getCampo());
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problema);
    }

    private Long userId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Usuário não autenticado");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user.getId();
        }
        String email = principal instanceof UserDetails d ? d.getUsername() : principal instanceof String s ? s : null;
        if (email == null) {
            throw new UnauthorizedException("Credenciais inválidas");
        }
        return userRepository.findByEmail(email).map(User::getId)
                .orElseThrow(() -> new UnauthorizedException("Usuário autenticado não encontrado"));
    }
}
