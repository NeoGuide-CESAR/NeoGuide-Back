package neoguide.project.service;

import neoguide.project.dto.EspacoComercialRequest;
import neoguide.project.dto.calculo.ItemValidacaoResponse;
import neoguide.project.engine.model.CommercialCalculationResult;
import neoguide.project.engine.model.CommercialUnitResult;
import neoguide.project.exception.ValidationException;
import neoguide.project.model.CommercialUnit;
import neoguide.project.model.CommonAreaLoad;
import neoguide.project.model.Project;
import neoguide.project.model.ResidentialTypology;
import neoguide.project.model.SpecialLoads;
import neoguide.project.model.TipoEdificacao;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Catálogo da validação normativa da Revisão. Olha só o que está gravado no banco e só os campos que pertencem à
 * etapa e à opção marcada. Quantidade 0 ou nula significa "não existe" (nem erro, nem alerta), salvo onde a regra diz.
 *
 * <p>ERRO bloqueia o cálculo; ALERTA é ponto de atenção e não bloqueia. Mensagens sobre o resultado (até 50 kVA,
 * média tensão, Ip) não são geradas aqui: dependem do cálculo e ficam na tela de Resultado.
 */
@Component
public class RegrasValidacaoNormativa {

    public static final String ETAPA_PROJETO = "projeto";
    public static final String ETAPA_RESIDENCIAL = "residencial";
    public static final String ETAPA_AREA_COMUM = "area-comum";
    public static final String ETAPA_COMERCIAL = "comercial";
    public static final String ETAPA_ESPECIAIS = "cargas-especiais";

    /** Nomes de campo usados nos itens: os mesmos dos DTOs de cada etapa (conferidos por teste). */
    public static final class Campos {
        public static final String TITULO = "titulo";
        public static final String TIPO_EDIFICACAO = "tipoEdificacao";
        public static final String CEP = "cep";
        public static final String ENDERECO = "endereco";
        public static final String TIPOLOGIAS = "tipologias";
        public static final String AREA_UTIL = "areaUtilM2";
        public static final String UNIDADES = "quantidadeUnidades";
        public static final String POTENCIA_AREA_COMUM = "potenciaNominalKw";
        public static final String OCUPACAO = "ocupacaoCodigo";
        public static final String AREA_ESPACO = "areaM2";
        public static final String CARGA_ESPACO = "cargaInstaladaKw";
        public static final String ESPACOS = "espacos";
        public static final String PONTOS_VE = "qtdPontosVe";
        public static final String POTENCIA_VE = "potenciaVeKw";
        public static final String POTENCIA_BOMBA = "potenciaBombaIncendioCv";
        public static final String MEDIDOR = "medidorExclusivo";

        private Campos() {
        }
    }

    static final int UNIDADES_MIN = 1;
    static final int UNIDADES_MAX = 1000;
    static final BigDecimal AREA_MIN = new BigDecimal("10");
    static final BigDecimal AREA_MAX = new BigDecimal("1000");
    static final int PONTOS_VE_MIN = 1;
    static final int PONTOS_VE_MAX = 500;
    static final BigDecimal POTENCIA_VE_MIN = new BigDecimal("3.70");
    static final BigDecimal POTENCIA_VE_MAX = new BigDecimal("22.0");
    static final BigDecimal BOMBA_CV_MIN = new BigDecimal("1.0");
    static final BigDecimal BOMBA_CV_MAX = new BigDecimal("50.0");
    static final int SMART_STUDIO_LIMITE_UNIDADES = 15;

    private final CommercialStageService commercialStageService;

    public RegrasValidacaoNormativa(CommercialStageService commercialStageService) {
        this.commercialStageService = commercialStageService;
    }

    public List<ItemValidacaoResponse> avaliar(Project project, List<ResidentialTypology> tipologias,
                                               List<CommonAreaLoad> cargas, List<CommercialUnit> espacos,
                                               SpecialLoads especiais) {
        List<ItemValidacaoResponse> itens = new ArrayList<>();
        TipoEdificacao tipo = project.getTipoEdificacao();

        projeto(project, itens);
        residencial(tipo, tipologias, itens);
        areaComum(cargas, itens);
        comercial(tipo, cargas, espacos, itens);
        especiais(project, especiais, itens);
        return itens;
    }

    // ------------------------------------------------------------------ etapa 1

    private void projeto(Project p, List<ItemValidacaoResponse> itens) {
        if (vazio(p.getTitulo())) {
            itens.add(erro("PROJETO_CAMPO_OBRIGATORIO", "Informe o nome do projeto.", Campos.TITULO, ETAPA_PROJETO));
        }
        if (p.getTipoEdificacao() == null) {
            itens.add(erro("PROJETO_CAMPO_OBRIGATORIO", "Informe o tipo de edificação.", Campos.TIPO_EDIFICACAO, ETAPA_PROJETO));
        }
        if (vazio(p.getCep())) {
            itens.add(erro("PROJETO_CAMPO_OBRIGATORIO", "Informe o CEP.", Campos.CEP, ETAPA_PROJETO));
        }
        if (vazio(p.getEndereco())) {
            itens.add(erro("PROJETO_CAMPO_OBRIGATORIO", "Informe o logradouro.", Campos.ENDERECO, ETAPA_PROJETO));
        }
    }

    // ------------------------------------------------------------------ etapa 2

    private void residencial(TipoEdificacao tipo, List<ResidentialTypology> tipologias, List<ItemValidacaoResponse> itens) {
        boolean aplica = tipo == TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR || tipo == TipoEdificacao.MISTO;
        if (!aplica) {
            return;
        }
        if (tipologias.isEmpty()) {
            itens.add(erro("RESIDENCIAL_SEM_GRUPOS", "Informe ao menos um grupo de unidades residenciais.", Campos.TIPOLOGIAS, ETAPA_RESIDENCIAL));
            return;
        }
        int totalUnidades = 0;
        Integer primeiroSmart = null;
        for (int i = 0; i < tipologias.size(); i++) {
            ResidentialTypology t = tipologias.get(i);
            Integer qtd = t.getQuantidadeUnidades();
            if (qtd == null || qtd < UNIDADES_MIN || qtd > UNIDADES_MAX) {
                itens.add(item("RESIDENCIAL_GRUPO_INCOMPLETO", "ERRO", "Grupo " + (i + 1) + ": informe de " + UNIDADES_MIN
                        + " a " + UNIDADES_MAX + " unidades.", Campos.UNIDADES, ETAPA_RESIDENCIAL, null, i));
            } else {
                totalUnidades += qtd;
            }
            BigDecimal area = t.getAreaUtilM2();
            if (area == null || area.compareTo(AREA_MIN) < 0 || area.compareTo(AREA_MAX) > 0) {
                itens.add(item("RESIDENCIAL_GRUPO_INCOMPLETO", "ERRO", "Grupo " + (i + 1) + ": informe a área de 10 a 1.000 m².",
                        Campos.AREA_UTIL, ETAPA_RESIDENCIAL, null, i));
            }
            if (t.isSmartStudio() && primeiroSmart == null) {
                primeiroSmart = i;
            }
        }
        if (primeiroSmart != null && totalUnidades > SMART_STUDIO_LIMITE_UNIDADES) {
            itens.add(item("SMART_STUDIO_FC_90", "ALERTA", "Smart studio com mais de " + SMART_STUDIO_LIMITE_UNIDADES
                    + " unidades: o fator de coincidência será de 90%.", Campos.UNIDADES, ETAPA_RESIDENCIAL, null, primeiroSmart));
        }
    }

    // ------------------------------------------------------------------ etapa 3

    private void areaComum(List<CommonAreaLoad> cargas, List<ItemValidacaoResponse> itens) {
        for (int i = 0; i < cargas.size(); i++) {
            CommonAreaLoad c = cargas.get(i);
            boolean doCatalogo = c.getCatalogoMotorId() != null || c.getCatalogoClimaId() != null || c.getCatalogoEquipamentoId() != null;
            boolean temPotencia = c.getPotenciaNominalKw() != null && c.getPotenciaNominalKw().signum() > 0;
            if (quantidade(c.getQuantidade()) > 0 && !doCatalogo && !temPotencia) {
                String nome = vazio(c.getDescricao()) ? (vazio(c.getTipoEquipamento()) ? "Item " + (i + 1) : c.getTipoEquipamento()) : c.getDescricao();
                itens.add(item("AREA_COMUM_POTENCIA_AUSENTE", "ERRO", nome + ": informe a potência.",
                        Campos.POTENCIA_AREA_COMUM, ETAPA_AREA_COMUM, aba(c.getSubmodulo()), i));
            }
        }
        // Só avalia quando a etapa tem cargas gravadas: sem etapa, não há como saber o que foi preenchido.
        if (!cargas.isEmpty()) {
            boolean temCoccao = cargas.stream().anyMatch(c -> "COCCAO".equalsIgnoreCase(c.getSubmodulo()) && quantidade(c.getQuantidade()) > 0);
            if (!temCoccao) {
                itens.add(item("ABA_FORNOS_VAZIA", "ALERTA",
                        "Aba Fornos e fogões sem nenhum valor. Se a área comum não tem fornos ou fogões, pode seguir.",
                        null, ETAPA_AREA_COMUM, "fornos", null));
            }
        }
    }

    static String aba(String submodulo) {
        if (submodulo == null) return null;
        return switch (submodulo.toUpperCase()) {
            case "ILUMINACAO_TUG" -> "tomadas";
            case "AQUECIMENTO", "CLIMATIZACAO" -> "aquecimento";
            case "COCCAO" -> "fornos";
            case "MOTORES" -> "motores";
            default -> null;
        };
    }

    // ------------------------------------------------------------------ etapa 4

    private void comercial(TipoEdificacao tipo, List<CommonAreaLoad> cargas, List<CommercialUnit> espacos, List<ItemValidacaoResponse> itens) {
        if (tipo == TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR && !espacos.isEmpty()) {
            itens.add(new ItemValidacaoResponse("TIPO_ENQUADRAMENTO_MISTO", "ALERTA",
                    "Este projeto tem espaços comerciais. Para o enquadramento, ele é tratado como edificação mista "
                            + "(limite de baixa tensão de 225 kVA).",
                    Campos.ESPACOS, ETAPA_COMERCIAL, null, null, "Revisar espaços",
                    new ItemValidacaoResponse.Alternativa("Alterar tipo", ETAPA_PROJETO, Campos.TIPO_EDIFICACAO)));
        }
        if (tipo == TipoEdificacao.COMERCIAL && cargas.isEmpty() && espacos.isEmpty()) {
            itens.add(erro("COMERCIAL_SEM_CARGAS", "Informe ao menos um espaço comercial ou uma carga de área comum.", Campos.ESPACOS, ETAPA_COMERCIAL));
        }
        for (int i = 0; i < espacos.size(); i++) {
            CommercialUnit e = espacos.get(i);
            if (vazio(e.getOcupacaoCodigo())) {
                itens.add(item("COMERCIAL_ESPACO_INCOMPLETO", "ERRO", "Espaço " + (i + 1) + " está sem finalidade.",
                        Campos.OCUPACAO, ETAPA_COMERCIAL, null, i));
            }
            if (e.getAreaM2() == null || e.getAreaM2().signum() <= 0) {
                itens.add(item("COMERCIAL_ESPACO_INCOMPLETO", "ERRO", "Espaço " + (i + 1) + " está sem área.",
                        Campos.AREA_ESPACO, ETAPA_COMERCIAL, null, i));
            }
            if (e.getPotenciaInstaladaKw() == null || e.getPotenciaInstaladaKw().signum() <= 0) {
                itens.add(item("COMERCIAL_ESPACO_INCOMPLETO", "ERRO", "Espaço " + (i + 1) + " está sem carga instalada.",
                        Campos.CARGA_ESPACO, ETAPA_COMERCIAL, null, i));
            }
        }
        cargaAbaixoDoMinimo(espacos, itens);
    }

    /** Usa o mesmo cálculo da etapa comercial (Tabela 22). Espaço incompleto fica de fora: já é ERRO. */
    private void cargaAbaixoDoMinimo(List<CommercialUnit> espacos, List<ItemValidacaoResponse> itens) {
        if (espacos.isEmpty() || itens.stream().anyMatch(i -> "COMERCIAL_ESPACO_INCOMPLETO".equals(i.codigo()))) {
            return;
        }
        List<EspacoComercialRequest> pedidos = espacos.stream().map(e -> {
            EspacoComercialRequest r = new EspacoComercialRequest();
            r.setOcupacaoCodigo(e.getOcupacaoCodigo());
            r.setAreaM2(e.getAreaM2());
            r.setCargaInstaladaKw(e.getPotenciaInstaladaKw());
            return r;
        }).toList();
        CommercialCalculationResult resultado;
        try {
            resultado = commercialStageService.calcular(pedidos);
        } catch (ValidationException ex) {
            return; // dado gravado antigo e inválido: a etapa comercial é que deve ser corrigida
        }
        for (int i = 0; i < resultado.unidadesDetalhadas().size(); i++) {
            CommercialUnitResult u = resultado.unidadesDetalhadas().get(i);
            if (u.alerta() != null) {
                itens.add(item("COMERCIAL_CARGA_ABAIXO_MINIMO", "ALERTA", "Espaço " + (i + 1) + ": carga de "
                        + um(u.potenciaInstaladaKw()) + " kW abaixo do mínimo da ocupação (" + um(u.cargaMinimaKw())
                        + " kW para " + u.areaM2().stripTrailingZeros().toPlainString().replace('.', ',') + " m²).",
                        Campos.CARGA_ESPACO, ETAPA_COMERCIAL, null, i));
            }
        }
    }

    // ------------------------------------------------------------------ etapa 5

    private void especiais(Project project, SpecialLoads e, List<ItemValidacaoResponse> itens) {
        if (project.isPossuiVe()) {
            Integer pontos = e == null ? null : e.getQtdPontosVe();
            if (pontos == null || pontos < PONTOS_VE_MIN || pontos > PONTOS_VE_MAX) {
                itens.add(erro("VE_INCOMPLETO", "Informe de " + PONTOS_VE_MIN + " a " + PONTOS_VE_MAX + " pontos de carregamento.",
                        Campos.PONTOS_VE, ETAPA_ESPECIAIS));
            }
            BigDecimal potencia = e == null ? null : e.getPotenciaEstacaoVeKw();
            if (potencia == null || potencia.compareTo(POTENCIA_VE_MIN) < 0 || potencia.compareTo(POTENCIA_VE_MAX) > 0) {
                itens.add(erro("VE_INCOMPLETO", "Informe a potência de cada ponto, de 3,70 a 22,0 kW.", Campos.POTENCIA_VE, ETAPA_ESPECIAIS));
            }
        }
        if (project.isPossuiIncendio()) {
            BigDecimal cv = e == null ? null : e.getPotenciaBombaIncendioCv();
            if (cv == null || cv.compareTo(BOMBA_CV_MIN) < 0 || cv.compareTo(BOMBA_CV_MAX) > 0) {
                itens.add(erro("MOTOBOMBA_INCOMPLETA", "Informe a potência da motobomba, de 1,0 a 50,0 cv.", Campos.POTENCIA_BOMBA, ETAPA_ESPECIAIS));
            }
            if (e == null || !Boolean.TRUE.equals(e.getMedidorExclusivo())) {
                itens.add(item("MOTOBOMBA_SEM_MEDIDOR_EXCLUSIVO", "ALERTA",
                        "Motobomba de incêndio sem medidor exclusivo marcado (DIS-NOR-053, item 6.20.2).",
                        Campos.MEDIDOR, ETAPA_ESPECIAIS, null, null));
            }
        }
    }

    // ------------------------------------------------------------------ utilidades

    private static ItemValidacaoResponse erro(String codigo, String mensagem, String campo, String etapa) {
        return item(codigo, "ERRO", mensagem, campo, etapa, null, null);
    }

    private static ItemValidacaoResponse item(String codigo, String nivel, String mensagem, String campo, String etapa,
                                              String aba, Integer indice) {
        return new ItemValidacaoResponse(codigo, nivel, mensagem, campo, etapa, aba, indice, null, null);
    }

    private static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static int quantidade(Integer valor) {
        return valor == null ? 0 : valor;
    }

    private static String um(BigDecimal valor) {
        return valor.setScale(1, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }
}
