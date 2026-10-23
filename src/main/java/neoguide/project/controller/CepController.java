package neoguide.project.controller;

import neoguide.project.dto.CepResponse;
import neoguide.project.service.CepService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/cep", "/cep"})
public class CepController {

    private final CepService cepService;

    public CepController(CepService cepService) {
        this.cepService = cepService;
    }

    @GetMapping("/{cep}")
    public ResponseEntity<CepResponse> consultarCep(@PathVariable("cep") String cep) {
        return ResponseEntity.ok(cepService.consultarCep(cep));
    }
}
