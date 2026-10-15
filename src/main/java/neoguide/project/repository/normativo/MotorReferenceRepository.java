package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.MotorReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface MotorReferenceRepository extends JpaRepository<MotorReference, Long> {
    Optional<MotorReference> findByPotenciaCv(BigDecimal potenciaCv);
}
