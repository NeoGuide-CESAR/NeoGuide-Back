package neoguide.project.dto.normas;

import java.util.List;

/**
 * Tabela de uma norma. {@code endpointCatalogo} é nulo (e {@code disponivel} falso) quando a tabela ainda não
 * está no catálogo. {@code usadaNoResultado} marca as tabelas de entrada e barramento, usadas depois do cálculo.
 */
public record TabelaNormaResponse(
        String ancora,
        String titulo,
        String referencia,
        Integer paginaPdf,
        String endpointCatalogo,
        boolean disponivel,
        List<Integer> etapasWizard,
        boolean usadaNoResultado,
        List<String> notasResumidas
) {
}
