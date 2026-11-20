package neoguide.project.config.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import neoguide.project.model.User;
import neoguide.project.security.jwt.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MdcLoggingFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("Deve gerar X-Correlation-ID, popular MDC completo e limpar MDC no finally")
    void shouldPopulateMdcAndClearAfterRequest() throws Exception {
        MdcLoggingFilter filter = new MdcLoggingFilter(jwtService);

        User user = new User("Engenheiro Teste", "eng.mdc@neoguide.com", "hash");
        user.setId(42L);
        String token = jwtService.generateToken(user);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/projects");
        request.addHeader("Authorization", "Bearer " + token);
        request.addHeader(MdcLoggingFilter.PROTOCOLO_HEADER, "PRJ-2026-0042");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Map<String, String> capturedMdc = new HashMap<>();
        FilterChain chain = (req, res) -> {
            capturedMdc.put(MdcLoggingFilter.MDC_TRACE_ID, MDC.get(MdcLoggingFilter.MDC_TRACE_ID));
            capturedMdc.put(MdcLoggingFilter.MDC_SPAN_ID, MDC.get(MdcLoggingFilter.MDC_SPAN_ID));
            capturedMdc.put(MdcLoggingFilter.MDC_PROTOCOLO, MDC.get(MdcLoggingFilter.MDC_PROTOCOLO));
            capturedMdc.put(MdcLoggingFilter.MDC_USER_ID, MDC.get(MdcLoggingFilter.MDC_USER_ID));
            capturedMdc.put(MdcLoggingFilter.MDC_URI, MDC.get(MdcLoggingFilter.MDC_URI));
            capturedMdc.put(MdcLoggingFilter.MDC_METODO, MDC.get(MdcLoggingFilter.MDC_METODO));
        };

        filter.doFilter(request, response, chain);

        String responseCorrelationId = response.getHeader(MdcLoggingFilter.CORRELATION_ID_HEADER);
        assertNotNull(responseCorrelationId);
        assertFalse(responseCorrelationId.isBlank());
        assertEquals(responseCorrelationId, capturedMdc.get(MdcLoggingFilter.MDC_TRACE_ID));
        assertNotNull(capturedMdc.get(MdcLoggingFilter.MDC_SPAN_ID));
        assertEquals("PRJ-2026-0042", capturedMdc.get(MdcLoggingFilter.MDC_PROTOCOLO));
        assertEquals("42", capturedMdc.get(MdcLoggingFilter.MDC_USER_ID));
        assertEquals("/api/v1/projects", capturedMdc.get(MdcLoggingFilter.MDC_URI));
        assertEquals("GET", capturedMdc.get(MdcLoggingFilter.MDC_METODO));
        assertNull(MDC.getCopyOfContextMap(), "MDC deve estar completamente limpo após a requisição");
    }

    @Test
    @DisplayName("Deve propagar X-Correlation-ID recebido e limpar MDC mesmo quando ocorre exceção")
    void shouldPropagateCorrelationIdAndClearMdcOnException() {
        MdcLoggingFilter filter = new MdcLoggingFilter(jwtService);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/projects/1/calcular");
        request.addHeader(MdcLoggingFilter.CORRELATION_ID_HEADER, "corr-test-12345");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain failingChain = (req, res) -> {
            assertEquals("corr-test-12345", MDC.get(MdcLoggingFilter.MDC_TRACE_ID));
            throw new ServletException("Falha simulada");
        };

        assertThrows(ServletException.class, () -> filter.doFilter(request, response, failingChain));
        assertEquals("corr-test-12345", response.getHeader(MdcLoggingFilter.CORRELATION_ID_HEADER));
        assertNull(MDC.getCopyOfContextMap(), "MDC deve ser limpo no finally mesmo em exceção");
    }

    @Test
    @DisplayName("Deve ignorar endpoints de health check (/actuator/health e sub-rotas)")
    void shouldNotFilterActuatorHealthEndpoints() {
        MdcLoggingFilter filter = new MdcLoggingFilter(jwtService);

        assertTrue(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/actuator/health")));
        assertTrue(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/actuator/health/liveness")));
        assertTrue(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/actuator/health/readiness")));
        assertFalse(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/api/v1/catalogo/faixas-area")));
    }

    @Test
    @DisplayName("Requisição HTTP real ao catálogo deve retornar X-Correlation-ID gerado ou propagado")
    void catalogEndpoint_shouldReturnCorrelationIdHeader() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/faixas-area"))
                .andExpect(status().isOk())
                .andExpect(header().string(MdcLoggingFilter.CORRELATION_ID_HEADER, not(blankOrNullString())));

        mockMvc.perform(get("/api/v1/catalogo/faixas-area")
                        .header(MdcLoggingFilter.CORRELATION_ID_HEADER, "neoguide-e2e-trace-001"))
                .andExpect(status().isOk())
                .andExpect(header().string(MdcLoggingFilter.CORRELATION_ID_HEADER, "neoguide-e2e-trace-001"));
    }
}
