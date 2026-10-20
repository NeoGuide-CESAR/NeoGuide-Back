package neoguide.project.service;

import neoguide.project.dto.catalogo.AreaRangeResponse;
import neoguide.project.dto.catalogo.CoincidenceFactorItemResponse;
import neoguide.project.dto.catalogo.CoincidenceFactorWrapperResponse;
import neoguide.project.dto.catalogo.SafetyFactorResponse;
import neoguide.project.repository.normativo.AreaRangeReferenceRepository;
import neoguide.project.repository.normativo.CoincidenceFactorReferenceRepository;
import neoguide.project.repository.normativo.SafetyFactorReferenceRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogService {

    private final AreaRangeReferenceRepository areaRangeRepository;
    private final CoincidenceFactorReferenceRepository coincidenceRepository;
    private final SafetyFactorReferenceRepository safetyRepository;

    public CatalogService(AreaRangeReferenceRepository areaRangeRepository,
                          CoincidenceFactorReferenceRepository coincidenceRepository,
                          SafetyFactorReferenceRepository safetyRepository) {
        this.areaRangeRepository = areaRangeRepository;
        this.coincidenceRepository = coincidenceRepository;
        this.safetyRepository = safetyRepository;
    }

    @Cacheable("catalogoFaixasArea")
    public List<AreaRangeResponse> getFaixasArea() {
        return areaRangeRepository.findAllByOrderByMinAreaM2Asc()
                .stream()
                .map(AreaRangeResponse::from)
                .toList();
    }

    @Cacheable("catalogoFatoresCoincidencia")
    public CoincidenceFactorWrapperResponse getFatoresCoincidenciaResidencial() {
        List<CoincidenceFactorItemResponse> itens = coincidenceRepository.findAllByOrderByTotalApartamentosAsc()
                .stream()
                .map(CoincidenceFactorItemResponse::from)
                .toList();
        return new CoincidenceFactorWrapperResponse(
                "DIS-NOR-053 Quadro 36",
                "Para edifícios tipo Smart/Studio acima de 15 unidades, adota-se 90% (0,90) fixo",
                itens
        );
    }

    @Cacheable("catalogoFatoresSeguranca")
    public List<SafetyFactorResponse> getFatoresSegurancaResidencial() {
        return safetyRepository.findAllByOrderByMinDrKvaAsc()
                .stream()
                .map(SafetyFactorResponse::from)
                .toList();
    }
}
