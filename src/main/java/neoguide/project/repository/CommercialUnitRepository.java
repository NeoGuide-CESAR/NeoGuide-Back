package neoguide.project.repository;

import neoguide.project.model.CommercialUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommercialUnitRepository extends JpaRepository<CommercialUnit, Long> {

    List<CommercialUnit> findByProjectIdOrderByIdAsc(Long projectId);

    List<CommercialUnit> findByProjectId(Long projectId);

    @Modifying
    @Query("DELETE FROM CommercialUnit c WHERE c.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}
