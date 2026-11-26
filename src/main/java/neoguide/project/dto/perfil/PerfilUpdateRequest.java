package neoguide.project.dto.perfil;

/**
 * Alteração do próprio cadastro. CPF/CNPJ e perfil não são aceitos (campos desconhecidos são ignorados).
 * {@code senhaAtual} só é exigida quando o e-mail muda.
 */
public record PerfilUpdateRequest(
        String nome,
        String telefone,
        String email,
        String registroProfissional,
        String senhaAtual
) {
}
