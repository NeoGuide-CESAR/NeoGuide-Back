package neoguide.project.exception;

/** Alteração de perfil ou de senha recusada. Vira RFC 7807 no controller, com a regra e o campo do formulário. */
public class PerfilRecusadoException extends RuntimeException {

    private final int status;
    private final String regra;
    private final String campo;

    public PerfilRecusadoException(int status, String regra, String campo, String mensagem) {
        super(mensagem);
        this.status = status;
        this.regra = regra;
        this.campo = campo;
    }

    public int getStatus() { return status; }
    public String getRegra() { return regra; }
    public String getCampo() { return campo; }
}
