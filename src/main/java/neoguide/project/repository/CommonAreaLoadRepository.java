package neoguide.project.repository;

import neoguide.project.model.CommonAreaLoad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommonAreaLoadRepository extends JpaRepository<CommonAreaLoad, Long> {

    List<CommonAreaLoad> findByProjectIdOrderByIdAsc(Long projectId);

    List<CommonAreaLoad> findByProjectId(Long projectId);

    @Modifying
    @Query("DELETE FROM CommonAreaLoad c WHERE c.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}
