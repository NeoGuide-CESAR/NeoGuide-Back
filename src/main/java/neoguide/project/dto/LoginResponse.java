package neoguide.project.dto;

import java.util.List;

public class LoginResponse {

    private String token;
    private String tipo;
    private String role;
    private UserResponse usuario;

    public LoginResponse() {
        this.tipo = "Bearer";
    }

    public LoginResponse(String token, UserResponse usuario) {
        this(token, "Bearer", usuario);
    }

    public LoginResponse(String token, String tipo, UserResponse usuario) {
        this.token = token;
        this.tipo = tipo != null ? tipo : "Bearer";
        this.usuario = usuario;
        this.role = (usuario != null && usuario.getRole() != null) ? usuario.getRole().name() : null;
    }

    public LoginResponse(String token, String tipo, String role, UserResponse usuario) {
        this.token = token;
        this.tipo = tipo != null ? tipo : "Bearer";
        this.role = role;
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

    public String getRole() {
        if (role != null) {
            return role;
        }
        return (usuario != null && usuario.getRole() != null) ? usuario.getRole().name() : null;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getRoles() {
        String currentRole = getRole();
        return currentRole != null ? List.of(currentRole) : List.of();
    }

    public UserResponse getUsuario() {
        return usuario;
    }

    public void setUsuario(UserResponse usuario) {
        this.usuario = usuario;
        if (this.role == null && usuario != null && usuario.getRole() != null) {
            this.role = usuario.getRole().name();
        }
    }
}
