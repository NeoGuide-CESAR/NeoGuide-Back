package neoguide.project.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import neoguide.project.dto.calculation.CalculationMemoryDto;
import neoguide.project.dto.calculation.DemandCalculationResponse;
import neoguide.project.engine.ConsolidatedCalculationEngine;
import neoguide.project.engine.ResidentialCalculationEngine;
import neoguide.project.engine.enums.SupplyVoltageLevel;
import neoguide.project.engine.enums.VoltageSystem;
import neoguide.project.engine.model.BusbarDimensioning;
import neoguide.project.engine.model.ConsolidatedCalculationInput;
import neoguide.project.engine.model.ConsolidatedCalculationResult;
import neoguide.project.engine.model.ResidentialCalculationResult;
import neoguide.project.engine.model.ResidentialTypologyItem;
import neoguide.project.engine.model.ServiceEntranceDimensioning;
import neoguide.project.exception.ForbiddenException;
import neoguide.project.exception.ResourceNotFoundException;
import neoguide.project.exception.UnprocessableEntityException;
import neoguide.project.model.CommercialUnit;
import neoguide.project.model.CommonAreaLoad;
import neoguide.project.model.DemandResults;
import neoguide.project.model.Project;
import neoguide.project.model.ProjectStatus;
import neoguide.project.model.ResidentialTypology;
import neoguide.project.model.SpecialLoads;
import neoguide.project.repository.CommercialUnitRepository;
import neoguide.project.repository.CommonAreaLoadRepository;
import neoguide.project.repository.DemandResultsRepository;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.ResidentialTypologyRepository;
import neoguide.project.repository.SpecialLoadsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DemandCalculationService {

    private final ProjectRepository projectRepository;
    private final ResidentialTypologyRepository residentialTypologyRepository;
    private final CommonAreaLoadRepository commonAreaLoadRepository;
    private final CommercialUnitRepository commercialUnitRepository;
    private final SpecialLoadsRepository specialLoadsRepository;
    private final DemandResultsRepository demandResultsRepository;
    private final ConsolidatedCalculationEngine consolidatedEngine;
    private final ResidentialCalculationEngine residentialCalculationEngine;
    private final ObjectMapper objectMapper;

    public DemandCalculationService(ProjectRepository projectRepository,
                                    ResidentialTypologyRepository residentialTypologyRepository,
                                    CommonAreaLoadRepository commonAreaLoadRepository,
                                    CommercialUnitRepository commercialUnitRepository,
                                    SpecialLoadsRepository specialLoadsRepository,
                                    DemandResultsRepository demandResultsRepository,
                                    ConsolidatedCalculationEngine consolidatedEngine,
                                    ResidentialCalculationEngine residentialCalculationEngine,
                                    ObjectMapper objectMapper) {
        this.projectRepository = projectRepository;
        this.residentialTypologyRepository = residentialTypologyRepository;
        this.commonAreaLoadRepository = commonAreaLoadRepository;
        this.commercialUnitRepository = commercialUnitRepository;
        this.specialLoadsRepository = specialLoadsRepository;
        this.demandResultsRepository = demandResultsRepository;
        this.consolidatedEngine = consolidatedEngine;
        this.residentialCalculationEngine = residentialCalculationEngine;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Transactional
    public DemandCalculationResponse calculateAndConsolidate(Long projectId, Long currentUserId) {
        Project project = projectRepository.findById(projectId)
                .filter(Project::isAtivo)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com id: " + projectId));

        if (project.getUser() == null || !project.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Usuário não tem permissão para calcular este projeto.");
        }

        // 1. Validação mandatória das tipologias residenciais da Etapa 2
        List<ResidentialTypology> typologies = residentialTypologyRepository.findByProjectIdOrderByIdAsc(projectId);
        if (typologies == null || typologies.isEmpty()) {
            throw new UnprocessableEntityException(
                    "Não é possível processar o cálculo oficial: Nenhuma tipologia residencial cadastrada na Etapa 2."
            );
        }

        // Cálculo de Drf via ResidentialCalculationEngine
        List<ResidentialTypologyItem> items = new ArrayList<>();
        for (ResidentialTypology t : typologies) {
            items.add(new ResidentialTypologyItem(
                    t.getNome(),
                    t.getAreaUtilM2(),
                    t.getQuantidadeUnidades(),
                    null,
                    t.isSmartStudio()
            ));
        }
        ResidentialCalculationResult resResult = residentialCalculationEngine.calculate(items);
        BigDecimal drf = resResult.demandaFinalDrf().setScale(2, RoundingMode.HALF_UP);
        int totalApartamentos = resResult.totalApartamentos();
        BigDecimal dr = resResult.demandaBrutaDr();
        BigDecimal fc = resResult.fatorCoincidenciaFc();
        BigDecimal fr = resResult.fatorSegurancaFr();

        // 2. Cargas de Áreas Comuns e Serviços (Ds)
        List<CommonAreaLoad> commonLoads = commonAreaLoadRepository.findByProjectId(projectId);
        BigDecimal ds = BigDecimal.ZERO;
        Map<String, BigDecimal> parcelasServico = new HashMap<>();
        if (commonLoads != null && !commonLoads.isEmpty()) {
            for (CommonAreaLoad load : commonLoads) {
                BigDecimal dem = load.getDemandaCalculadaKva() != null ? load.getDemandaCalculadaKva() : BigDecimal.ZERO;
                ds = ds.add(dem);
                String sub = load.getSubmodulo() != null ? load.getSubmodulo() : "OUTROS";
                parcelasServico.merge(sub, dem, BigDecimal::add);
            }
        }
        ds = ds.setScale(2, RoundingMode.HALF_UP);

        // 3. Unidades Comerciais (Dc)
        List<CommercialUnit> commercialUnits = commercialUnitRepository.findByProjectId(projectId);
        BigDecimal dc = BigDecimal.ZERO;
        if (commercialUnits != null && !commercialUnits.isEmpty()) {
            for (CommercialUnit cu : commercialUnits) {
                BigDecimal dem = cu.getDemandaCalculadaKva() != null ? cu.getDemandaCalculadaKva() : BigDecimal.ZERO;
                dc = dc.add(dem);
            }
        }
        dc = dc.setScale(2, RoundingMode.HALF_UP);

        // 4. Cargas Especiais (Dve e Dmbi)
        SpecialLoads specialLoads = specialLoadsRepository.findByProjectId(projectId).orElse(null);
        BigDecimal dve = (specialLoads != null && specialLoads.getDemandaVeKva() != null)
                ? specialLoads.getDemandaVeKva().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal dmbi = (specialLoads != null && specialLoads.getDemandaBombaIncendioKva() != null)
                ? specialLoads.getDemandaBombaIncendioKva().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        // 5. Sistema de Tensão
        VoltageSystem sistemaTensao = VoltageSystem.fromString(project.getTensaoFornecimento());

        // 6. Execução do Motor Mestre Consolidado (US-17)
        ConsolidatedCalculationInput input = new ConsolidatedCalculationInput(
                drf, ds, dc, dve, dmbi, sistemaTensao, null
        );
        ConsolidatedCalculationResult engineResult = consolidatedEngine.calculate(input);

        // 7. Serialização da memória de cálculo
        CalculationMemoryDto memoryDto = new CalculationMemoryDto(
                sistemaTensao.getDescription(),
                totalApartamentos,
                dr,
                fc,
                fr,
                drf,
                ds,
                parcelasServico,
                dc,
                dve,
                dmbi,
                engineResult.demandaTotalEdificacaoDed(),
                engineResult.demandaQdgDqdg(),
                engineResult.correnteProjetoIpA(),
                engineResult.nivelFornecimento().name(),
                "DIS-NOR-053 (Rev. 06)"
        );
        String memoriaJson;
        try {
            memoriaJson = objectMapper.writeValueAsString(memoryDto);
        } catch (JsonProcessingException e) {
            memoriaJson = "{}";
        }

        // 8. Upsert Idempotente em DemandResults
        DemandResults demandResults = demandResultsRepository.findByProjectId(projectId)
                .orElseGet(() -> {
                    DemandResults drEntity = new DemandResults();
                    drEntity.setProject(project);
                    return drEntity;
                });

        demandResults.setDemandaResidencialKva(drf);
        demandResults.setDemandaServicoKva(ds);
        demandResults.setDemandaComercialKva(dc);
        demandResults.setDemandaVeKva(dve);
        demandResults.setDemandaIncendioKva(dmbi);
        demandResults.setDemandaGlobalKva(engineResult.demandaTotalEdificacaoDed());
        demandResults.setDemandaQdgKva(engineResult.demandaQdgDqdg());

        String tipoFornecimento = engineResult.nivelFornecimento() == SupplyVoltageLevel.BAIXA_TENSAO_SECUNDARIA
                ? (sistemaTensao == VoltageSystem.TRIFASICO_380_220V ? "TRIFASICO_380_220V_BT" : "TRIFASICO_220_127V_BT")
                : "SUBESTACAO_ABRIGADA_13_8KV";
        demandResults.setTipoFornecimento(tipoFornecimento);

        ServiceEntranceDimensioning se = engineResult.padraoEntrada();
        BusbarDimensioning bb = engineResult.barramentoQdg();

        demandResults.setDisjuntorGeralA(se != null ? se.disjuntorCorrenteInA() : 0);
        demandResults.setCaboFaseMm2(se != null ? se.secaoCondutorFase() : "N/A (Subestação Particular)");
        demandResults.setCaboNeutroMm2(se != null ? se.secaoCondutorNeutro() : "N/A (Subestação Particular)");
        demandResults.setBarramentoCobre(bb != null ? bb.barraDimensaoMm() : "N/A");
        demandResults.setCapacidadeBarramentoA(bb != null ? bb.correnteNominalA() : 0);
        demandResults.setMemoriaCalculoJson(memoriaJson);
        demandResults.setCalculadoEm(LocalDateTime.now());

        demandResultsRepository.save(demandResults);

        // 9. Atualização de status e etapa do projeto
        project.setStatus(ProjectStatus.CONCLUIDO);
        project.setEtapaAtual(6);
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);

        String disjuntorCarcaca = se != null ? se.disjuntorCarcaca() : "N/A";
        String eletroduto = se != null ? (se.eletrodutoPeadMm() + " (PEAD) / " + se.eletrodutoAcoMm() + " (Aço)") : "N/A";

        return DemandCalculationResponse.of(project, demandResults, disjuntorCarcaca, eletroduto);
    }
}
