package neoguide.project.dto;

public class CreateProjectRequest {

    private String titulo;
    private String descricao;
    private String tipo;
    private String tipoEdificacao;
    private String tensaoFornecimento;
    private boolean possuiComercial;
    private boolean possuiVe;
    private boolean possuiIncendio;
    private String responsavel;
    private String crea;
    private String municipio;
    private String cep;
    private String endereco;
    private String bairro;

    public CreateProjectRequest() {
    }

    public CreateProjectRequest(String titulo, String descricao, String tipo, String tensaoFornecimento,
                                boolean possuiComercial, boolean possuiVe, boolean possuiIncendio,
                                String responsavel, String crea, String municipio,
                                String cep, String endereco, String bairro) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.tipo = tipo;
        this.tensaoFornecimento = tensaoFornecimento;
        this.possuiComercial = possuiComercial;
        this.possuiVe = possuiVe;
        this.possuiIncendio = possuiIncendio;
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
