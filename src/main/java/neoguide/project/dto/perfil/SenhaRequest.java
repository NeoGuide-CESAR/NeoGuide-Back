package neoguide.project.dto.perfil;

/** Troca de senha: a atual (confere com o hash) e a nova. */
public record SenhaRequest(String senhaAtual, String novaSenha) {
}
