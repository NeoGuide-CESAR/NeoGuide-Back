package neoguide.project.repository;

import neoguide.project.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByOrderByDataSubmissaoDesc();

    List<Project> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT p.protocolo FROM Project p WHERE p.protocolo LIKE :prefix ORDER BY p.protocolo DESC LIMIT 1")
    Optional<String> findLastProtocoloByPrefix(@Param("prefix") String prefix);
}