package neoguide.project.service;

import neoguide.project.dto.catalogo.MinimumOutletsCatalogResponse;
import neoguide.project.dto.catalogo.MotorStarterCatalogResponse;
import neoguide.project.dto.catalogo.SinglePhaseMotorCatalogResponse;
import neoguide.project.model.normativo.MinimumOutletsReference;
import neoguide.project.model.normativo.MotorStarterReference;
import neoguide.project.model.normativo.SinglePhaseMotorReference;
import neoguide.project.repository.normativo.MinimumOutletsReferenceRepository;
import neoguide.project.repository.normativo.MotorStarterReferenceRepository;
import neoguide.project.repository.normativo.SinglePhaseMotorReferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class CatalogSpecialtiesServiceTest {

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoSpyBean
    private SinglePhaseMotorReferenceRepository singlePhaseMotorRepository;

    @MockitoSpyBean
    private MotorStarterReferenceRepository motorStarterRepository;

    @MockitoSpyBean
    private MinimumOutletsReferenceRepository minimumOutletsRepository;

    @BeforeEach
    void setUp() {
        limparCache("catalogoMotoresMonofasicos");
        limparCache("catalogoDispositivosPartida");
        limparCache("catalogoTomadasMinimas");
        reset(singlePhaseMotorRepository, motorStarterRepository, minimumOutletsRepository);
    }

    private void limparCache(String nomeCache) {
        Cache cache = cacheManager.getCache(nomeCache);
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    @DisplayName("getMotoresMonofasicos deve utilizar cache na segunda chamada (times(1))")
    void deveCachearMotoresMonofasicos() {
        List<SinglePhaseMotorCatalogResponse> primeira = catalogService.getMotoresMonofasicos();
        assertNotNull(primeira);
        assertEquals(11, primeira.size());
        verify(singlePhaseMotorRepository, times(1)).findAllByOrderByPotenciaCvNumericaAsc();

        List<SinglePhaseMotorCatalogResponse> segunda = catalogService.getMotoresMonofasicos();
        assertNotNull(segunda);
        assertEquals(11, segunda.size());
        verify(singlePhaseMotorRepository, times(1)).findAllByOrderByPotenciaCvNumericaAsc();

        limparCache("catalogoMotoresMonofasicos");
        catalogService.getMotoresMonofasicos();
        verify(singlePhaseMotorRepository, times(2)).findAllByOrderByPotenciaCvNumericaAsc();
    }

    @Test
    @DisplayName("getDispositivosPartida deve utilizar cache na segunda chamada (times(1))")
    void deveCachearDispositivosPartida() {
        List<MotorStarterCatalogResponse> primeira = catalogService.getDispositivosPartida();
        assertNotNull(primeira);
        assertEquals(4, primeira.size());
        verify(motorStarterRepository, times(1)).findAllByOrderByIdAsc();

        List<MotorStarterCatalogResponse> segunda = catalogService.getDispositivosPartida();
        assertNotNull(segunda);
        assertEquals(4, segunda.size());
        verify(motorStarterRepository, times(1)).findAllByOrderByIdAsc();
    }

    @Test
    @DisplayName("getTomadasMinimasResidenciais deve utilizar cache na segunda chamada (times(1))")
    void deveCachearTomadasMinimas() {
        List<MinimumOutletsCatalogResponse> primeira = catalogService.getTomadasMinimasResidenciais();
        assertNotNull(primeira);
        assertEquals(7, primeira.size());
        verify(minimumOutletsRepository, times(1)).findAllByOrderByMinAreaM2Asc();

        List<MinimumOutletsCatalogResponse> segunda = catalogService.getTomadasMinimasResidenciais();
        assertNotNull(segunda);
        assertEquals(7, segunda.size());
        verify(minimumOutletsRepository, times(1)).findAllByOrderByMinAreaM2Asc();
    }

    @Test
    @DisplayName("Deve converter entidades em DTOs corretamente via from()")
    void deveConverterEntidadesEmDtos() {
        SinglePhaseMotorReference motor = new SinglePhaseMotorReference("1/4", new BigDecimal("0.25"),
                new BigDecimal("420.00"), new BigDecimal("660.00"), new BigDecimal("5.90"),
                new BigDecimal("3.00"), new BigDecimal("0.63"), "DIS-NOR-030");
        motor.setId(1L);
        SinglePhaseMotorCatalogResponse motorDto = SinglePhaseMotorCatalogResponse.from(motor);
        assertNotNull(motorDto);
        assertEquals(1L, motorDto.getId());
        assertEquals("1/4", motorDto.getPotenciaCv());
        assertEquals(new BigDecimal("0.25"), motorDto.getPotenciaCvNumerica());
        assertEquals(new BigDecimal("420.00"), motorDto.getPotenciaW());
        assertEquals(new BigDecimal("660.00"), motorDto.getPotenciaVa());
        assertEquals(new BigDecimal("5.90"), motorDto.getCorrente110vA());
        assertEquals(new BigDecimal("3.00"), motorDto.getCorrente220vA());
        assertEquals(new BigDecimal("0.63"), motorDto.getFatorPotencia());

        MotorStarterReference starter = new MotorStarterReference("DIRETA", null, new BigDecimal("5.00"),
                null, new BigDecimal("7.50"), "Partida direta", "DIS-NOR-030");
        starter.setId(2L);
        MotorStarterCatalogResponse starterDto = MotorStarterCatalogResponse.from(starter);
        assertNotNull(starterDto);
        assertEquals(2L, starterDto.getId());
        assertEquals("DIRETA", starterDto.getTipoPartida());
        assertEquals(new BigDecimal("5.00"), starterDto.getMaxCv220v());
        assertEquals(new BigDecimal("7.50"), starterDto.getMaxCv380v());

        MinimumOutletsReference outlet = new MinimumOutletsReference(new BigDecimal("0.00"), new BigDecimal("30.00"),
                "Até 30 m²", 3, 2, "DIS-NOR-030");
        outlet.setId(3L);
        MinimumOutletsCatalogResponse outletDto = MinimumOutletsCatalogResponse.from(outlet);
        assertNotNull(outletDto);
        assertEquals(3L, outletDto.getId());
        assertEquals("Até 30 m²", outletDto.getFaixaDescricao());
        assertEquals(3, outletDto.getMinTug100w());
        assertEquals(2, outletDto.getMinTug600w());
    }

    @Test
    @DisplayName("Latência de leitura dos catálogos de especialidades em memória RAM deve ser sub-15ms")
    void deveGarantirDesempenhoSub15ms() {
        catalogService.getMotoresMonofasicos();
        catalogService.getDispositivosPartida();
        catalogService.getTomadasMinimasResidenciais();

        long inicio = System.nanoTime();
        for (int i = 0; i < 50; i++) {
            catalogService.getMotoresMonofasicos();
            catalogService.getDispositivosPartida();
            catalogService.getTomadasMinimasResidenciais();
        }
        long duracaoMs = (System.nanoTime() - inicio) / 1_000_000;
        double mediaPorChamadaMs = (double) duracaoMs / (50 * 3);

        assertTrue(mediaPorChamadaMs < 15.0, "Latência média (" + mediaPorChamadaMs + "ms) deve ser inferior a 15ms");
    }
}
