package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.SinglePhaseMotorReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SinglePhaseMotorReferenceRepository extends JpaRepository<SinglePhaseMotorReference, Long> {
    Optional<SinglePhaseMotorReference> findByPotenciaCv(String potenciaCv);
}
