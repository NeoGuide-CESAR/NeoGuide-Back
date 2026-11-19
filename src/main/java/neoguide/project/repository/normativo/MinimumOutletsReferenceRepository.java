package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.MinimumOutletsReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MinimumOutletsReferenceRepository extends JpaRepository<MinimumOutletsReference, Long> {
    List<MinimumOutletsReference> findAllByOrderByMinAreaM2Asc();
}
