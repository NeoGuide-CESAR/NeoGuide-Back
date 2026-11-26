package neoguide.project.engine;

import neoguide.project.engine.model.CommercialCalculationResult;
import neoguide.project.engine.model.CommercialUnitInput;
import neoguide.project.engine.model.CommercialUnitResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Demanda comercial (Dc) por espaço, DIS-NOR-030 Tabela 22.
 * Dc do espaço = demanda ativa ÷ FP. Para ocupações escalonadas (escritório), a demanda ativa é
 * min(P, 20) × 1,00 + max(P − 20, 0) × 0,70; nas demais, P × FD da tabela (1,00).
 * Dc total = soma dos Dc dos espaços.
 */
@Component
public class CommercialCalculationEngine {

    public static final BigDecimal LIMITE_PRIMEIRA_FAIXA_KW = new BigDecimal("20.00");
    public static final BigDecimal FD_PRIMEIRA_FAIXA = new BigDecimal("1.00");
    public static final BigDecimal FD_EXCEDENTE = new BigDecimal("0.70");
    private static final BigDecimal MIL = new BigDecimal("1000");

    public CommercialCalculationResult calcular(List<CommercialUnitInput> espacos) {
        if (espacos == null || espacos.isEmpty()) {
            return new CommercialCalculationResult(zero(), zero(), List.of(),
                    "Sem espaços comerciais informados (Dc = 0,00 kVA).");
        }

        BigDecimal potenciaTotal = BigDecimal.ZERO;
        BigDecimal dcTotal = BigDecimal.ZERO;
        List<CommercialUnitResult> detalhes = new ArrayList<>();
        StringBuilder memoria = new StringBuilder("DIS-NOR-030 Tabela 22: ");

        for (CommercialUnitInput espaco : espacos) {
            BigDecimal p = espaco.potenciaInstaladaKw();
            BigDecimal demandaAtiva = espaco.escalonado()
                    ? demandaEscalonada(p)
                    : p.multiply(espaco.fatorDemandaIlum());
            BigDecimal dcEspaco = demandaAtiva.divide(espaco.fatorPotencia(), 2, RoundingMode.HALF_UP);
            BigDecimal fdEfetivo = p.signum() == 0
                    ? BigDecimal.ZERO
                    : demandaAtiva.divide(p, 4, RoundingMode.HALF_UP);

            BigDecimal cargaMinima = espaco.areaM2() == null || espaco.cargaMinimaWm2() == null
                    ? null
                    : espaco.areaM2().multiply(espaco.cargaMinimaWm2()).divide(MIL, 2, RoundingMode.HALF_UP);
            String alerta = cargaMinima != null && p.compareTo(cargaMinima) < 0
                    ? String.format(Locale.forLanguageTag("pt-BR"),
                    "Carga abaixo do mínimo da ocupação (%s kW para %s m²).",
                    cargaMinima.toPlainString().replace('.', ','), espaco.areaM2().stripTrailingZeros().toPlainString().replace('.', ','))
                    : null;

            potenciaTotal = potenciaTotal.add(p);
            dcTotal = dcTotal.add(dcEspaco);
            detalhes.add(new CommercialUnitResult(espaco.ocupacaoCodigo(), espaco.rotulo(), espaco.areaM2(), p,
                    fdEfetivo, dcEspaco, cargaMinima, alerta));
            memoria.append(String.format(Locale.US, "[%s: P = %.2f kW, FP = %s -> %.2f kVA] ",
                    espaco.ocupacaoCodigo(), p.doubleValue(), espaco.fatorPotencia().toPlainString(), dcEspaco.doubleValue()));
        }

        potenciaTotal = potenciaTotal.setScale(2, RoundingMode.HALF_UP);
        dcTotal = dcTotal.setScale(2, RoundingMode.HALF_UP);
        memoria.append(String.format(Locale.US, "-> Dc total = %.2f kVA", dcTotal.doubleValue()));

        return new CommercialCalculationResult(potenciaTotal, dcTotal, List.copyOf(detalhes), memoria.toString());
    }

    private static BigDecimal demandaEscalonada(BigDecimal p) {
        BigDecimal primeira = p.min(LIMITE_PRIMEIRA_FAIXA_KW).multiply(FD_PRIMEIRA_FAIXA);
        BigDecimal excedente = p.subtract(LIMITE_PRIMEIRA_FAIXA_KW).max(BigDecimal.ZERO).multiply(FD_EXCEDENTE);
        return primeira.add(excedente);
    }

    private static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}
