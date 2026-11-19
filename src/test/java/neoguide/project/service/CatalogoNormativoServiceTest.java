package neoguide.project.service;

import neoguide.project.dto.catalogo.OccupancyLoadResponse;
import neoguide.project.dto.catalogo.PoolPumpFactorResponse;
import neoguide.project.dto.catalogo.VeFactorResponse;
import neoguide.project.model.normativo.OccupancyLoadReference;
import neoguide.project.model.normativo.PoolPumpDemandFactor;
import neoguide.project.model.normativo.VeFactorReference;
import neoguide.project.repository.normativo.OccupancyLoadReferenceRepository;
import neoguide.project.repository.normativo.PoolPumpDemandFactorRepository;
import neoguide.project.repository.normativo.VeFactorReferenceRepository;
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
class CatalogoNormativoServiceTest {

    @Autowired
    private CatalogoNormativoService catalogoService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoSpyBean
    private VeFactorReferenceRepository veFactorRepository;

    @MockitoSpyBean
    private OccupancyLoadReferenceRepository occupancyLoadRepository;

    @MockitoSpyBean
    private PoolPumpDemandFactorRepository poolPumpRepository;

    @BeforeEach
    void setUp() {
        limparCache("catalogo_fatores_ve");
        limparCache("catalogo_cargas_ocupacao");
        limparCache("catalogo_fatores_piscina");
        reset(veFactorRepository, occupancyLoadRepository, poolPumpRepository);
    }

    private void limparCache(String nomeCache) {
        Cache cache = cacheManager.getCache(nomeCache);
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    @DisplayName("getFatoresRecargaVe deve utilizar cache na segunda chamada (times(1))")
    void deveCachearFatoresRecargaVe() {
        List<VeFactorResponse> primeira = catalogoService.getFatoresRecargaVe();
        assertNotNull(primeira);
        assertEquals(8, primeira.size());
        verify(veFactorRepository, times(1)).findAllByOrderByMaxVagasAsc();

        List<VeFactorResponse> segunda = catalogoService.getFatoresRecargaVe();
        assertNotNull(segunda);
        assertEquals(8, segunda.size());
        verify(veFactorRepository, times(1)).findAllByOrderByMaxVagasAsc();

        limparCache("catalogo_fatores_ve");
        catalogoService.getFatoresRecargaVe();
        verify(veFactorRepository, times(2)).findAllByOrderByMaxVagasAsc();
    }

    @Test
    @DisplayName("getCargasMinimasOcupacao deve utilizar cache na segunda chamada (times(1))")
    void deveCachearCargasMinimasOcupacao() {
        List<OccupancyLoadResponse> primeira = catalogoService.getCargasMinimasOcupacao();
        assertNotNull(primeira);
        assertEquals(6, primeira.size());
        verify(occupancyLoadRepository, times(1)).findAllByOrderByOcupacaoAsc();

        List<OccupancyLoadResponse> segunda = catalogoService.getCargasMinimasOcupacao();
        assertNotNull(segunda);
        assertEquals(6, segunda.size());
        verify(occupancyLoadRepository, times(1)).findAllByOrderByOcupacaoAsc();
    }

    @Test
    @DisplayName("getFatoresBombasPiscina deve utilizar cache na segunda chamada (times(1))")
    void deveCachearFatoresBombasPiscina() {
        List<PoolPumpFactorResponse> primeira = catalogoService.getFatoresBombasPiscina();
        assertNotNull(primeira);
        assertEquals(4, primeira.size());
        verify(poolPumpRepository, times(1)).findAllByOrderByQuantidadeBombasAsc();

        List<PoolPumpFactorResponse> segunda = catalogoService.getFatoresBombasPiscina();
        assertNotNull(segunda);
        assertEquals(4, segunda.size());
        verify(poolPumpRepository, times(1)).findAllByOrderByQuantidadeBombasAsc();
    }

    @Test
    @DisplayName("Deve mapear corretamente os DTOs a partir das entidades")
    void deveMapearDtosCorretamente() {
        VeFactorReference veEntity = new VeFactorReference(1, new BigDecimal("1.00"), "1 vaga", "DIS-NOR-053");
        veEntity.setId(10L);
        VeFactorResponse veDto = VeFactorResponse.fromEntity(veEntity);
        assertNotNull(veDto);
        assertEquals(10L, veDto.id());
        assertEquals(1, veDto.maxVagas());
        assertEquals(new BigDecimal("1.00"), veDto.fatorSimultaneidade());
        assertEquals("1 vaga", veDto.faixaDescricao());
        assertEquals("DIS-NOR-053", veDto.normaOrigem());

        OccupancyLoadReference occEntity = new OccupancyLoadReference("LOJAS", "Lojas", new BigDecimal("30.00"),
                new BigDecimal("700.00"), new BigDecimal("1.00"), new BigDecimal("1.00"), new BigDecimal("0.92"), "Regra", "DIS-NOR-030");
        occEntity.setId(20L);
        OccupancyLoadResponse occDto = OccupancyLoadResponse.fromEntity(occEntity);
        assertNotNull(occDto);
        assertEquals(20L, occDto.id());
        assertEquals("LOJAS", occDto.ocupacao());
        assertEquals("Lojas", occDto.descricao());
        assertEquals(new BigDecimal("30.00"), occDto.cargaMinimaWm2());
        assertEquals(new BigDecimal("700.00"), occDto.vitrineWMetro());
        assertEquals(new BigDecimal("0.92"), occDto.fatorPotencia());

        PoolPumpDemandFactor pumpEntity = new PoolPumpDemandFactor(2, new BigDecimal("0.56"), new BigDecimal("0.80"), "DIS-NOR-030");
        pumpEntity.setId(30L);
        PoolPumpFactorResponse pumpDto = PoolPumpFactorResponse.fromEntity(pumpEntity);
        assertNotNull(pumpDto);
        assertEquals(30L, pumpDto.id());
        assertEquals(2, pumpDto.quantidadeBombas());
        assertEquals(new BigDecimal("0.56"), pumpDto.fatorDemanda());
        assertEquals(new BigDecimal("0.80"), pumpDto.fatorPotencia());
    }

    @Test
    @DisplayName("Latência de leitura dos catálogos normativos deve ser sub-15ms em cache")
    void deveGarantirDesempenhoSub15ms() {
        catalogoService.getFatoresRecargaVe();
        catalogoService.getCargasMinimasOcupacao();
        catalogoService.getFatoresBombasPiscina();

        long inicio = System.nanoTime();
        for (int i = 0; i < 50; i++) {
            catalogoService.getFatoresRecargaVe();
            catalogoService.getCargasMinimasOcupacao();
            catalogoService.getFatoresBombasPiscina();
        }
        long duracaoMs = (System.nanoTime() - inicio) / 1_000_000;
        double mediaPorChamadaMs = (double) duracaoMs / (50 * 3);

        assertTrue(mediaPorChamadaMs < 15.0, "Latência média (" + mediaPorChamadaMs + "ms) deve ser inferior a 15ms");
    }
}
