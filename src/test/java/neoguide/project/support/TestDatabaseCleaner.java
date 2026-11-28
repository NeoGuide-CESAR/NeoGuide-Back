package neoguide.project.support;

import neoguide.project.repository.CommercialUnitRepository;
import neoguide.project.repository.CommonAreaLoadRepository;
import neoguide.project.repository.DemandResultRepository;
import neoguide.project.repository.DemandResultsRepository;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.ResidentialTypologyRepository;
import neoguide.project.repository.SpecialLoadsRepository;
import neoguide.project.repository.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Helper centralizado para limpeza das tabelas operacionais em testes de integração,
 * respeitando a ordem de integridade referencial (chaves estrangeiras) sem afetar
 * o catálogo normativo.
 */
@Component
@Profile("test")
public class TestDatabaseCleaner {

    private final DemandResultRepository demandResultRepository;
    private final DemandResultsRepository demandResultsRepository;
    private final ResidentialTypologyRepository residentialTypologyRepository;
    private final CommonAreaLoadRepository commonAreaLoadRepository;
    private final CommercialUnitRepository commercialUnitRepository;
    private final SpecialLoadsRepository specialLoadsRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TestDatabaseCleaner(
            DemandResultRepository demandResultRepository,
            DemandResultsRepository demandResultsRepository,
            ResidentialTypologyRepository residentialTypologyRepository,
            CommonAreaLoadRepository commonAreaLoadRepository,
            CommercialUnitRepository commercialUnitRepository,
            SpecialLoadsRepository specialLoadsRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository) {
        this.demandResultRepository = demandResultRepository;
        this.demandResultsRepository = demandResultsRepository;
        this.residentialTypologyRepository = residentialTypologyRepository;
        this.commonAreaLoadRepository = commonAreaLoadRepository;
        this.commercialUnitRepository = commercialUnitRepository;
        this.specialLoadsRepository = specialLoadsRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void cleanOperationalTables() {
        demandResultRepository.deleteAllInBatch();
        demandResultsRepository.deleteAllInBatch();
        residentialTypologyRepository.deleteAllInBatch();
        commonAreaLoadRepository.deleteAllInBatch();
        commercialUnitRepository.deleteAllInBatch();
        specialLoadsRepository.deleteAllInBatch();
        projectRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }
}
