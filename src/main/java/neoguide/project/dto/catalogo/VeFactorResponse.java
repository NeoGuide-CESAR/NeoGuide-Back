package neoguide.project.dto.catalogo;

import neoguide.project.model.normativo.VeFactorReference;
import java.math.BigDecimal;

public record VeFactorResponse(
        Long id,
        Integer maxVagas,
        BigDecimal fatorSimultaneidade,
        String faixaDescricao,
        String normaOrigem
) {
    public static VeFactorResponse fromEntity(VeFactorReference entity) {
        if (entity == null) {
            return null;
        }
        return new VeFactorResponse(
                entity.getId(),
                entity.getMaxVagas(),
                entity.getFatorSimultaneidade(),
                entity.getFaixaDescricao(),
                entity.getNormaOrigem()
        );
    }
}
