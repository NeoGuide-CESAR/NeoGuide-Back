package neoguide.project.dto.catalogo;

import neoguide.project.model.normativo.ServiceEntranceReference;
import java.math.BigDecimal;

public record ServiceEntranceCatalogResponse(
        Long id,
        String tensaoFornecimento,
        BigDecimal maxDemandaKva,
        Integer circuitos,
        String condutorMm2,
        String dutoPead,
        String dutoAco,
        Integer correnteA,
        String disjuntorCarcaca,
        Integer disjuntorIn,
        String normaOrigem
) {
    public static ServiceEntranceCatalogResponse fromEntity(ServiceEntranceReference entity) {
        if (entity == null) return null;
        return new ServiceEntranceCatalogResponse(
                entity.getId(),
                entity.getTensaoFornecimento(),
                entity.getMaxDemandaKva(),
                entity.getQuantidadeCircuitos(),
                entity.getSecaoCondutorMm2(),
                entity.getDutoPeadMm(),
                entity.getDutoAcoMm(),
                entity.getCorrenteTotalA(),
                entity.getDisjuntorCarcaca(),
                entity.getDisjuntorCorrenteA(),
                entity.getNormaOrigem()
        );
    }
}
