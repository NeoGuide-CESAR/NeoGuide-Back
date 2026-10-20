package neoguide.project.repository;

import neoguide.project.model.SpecialLoads;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpecialLoadsRepository extends JpaRepository<SpecialLoads, Long> {

    Optional<SpecialLoads> findByProjectId(Long projectId);

    boolean existsByProjectId(Long projectId);

    @Modifying
    @Query("DELETE FROM SpecialLoads s WHERE s.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}
