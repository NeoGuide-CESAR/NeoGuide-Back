package neoguide.project.repository;

import neoguide.project.model.DemandResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DemandResultRepository extends JpaRepository<DemandResult, Long> {

    Optional<DemandResult> findTopByProjectIdOrderByCalculadoEmDescIdDesc(Long projectId);

    void deleteByProjectId(Long projectId);
}
