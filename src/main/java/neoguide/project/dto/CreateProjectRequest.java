package neoguide.project.dto;

public class CreateProjectRequest {

    private String titulo;
    private String descricao;
    private String tipo;
    private String tipoEdificacao;
    private String tensaoFornecimento;
    private Boolean possuiComercial = false;
    private Boolean possuiVe = false;
    private Boolean possuiIncendio = false;
    private String responsavel;
    private String crea;
    private String municipio;
    private String cep;
    private String endereco;
    private String bairro;

    public CreateProjectRequest() {
    }

    public CreateProjectRequest(String titulo, String descricao, String tipo, String tensaoFornecimento,
                                Boolean possuiComercial, Boolean possuiVe, Boolean possuiIncendio,
                                String responsavel, String crea, String municipio,
                                String cep, String endereco, String bairro) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.tipo = tipo;
        this.tensaoFornecimento = tensaoFornecimento;
        this.possuiComercial = possuiComercial != null ? possuiComercial : false;
        this.possuiVe = possuiVe != null ? possuiVe : false;
        this.possuiIncendio = possuiIncendio != null ? possuiIncendio : false;
        this.responsavel = responsavel;
        this.crea = crea;
        this.municipio = municipio;
        this.cep = cep;
        this.endereco = endereco;
        this.bairro = bairro;
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
        return tipo != null ? tipo : tipoEdificacao;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTipoEdificacao() {
        return tipoEdificacao != null ? tipoEdificacao : tipo;
    }

    public void setTipoEdificacao(String tipoEdificacao) {
        this.tipoEdificacao = tipoEdificacao;
    }

    public String getTensaoFornecimento() {
        return tensaoFornecimento;
    }

    public void setTensaoFornecimento(String tensaoFornecimento) {
        this.tensaoFornecimento = tensaoFornecimento;
    }

    public boolean isPossuiComercial() {
        return Boolean.TRUE.equals(possuiComercial);
    }

    public Boolean getPossuiComercial() {
        return possuiComercial;
    }

    public void setPossuiComercial(Boolean possuiComercial) {
        this.possuiComercial = possuiComercial != null ? possuiComercial : false;
    }

    public boolean isPossuiVe() {
        return Boolean.TRUE.equals(possuiVe);
    }

    public Boolean getPossuiVe() {
        return possuiVe;
    }

    public void setPossuiVe(Boolean possuiVe) {
        this.possuiVe = possuiVe != null ? possuiVe : false;
    }

    public boolean isPossuiIncendio() {
        return Boolean.TRUE.equals(possuiIncendio);
    }

    public Boolean getPossuiIncendio() {
        return possuiIncendio;
    }

    public void setPossuiIncendio(Boolean possuiIncendio) {
        this.possuiIncendio = possuiIncendio != null ? possuiIncendio : false;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public String getCrea() {
        return crea;
    }

    public void setCrea(String crea) {
        this.crea = crea;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
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
}
