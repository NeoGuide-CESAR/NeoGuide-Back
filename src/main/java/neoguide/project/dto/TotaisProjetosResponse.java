package neoguide.project.dto;

import neoguide.project.model.ProjectStatus;

import java.util.Map;

/**
 * Contagens de projetos por situação (indicadores da Home e chips de Meus Projetos).
 * {@code reprovado} não inclui {@code aguardandoCorrecao}, que aparece separado.
 */
public record TotaisProjetosResponse(
        long todos,
        long rascunho,
        long emAnalise,
        long aguardandoCorrecao,
        long aprovado,
        long reprovado) {

    public static TotaisProjetosResponse de(Map<ProjectStatus, Long> porStatus) {
        long rascunho = porStatus.getOrDefault(ProjectStatus.RASCUNHO, 0L);
        long emAnalise = porStatus.getOrDefault(ProjectStatus.EM_ANALISE, 0L);
        long aguardando = porStatus.getOrDefault(ProjectStatus.AGUARDANDO_CORRECAO, 0L);
        long aprovado = porStatus.getOrDefault(ProjectStatus.APROVADO, 0L);
        long reprovado = porStatus.getOrDefault(ProjectStatus.REPROVADO, 0L);
        long todos = porStatus.values().stream().mapToLong(Long::longValue).sum();
        return new TotaisProjetosResponse(todos, rascunho, emAnalise, aguardando, aprovado, reprovado);
    }
}
