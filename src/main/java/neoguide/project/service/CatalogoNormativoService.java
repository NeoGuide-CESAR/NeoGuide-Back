package neoguide.project.service;

import neoguide.project.model.normativo.AppliancesDemandFactor;
import neoguide.project.model.normativo.BusbarReference;
import neoguide.project.model.normativo.MotorDemandFactor;
import neoguide.project.model.normativo.MotorStarterReference;
import neoguide.project.model.normativo.PoolPumpDemandFactor;
import neoguide.project.model.normativo.ServiceEntranceReference;
import neoguide.project.model.normativo.SinglePhaseMotorReference;
import neoguide.project.model.normativo.SpecialEquipmentDemandFactor;
import neoguide.project.model.normativo.VeFactorReference;
import neoguide.project.repository.normativo.AppliancesDemandFactorRepository;
import neoguide.project.repository.normativo.BusbarReferenceRepository;
import neoguide.project.repository.normativo.MotorDemandFactorRepository;
import neoguide.project.repository.normativo.MotorStarterReferenceRepository;
import neoguide.project.repository.normativo.PoolPumpDemandFactorRepository;
import neoguide.project.repository.normativo.ServiceEntranceReferenceRepository;
import neoguide.project.repository.normativo.SinglePhaseMotorReferenceRepository;
import neoguide.project.repository.normativo.SpecialEquipmentDemandFactorRepository;
import neoguide.project.repository.normativo.VeFactorReferenceRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tabelas das normas que já estão no banco e ainda não tinham endpoint de catálogo (somente leitura, com cache).
 * Cada linha traz a origem (norma, tabela, página), já gravada no seed.
 */
@Service
public class CatalogoNormativoService {

    private final VeFactorReferenceRepository ve;
    private final ServiceEntranceReferenceRepository entradas;
    private final BusbarReferenceRepository barramentos;
    private final AppliancesDemandFactorRepository eletrodomesticos;
    private final MotorDemandFactorRepository motores;
    private final SpecialEquipmentDemandFactorRepository especiais;
    private final PoolPumpDemandFactorRepository piscina;
    private final MotorStarterReferenceRepository partidas;
    private final SinglePhaseMotorReferenceRepository monofasicos;

    public CatalogoNormativoService(VeFactorReferenceRepository ve, ServiceEntranceReferenceRepository entradas,
                                    BusbarReferenceRepository barramentos, AppliancesDemandFactorRepository eletrodomesticos,
                                    MotorDemandFactorRepository motores, SpecialEquipmentDemandFactorRepository especiais,
                                    PoolPumpDemandFactorRepository piscina, MotorStarterReferenceRepository partidas,
                                    SinglePhaseMotorReferenceRepository monofasicos) {
        this.ve = ve;
        this.entradas = entradas;
        this.barramentos = barramentos;
        this.eletrodomesticos = eletrodomesticos;
        this.motores = motores;
        this.especiais = especiais;
        this.piscina = piscina;
        this.partidas = partidas;
        this.monofasicos = monofasicos;
    }

    /** Quadro 33 (DIS-NOR-053): fator de demanda dos agrupamentos de recarga veicular, por número de vagas. */
    @Cacheable("catalogoRecargaVe")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> recargaVe() {
        // Só a revisão vigente (bancos antigos podem ter faixas da revisão anterior).
        return ve.findAll().stream()
                .filter(r -> r.getNormaOrigem() != null && r.getNormaOrigem().contains("Rev. 06"))
                .sorted(Comparator.comparing(VeFactorReference::getMaxVagas))
                .map(r -> linha("faixa", r.getFaixaDescricao(), "maxVagas", r.getMaxVagas(),
                        "fatorSimultaneidade", r.getFatorSimultaneidade(), "norma", r.getNormaOrigem()))
                .toList();
    }

    /** Tabela 2 (380/220 V) ou Tabela 1 (220/127 V) do Anexo II: entrada de serviço pela demanda da edificação. */
    @Cacheable(value = "catalogoEntradaServico", key = "#tensao")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> entradaServico(String tensao) {
        return entradas.findAll().stream()
                .filter(e -> tensao.equals(e.getTensaoFornecimento()))
                .sorted(Comparator.comparing(ServiceEntranceReference::getMaxDemandaKva))
                .map(e -> linha("maxDemandaKva", e.getMaxDemandaKva(), "circuitos", e.getQuantidadeCircuitos(),
                        "secaoCondutor", e.getSecaoCondutorMm2(), "dutoPead", e.getDutoPeadMm(), "dutoAco", e.getDutoAcoMm(),
                        "correnteTotalA", e.getCorrenteTotalA(), "disjuntorCarcaca", e.getDisjuntorCarcaca(),
                        "disjuntorCorrenteA", e.getDisjuntorCorrenteA(), "norma", e.getNormaOrigem()))
                .toList();
    }

    /** Tabela 3 do Anexo II: barramento de cobre do QDG pela demanda do QDG. */
    @Cacheable("catalogoBarramentos")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> barramentos() {
        return barramentos.findAll().stream().sorted(Comparator.comparing(BusbarReference::getMaxDemandaKva))
                .map(b -> linha("maxDemandaKva", b.getMaxDemandaKva(), "dimensaoMm", b.getBarraDimensaoMm(),
                        "dimensaoPol", b.getBarraDimensaoPol(), "correnteNominalA", b.getCorrenteNominalA(), "norma", b.getNormaOrigem()))
                .toList();
    }

    /** Tabela 9 (DIS-NOR-030): secadora, forno elétrico, lavadora, lava-louça e micro-ondas. */
    @Cacheable("catalogoEletrodomesticos")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> eletrodomesticos() {
        return eletrodomesticos.findAll().stream().sorted(Comparator.comparing(AppliancesDemandFactor::getMinAparelhos))
                .map(r -> linha("faixa", r.getDescricaoFaixa(), "minAparelhos", r.getMinAparelhos(), "maxAparelhos", r.getMaxAparelhos(),
                        "fatorDemanda", r.getFatorDemanda(), "fatorPotencia", r.getFatorPotencia(), "norma", r.getNormaOrigem()))
                .toList();
    }

    /** Tabela 14: motores. */
    @Cacheable("catalogoFatoresMotores")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> fatoresMotores() {
        return motores.findAll().stream().sorted(Comparator.comparing(MotorDemandFactor::getId))
                .map(r -> linha("tipoEnquadramento", r.getTipoEnquadramento(), "descricao", r.getDescricao(),
                        "fatorDemanda", r.getFatorDemanda(), "norma", r.getNormaOrigem()))
                .toList();
    }

    /** Tabela 15: equipamentos especiais (elevadores, portões etc.). */
    @Cacheable("catalogoEquipamentosEspeciais")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> equipamentosEspeciais() {
        return especiais.findAll().stream().sorted(Comparator.comparing(SpecialEquipmentDemandFactor::getId))
                .map(r -> linha("tipoEquipamento", r.getTipoEquipamento(), "descricao", r.getDescricao(),
                        "fatorDemanda", r.getFatorDemanda(), "fatorPotencia", r.getFatorPotencia(), "norma", r.getNormaOrigem()))
                .toList();
    }

    /** Tabela 16: bombas de piscina e hidromassagem. */
    @Cacheable("catalogoBombasPiscina")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> bombasPiscina() {
        return piscina.findAll().stream().sorted(Comparator.comparing(PoolPumpDemandFactor::getQuantidadeBombas))
                .map(r -> linha("quantidadeBombas", r.getQuantidadeBombas(), "fatorDemanda", r.getFatorDemanda(),
                        "fatorPotencia", r.getFatorPotencia(), "norma", r.getNormaOrigem()))
                .toList();
    }

    /** Tabela 17: dispositivos de partida de motores. */
    @Cacheable("catalogoDispositivosPartida")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> dispositivosPartida() {
        return partidas.findAll().stream().sorted(Comparator.comparing(MotorStarterReference::getId))
                .map(r -> linha("tipoPartida", r.getTipoPartida(), "descricao", r.getDescricao(),
                        "minCv220v", r.getMinCv220v(), "maxCv220v", r.getMaxCv220v(),
                        "minCv380v", r.getMinCv380v(), "maxCv380v", r.getMaxCv380v(), "norma", r.getNormaOrigem()))
                .toList();
    }

    /** Tabela 18: motores monofásicos. */
    @Cacheable("catalogoMotoresMonofasicos")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> motoresMonofasicos() {
        return monofasicos.findAll().stream().sorted(Comparator.comparing(SinglePhaseMotorReference::getPotenciaCvNumerica))
                .map(r -> linha("potenciaCv", r.getPotenciaCv(), "potenciaW", r.getPotenciaW(), "potenciaVa", r.getPotenciaVa(),
                        "corrente110vA", r.getCorrente110vA(), "corrente220vA", r.getCorrente220vA(),
                        "fatorPotencia", r.getFatorPotencia(), "norma", r.getNormaOrigem()))
                .toList();
    }

    /** Monta uma linha com os campos na ordem dada (valores nulos entram como null). */
    private static Map<String, Object> linha(Object... pares) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < pares.length; i += 2) {
            m.put((String) pares[i], pares[i + 1]);
        }
        return m;
    }
}
