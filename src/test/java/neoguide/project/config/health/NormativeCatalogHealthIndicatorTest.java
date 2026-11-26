package neoguide.project.config.health;

import neoguide.project.repository.normativo.AreaRangeReferenceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NormativeCatalogHealthIndicatorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Indicador unitário deve retornar UP quando existem 39 ou mais faixas de área")
    void healthIndicator_shouldReturnUpWhenCatalogIsComplete() {
        AreaRangeReferenceRepository repository = mock(AreaRangeReferenceRepository.class);
        when(repository.count()).thenReturn(39L);

        NormativeCatalogHealthIndicator indicator = new NormativeCatalogHealthIndicator(repository);
        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(39L, health.getDetails().get("faixasArea"));
    }

    @Test
    @DisplayName("Indicador unitário deve retornar DOWN quando existem menos de 39 faixas de área")
    void healthIndicator_shouldReturnDownWhenCatalogIsIncomplete() {
        AreaRangeReferenceRepository repository = mock(AreaRangeReferenceRepository.class);
        when(repository.count()).thenReturn(12L);

        NormativeCatalogHealthIndicator indicator = new NormativeCatalogHealthIndicator(repository);
        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(12L, health.getDetails().get("faixasArea"));
    }

    @Test
    @DisplayName("Indicador unitário deve retornar DOWN quando ocorre erro ao consultar o banco")
    void healthIndicator_shouldReturnDownOnDatabaseException() {
        AreaRangeReferenceRepository repository = mock(AreaRangeReferenceRepository.class);
        when(repository.count()).thenThrow(new RuntimeException("Database unavailable"));

        NormativeCatalogHealthIndicator indicator = new NormativeCatalogHealthIndicator(repository);
        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    @DisplayName("GET /actuator/health/liveness deve responder 200 UP sem autenticação")
    void livenessEndpoint_shouldBePublicAndUp() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /actuator/health/readiness deve responder 200 UP com db e normativeCatalog")
    void readinessEndpoint_shouldIncludeNormativeCatalogAndDb() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.normativeCatalog.status").value("UP"))
                .andExpect(jsonPath("$.components.normativeCatalog.details.faixasArea").value(39))
                .andExpect(jsonPath("$.components.db.status").value("UP"));
    }

    @Test
    @DisplayName("GET /actuator/metrics deve permanecer protegido exigindo autenticação (401)")
    void metricsEndpoint_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized());
    }
}
