package neoguide.project.engine;

import neoguide.project.engine.enums.SupplyVoltageLevel;
import neoguide.project.engine.enums.VoltageSystem;
import neoguide.project.engine.model.BusbarDimensioning;
import neoguide.project.engine.model.ConsolidatedCalculationInput;
import neoguide.project.engine.model.ConsolidatedCalculationResult;
import neoguide.project.engine.model.ServiceEntranceDimensioning;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class ConsolidatedCalculationEngine {

    private static final BigDecimal LIMITE_BAIXA_TENSAO = new BigDecimal("300.00");
    private static final String NORMA_TABELA_1 = "DIS-NOR-053 Anexo II Tabela 1";
    private static final String NORMA_TABELA_2 = "DIS-NOR-053 Anexo II Tabela 2";
    private static final String NORMA_TABELA_3 = "DIS-NOR-053 Anexo II Tabela 3";

    private static final List<ServiceEntranceDimensioning> TABELA_2_380V = List.of(
            new ServiceEntranceDimensioning("380/220 V", new BigDecimal("46.00"), 1, "1 x 16 mm²", "1 x 16 mm²", "1 x 110 mm", "1 x 100 mm", 70, "160 A", 70, 10, NORMA_TABELA_2),
            new ServiceEntranceDimensioning("380/220 V", new BigDecimal("53.00"), 1, "1 x 25 mm²", "1 x 25 mm²", "1 x 110 mm", "1 x 100 mm", 80, "160 A", 80, 10, NORMA_TABELA_2),
            new ServiceEntranceDimensioning("380/220 V", new BigDecimal("66.00"), 1, "1 x 35 mm²", "1 x 35 mm²", "1 x 110 mm", "1 x 100 mm", 100, "160 A", 100, 10, NORMA_TABELA_2),
            new ServiceEntranceDimensioning("380/220 V", new BigDecimal("82.00"), 1, "1 x 50 mm²", "1 x 50 mm²", "1 x 110 mm", "1 x 100 mm", 125, "160 A", 125, 10, NORMA_TABELA_2),
            new ServiceEntranceDimensioning("380/220 V", new BigDecimal("99.00"), 1, "1 x 70 mm²", "1 x 70 mm²", "1 x 110 mm", "1 x 100 mm", 150, "160 A", 150, 10, NORMA_TABELA_2),
            new ServiceEntranceDimensioning("380/220 V", new BigDecimal("135.00"), 1, "1 x 95 mm²", "1 x 95 mm²", "1 x 110 mm", "1 x 100 mm", 200, "250 A", 200, 10, NORMA_TABELA_2),
            new ServiceEntranceDimensioning("380/220 V", new BigDecimal("165.00"), 1, "1 x 150 mm²", "1 x 150 mm²", "1 x 110 mm", "1 x 100 mm", 250, "250 A", 250, 10, NORMA_TABELA_2),
            new ServiceEntranceDimensioning("380/220 V", new BigDecimal("211.00"), 1, "1 x 240 mm²", "1 x 240 mm²", "1 x 110 mm", "1 x 100 mm", 320, "400 A", 320, 10, NORMA_TABELA_2),
            new ServiceEntranceDimensioning("380/220 V", new BigDecimal("263.00"), 2, "2 x (1 x 150 mm²)", "2 x (1 x 150 mm²)", "2 x 110 mm", "2 x 100 mm", 400, "400 A", 400, 10, NORMA_TABELA_2),
            new ServiceEntranceDimensioning("380/220 V", new BigDecimal("300.00"), 2, "2 x (1 x 185 mm²)", "2 x (1 x 185 mm²)", "2 x 110 mm", "2 x 100 mm", 450, "630 A", 450, 10, NORMA_TABELA_2)
    );

    private static final List<ServiceEntranceDimensioning> TABELA_1_220V = List.of(
            new ServiceEntranceDimensioning("220/127 V", new BigDecimal("26.00"), 1, "1 x 16 mm²", "1 x 16 mm²", "1 x 110 mm", "1 x 100 mm", 70, "160 A", 70, 10, NORMA_TABELA_1),
            new ServiceEntranceDimensioning("220/127 V", new BigDecimal("30.00"), 1, "1 x 25 mm²", "1 x 25 mm²", "1 x 110 mm", "1 x 100 mm", 80, "160 A", 80, 10, NORMA_TABELA_1),
            new ServiceEntranceDimensioning("220/127 V", new BigDecimal("38.00"), 1, "1 x 35 mm²", "1 x 35 mm²", "1 x 110 mm", "1 x 100 mm", 100, "160 A", 100, 10, NORMA_TABELA_1),
            new ServiceEntranceDimensioning("220/127 V", new BigDecimal("47.00"), 1, "1 x 50 mm²", "1 x 50 mm²", "1 x 110 mm", "1 x 100 mm", 125, "160 A", 125, 10, NORMA_TABELA_1),
            new ServiceEntranceDimensioning("220/127 V", new BigDecimal("57.00"), 1, "1 x 70 mm²", "1 x 70 mm²", "1 x 110 mm", "1 x 100 mm", 150, "160 A", 150, 10, NORMA_TABELA_1),
            new ServiceEntranceDimensioning("220/127 V", new BigDecimal("76.00"), 1, "1 x 95 mm²", "1 x 95 mm²", "1 x 110 mm", "1 x 100 mm", 200, "250 A", 200, 10, NORMA_TABELA_1),
            new ServiceEntranceDimensioning("220/127 V", new BigDecimal("95.00"), 1, "1 x 150 mm²", "1 x 150 mm²", "1 x 110 mm", "1 x 100 mm", 250, "250 A", 250, 10, NORMA_TABELA_1),
            new ServiceEntranceDimensioning("220/127 V", new BigDecimal("122.00"), 1, "1 x 240 mm²", "1 x 240 mm²", "1 x 110 mm", "1 x 100 mm", 320, "400 A", 320, 10, NORMA_TABELA_1),
            new ServiceEntranceDimensioning("220/127 V", new BigDecimal("152.00"), 2, "2 x (1 x 150 mm²)", "2 x (1 x 150 mm²)", "2 x 110 mm", "2 x 100 mm", 400, "400 A", 400, 10, NORMA_TABELA_1),
            new ServiceEntranceDimensioning("220/127 V", new BigDecimal("173.00"), 2, "2 x (1 x 185 mm²)", "2 x (1 x 185 mm²)", "2 x 110 mm", "2 x 100 mm", 450, "630 A", 450, 10, NORMA_TABELA_1)
    );

    private static final List<BusbarDimensioning> TABELA_3_BARRAMENTOS = List.of(
            new BusbarDimensioning(new BigDecimal("40.00"), "19 x 3 mm", "3/4\" x 1/8\"", 208, NORMA_TABELA_3),
            new BusbarDimensioning(new BigDecimal("74.00"), "25 x 3 mm", "1\" x 1/8\"", 250, NORMA_TABELA_3),
            new BusbarDimensioning(new BigDecimal("104.00"), "25 x 5 mm", "1\" x 3/16\"", 340, NORMA_TABELA_3),
            new BusbarDimensioning(new BigDecimal("207.00"), "30 x 5 mm", "1 1/4\" x 3/16\"", 449, NORMA_TABELA_3),
            new BusbarDimensioning(new BigDecimal("259.00"), "40 x 5 mm", "1 1/2\" x 3/16\"", 460, NORMA_TABELA_3),
            new BusbarDimensioning(new BigDecimal("346.00"), "50 x 5 mm", "2\" x 3/16\"", 595, NORMA_TABELA_3),
            new BusbarDimensioning(new BigDecimal("432.00"), "40 x 10 mm", "—", 994, NORMA_TABELA_3),
            new BusbarDimensioning(new BigDecimal("519.00"), "50 x 10 mm", "—", 1312, NORMA_TABELA_3),
            new BusbarDimensioning(new BigDecimal("605.00"), "60 x 10 mm", "—", 1640, NORMA_TABELA_3),
            new BusbarDimensioning(new BigDecimal("778.00"), "80 x 10 mm", "—", 1968, NORMA_TABELA_3),
            new BusbarDimensioning(new BigDecimal("951.00"), "100 x 10 mm", "—", 2625, NORMA_TABELA_3),
            new BusbarDimensioning(new BigDecimal("1211.00"), "120 x 10 mm", "—", 3281, NORMA_TABELA_3)
    );

    public ConsolidatedCalculationResult calculate(ConsolidatedCalculationInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Input de cálculo consolidado não pode ser nulo.");
        }

        BigDecimal drf = input.demandaResidencialDrf();
        BigDecimal ds = input.demandaServicosDs();
        BigDecimal dc = input.demandaComercialDc();
        BigDecimal dve = input.demandaVeDve();
        BigDecimal dmbi = input.demandaIncendioDmbi();
        VoltageSystem sistemaTensao = input.sistemaTensao();

        if (drf.compareTo(BigDecimal.ZERO) == 0 &&
                ds.compareTo(BigDecimal.ZERO) == 0 &&
                dc.compareTo(BigDecimal.ZERO) == 0 &&
                dve.compareTo(BigDecimal.ZERO) == 0 &&
                dmbi.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("A edificação deve possuir ao menos uma carga de demanda positiva.");
        }

        BigDecimal ded = drf.add(ds).add(dc).add(dve).add(dmbi).setScale(2, RoundingMode.HALF_UP);
        BigDecimal dqdg = ded.subtract(dmbi).setScale(2, RoundingMode.HALF_UP);

        SupplyVoltageLevel nivelFornecimento = (ded.compareTo(LIMITE_BAIXA_TENSAO) <= 0)
                ? SupplyVoltageLevel.BAIXA_TENSAO_SECUNDARIA
                : SupplyVoltageLevel.MEDIA_TENSAO_PRIMARIA;

        List<String> notasNormativas = new ArrayList<>();
        if (dmbi.compareTo(BigDecimal.ZERO) > 0) {
            notasNormativas.add("Conforme DIS-NOR-053 e NBR 5410, o circuito da motobomba de incêndio deriva a montante do disjuntor geral do QDG, não solicitando seus barramentos.");
        }
        if (nivelFornecimento == SupplyVoltageLevel.MEDIA_TENSAO_PRIMARIA) {
            notasNormativas.add("Demanda global superior a 300 kVA: Atendimento obrigatório em Média Tensão Primária (13,8 kV) com Subestação Abrigada Particular (DIS-NOR-053 item 6.1).");
        }

        // Corrente de Projeto: Ip = (D_QDG * 1000) / (sqrt(3) * V_linha)
        double sqrt3 = Math.sqrt(3.0);
        BigDecimal divisor = sistemaTensao.getLineVoltage().multiply(BigDecimal.valueOf(sqrt3));
        BigDecimal correnteProjetoIpA = dqdg.multiply(BigDecimal.valueOf(1000))
                .divide(divisor, 2, RoundingMode.HALF_UP);

        // Dimensionamento do padrão de entrada
        ServiceEntranceDimensioning padraoEntrada = dimensionarPadraoEntrada(ded, sistemaTensao);

        // Dimensionamento dos barramentos do QDG
        BusbarDimensioning barramentoQdg = dimensionarBarramentoQdg(dqdg);

        boolean disjuntorAdequado = padraoEntrada != null &&
                BigDecimal.valueOf(padraoEntrada.disjuntorCorrenteInA()).compareTo(correnteProjetoIpA) >= 0;

        boolean barramentoAdequado = barramentoQdg != null &&
                BigDecimal.valueOf(barramentoQdg.correnteNominalA()).compareTo(correnteProjetoIpA) >= 0;

        return new ConsolidatedCalculationResult(
                ded,
                dqdg,
                dmbi,
                correnteProjetoIpA,
                sistemaTensao,
                nivelFornecimento,
                padraoEntrada,
                barramentoQdg,
                disjuntorAdequado,
                barramentoAdequado,
                notasNormativas
        );
    }

    private ServiceEntranceDimensioning dimensionarPadraoEntrada(BigDecimal ded, VoltageSystem sistemaTensao) {
        List<ServiceEntranceDimensioning> tabela = (sistemaTensao == VoltageSystem.TRIFASICO_220_127V)
                ? TABELA_1_220V
                : TABELA_2_380V;

        for (ServiceEntranceDimensioning row : tabela) {
            if (ded.compareTo(row.limiteDemandaKva()) <= 0) {
                return row;
            }
        }
        return null;
    }

    private BusbarDimensioning dimensionarBarramentoQdg(BigDecimal dqdg) {
        for (BusbarDimensioning row : TABELA_3_BARRAMENTOS) {
            if (dqdg.compareTo(row.limiteDemandaKva()) <= 0) {
                return row;
            }
        }
        return null;
    }
}
