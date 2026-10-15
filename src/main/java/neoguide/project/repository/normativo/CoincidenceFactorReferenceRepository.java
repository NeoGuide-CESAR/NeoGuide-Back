package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.CoincidenceFactorReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoincidenceFactorReferenceRepository extends JpaRepository<CoincidenceFactorReference, Long> {
    Optional<CoincidenceFactorReference> findByTotalApartamentos(Integer totalApartamentos);
}
