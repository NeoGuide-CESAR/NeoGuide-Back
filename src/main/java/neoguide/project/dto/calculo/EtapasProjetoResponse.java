package neoguide.project.dto.calculo;

import java.math.BigDecimal;
import java.util.List;

/** Dados gravados em cada etapa, para reabrir o projeto, revisar e exibir o resultado. */
public record EtapasProjetoResponse(
        Residencial residencial,
        AreaComum areaComum,
        CargasEspeciais cargasEspeciais
) {
    public record Residencial(List<Tipologia> tipologias) {
    }

    public record Tipologia(BigDecimal areaUtilM2, Integer quantidadeUnidades, boolean smartStudio) {
    }

    public record AreaComum(List<Carga> cargas) {
    }

    public record Carga(String origem, String submodulo, Long catalogoMotorId, Long catalogoClimaId,
                        Long catalogoEquipamentoId, String tipoEquipamento, String descricao, Integer quantidade,
                        BigDecimal potenciaNominalKw, BigDecimal fatorPotencia, BigDecimal fatorDemanda, boolean reserva) {
    }

    public record CargasEspeciais(Integer qtdPontosVe, BigDecimal potenciaVeKw, BigDecimal potenciaBombaIncendioCv,
                                  BigDecimal potenciaSolarInversorKw, Boolean medidorExclusivo) {
    }
}
