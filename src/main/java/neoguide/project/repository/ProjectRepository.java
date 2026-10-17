package neoguide.project.repository;

import neoguide.project.model.Project;
import neoguide.project.model.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByOrderByDataSubmissaoDesc();

    List<Project> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT p FROM Project p WHERE p.user.id = :userId " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (CAST(:termo AS string) IS NULL " +
           "     OR LOWER(p.titulo) LIKE LOWER(CONCAT('%', CAST(:termo AS string), '%')) " +
           "     OR LOWER(p.protocolo) LIKE LOWER(CONCAT('%', CAST(:termo AS string), '%')) " +
           "     OR LOWER(p.municipio) LIKE LOWER(CONCAT('%', CAST(:termo AS string), '%')))")
    Page<Project> findByUserWithFilters(@Param("userId") Long userId,
                                        @Param("status") ProjectStatus status,
                                        @Param("termo") String termo,
                                        Pageable pageable);

    @Query("SELECT p.protocolo FROM Project p WHERE p.protocolo LIKE :prefix ORDER BY p.protocolo DESC LIMIT 1")
    Optional<String> findLastProtocoloByPrefix(@Param("prefix") String prefix);
}