package neoguide.project.security;

import io.jsonwebtoken.Claims;
import neoguide.project.model.User;
import neoguide.project.model.UserRole;
import neoguide.project.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "neoguide-super-secret-key-for-jwt-token-hmac-sha256-minimum-32-bytes";
    private static final long EXPIRATION_MS = 86400000; // 24 hours

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_MS);
        testUser = new User("Maria Projetista", "maria@empresa.com", "passwordHash123", "12345678901", UserRole.ROLE_PROJETISTA);
        testUser.setId(42L);
    }

    @Test
    @DisplayName("Deve gerar token JWT válido para um usuário")
    void shouldGenerateValidToken() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    @DisplayName("Deve extrair claims corretamente do token incluindo role")
    void shouldExtractClaimsCorrectly() {
        String token = jwtService.generateToken(testUser);

        assertEquals("maria@empresa.com", jwtService.extractUsername(token));
        assertEquals(42L, jwtService.extractUserId(token));
        assertEquals("ROLE_PROJETISTA", jwtService.extractRole(token));
        assertEquals(List.of("ROLE_PROJETISTA"), jwtService.extractRoles(token));

        Claims claims = jwtService.extractAllClaims(token);
        assertEquals("Maria Projetista", claims.get("name"));
        assertEquals("maria@empresa.com", claims.get("email"));
        assertEquals("ROLE_PROJETISTA", claims.get("role"));
    }

    @Test
    @DisplayName("Deve gerar token com ROLE_ANALISTA e extrair a role corretamente")
    void shouldGenerateTokenWithAnalistaRole() {
        User analista = new User("Pedro Analista", "pedro@concessionaria.com", "senha123", "98765432100", UserRole.ROLE_ANALISTA);
        analista.setId(99L);

        String token = jwtService.generateToken(analista);

        assertEquals("ROLE_ANALISTA", jwtService.extractRole(token));
        assertEquals(List.of("ROLE_ANALISTA"), jwtService.extractRoles(token));
    }

    @Test
    @DisplayName("Deve invalidar token malformado ou nulo")
    void shouldRejectInvalidToken() {
        assertFalse(jwtService.validateToken("invalid.token.value"));
        assertFalse(jwtService.validateToken(""));
        assertFalse(jwtService.validateToken(null));
    }

    @Test
    @DisplayName("Deve invalidar token expirado")
    void shouldRejectExpiredToken() {
        JwtService expiredJwtService = new JwtService(SECRET, -1000);
        String expiredToken = expiredJwtService.generateToken(testUser);

        assertFalse(jwtService.validateToken(expiredToken));
    }

    @Test
    @DisplayName("Deve invalidar token com assinatura adulterada")
    void shouldRejectTamperedToken() {
        String token = jwtService.generateToken(testUser);
        String tamperedToken = token.substring(0, token.length() - 5) + "abcde";

        assertFalse(jwtService.validateToken(tamperedToken));
    }
}
