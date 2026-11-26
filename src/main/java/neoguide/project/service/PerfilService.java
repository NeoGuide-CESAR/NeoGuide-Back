package neoguide.project.service;

import neoguide.project.dto.perfil.PerfilResponse;
import neoguide.project.dto.perfil.PerfilUpdateRequest;
import neoguide.project.dto.perfil.SenhaRequest;
import neoguide.project.exception.PerfilRecusadoException;
import neoguide.project.exception.ResourceNotFoundException;
import neoguide.project.model.User;
import neoguide.project.model.UserRole;
import neoguide.project.repository.UserRepository;
import neoguide.project.security.jwt.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

/**
 * Perfil do usuário logado: ver e alterar nome, telefone, e-mail e registro profissional, e trocar a senha.
 * O id vem sempre do token, nunca do corpo; CPF/CNPJ e perfil não mudam por aqui.
 */
@Service
public class PerfilService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int REGISTRO_MAX = 30;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public PerfilService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public PerfilResponse obter(Long userId) {
        return resposta(buscar(userId), null);
    }

    @Transactional
    public PerfilResponse atualizar(Long userId, PerfilUpdateRequest r) {
        User user = buscar(userId);
        PerfilUpdateRequest dados = r == null ? new PerfilUpdateRequest(null, null, null, null, null) : r;

        String nome = dados.nome() == null ? "" : dados.nome().trim().replaceAll("\\s+", " ");
        if (nome.length() < 3 || nome.length() > 100 || !nome.contains(" ")) {
            throw recusa(422, "nome_invalido", "nome", "Informe seu nome completo.");
        }

        String telefone = null;
        if (dados.telefone() != null && !dados.telefone().isBlank()) {
            String digitos = dados.telefone().replaceAll("\\D", "");
            if (digitos.length() != 10 && digitos.length() != 11) {
                throw recusa(422, "telefone_invalido", "telefone", "Telefone inválido.");
            }
            telefone = digitos;
        }

        String email = dados.email() == null ? "" : dados.email().trim().toLowerCase();
        if (email.isEmpty() || email.length() > 150 || !EMAIL.matcher(email).matches()) {
            throw recusa(422, "email_invalido", "email", "Informe um e-mail válido.");
        }

        String registro = dados.registroProfissional() == null ? "" : dados.registroProfissional().trim();
        if (registro.length() > REGISTRO_MAX || (registro.isEmpty() && user.getRole() == UserRole.ROLE_PROJETISTA)) {
            throw recusa(422, "registro_invalido", "registroProfissional", "Informe seu registro profissional.");
        }

        boolean trocouEmail = !email.equalsIgnoreCase(user.getEmail());
        if (trocouEmail) {
            if (dados.senhaAtual() == null || dados.senhaAtual().isEmpty()
                    || !passwordEncoder.matches(dados.senhaAtual(), user.getPassword())) {
                throw recusa(422, "senha_incorreta", "senhaAtual", "Para trocar o e-mail, confirme sua senha atual.");
            }
            if (userRepository.findByEmail(email).filter(outro -> !outro.getId().equals(userId)).isPresent()) {
                throw recusa(409, "email_em_uso", "email", "Este e-mail já está em uso.");
            }
            user.setEmail(email);
        }

        user.setName(nome);
        user.setTelefone(telefone);
        user.setCrea(registro.isEmpty() ? null : registro);
        userRepository.save(user);
        return resposta(user, trocouEmail ? jwtService.generateToken(user) : null);
    }

    @Transactional
    public void alterarSenha(Long userId, SenhaRequest r) {
        User user = buscar(userId);
        String atual = r == null ? null : r.senhaAtual();
        String nova = r == null ? null : r.novaSenha();

        if (atual == null || atual.isEmpty() || !passwordEncoder.matches(atual, user.getPassword())) {
            throw recusa(422, "senha_incorreta", "senhaAtual", "A senha atual está incorreta.");
        }
        if (nova == null || nova.length() < 8 || !nova.matches(".*[A-Za-z].*") || !nova.matches(".*\\d.*")) {
            throw recusa(422, "senha_fraca", "novaSenha", "A nova senha precisa ter 8 caracteres, com letras e números.");
        }
        if (nova.equals(atual)) {
            throw recusa(422, "senha_igual", "novaSenha", "A nova senha deve ser diferente da atual.");
        }
        user.setPassword(passwordEncoder.encode(nova));
        userRepository.save(user);
    }

    // ------------------------------------------------------------------ utilidades

    private User buscar(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    private static PerfilRecusadoException recusa(int status, String regra, String campo, String mensagem) {
        return new PerfilRecusadoException(status, regra, campo, mensagem);
    }

    private static PerfilResponse resposta(User u, String token) {
        return new PerfilResponse(u.getId(), u.getName(), u.getEmail(), formatarDocumento(u.getCpfCnpj()), u.getTelefone(),
                u.getCrea(), u.getRole() == null ? null : u.getRole().name().replace("ROLE_", ""), token);
    }

    /** 000.000.000-00 para CPF e 00.000.000/0000-00 para CNPJ; outro formato volta como está. */
    static String formatarDocumento(String documento) {
        if (documento == null) return null;
        String d = documento.replaceAll("\\D", "");
        if (d.length() == 11) {
            return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
        }
        if (d.length() == 14) {
            return d.substring(0, 2) + "." + d.substring(2, 5) + "." + d.substring(5, 8) + "/" + d.substring(8, 12) + "-" + d.substring(12);
        }
        return documento;
    }
}
