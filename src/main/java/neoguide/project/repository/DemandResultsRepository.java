package neoguide.project.repository;

import neoguide.project.model.DemandResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DemandResultsRepository extends JpaRepository<DemandResults, Long> {

    Optional<DemandResults> findByProjectId(Long projectId);

    void deleteByProjectId(Long projectId);
}
