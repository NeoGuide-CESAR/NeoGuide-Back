package neoguide.project.service;

import neoguide.project.dto.LoginRequest;
import neoguide.project.dto.LoginResponse;
import neoguide.project.dto.RegisterRequest;
import neoguide.project.dto.UserResponse;
import neoguide.project.exception.BusinessException;
import neoguide.project.exception.ConflictException;
import neoguide.project.exception.UnauthorizedException;
import neoguide.project.exception.ValidationException;
import neoguide.project.model.User;
import neoguide.project.model.UserRole;
import neoguide.project.repository.UserRepository;
import neoguide.project.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
        sampleUser = new User("João Projetista", "joao@empresa.com", "encodedPassword123", "12345678901", UserRole.ROLE_PROJETISTA);
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
    @DisplayName("Deve registrar usuário com CPF formatado e perfil ROLE_ANALISTA salvando CPF sanitizado")
    void shouldRegisterUserWithCpfAndCustomRoleSuccessfully() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Maria Analista");
        request.setEmail("maria.analista@concessionaria.com");
        request.setCpfCnpj("123.456.789-01");
        request.setRole(UserRole.ROLE_ANALISTA);
        request.setPassword("SenhaForte@123");
        request.setConfirmPassword("SenhaForte@123");

        User analistaUser = new User("Maria Analista", "maria.analista@concessionaria.com", "encodedPassword123", "12345678901", UserRole.ROLE_ANALISTA);
        analistaUser.setId(20L);

        when(userRepository.existsByEmail("maria.analista@concessionaria.com")).thenReturn(false);
        when(userRepository.existsByCpfCnpj("12345678901")).thenReturn(false);
        when(passwordEncoder.encode("SenhaForte@123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(analistaUser);

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(20L, response.getId());
        assertEquals("12345678901", response.getCpfCnpj());
        assertEquals(UserRole.ROLE_ANALISTA, response.getRole());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("12345678901", saved.getCpfCnpj(), "CPF deve ser salvo sanitizado");
        assertEquals(UserRole.ROLE_ANALISTA, saved.getRole());
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
    @DisplayName("Deve lançar ConflictException ao registrar com CPF/CNPJ já existente")
    void shouldRejectDuplicateCpfOnRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Carlos Outro");
        request.setEmail("carlos.outro@empresa.com");
        request.setCpfCnpj("123.456.789-01");
        request.setPassword("SenhaForte@123");
        request.setConfirmPassword("SenhaForte@123");

        when(userRepository.existsByEmail("carlos.outro@empresa.com")).thenReturn(false);
        when(userRepository.existsByCpfCnpj("12345678901")).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> authService.register(request));
        assertEquals("CPF/CNPJ já cadastrado", ex.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve autenticar com sucesso via e-mail e retornar LoginResponse com JWT e roles")
    void shouldLoginSuccessfullyAndReturnJwt() {
        LoginRequest request = new LoginRequest();
        request.setEmail("joao@empresa.com");
        request.setPassword("SenhaForte@123");

        when(userRepository.findByEmailOrCpfCnpj("joao@empresa.com", "joao@empresa.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("SenhaForte@123", sampleUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(sampleUser)).thenReturn("mocked.jwt.token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getToken());
        assertEquals("Bearer", response.getTipo());
        assertEquals("ROLE_PROJETISTA", response.getRole());
        assertNotNull(response.getUsuario());
        assertEquals(10L, response.getUsuario().getId());
        assertEquals("joao@empresa.com", response.getUsuario().getEmail());
        assertEquals(UserRole.ROLE_PROJETISTA, response.getUsuario().getRole());

        verify(jwtService).generateToken(sampleUser);
    }

    @Test
    @DisplayName("Deve autenticar com sucesso via CPF sem máscara")
    void shouldLoginSuccessfullyWithCpfWithoutMask() {
        LoginRequest request = new LoginRequest("12345678901", "SenhaForte@123");

        when(userRepository.findByEmailOrCpfCnpj("12345678901", "12345678901")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("SenhaForte@123", sampleUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(sampleUser)).thenReturn("jwt.token.cpf");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt.token.cpf", response.getToken());
        assertEquals("ROLE_PROJETISTA", response.getRole());
    }

    @Test
    @DisplayName("Deve autenticar com sucesso via CPF com máscara")
    void shouldLoginSuccessfullyWithCpfWithMask() {
        LoginRequest request = new LoginRequest("123.456.789-01", "SenhaForte@123");

        when(userRepository.findByEmailOrCpfCnpj("123.456.789-01", "12345678901")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("SenhaForte@123", sampleUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(sampleUser)).thenReturn("jwt.token.cpf.masked");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt.token.cpf.masked", response.getToken());
        assertEquals("ROLE_PROJETISTA", response.getRole());
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando a senha estiver incorreta")
    void shouldRejectLoginWithIncorrectPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("joao@empresa.com");
        request.setPassword("SenhaIncorreta@123");

        when(userRepository.findByEmailOrCpfCnpj("joao@empresa.com", "joao@empresa.com")).thenReturn(Optional.of(sampleUser));
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

        when(userRepository.findByEmailOrCpfCnpj("inexistente@empresa.com", "inexistente@empresa.com")).thenReturn(Optional.empty());

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
