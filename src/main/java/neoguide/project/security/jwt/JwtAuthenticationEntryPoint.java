package neoguide.project.security.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /** Código de erro para sessão ausente/expirada (contrato B-SEC-04). */
    public static final String CODIGO_SESSAO_EXPIRADA = "sessao_expirada";

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // "codigo" permite ao front distinguir a sessão expirada (Estado B da tela 11) de outros 401.
        String json = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"codigo\":\"%s\",\"message\":\"%s\",\"errors\":{}}",
                LocalDateTime.now(),
                HttpServletResponse.SC_UNAUTHORIZED,
                CODIGO_SESSAO_EXPIRADA,
                "Acesso não autorizado. Token ausente ou inválido."
        );

        response.getWriter().write(json);
    }
}
