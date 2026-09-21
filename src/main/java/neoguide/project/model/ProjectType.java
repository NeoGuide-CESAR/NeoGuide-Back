package neoguide.project.model;

/**
 * Tipo do projeto técnico submetido para aprovação da distribuidora.
 */
public enum ProjectType {

    LIGACAO_NOVA("Ligação nova"),
    AUMENTO_CARGA("Aumento de carga"),
    INSTALACAO_ELETRICA("Instalação elétrica"),
    GERACAO_DISTRIBUIDA("Geração distribuída"),
    EXTENSAO_REDE("Extensão de rede de distribuição");

    private final String descricao;

    ProjectType(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}