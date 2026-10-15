package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.CooktopDemandFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CooktopDemandFactorRepository extends JpaRepository<CooktopDemandFactor, Long> {
    Optional<CooktopDemandFactor> findByQuantidadeAparelhos(Integer quantidadeAparelhos);
}
