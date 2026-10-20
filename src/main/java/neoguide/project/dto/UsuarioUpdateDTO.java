package neoguide.project.dto;

public class UsuarioUpdateDTO {

    private String name;
    private String telefone;
    private String empresa;
    private String crea;

    public UsuarioUpdateDTO() {
    }

    public UsuarioUpdateDTO(String name, String telefone, String empresa, String crea) {
        this.name = name;
        this.telefone = telefone;
        this.empresa = empresa;
        this.crea = crea;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getCrea() {
        return crea;
    }

    public void setCrea(String crea) {
        this.crea = crea;
    }
}
