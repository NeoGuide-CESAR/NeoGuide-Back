package neoguide.project.repository;

import neoguide.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByCpfCnpj(String cpfCnpj);

    boolean existsByCpfCnpj(String cpfCnpj);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:login) OR u.cpfCnpj = :login OR u.cpfCnpj = :sanitized")
    Optional<User> findByEmailOrCpfCnpj(@Param("login") String login, @Param("sanitized") String sanitized);

    default Optional<User> findByEmailOrCpfCnpj(String login) {
        String sanitized = login != null ? login.replaceAll("[^0-9]", "") : "";
        return findByEmailOrCpfCnpj(login, sanitized);
    }
}