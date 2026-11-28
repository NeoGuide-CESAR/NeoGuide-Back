package neoguide.project.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import neoguide.project.exception.UnauthorizedException;
import neoguide.project.model.Project;
import neoguide.project.model.User;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.service.pdf.MemorialPdfService;

@RestController
@RequestMapping({"/api/v1/projects", "/projects"})
public class MemorialPdfController {

    private final MemorialPdfService memorialPdfService;
    private final ProjectRepository projectRepository;

    public MemorialPdfController(MemorialPdfService memorialPdfService,
                                  ProjectRepository projectRepository) {
        this.memorialPdfService = memorialPdfService;
        this.projectRepository = projectRepository;
    }

    @GetMapping(value = "/{id}/motor/memorial-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadMemorialPdf(@PathVariable("id") Long id,
                                                      Authentication authentication) {
        String userEmail = getAuthenticatedUserEmail(authentication);
        byte[] pdfBytes = memorialPdfService.generateMemorialPdf(id, userEmail);

        String protocolo = projectRepository.findById(id)
                .map(Project::getProtocolo)
                .orElse("PRJ-" + id);
        String filename = "Memorial_Descritivo_" + protocolo + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    private String getAuthenticatedUserEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Usuário não autenticado");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user.getEmail();
        }
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof String email) {
            return email;
        }
        throw new UnauthorizedException("Não foi possível identificar o usuário autenticado");
    }
}
