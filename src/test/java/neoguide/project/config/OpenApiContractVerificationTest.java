package neoguide.project.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiContractVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /v3/api-docs deve retornar 200 OK com especificação OpenAPI 3 sem exigir autenticação")
    void apiDocsEndpoint_shouldReturnOpenApiSpecification() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().string(containsString("\"openapi\"")))
                .andExpect(content().string(containsString("/api/v1/projects")))
                .andExpect(content().string(containsString("/api/v1/projects/{id}/etapa-residencial")))
                .andExpect(content().string(containsString("/api/v1/projects/{id}/memorial-pdf")));
    }

    @Test
    @DisplayName("GET /swagger-ui/index.html deve estar acessível sem autenticação")
    void swaggerUiIndex_shouldBePubliclyAccessible() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
