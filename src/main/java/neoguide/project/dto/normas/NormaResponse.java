package neoguide.project.dto.normas;

import java.time.LocalDate;

/** Norma usada no cálculo. {@code dataAprovacao} vem do cabeçalho do PDF; nulo se não constar. */
public record NormaResponse(
        String codigo,
        String titulo,
        String revisao,
        LocalDate dataAprovacao,
        String papel,
        String urlDocumento,
        String resumo
) {
}
