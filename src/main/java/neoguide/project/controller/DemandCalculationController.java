package neoguide.project.controller;

import neoguide.project.dto.calculation.DemandCalculationResponse;
import neoguide.project.exception.UnauthorizedException;
import neoguide.project.model.User;
import neoguide.project.repository.UserRepository;
import neoguide.project.security.jwt.JwtService;
import neoguide.project.service.DemandCalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/projects", "/projects"})
public class DemandCalculationController {

    private final DemandCalculationService demandCalculationService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public DemandCalculationController(DemandCalculationService demandCalculationService,
                                       UserRepository userRepository,
                                       JwtService jwtService) {
        this.demandCalculationService = demandCalculationService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/{id}/motor/calcular")
    public ResponseEntity<DemandCalculationResponse> calcularProjeto(
            @PathVariable("id") Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            Authentication authentication) {
        Long currentUserId = resolveUserId(authHeader, authentication);
        DemandCalculationResponse response = demandCalculationService.calculateAndConsolidate(id, currentUserId);
        return ResponseEntity.ok(response);
    }

    private Long resolveUserId(String authHeader, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return user.getId();
            }
            if (principal instanceof String email) {
                return userRepository.findByEmail(email)
                        .map(User::getId)
                        .orElse(null);
            }
            if (principal instanceof UserDetails userDetails) {
                return userRepository.findByEmail(userDetails.getUsername())
                        .map(User::getId)
                        .orElse(null);
            }
        }
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);
            if (userId != null) {
                return userId;
            }
            String username = jwtService.extractUsername(token);
            if (username != null) {
                return userRepository.findByEmail(username)
                        .map(User::getId)
                        .orElse(null);
            }
        }
        throw new UnauthorizedException("Usuário não autenticado");
    }
}
