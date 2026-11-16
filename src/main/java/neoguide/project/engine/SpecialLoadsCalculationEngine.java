package neoguide.project.engine;

import neoguide.project.engine.model.ElectricVehicleLoadInput;
import neoguide.project.engine.model.ElectricVehicleLoadOutput;
import neoguide.project.engine.model.FirePumpLoadInput;
import neoguide.project.engine.model.FirePumpLoadOutput;
import neoguide.project.engine.model.SpecialLoadsCalculationInput;
import neoguide.project.engine.model.SpecialLoadsCalculationResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.NavigableMap;
import java.util.TreeMap;

@Component
public class SpecialLoadsCalculationEngine {

    public static final String NORMA_VE = "DIS-NOR-053 Quadro 33";
    public static final String NORMA_BOMBA = "DIS-NOR-030 Tabela 19";
    public static final BigDecimal FATOR_POTENCIA_VE_PADRAO = new BigDecimal("1.00");
    public static final BigDecimal FATOR_DEMANDA_EMERGENCIA = new BigDecimal("1.00");

    private static record MotorEntry(BigDecimal kva, String chavePartida) {}

    private static final NavigableMap<BigDecimal, MotorEntry> TABELA_19_MOTORES = new TreeMap<>();

    static {
        // kVA: DIS-NOR-030 Rev. 07, Tabela 19 (coluna VA). Tipo de partida: mantido do modelo anterior (não consta na Tabela 19).
        TABELA_19_MOTORES.put(new BigDecimal("0.50"), new MotorEntry(new BigDecimal("0.87"), "DIRETA"));
        TABELA_19_MOTORES.put(new BigDecimal("0.75"), new MotorEntry(new BigDecimal("1.26"), "DIRETA"));
        TABELA_19_MOTORES.put(new BigDecimal("1.00"), new MotorEntry(new BigDecimal("1.52"), "DIRETA"));
        TABELA_19_MOTORES.put(new BigDecimal("1.50"), new MotorEntry(new BigDecimal("2.17"), "DIRETA"));
        TABELA_19_MOTORES.put(new BigDecimal("2.00"), new MotorEntry(new BigDecimal("2.70"), "DIRETA"));
        TABELA_19_MOTORES.put(new BigDecimal("3.00"), new MotorEntry(new BigDecimal("4.04"), "DIRETA"));
        TABELA_19_MOTORES.put(new BigDecimal("4.00"), new MotorEntry(new BigDecimal("5.03"), "DIRETA"));
        TABELA_19_MOTORES.put(new BigDecimal("5.00"), new MotorEntry(new BigDecimal("6.02"), "DIRETA"));
        TABELA_19_MOTORES.put(new BigDecimal("7.50"), new MotorEntry(new BigDecimal("8.65"), "DIRETA"));
        TABELA_19_MOTORES.put(new BigDecimal("10.00"), new MotorEntry(new BigDecimal("11.54"), "ESTRELA_TRIANGULO"));
        TABELA_19_MOTORES.put(new BigDecimal("12.50"), new MotorEntry(new BigDecimal("14.09"), "ESTRELA_TRIANGULO"));
        TABELA_19_MOTORES.put(new BigDecimal("15.00"), new MotorEntry(new BigDecimal("16.65"), "ESTRELA_TRIANGULO"));
        TABELA_19_MOTORES.put(new BigDecimal("20.00"), new MotorEntry(new BigDecimal("22.10"), "ESTRELA_TRIANGULO"));
        TABELA_19_MOTORES.put(new BigDecimal("25.00"), new MotorEntry(new BigDecimal("25.83"), "COMPENSADORA"));
        TABELA_19_MOTORES.put(new BigDecimal("30.00"), new MotorEntry(new BigDecimal("30.52"), "SOFT_STARTER"));
    }

    public SpecialLoadsCalculationResult calculate(SpecialLoadsCalculationInput input) {
        if (input == null) {
            input = new SpecialLoadsCalculationInput(null, null, null, null);
        }

        ElectricVehicleLoadOutput veOutput = calcularRecargaVeicular(input.recargaVeicular());
        FirePumpLoadOutput mbiOutput = calcularMotobombaIncendio(input.motobombaIncendio());

        BigDecimal dVe = veOutput.demandaDveKva();
        BigDecimal dMbi = mbiOutput.demandaDmbiKva();

        BigDecimal demandaEspecialTotal = dVe.add(dMbi).setScale(2, RoundingMode.HALF_UP);

        boolean derivadoAntesDoQdg = mbiOutput.derivadoAntesDoQdg();
        BigDecimal parcelaDedutivelQdg = derivadoAntesDoQdg ? dMbi : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal contribuicaoQdg = demandaEspecialTotal.subtract(parcelaDedutivelQdg).setScale(2, RoundingMode.HALF_UP);

        boolean alertaTrafo50 = false;
        BigDecimal percentualUsoTrafo = null;
        StringBuilder auditoria = new StringBuilder();

        auditoria.append(String.format(Locale.US,
                "Cargas Especiais: Recarga VE = %.2f kVA (Quadro 33); Motobomba Incêndio = %.2f kVA (Tabela 19). ",
                dVe.doubleValue(), dMbi.doubleValue()));

        if (derivadoAntesDoQdg && dMbi.compareTo(BigDecimal.ZERO) > 0) {
            auditoria.append(String.format(Locale.US,
                    "Isolamento QDG (NBR 5410 / DIS-NOR-053): Bomba de incêndio (%.2f kVA) deduzida do barramento do QDG (DQDG = Ded - Dmbi). ",
                    dMbi.doubleValue()));
        }

        BigDecimal sTrafo = input.potenciaTransformadorKva();
        BigDecimal ds = input.demandaServicosDsKva();

        if (sTrafo != null && sTrafo.compareTo(BigDecimal.ZERO) > 0 && ds != null) {
            BigDecimal cargaServicosEVe = ds.add(dVe).setScale(2, RoundingMode.HALF_UP);
            BigDecimal limite50 = sTrafo.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);
            percentualUsoTrafo = cargaServicosEVe.multiply(new BigDecimal("100"))
                    .divide(sTrafo, 2, RoundingMode.HALF_UP);

            if (cargaServicosEVe.compareTo(limite50) > 0) {
                alertaTrafo50 = true;
                auditoria.append(String.format(Locale.US,
                        "ATENÇÃO NORMATIVA: A soma das cargas de serviços e recarga veicular (%.2f kVA) ultrapassa 50%% da capacidade do transformador dedicado (%.2f kVA, limite = %.2f kVA, uso = %.2f%%).",
                        cargaServicosEVe.doubleValue(), sTrafo.doubleValue(), limite50.doubleValue(), percentualUsoTrafo.doubleValue()));
            } else {
                auditoria.append(String.format(Locale.US,
                        "Capacidade do transformador dedicado: Carga de serviços + VE = %.2f kVA (%.2f%% da capacidade de %.2f kVA; limite de 50%% respeitado).",
                        cargaServicosEVe.doubleValue(), percentualUsoTrafo.doubleValue(), sTrafo.doubleValue()));
            }
        }

        return new SpecialLoadsCalculationResult(
                veOutput,
                mbiOutput,
                demandaEspecialTotal,
                parcelaDedutivelQdg,
                contribuicaoQdg,
                alertaTrafo50,
                percentualUsoTrafo,
                auditoria.toString().trim()
        );
    }

    public ElectricVehicleLoadOutput calcularRecargaVeicular(ElectricVehicleLoadInput input) {
        if (input == null || input.vagas() <= 0 || input.potenciaEstacaoKw() == null || input.potenciaEstacaoKw().compareTo(BigDecimal.ZERO) <= 0) {
            return new ElectricVehicleLoadOutput(
                    0,
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP),
                    FATOR_POTENCIA_VE_PADRAO.setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
            );
        }

        int vagas = input.vagas();
        BigDecimal pUnit = input.potenciaEstacaoKw().setScale(2, RoundingMode.HALF_UP);
        BigDecimal pTotal = new BigDecimal(vagas).multiply(pUnit).setScale(2, RoundingMode.HALF_UP);

        BigDecimal fVe = input.fatorSimultaneidadeCustom() != null
                ? input.fatorSimultaneidadeCustom().setScale(2, RoundingMode.HALF_UP)
                : resolverFatorSimultaneidadeVe(vagas);

        BigDecimal fp = input.fatorPotenciaCustom() != null
                ? input.fatorPotenciaCustom().setScale(2, RoundingMode.HALF_UP)
                : FATOR_POTENCIA_VE_PADRAO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal dVe = pTotal.multiply(fVe).divide(fp, 2, RoundingMode.HALF_UP);

        return new ElectricVehicleLoadOutput(
                vagas,
                pUnit,
                pTotal,
                fVe,
                fp,
                dVe
        );
    }

    /**
     * Fator de simultaneidade de recarga de VE (DIS-NOR-053 Rev. 06, Quadro 33).
     * Sem interpolação: 1–10 → 1,00; 11–20 → 0,86; … ; acima de 100 → 0,60.
     */
    public BigDecimal resolverFatorSimultaneidadeVe(int vagas) {
        if (vagas <= 0) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return neoguide.project.engine.normativo.TabelasNormativas.fatorVe(vagas).setScale(2, RoundingMode.HALF_UP);
    }

    public FirePumpLoadOutput calcularMotobombaIncendio(FirePumpLoadInput input) {
        if (input == null) {
            return new FirePumpLoadOutput(
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    true,
                    "N/A",
                    NORMA_BOMBA
            );
        }

        boolean derivadoMontante = input.derivadoAntesDoQdg();

        if (input.demandaKvaCustom() != null && input.demandaKvaCustom().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal cv = input.potenciaCv() != null ? input.potenciaCv().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            String partida = sugerirChavePartida(cv);
            return new FirePumpLoadOutput(
                    cv,
                    input.demandaKvaCustom().setScale(2, RoundingMode.HALF_UP),
                    derivadoMontante,
                    partida,
                    NORMA_BOMBA
            );
        }

        BigDecimal cv = input.potenciaCv();
        if (cv == null || cv.compareTo(BigDecimal.ZERO) <= 0) {
            return new FirePumpLoadOutput(
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    derivadoMontante,
                    "N/A",
                    NORMA_BOMBA
            );
        }

        cv = cv.setScale(2, RoundingMode.HALF_UP);
        BigDecimal kva = resolverDemandaTabela19(cv);
        String partida = sugerirChavePartida(cv);

        return new FirePumpLoadOutput(
                cv,
                kva,
                derivadoMontante,
                partida,
                NORMA_BOMBA
        );
    }

    /**
     * Demanda aparente (kVA) pela Tabela 19 (DIS-NOR-030 Rev. 07, coluna VA).
     * Potência tabelada: valor da norma, sem interpolação. Entre linhas: linha imediatamente acima (conservador).
     */
    public BigDecimal resolverDemandaTabela19(BigDecimal cv) {
        if (cv == null || cv.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return neoguide.project.engine.normativo.TabelasNormativas.kvaTabela19(cv).setScale(2, RoundingMode.HALF_UP);
    }

    public String sugerirChavePartida(BigDecimal cv) {
        if (cv == null || cv.compareTo(BigDecimal.ZERO) <= 0) return "N/A";
        if (cv.compareTo(new BigDecimal("7.50")) <= 0) return "DIRETA";
        if (cv.compareTo(new BigDecimal("20.00")) <= 0) return "ESTRELA_TRIANGULO";
        if (cv.compareTo(new BigDecimal("25.00")) <= 0) return "COMPENSADORA";
        return "SOFT_STARTER";
    }
}
