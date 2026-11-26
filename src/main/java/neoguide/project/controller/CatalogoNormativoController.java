package neoguide.project.controller;

import neoguide.project.dto.catalogo.BusbarCatalogResponse;
import neoguide.project.dto.catalogo.PoolPumpFactorResponse;
import neoguide.project.dto.catalogo.ServiceEntranceCatalogResponse;
import neoguide.project.dto.catalogo.VeFactorResponse;
import neoguide.project.service.CatalogoNormativoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogo")
public class CatalogoNormativoController {

    private final CatalogoNormativoService catalogoService;

    public CatalogoNormativoController(CatalogoNormativoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/fatores-recarga-ve")
    public ResponseEntity<List<VeFactorResponse>> getFatoresRecargaVe() {
        return ResponseEntity.ok(catalogoService.getFatoresRecargaVe());
    }

    @GetMapping("/fatores-bombas-piscina")
    public ResponseEntity<List<PoolPumpFactorResponse>> getFatoresBombasPiscina() {
        return ResponseEntity.ok(catalogoService.getFatoresBombasPiscina());
    }

    @GetMapping("/dimensionamento-entrada/380v")
    public ResponseEntity<List<ServiceEntranceCatalogResponse>> getDimensionamentoEntrada380v() {
        return ResponseEntity.ok(catalogoService.getDimensionamentoEntrada380v());
    }

    @GetMapping("/dimensionamento-entrada/220v")
    public ResponseEntity<List<ServiceEntranceCatalogResponse>> getDimensionamentoEntrada220v() {
        return ResponseEntity.ok(catalogoService.getDimensionamentoEntrada220v());
    }

    @GetMapping("/barramentos-qdg")
    public ResponseEntity<List<BusbarCatalogResponse>> getBarramentosQdg() {
        return ResponseEntity.ok(catalogoService.getBarramentosQdg());
    }
}
