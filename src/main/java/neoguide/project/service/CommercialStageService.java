package neoguide.project.service;

import neoguide.project.dto.EspacoComercialRequest;
import neoguide.project.engine.CommercialCalculationEngine;
import neoguide.project.engine.model.CommercialCalculationResult;
import neoguide.project.engine.model.CommercialUnitInput;
import neoguide.project.exception.ValidationException;
import neoguide.project.model.normativo.OccupancyLoadReference;
import neoguide.project.repository.normativo.OccupancyLoadReferenceRepository;
import neoguide.project.validation.Validator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Regras da etapa 4 (espaços comerciais): validação dos dados enviados, resolução da ocupação na Tabela 22
 * e cálculo da demanda comercial. Usada tanto pela gravação do projeto quanto pela pré-visualização.
 */
@Service
public class CommercialStageService {

    public static final int MAX_ESPACOS = 100;
    public static final BigDecimal AREA_MINIMA_M2 = new BigDecimal("1.0");
    public static final BigDecimal CARGA_MINIMA_KW = new BigDecimal("0.5");

    /** Ocupações da Tabela 22 que usam o escalonamento 20 kW / 70 %. A norma só traz esse caso para escritórios. */
    static final Set<String> OCUPACOES_ESCALONADAS = Set.of("ESCRITORIOS");

    private final OccupancyLoadReferenceRepository ocupacoes;
    private final CommercialCalculationEngine engine;

    public CommercialStageService(OccupancyLoadReferenceRepository ocupacoes, CommercialCalculationEngine engine) {
        this.ocupacoes = ocupacoes;
        this.engine = engine;
    }

    /** Valida formato, quantidade e limites de cada espaço. Lança ValidationException com o campo de cada erro. */
    public void validarEspacos(List<EspacoComercialRequest> espacos) {
        if (espacos.size() > MAX_ESPACOS) {
            throw new ValidationException(Map.of("espacos", "Máximo de " + MAX_ESPACOS + " espaços comerciais por projeto"));
        }
        Validator validator = Validator.create();
        for (int i = 0; i < espacos.size(); i++) {
            EspacoComercialRequest espaco = espacos.get(i);
            String prefixo = "espacos[" + i + "].";
            if (espaco == null) {
                validator.custom("espacos[" + i + "]", false, "Espaço comercial não pode ser nulo");
                continue;
            }
            validator.required(prefixo + "ocupacaoCodigo", espaco.getOcupacaoCodigo());
            validator.custom(prefixo + "areaM2",
                    espaco.getAreaM2() != null && espaco.getAreaM2().compareTo(AREA_MINIMA_M2) >= 0,
                    "Informe a área do espaço, a partir de 1,0 m²");
            validator.custom(prefixo + "cargaInstaladaKw",
                    espaco.getCargaInstaladaKw() != null && espaco.getCargaInstaladaKw().compareTo(CARGA_MINIMA_KW) >= 0,
                    "Informe a carga instalada do espaço, a partir de 0,5 kW");
        }
        validator.validate();
    }

    /** Resolve cada ocupação na Tabela 22. Código desconhecido vira erro no campo ocupacaoCodigo. */
    public List<CommercialUnitInput> resolverEntradas(List<EspacoComercialRequest> espacos) {
        List<String> codigos = espacos.stream().map(EspacoComercialRequest::getOcupacaoCodigo).distinct().toList();
        Map<String, OccupancyLoadReference> porCodigo = ocupacoes.findByOcupacaoIn(codigos).stream()
                .collect(Collectors.toMap(OccupancyLoadReference::getOcupacao, Function.identity()));

        Validator validator = Validator.create();
        for (int i = 0; i < espacos.size(); i++) {
            String codigo = espacos.get(i).getOcupacaoCodigo();
            validator.custom("espacos[" + i + "].ocupacaoCodigo", porCodigo.containsKey(codigo),
                    "Ocupação não encontrada na Tabela 22 da DIS-NOR-030");
        }
        validator.validate();

        return espacos.stream().map(e -> {
            OccupancyLoadReference ref = porCodigo.get(e.getOcupacaoCodigo());
            return new CommercialUnitInput(
                    ref.getOcupacao(),
                    ref.getDescricao(),
                    e.getAreaM2(),
                    e.getCargaInstaladaKw(),
                    ref.getCargaMinimaWm2(),
                    ref.getFatorDemandaIlum(),
                    ref.getFatorPotencia(),
                    OCUPACOES_ESCALONADAS.contains(ref.getOcupacao()));
        }).toList();
    }

    /** Valida, resolve e calcula. Não grava nada. */
    public CommercialCalculationResult calcular(List<EspacoComercialRequest> espacos) {
        validarEspacos(espacos);
        if (espacos.isEmpty()) {
            return engine.calcular(List.of());
        }
        return engine.calcular(resolverEntradas(espacos));
    }
}
