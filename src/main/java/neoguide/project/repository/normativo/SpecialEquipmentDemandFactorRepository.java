package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.SpecialEquipmentDemandFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpecialEquipmentDemandFactorRepository extends JpaRepository<SpecialEquipmentDemandFactor, Long> {
    Optional<SpecialEquipmentDemandFactor> findByTipoEquipamento(String tipoEquipamento);
}
