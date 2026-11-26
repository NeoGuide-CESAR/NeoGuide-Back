package neoguide.project.model;

/** Tipo de edificação do projeto (DIS-NOR-053: define o limite BT/MT). Distinto do tipo de ligação (ProjectType). */
public enum TipoEdificacao {
    RESIDENCIAL_MULTIFAMILIAR("Residencial multifamiliar"),
    COMERCIAL("Comercial"),
    MISTO("Uso misto (residencial e comercial)");

    private final String descricao;

    TipoEdificacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Aceita o nome do enum (RESIDENCIAL_MULTIFAMILIAR) ou a descrição exibida (Residencial multifamiliar). */
    public static TipoEdificacao from(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String v = valor.trim();
        for (TipoEdificacao t : values()) {
            if (t.name().equalsIgnoreCase(v) || t.descricao.equalsIgnoreCase(v)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Tipo de edificação inválido: " + valor);
    }
}
