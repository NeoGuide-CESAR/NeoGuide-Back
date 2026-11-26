package neoguide.project.controller;

import neoguide.project.dto.normas.NormaResponse;
import neoguide.project.dto.normas.PremissaResponse;
import neoguide.project.dto.normas.TabelaNormaResponse;
import neoguide.project.service.NormasService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Página "Normas técnicas": normas vigentes, suas tabelas e as premissas do projeto. Exige login. */
@RestController
@RequestMapping("/api/v1/normas")
public class NormasController {

    private final NormasService normasService;

    public NormasController(NormasService normasService) {
        this.normasService = normasService;
    }

    @GetMapping
    public ResponseEntity<List<NormaResponse>> normas() {
        return ResponseEntity.ok(normasService.normas());
    }

    @GetMapping("/premissas")
    public ResponseEntity<List<PremissaResponse>> premissas() {
        return ResponseEntity.ok(normasService.premissas());
    }

    @GetMapping("/{codigo}/tabelas")
    public ResponseEntity<List<TabelaNormaResponse>> tabelas(@PathVariable String codigo) {
        return ResponseEntity.ok(normasService.tabelas(codigo));
    }
}
