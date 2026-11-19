package neoguide.project.engine.normativo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tabelas normativas usadas pelo motor de cálculo e pelos seeds do banco.
 *
 * <p>Fonte de valor: DIS-NOR-053 Rev. 06 (Quadros 33, 35 e 36) e DIS-NOR-030 Rev. 07 (Tabela 19),
 * copiadas de {@code NeoGuide-NeoChat/docs/info/DIS-NOR-053-REV06.md} e {@code DIS-NOR-030-REV07.md}.
 * Esta é a única fonte de valor: motor e seeders leem daqui. Não alterar um valor sem citar o quadro.</p>
 */
public final class TabelasNormativas {

    public static final String NORMA_QUADRO_33 = "DIS-NOR-053 Rev. 06 Quadro 33";
    public static final String NORMA_QUADRO_35 = "DIS-NOR-053 Rev. 06 Quadro 35";
    public static final String NORMA_QUADRO_36 = "DIS-NOR-053 Rev. 06 Quadro 36";
    public static final String NORMA_TABELA_19 = "DIS-NOR-030 Rev. 07 Tabela 19";

    private TabelasNormativas() {
    }

    // =====================================================================
    // Quadro 35 — demanda unitária (Di) por área útil privativa (m²)
    // Faixas com limite superior inclusivo: a área cai na primeira faixa cujo máximo é >= área.
    // =====================================================================

    /** Uma faixa do Quadro 35. {@code minM2} é exclusivo (0 para a primeira faixa). */
    public record FaixaArea(String codigo, String rotulo, BigDecimal minM2, BigDecimal maxM2, BigDecimal demandaKva) {
    }

    /** Limite superior do Quadro 35 (m²). Áreas acima disso não são tabeladas. */
    public static final BigDecimal AREA_MAXIMA_QUADRO_35 = new BigDecimal("1000");

    public static final List<FaixaArea> QUADRO_35 = List.of(
            faixa("FAIXA_ATE_40", "Até 40 m²", "0.00", "40", "1.00"),
            faixa("FAIXA_41_45", "41 a 45 m²", "40.01", "45", "1.05"),
            faixa("FAIXA_46_50", "46 a 50 m²", "45.01", "50", "1.16"),
            faixa("FAIXA_51_55", "51 a 55 m²", "50.01", "55", "1.26"),
            faixa("FAIXA_56_60", "56 a 60 m²", "55.01", "60", "1.36"),
            faixa("FAIXA_61_65", "61 a 65 m²", "60.01", "65", "1.47"),
            faixa("FAIXA_66_70", "66 a 70 m²", "65.01", "70", "1.57"),
            faixa("FAIXA_71_75", "71 a 75 m²", "70.01", "75", "1.67"),
            faixa("FAIXA_76_80", "76 a 80 m²", "75.01", "80", "1.76"),
            faixa("FAIXA_81_85", "81 a 85 m²", "80.01", "85", "1.86"),
            faixa("FAIXA_86_90", "86 a 90 m²", "85.01", "90", "1.96"),
            faixa("FAIXA_91_95", "91 a 95 m²", "90.01", "95", "2.06"),
            faixa("FAIXA_96_100", "96 a 100 m²", "95.01", "100", "2.16"),
            faixa("FAIXA_101_110", "101 a 110 m²", "100.01", "110", "2.35"),
            faixa("FAIXA_111_120", "111 a 120 m²", "110.01", "120", "2.54"),
            faixa("FAIXA_121_130", "121 a 130 m²", "120.01", "130", "2.73"),
            faixa("FAIXA_131_140", "131 a 140 m²", "130.01", "140", "2.91"),
            faixa("FAIXA_141_150", "141 a 150 m²", "140.01", "150", "3.06"),
            faixa("FAIXA_151_160", "151 a 160 m²", "150.01", "160", "3.28"),
            faixa("FAIXA_161_170", "161 a 170 m²", "160.01", "170", "3.47"),
            faixa("FAIXA_171_180", "171 a 180 m²", "170.01", "180", "3.65"),
            faixa("FAIXA_181_190", "181 a 190 m²", "180.01", "190", "3.83"),
            faixa("FAIXA_191_200", "191 a 200 m²", "190.01", "200", "4.01"),
            faixa("FAIXA_201_220", "201 a 220 m²", "200.01", "220", "4.36"),
            faixa("FAIXA_221_240", "221 a 240 m²", "220.01", "240", "4.72"),
            faixa("FAIXA_241_260", "241 a 260 m²", "240.01", "260", "5.07"),
            faixa("FAIXA_261_280", "261 a 280 m²", "260.01", "280", "5.42"),
            faixa("FAIXA_281_300", "281 a 300 m²", "280.01", "300", "5.76"),
            faixa("FAIXA_301_350", "301 a 350 m²", "300.01", "350", "6.61"),
            faixa("FAIXA_351_400", "351 a 400 m²", "350.01", "400", "7.45"),
            faixa("FAIXA_401_450", "401 a 450 m²", "400.01", "450", "8.28"),
            faixa("FAIXA_451_500", "451 a 500 m²", "450.01", "500", "9.10"),
            faixa("FAIXA_501_550", "501 a 550 m²", "500.01", "550", "9.91"),
            faixa("FAIXA_551_600", "551 a 600 m²", "550.01", "600", "10.71"),
            faixa("FAIXA_601_650", "601 a 650 m²", "600.01", "650", "11.51"),
            faixa("FAIXA_651_700", "651 a 700 m²", "650.01", "700", "12.30"),
            faixa("FAIXA_701_800", "701 a 800 m²", "700.01", "800", "13.86"),
            faixa("FAIXA_801_900", "801 a 900 m²", "800.01", "900", "15.40"),
            faixa("FAIXA_901_1000", "901 a 1000 m²", "900.01", "1000", "16.93"));

    /** Retorna a faixa do Quadro 35 para a área útil informada (m²). */
    public static FaixaArea faixaArea(BigDecimal areaM2) {
        if (areaM2 == null || areaM2.signum() <= 0) {
            throw new IllegalArgumentException("A área útil privativa deve ser maior que zero.");
        }
        if (areaM2.compareTo(AREA_MAXIMA_QUADRO_35) > 0) {
            throw new IllegalArgumentException("Área útil acima de 1000 m² não é tabelada no Quadro 35.");
        }
        for (FaixaArea f : QUADRO_35) {
            if (areaM2.compareTo(f.maxM2()) <= 0) {
                return f;
            }
        }
        throw new IllegalStateException("Quadro 35 incompleto para a área " + areaM2);
    }

    private static FaixaArea faixa(String codigo, String rotulo, String min, String max, String di) {
        return new FaixaArea(codigo, rotulo, new BigDecimal(min), new BigDecimal(max), new BigDecimal(di));
    }

    // =====================================================================
    // Quadro 36 — fator de coincidência (Fc) por número de apartamentos
    // Tabela discreta. Para N >= 60, usa-se o valor "60 ou +".
    // =====================================================================

    /** Índice = número de apartamentos (1..59). Posição 0 não usada. */
    private static final String[] QUADRO_36_PERCENTUAL = {
            null,
            "100.00", "98.00", "97.30", "97.00", "96.80", "96.60", "96.57", "96.50", "96.45", "96.40",
            "94.73", "93.33", "92.15", "91.14", "90.27", "89.50", "88.82", "88.22", "87.68", "87.20",
            "85.90", "84.77", "83.70", "82.75", "81.84", "81.00", "80.26", "79.54", "78.90", "78.27",
            "77.68", "77.16", "76.64", "76.18", "75.71", "75.28", "74.89", "74.45", "74.11", "73.80",
            "73.46", "73.17", "72.89", "72.60", "72.31", "71.96", "71.62", "71.29", "70.98", "70.68",
            "70.39", "70.17", "69.85", "69.60", "69.35", "69.11", "68.88", "68.66", "68.44"
    };

    /** Valor para 60 apartamentos ou mais. */
    public static final BigDecimal FC_60_OU_MAIS = new BigDecimal("0.6823");

    /** Fator de coincidência do Quadro 36 para N apartamentos (sem interpolação). */
    public static BigDecimal fatorCoincidencia(int totalApartamentos) {
        if (totalApartamentos <= 0) {
            throw new IllegalArgumentException("Total de apartamentos deve ser maior que zero.");
        }
        if (totalApartamentos >= 60) {
            return FC_60_OU_MAIS;
        }
        return new BigDecimal(QUADRO_36_PERCENTUAL[totalApartamentos]).movePointLeft(2).setScale(4);
    }

    /** Linhas do Quadro 36 como (N, Fc) para o seed. N = 60 representa "60 ou +". */
    public static List<FatorCoincidenciaLinha> linhasQuadro36() {
        List<FatorCoincidenciaLinha> linhas = new java.util.ArrayList<>();
        for (int n = 1; n <= 59; n++) {
            linhas.add(new FatorCoincidenciaLinha(n, fatorCoincidencia(n)));
        }
        linhas.add(new FatorCoincidenciaLinha(60, FC_60_OU_MAIS));
        return linhas;
    }

    public record FatorCoincidenciaLinha(int totalApartamentos, BigDecimal fator) {
    }

    // =====================================================================
    // Quadro 33 — fator de demanda de estações de recarga de VE (agrupamento)
    // =====================================================================

    /** Uma linha do Quadro 33. {@code maxVagas} é o limite superior inclusivo da faixa. */
    public record FatorVe(int maxVagas, BigDecimal fator, String descricao) {
    }

    public static final List<FatorVe> QUADRO_33 = List.of(
            new FatorVe(10, new BigDecimal("1.00"), "1 a 10"),
            new FatorVe(20, new BigDecimal("0.86"), "11 a 20"),
            new FatorVe(30, new BigDecimal("0.80"), "21 a 30"),
            new FatorVe(40, new BigDecimal("0.78"), "31 a 40"),
            new FatorVe(50, new BigDecimal("0.75"), "41 a 50"),
            new FatorVe(75, new BigDecimal("0.70"), "51 a 75"),
            new FatorVe(100, new BigDecimal("0.65"), "76 a 100"),
            new FatorVe(9999, new BigDecimal("0.60"), "Acima de 100"));

    /** Fator de simultaneidade do Quadro 33 para {@code pontos} estações. */
    public static BigDecimal fatorVe(int pontos) {
        if (pontos <= 0) {
            throw new IllegalArgumentException("Número de pontos de recarga deve ser maior que zero.");
        }
        for (FatorVe f : QUADRO_33) {
            if (pontos <= f.maxVagas()) {
                return f.fator();
            }
        }
        throw new IllegalStateException("Quadro 33 incompleto para " + pontos + " pontos");
    }

    // =====================================================================
    // Tabela 19 — características dos motores trifásicos (DIS-NOR-030 Rev. 07)
    // kVA = potência aparente (coluna VA / 1000). Cos φ e corrente a 380 V para o seed.
    // =====================================================================

    /** Uma linha da Tabela 19: potência nominal (cv), demanda aparente (kVA), cos φ médio e corrente nominal 380 V (A). */
    public record MotorTabela19(BigDecimal cv, BigDecimal kva, BigDecimal cosPhi, BigDecimal correnteNominal380) {
    }

    public static final List<MotorTabela19> TABELA_19 = List.of(
            linha19("0.33", "0.65", "0.61", "0.9"),
            linha19("0.50", "0.87", "0.66", "1.3"),
            linha19("0.75", "1.26", "0.66", "1.9"),
            linha19("1.00", "1.52", "0.69", "2.3"),
            linha19("1.50", "2.17", "0.71", "3.3"),
            linha19("2.00", "2.70", "0.72", "4.1"),
            linha19("3.00", "4.04", "0.73", "6.1"),
            linha19("4.00", "5.03", "0.74", "7.6"),
            linha19("5.00", "6.02", "0.75", "9.1"),
            linha19("7.50", "8.65", "0.76", "12.7"),
            linha19("10.00", "11.54", "0.77", "17.5"),
            linha19("12.50", "14.09", "0.77", "21.3"),
            linha19("15.00", "16.65", "0.77", "25.2"),
            linha19("20.00", "22.10", "0.77", "33.5"),
            linha19("25.00", "25.83", "0.81", "39.1"),
            linha19("30.00", "30.52", "0.82", "46.2"),
            linha19("40.00", "39.74", "0.84", "60.2"),
            linha19("50.00", "48.73", "0.84", "73.8"),
            linha19("60.00", "58.15", "0.85", "88.1"),
            linha19("75.00", "72.28", "0.85", "109.5"),
            linha19("100.00", "95.56", "0.85", "144.8"),
            linha19("125.00", "117.05", "0.85", "177.3"),
            linha19("150.00", "141.29", "0.85", "214.0"),
            linha19("200.00", "190.18", "0.85", "288.1"));

    private static MotorTabela19 linha19(String cv, String kva, String cosPhi, String corrente) {
        return new MotorTabela19(new BigDecimal(cv), new BigDecimal(kva), new BigDecimal(cosPhi), new BigDecimal(corrente));
    }

    /**
     * Demanda aparente (kVA) de um motor pela Tabela 19.
     * Potência exatamente tabelada: valor da norma. Entre linhas: a linha imediatamente acima (critério conservador).
     * Acima de 200 cv: extrapolação proporcional à última linha, como no motor anterior.
     */
    public static BigDecimal kvaTabela19(BigDecimal cv) {
        if (cv == null || cv.signum() <= 0) {
            throw new IllegalArgumentException("Potência do motor em CV deve ser maior que zero.");
        }
        BigDecimal alvo = cv.setScale(2, java.math.RoundingMode.HALF_UP);
        for (MotorTabela19 l : TABELA_19) {
            if (alvo.compareTo(l.cv()) <= 0) {
                return l.kva();
            }
        }
        MotorTabela19 ultima = TABELA_19.get(TABELA_19.size() - 1);
        return alvo.multiply(ultima.kva()).divide(ultima.cv(), 4, java.math.RoundingMode.HALF_UP);
    }
}
