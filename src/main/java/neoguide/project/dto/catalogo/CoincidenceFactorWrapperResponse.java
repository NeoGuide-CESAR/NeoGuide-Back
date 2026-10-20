package neoguide.project.dto.catalogo;

import java.util.ArrayList;
import java.util.List;

public class CoincidenceFactorWrapperResponse {

    private String norma;
    private String regraEspecialStudios;
    private List<CoincidenceFactorItemResponse> tabela;

    public CoincidenceFactorWrapperResponse() {
        this.norma = "DIS-NOR-053 Quadro 36";
        this.regraEspecialStudios = "Para edifícios tipo Smart/Studio acima de 15 unidades, adota-se 90% (0,90) fixo";
        this.tabela = new ArrayList<>();
    }

    public CoincidenceFactorWrapperResponse(String norma, String regraEspecialStudios, List<CoincidenceFactorItemResponse> tabela) {
        this.norma = norma;
        this.regraEspecialStudios = regraEspecialStudios;
        this.tabela = tabela;
    }

    public String getNorma() {
        return norma;
    }

    public void setNorma(String norma) {
        this.norma = norma;
    }

    public String getRegraEspecialStudios() {
        return regraEspecialStudios;
    }

    public void setRegraEspecialStudios(String regraEspecialStudios) {
        this.regraEspecialStudios = regraEspecialStudios;
    }

    public List<CoincidenceFactorItemResponse> getTabela() {
        return tabela;
    }

    public void setTabela(List<CoincidenceFactorItemResponse> tabela) {
        this.tabela = tabela;
    }
}
