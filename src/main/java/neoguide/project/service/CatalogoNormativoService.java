package neoguide.project.service;

import neoguide.project.dto.catalogo.BusbarCatalogResponse;
import neoguide.project.dto.catalogo.OccupancyLoadResponse;
import neoguide.project.dto.catalogo.PoolPumpFactorResponse;
import neoguide.project.dto.catalogo.ServiceEntranceCatalogResponse;
import neoguide.project.dto.catalogo.VeFactorResponse;
import neoguide.project.repository.normativo.BusbarReferenceRepository;
import neoguide.project.repository.normativo.OccupancyLoadReferenceRepository;
import neoguide.project.repository.normativo.PoolPumpDemandFactorRepository;
import neoguide.project.repository.normativo.ServiceEntranceReferenceRepository;
import neoguide.project.repository.normativo.VeFactorReferenceRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogoNormativoService {

    private final VeFactorReferenceRepository veFactorRepository;
    private final OccupancyLoadReferenceRepository occupancyLoadRepository;
    private final PoolPumpDemandFactorRepository poolPumpRepository;
    private final ServiceEntranceReferenceRepository serviceEntranceRepository;
    private final BusbarReferenceRepository busbarRepository;

    public CatalogoNormativoService(VeFactorReferenceRepository veFactorRepository,
                                    OccupancyLoadReferenceRepository occupancyLoadRepository,
                                    PoolPumpDemandFactorRepository poolPumpRepository,
                                    ServiceEntranceReferenceRepository serviceEntranceRepository,
                                    BusbarReferenceRepository busbarRepository) {
        this.veFactorRepository = veFactorRepository;
        this.occupancyLoadRepository = occupancyLoadRepository;
        this.poolPumpRepository = poolPumpRepository;
        this.serviceEntranceRepository = serviceEntranceRepository;
        this.busbarRepository = busbarRepository;
    }

    @Cacheable("catalogo_fatores_ve")
    public List<VeFactorResponse> getFatoresRecargaVe() {
        return veFactorRepository.findAllByOrderByMaxVagasAsc()
                .stream()
                .map(VeFactorResponse::fromEntity)
                .toList();
    }

    @Cacheable("catalogo_cargas_ocupacao")
    public List<OccupancyLoadResponse> getCargasMinimasOcupacao() {
        return occupancyLoadRepository.findAllByOrderByOcupacaoAsc()
                .stream()
                .map(OccupancyLoadResponse::fromEntity)
                .toList();
    }

    @Cacheable("catalogo_fatores_piscina")
    public List<PoolPumpFactorResponse> getFatoresBombasPiscina() {
        return poolPumpRepository.findAllByOrderByQuantidadeBombasAsc()
                .stream()
                .map(PoolPumpFactorResponse::fromEntity)
                .toList();
    }

    @Cacheable("catalogo_entrada_380v")
    public List<ServiceEntranceCatalogResponse> getDimensionamentoEntrada380v() {
        return serviceEntranceRepository.findByTensaoFornecimentoOrderByMaxDemandaKvaAsc("380_220V")
                .stream()
                .map(ServiceEntranceCatalogResponse::fromEntity)
                .toList();
    }

    @Cacheable("catalogo_entrada_220v")
    public List<ServiceEntranceCatalogResponse> getDimensionamentoEntrada220v() {
        return serviceEntranceRepository.findByTensaoFornecimentoOrderByMaxDemandaKvaAsc("220_127V")
                .stream()
                .map(ServiceEntranceCatalogResponse::fromEntity)
                .toList();
    }

    @Cacheable("catalogo_barramentos_qdg")
    public List<BusbarCatalogResponse> getBarramentosQdg() {
        return busbarRepository.findAllByOrderByMaxDemandaKvaAsc()
                .stream()
                .map(BusbarCatalogResponse::fromEntity)
                .toList();
    }
}
