package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.BusbarReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusbarReferenceRepository extends JpaRepository<BusbarReference, Long> {
}
