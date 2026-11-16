package neoguide.project.controller;

import neoguide.project.dto.AvisoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Avisos da Home (contrato B-EP-23). Endpoint público.
 * Ainda não há fonte de conteúdo publicada pela equipe (pergunta P10): retorna lista vazia,
 * que é o comportamento previsto quando não há aviso (o bloco "Novidades" some no front).
 */
@RestController
@RequestMapping({"/api/v1/avisos"})
public class AvisoController {

    @GetMapping
    public ResponseEntity<List<AvisoResponse>> listar() {
        return ResponseEntity.ok(List.of());
    }
}
