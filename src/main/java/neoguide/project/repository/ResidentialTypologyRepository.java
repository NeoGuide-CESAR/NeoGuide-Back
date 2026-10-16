package neoguide.project.repository;

import neoguide.project.model.ResidentialTypology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResidentialTypologyRepository extends JpaRepository<ResidentialTypology, Long> {

    List<ResidentialTypology> findByProjectIdOrderByIdAsc(Long projectId);

    @Modifying
    @Query("DELETE FROM ResidentialTypology r WHERE r.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}
