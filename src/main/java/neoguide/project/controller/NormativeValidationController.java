package neoguide.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import neoguide.project.dto.validation.NormativeValidationResponse;
import neoguide.project.exception.UnauthorizedException;
import neoguide.project.model.User;
import neoguide.project.repository.UserRepository;
import neoguide.project.service.NormativeValidationService;

@RestController
@RequestMapping({"/api/v1/projects", "/projects"})
public class NormativeValidationController {

    private final NormativeValidationService normativeValidationService;
    private final UserRepository userRepository;

    public NormativeValidationController(NormativeValidationService normativeValidationService,
                                         UserRepository userRepository) {
        this.normativeValidationService = normativeValidationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}/validacao-normativa")
    public ResponseEntity<NormativeValidationResponse> validateNormative(
            @PathVariable("id") Long id,
            Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        NormativeValidationResponse response = normativeValidationService.validateProject(id, userId);
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
