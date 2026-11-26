package neoguide.project.repository;

import neoguide.project.model.Project;
import neoguide.project.model.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByOrderByDataSubmissaoDesc();

    List<Project> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<Project> findAllByUserIdAndAtivoTrueOrderByCreatedAtDesc(Long userId);

    Optional<Project> findByIdAndAtivoTrue(Long id);

    /**
     * Projetos ativos do usuário, filtrados por status (uma lista; sempre informada) e busca textual
     * em nome, protocolo, município e endereço.
     */
    @Query("SELECT p FROM Project p WHERE p.user.id = :userId " +
           "AND p.ativo = true " +
           "AND p.status IN :statuses " +
           "AND (CAST(:termo AS string) IS NULL " +
           "     OR LOWER(p.titulo) LIKE LOWER(CONCAT('%', CAST(:termo AS string), '%')) " +
           "     OR LOWER(p.protocolo) LIKE LOWER(CONCAT('%', CAST(:termo AS string), '%')) " +
           "     OR LOWER(p.municipio) LIKE LOWER(CONCAT('%', CAST(:termo AS string), '%')) " +
           "     OR LOWER(p.endereco) LIKE LOWER(CONCAT('%', CAST(:termo AS string), '%')))")
    Page<Project> findByUserWithFilters(@Param("userId") Long userId,
                                        @Param("statuses") Collection<ProjectStatus> statuses,
                                        @Param("termo") String termo,
                                        Pageable pageable);

    /** Contagem por status com a mesma busca textual (sem filtro de status). Retorna [status, quantidade]. */
    @Query("SELECT p.status, COUNT(p) FROM Project p WHERE p.user.id = :userId " +
           "AND p.ativo = true " +
           "AND (CAST(:termo AS string) IS NULL " +
           "     OR LOWER(p.titulo) LIKE LOWER(CONCAT('%', CAST(:termo AS string), '%')) " +
           "     OR LOWER(p.protocolo) LIKE LOWER(CONCAT('%', CAST(:termo AS string), '%')) " +
           "     OR LOWER(p.municipio) LIKE LOWER(CONCAT('%', CAST(:termo AS string), '%')) " +
           "     OR LOWER(p.endereco) LIKE LOWER(CONCAT('%', CAST(:termo AS string), '%'))) " +
           "GROUP BY p.status")
    List<Object[]> contarPorStatus(@Param("userId") Long userId, @Param("termo") String termo);

    @Query("SELECT p.protocolo FROM Project p WHERE p.protocolo LIKE :prefix ORDER BY p.protocolo DESC LIMIT 1")
    Optional<String> findLastProtocoloByPrefix(@Param("prefix") String prefix);
}
