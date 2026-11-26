package neoguide.project.exception;

import neoguide.project.dto.calculo.ItemValidacaoResponse;

import java.util.List;

/**
 * Cálculo ou memorial não permitido agora. Vira RFC 7807 (ProblemDetail) no controller de cálculo, com a regra violada.
 */
public class CalculoRecusadoException extends RuntimeException {

    private final int status;
    private final String regra;
    private final List<ItemValidacaoResponse> erros;

    public CalculoRecusadoException(int status, String regra, String detalhe, List<ItemValidacaoResponse> erros) {
        super(detalhe);
        this.status = status;
        this.regra = regra;
        this.erros = erros != null ? List.copyOf(erros) : List.of();
    }

    public int getStatus() { return status; }
    public String getRegra() { return regra; }
    public List<ItemValidacaoResponse> getErros() { return erros; }
}
