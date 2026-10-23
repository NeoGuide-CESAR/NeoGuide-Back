package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.AcDemandFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcDemandFactorRepository extends JpaRepository<AcDemandFactor, Long> {
    List<AcDemandFactor> findAllByOrderByMinAparelhosAsc();
}
