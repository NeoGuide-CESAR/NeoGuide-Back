package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.SafetyFactorReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SafetyFactorReferenceRepository extends JpaRepository<SafetyFactorReference, Long> {
}
