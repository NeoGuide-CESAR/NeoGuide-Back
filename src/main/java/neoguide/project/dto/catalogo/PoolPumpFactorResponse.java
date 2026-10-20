package neoguide.project.dto.catalogo;

import neoguide.project.model.normativo.PoolPumpDemandFactor;
import java.math.BigDecimal;

public record PoolPumpFactorResponse(
        Long id,
        Integer quantidadeBombas,
        BigDecimal fatorDemanda,
        BigDecimal fatorPotencia,
        String normaOrigem
) {
    public static PoolPumpFactorResponse fromEntity(PoolPumpDemandFactor entity) {
        if (entity == null) {
            return null;
        }
        return new PoolPumpFactorResponse(
                entity.getId(),
                entity.getQuantidadeBombas(),
                entity.getFatorDemanda(),
                entity.getFatorPotencia(),
                entity.getNormaOrigem()
        );
    }
}
