package neoguide.project.service;

import neoguide.project.dto.catalogo.AcDemandFactorWrapperResponse;
import neoguide.project.dto.catalogo.AirConditionerCatalogResponse;
import neoguide.project.dto.catalogo.BoilerDemandFactorWrapperResponse;
import neoguide.project.dto.catalogo.CooktopDemandFactorWrapperResponse;
import neoguide.project.dto.catalogo.MotorCatalogResponse;
import neoguide.project.dto.catalogo.WaterHeaterInstantFactorWrapperResponse;
import neoguide.project.repository.normativo.AcDemandFactorRepository;
import neoguide.project.repository.normativo.AirConditionerCatalogRepository;
import neoguide.project.repository.normativo.BoilerDemandFactorRepository;
import neoguide.project.repository.normativo.CooktopDemandFactorRepository;
import neoguide.project.repository.normativo.MotorReferenceRepository;
import neoguide.project.repository.normativo.WaterHeaterInstantFactorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class CatalogServiceCommonAreaTest {

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoSpyBean
    private MotorReferenceRepository motorRepository;

    @MockitoSpyBean
    private AirConditionerCatalogRepository airConditionerRepository;

    @MockitoSpyBean
    private AcDemandFactorRepository acDemandFactorRepository;

    @MockitoSpyBean
    private CooktopDemandFactorRepository cooktopRepository;

    @MockitoSpyBean
    private BoilerDemandFactorRepository boilerRepository;

    @MockitoSpyBean
    private WaterHeaterInstantFactorRepository waterHeaterRepository;

    @BeforeEach
    void setUp() {
        limparCache("catalogoMotoresPadrao");
        limparCache("catalogoAparelhosClimatizacao");
        limparCache("catalogoFatoresArCondicionado");
        limparCache("catalogoFatoresCooktop");
        limparCache("catalogoFatoresBoilers");
        limparCache("catalogoFatoresAquecedoresPassagem");
        reset(motorRepository, airConditionerRepository, acDemandFactorRepository,
                cooktopRepository, boilerRepository, waterHeaterRepository);
    }

    private void limparCache(String nomeCache) {
        Cache cache = cacheManager.getCache(nomeCache);
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    @DisplayName("getMotoresPadrao deve utilizar cache na segunda chamada (times(1))")
    void deveCachearMotoresPadrao() {
        List<MotorCatalogResponse> primeira = catalogService.getMotoresPadrao();
        assertNotNull(primeira);
        assertEquals(12, primeira.size());
        verify(motorRepository, times(1)).findAllByOrderByPotenciaCvAsc();

        List<MotorCatalogResponse> segunda = catalogService.getMotoresPadrao();
        assertNotNull(segunda);
        assertEquals(12, segunda.size());
        verify(motorRepository, times(1)).findAllByOrderByPotenciaCvAsc();

        limparCache("catalogoMotoresPadrao");
        catalogService.getMotoresPadrao();
        verify(motorRepository, times(2)).findAllByOrderByPotenciaCvAsc();
    }

    @Test
    @DisplayName("getAparelhosClimatizacao deve utilizar cache na segunda chamada (times(1))")
    void deveCachearAparelhosClimatizacao() {
        List<AirConditionerCatalogResponse> primeira = catalogService.getAparelhosClimatizacao();
        assertNotNull(primeira);
        assertEquals(8, primeira.size());
        verify(airConditionerRepository, times(1)).findAllByOrderByCapacidadeBtuAsc();

        List<AirConditionerCatalogResponse> segunda = catalogService.getAparelhosClimatizacao();
        assertNotNull(segunda);
        verify(airConditionerRepository, times(1)).findAllByOrderByCapacidadeBtuAsc();
    }

    @Test
    @DisplayName("getFatoresArCondicionado deve utilizar cache na segunda chamada (times(1))")
    void deveCachearFatoresArCondicionado() {
        AcDemandFactorWrapperResponse primeira = catalogService.getFatoresArCondicionado();
        assertNotNull(primeira);
        verify(acDemandFactorRepository, times(1)).findAllByOrderByMinAparelhosAsc();

        AcDemandFactorWrapperResponse segunda = catalogService.getFatoresArCondicionado();
        assertNotNull(segunda);
        verify(acDemandFactorRepository, times(1)).findAllByOrderByMinAparelhosAsc();
    }

    @Test
    @DisplayName("getFatoresCooktop deve utilizar cache na segunda chamada (times(1))")
    void deveCachearFatoresCooktop() {
        CooktopDemandFactorWrapperResponse primeira = catalogService.getFatoresCooktop();
        assertNotNull(primeira);
        verify(cooktopRepository, times(1)).findAllByOrderByQuantidadeAparelhosAsc();

        CooktopDemandFactorWrapperResponse segunda = catalogService.getFatoresCooktop();
        assertNotNull(segunda);
        verify(cooktopRepository, times(1)).findAllByOrderByQuantidadeAparelhosAsc();
    }

    @Test
    @DisplayName("getFatoresBoilers deve utilizar cache na segunda chamada (times(1))")
    void deveCachearFatoresBoilers() {
        BoilerDemandFactorWrapperResponse primeira = catalogService.getFatoresBoilers();
        assertNotNull(primeira);
        verify(boilerRepository, times(1)).findAllByOrderByQuantidadeBoilersAsc();

        BoilerDemandFactorWrapperResponse segunda = catalogService.getFatoresBoilers();
        assertNotNull(segunda);
        verify(boilerRepository, times(1)).findAllByOrderByQuantidadeBoilersAsc();
    }

    @Test
    @DisplayName("getFatoresAquecedoresPassagem deve utilizar cache na segunda chamada (times(1))")
    void deveCachearFatoresAquecedoresPassagem() {
        WaterHeaterInstantFactorWrapperResponse primeira = catalogService.getFatoresAquecedoresPassagem();
        assertNotNull(primeira);
        verify(waterHeaterRepository, times(1)).findAllByOrderByQuantidadeAparelhosAsc();

        WaterHeaterInstantFactorWrapperResponse segunda = catalogService.getFatoresAquecedoresPassagem();
        assertNotNull(segunda);
        verify(waterHeaterRepository, times(1)).findAllByOrderByQuantidadeAparelhosAsc();
    }

    @Test
    @DisplayName("Latência de leitura dos catálogos em memória RAM deve ser sub-15ms")
    void deveGarantirDesempenhoSub15ms() {
        // Aquecimento do cache
        catalogService.getMotoresPadrao();
        catalogService.getAparelhosClimatizacao();
        catalogService.getFatoresCooktop();

        // 50 requisições consecutivas
        long inicio = System.nanoTime();
        for (int i = 0; i < 50; i++) {
            catalogService.getMotoresPadrao();
            catalogService.getAparelhosClimatizacao();
            catalogService.getFatoresCooktop();
        }
        long duracaoMs = (System.nanoTime() - inicio) / 1_000_000;
        double mediaPorChamadaMs = (double) duracaoMs / (50 * 3);

        assertTrue(mediaPorChamadaMs < 15.0, "Latência média em cache (" + mediaPorChamadaMs + "ms) deve ser inferior a 15ms");
    }
}
