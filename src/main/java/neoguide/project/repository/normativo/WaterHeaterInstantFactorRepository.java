package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.WaterHeaterInstantFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaterHeaterInstantFactorRepository extends JpaRepository<WaterHeaterInstantFactor, Long> {
    Optional<WaterHeaterInstantFactor> findByQuantidadeAparelhos(Integer quantidadeAparelhos);
    List<WaterHeaterInstantFactor> findAllByOrderByQuantidadeAparelhosAsc();
}
