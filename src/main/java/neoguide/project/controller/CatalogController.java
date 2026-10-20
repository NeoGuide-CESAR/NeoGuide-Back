package neoguide.project.controller;

import neoguide.project.dto.catalogo.AcDemandFactorWrapperResponse;
import neoguide.project.dto.catalogo.AirConditionerCatalogResponse;
import neoguide.project.dto.catalogo.AreaRangeResponse;
import neoguide.project.dto.catalogo.BoilerDemandFactorWrapperResponse;
import neoguide.project.dto.catalogo.CoincidenceFactorWrapperResponse;
import neoguide.project.dto.catalogo.CooktopDemandFactorWrapperResponse;
import neoguide.project.dto.catalogo.MotorCatalogResponse;
import neoguide.project.dto.catalogo.SafetyFactorResponse;
import neoguide.project.dto.catalogo.WaterHeaterInstantFactorWrapperResponse;
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

    // =========================================================================
    // Catálogo Residencial (DIS-NOR-053 - US-08)
    // =========================================================================

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

    // =========================================================================
    // Catálogo de Áreas Comuns e Serviços (DIS-NOR-030 - US-11)
    // =========================================================================

    @GetMapping("/motores-padrao")
    public ResponseEntity<List<MotorCatalogResponse>> getMotoresPadrao() {
        return ResponseEntity.ok(catalogService.getMotoresPadrao());
    }

    @GetMapping("/aparelhos-climatizacao")
    public ResponseEntity<List<AirConditionerCatalogResponse>> getAparelhosClimatizacao() {
        return ResponseEntity.ok(catalogService.getAparelhosClimatizacao());
    }

    @GetMapping("/fatores-ar-condicionado")
    public ResponseEntity<AcDemandFactorWrapperResponse> getFatoresArCondicionado() {
        return ResponseEntity.ok(catalogService.getFatoresArCondicionado());
    }

    @GetMapping("/fatores-fogao-cooktop")
    public ResponseEntity<CooktopDemandFactorWrapperResponse> getFatoresCooktop() {
        return ResponseEntity.ok(catalogService.getFatoresCooktop());
    }

    @GetMapping("/fatores-boilers")
    public ResponseEntity<BoilerDemandFactorWrapperResponse> getFatoresBoilers() {
        return ResponseEntity.ok(catalogService.getFatoresBoilers());
    }

    @GetMapping("/fatores-aquecedores-passagem")
    public ResponseEntity<WaterHeaterInstantFactorWrapperResponse> getFatoresAquecedoresPassagem() {
        return ResponseEntity.ok(catalogService.getFatoresAquecedoresPassagem());
    }
}
