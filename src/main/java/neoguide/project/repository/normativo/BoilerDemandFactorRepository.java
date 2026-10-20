package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.BoilerDemandFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoilerDemandFactorRepository extends JpaRepository<BoilerDemandFactor, Long> {
    Optional<BoilerDemandFactor> findByQuantidadeBoilers(Integer quantidadeBoilers);
    List<BoilerDemandFactor> findAllByOrderByQuantidadeBoilersAsc();
}
