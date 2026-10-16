package neoguide.project.service;

import neoguide.project.dto.LoginRequest;
import neoguide.project.dto.LoginResponse;
import neoguide.project.dto.RegisterRequest;
import neoguide.project.dto.UserResponse;
import neoguide.project.exception.BusinessException;
import neoguide.project.exception.ConflictException;
import neoguide.project.exception.UnauthorizedException;
import neoguide.project.model.User;
import neoguide.project.model.UserRole;
import neoguide.project.repository.UserRepository;
import neoguide.project.security.jwt.JwtService;
import neoguide.project.validation.Validator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        validateRegister(request);

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("E-mail já cadastrado");
        }

        String rawCpfCnpj = request.getCpfCnpj();
        String sanitizedCpfCnpj = sanitizeCpfCnpj(rawCpfCnpj);

        if (sanitizedCpfCnpj != null && !sanitizedCpfCnpj.isBlank()) {
            if (userRepository.existsByCpfCnpj(sanitizedCpfCnpj)
                    || (rawCpfCnpj != null && userRepository.existsByCpfCnpj(rawCpfCnpj.trim()))) {
                throw new ConflictException("CPF/CNPJ já cadastrado");
            }
        }

        UserRole role = request.getRole() != null ? request.getRole() : UserRole.ROLE_PROJETISTA;

        User user = new User(
                request.getName().trim(),
                email,
                passwordEncoder.encode(request.getPassword()),
                sanitizedCpfCnpj,
                role
        );

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        validateLogin(request);

        String login = request.getLogin().trim();
        String sanitized = sanitizeCpfCnpj(login);
        String searchSanitized = (sanitized != null && !sanitized.isBlank()) ? sanitized : login;

        User user = userRepository.findByEmailOrCpfCnpj(login, searchSanitized)
                .or(() -> (sanitized != null && !sanitized.isBlank()) ? userRepository.findByCpfCnpj(sanitized) : Optional.empty())
                .or(() -> login.contains("@") ? userRepository.findByEmail(login.toLowerCase()) : Optional.empty())
                .orElseThrow(() -> new UnauthorizedException("E-mail ou senha inválidos"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("E-mail ou senha inválidos");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(token, "Bearer", UserResponse.from(user));
    }

    private String sanitizeCpfCnpj(String document) {
        if (document == null) {
            return null;
        }
        String sanitized = document.replaceAll("[^0-9]", "");
        return sanitized.isBlank() ? null : sanitized;
    }

    private void validateRegister(RegisterRequest request) {
        if (request == null) {
            throw new BusinessException("Corpo da requisição é obrigatório");
        }

        Validator.create()
                .required("name", request.getName())
                .length("name", request.getName(), 3, 100)
                .required("email", request.getEmail())
                .length("email", request.getEmail(), 5, 150)
                .email("email", request.getEmail())
                .required("password", request.getPassword())
                .length("password", request.getPassword(), 8, 64)
                .strongPassword("password", request.getPassword())
                .required("confirmPassword", request.getConfirmPassword())
                .sameAs("confirmPassword", request.getPassword(), request.getConfirmPassword(),
                        "As senhas não conferem")
                .validate();
    }

    private void validateLogin(LoginRequest request) {
        if (request == null) {
            throw new BusinessException("Corpo da requisição é obrigatório");
        }

        String login = request.getLogin();
        Validator.create()
                .required("login", login)
                .required("password", request.getPassword())
                .validate();
    }
}