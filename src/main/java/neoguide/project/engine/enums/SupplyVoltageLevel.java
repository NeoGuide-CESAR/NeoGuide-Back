package neoguide.project.engine.enums;

public enum SupplyVoltageLevel {
    BAIXA_TENSAO_SECUNDARIA("Baixa Tensão Secundária (Rede Aérea/Subterrânea da Concessionária)"),
    MEDIA_TENSAO_PRIMARIA("Média Tensão Primária (13,8 kV — Subestação Abrigada Particular)");

    private final String descricao;

    SupplyVoltageLevel(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
