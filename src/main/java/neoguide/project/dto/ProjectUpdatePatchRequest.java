package neoguide.project.dto;

public class ProjectUpdatePatchRequest {

    private String titulo;
    private String nome;
    private String descricao;
    private String tipo;
    private String normaTecnica;
    private String responsavel;
    private String municipio;
    private String tensaoFornecimento;
    private Boolean possuiComercial;
    private Boolean possuiVe;
    private Boolean possuiIncendio;
    private String cep;
    private String endereco;
    private String logradouro;
    private String bairro;
    private String crea;

    public ProjectUpdatePatchRequest() {
    }

    public String getTitulo() {
        return (titulo != null && !titulo.isBlank()) ? titulo : nome;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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

    public Boolean getPossuiComercial() {
        return possuiComercial;
    }

    public void setPossuiComercial(Boolean possuiComercial) {
        this.possuiComercial = possuiComercial;
    }

    public Boolean getPossuiVe() {
        return possuiVe;
    }

    public void setPossuiVe(Boolean possuiVe) {
        this.possuiVe = possuiVe;
    }

    public Boolean getPossuiIncendio() {
        return possuiIncendio;
    }

    public void setPossuiIncendio(Boolean possuiIncendio) {
        this.possuiIncendio = possuiIncendio;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getEndereco() {
        return (endereco != null && !endereco.isBlank()) ? endereco : logradouro;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
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
}
