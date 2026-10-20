package neoguide.project.dto;

import neoguide.project.model.UserRole;

public class RegisterRequest {

    private String name;
    private String email;
    private String cpfCnpj;
    private UserRole role = UserRole.ROLE_PROJETISTA;
    private String password;
    private String confirmPassword;

    public RegisterRequest() {
    }

    public RegisterRequest(String name, String email, String password, String confirmPassword) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.role = UserRole.ROLE_PROJETISTA;
    }

    public RegisterRequest(String name, String email, String cpfCnpj, String password, String confirmPassword) {
        this.name = name;
        this.email = email;
        this.cpfCnpj = cpfCnpj;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.role = UserRole.ROLE_PROJETISTA;
    }

    public RegisterRequest(String name, String email, String cpfCnpj, UserRole role, String password, String confirmPassword) {
        this.name = name;
        this.email = email;
        this.cpfCnpj = cpfCnpj;
        this.role = role != null ? role : UserRole.ROLE_PROJETISTA;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNome() {
        return name;
    }

    public void setNome(String nome) {
        if (this.name == null || this.name.isBlank()) {
            this.name = nome;
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public UserRole getRole() {
        return role != null ? role : UserRole.ROLE_PROJETISTA;
    }

    public void setRole(UserRole role) {
        this.role = role != null ? role : UserRole.ROLE_PROJETISTA;
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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public void setConfirmacaoSenha(String confirmacaoSenha) {
        if (this.confirmPassword == null || this.confirmPassword.isBlank()) {
            this.confirmPassword = confirmPassword;
        }
    }

    public void setConfirmacaoNovaSenha(String confirmacaoNovaSenha) {
        if (this.confirmPassword == null || this.confirmPassword.isBlank()) {
            this.confirmPassword = confirmPassword;
        }
    }
}
