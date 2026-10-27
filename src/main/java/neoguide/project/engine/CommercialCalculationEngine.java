package neoguide.project.engine;

import neoguide.project.engine.model.CommercialCalculationInput;
import neoguide.project.engine.model.CommercialCalculationResult;
import neoguide.project.engine.model.CommercialUnitInput;
import neoguide.project.engine.model.CommercialUnitResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.List;

@Component
public class CommercialCalculationEngine {

    public static final BigDecimal LIMITE_PRIMEIRA_FAIXA_KW = new BigDecimal("20.00");
    public static final BigDecimal FD_PRIMEIRA_FAIXA = new BigDecimal("1.00");
    public static final BigDecimal FD_EXCEDENTE = new BigDecimal("0.70");
    public static final BigDecimal FATOR_POTENCIA_COMERCIAL = new BigDecimal("0.92");

    public CommercialCalculationResult calcularDemandaComercial(CommercialCalculationInput input) {
        if (input == null || !input.possuiComercial() || input.unidades() == null || input.unidades().isEmpty()) {
            return new CommercialCalculationResult(
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    FATOR_POTENCIA_COMERCIAL,
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    Collections.emptyList(),
                    "Empreendimento sem unidades comerciais cadastradas (Dc = 0.00 kVA)."
            );
        }

        BigDecimal potenciaTotal = BigDecimal.ZERO;
        for (CommercialUnitInput unit : input.unidades()) {
            if (unit != null && unit.potenciaInstaladaKw() != null) {
                if (unit.potenciaInstaladaKw().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("A potência instalada da unidade '" + unit.identificacao() + "' não pode ser negativa.");
                }
                potenciaTotal = potenciaTotal.add(unit.potenciaInstaladaKw());
            }
        }
        potenciaTotal = potenciaTotal.setScale(2, RoundingMode.HALF_UP);

        if (potenciaTotal.compareTo(BigDecimal.ZERO) == 0) {
            return new CommercialCalculationResult(
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    FATOR_POTENCIA_COMERCIAL,
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    Collections.emptyList(),
                    "Potência total instalada nula (Dc = 0.00 kVA)."
            );
        }

        BigDecimal pPrimeiraFaixa = potenciaTotal.min(LIMITE_PRIMEIRA_FAIXA_KW);
        BigDecimal pExcedente = potenciaTotal.subtract(pPrimeiraFaixa).max(BigDecimal.ZERO);

        BigDecimal demPrimeiraFaixa = pPrimeiraFaixa.multiply(FD_PRIMEIRA_FAIXA);
        BigDecimal demExcedente = pExcedente.multiply(FD_EXCEDENTE);
        BigDecimal demandaAtivaTotalKw = demPrimeiraFaixa.add(demExcedente).setScale(2, RoundingMode.HALF_UP);

        BigDecimal demandaKva = demandaAtivaTotalKw.divide(FATOR_POTENCIA_COMERCIAL, 2, RoundingMode.HALF_UP);

        BigDecimal fdGlobal = demandaAtivaTotalKw.divide(potenciaTotal, 4, RoundingMode.HALF_UP);

        List<CommercialUnitResult> unidadesDetalhadas = new ArrayList<>();
        for (CommercialUnitInput unit : input.unidades()) {
            BigDecimal pUnit = (unit != null && unit.potenciaInstaladaKw() != null)
                    ? unit.potenciaInstaladaKw()
                    : BigDecimal.ZERO;
            BigDecimal demAtivaUnit = pUnit.multiply(fdGlobal).setScale(2, RoundingMode.HALF_UP);
            BigDecimal demKvaUnit = demAtivaUnit.divide(FATOR_POTENCIA_COMERCIAL, 2, RoundingMode.HALF_UP);

            unidadesDetalhadas.add(new CommercialUnitResult(
                    unit != null ? unit.identificacao() : null,
                    unit != null ? unit.atividadeComercial() : null,
                    unit != null ? unit.areaM2() : null,
                    pUnit.setScale(2, RoundingMode.HALF_UP),
                    fdGlobal,
                    demKvaUnit
            ));
        }

        String memoria = String.format(Locale.US, 
                "DIS-NOR-030 Tabela 22: Ptotal = %.2f kW [P1 (<= 20kW) = %.2f kW x 1.00; P2 (> 20kW) = %.2f kW x 0.70] -> Pdem = %.2f kW; FP = 0.92 -> Dc = %.2f kVA",
                potenciaTotal.doubleValue(),
                pPrimeiraFaixa.doubleValue(),
                pExcedente.doubleValue(),
                demandaAtivaTotalKw.doubleValue(),
                demandaKva.doubleValue()
        );

        return new CommercialCalculationResult(
                potenciaTotal,
                pPrimeiraFaixa.setScale(2, RoundingMode.HALF_UP),
                pExcedente.setScale(2, RoundingMode.HALF_UP),
                demandaAtivaTotalKw,
                FATOR_POTENCIA_COMERCIAL,
                demandaKva,
                Collections.unmodifiableList(unidadesDetalhadas),
                memoria
        );
    }
}
