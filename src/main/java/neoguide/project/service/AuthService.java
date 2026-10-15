package neoguide.project.service;

import neoguide.project.dto.LoginResponse;
import neoguide.project.dto.LoginRequest;
import neoguide.project.dto.RegisterRequest;
import neoguide.project.dto.UserResponse;
import neoguide.project.exception.BusinessException;
import neoguide.project.exception.UnauthorizedException;
import neoguide.project.model.User;
import neoguide.project.repository.UserRepository;
import neoguide.project.security.jwt.JwtService;
import neoguide.project.validation.Validator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        User user = new User(
                request.getName().trim(),
                email,
                passwordEncoder.encode(request.getPassword())
        );

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        validateLogin(request);

        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("E-mail ou senha inválidos"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("E-mail ou senha inválidos");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(token, "Bearer", UserResponse.from(user));
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

        Validator.create()
                .required("email", request.getEmail())
                .email("email", request.getEmail())
                .required("password", request.getPassword())
                .validate();
    }
}