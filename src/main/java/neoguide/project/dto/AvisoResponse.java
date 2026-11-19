package neoguide.project.dto;

import java.time.LocalDate;

/** Aviso exibido na Home (bloco "Novidades"). Contrato B-EP-23. */
public record AvisoResponse(Long id, String titulo, String texto, LocalDate publicadoEm) {
}
