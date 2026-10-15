package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.PoolPumpDemandFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PoolPumpDemandFactorRepository extends JpaRepository<PoolPumpDemandFactor, Long> {
    Optional<PoolPumpDemandFactor> findByQuantidadeBombas(Integer quantidadeBombas);
}
