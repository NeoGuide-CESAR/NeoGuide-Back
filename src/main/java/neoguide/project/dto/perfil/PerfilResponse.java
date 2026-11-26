package neoguide.project.dto.perfil;

/**
 * Dados do usuário logado. {@code cpfCnpj} sai formatado e é somente leitura. {@code token} só vem quando o e-mail
 * foi alterado (o e-mail faz parte do token); nunca há hash de senha aqui.
 */
public record PerfilResponse(
        Long id,
        String nome,
        String email,
        String cpfCnpj,
        String telefone,
        String registroProfissional,
        String perfil,
        String token
) {
}
