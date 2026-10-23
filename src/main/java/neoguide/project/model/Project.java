package neoguide.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Número de protocolo gerado no padrão PRJ-AAAA-XXXX. */
    @Column(nullable = false, unique = true, length = 30)
    private String protocolo;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProjectType tipo = ProjectType.LIGACAO_NOVA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProjectStatus status = ProjectStatus.RASCUNHO;

    /** Norma técnica que rege o projeto (ex.: NBR 5410). */
    @Column(name = "norma_tecnica", length = 100)
    private String normaTecnica;

    /** Engenheiro ou projetista responsável pela submissão. */
    @Column(length = 100)
    private String responsavel;

    @Column(nullable = false, length = 100)
    private String municipio;

    @Column(name = "tensao_fornecimento", nullable = false, length = 20)
    private String tensaoFornecimento;

    @Column(name = "possui_comercial", nullable = false)
    private boolean possuiComercial = false;

    @Column(name = "possui_ve", nullable = false)
    private boolean possuiVe = false;

    @Column(name = "possui_incendio", nullable = false)
    private boolean possuiIncendio = false;

    @Column(name = "etapa_atual", nullable = false)
    private Integer etapaAtual = 1;

    @Column(length = 20)
    private String cep;

    @Column(length = 255)
    private String endereco;

    @Column(length = 100)
    private String bairro;

    @Column(length = 50)
    private String crea;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "data_submissao")
    private LocalDateTime dataSubmissao;

    public Project() {
        this.createdAt = LocalDateTime.now();
        this.etapaAtual = 1;
        this.status = ProjectStatus.RASCUNHO;
        this.tipo = ProjectType.LIGACAO_NOVA;
        this.ativo = true;
    }

    public Project(String protocolo, String titulo, String descricao, ProjectType tipo, ProjectStatus status,
                   String normaTecnica, String responsavel, String municipio, LocalDateTime dataSubmissao) {
        this();
        this.protocolo = protocolo;
        this.titulo = titulo;
        this.descricao = descricao;
        this.tipo = tipo != null ? tipo : ProjectType.LIGACAO_NOVA;
        this.status = status != null ? status : ProjectStatus.RASCUNHO;
        this.normaTecnica = normaTecnica;
        this.responsavel = responsavel;
        this.municipio = municipio;
        this.dataSubmissao = dataSubmissao;
        this.ativo = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public ProjectType getTipo() {
        return tipo;
    }

    public void setTipo(ProjectType tipo) {
        this.tipo = tipo;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
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

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
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

    public LocalDateTime getDataSubmissao() {
        return dataSubmissao;
    }

    public void setDataSubmissao(LocalDateTime dataSubmissao) {
        this.dataSubmissao = dataSubmissao;
    }
}
