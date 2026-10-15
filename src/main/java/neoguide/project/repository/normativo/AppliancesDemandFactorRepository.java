package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.AppliancesDemandFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppliancesDemandFactorRepository extends JpaRepository<AppliancesDemandFactor, Long> {
}
