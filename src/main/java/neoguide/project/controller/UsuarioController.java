package neoguide.project.controller;

import neoguide.project.dto.AlterarSenhaDTO;
import neoguide.project.dto.UsuarioResponseDTO;
import neoguide.project.dto.UsuarioUpdateDTO;
import neoguide.project.exception.UnauthorizedException;
import neoguide.project.model.User;
import neoguide.project.repository.UserRepository;
import neoguide.project.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/usuarios", "/usuarios"})
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UserRepository userRepository;

    public UsuarioController(UsuarioService usuarioService, UserRepository userRepository) {
        this.usuarioService = usuarioService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> getPerfil(Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        return ResponseEntity.ok(usuarioService.getPerfil(userId));
    }

    @PatchMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> atualizarPerfil(@RequestBody(required = false) UsuarioUpdateDTO dto,
                                                              Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        return ResponseEntity.ok(usuarioService.atualizarPerfil(userId, dto));
    }

    @PutMapping("/me/senha")
    public ResponseEntity<Void> alterarSenha(@RequestBody(required = false) AlterarSenhaDTO dto,
                                             Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        usuarioService.alterarSenha(userId, dto);
        return ResponseEntity.noContent().build();
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
