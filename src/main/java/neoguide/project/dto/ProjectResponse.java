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

    // Campos do DNA
    private String tensaoFornecimento;
    private boolean possuiComercial;
    private boolean possuiVe;
    private boolean possuiIncendio;
    private Integer etapaAtual;
    private String cep;
    private String endereco;
    private String bairro;
    private String crea;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProjectResponse() {
    }

    public static ProjectResponse from(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setProtocolo(project.getProtocolo());
        response.setTitulo(project.getTitulo());
        response.setDescricao(project.getDescricao());
        if (project.getTipo() != null) {
            response.setTipo(project.getTipo().name());
            response.setTipoDescricao(project.getTipo().getDescricao());
        }
        if (project.getStatus() != null) {
            response.setStatus(project.getStatus().name());
            response.setStatusDescricao(project.getStatus().getDescricao());
        }
        response.setNormaTecnica(project.getNormaTecnica());
        response.setResponsavel(project.getResponsavel());
        response.setMunicipio(project.getMunicipio());
        response.setDataSubmissao(project.getDataSubmissao());

        response.setTensaoFornecimento(project.getTensaoFornecimento());
        response.setPossuiComercial(project.isPossuiComercial());
        response.setPossuiVe(project.isPossuiVe());
        response.setPossuiIncendio(project.isPossuiIncendio());
        response.setEtapaAtual(project.getEtapaAtual());
        response.setCep(project.getCep());
        response.setEndereco(project.getEndereco());
        response.setBairro(project.getBairro());
        response.setCrea(project.getCrea());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());

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

    public String getTensaoFornecimento() {
        return tensaoFornecimento;
    }

    public void setTensaoFornecimento(String tensaoFornecimento) {
        this.tensaoFornecimento = tensaoFornecimento;
    }

    public boolean isPossuiComercial() {
        return possuiComercial;
    }

    public void setPossuiComercial(boolean possuiComercial) {
        this.possuiComercial = possuiComercial;
    }

    public boolean isPossuiVe() {
        return possuiVe;
    }

    public void setPossuiVe(boolean possuiVe) {
        this.possuiVe = possuiVe;
    }

    public boolean isPossuiIncendio() {
        return possuiIncendio;
    }

    public void setPossuiIncendio(boolean possuiIncendio) {
        this.possuiIncendio = possuiIncendio;
    }

    public Integer getEtapaAtual() {
        return etapaAtual;
    }

    public void setEtapaAtual(Integer etapaAtual) {
        this.etapaAtual = etapaAtual;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCrea() {
        return crea;
    }

    public void setCrea(String crea) {
        this.crea = crea;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}