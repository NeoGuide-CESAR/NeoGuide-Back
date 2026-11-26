package neoguide.project.controller;

import neoguide.project.dto.calculo.CalculoResponse;
import neoguide.project.dto.calculo.EtapasProjetoResponse;
import neoguide.project.dto.calculo.ValidacaoNormativaResponse;
import neoguide.project.exception.CalculoRecusadoException;
import neoguide.project.exception.UnauthorizedException;
import neoguide.project.model.User;
import neoguide.project.repository.UserRepository;
import neoguide.project.service.CalculoDemandaService;
import neoguide.project.service.CalculoDemandaService.MemorialPdf;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Validação normativa, cálculo, resultado e memorial do projeto. Erros de regra saem em RFC 7807 (ProblemDetail),
 * com a propriedade {@code regra} e a lista {@code erros} (campo e etapa a corrigir).
 */
@RestController
public class CalculoController {

    private final CalculoDemandaService calculoDemandaService;
    private final UserRepository userRepository;

    public CalculoController(CalculoDemandaService calculoDemandaService, UserRepository userRepository) {
        this.calculoDemandaService = calculoDemandaService;
        this.userRepository = userRepository;
    }

    @GetMapping({"/api/v1/projects/{id}/validacao-normativa", "/projects/{id}/validacao-normativa"})
    public ResponseEntity<ValidacaoNormativaResponse> validarNormativa(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(calculoDemandaService.validar(id, getAuthenticatedUserId(authentication)));
    }

    @PostMapping({"/api/v1/projects/{id}/calcular", "/projects/{id}/calcular"})
    public ResponseEntity<CalculoResponse> calcular(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(calculoDemandaService.calcular(id, getAuthenticatedUserId(authentication)));
    }

    @GetMapping({"/api/v1/projects/{id}/resultado", "/projects/{id}/resultado"})
    public ResponseEntity<CalculoResponse> resultado(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(calculoDemandaService.resultado(id, getAuthenticatedUserId(authentication)));
    }

    @GetMapping({"/api/v1/projects/{id}/etapas", "/projects/{id}/etapas"})
    public ResponseEntity<EtapasProjetoResponse> etapas(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(calculoDemandaService.etapas(id, getAuthenticatedUserId(authentication)));
    }

    @GetMapping({"/api/v1/projects/{id}/memorial-pdf", "/projects/{id}/memorial-pdf"})
    public ResponseEntity<byte[]> memorialPdf(@PathVariable Long id, Authentication authentication) {
        MemorialPdf memorial = calculoDemandaService.memorialPdf(id, getAuthenticatedUserId(authentication));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"memorial-" + memorial.protocolo() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(memorial.conteudo());
    }

    @ExceptionHandler(CalculoRecusadoException.class)
    public ResponseEntity<ProblemDetail> tratarRegraDeCalculo(CalculoRecusadoException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatus());
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problema.setTitle(status == HttpStatus.UNPROCESSABLE_ENTITY ? "Dados incompletos para o cálculo" : "Cálculo não permitido");
        problema.setProperty("regra", ex.getRegra());
        problema.setProperty("erros", ex.getErros());
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problema);
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
