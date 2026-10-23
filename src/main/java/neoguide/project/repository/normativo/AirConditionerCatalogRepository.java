package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.AirConditionerCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirConditionerCatalogRepository extends JpaRepository<AirConditionerCatalog, Long> {
    Optional<AirConditionerCatalog> findByCapacidadeBtu(Integer capacidadeBtu);
    List<AirConditionerCatalog> findAllByOrderByCapacidadeBtuAsc();
}
