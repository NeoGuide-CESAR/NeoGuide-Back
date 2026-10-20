package neoguide.project.validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

import neoguide.project.dto.validation.NormativeMessageType;
import neoguide.project.dto.validation.NormativeTechnicalDetails;
import neoguide.project.dto.validation.NormativeValidationMessage;
import neoguide.project.dto.validation.NormativeValidationResponse;
import neoguide.project.model.DemandResults;
import neoguide.project.model.Project;

@Component
public class NormativeRuleValidator {

    private static final BigDecimal LIMIAR_MUC_KVA = new BigDecimal("50.00");
    private static final BigDecimal LIMIAR_BT_MAX_KVA = new BigDecimal("300.00");
    private static final BigDecimal TETO_VE_PERCENTUAL = new BigDecimal("0.50"); // 50%
    private static final BigDecimal ALERTA_VE_PERCENTUAL = new BigDecimal("0.40"); // 40%
    private static final BigDecimal SQRT_3 = new BigDecimal("1.732050807568877");

    private static final List<BigDecimal> TRAFOS_PADRAO = Arrays.asList(
            new BigDecimal("45.0"), new BigDecimal("75.0"), new BigDecimal("112.5"),
            new BigDecimal("150.0"), new BigDecimal("225.0"), new BigDecimal("300.0"),
            new BigDecimal("500.0"), new BigDecimal("750.0"), new BigDecimal("1000.0")
    );

    public NormativeValidationResponse validate(Project project, DemandResults results) {
        List<NormativeValidationMessage> mensagens = new ArrayList<>();

        BigDecimal ded = results.getDemandaGlobalKva() != null ? results.getDemandaGlobalKva() : BigDecimal.ZERO;
        BigDecimal dqdg = results.getDemandaQdgKva() != null ? results.getDemandaQdgKva() : BigDecimal.ZERO;
        BigDecimal ds = results.getDemandaServicoKva() != null ? results.getDemandaServicoKva() : BigDecimal.ZERO;
        BigDecimal dve = results.getDemandaVeKva() != null ? results.getDemandaVeKva() : BigDecimal.ZERO;
        Integer disjuntorIn = results.getDisjuntorGeralA();
        Integer capBarramento = results.getCapacidadeBarramentoA();

        // 1. Determinação de Tensão e Corrente de Projeto Ip
        BigDecimal tensaoLinha = resolverTensaoLinha(project.getTensaoFornecimento());
        BigDecimal denominador = SQRT_3.multiply(tensaoLinha);
        BigDecimal ip = BigDecimal.ZERO;
        if (denominador.compareTo(BigDecimal.ZERO) > 0) {
            ip = dqdg.multiply(new BigDecimal("1000"))
                    .divide(denominador, 2, RoundingMode.HALF_UP);
        }

        // 2. Determinação de Strafo
        BigDecimal strafo = resolverPotenciaTrafo(ded);

        // --- REGRA 1: Limiar de 50 kVA (MUC) ---
        auditarLimiar50kVa(ded, mensagens);

        // --- REGRA 2: Limiar de 300 kVA (BT x MT) ---
        String nivelTensao = auditarLimiar300kVa(ded, mensagens);

        // --- REGRA 3: Teto de Recarga Veicular (Item 6.26.2) ---
        BigDecimal percentualVeServico = auditarTetoVeiculosEletricos(ds, dve, strafo, mensagens);

        // --- REGRA 4: Ampacidade e Proteção Geral ---
        auditarAmpacidadeEProtecao(ip, disjuntorIn, capBarramento, mensagens);

        // Concessão do Selo
        int totalErros = (int) mensagens.stream()
                .filter(m -> m.tipo() == NormativeMessageType.ERRO_NORMATIVO)
                .count();
        int totalAlertas = (int) mensagens.stream()
                .filter(m -> m.tipo() == NormativeMessageType.ALERTA)
                .count();
        int totalInfos = (int) mensagens.stream()
                .filter(m -> m.tipo() == NormativeMessageType.INFO)
                .count();

        boolean conforme = (totalErros == 0);
        String selo = conforme ? "CONFORME_DIS_NOR_053" : "PENDENTE_AJUSTES";

        NormativeTechnicalDetails detalhes = new NormativeTechnicalDetails(
                ded,
                dqdg,
                ip,
                disjuntorIn,
                capBarramento,
                strafo,
                percentualVeServico,
                nivelTensao
        );

        return new NormativeValidationResponse(
                project.getId(),
                project.getProtocolo(),
                conforme,
                selo,
                totalErros,
                totalAlertas,
                totalInfos,
                LocalDateTime.now(),
                mensagens,
                detalhes
        );
    }

    private void auditarLimiar50kVa(BigDecimal ded, List<NormativeValidationMessage> mensagens) {
        if (ded.compareTo(LIMIAR_MUC_KVA) > 0) {
            mensagens.add(new NormativeValidationMessage(
                    NormativeMessageType.INFO,
                    "DIS-NOR-053 Item 4.1 / RN ANEEL 1.000",
                    "Demanda global de " + ded + " kVA enquadra a edificação na obrigatoriedade plena de MUC, exigindo Memorial Descritivo de Cálculo e Anotação de Responsabilidade Técnica (ART/TRT).",
                    "Ded > 50 kVA"
            ));
        } else {
            mensagens.add(new NormativeValidationMessage(
                    NormativeMessageType.INFO,
                    "DIS-NOR-053 Item 4.1 / RN ANEEL 1.000",
                    "Demanda global de " + ded + " kVA qualifica o empreendimento para procedimento simplificado de ligação coletiva ou agrupamento de medição (Ded <= 50 kVA).",
                    "Ded <= 50 kVA"
            ));
        }
    }

    private String auditarLimiar300kVa(BigDecimal ded, List<NormativeValidationMessage> mensagens) {
        if (ded.compareTo(LIMIAR_BT_MAX_KVA) <= 0) {
            mensagens.add(new NormativeValidationMessage(
                    NormativeMessageType.INFO,
                    "DIS-NOR-053 Item 6.1",
                    "Atendimento aprovado em Baixa Tensão Secundária de distribuição (380/220V ou 220/127V) para demanda global de " + ded + " kVA (limite normativo de 300 kVA).",
                    "Ded <= 300 kVA"
            ));
            return "BAIXA_TENSAO";
        } else {
            mensagens.add(new NormativeValidationMessage(
                    NormativeMessageType.ERRO_NORMATIVO,
                    "DIS-NOR-053 Item 6.2",
                    "Impedimento técnico para fornecimento em Baixa Tensão: a demanda global calculada de " + ded + " kVA excede o limite máximo de 300 kVA da rede secundária. É compulsório o atendimento em Média Tensão Primária (13,8 kV) via Subestação Abrigada própria da edificação.",
                    "Ded > 300 kVA"
            ));
            return "MEDIA_TENSAO_OBRIGATORIA";
        }
    }

    private BigDecimal auditarTetoVeiculosEletricos(BigDecimal ds, BigDecimal dve, BigDecimal strafo,
                                                   List<NormativeValidationMessage> mensagens) {
        if (dve == null || dve.compareTo(BigDecimal.ZERO) == 0) {
            mensagens.add(new NormativeValidationMessage(
                    NormativeMessageType.INFO,
                    "DIS-NOR-053 Item 6.26",
                    "Empreendimento sem infraestrutura de recarga veicular cadastrada.",
                    "Dve = 0 kVA"
            ));
            return BigDecimal.ZERO;
        }

        BigDecimal somaServicoVe = ds.add(dve);
        BigDecimal limiteMaximo = strafo.multiply(TETO_VE_PERCENTUAL);
        BigDecimal percentualOcupacao = BigDecimal.ZERO;
        if (strafo.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal fracaoOcupacao = somaServicoVe.divide(strafo, 4, RoundingMode.HALF_UP);
            percentualOcupacao = fracaoOcupacao.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
        }

        if (somaServicoVe.compareTo(limiteMaximo) > 0) {
            mensagens.add(new NormativeValidationMessage(
                    NormativeMessageType.ERRO_NORMATIVO,
                    "DIS-NOR-053 Item 6.26.2",
                    "Violação de segurança técnica: a soma das demandas de serviços e recarga veicular (Ds + Dve = " + somaServicoVe + " kVA) atinge " + percentualOcupacao + "% da capacidade do transformador de referência (" + strafo + " kVA), superando o teto regulamentar de 50%. Necessário redimensionar subestação ou implementar sistema de gestão de recarga dinâmica.",
                    "Ds + Dve > 50% Strafo"
            ));
        } else if (somaServicoVe.compareTo(strafo.multiply(ALERTA_VE_PERCENTUAL)) > 0) {
            mensagens.add(new NormativeValidationMessage(
                    NormativeMessageType.ALERTA,
                    "DIS-NOR-053 Item 6.26.2",
                    "Atenção de sobrecarga térmica: a soma das demandas de serviços e recarga veicular (Ds + Dve = " + somaServicoVe + " kVA) ocupa " + percentualOcupacao + "% da capacidade do transformador (" + strafo + " kVA), operando próximo ao teto crítico regulamentar de 50%.",
                    "40% Strafo < Ds + Dve <= 50% Strafo"
            ));
        } else {
            mensagens.add(new NormativeValidationMessage(
                    NormativeMessageType.INFO,
                    "DIS-NOR-053 Item 6.26.2",
                    "Infraestrutura de recarga veicular em plena conformidade normativa: Ds + Dve (" + somaServicoVe + " kVA) representa " + percentualOcupacao + "% da capacidade do transformador (" + strafo + " kVA), respeitando a margem máxima de 50%.",
                    "Ds + Dve <= 40% Strafo"
            ));
        }

        return percentualOcupacao;
    }

    private void auditarAmpacidadeEProtecao(BigDecimal ip, Integer disjuntorIn, Integer capBarramento,
                                           List<NormativeValidationMessage> mensagens) {
        if (disjuntorIn != null) {
            BigDecimal inVal = new BigDecimal(disjuntorIn);
            if (ip.compareTo(inVal) > 0) {
                mensagens.add(new NormativeValidationMessage(
                        NormativeMessageType.ERRO_NORMATIVO,
                        "DIS-NOR-053 Anexo II / NBR 5410",
                        "Incompatibilidade de proteção geral: a corrente de projeto calculada (Ip = " + ip + " A) é superior à corrente nominal do disjuntor geral selecionado (In = " + disjuntorIn + " A), acarretando risco de disparos intempestivos e sobreaquecimento dos condutores.",
                        "Ip > In"
                ));
            } else {
                mensagens.add(new NormativeValidationMessage(
                        NormativeMessageType.INFO,
                        "DIS-NOR-053 Anexo II",
                        "Coordenação de proteção aprovada: disjuntor geral de " + disjuntorIn + " A protege adequadamente a corrente de projeto calculada de " + ip + " A.",
                        "Ip <= In"
                ));
            }
        }

        if (capBarramento != null) {
            BigDecimal capVal = new BigDecimal(capBarramento);
            if (ip.compareTo(capVal) > 0) {
                mensagens.add(new NormativeValidationMessage(
                        NormativeMessageType.ERRO_NORMATIVO,
                        "DIS-NOR-053 Tabela 3",
                        "Subdimensionamento de barramento: a capacidade nominal do barramento de cobre do QDG (" + capBarramento + " A) é inferior à corrente de projeto requerida (Ip = " + ip + " A).",
                        "Ip > Ibarramento"
                ));
            } else {
                mensagens.add(new NormativeValidationMessage(
                        NormativeMessageType.INFO,
                        "DIS-NOR-053 Tabela 3",
                        "Barramento de cobre do QDG aprovado com capacidade nominal de " + capBarramento + " A para solicitação de " + ip + " A.",
                        "Ip <= Ibarramento"
                ));
            }
        }
    }

    private BigDecimal resolverTensaoLinha(String tensaoFornecimento) {
        if (tensaoFornecimento != null && tensaoFornecimento.contains("220") && !tensaoFornecimento.contains("380")) {
            return new BigDecimal("220.0");
        }
        return new BigDecimal("380.0");
    }

    private BigDecimal resolverPotenciaTrafo(BigDecimal ded) {
        for (BigDecimal trafo : TRAFOS_PADRAO) {
            if (trafo.compareTo(ded) >= 0) {
                return trafo;
            }
        }
        return ded;
    }
}
