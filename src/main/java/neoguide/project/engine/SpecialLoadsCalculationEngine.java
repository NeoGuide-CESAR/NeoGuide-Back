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
        TABELA_19_MOTORES.put(new BigDecimal("0.50"), new MotorEntry(new BigDecimal("0.75"), "DIRETA"));
        TABELA_19_MOTORES.put(new BigDecimal("0.75"), new MotorEntry(new BigDecimal("1.03"), "DIRETA"));
        TABELA_19_MOTORES.put(new BigDecimal("1.00"), new MotorEntry(new BigDecimal("1.31"), "DIRETA"));
        TABELA_19_MOTORES.put(new BigDecimal("1.50"), new MotorEntry(new BigDecimal("1.86"), "DIRETA"));
        TABELA_19_MOTORES.put(new BigDecimal("2.00"), new MotorEntry(new BigDecimal("2.36"), "DIRETA"));
        TABELA_19_MOTORES.put(new BigDecimal("3.00"), new MotorEntry(new BigDecimal("3.37"), "DIRETA"));
        TABELA_19_MOTORES.put(new BigDecimal("4.00"), new MotorEntry(new BigDecimal("4.32"), "DIRETA"));
        TABELA_19_MOTORES.put(new BigDecimal("5.00"), new MotorEntry(new BigDecimal("5.27"), "DIRETA"));
        TABELA_19_MOTORES.put(new BigDecimal("7.50"), new MotorEntry(new BigDecimal("7.64"), "DIRETA"));
        TABELA_19_MOTORES.put(new BigDecimal("10.00"), new MotorEntry(new BigDecimal("9.95"), "ESTRELA_TRIANGULO"));
        TABELA_19_MOTORES.put(new BigDecimal("12.50"), new MotorEntry(new BigDecimal("12.14"), "ESTRELA_TRIANGULO"));
        TABELA_19_MOTORES.put(new BigDecimal("15.00"), new MotorEntry(new BigDecimal("14.40"), "ESTRELA_TRIANGULO"));
        TABELA_19_MOTORES.put(new BigDecimal("20.00"), new MotorEntry(new BigDecimal("19.10"), "ESTRELA_TRIANGULO"));
        TABELA_19_MOTORES.put(new BigDecimal("25.00"), new MotorEntry(new BigDecimal("23.50"), "COMPENSADORA"));
        TABELA_19_MOTORES.put(new BigDecimal("30.00"), new MotorEntry(new BigDecimal("28.00"), "SOFT_STARTER"));
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

    public BigDecimal resolverFatorSimultaneidadeVe(int vagas) {
        if (vagas <= 0) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (vagas == 1) return new BigDecimal("1.00");
        if (vagas == 2) return new BigDecimal("0.85");
        if (vagas == 3) return new BigDecimal("0.75");
        if (vagas <= 8) return new BigDecimal("0.70");
        if (vagas <= 12) return new BigDecimal("0.60");
        if (vagas <= 20) return new BigDecimal("0.50");
        if (vagas <= 30) return new BigDecimal("0.45");
        return new BigDecimal("0.35");
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

    public BigDecimal resolverDemandaTabela19(BigDecimal cv) {
        if (cv == null || cv.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        if (TABELA_19_MOTORES.containsKey(cv)) {
            return TABELA_19_MOTORES.get(cv).kva().setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal menor = TABELA_19_MOTORES.firstKey();
        BigDecimal maior = TABELA_19_MOTORES.lastKey();

        if (cv.compareTo(menor) < 0) {
            return TABELA_19_MOTORES.get(menor).kva().setScale(2, RoundingMode.HALF_UP);
        }

        if (cv.compareTo(maior) > 0) {
            // Proporção além de 30 CV (28 kVA / 30 CV = 0.9333 kVA/CV)
            return cv.multiply(new BigDecimal("0.9333")).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal floorKey = TABELA_19_MOTORES.floorKey(cv);
        BigDecimal ceilingKey = TABELA_19_MOTORES.ceilingKey(cv);

        BigDecimal kvaFloor = TABELA_19_MOTORES.get(floorKey).kva();
        BigDecimal kvaCeil = TABELA_19_MOTORES.get(ceilingKey).kva();

        // Interpolação linear
        BigDecimal deltaCv = ceilingKey.subtract(floorKey);
        BigDecimal deltaKva = kvaCeil.subtract(kvaFloor);
        BigDecimal ratio = cv.subtract(floorKey).divide(deltaCv, 4, RoundingMode.HALF_UP);

        return kvaFloor.add(ratio.multiply(deltaKva)).setScale(2, RoundingMode.HALF_UP);
    }

    public String sugerirChavePartida(BigDecimal cv) {
        if (cv == null || cv.compareTo(BigDecimal.ZERO) <= 0) return "N/A";
        if (cv.compareTo(new BigDecimal("7.50")) <= 0) return "DIRETA";
        if (cv.compareTo(new BigDecimal("20.00")) <= 0) return "ESTRELA_TRIANGULO";
        if (cv.compareTo(new BigDecimal("25.00")) <= 0) return "COMPENSADORA";
        return "SOFT_STARTER";
    }
}
