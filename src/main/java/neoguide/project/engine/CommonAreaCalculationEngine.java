package neoguide.project.engine;

import neoguide.project.engine.model.commonarea.AirConditioningItem;
import neoguide.project.engine.model.commonarea.ApplianceItem;
import neoguide.project.engine.model.commonarea.BoilerItem;
import neoguide.project.engine.model.commonarea.CommonAreaCalculationInput;
import neoguide.project.engine.model.commonarea.CommonAreaCalculationResult;
import neoguide.project.engine.model.commonarea.CommonAreaSubtotal;
import neoguide.project.engine.model.commonarea.CookingInput;
import neoguide.project.engine.model.commonarea.CooktopItem;
import neoguide.project.engine.model.commonarea.CustomLoadItem;
import neoguide.project.engine.model.commonarea.ElevatorItem;
import neoguide.project.engine.model.commonarea.LightingTugInput;
import neoguide.project.engine.model.commonarea.MotorItem;
import neoguide.project.engine.model.commonarea.OvenItem;
import neoguide.project.engine.model.commonarea.PoolPumpItem;
import neoguide.project.engine.model.commonarea.WaterHeaterItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Motor determinístico e puro de cálculo da Demanda de Serviços e Áreas Comuns (Ds)
 * segundo as diretrizes normativas da DIS-NOR-030 (Rev. 07) da Neoenergia Pernambuco.
 *
 * Apura analiticamente as 9 parcelas regulatórias (a a i) mais cargas manuais:
 * - Parcela a: Iluminação e Tomadas de Uso Geral (Tabela 22)
 * - Parcela b: Chuveiros e Aquecedores Rápidos (Tabela 7)
 * - Parcela c: Boilers Centrais de Acumulação (Tabela 8)
 * - Parcela d: Eletrodomésticos Coletivos (Tabela 9)
 * - Parcela e: Cocção Gourmet com Desmembramento Forno x Cooktop (Tabelas 9 e 10)
 * - Parcela f: Climatização e Ar-Condicionado (Tabelas 11 e 12)
 * - Parcela g: Força Motriz com Revezamento de Bombas (Tabelas 14 e 19)
 * - Parcela h: Elevadores e Equipamentos Especiais (Tabela 15)
 * - Parcela i: Bombas de Piscina e Hidromassagem (Tabela 16)
 * - Cargas Manuais Personalizadas
 */
@Component
public class CommonAreaCalculationEngine {

    private static final int CALC_SCALE = 6;
    private static final int DISPLAY_SCALE = 2;
    private static final int FACTOR_SCALE = 4;
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    // Fatores de Potência Fixos da Norma
    private static final BigDecimal FP_ILUMINACAO = new BigDecimal("0.80");
    private static final BigDecimal FP_TUG = new BigDecimal("1.00");
    private static final BigDecimal FP_AQUECEDOR_RAPIDO = new BigDecimal("1.00");
    private static final BigDecimal FP_BOILER = new BigDecimal("1.00");
    private static final BigDecimal FP_ELETRODOMESTICOS = new BigDecimal("0.92");
    private static final BigDecimal FP_FORNO = new BigDecimal("1.00");
    private static final BigDecimal FP_COOKTOP = new BigDecimal("1.00");
    private static final BigDecimal FP_CLIMATIZACAO = new BigDecimal("0.92");
    private static final BigDecimal FP_ELEVADOR = new BigDecimal("0.92");
    private static final BigDecimal FP_PISCINA = new BigDecimal("0.80");

    // Tabela 19 da DIS-NOR-030: cv -> kVA trifásico a 380V
    private static final NavigableMap<BigDecimal, BigDecimal> TABELA_19_CV_KVA = new TreeMap<>();

    // Tabela 11 da DIS-NOR-030: BTU/h -> Potência W e Potência VA
    private static final Map<Integer, BtuPower> TABELA_11_BTU_W = new TreeMap<>();

    private record BtuPower(BigDecimal watts, BigDecimal va) {}

    static {
        // Tabela 19
        TABELA_19_CV_KVA.put(new BigDecimal("0.50"), new BigDecimal("0.75"));
        TABELA_19_CV_KVA.put(new BigDecimal("0.75"), new BigDecimal("1.03"));
        TABELA_19_CV_KVA.put(new BigDecimal("1.00"), new BigDecimal("1.31"));
        TABELA_19_CV_KVA.put(new BigDecimal("1.50"), new BigDecimal("1.86"));
        TABELA_19_CV_KVA.put(new BigDecimal("2.00"), new BigDecimal("2.36"));
        TABELA_19_CV_KVA.put(new BigDecimal("3.00"), new BigDecimal("3.37"));
        TABELA_19_CV_KVA.put(new BigDecimal("4.00"), new BigDecimal("4.32"));
        TABELA_19_CV_KVA.put(new BigDecimal("5.00"), new BigDecimal("5.27"));
        TABELA_19_CV_KVA.put(new BigDecimal("7.50"), new BigDecimal("7.64"));
        TABELA_19_CV_KVA.put(new BigDecimal("10.00"), new BigDecimal("9.95"));
        TABELA_19_CV_KVA.put(new BigDecimal("12.50"), new BigDecimal("12.14"));
        TABELA_19_CV_KVA.put(new BigDecimal("15.00"), new BigDecimal("14.40"));

        // Tabela 11
        TABELA_11_BTU_W.put(7500, new BtuPower(new BigDecimal("900.00"), new BigDecimal("1100.00")));
        TABELA_11_BTU_W.put(9000, new BtuPower(new BigDecimal("1300.00"), new BigDecimal("1550.00")));
        TABELA_11_BTU_W.put(10000, new BtuPower(new BigDecimal("1400.00"), new BigDecimal("1650.00")));
        TABELA_11_BTU_W.put(12000, new BtuPower(new BigDecimal("1600.00"), new BigDecimal("1900.00")));
        TABELA_11_BTU_W.put(15000, new BtuPower(new BigDecimal("1900.00"), new BigDecimal("2100.00")));
        TABELA_11_BTU_W.put(18000, new BtuPower(new BigDecimal("2300.00"), new BigDecimal("2600.00")));
        TABELA_11_BTU_W.put(24000, new BtuPower(new BigDecimal("3000.00"), new BigDecimal("3400.00")));
        TABELA_11_BTU_W.put(30000, new BtuPower(new BigDecimal("3800.00"), new BigDecimal("4300.00")));
    }

    /**
     * Ponto de entrada operacional do cálculo consolidado de áreas comuns.
     */
    public CommonAreaCalculationResult calculate(CommonAreaCalculationInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Parâmetros de entrada para o cálculo de áreas comuns não podem ser nulos.");
        }

        CommonAreaSubtotal parcelaA = calculateParcelaA(input.iluminacaoTug());
        CommonAreaSubtotal parcelaB = calculateParcelaB(input.aquecedoresPassagem());
        CommonAreaSubtotal parcelaC = calculateParcelaC(input.boilers());
        CommonAreaSubtotal parcelaD = calculateParcelaD(input.eletrodomesticos());
        CommonAreaSubtotal parcelaE = calculateParcelaE(input.coccaoGourmet());
        CommonAreaSubtotal parcelaF = calculateParcelaF(input.climatizacao());
        CommonAreaSubtotal parcelaG = calculateParcelaG(input.motores());
        CommonAreaSubtotal parcelaH = calculateParcelaH(input.elevadores());
        CommonAreaSubtotal parcelaI = calculateParcelaI(input.bombasPiscina());
        CommonAreaSubtotal custom = calculateCustomLoads(input.cargasPersonalizadas());

        BigDecimal demandaTotalDs = parcelaA.demandaKva()
                .add(parcelaB.demandaKva())
                .add(parcelaC.demandaKva())
                .add(parcelaD.demandaKva())
                .add(parcelaE.demandaKva())
                .add(parcelaF.demandaKva())
                .add(parcelaG.demandaKva())
                .add(parcelaH.demandaKva())
                .add(parcelaI.demandaKva())
                .add(custom.demandaKva())
                .setScale(DISPLAY_SCALE, RM);

        return new CommonAreaCalculationResult(
                demandaTotalDs,
                parcelaA,
                parcelaB,
                parcelaC,
                parcelaD,
                parcelaE,
                parcelaF,
                parcelaG,
                parcelaH,
                parcelaI,
                custom
        );
    }

    // =========================================================================
    // PARCELA A: Iluminação e TUG (Tabela 22)
    // a = (P_ilum * 1.00 / 0.80) + (P_tug * 0.50 / 1.00)
    // =========================================================================
    public CommonAreaSubtotal calculateParcelaA(LightingTugInput input) {
        if (input == null) {
            return zeroSubtotal("Parcela A (Iluminação e TUG): nenhuma carga informada.");
        }

        BigDecimal pIlum = input.potenciaIluminacaoKw() != null ? input.potenciaIluminacaoKw() : BigDecimal.ZERO;
        BigDecimal pTug = input.potenciaTugKw() != null ? input.potenciaTugKw() : BigDecimal.ZERO;

        validateNonNegative(pIlum, "potenciaIluminacaoKw");
        validateNonNegative(pTug, "potenciaTugKw");

        BigDecimal sIlum = pIlum.divide(FP_ILUMINACAO, CALC_SCALE, RM);
        BigDecimal sTug = pTug.multiply(new BigDecimal("0.50")).divide(FP_TUG, CALC_SCALE, RM);
        BigDecimal demandaKva = sIlum.add(sTug).setScale(DISPLAY_SCALE, RM);
        BigDecimal potenciaAtivaKw = pIlum.add(pTug).setScale(DISPLAY_SCALE, RM);

        String memoria = String.format("Iluminação: %s kW / 0.80 = %s kVA; TUG: (%s kW * 0.50) / 1.00 = %s kVA; Total = %s kVA",
                pIlum.setScale(DISPLAY_SCALE, RM), sIlum.setScale(DISPLAY_SCALE, RM),
                pTug.setScale(DISPLAY_SCALE, RM), sTug.setScale(DISPLAY_SCALE, RM),
                demandaKva);

        return new CommonAreaSubtotal(
                demandaKva,
                potenciaAtivaKw,
                new BigDecimal("1.0000"),
                FP_ILUMINACAO,
                memoria
        );
    }

    // =========================================================================
    // PARCELA B: Chuveiros e Aquecedores Rápidos de Passagem (Tabela 7)
    // b = (P_total * FD) / 1.00
    // =========================================================================
    public CommonAreaSubtotal calculateParcelaB(List<WaterHeaterItem> items) {
        if (items == null || items.isEmpty()) {
            return zeroSubtotal("Parcela B (Aquecedores de Passagem): nenhuma carga informada.");
        }

        int totalQtd = 0;
        BigDecimal potenciaAtivaTotal = BigDecimal.ZERO;

        for (WaterHeaterItem item : items) {
            if (item == null) continue;
            validateItem(item.potenciaKw(), item.quantidade(), "Aquecedor de Passagem");
            totalQtd += item.quantidade();
            BigDecimal sub = item.potenciaKw().multiply(BigDecimal.valueOf(item.quantidade()));
            potenciaAtivaTotal = potenciaAtivaTotal.add(sub);
        }

        if (totalQtd == 0) {
            return zeroSubtotal("Parcela B (Aquecedores de Passagem): quantidade total zero.");
        }

        BigDecimal fd = resolveTabela7FatorDemanda(totalQtd);
        BigDecimal demandaKva = potenciaAtivaTotal.multiply(fd).divide(FP_AQUECEDOR_RAPIDO, CALC_SCALE, RM)
                .setScale(DISPLAY_SCALE, RM);

        String memoria = String.format("Total: %d aparelhos, P = %s kW, FD = %s (Tabela 7), FP = 1.00 => Demanda = %s kVA",
                totalQtd, potenciaAtivaTotal.setScale(DISPLAY_SCALE, RM), fd, demandaKva);

        return new CommonAreaSubtotal(
                demandaKva,
                potenciaAtivaTotal.setScale(DISPLAY_SCALE, RM),
                fd,
                FP_AQUECEDOR_RAPIDO,
                memoria
        );
    }

    // =========================================================================
    // PARCELA C: Boilers Centrais de Acumulação (Tabela 8)
    // c = (P_total * FD) / 1.00
    // =========================================================================
    public CommonAreaSubtotal calculateParcelaC(List<BoilerItem> items) {
        if (items == null || items.isEmpty()) {
            return zeroSubtotal("Parcela C (Boilers Centrais): nenhuma carga informada.");
        }

        int totalQtd = 0;
        BigDecimal potenciaAtivaTotal = BigDecimal.ZERO;

        for (BoilerItem item : items) {
            if (item == null) continue;
            validateItem(item.potenciaKw(), item.quantidade(), "Boiler Central");
            totalQtd += item.quantidade();
            BigDecimal sub = item.potenciaKw().multiply(BigDecimal.valueOf(item.quantidade()));
            potenciaAtivaTotal = potenciaAtivaTotal.add(sub);
        }

        if (totalQtd == 0) {
            return zeroSubtotal("Parcela C (Boilers Centrais): quantidade total zero.");
        }

        BigDecimal fd = resolveTabela8FatorDemanda(totalQtd);
        BigDecimal demandaKva = potenciaAtivaTotal.multiply(fd).divide(FP_BOILER, CALC_SCALE, RM)
                .setScale(DISPLAY_SCALE, RM);

        String memoria = String.format("Total: %d boilers, P = %s kW, FD = %s (Tabela 8), FP = 1.00 => Demanda = %s kVA",
                totalQtd, potenciaAtivaTotal.setScale(DISPLAY_SCALE, RM), fd, demandaKva);

        return new CommonAreaSubtotal(
                demandaKva,
                potenciaAtivaTotal.setScale(DISPLAY_SCALE, RM),
                fd,
                FP_BOILER,
                memoria
        );
    }

    // =========================================================================
    // PARCELA D: Eletrodomésticos Coletivos (Tabela 9)
    // d = (P_total * FD) / 0.92
    // =========================================================================
    public CommonAreaSubtotal calculateParcelaD(List<ApplianceItem> items) {
        if (items == null || items.isEmpty()) {
            return zeroSubtotal("Parcela D (Eletrodomésticos Coletivos): nenhuma carga informada.");
        }

        int totalQtd = 0;
        BigDecimal potenciaAtivaTotal = BigDecimal.ZERO;

        for (ApplianceItem item : items) {
            if (item == null) continue;
            validateItem(item.potenciaKw(), item.quantidade(), "Eletrodoméstico");
            totalQtd += item.quantidade();
            BigDecimal sub = item.potenciaKw().multiply(BigDecimal.valueOf(item.quantidade()));
            potenciaAtivaTotal = potenciaAtivaTotal.add(sub);
        }

        if (totalQtd == 0) {
            return zeroSubtotal("Parcela D (Eletrodomésticos Coletivos): quantidade total zero.");
        }

        BigDecimal fd = resolveTabela9FatorDemanda(totalQtd);
        BigDecimal demandaKva = potenciaAtivaTotal.multiply(fd).divide(FP_ELETRODOMESTICOS, CALC_SCALE, RM)
                .setScale(DISPLAY_SCALE, RM);

        String memoria = String.format("Total: %d aparelhos, P = %s kW, FD = %s (Tabela 9), FP = 0.92 => Demanda = %s kVA",
                totalQtd, potenciaAtivaTotal.setScale(DISPLAY_SCALE, RM), fd, demandaKva);

        return new CommonAreaSubtotal(
                demandaKva,
                potenciaAtivaTotal.setScale(DISPLAY_SCALE, RM),
                fd,
                FP_ELETRODOMESTICOS,
                memoria
        );
    }

    // =========================================================================
    // PARCELA E: Cocção Gourmet com Desmembramento Forno x Cooktop
    // e1 (Fornos - Tabela 9, FP=1.00) + e2 (Cooktops - Tabela 10, FP=1.00)
    // =========================================================================
    public CommonAreaSubtotal calculateParcelaE(CookingInput input) {
        if (input == null || ((input.fornos() == null || input.fornos().isEmpty()) && (input.cooktops() == null || input.cooktops().isEmpty()))) {
            return zeroSubtotal("Parcela E (Cocção Gourmet): nenhuma carga informada.");
        }

        // 1. Fornos Elétricos (Tabela 9)
        int qtdFornos = 0;
        BigDecimal potenciaFornos = BigDecimal.ZERO;
        if (input.fornos() != null) {
            for (OvenItem item : input.fornos()) {
                if (item == null) continue;
                validateItem(item.potenciaKw(), item.quantidade(), "Forno Elétrico");
                qtdFornos += item.quantidade();
                potenciaFornos = potenciaFornos.add(item.potenciaKw().multiply(BigDecimal.valueOf(item.quantidade())));
            }
        }

        BigDecimal fdFornos = qtdFornos > 0 ? resolveTabela9FatorDemanda(qtdFornos) : BigDecimal.ZERO;
        BigDecimal demandaFornosKva = qtdFornos > 0
                ? potenciaFornos.multiply(fdFornos).divide(FP_FORNO, CALC_SCALE, RM)
                : BigDecimal.ZERO;

        // 2. Cooktops de Indução (Tabela 10)
        int qtdCooktops = 0;
        BigDecimal potenciaCooktops = BigDecimal.ZERO;
        if (input.cooktops() != null) {
            for (CooktopItem item : input.cooktops()) {
                if (item == null) continue;
                validateItem(item.potenciaKw(), item.quantidade(), "Cooktop de Indução");
                qtdCooktops += item.quantidade();
                potenciaCooktops = potenciaCooktops.add(item.potenciaKw().multiply(BigDecimal.valueOf(item.quantidade())));
            }
        }

        BigDecimal fdCooktops = qtdCooktops > 0 ? resolveTabela10FatorDemanda(qtdCooktops) : BigDecimal.ZERO;
        BigDecimal demandaCooktopsKva = qtdCooktops > 0
                ? potenciaCooktops.multiply(fdCooktops).divide(FP_COOKTOP, CALC_SCALE, RM)
                : BigDecimal.ZERO;

        BigDecimal demandaTotalKva = demandaFornosKva.add(demandaCooktopsKva).setScale(DISPLAY_SCALE, RM);
        BigDecimal potenciaTotalKw = potenciaFornos.add(potenciaCooktops).setScale(DISPLAY_SCALE, RM);

        String memoria = String.format("Fornos: %d un, P = %s kW, FD = %s => %s kVA; Cooktops: %d un, P = %s kW, FD = %s => %s kVA; Total = %s kVA",
                qtdFornos, potenciaFornos.setScale(DISPLAY_SCALE, RM), fdFornos, demandaFornosKva.setScale(DISPLAY_SCALE, RM),
                qtdCooktops, potenciaCooktops.setScale(DISPLAY_SCALE, RM), fdCooktops, demandaCooktopsKva.setScale(DISPLAY_SCALE, RM),
                demandaTotalKva);

        return new CommonAreaSubtotal(
                demandaTotalKva,
                potenciaTotalKw,
                new BigDecimal("1.0000"),
                FP_COOKTOP,
                memoria
        );
    }

    // =========================================================================
    // PARCELA F: Climatização e Ar-Condicionado (Tabelas 11 e 12)
    // f = (P_total_kW * 1.00) / 0.92
    // =========================================================================
    public CommonAreaSubtotal calculateParcelaF(List<AirConditioningItem> items) {
        if (items == null || items.isEmpty()) {
            return zeroSubtotal("Parcela F (Climatização): nenhuma carga informada.");
        }

        BigDecimal potenciaAtivaTotalKw = BigDecimal.ZERO;
        int totalQtd = 0;

        for (AirConditioningItem item : items) {
            if (item == null) continue;
            if (item.quantidade() <= 0) {
                throw new IllegalArgumentException("A quantidade de aparelhos de climatização deve ser maior que zero.");
            }

            BigDecimal pWatts = item.potenciaW();
            if (pWatts == null && item.capacidadeBtu() != null) {
                BtuPower power = TABELA_11_BTU_W.get(item.capacidadeBtu());
                if (power != null) {
                    pWatts = power.watts();
                } else {
                    throw new IllegalArgumentException("Capacidade BTU não encontrada na Tabela 11: " + item.capacidadeBtu());
                }
            }

            if (pWatts == null) {
                throw new IllegalArgumentException("Potência do aparelho de climatização não informada.");
            }
            validateNonNegative(pWatts, "potenciaW");

            BigDecimal pKwUnitario = pWatts.divide(new BigDecimal("1000.00"), CALC_SCALE, RM);
            potenciaAtivaTotalKw = potenciaAtivaTotalKw.add(pKwUnitario.multiply(BigDecimal.valueOf(item.quantidade())));
            totalQtd += item.quantidade();
        }

        if (totalQtd == 0) {
            return zeroSubtotal("Parcela F (Climatização): quantidade total zero.");
        }

        BigDecimal fd = new BigDecimal("1.0000");
        BigDecimal demandaKva = potenciaAtivaTotalKw.multiply(fd).divide(FP_CLIMATIZACAO, CALC_SCALE, RM)
                .setScale(DISPLAY_SCALE, RM);

        String memoria = String.format("Total: %d aparelhos, P = %s kW, FD = %s, FP = 0.92 => Demanda = %s kVA",
                totalQtd, potenciaAtivaTotalKw.setScale(DISPLAY_SCALE, RM), fd, demandaKva);

        return new CommonAreaSubtotal(
                demandaKva,
                potenciaAtivaTotalKw.setScale(DISPLAY_SCALE, RM),
                fd,
                FP_CLIMATIZACAO,
                memoria
        );
    }

    // =========================================================================
    // PARCELA G: Força Motriz com Revezamento de Bombas (Tabelas 14 e 19)
    // Maior motor ativo: FD = 1.00
    // Demais motores ativos: FD = 0.50
    // Motores reserva (isReserva = true): FD = 0.00
    // =========================================================================
    public CommonAreaSubtotal calculateParcelaG(List<MotorItem> items) {
        if (items == null || items.isEmpty()) {
            return zeroSubtotal("Parcela G (Força Motriz): nenhum motor informado.");
        }

        List<BigDecimal> ativosKva = new ArrayList<>();
        BigDecimal potenciaAtivaEstimadaKw = BigDecimal.ZERO;
        int reservasCount = 0;

        for (MotorItem item : items) {
            if (item == null) continue;
            BigDecimal kva = item.demandaKvaTabela19();
            if (kva == null) {
                if (item.potenciaCv() == null) {
                    throw new IllegalArgumentException("Potência mecânica em CV ou demanda em kVA deve ser informada para o motor.");
                }
                validateNonNegative(item.potenciaCv(), "potenciaCv");
                kva = resolveTabela19DemandaKva(item.potenciaCv());
            } else {
                validateNonNegative(kva, "demandaKvaTabela19");
            }

            // Potência aproximada em kW (1 cv ~ 0.7355 kW)
            if (item.potenciaCv() != null) {
                potenciaAtivaEstimadaKw = potenciaAtivaEstimadaKw.add(item.potenciaCv().multiply(new BigDecimal("0.7355")));
            }

            if (item.isReserva()) {
                reservasCount++;
            } else {
                ativosKva.add(kva);
            }
        }

        if (ativosKva.isEmpty()) {
            return new CommonAreaSubtotal(
                    BigDecimal.ZERO.setScale(DISPLAY_SCALE, RM),
                    potenciaAtivaEstimadaKw.setScale(DISPLAY_SCALE, RM),
                    BigDecimal.ZERO.setScale(FACTOR_SCALE, RM),
                    BigDecimal.ONE,
                    "Todos os " + reservasCount + " motores estão configurados como reserva (FD = 0.00)."
            );
        }

        // Ordenar do maior para o menor
        ativosKva.sort(Comparator.reverseOrder());

        BigDecimal maiorMotorKva = ativosKva.get(0);
        BigDecimal somaDemaisKva = BigDecimal.ZERO;

        for (int i = 1; i < ativosKva.size(); i++) {
            somaDemaisKva = somaDemaisKva.add(ativosKva.get(i).multiply(new BigDecimal("0.50")));
        }

        BigDecimal demandaKva = maiorMotorKva.add(somaDemaisKva).setScale(DISPLAY_SCALE, RM);

        String memoria = String.format("Motores ativos: %d (Maior: %s kVA [FD=1.00]; Demais: %d motores [FD=0.50]); Reservas: %d [FD=0.00] => Total = %s kVA",
                ativosKva.size(), maiorMotorKva.setScale(DISPLAY_SCALE, RM), ativosKva.size() - 1, reservasCount, demandaKva);

        return new CommonAreaSubtotal(
                demandaKva,
                potenciaAtivaEstimadaKw.setScale(DISPLAY_SCALE, RM),
                new BigDecimal("1.0000"),
                BigDecimal.ONE,
                memoria
        );
    }

    // =========================================================================
    // PARCELA H: Elevadores e Equipamentos Especiais (Tabela 15)
    // Maior elevador: FD = 1.00
    // Demais elevadores: FD = 0.60
    // FP = 0.92
    // =========================================================================
    public CommonAreaSubtotal calculateParcelaH(List<ElevatorItem> items) {
        if (items == null || items.isEmpty()) {
            return zeroSubtotal("Parcela H (Elevadores): nenhum elevador informado.");
        }

        List<BigDecimal> elevadoresKw = new ArrayList<>();
        for (ElevatorItem item : items) {
            if (item == null) continue;
            validateItem(item.potenciaKw(), item.quantidade(), "Elevador");
            for (int q = 0; q < item.quantidade(); q++) {
                elevadoresKw.add(item.potenciaKw());
            }
        }

        if (elevadoresKw.isEmpty()) {
            return zeroSubtotal("Parcela H (Elevadores): quantidade total zero.");
        }

        elevadoresKw.sort(Comparator.reverseOrder());

        BigDecimal maiorElevadorKw = elevadoresKw.get(0);
        BigDecimal somaDemaisKw = BigDecimal.ZERO;
        BigDecimal potenciaTotalKw = maiorElevadorKw;

        for (int i = 1; i < elevadoresKw.size(); i++) {
            BigDecimal kw = elevadoresKw.get(i);
            potenciaTotalKw = potenciaTotalKw.add(kw);
            somaDemaisKw = somaDemaisKw.add(kw.multiply(new BigDecimal("0.60")));
        }

        BigDecimal potenciaPonderadaKw = maiorElevadorKw.add(somaDemaisKw);
        BigDecimal demandaKva = potenciaPonderadaKw.divide(FP_ELEVADOR, CALC_SCALE, RM)
                .setScale(DISPLAY_SCALE, RM);

        String memoria = String.format("Elevadores: %d (Maior: %s kW [FD=1.00]; Demais: %d [FD=0.60]), FP = 0.92 => Demanda = %s kVA",
                elevadoresKw.size(), maiorElevadorKw.setScale(DISPLAY_SCALE, RM), elevadoresKw.size() - 1, demandaKva);

        return new CommonAreaSubtotal(
                demandaKva,
                potenciaTotalKw.setScale(DISPLAY_SCALE, RM),
                new BigDecimal("1.0000"),
                FP_ELEVADOR,
                memoria
        );
    }

    // =========================================================================
    // PARCELA I: Bombas de Piscina e Hidromassagem (Tabela 16)
    // i = (P_total * FD) / 0.80
    // =========================================================================
    public CommonAreaSubtotal calculateParcelaI(List<PoolPumpItem> items) {
        if (items == null || items.isEmpty()) {
            return zeroSubtotal("Parcela I (Bombas de Piscina): nenhuma bomba informada.");
        }

        int totalQtd = 0;
        BigDecimal potenciaAtivaTotal = BigDecimal.ZERO;

        for (PoolPumpItem item : items) {
            if (item == null) continue;
            validateItem(item.potenciaKw(), item.quantidade(), "Bomba de Piscina");
            totalQtd += item.quantidade();
            potenciaAtivaTotal = potenciaAtivaTotal.add(item.potenciaKw().multiply(BigDecimal.valueOf(item.quantidade())));
        }

        if (totalQtd == 0) {
            return zeroSubtotal("Parcela I (Bombas de Piscina): quantidade total zero.");
        }

        BigDecimal fd = resolveTabela16FatorDemanda(totalQtd);
        BigDecimal demandaKva = potenciaAtivaTotal.multiply(fd).divide(FP_PISCINA, CALC_SCALE, RM)
                .setScale(DISPLAY_SCALE, RM);

        String memoria = String.format("Total: %d bombas, P = %s kW, FD = %s (Tabela 16), FP = 0.80 => Demanda = %s kVA",
                totalQtd, potenciaAtivaTotal.setScale(DISPLAY_SCALE, RM), fd, demandaKva);

        return new CommonAreaSubtotal(
                demandaKva,
                potenciaAtivaTotal.setScale(DISPLAY_SCALE, RM),
                fd,
                FP_PISCINA,
                memoria
        );
    }

    // =========================================================================
    // CARGAS PERSONALIZADAS AVULSAS
    // S_k = (P_kW * Qtd * FD) / FP
    // =========================================================================
    public CommonAreaSubtotal calculateCustomLoads(List<CustomLoadItem> items) {
        if (items == null || items.isEmpty()) {
            return zeroSubtotal("Cargas Personalizadas: nenhuma carga informada.");
        }

        BigDecimal potenciaTotalKw = BigDecimal.ZERO;
        BigDecimal demandaTotalKva = BigDecimal.ZERO;
        int totalItens = 0;

        for (CustomLoadItem item : items) {
            if (item == null) continue;
            validateCustomItem(item);

            BigDecimal pUnit = item.potenciaKw();
            BigDecimal fp = item.fatorPotencia();
            BigDecimal fd = item.fatorDemanda();
            int qtd = item.quantidade();

            BigDecimal pItem = pUnit.multiply(BigDecimal.valueOf(qtd));
            potenciaTotalKw = potenciaTotalKw.add(pItem);

            BigDecimal sItem = pItem.multiply(fd).divide(fp, CALC_SCALE, RM);
            demandaTotalKva = demandaTotalKva.add(sItem);
            totalItens += qtd;
        }

        if (totalItens == 0) {
            return zeroSubtotal("Cargas Personalizadas: quantidade total zero.");
        }

        BigDecimal finalDemandaKva = demandaTotalKva.setScale(DISPLAY_SCALE, RM);
        String memoria = String.format("Total: %d cargas manuais, P = %s kW => Demanda = %s kVA",
                totalItens, potenciaTotalKw.setScale(DISPLAY_SCALE, RM), finalDemandaKva);

        return new CommonAreaSubtotal(
                finalDemandaKva,
                potenciaTotalKw.setScale(DISPLAY_SCALE, RM),
                new BigDecimal("1.0000"),
                BigDecimal.ONE,
                memoria
        );
    }

    // =========================================================================
    // RESOLUÇÃO DE TABELAS NORMATIVAS DIS-NOR-030
    // =========================================================================

    public BigDecimal resolveTabela7FatorDemanda(int quantidade) {
        if (quantidade <= 0) return BigDecimal.ZERO;
        if (quantidade <= 2) return new BigDecimal("1.00");
        return switch (quantidade) {
            case 3 -> new BigDecimal("0.84");
            case 4 -> new BigDecimal("0.76");
            case 5 -> new BigDecimal("0.70");
            case 6 -> new BigDecimal("0.65");
            case 7 -> new BigDecimal("0.60");
            case 8 -> new BigDecimal("0.57");
            case 9 -> new BigDecimal("0.54");
            case 10 -> new BigDecimal("0.52");
            case 11 -> new BigDecimal("0.49");
            case 12 -> new BigDecimal("0.48");
            case 13 -> new BigDecimal("0.46");
            case 14 -> new BigDecimal("0.45");
            case 15 -> new BigDecimal("0.44");
            case 16 -> new BigDecimal("0.43");
            case 17 -> new BigDecimal("0.42");
            case 18 -> new BigDecimal("0.41");
            case 19, 20 -> new BigDecimal("0.40");
            case 21, 22, 23 -> new BigDecimal("0.39");
            default -> new BigDecimal("0.38");
        };
    }

    public BigDecimal resolveTabela8FatorDemanda(int quantidade) {
        if (quantidade <= 0) return BigDecimal.ZERO;
        if (quantidade == 1) return new BigDecimal("1.00");
        if (quantidade == 2) return new BigDecimal("0.72");
        return new BigDecimal("0.62");
    }

    public BigDecimal resolveTabela9FatorDemanda(int quantidade) {
        if (quantidade <= 0) return BigDecimal.ZERO;
        if (quantidade == 1) return new BigDecimal("1.00");
        if (quantidade <= 4) return new BigDecimal("0.70");
        if (quantidade <= 6) return new BigDecimal("0.60");
        return new BigDecimal("0.50");
    }

    public BigDecimal resolveTabela10FatorDemanda(int quantidade) {
        if (quantidade <= 0) return BigDecimal.ZERO;
        return switch (quantidade) {
            case 1 -> new BigDecimal("1.00");
            case 2 -> new BigDecimal("0.60");
            case 3 -> new BigDecimal("0.48");
            case 4 -> new BigDecimal("0.40");
            case 5 -> new BigDecimal("0.37");
            case 6 -> new BigDecimal("0.35");
            case 7 -> new BigDecimal("0.33");
            case 8 -> new BigDecimal("0.32");
            case 9 -> new BigDecimal("0.31");
            case 10, 11 -> new BigDecimal("0.30");
            case 12, 13, 14, 15 -> new BigDecimal("0.28");
            default -> new BigDecimal("0.26");
        };
    }

    public BigDecimal resolveTabela16FatorDemanda(int quantidade) {
        if (quantidade <= 0) return BigDecimal.ZERO;
        return switch (quantidade) {
            case 1 -> new BigDecimal("1.00");
            case 2 -> new BigDecimal("0.56");
            case 3 -> new BigDecimal("0.47");
            default -> new BigDecimal("0.39");
        };
    }

    public BigDecimal resolveTabela19DemandaKva(BigDecimal potenciaCv) {
        if (potenciaCv == null || potenciaCv.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Potência do motor em CV deve ser maior que zero.");
        }
        BigDecimal normalizedCv = potenciaCv.setScale(2, RM);
        if (TABELA_19_CV_KVA.containsKey(normalizedCv)) {
            return TABELA_19_CV_KVA.get(normalizedCv);
        }
        // Ponto mais próximo ou teto
        Map.Entry<BigDecimal, BigDecimal> ceiling = TABELA_19_CV_KVA.ceilingEntry(normalizedCv);
        if (ceiling != null) {
            return ceiling.getValue();
        }
        // Se maior que o maior motor da tabela (15 cv), extrapola proporcionalmente
        BigDecimal maiorCv = TABELA_19_CV_KVA.lastKey();
        BigDecimal maiorKva = TABELA_19_CV_KVA.get(maiorCv);
        return normalizedCv.multiply(maiorKva).divide(maiorCv, CALC_SCALE, RM).setScale(DISPLAY_SCALE, RM);
    }

    // =========================================================================
    // VALIDAÇÕES AUXILIARES
    // =========================================================================

    private void validateNonNegative(BigDecimal value, String fieldName) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O campo " + fieldName + " não pode ter valor negativo.");
        }
    }

    private void validateItem(BigDecimal potenciaKw, int quantidade, String tipo) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade para " + tipo + " deve ser maior que zero.");
        }
        if (potenciaKw == null || potenciaKw.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A potência em kW para " + tipo + " deve ser maior que zero.");
        }
    }

    private void validateCustomItem(CustomLoadItem item) {
        if (item.quantidade() <= 0) {
            throw new IllegalArgumentException("A quantidade de cargas personalizadas deve ser maior que zero.");
        }
        if (item.potenciaKw() == null || item.potenciaKw().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A potência em kW da carga personalizada deve ser maior que zero.");
        }
        if (item.fatorPotencia() == null || item.fatorPotencia().compareTo(BigDecimal.ZERO) <= 0
                || item.fatorPotencia().compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("O fator de potência da carga personalizada deve estar no intervalo (0, 1.0].");
        }
        if (item.fatorDemanda() == null || item.fatorDemanda().compareTo(BigDecimal.ZERO) < 0
                || item.fatorDemanda().compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("O fator de demanda da carga personalizada deve estar no intervalo [0, 1.0].");
        }
    }

    private CommonAreaSubtotal zeroSubtotal(String memoria) {
        return new CommonAreaSubtotal(
                BigDecimal.ZERO.setScale(DISPLAY_SCALE, RM),
                BigDecimal.ZERO.setScale(DISPLAY_SCALE, RM),
                BigDecimal.ZERO.setScale(FACTOR_SCALE, RM),
                BigDecimal.ONE.setScale(DISPLAY_SCALE, RM),
                memoria
        );
    }
}
