package neoguide.project.service;

import neoguide.project.dto.AlterarSenhaDTO;
import neoguide.project.dto.UsuarioResponseDTO;
import neoguide.project.dto.UsuarioUpdateDTO;
import neoguide.project.exception.BusinessException;
import neoguide.project.exception.ResourceNotFoundException;
import neoguide.project.model.User;
import neoguide.project.repository.UserRepository;
import neoguide.project.validation.Validator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO getPerfil(Long userId) {
        User user = findUserById(userId);
        return UsuarioResponseDTO.from(user);
    }

    @Transactional
    public UsuarioResponseDTO atualizarPerfil(Long userId, UsuarioUpdateDTO dto) {
        if (dto == null) {
            throw new BusinessException("Corpo da requisição é obrigatório");
        }

        Validator validator = Validator.create();
        if (dto.getName() != null) {
            validator.length("name", dto.getName(), 3, 100);
        }
        if (dto.getTelefone() != null) {
            validator.length("telefone", dto.getTelefone(), 8, 20);
        }
        if (dto.getEmpresa() != null) {
            validator.length("empresa", dto.getEmpresa(), 2, 150);
        }
        if (dto.getCrea() != null) {
            validator.length("crea", dto.getCrea(), 3, 50);
        }
        validator.validate();

        User user = findUserById(userId);

        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName().trim());
        }
        if (dto.getTelefone() != null) {
            user.setTelefone(dto.getTelefone().trim());
        }
        if (dto.getEmpresa() != null) {
            user.setEmpresa(dto.getEmpresa().trim());
        }
        if (dto.getCrea() != null) {
            user.setCrea(dto.getCrea().trim());
        }

        User saved = userRepository.save(user);
        return UsuarioResponseDTO.from(saved);
    }

    @Transactional
    public void alterarSenha(Long userId, AlterarSenhaDTO dto) {
        if (dto == null) {
            throw new BusinessException("Corpo da requisição é obrigatório");
        }

        Validator.create()
                .required("senhaAtual", dto.getSenhaAtual())
                .required("novaSenha", dto.getNovaSenha())
                .length("novaSenha", dto.getNovaSenha(), 8, 64)
                .strongPassword("novaSenha", dto.getNovaSenha())
                .required("confirmacaoNovaSenha", dto.getConfirmacaoNovaSenha())
                .sameAs("confirmacaoNovaSenha", dto.getNovaSenha(), dto.getConfirmacaoNovaSenha(),
                        "As senhas não conferem")
                .validate();

        User user = findUserById(userId);

        if (!passwordEncoder.matches(dto.getSenhaAtual(), user.getPassword())) {
            Validator.create()
                    .custom("senhaAtual", false, "Senha atual incorreta")
                    .validate();
        }

        user.setPassword(passwordEncoder.encode(dto.getNovaSenha()));
        userRepository.save(user);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userId));
    }
}
