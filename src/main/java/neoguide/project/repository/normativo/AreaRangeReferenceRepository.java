package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.AreaRangeReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AreaRangeReferenceRepository extends JpaRepository<AreaRangeReference, Long> {
    Optional<AreaRangeReference> findByFaixa(String faixa);
    List<AreaRangeReference> findAllByOrderByMinAreaM2Asc();
}
