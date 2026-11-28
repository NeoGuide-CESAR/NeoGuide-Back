package neoguide.project.controller;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import neoguide.project.dto.simulation.PreCalculationRequest;
import neoguide.project.dto.simulation.PreCalculationResponse;
import neoguide.project.service.PreCalculationService;

@RestController
@RequestMapping(value = {"/api/v1/simulacao/motor", "/simulacao/motor"}, produces = MediaType.APPLICATION_JSON_VALUE)
public class SimulationController {

    private final PreCalculationService preCalculationService;

    public SimulationController(PreCalculationService preCalculationService) {
        this.preCalculationService = preCalculationService;
    }

    @PostMapping(value = "/pre-calculo-etapa", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PreCalculationResponse> preCalcular(
        @Valid @RequestBody(required = false) PreCalculationRequest request
    ) {
        PreCalculationResponse response = preCalculationService.preCalcular(request);
        return ResponseEntity.ok(response);
    }
}
