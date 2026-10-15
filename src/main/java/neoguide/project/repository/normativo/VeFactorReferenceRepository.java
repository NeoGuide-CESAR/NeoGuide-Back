package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.VeFactorReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VeFactorReferenceRepository extends JpaRepository<VeFactorReference, Long> {
    Optional<VeFactorReference> findByMaxVagas(Integer maxVagas);
}
