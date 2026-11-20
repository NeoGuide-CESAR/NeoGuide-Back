package neoguide.project.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import neoguide.project.dto.simulation.CommercialUnitItemDto;
import neoguide.project.dto.simulation.CommonAreaLoadItemDto;
import neoguide.project.dto.simulation.PreCalculationRequest;
import neoguide.project.dto.simulation.PreCalculationResponse;
import neoguide.project.dto.simulation.ResidentialTypologyItemDto;
import neoguide.project.dto.simulation.SpecialLoadsItemDto;
import neoguide.project.engine.CommercialCalculationEngine;
import neoguide.project.engine.CommonAreaCalculationEngine;
import neoguide.project.engine.ConsolidatedCalculationEngine;
import neoguide.project.engine.ResidentialCalculationEngine;
import neoguide.project.engine.SpecialLoadsCalculationEngine;

class PreCalculationServiceTest {

    private PreCalculationService preCalculationService;

    @BeforeEach
    void setUp() {
        ResidentialCalculationEngine residentialEngine = new ResidentialCalculationEngine();
        CommonAreaCalculationEngine commonAreaEngine = new CommonAreaCalculationEngine();
        CommercialCalculationEngine commercialEngine = new CommercialCalculationEngine();
        SpecialLoadsCalculationEngine specialLoadsEngine = new SpecialLoadsCalculationEngine();
        ConsolidatedCalculationEngine consolidatedEngine = new ConsolidatedCalculationEngine();

        preCalculationService = new PreCalculationService(
                residentialEngine,
                commonAreaEngine,
                commercialEngine,
                specialLoadsEngine,
                consolidatedEngine
        );
    }

    @Test
    @DisplayName("Deve calcular memorial completo de 100 apartamentos com servicos, comercio, VE e bomba")
    void deveCalcularMemorialCompletoComSucesso() {
        PreCalculationRequest request = new PreCalculationRequest(
                "380_220V",
                List.of(
                        new ResidentialTypologyItemDto("Tipo A - 2 Quartos", new BigDecimal("63.00"), 50, false),
                        new ResidentialTypologyItemDto("Tipo B - 3 Quartos", new BigDecimal("68.00"), 50, false)
                ),
                List.of(
                        new CommonAreaLoadItemDto("PERSONALIZADO", "ILUMINACAO_TUG", "Iluminação Hall", new BigDecimal("10.00"), 1, new BigDecimal("0.85"), new BigDecimal("1.00"), false, null, null, null)
                ),
                List.of(
                        new CommercialUnitItemDto("Loja 01", "PADARIA", new BigDecimal("100.00"), new BigDecimal("20.00"), new BigDecimal("1.00"))
                ),
                new SpecialLoadsItemDto(4, new BigDecimal("7.40"), new BigDecimal("2.00"), BigDecimal.ZERO)
        );

        PreCalculationResponse response = preCalculationService.preCalcular(request);

        assertNotNull(response);
        assertTrue(response.tempoProcessamentoMs() < 100L);
        assertTrue(response.demandaResidencialFinalDrfKva().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(response.demandaServicosDsKva().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(response.demandaComercialDcKva().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(response.demandaVeDveKva().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(response.demandaIncendioDmbiKva().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(response.demandaGlobalDedKva().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(response.demandaQdgKva().compareTo(BigDecimal.ZERO) > 0);

        // Verificação D_QDG = D_ed - D_mbi
        BigDecimal esperadoDqdg = response.demandaGlobalDedKva().subtract(response.demandaIncendioDmbiKva());
        assertEquals(esperadoDqdg, response.demandaQdgKva());

        assertNotNull(response.disjuntorGeralSugeridoA());
        assertNotNull(response.caboFaseSugeridoMm2());
        assertNotNull(response.barramentoQdgSugeridoMm());
    }

    @Test
    @DisplayName("Deve processar de forma resiliente quando listas sao vazias ou nulas")
    void deveProcessarResilienteQuandoListasVazias() {
        PreCalculationRequest request = new PreCalculationRequest(
                "380_220V",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                SpecialLoadsItemDto.empty()
        );

        PreCalculationResponse response = preCalculationService.preCalcular(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("0.00"), response.demandaGlobalDedKva());
        assertEquals(new BigDecimal("0.00"), response.demandaQdgKva());
        assertEquals(new BigDecimal("0.00"), response.demandaResidencialFinalDrfKva());
    }

    @Test
    @DisplayName("Deve emitir alerta de subestacao propria quando demanda ultrapassa 300 kVA")
    void deveAlertarQuandoDemandaUltrapassa300kVa() {
        PreCalculationRequest request = new PreCalculationRequest(
                "380_220V",
                List.of(
                        new ResidentialTypologyItemDto("Mansao", new BigDecimal("400.00"), 80, false)
                ),
                Collections.emptyList(),
                Collections.emptyList(),
                SpecialLoadsItemDto.empty()
        );

        PreCalculationResponse response = preCalculationService.preCalcular(request);

        assertNotNull(response);
        assertTrue(response.demandaGlobalDedKva().compareTo(new BigDecimal("300.00")) > 0);
        assertEquals("MEDIA_TENSAO_PRIMARIA_13_8KV", response.faixaAtendimento());
        assertNotNull(response.alertaNormativo());
        assertTrue(response.alertaNormativo().contains("300 kVA"));
    }

    @Test
    @DisplayName("Deve cumprir SLA de tempo de processamento sub-20ms em memoria RAM")
    void deveCumprirSlaSub20ms() {
        PreCalculationRequest request = new PreCalculationRequest(
                "380_220V",
                List.of(
                        new ResidentialTypologyItemDto("Tipo A", new BigDecimal("60.00"), 40, false)
                ),
                Collections.emptyList(),
                Collections.emptyList(),
                SpecialLoadsItemDto.empty()
        );

        // Warm up
        for (int i = 0; i < 5; i++) {
            preCalculationService.preCalcular(request);
        }

        PreCalculationResponse response = preCalculationService.preCalcular(request);
        assertTrue(response.tempoProcessamentoMs() < 20L, "Tempo deve ser menor que 20ms, obtido: " + response.tempoProcessamentoMs());
    }
}
