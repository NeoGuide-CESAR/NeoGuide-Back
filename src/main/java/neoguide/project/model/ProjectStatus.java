package neoguide.project.model;

/**
 * Situação do projeto dentro do fluxo de análise da distribuidora.
 */
public enum ProjectStatus {

    EM_ANALISE("Em análise"),
    AGUARDANDO_CORRECAO("Aguardando correção"),
    APROVADO("Aprovado"),
    REPROVADO("Reprovado");

    private final String descricao;

    ProjectStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}