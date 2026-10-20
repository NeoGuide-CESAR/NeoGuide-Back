package neoguide.project.controller;

import neoguide.project.dto.catalogo.AreaRangeResponse;
import neoguide.project.dto.catalogo.CoincidenceFactorWrapperResponse;
import neoguide.project.dto.catalogo.SafetyFactorResponse;
import neoguide.project.service.CatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogo")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/faixas-area")
    public ResponseEntity<List<AreaRangeResponse>> getFaixasArea() {
        return ResponseEntity.ok(catalogService.getFaixasArea());
    }

    @GetMapping("/fatores-coincidencia-residencial")
    public ResponseEntity<CoincidenceFactorWrapperResponse> getFatoresCoincidencia() {
        return ResponseEntity.ok(catalogService.getFatoresCoincidenciaResidencial());
    }

    @GetMapping("/fatores-seguranca-residencial")
    public ResponseEntity<List<SafetyFactorResponse>> getFatoresSeguranca() {
        return ResponseEntity.ok(catalogService.getFatoresSegurancaResidencial());
    }
}
