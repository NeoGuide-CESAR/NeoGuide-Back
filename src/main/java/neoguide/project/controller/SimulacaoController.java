package neoguide.project.controller;

import neoguide.project.dto.CommercialUnitItemResponse;
import neoguide.project.dto.EspacoComercialRequest;
import neoguide.project.engine.model.CommercialCalculationResult;
import neoguide.project.engine.model.CommercialUnitResult;
import neoguide.project.exception.ValidationException;
import neoguide.project.service.CommercialStageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pré-cálculo de uma etapa, sem gravar nada. Alimenta o indicador "Demanda comercial estimada" da tela.
 */
@RestController
@RequestMapping("/api/v1/simulacao")
public class SimulacaoController {

    private static final String ETAPA_COMERCIAL = "comercial";

    private final CommercialStageService commercialStageService;

    public SimulacaoController(CommercialStageService commercialStageService) {
        this.commercialStageService = commercialStageService;
    }

    @PostMapping("/pre-calculo-etapa")
    public ResponseEntity<PreCalculoEtapaResponse> preCalcular(@RequestBody PreCalculoEtapaRequest request) {
        if (request == null || !ETAPA_COMERCIAL.equals(request.etapa())) {
            throw new ValidationException(Map.of("etapa", "Pré-cálculo disponível apenas para a etapa 'comercial'"));
        }
        List<EspacoComercialRequest> espacos = request.espacos() == null ? List.of() : request.espacos();
        CommercialCalculationResult calculo = commercialStageService.calcular(espacos);
        List<CommercialUnitItemResponse> itens = new ArrayList<>();
        for (int i = 0; i < calculo.unidadesDetalhadas().size(); i++) {
            CommercialUnitResult r = calculo.unidadesDetalhadas().get(i);
            itens.add(new CommercialUnitItemResponse(null, i + 1, r.ocupacaoCodigo(), r.rotulo(), r.areaM2(),
                    r.potenciaInstaladaKw(), r.cargaMinimaKw(), r.demandaCalculadaKva(), r.alerta()));
        }
        return ResponseEntity.ok(new PreCalculoEtapaResponse(ETAPA_COMERCIAL, calculo.potenciaTotalInstaladaKw(),
                calculo.demandaComercialDcKva(), itens));
    }

    public record PreCalculoEtapaRequest(String etapa, List<EspacoComercialRequest> espacos) {
    }

    public record PreCalculoEtapaResponse(String etapa, BigDecimal potenciaTotalInstaladaKw,
                                          BigDecimal demandaComercialDcKva, List<CommercialUnitItemResponse> espacos) {
    }
}
