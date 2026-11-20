package neoguide.project.config.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import neoguide.project.model.User;
import neoguide.project.security.jwt.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(MdcLoggingFilter.class);

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String PROTOCOLO_HEADER = "X-Project-Protocolo";

    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_SPAN_ID = "spanId";
    public static final String MDC_PROTOCOLO = "protocolo";
    public static final String MDC_USER_ID = "userId";
    public static final String MDC_URI = "uri";
    public static final String MDC_METODO = "metodo";
    public static final String MDC_TEMPO_EXECUCAO_MS = "tempoExecucaoMs";

    private static final String ANONYMOUS = "anonymous";
    private static final String EMPTY_MARKER = "-";

    private final JwtService jwtService;

    public MdcLoggingFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return "/actuator/health".equals(uri) || (uri != null && uri.startsWith("/actuator/health/"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        long startNanos = System.nanoTime();
        String traceId = resolveCorrelationId(request);
        String spanId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String protocolo = resolveProtocolo(request);
        String userId = resolveUserId(request);

        MDC.put(MDC_TRACE_ID, traceId);
        MDC.put(MDC_SPAN_ID, spanId);
        MDC.put(MDC_PROTOCOLO, protocolo);
        MDC.put(MDC_USER_ID, userId);
        MDC.put(MDC_URI, sanitize(request.getRequestURI()));
        MDC.put(MDC_METODO, sanitize(request.getMethod()));

        response.setHeader(CORRELATION_ID_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
            MDC.put(MDC_TEMPO_EXECUCAO_MS, String.valueOf(elapsedMs));

            // Atualiza userId se autenticado durante a cadeia de filtros
            if (ANONYMOUS.equals(MDC.get(MDC_USER_ID))) {
                String resolvedAfter = resolveUserIdFromSecurityContext();
                if (resolvedAfter != null) {
                    MDC.put(MDC_USER_ID, resolvedAfter);
                }
            }

            log.info("HTTP {} {} status={} tempoExecucaoMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    elapsedMs);

            MDC.clear();
        }
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String incoming = sanitize(request.getHeader(CORRELATION_ID_HEADER));
        if (incoming != null && !incoming.isBlank()) {
            return incoming;
        }
        return UUID.randomUUID().toString();
    }

    private String resolveProtocolo(HttpServletRequest request) {
        String fromHeader = sanitize(request.getHeader(PROTOCOLO_HEADER));
        if (fromHeader != null && !fromHeader.isBlank()) {
            return fromHeader;
        }
        String fromParam = sanitize(request.getParameter("protocolo"));
        if (fromParam != null && !fromParam.isBlank()) {
            return fromParam;
        }
        return EMPTY_MARKER;
    }

    private String resolveUserId(HttpServletRequest request) {
        String fromContext = resolveUserIdFromSecurityContext();
        if (fromContext != null) {
            return fromContext;
        }
        if (jwtService != null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtService.validateToken(token)) {
                    Long uid = jwtService.extractUserId(token);
                    if (uid != null) {
                        return String.valueOf(uid);
                    }
                    String username = jwtService.extractUsername(token);
                    if (username != null && !username.isBlank()) {
                        return sanitize(username);
                    }
                }
            }
        }
        return ANONYMOUS;
    }

    private String resolveUserIdFromSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof User user && user.getId() != null) {
            return String.valueOf(user.getId());
        }
        if (principal instanceof String s && !"anonymousUser".equals(s) && !s.isBlank()) {
            return sanitize(s);
        }
        String name = auth.getName();
        if (name != null && !"anonymousUser".equals(name) && !name.isBlank()) {
            return sanitize(name);
        }
        return null;
    }

    private String sanitize(String value) {
        if (value == null) {
            return null;
        }
        return value.replace('\r', ' ').replace('\n', ' ').trim();
    }
}
