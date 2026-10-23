package neoguide.project.dto;

import neoguide.project.model.User;
import neoguide.project.model.UserRole;

import java.time.LocalDateTime;

public class UsuarioResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String cpfCnpj;
    private String telefone;
    private String crea;
    private String empresa;
    private UserRole role;
    private LocalDateTime createdAt;

    public UsuarioResponseDTO() {
    }

    public UsuarioResponseDTO(Long id, String name, String email, String cpfCnpj,
                              String telefone, String crea, String empresa,
                              UserRole role, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.cpfCnpj = cpfCnpj;
        this.telefone = telefone;
        this.crea = crea;
        this.empresa = empresa;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static UsuarioResponseDTO from(User user) {
        if (user == null) {
            return null;
        }
        return new UsuarioResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCpfCnpj(),
                user.getTelefone(),
                user.getCrea(),
                user.getEmpresa(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCrea() {
        return crea;
    }

    public void setCrea(String crea) {
        this.crea = crea;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
