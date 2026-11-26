package neoguide.project.dto;

/**
 * Estado de uma etapa do wizard, calculado no servidor.
 * CONCLUIDA, ATUAL, DISPONIVEL, BLOQUEADA ou NAO_SE_APLICA.
 */
public record EtapaEstadoResponse(int numero, String nome, String estado) {
}
