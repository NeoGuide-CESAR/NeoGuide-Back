package neoguide.project.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorsIntegrationTest {

    private static final String ANGULAR_ORIGIN = "http://localhost:4200";

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest(name = "OPTIONS pre-flight para método {0} deve retornar 200 sem JWT")
    @ValueSource(strings = {"POST", "PUT", "PATCH", "DELETE"})
    void preflightOptions_shouldAllowAngularMethodsWithoutAuthentication(String method) throws Exception {
        mockMvc.perform(options("/api/v1/projects/1")
                        .header("Origin", ANGULAR_ORIGIN)
                        .header("Access-Control-Request-Method", method)
                        .header("Access-Control-Request-Headers", "Content-Type, Authorization, Accept, X-Requested-With"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ANGULAR_ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString(method)))
                .andExpect(header().string("Access-Control-Allow-Headers", containsString("Authorization")))
                .andExpect(header().string("Access-Control-Allow-Headers", containsString("Content-Type")));
    }

    @Test
    @DisplayName("Resposta CORS deve expor Content-Disposition, Content-Type e X-Correlation-ID para o Angular")
    void corsRequest_shouldExposeDownloadAndCorrelationHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/avisos")
                        .header("Origin", ANGULAR_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ANGULAR_ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Expose-Headers", containsString("Content-Disposition")))
                .andExpect(header().string("Access-Control-Expose-Headers", containsString("Content-Type")))
                .andExpect(header().string("Access-Control-Expose-Headers", containsString("X-Correlation-ID")));
    }

    @Test
    @DisplayName("OPTIONS pre-flight para rota de simulação deve ser liberado enquanto GET autenticado exige JWT")
    void simulationEndpoint_shouldAllowPreflightAndProtectActualRequest() throws Exception {
        mockMvc.perform(options("/api/v1/simulacao/residencial")
                        .header("Origin", ANGULAR_ORIGIN)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type, Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ANGULAR_ORIGIN));

        mockMvc.perform(get("/api/v1/simulacao/residencial")
                        .header("Origin", ANGULAR_ORIGIN))
                .andExpect(status().isUnauthorized());
    }
}
