package neoguide.project.dto;

import neoguide.project.model.Project;

import java.time.LocalDateTime;

public class CreateProjectResponse {

    private Long id;
    private String protocolo;
    private String titulo;
    private String status;
    private Integer etapaAtual;
    private LocalDateTime createdAt;

    public CreateProjectResponse() {
    }

    public CreateProjectResponse(Long id, String protocolo, String titulo, String status,
                                 Integer etapaAtual, LocalDateTime createdAt) {
        this.id = id;
        this.protocolo = protocolo;
        this.titulo = titulo;
        this.status = status;
        this.etapaAtual = etapaAtual;
        this.createdAt = createdAt;
    }

    public static CreateProjectResponse from(Project project) {
        return new CreateProjectResponse(
                project.getId(),
                project.getProtocolo(),
                project.getTitulo(),
                project.getStatus() != null ? project.getStatus().name() : null,
                project.getEtapaAtual(),
                project.getCreatedAt()
        );
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

    public String getNome() {
        return titulo;
    }

    public void setNome(String nome) {
        if (this.titulo == null || this.titulo.isBlank()) {
            this.titulo = nome;
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getEtapaAtual() {
        return etapaAtual;
    }

    public void setEtapaAtual(Integer etapaAtual) {
        this.etapaAtual = etapaAtual;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
