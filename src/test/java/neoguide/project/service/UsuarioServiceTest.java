package neoguide.project.service;

import neoguide.project.dto.AlterarSenhaDTO;
import neoguide.project.dto.UsuarioResponseDTO;
import neoguide.project.dto.UsuarioUpdateDTO;
import neoguide.project.exception.BusinessException;
import neoguide.project.exception.ResourceNotFoundException;
import neoguide.project.exception.ValidationException;
import neoguide.project.model.User;
import neoguide.project.model.UserRole;
import neoguide.project.repository.UserRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(
                "Carlos Silva",
                "carlos@eng.com",
                "hashSenha123",
                "12345678901",
                UserRole.ROLE_PROJETISTA,
                "81988887777",
                "Engenharia Ltda",
                "CREA-PE 12345"
        );
        sampleUser.setId(10L);
        sampleUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("getPerfil deve retornar dados completos do usuário existente")
    void getPerfil_sucesso() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));

        UsuarioResponseDTO response = usuarioService.getPerfil(10L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Carlos Silva", response.getName());
        assertEquals("carlos@eng.com", response.getEmail());
        assertEquals("81988887777", response.getTelefone());
        assertEquals("CREA-PE 12345", response.getCrea());
        assertEquals("Engenharia Ltda", response.getEmpresa());
    }

    @Test
    @DisplayName("getPerfil deve lançar ResourceNotFoundException quando usuário não for encontrado")
    void getPerfil_usuarioNaoEncontrado() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.getPerfil(99L));
    }

    @Test
    @DisplayName("atualizarPerfil deve atualizar campos permitidos com sucesso")
    void atualizarPerfil_sucesso() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioUpdateDTO update = new UsuarioUpdateDTO("Carlos A. Silva", "81999990000", "Nova Empresa", "CREA-PE 99999");
        UsuarioResponseDTO response = usuarioService.atualizarPerfil(10L, update);

        assertNotNull(response);
        assertEquals("Carlos A. Silva", response.getName());
        assertEquals("81999990000", response.getTelefone());
        assertEquals("Nova Empresa", response.getEmpresa());
        assertEquals("CREA-PE 99999", response.getCrea());
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("atualizarPerfil parcial deve manter inalterados campos nulos")
    void atualizarPerfil_parcial() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioUpdateDTO update = new UsuarioUpdateDTO();
        update.setTelefone("81911112222");

        UsuarioResponseDTO response = usuarioService.atualizarPerfil(10L, update);

        assertEquals("Carlos Silva", response.getName());
        assertEquals("81911112222", response.getTelefone());
        assertEquals("CREA-PE 12345", response.getCrea());
        assertEquals("Engenharia Ltda", response.getEmpresa());
    }

    @Test
    @DisplayName("atualizarPerfil deve lançar BusinessException se corpo for nulo")
    void atualizarPerfil_corpoNulo() {
        assertThrows(BusinessException.class, () -> usuarioService.atualizarPerfil(10L, null));
    }

    @Test
    @DisplayName("atualizarPerfil deve lançar ValidationException se nome tiver menos de 3 caracteres")
    void atualizarPerfil_nomeInvalido() {
        UsuarioUpdateDTO update = new UsuarioUpdateDTO();
        update.setName("Jo");

        assertThrows(ValidationException.class, () -> usuarioService.atualizarPerfil(10L, update));
    }

    @Test
    @DisplayName("alterarSenha com dados válidos deve criptografar e atualizar a senha")
    void alterarSenha_sucesso() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("SenhaAtual123", "hashSenha123")).thenReturn(true);
        when(passwordEncoder.encode("NovaSenha123")).thenReturn("novoHashSeguro");

        AlterarSenhaDTO dto = new AlterarSenhaDTO("SenhaAtual123", "NovaSenha123", "NovaSenha123");
        usuarioService.alterarSenha(10L, dto);

        assertEquals("novoHashSeguro", sampleUser.getPassword());
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("alterarSenha com senha atual incorreta deve lançar ValidationException")
    void alterarSenha_senhaAtualIncorreta() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("SenhaErrada123", "hashSenha123")).thenReturn(false);

        AlterarSenhaDTO dto = new AlterarSenhaDTO("SenhaErrada123", "NovaSenha123", "NovaSenha123");

        ValidationException ex = assertThrows(ValidationException.class, () -> usuarioService.alterarSenha(10L, dto));
        assertTrue(ex.getErrors().containsKey("senhaAtual"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("alterarSenha com senhas divergentes deve lançar ValidationException")
    void alterarSenha_senhasDivergentes() {
        AlterarSenhaDTO dto = new AlterarSenhaDTO("SenhaAtual123", "NovaSenha123", "OutraSenha456");

        ValidationException ex = assertThrows(ValidationException.class, () -> usuarioService.alterarSenha(10L, dto));
        assertTrue(ex.getErrors().containsKey("confirmacaoNovaSenha"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("alterarSenha com senha fraca (sem número) deve lançar ValidationException")
    void alterarSenha_senhaFraca() {
        AlterarSenhaDTO dto = new AlterarSenhaDTO("SenhaAtual123", "SenhaSemNumero", "SenhaSemNumero");

        ValidationException ex = assertThrows(ValidationException.class, () -> usuarioService.alterarSenha(10L, dto));
        assertTrue(ex.getErrors().containsKey("novaSenha"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("alterarSenha com corpo nulo deve lançar BusinessException")
    void alterarSenha_corpoNulo() {
        assertThrows(BusinessException.class, () -> usuarioService.alterarSenha(10L, null));
    }
}
