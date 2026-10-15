package neoguide.project.dto;

public class LoginResponse {

    private String token;
    private String tipo;
    private UserResponse usuario;

    public LoginResponse() {
        this.tipo = "Bearer";
    }

    public LoginResponse(String token, UserResponse usuario) {
        this.token = token;
        this.tipo = "Bearer";
        this.usuario = usuario;
    }

    public LoginResponse(String token, String tipo, UserResponse usuario) {
        this.token = token;
        this.tipo = tipo != null ? tipo : "Bearer";
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public UserResponse getUsuario() {
        return usuario;
    }

    public void setUsuario(UserResponse usuario) {
        this.usuario = usuario;
    }
}
