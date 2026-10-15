package neoguide.project.service;

import neoguide.project.dto.LoginRequest;
import neoguide.project.dto.LoginResponse;
import neoguide.project.dto.RegisterRequest;
import neoguide.project.dto.UserResponse;
import neoguide.project.exception.BusinessException;
import neoguide.project.exception.UnauthorizedException;
import neoguide.project.exception.ValidationException;
import neoguide.project.model.User;
import neoguide.project.repository.UserRepository;
import neoguide.project.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("João Projetista", "joao@empresa.com", "encodedPassword123");
        sampleUser.setId(10L);
        sampleUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve registrar novo usuário com sucesso e senha criptografada")
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest();
        request.setName("João Projetista");
        request.setEmail("joao@empresa.com");
        request.setPassword("SenhaForte@123");
        request.setConfirmPassword("SenhaForte@123");

        when(userRepository.existsByEmail("joao@empresa.com")).thenReturn(false);
        when(passwordEncoder.encode("SenhaForte@123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("João Projetista", response.getName());
        assertEquals("joao@empresa.com", response.getEmail());

        verify(passwordEncoder).encode("SenhaForte@123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao registrar com e-mail já existente")
    void shouldRejectDuplicateEmailOnRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setName("João Projetista");
        request.setEmail("joao@empresa.com");
        request.setPassword("SenhaForte@123");
        request.setConfirmPassword("SenhaForte@123");

        when(userRepository.existsByEmail("joao@empresa.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(request));
        assertEquals("E-mail já cadastrado", ex.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve autenticar com sucesso e retornar LoginResponse com JWT")
    void shouldLoginSuccessfullyAndReturnJwt() {
        LoginRequest request = new LoginRequest();
        request.setEmail("joao@empresa.com");
        request.setPassword("SenhaForte@123");

        when(userRepository.findByEmail("joao@empresa.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("SenhaForte@123", sampleUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(sampleUser)).thenReturn("mocked.jwt.token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getToken());
        assertEquals("Bearer", response.getTipo());
        assertNotNull(response.getUsuario());
        assertEquals(10L, response.getUsuario().getId());
        assertEquals("joao@empresa.com", response.getUsuario().getEmail());

        verify(jwtService).generateToken(sampleUser);
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando a senha estiver incorreta")
    void shouldRejectLoginWithIncorrectPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("joao@empresa.com");
        request.setPassword("SenhaIncorreta@123");

        when(userRepository.findByEmail("joao@empresa.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("SenhaIncorreta@123", sampleUser.getPassword())).thenReturn(false);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.login(request));
        assertEquals("E-mail ou senha inválidos", ex.getMessage());

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando o usuário não for encontrado")
    void shouldRejectLoginWithNonExistentEmail() {
        LoginRequest request = new LoginRequest();
        request.setEmail("inexistente@empresa.com");
        request.setPassword("SenhaForte@123");

        when(userRepository.findByEmail("inexistente@empresa.com")).thenReturn(Optional.empty());

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.login(request));
        assertEquals("E-mail ou senha inválidos", ex.getMessage());

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Deve rejeitar requisição de cadastro com senhas divergentes")
    void shouldRejectRegisterWithMismatchedPasswords() {
        RegisterRequest request = new RegisterRequest();
        request.setName("João Projetista");
        request.setEmail("joao@empresa.com");
        request.setPassword("SenhaForte@123");
        request.setConfirmPassword("OutraSenha@456");

        assertThrows(ValidationException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }
}
