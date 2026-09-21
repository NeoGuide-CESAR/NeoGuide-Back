package neoguide.project.dto;

import neoguide.project.model.Project;

import java.time.LocalDateTime;

public class ProjectResponse {

    private Long id;
    private String protocolo;
    private String titulo;
    private String descricao;
    private String tipo;
    private String tipoDescricao;
    private String status;
    private String statusDescricao;
    private String normaTecnica;
    private String responsavel;
    private String municipio;
    private LocalDateTime dataSubmissao;

    public ProjectResponse() {
    }

    public static ProjectResponse from(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setProtocolo(project.getProtocolo());
        response.setTitulo(project.getTitulo());
        response.setDescricao(project.getDescricao());
        response.setTipo(project.getTipo().name());
        response.setTipoDescricao(project.getTipo().getDescricao());
        response.setStatus(project.getStatus().name());
        response.setStatusDescricao(project.getStatus().getDescricao());
        response.setNormaTecnica(project.getNormaTecnica());
        response.setResponsavel(project.getResponsavel());
        response.setMunicipio(project.getMunicipio());
        response.setDataSubmissao(project.getDataSubmissao());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTipoDescricao() {
        return tipoDescricao;
    }

    public void setTipoDescricao(String tipoDescricao) {
        this.tipoDescricao = tipoDescricao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDescricao() {
        return statusDescricao;
    }

    public void setStatusDescricao(String statusDescricao) {
        this.statusDescricao = statusDescricao;
    }

    public String getNormaTecnica() {
        return normaTecnica;
    }

    public void setNormaTecnica(String normaTecnica) {
        this.normaTecnica = normaTecnica;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public LocalDateTime getDataSubmissao() {
        return dataSubmissao;
    }

    public void setDataSubmissao(LocalDateTime dataSubmissao) {
        this.dataSubmissao = dataSubmissao;
    }
}