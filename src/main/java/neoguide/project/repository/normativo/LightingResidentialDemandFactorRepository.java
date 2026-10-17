package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.LightingResidentialDemandFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LightingResidentialDemandFactorRepository extends JpaRepository<LightingResidentialDemandFactor, Long> {
}
