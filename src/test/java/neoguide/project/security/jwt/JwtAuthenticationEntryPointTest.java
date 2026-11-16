package neoguide.project.security.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

/** B-SEC-04: o 401 de token ausente/expirado traz o código "sessao_expirada" para o front (Estado B da tela 11). */
class JwtAuthenticationEntryPointTest {

    @Test
    @DisplayName("401 deve conter codigo=sessao_expirada e status 401")
    void deveIdentificarSessaoExpirada() throws Exception {
        JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/projects");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new AuthenticationException("sem token") {
        });

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"codigo\":\"sessao_expirada\"");
        assertThat(response.getContentAsString()).contains("\"status\":401");
    }
}
