package neoguide.project.config;

import neoguide.project.model.User;
import neoguide.project.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Popula o banco H2 com usuários de teste ao subir a aplicação.
 * As senhas são codificadas com o mesmo {@link PasswordEncoder} usado no cadastro,
 * então é possível logar direto com elas.
 *
 * Usuários criados (e-mail / senha):
 *   admin@neoguide.com   / admin123
 *   maria@neoguide.com   / maria123
 *   joao@neoguide.com    / joao1234
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        List<User> users = List.of(
                new User("Administrador", "admin@neoguide.com", passwordEncoder.encode("admin123")),
                new User("Maria Silva", "maria@neoguide.com", passwordEncoder.encode("maria123")),
                new User("João Souza", "joao@neoguide.com", passwordEncoder.encode("joao1234"))
        );

        userRepository.saveAll(users);
    }
}