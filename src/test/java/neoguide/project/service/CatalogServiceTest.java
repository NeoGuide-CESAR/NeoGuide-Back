package neoguide.project.service;

import neoguide.project.dto.catalogo.AreaRangeResponse;
import neoguide.project.dto.catalogo.CoincidenceFactorWrapperResponse;
import neoguide.project.dto.catalogo.SafetyFactorResponse;
import neoguide.project.repository.normativo.AreaRangeReferenceRepository;
import neoguide.project.repository.normativo.CoincidenceFactorReferenceRepository;
import neoguide.project.repository.normativo.SafetyFactorReferenceRepository;
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
class CatalogServiceTest {

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoSpyBean
    private AreaRangeReferenceRepository areaRangeRepository;

    @MockitoSpyBean
    private CoincidenceFactorReferenceRepository coincidenceRepository;

    @MockitoSpyBean
    private SafetyFactorReferenceRepository safetyRepository;

    @BeforeEach
    void setUp() {
        limparCache("catalogoFaixasArea");
        limparCache("catalogoFatoresCoincidencia");
        limparCache("catalogoFatoresSeguranca");
        reset(areaRangeRepository, coincidenceRepository, safetyRepository);
    }

    private void limparCache(String nomeCache) {
        Cache cache = cacheManager.getCache(nomeCache);
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    @DisplayName("getFaixasArea deve consultar banco na primeira chamada e usar cache na segunda (verify times(1))")
    void deveCachearChamadasDeFaixasArea() {
        // 1ª chamada: carrega do banco de dados
        List<AreaRangeResponse> primeiraChamada = catalogService.getFaixasArea();
        assertNotNull(primeiraChamada);
        assertEquals(13, primeiraChamada.size());
        verify(areaRangeRepository, times(1)).findAllByOrderByMinAreaM2Asc();

        // 2ª chamada: deve recuperar do cache em memória RAM sem nova consulta SQL
        List<AreaRangeResponse> segundaChamada = catalogService.getFaixasArea();
        assertNotNull(segundaChamada);
        assertEquals(13, segundaChamada.size());
        verify(areaRangeRepository, times(1)).findAllByOrderByMinAreaM2Asc();

        // Limpeza explícita do cache
        limparCache("catalogoFaixasArea");

        // 3ª chamada: com cache limpo, deve consultar o repositório novamente
        List<AreaRangeResponse> terceiraChamada = catalogService.getFaixasArea();
        assertNotNull(terceiraChamada);
        verify(areaRangeRepository, times(2)).findAllByOrderByMinAreaM2Asc();
    }

    @Test
    @DisplayName("getFatoresCoincidenciaResidencial deve utilizar cache na segunda chamada")
    void deveCachearChamadasDeFatoresCoincidencia() {
        CoincidenceFactorWrapperResponse primeira = catalogService.getFatoresCoincidenciaResidencial();
        assertNotNull(primeira);
        assertEquals("DIS-NOR-053 Quadro 36", primeira.getNorma());
        assertFalse(primeira.getTabela().isEmpty());
        verify(coincidenceRepository, times(1)).findAllByOrderByTotalApartamentosAsc();

        CoincidenceFactorWrapperResponse segunda = catalogService.getFatoresCoincidenciaResidencial();
        assertNotNull(segunda);
        verify(coincidenceRepository, times(1)).findAllByOrderByTotalApartamentosAsc();
    }

    @Test
    @DisplayName("getFatoresSegurancaResidencial deve utilizar cache na segunda chamada")
    void deveCachearChamadasDeFatoresSeguranca() {
        List<SafetyFactorResponse> primeira = catalogService.getFatoresSegurancaResidencial();
        assertNotNull(primeira);
        assertEquals(4, primeira.size());
        verify(safetyRepository, times(1)).findAllByOrderByMinDrKvaAsc();

        List<SafetyFactorResponse> segunda = catalogService.getFatoresSegurancaResidencial();
        assertNotNull(segunda);
        assertEquals(4, segunda.size());
        verify(safetyRepository, times(1)).findAllByOrderByMinDrKvaAsc();
    }

    @Test
    @DisplayName("Chamadas subsequentes em cache devem responder em tempo sub-15ms")
    void deveResponderEmTempoSub15msAposPrimeiraCarga() {
        // Warm-up do cache
        catalogService.getFaixasArea();

        // Medição da chamada em cache
        long inicio = System.nanoTime();
        List<AreaRangeResponse> cached = catalogService.getFaixasArea();
        long duracaoMs = (System.nanoTime() - inicio) / 1_000_000;

        assertNotNull(cached);
        assertTrue(duracaoMs < 15, "Tempo de resposta em cache deve ser sub-15ms, mas foi " + duracaoMs + "ms");
    }
}
