package neoguide.project.service;

import neoguide.project.dto.CommonAreaLoadItemRequest;
import neoguide.project.dto.calculo.CalculoResponse;
import neoguide.project.dto.calculo.CalculoResponse.AlertaResponse;
import neoguide.project.dto.calculo.CalculoResponse.BarramentoQdg;
import neoguide.project.dto.calculo.CalculoResponse.DimensionamentoEntrada;
import neoguide.project.dto.calculo.CalculoResponse.Enquadramento;
import neoguide.project.dto.calculo.CalculoResponse.ResumoDemandas;
import neoguide.project.dto.calculo.EtapasProjetoResponse;
import neoguide.project.dto.calculo.ItemValidacaoResponse;
import neoguide.project.dto.calculo.ValidacaoNormativaResponse;
import neoguide.project.engine.CommonAreaCalculationEngine;
import neoguide.project.engine.DemandaFinalEngine;
import neoguide.project.engine.ResidentialCalculationEngine;
import neoguide.project.engine.model.EntradaDemandaFinal;
import neoguide.project.engine.model.ResidentialTypologyItem;
import neoguide.project.engine.model.ResultadoDemandaFinal;
import neoguide.project.exception.CalculoRecusadoException;
import neoguide.project.exception.ResourceNotFoundException;
import neoguide.project.model.CommercialUnit;
import neoguide.project.model.CommonAreaLoad;
import neoguide.project.model.DemandResult;
import neoguide.project.model.Project;
import neoguide.project.model.ResidentialTypology;
import neoguide.project.model.SpecialLoads;
import neoguide.project.model.TipoEdificacao;
import neoguide.project.model.normativo.BusbarReference;
import neoguide.project.model.normativo.ServiceEntranceReference;
import neoguide.project.repository.CommercialUnitRepository;
import neoguide.project.repository.CommonAreaLoadRepository;
import neoguide.project.repository.DemandResultRepository;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.ResidentialTypologyRepository;
import neoguide.project.repository.SpecialLoadsRepository;
import neoguide.project.repository.normativo.BusbarReferenceRepository;
import neoguide.project.repository.normativo.ServiceEntranceReferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * Cálculo final da demanda do projeto. Consolida as etapas gravadas no banco, valida a completude, calcula
 * (motor {@link DemandaFinalEngine}), grava o último resultado e serve resultado e memorial.
 *
 * <p>Situação do cálculo: CALCULADO quando a impressão digital dos insumos atuais é igual à do último cálculo;
 * DESATUALIZADO quando qualquer etapa mudou depois dele.
 */
@Service
public class CalculoDemandaService {

    static final String ETAPA_PROJETO = "projeto";
    static final String ETAPA_RESIDENCIAL = "residencial";
    static final String ETAPA_COMERCIAL = "comercial";
    static final String TENSAO_BT = "380_220V";

    static final String SITUACAO_CALCULADO = "CALCULADO";
    static final String SITUACAO_DESATUALIZADO = "DESATUALIZADO";
    static final String STATUS_CONCLUIDO = "CONCLUIDO";

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ProjectRepository projectRepository;
    private final ResidentialTypologyRepository residentialTypologyRepository;
    private final CommonAreaLoadRepository commonAreaLoadRepository;
    private final CommercialUnitRepository commercialUnitRepository;
    private final SpecialLoadsRepository specialLoadsRepository;
    private final DemandResultRepository demandResultRepository;
    private final ServiceEntranceReferenceRepository entradaRepository;
    private final BusbarReferenceRepository busbarRepository;
    private final ResidentialCalculationEngine residentialCalculationEngine;
    private final CommonAreaCalculationEngine commonAreaCalculationEngine;
    private final DemandaFinalEngine demandaFinalEngine;
    private final RegrasValidacaoNormativa regras;

    public CalculoDemandaService(ProjectRepository projectRepository,
                                 ResidentialTypologyRepository residentialTypologyRepository,
                                 CommonAreaLoadRepository commonAreaLoadRepository,
                                 CommercialUnitRepository commercialUnitRepository,
                                 SpecialLoadsRepository specialLoadsRepository,
                                 DemandResultRepository demandResultRepository,
                                 ServiceEntranceReferenceRepository entradaRepository,
                                 BusbarReferenceRepository busbarRepository,
                                 ResidentialCalculationEngine residentialCalculationEngine,
                                 CommonAreaCalculationEngine commonAreaCalculationEngine,
                                 DemandaFinalEngine demandaFinalEngine,
                                 RegrasValidacaoNormativa regras) {
        this.projectRepository = projectRepository;
        this.residentialTypologyRepository = residentialTypologyRepository;
        this.commonAreaLoadRepository = commonAreaLoadRepository;
        this.commercialUnitRepository = commercialUnitRepository;
        this.specialLoadsRepository = specialLoadsRepository;
        this.demandResultRepository = demandResultRepository;
        this.entradaRepository = entradaRepository;
        this.busbarRepository = busbarRepository;
        this.residentialCalculationEngine = residentialCalculationEngine;
        this.commonAreaCalculationEngine = commonAreaCalculationEngine;
        this.demandaFinalEngine = demandaFinalEngine;
        this.regras = regras;
    }

    /**
     * Validação normativa da Revisão: catálogo de regras sobre o que está gravado no banco. Erros bloqueiam o
     * cálculo; alertas não. Mensagens do resultado (50 kVA, média tensão, Ip) não entram aqui.
     */
    @Transactional(readOnly = true)
    public ValidacaoNormativaResponse validar(Long projectId, Long userId) {
        Project project = projetoDoUsuario(projectId, userId);
        List<ItemValidacaoResponse> itens = consolidar(project).itens();
        int erros = (int) itens.stream().filter(i -> "ERRO".equals(i.nivel())).count();
        int alertas = (int) itens.stream().filter(i -> "ALERTA".equals(i.nivel())).count();
        int infos = (int) itens.stream().filter(i -> "INFO".equals(i.nivel())).count();
        String situacao = erros > 0 ? "ERRO" : alertas > 0 ? "ATENCAO" : "OK";
        return new ValidacaoNormativaResponse(projectId, situacao, erros, alertas,
                new ValidacaoNormativaResponse.Totais(erros, alertas, infos), itens);
    }

    /** Calcula a demanda com os dados atuais e grava o resultado como o último cálculo do projeto. */
    @Transactional
    public CalculoResponse calcular(Long projectId, Long userId) {
        Project project = projetoDoUsuario(projectId, userId);
        Insumos insumos = consolidar(project);
        if (!insumos.erros().isEmpty()) {
            throw new CalculoRecusadoException(422, "etapas_obrigatorias",
                    "Há etapas obrigatórias sem dados. Corrija os campos indicados para calcular.", insumos.erros());
        }
        demandResultRepository.deleteByProjectId(projectId);
        DemandResult gravado = demandResultRepository.save(new DemandResult(project, LocalDateTime.now(),
                insumos.fingerprint(), insumos.tipoEnquadramento(), insumos.drf(), insumos.ds(), insumos.dc(), insumos.dve(), insumos.dmbi()));
        return montarResposta(project, gravado, SITUACAO_CALCULADO);
    }

    /** Última versão calculada. Se os dados mudaram depois, volta com situação DESATUALIZADO. */
    @Transactional(readOnly = true)
    public CalculoResponse resultado(Long projectId, Long userId) {
        Project project = projetoDoUsuario(projectId, userId);
        DemandResult ultimo = ultimoCalculo(projectId, 404);
        return montarResposta(project, ultimo, situacaoAtual(project, ultimo));
    }

    /** Memorial em PDF. Só com cálculo atual e atendimento em BT. */
    @Transactional(readOnly = true)
    public MemorialPdf memorialPdf(Long projectId, Long userId) {
        Project project = projetoDoUsuario(projectId, userId);
        DemandResult ultimo = ultimoCalculo(projectId, 409);
        String situacao = situacaoAtual(project, ultimo);
        if (!SITUACAO_CALCULADO.equals(situacao)) {
            throw new CalculoRecusadoException(409, "calculo_desatualizado",
                    "Os dados mudaram depois do último cálculo. Recalcule a demanda para gerar o memorial.", List.of());
        }
        CalculoResponse resposta = montarResposta(project, ultimo, situacao);
        if (resposta.enquadramento().atendimentoMT()) {
            throw new CalculoRecusadoException(409, "atendimento_mt",
                    "Projeto em média tensão: o memorial de baixa tensão não é emitido.", List.of());
        }
        return new MemorialPdf(project.getProtocolo(), MemorialPdfWriter.gerar(linhasDoMemorial(resposta)));
    }

    /** Dados gravados em cada etapa (residencial, área comum, VE e bomba), para reabrir e revisar o projeto. */
    @Transactional(readOnly = true)
    public EtapasProjetoResponse etapas(Long projectId, Long userId) {
        Project project = projetoDoUsuario(projectId, userId);
        Long id = project.getId();
        List<EtapasProjetoResponse.Tipologia> tipologias = residentialTypologyRepository.findByProjectIdOrderByIdAsc(id).stream()
                .map(t -> new EtapasProjetoResponse.Tipologia(t.getAreaUtilM2(), t.getQuantidadeUnidades(), t.isSmartStudio()))
                .toList();
        List<EtapasProjetoResponse.Carga> cargas = commonAreaLoadRepository.findByProjectIdOrderByIdAsc(id).stream()
                .map(c -> new EtapasProjetoResponse.Carga(c.getOrigem(), c.getSubmodulo(), c.getCatalogoMotorId(),
                        c.getCatalogoClimaId(), c.getCatalogoEquipamentoId(), c.getTipoEquipamento(), c.getDescricao(),
                        c.getQuantidade(), c.getPotenciaNominalKw(), c.getFatorPotencia(), c.getFatorDemanda(), c.isReserva()))
                .toList();
        EtapasProjetoResponse.CargasEspeciais especiais = specialLoadsRepository.findByProjectId(id)
                .map(s -> new EtapasProjetoResponse.CargasEspeciais(s.getQtdPontosVe(), s.getPotenciaEstacaoVeKw(),
                        s.getPotenciaBombaIncendioCv(), s.getPotenciaSolarInversorKw(), s.getMedidorExclusivo()))
                .orElse(null);
        return new EtapasProjetoResponse(new EtapasProjetoResponse.Residencial(tipologias),
                new EtapasProjetoResponse.AreaComum(cargas), especiais);
    }

    /** Conteúdo do memorial: protocolo para o nome do arquivo e bytes do PDF. */
    public record MemorialPdf(String protocolo, byte[] conteudo) {
    }

    // ---------------------------------------------------------------- consolidação

    /** Insumos do cálculo lidos do banco, com a lista de erros que impedem o cálculo. */
    record Insumos(TipoEdificacao tipo, TipoEdificacao tipoEnquadramento, BigDecimal drf, BigDecimal ds, BigDecimal dc, BigDecimal dve, BigDecimal dmbi,
                   String fingerprint, List<ItemValidacaoResponse> itens) {

        /** Itens que impedem o cálculo. */
        List<ItemValidacaoResponse> erros() {
            return itens.stream().filter(i -> "ERRO".equals(i.nivel())).toList();
        }

        EntradaDemandaFinal comoEntrada() {
            return new EntradaDemandaFinal(tipoEnquadramento, drf, ds, dc, dve, dmbi);
        }
    }

    private Insumos consolidar(Project project) {
        Long id = project.getId();
        TipoEdificacao tipo = project.getTipoEdificacao();
        List<ResidentialTypology> tipologias = residentialTypologyRepository.findByProjectIdOrderByIdAsc(id);
        List<CommonAreaLoad> cargas = commonAreaLoadRepository.findByProjectIdOrderByIdAsc(id);
        List<CommercialUnit> espacos = commercialUnitRepository.findByProjectIdOrderByIdAsc(id);
        SpecialLoads especiais = specialLoadsRepository.findByProjectId(id).orElse(null);

        BigDecimal drf = calcularDrf(tipologias);
        BigDecimal ds = calcularDs(cargas);
        BigDecimal dc = espacos.stream()
                .map(CommercialUnit::getDemandaCalculadaKva)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal dve = especiais != null && especiais.getDemandaVeKva() != null ? especiais.getDemandaVeKva() : BigDecimal.ZERO;
        BigDecimal dmbi = especiais != null && especiais.getDemandaBombaIncendioKva() != null
                ? especiais.getDemandaBombaIncendioKva() : BigDecimal.ZERO;

        String fingerprint = fingerprint(tipo, tipologias, cargas, espacos, especiais);
        List<ItemValidacaoResponse> itens = regras.avaliar(project, tipologias, cargas, espacos, especiais);
        TipoEdificacao enquadramento = EnquadramentoEdificacao.derivar(tipo, !espacos.isEmpty());
        return new Insumos(tipo, enquadramento, drf, ds, dc, dve, dmbi, fingerprint, itens);
    }

    private BigDecimal calcularDrf(List<ResidentialTypology> tipologias) {
        if (tipologias.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<ResidentialTypologyItem> itens = tipologias.stream()
                .map(t -> new ResidentialTypologyItem(t.getNome(), t.getAreaUtilM2(),
                        t.getQuantidadeUnidades() != null ? t.getQuantidadeUnidades() : 0, null, t.isSmartStudio()))
                .toList();
        return residentialCalculationEngine.calculate(itens).getDemandaFinalDrf();
    }

    private BigDecimal calcularDs(List<CommonAreaLoad> cargas) {
        if (cargas.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<CommonAreaLoadItemRequest> itens = cargas.stream().map(c -> {
            CommonAreaLoadItemRequest item = new CommonAreaLoadItemRequest();
            item.setOrigem(c.getOrigem());
            item.setSubmodulo(c.getSubmodulo());
            item.setCatalogoMotorId(c.getCatalogoMotorId());
            item.setCatalogoClimaId(c.getCatalogoClimaId());
            item.setCatalogoEquipamentoId(c.getCatalogoEquipamentoId());
            item.setTipoEquipamento(c.getTipoEquipamento());
            item.setDescricao(c.getDescricao());
            item.setQuantidade(c.getQuantidade());
            item.setPotenciaNominalKw(c.getPotenciaNominalKw());
            item.setFatorPotencia(c.getFatorPotencia());
            item.setFatorDemanda(c.getFatorDemanda());
            item.setReserva(c.isReserva());
            return item;
        }).toList();
        return commonAreaCalculationEngine.calculate(itens).getDemandaServicosDsKva();
    }

    /** Impressão digital dos insumos: muda quando qualquer dado que entra no cálculo muda. */
    static String fingerprint(TipoEdificacao tipo, List<ResidentialTypology> tipologias, List<CommonAreaLoad> cargas,
                              List<CommercialUnit> espacos, SpecialLoads especiais) {
        StringBuilder s = new StringBuilder("v1|tipo=").append(tipo == null ? "-" : tipo.name());
        for (ResidentialTypology t : tipologias) {
            s.append("|T:").append(norm(t.getAreaUtilM2())).append(',').append(t.getQuantidadeUnidades())
                    .append(',').append(t.isSmartStudio());
        }
        for (CommonAreaLoad c : cargas) {
            s.append("|C:").append(c.getOrigem()).append(',').append(c.getSubmodulo()).append(',')
                    .append(c.getCatalogoMotorId()).append(',').append(c.getCatalogoClimaId()).append(',')
                    .append(c.getCatalogoEquipamentoId()).append(',').append(c.getTipoEquipamento()).append(',')
                    .append(c.getDescricao()).append(',').append(c.getQuantidade()).append(',')
                    .append(norm(c.getPotenciaNominalKw())).append(',').append(norm(c.getFatorPotencia())).append(',')
                    .append(norm(c.getFatorDemanda())).append(',').append(c.isReserva());
        }
        for (CommercialUnit e : espacos) {
            s.append("|E:").append(e.getOrdem()).append(',').append(e.getOcupacaoCodigo()).append(',')
                    .append(norm(e.getAreaM2())).append(',').append(norm(e.getPotenciaInstaladaKw())).append(',')
                    .append(norm(e.getFatorDemanda()));
        }
        if (especiais != null) {
            s.append("|S:").append(especiais.getQtdPontosVe()).append(',').append(norm(especiais.getPotenciaEstacaoVeKw()))
                    .append(',').append(norm(especiais.getFatorSimultaneidadeVe())).append(',')
                    .append(norm(especiais.getPotenciaBombaIncendioCv())).append(',')
                    .append(norm(especiais.getPotenciaSolarInversorKw()));
        }
        return sha256(s.toString());
    }

    private static String norm(BigDecimal valor) {
        return valor == null ? "-" : valor.stripTrailingZeros().toPlainString();
    }

    private static String sha256(String texto) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível na JVM", e);
        }
    }

    // ---------------------------------------------------------------- cálculo e respostas

    private ResultadoDemandaFinal calcularDemanda(EntradaDemandaFinal entrada) {
        List<ServiceEntranceReference> entradas = entradaRepository.findAll().stream()
                .filter(e -> TENSAO_BT.equals(e.getTensaoFornecimento()))
                .toList();
        List<BusbarReference> barramentos = busbarRepository.findAll();
        return demandaFinalEngine.calcular(entrada, entradas, barramentos);
    }

    private CalculoResponse montarResposta(Project project, DemandResult d, String situacao) {
        ResultadoDemandaFinal r = calcularDemanda(new EntradaDemandaFinal(d.getTipoEdificacao(),
                d.getDrf(), d.getDs(), d.getDc(), d.getDve(), d.getDmbi()));

        ResumoDemandas resumo = new ResumoDemandas(
                apresentar(r.drf()), apresentar(r.ds()), apresentar(r.dc()), apresentar(r.dve()),
                apresentar(r.dmbi()), apresentar(r.ded()), apresentar(r.dqdg()));
        TipoEdificacao escolhido = project.getTipoEdificacao();
        Enquadramento enquadramento = new Enquadramento(r.atendimentoMt() ? "MT" : "BT_380_220V",
                r.limiteBtKva(), r.atendimentoMt(),
                escolhido == null ? null : escolhido.name(), d.getTipoEdificacao().name(),
                EnquadramentoEdificacao.motivo(escolhido, d.getTipoEdificacao()));
        DimensionamentoEntrada entrada = r.entrada() == null ? null : new DimensionamentoEntrada(
                r.entrada().disjuntorGeralA(), r.entrada().circuitos(), r.entrada().caboFaseMm2(), r.entrada().caboNeutroMm2(),
                r.entrada().eletrodutoEntrada(), r.entrada().tabelaReferencia(),
                r.ipA() == null ? null : apresentar(r.ipA()));
        BarramentoQdg barramento = r.barramento() == null ? null : new BarramentoQdg(
                r.barramento().secao(), r.barramento().capacidadeA(), r.barramento().tabelaReferencia());
        List<AlertaResponse> alertas = montarAlertas(r);

        return new CalculoResponse(project.getId(), project.getProtocolo(), STATUS_CONCLUIDO, situacao,
                d.getCalculadoEm(), resumo, enquadramento, entrada, barramento, alertas, selo(r));
    }

    private List<AlertaResponse> montarAlertas(ResultadoDemandaFinal r) {
        return r.alertas().stream()
                .map(a -> new AlertaResponse(a.codigo(), a.nivel(), a.mensagem(), a.campo()))
                .toList();
    }

    /** ATENDIMENTO_MT, COM_ALERTAS (há alerta de atenção) ou CONFORME. */
    private static String selo(ResultadoDemandaFinal r) {
        if (r.atendimentoMt()) return "ATENDIMENTO_MT";
        boolean alerta = r.alertas().stream().anyMatch(a -> "ALERTA".equals(a.nivel()));
        return alerta ? "COM_ALERTAS" : "CONFORME";
    }

    private String situacaoAtual(Project project, DemandResult ultimo) {
        return consolidar(project).fingerprint().equals(ultimo.getFingerprint())
                ? SITUACAO_CALCULADO : SITUACAO_DESATUALIZADO;
    }

    private DemandResult ultimoCalculo(Long projectId, int status) {
        Optional<DemandResult> ultimo = demandResultRepository.findTopByProjectIdOrderByCalculadoEmDescIdDesc(projectId);
        return ultimo.orElseThrow(() -> new CalculoRecusadoException(status, "nao_calculado",
                "O projeto ainda não foi calculado.", List.of()));
    }

    private Project projetoDoUsuario(Long projectId, Long userId) {
        return projectRepository.findById(projectId)
                .filter(Project::isAtivo)
                .filter(p -> p.getUser() != null && p.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com id " + projectId));
    }

    private static BigDecimal apresentar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    // ---------------------------------------------------------------- memorial

    private static List<String> linhasDoMemorial(CalculoResponse c) {
        List<String> l = new ArrayList<>();
        l.add("Memorial de cálculo de demanda - NeoGuide");
        l.add("");
        l.add("Protocolo: " + c.protocolo());
        l.add("Calculado em: " + (c.calculadoEm() == null ? "-" : c.calculadoEm().format(DATA_HORA)));
        l.add("Situação do cálculo: " + c.situacaoCalculo() + " | Selo de conformidade: " + c.seloConformidade());
        l.add("");
        l.add("Demandas (kVA)");
        ResumoDemandas r = c.resumoDemandas();
        l.add("  Drf (residencial): " + pt(r.drf()));
        l.add("  Ds (áreas comuns e serviços): " + pt(r.ds()));
        l.add("  Dc (comercial): " + pt(r.dc()));
        l.add("  Dve (recarga de veículos elétricos): " + pt(r.dve()));
        l.add("  Dmbi (motobomba de incêndio): " + pt(r.dmbi()));
        l.add("  Ded (demanda total): " + pt(r.ded()));
        l.add("  DQDG (sem motobomba de incêndio): " + pt(r.dqdg()));
        l.add("");
        Enquadramento e = c.enquadramento();
        l.add("Tipo de edificação: " + e.tipoEdificacao() + " | Tipo de enquadramento: " + e.tipoEnquadramento());
        if (e.motivoEnquadramento() != null) {
            l.add("  Motivo: " + e.motivoEnquadramento() + ".");
        }
        l.add("Enquadramento: " + ("MT".equals(e.tipoFornecimento()) ? "média tensão" : "baixa tensão 380/220 V")
                + " (limite " + pt(e.limiteKva()) + " kVA)");
        DimensionamentoEntrada entrada = c.dimensionamentoEntrada();
        if (entrada != null) {
            l.add("Entrada de serviço (" + entrada.tabelaReferencia() + "): disjuntor geral " + entrada.disjuntorGeralA()
                    + " A; condutor de fase " + entrada.caboFaseMm2() + "; condutor de neutro " + entrada.caboNeutroMm2()
                    + "; eletroduto " + entrada.eletrodutoEntrada() + ".");
            l.add("Corrente de projeto Ip: " + pt(entrada.correnteProjetoA()) + " A");
        }
        BarramentoQdg barramento = c.barramentoQdg();
        if (barramento != null) {
            l.add("Barramento do QDG (" + barramento.tabelaReferencia() + "): " + barramento.secao()
                    + ", " + barramento.capacidadeA() + " A.");
        }
        l.add("");
        l.add("Premissas adotadas");
        for (neoguide.project.dto.normas.PremissaResponse p : PremissasNormativas.TODAS) {
            l.add("  - " + p.texto() + " (" + p.referencia() + ")");
        }
        l.add("");
        l.add("Alertas e informações");
        if (c.alertas().isEmpty()) {
            l.add("  Nenhum.");
        }
        for (AlertaResponse a : c.alertas()) {
            l.add("  [" + a.nivel() + "] " + a.mensagem());
        }
        return l;
    }

    private static String pt(BigDecimal valor) {
        return valor == null ? "-" : valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }
}
