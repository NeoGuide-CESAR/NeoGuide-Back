package neoguide.project.dto.validation;

import java.time.LocalDateTime;
import java.util.List;

public record NormativeValidationResponse(
    Long projectId,
    String protocolo,
    boolean conforme,
    String seloConformidade,
    int totalErrosNormativos,
    int totalAlertas,
    int totalInformacoes,
    LocalDateTime avaliadoEm,
    List<NormativeValidationMessage> mensagens,
    NormativeTechnicalDetails detalhesTecnicos
) {}
