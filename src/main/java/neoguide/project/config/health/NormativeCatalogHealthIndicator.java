package neoguide.project.config.health;

import neoguide.project.repository.normativo.AreaRangeReferenceRepository;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("normativeCatalog")
public class NormativeCatalogHealthIndicator implements HealthIndicator {

    static final long MIN_AREA_RANGES = 39L;

    private final AreaRangeReferenceRepository areaRangeReferenceRepository;

    public NormativeCatalogHealthIndicator(AreaRangeReferenceRepository areaRangeReferenceRepository) {
        this.areaRangeReferenceRepository = areaRangeReferenceRepository;
    }

    @Override
    public Health health() {
        try {
            long count = areaRangeReferenceRepository.count();
            if (count >= MIN_AREA_RANGES) {
                return Health.up()
                        .withDetail("faixasArea", count)
                        .withDetail("minimoEsperado", MIN_AREA_RANGES)
                        .build();
            }
            return Health.down()
                    .withDetail("faixasArea", count)
                    .withDetail("minimoEsperado", MIN_AREA_RANGES)
                    .withDetail("motivo", "Catálogo normativo incompleto (Quadro 35)")
                    .build();
        } catch (Exception ex) {
            return Health.down(ex)
                    .withDetail("minimoEsperado", MIN_AREA_RANGES)
                    .build();
        }
    }
}
