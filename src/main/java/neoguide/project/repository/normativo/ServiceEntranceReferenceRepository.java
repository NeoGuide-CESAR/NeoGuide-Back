package neoguide.project.repository.normativo;

import neoguide.project.model.normativo.ServiceEntranceReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceEntranceReferenceRepository extends JpaRepository<ServiceEntranceReference, Long> {
    List<ServiceEntranceReference> findByTensaoFornecimento(String tensaoFornecimento);
}
