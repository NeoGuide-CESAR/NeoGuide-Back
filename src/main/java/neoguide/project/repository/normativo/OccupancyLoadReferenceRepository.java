package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.OccupancyLoadReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OccupancyLoadReferenceRepository extends JpaRepository<OccupancyLoadReference, Long> {
    Optional<OccupancyLoadReference> findByOcupacao(String ocupacao);
}
