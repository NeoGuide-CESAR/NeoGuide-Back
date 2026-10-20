package neoguide.project.engine;

import neoguide.project.engine.model.ResidentialCalculationInput;
import neoguide.project.engine.model.ResidentialCalculationResult;
import neoguide.project.engine.model.ResidentialTypologyItem;
import neoguide.project.engine.model.ResidentialTypologyOutput;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Motor puro e determinístico de cálculo da Demanda Residencial Final (Drf)
 * segundo as diretrizes normativas da DIS-NOR-053 (Rev. 06) da Neoenergia Pernambuco.
 */
@Component
public class ResidentialCalculationEngine {

    private static final BigDecimal SMART_STUDIO_COINCIDENCE_FACTOR = new BigDecimal("0.9000");
    private static final int SMART_STUDIO_THRESHOLD = 15;

    // Tabela discreta do Quadro 36 para interpolação
    private static final NavigableMap<Integer, BigDecimal> QUADRO_36_PONTOS = new TreeMap<>();

    static {
        QUADRO_36_PONTOS.put(1, new BigDecimal("1.0000"));
        QUADRO_36_PONTOS.put(10, new BigDecimal("0.9640"));
        QUADRO_36_PONTOS.put(15, new BigDecimal("0.9027"));
        QUADRO_36_PONTOS.put(20, new BigDecimal("0.8560"));
        QUADRO_36_PONTOS.put(25, new BigDecimal("0.8184"));
        QUADRO_36_PONTOS.put(30, new BigDecimal("0.7827"));
        QUADRO_36_PONTOS.put(35, new BigDecimal("0.7580"));
        QUADRO_36_PONTOS.put(40, new BigDecimal("0.7380"));
        QUADRO_36_PONTOS.put(45, new BigDecimal("0.7231"));
        QUADRO_36_PONTOS.put(50, new BigDecimal("0.7080"));
        QUADRO_36_PONTOS.put(55, new BigDecimal("0.6935"));
        QUADRO_36_PONTOS.put(60, new BigDecimal("0.6823"));
    }

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

        // Demanda Provisória Coincidente (Dtr = Dr * Fc)
        BigDecimal demandaProvisoriaDtr = demandaBrutaDr.multiply(fatorCoincidenciaFc)
                .setScale(2, RoundingMode.HALF_UP);

        // Fator de Segurança (Quadro 37)
        BigDecimal fatorSegurancaFr = resolveFatorSeguranca(demandaBrutaDr);

        // Demanda Residencial Final (Drf = Dtr * Fr)
        BigDecimal demandaFinalDrf = demandaProvisoriaDtr.multiply(fatorSegurancaFr)
                .setScale(2, RoundingMode.HALF_UP);

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
        if (areaM2 == null || areaM2.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A área útil privativa deve ser maior que zero.");
        }

        if (areaM2.compareTo(new BigDecimal("40.00")) <= 0) {
            return new BigDecimal("1.20");
        } else if (areaM2.compareTo(new BigDecimal("45.00")) <= 0) {
            return new BigDecimal("1.28");
        } else if (areaM2.compareTo(new BigDecimal("50.00")) <= 0) {
            return new BigDecimal("1.35");
        } else if (areaM2.compareTo(new BigDecimal("55.00")) <= 0) {
            return new BigDecimal("1.40");
        } else if (areaM2.compareTo(new BigDecimal("60.00")) <= 0) {
            return new BigDecimal("1.43");
        } else if (areaM2.compareTo(new BigDecimal("65.00")) <= 0) {
            return new BigDecimal("1.47");
        } else if (areaM2.compareTo(new BigDecimal("70.00")) <= 0) {
            return new BigDecimal("1.57");
        } else if (areaM2.compareTo(new BigDecimal("80.00")) <= 0) {
            return new BigDecimal("1.80");
        } else if (areaM2.compareTo(new BigDecimal("90.00")) <= 0) {
            return new BigDecimal("1.95");
        } else if (areaM2.compareTo(new BigDecimal("100.00")) <= 0) {
            return new BigDecimal("2.15");
        } else if (areaM2.compareTo(new BigDecimal("120.00")) <= 0) {
            return new BigDecimal("2.45");
        } else if (areaM2.compareTo(new BigDecimal("140.00")) <= 0) {
            return new BigDecimal("2.80");
        } else if (areaM2.compareTo(new BigDecimal("170.00")) <= 0) {
            return new BigDecimal("3.30");
        } else if (areaM2.compareTo(new BigDecimal("200.00")) <= 0) {
            return new BigDecimal("3.90");
        } else if (areaM2.compareTo(new BigDecimal("300.00")) <= 0) {
            return new BigDecimal("5.40");
        } else {
            return new BigDecimal("7.20");
        }
    }

    /**
     * Resolução do Fator de Coincidência (Fc) pelo Quadro 36 (DIS-NOR-053).
     * Aplica a regra de Smart Studios para N > 15 e interpolação linear nos nós intermediários.
     */
    public BigDecimal resolveFatorCoincidencia(int totalApartamentos, boolean isSmartStudio) {
        if (totalApartamentos <= 0) {
            throw new IllegalArgumentException("Total de apartamentos deve ser maior que zero.");
        }

        // Regra regulatória DIS-NOR-053 Item 6.25: Smart Studios com N > 15
        if (isSmartStudio && totalApartamentos > SMART_STUDIO_THRESHOLD) {
            return SMART_STUDIO_COINCIDENCE_FACTOR;
        }

        // Limite superior tabelado da norma (N >= 60)
        if (totalApartamentos >= 60) {
            return new BigDecimal("0.6823");
        }

        // Ponto exato tabelado
        if (QUADRO_36_PONTOS.containsKey(totalApartamentos)) {
            return QUADRO_36_PONTOS.get(totalApartamentos);
        }

        // Interpolação linear entre nós adjacentes
        Map.Entry<Integer, BigDecimal> lower = QUADRO_36_PONTOS.floorEntry(totalApartamentos);
        Map.Entry<Integer, BigDecimal> higher = QUADRO_36_PONTOS.ceilingEntry(totalApartamentos);

        if (lower == null) {
            return QUADRO_36_PONTOS.firstEntry().getValue();
        }
        if (higher == null) {
            return QUADRO_36_PONTOS.lastEntry().getValue();
        }

        int n1 = lower.getKey();
        BigDecimal fc1 = lower.getValue();
        int n2 = higher.getKey();
        BigDecimal fc2 = higher.getValue();

        BigDecimal deltaN = BigDecimal.valueOf(totalApartamentos - n1);
        BigDecimal stepN = BigDecimal.valueOf(n2 - n1);
        BigDecimal deltaFc = fc2.subtract(fc1);

        BigDecimal interpolated = fc1.add(deltaN.multiply(deltaFc).divide(stepN, 8, RoundingMode.HALF_UP));
        return interpolated.setScale(4, RoundingMode.HALF_UP);
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
