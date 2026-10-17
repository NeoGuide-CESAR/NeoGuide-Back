package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.MotorDemandFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MotorDemandFactorRepository extends JpaRepository<MotorDemandFactor, Long> {
    Optional<MotorDemandFactor> findByTipoEnquadramento(String tipoEnquadramento);
}
