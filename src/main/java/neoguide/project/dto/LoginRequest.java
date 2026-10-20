package neoguide.project.dto;

public class LoginRequest {

    private String login;
    private String email;
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String login, String password) {
        this.login = login;
        this.email = login;
        this.password = password;
    }

    public String getLogin() {
        if (login != null && !login.isBlank()) {
            return login;
        }
        return email;
    }

    public void setLogin(String login) {
        this.login = login;
        if (this.email == null) {
            this.email = login;
        }
    }

    public void setIdentificador(String identificador) {
        this.login = identificador;
        if (this.email == null) {
            this.email = identificador;
        }
    }

    public String getEmail() {
        if (email != null && !email.isBlank()) {
            return email;
        }
        return login;
    }

    public void setEmail(String email) {
        this.email = email;
        if (this.login == null) {
            this.login = email;
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSenha(String senha) {
        if (this.password == null || this.password.isBlank()) {
            this.password = senha;
        }
    }
}
