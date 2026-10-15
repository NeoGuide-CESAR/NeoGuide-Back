package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.MotorStarterReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MotorStarterReferenceRepository extends JpaRepository<MotorStarterReference, Long> {
    Optional<MotorStarterReference> findByTipoPartida(String tipoPartida);
}
