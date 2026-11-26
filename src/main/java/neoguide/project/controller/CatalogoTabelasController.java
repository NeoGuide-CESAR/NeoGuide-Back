package neoguide.project.controller;

import neoguide.project.service.CatalogoNormativoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Catálogo (somente leitura) das tabelas das normas exibidas na página Normas técnicas. Público, como o catálogo. */
@RestController
@RequestMapping("/api/v1/catalogo")
public class CatalogoTabelasController {

    private final CatalogoNormativoService catalogo;

    public CatalogoTabelasController(CatalogoNormativoService catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping("/fatores-recarga-ve")
    public ResponseEntity<List<Map<String, Object>>> recargaVe() {
        return ResponseEntity.ok(catalogo.recargaVe());
    }

    @GetMapping("/dimensionamento-entrada/380v")
    public ResponseEntity<List<Map<String, Object>>> entrada380() {
        return ResponseEntity.ok(catalogo.entradaServico("380_220V"));
    }

    @GetMapping("/dimensionamento-entrada/220v")
    public ResponseEntity<List<Map<String, Object>>> entrada220() {
        return ResponseEntity.ok(catalogo.entradaServico("220_127V"));
    }

    @GetMapping("/barramentos-qdg")
    public ResponseEntity<List<Map<String, Object>>> barramentos() {
        return ResponseEntity.ok(catalogo.barramentos());
    }

    @GetMapping("/fatores-eletrodomesticos")
    public ResponseEntity<List<Map<String, Object>>> eletrodomesticos() {
        return ResponseEntity.ok(catalogo.eletrodomesticos());
    }

    @GetMapping("/fatores-motores")
    public ResponseEntity<List<Map<String, Object>>> motores() {
        return ResponseEntity.ok(catalogo.fatoresMotores());
    }

    @GetMapping("/fatores-equipamentos-especiais")
    public ResponseEntity<List<Map<String, Object>>> especiais() {
        return ResponseEntity.ok(catalogo.equipamentosEspeciais());
    }

    @GetMapping("/fatores-bombas-piscina")
    public ResponseEntity<List<Map<String, Object>>> piscina() {
        return ResponseEntity.ok(catalogo.bombasPiscina());
    }

    @GetMapping("/dispositivos-partida-motores")
    public ResponseEntity<List<Map<String, Object>>> partidas() {
        return ResponseEntity.ok(catalogo.dispositivosPartida());
    }

    @GetMapping("/motores-monofasicos")
    public ResponseEntity<List<Map<String, Object>>> monofasicos() {
        return ResponseEntity.ok(catalogo.motoresMonofasicos());
    }
}
