package neoguide.project.service;

import neoguide.project.dto.catalogo.AcDemandFactorItemResponse;
import neoguide.project.dto.catalogo.AcDemandFactorWrapperResponse;
import neoguide.project.dto.catalogo.AirConditionerCatalogResponse;
import neoguide.project.dto.catalogo.AreaRangeResponse;
import neoguide.project.dto.catalogo.BoilerDemandFactorItemResponse;
import neoguide.project.dto.catalogo.BoilerDemandFactorWrapperResponse;
import neoguide.project.dto.catalogo.CoincidenceFactorItemResponse;
import neoguide.project.dto.catalogo.CoincidenceFactorWrapperResponse;
import neoguide.project.dto.catalogo.CooktopDemandFactorItemResponse;
import neoguide.project.dto.catalogo.CooktopDemandFactorWrapperResponse;
import neoguide.project.dto.catalogo.MotorCatalogResponse;
import neoguide.project.dto.catalogo.SafetyFactorResponse;
import neoguide.project.dto.catalogo.WaterHeaterInstantFactorItemResponse;
import neoguide.project.dto.catalogo.WaterHeaterInstantFactorWrapperResponse;
import neoguide.project.repository.normativo.AcDemandFactorRepository;
import neoguide.project.repository.normativo.AirConditionerCatalogRepository;
import neoguide.project.repository.normativo.AreaRangeReferenceRepository;
import neoguide.project.repository.normativo.BoilerDemandFactorRepository;
import neoguide.project.repository.normativo.CoincidenceFactorReferenceRepository;
import neoguide.project.repository.normativo.CooktopDemandFactorRepository;
import neoguide.project.repository.normativo.MotorReferenceRepository;
import neoguide.project.repository.normativo.SafetyFactorReferenceRepository;
import neoguide.project.repository.normativo.WaterHeaterInstantFactorRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CatalogService {

    private final AreaRangeReferenceRepository areaRangeRepository;
    private final CoincidenceFactorReferenceRepository coincidenceRepository;
    private final SafetyFactorReferenceRepository safetyRepository;
    private final MotorReferenceRepository motorRepository;
    private final AirConditionerCatalogRepository airConditionerRepository;
    private final AcDemandFactorRepository acDemandFactorRepository;
    private final CooktopDemandFactorRepository cooktopRepository;
    private final BoilerDemandFactorRepository boilerRepository;
    private final WaterHeaterInstantFactorRepository waterHeaterRepository;

    public CatalogService(AreaRangeReferenceRepository areaRangeRepository,
                          CoincidenceFactorReferenceRepository coincidenceRepository,
                          SafetyFactorReferenceRepository safetyRepository,
                          MotorReferenceRepository motorRepository,
                          AirConditionerCatalogRepository airConditionerRepository,
                          AcDemandFactorRepository acDemandFactorRepository,
                          CooktopDemandFactorRepository cooktopRepository,
                          BoilerDemandFactorRepository boilerRepository,
                          WaterHeaterInstantFactorRepository waterHeaterRepository) {
        this.areaRangeRepository = areaRangeRepository;
        this.coincidenceRepository = coincidenceRepository;
        this.safetyRepository = safetyRepository;
        this.motorRepository = motorRepository;
        this.airConditionerRepository = airConditionerRepository;
        this.acDemandFactorRepository = acDemandFactorRepository;
        this.cooktopRepository = cooktopRepository;
        this.boilerRepository = boilerRepository;
        this.waterHeaterRepository = waterHeaterRepository;
    }

    // =========================================================================
    // Catálogo Residencial (DIS-NOR-053 - US-08)
    // =========================================================================

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

    // =========================================================================
    // Catálogo de Áreas Comuns e Serviços (DIS-NOR-030 - US-11)
    // =========================================================================

    @Cacheable("catalogoMotoresPadrao")
    public List<MotorCatalogResponse> getMotoresPadrao() {
        return motorRepository.findAllByOrderByPotenciaCvAsc()
                .stream()
                .map(MotorCatalogResponse::from)
                .toList();
    }

    @Cacheable("catalogoAparelhosClimatizacao")
    public List<AirConditionerCatalogResponse> getAparelhosClimatizacao() {
        return airConditionerRepository.findAllByOrderByCapacidadeBtuAsc()
                .stream()
                .map(AirConditionerCatalogResponse::from)
                .toList();
    }

    @Cacheable("catalogoFatoresArCondicionado")
    public AcDemandFactorWrapperResponse getFatoresArCondicionado() {
        List<AcDemandFactorItemResponse> itens = acDemandFactorRepository.findAllByOrderByMinAparelhosAsc()
                .stream()
                .map(AcDemandFactorItemResponse::from)
                .toList();
        return new AcDemandFactorWrapperResponse(
                "DIS-NOR-030 Tabela 12",
                new BigDecimal("0.92"),
                itens
        );
    }

    @Cacheable("catalogoFatoresCooktop")
    public CooktopDemandFactorWrapperResponse getFatoresCooktop() {
        List<CooktopDemandFactorItemResponse> itens = cooktopRepository.findAllByOrderByQuantidadeAparelhosAsc()
                .stream()
                .map(CooktopDemandFactorItemResponse::from)
                .toList();
        return new CooktopDemandFactorWrapperResponse(
                "DIS-NOR-030 Tabela 10",
                "Fogões Elétricos e Cooktops de Indução",
                new BigDecimal("1.00"),
                itens
        );
    }

    @Cacheable("catalogoFatoresBoilers")
    public BoilerDemandFactorWrapperResponse getFatoresBoilers() {
        List<BoilerDemandFactorItemResponse> itens = boilerRepository.findAllByOrderByQuantidadeBoilersAsc()
                .stream()
                .map(BoilerDemandFactorItemResponse::from)
                .toList();
        return new BoilerDemandFactorWrapperResponse(
                "DIS-NOR-030 Tabela 8",
                "Aquecedores Centrais / Boilers de Acumulação",
                new BigDecimal("1.00"),
                itens
        );
    }

    @Cacheable("catalogoFatoresAquecedoresPassagem")
    public WaterHeaterInstantFactorWrapperResponse getFatoresAquecedoresPassagem() {
        List<WaterHeaterInstantFactorItemResponse> itens = waterHeaterRepository.findAllByOrderByQuantidadeAparelhosAsc()
                .stream()
                .map(WaterHeaterInstantFactorItemResponse::from)
                .toList();
        return new WaterHeaterInstantFactorWrapperResponse(
                "DIS-NOR-030 Tabela 7",
                "Chuveiros Elétricos e Aquecedores de Passagem",
                new BigDecimal("1.00"),
                itens
        );
    }
}
