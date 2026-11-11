package neoguide.project.service;

import neoguide.project.dto.catalogo.OccupancyLoadResponse;
import neoguide.project.dto.catalogo.PoolPumpFactorResponse;
import neoguide.project.dto.catalogo.VeFactorResponse;
import neoguide.project.repository.normativo.OccupancyLoadReferenceRepository;
import neoguide.project.repository.normativo.PoolPumpDemandFactorRepository;
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

    public CatalogoNormativoService(VeFactorReferenceRepository veFactorRepository,
                                    OccupancyLoadReferenceRepository occupancyLoadRepository,
                                    PoolPumpDemandFactorRepository poolPumpRepository) {
        this.veFactorRepository = veFactorRepository;
        this.occupancyLoadRepository = occupancyLoadRepository;
        this.poolPumpRepository = poolPumpRepository;
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
}
