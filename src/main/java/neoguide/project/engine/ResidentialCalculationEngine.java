package neoguide.project.engine;

import neoguide.project.engine.model.ResidentialCalculationInput;
import neoguide.project.engine.model.ResidentialCalculationResult;
import neoguide.project.engine.model.ResidentialTypologyItem;
import neoguide.project.engine.model.ResidentialTypologyOutput;
import neoguide.project.engine.normativo.TabelasNormativas;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Motor puro e determinístico de cálculo da Demanda Residencial Final (Drf)
 * segundo as diretrizes normativas da DIS-NOR-053 (Rev. 06) da Neoenergia Pernambuco.
 */
@Component
public class ResidentialCalculationEngine {

    private static final BigDecimal SMART_STUDIO_COINCIDENCE_FACTOR = new BigDecimal("0.9000");
    private static final int SMART_STUDIO_THRESHOLD = 15;

    /**
     * Ponto de entrada operacional recebendo o payload unificado.
     */
    public ResidentialCalculationResult calculate(ResidentialCalculationInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Parâmetros de entrada para o cálculo residencial não podem ser nulos.");
        }
        return calculate(input.tipologias(), input.isSmartStudio());
    }

    /**
     * Sobrecarga de conveniência que infere se é Smart Studio caso algum item esteja marcado como tal.
     */
    public ResidentialCalculationResult calculate(List<ResidentialTypologyItem> tipologias) {
        boolean inferredSmartStudio = false;
        if (tipologias != null) {
            inferredSmartStudio = tipologias.stream()
                    .anyMatch(t -> Boolean.TRUE.equals(t.isSmartStudio()));
        }
        return calculate(tipologias, inferredSmartStudio);
    }

    /**
     * Execução principal do fluxo de cálculo residencial regulatório.
     */
    public ResidentialCalculationResult calculate(List<ResidentialTypologyItem> tipologias, boolean isSmartStudio) {
        if (tipologias == null || tipologias.isEmpty()) {
            throw new IllegalArgumentException("A lista de tipologias residenciais não pode ser nula ou vazia.");
        }

        List<ResidentialTypologyOutput> outputs = new ArrayList<>(tipologias.size());
        int totalApartamentos = 0;
        BigDecimal demandaBrutaDr = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (ResidentialTypologyItem item : tipologias) {
            if (item == null) {
                throw new IllegalArgumentException("Item de tipologia residencial não pode ser nulo.");
            }
            if (item.quantidade() <= 0) {
                throw new IllegalArgumentException("A quantidade de apartamentos deve ser maior que zero.");
            }
            if (item.areaM2() == null || item.areaM2().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("A área útil privativa deve ser maior que zero.");
            }
            if (item.demandaUnitariaDi() != null && item.demandaUnitariaDi().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("A demanda unitária Di informada deve ser maior que zero.");
            }

            BigDecimal diAplicado = item.demandaUnitariaDi() != null
                    ? item.demandaUnitariaDi().setScale(2, RoundingMode.HALF_UP)
                    : resolveDemandaUnitaria(item.areaM2());

            BigDecimal subtotal = diAplicado.multiply(BigDecimal.valueOf(item.quantidade()))
                    .setScale(2, RoundingMode.HALF_UP);

            outputs.add(new ResidentialTypologyOutput(
                    item.nome() != null ? item.nome() : "Tipologia",
                    item.areaM2().setScale(2, RoundingMode.HALF_UP),
                    item.quantidade(),
                    diAplicado,
                    subtotal
            ));

            totalApartamentos += item.quantidade();
            demandaBrutaDr = demandaBrutaDr.add(subtotal);
        }

        // Fator de Coincidência (Quadro 36)
        BigDecimal fatorCoincidenciaFc = resolveFatorCoincidencia(totalApartamentos, isSmartStudio);

        // Demanda Provisória Coincidente (Dtr = Dr * Fc). Sem arredondamento intermediário (B-MT-07).
        BigDecimal demandaProvisoriaDtr = demandaBrutaDr.multiply(fatorCoincidenciaFc);

        // Fator de Segurança (Quadro 37)
        BigDecimal fatorSegurancaFr = resolveFatorSeguranca(demandaBrutaDr);

        // Demanda Residencial Final (Drf = Dtr * Fr). Sem arredondamento intermediário.
        BigDecimal demandaFinalDrf = demandaProvisoriaDtr.multiply(fatorSegurancaFr);

        return new ResidentialCalculationResult(
                totalApartamentos,
                demandaBrutaDr,
                fatorCoincidenciaFc,
                demandaProvisoriaDtr,
                fatorSegurancaFr,
                demandaFinalDrf,
                isSmartStudio,
                outputs
        );
    }

    /**
     * Resolução de Demanda Unitária (Di) por área útil privativa (Quadro 35 — DIS-NOR-053).
     */
    public BigDecimal resolveDemandaUnitaria(BigDecimal areaM2) {
        // Quadro 35 (DIS-NOR-053 Rev. 06): faixa tabelada, sem interpolação.
        return TabelasNormativas.faixaArea(areaM2).demandaKva();
    }

    /**
     * Resolução do Fator de Coincidência (Fc) pelo Quadro 36 (DIS-NOR-053 Rev. 06).
     * Tabela discreta (sem interpolação). Aplica a regra de Smart Studios para N > 15 (item 6.25).
     */
    public BigDecimal resolveFatorCoincidencia(int totalApartamentos, boolean isSmartStudio) {
        if (totalApartamentos <= 0) {
            throw new IllegalArgumentException("Total de apartamentos deve ser maior que zero.");
        }

        // Regra regulatória DIS-NOR-053 Item 6.25: Smart Studios com N > 15
        if (isSmartStudio && totalApartamentos > SMART_STUDIO_THRESHOLD) {
            return SMART_STUDIO_COINCIDENCE_FACTOR;
        }

        return TabelasNormativas.fatorCoincidencia(totalApartamentos);
    }

    /**
     * Resolução do Fator de Segurança (Fr) pelo Quadro 37 (DIS-NOR-053).
     */
    public BigDecimal resolveFatorSeguranca(BigDecimal demandaBrutaDr) {
        if (demandaBrutaDr == null || demandaBrutaDr.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Demanda bruta Dr deve ser maior ou igual a zero.");
        }

        if (demandaBrutaDr.compareTo(new BigDecimal("25.00")) <= 0) {
            return new BigDecimal("1.50");
        } else if (demandaBrutaDr.compareTo(new BigDecimal("50.00")) <= 0) {
            return new BigDecimal("1.30");
        } else if (demandaBrutaDr.compareTo(new BigDecimal("100.00")) <= 0) {
            return new BigDecimal("1.20");
        } else {
            return new BigDecimal("1.10");
        }
    }
}
