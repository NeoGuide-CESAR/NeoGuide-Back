package neoguide.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import neoguide.project.dto.calculation.DemandCalculationResponse;
import neoguide.project.engine.ConsolidatedCalculationEngine;
import neoguide.project.engine.ResidentialCalculationEngine;
import neoguide.project.engine.enums.SupplyVoltageLevel;
import neoguide.project.engine.enums.VoltageSystem;
import neoguide.project.engine.model.BusbarDimensioning;
import neoguide.project.engine.model.ConsolidatedCalculationResult;
import neoguide.project.engine.model.ResidentialCalculationResult;
import neoguide.project.engine.model.ResidentialTypologyOutput;
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
import neoguide.project.model.User;
import neoguide.project.repository.CommercialUnitRepository;
import neoguide.project.repository.CommonAreaLoadRepository;
import neoguide.project.repository.DemandResultsRepository;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.ResidentialTypologyRepository;
import neoguide.project.repository.SpecialLoadsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemandCalculationServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ResidentialTypologyRepository residentialTypologyRepository;

    @Mock
    private CommonAreaLoadRepository commonAreaLoadRepository;

    @Mock
    private CommercialUnitRepository commercialUnitRepository;

    @Mock
    private SpecialLoadsRepository specialLoadsRepository;

    @Mock
    private DemandResultsRepository demandResultsRepository;

    @Mock
    private ConsolidatedCalculationEngine consolidatedEngine;

    @Mock
    private ResidentialCalculationEngine residentialCalculationEngine;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private DemandCalculationService service;

    private User user;
    private Project project;

    @BeforeEach
    void setUp() {
        service = new DemandCalculationService(
                projectRepository,
                residentialTypologyRepository,
                commonAreaLoadRepository,
                commercialUnitRepository,
                specialLoadsRepository,
                demandResultsRepository,
                consolidatedEngine,
                residentialCalculationEngine,
                objectMapper
        );

        user = new User();
        user.setId(1L);
        user.setName("Projetista Teste");
        user.setEmail("teste@empresa.com");

        project = new Project();
        project.setId(10L);
        project.setProtocolo("PRJ-2026-0001");
        project.setTitulo("Edifício Solar dos Pinheiros");
        project.setTensaoFornecimento("380_220V");
        project.setUser(user);
        project.setEtapaAtual(5);
        project.setStatus(ProjectStatus.RASCUNHO);
        project.setAtivo(true);
    }

    @Test
    @DisplayName("Deve calcular e consolidar projeto com sucesso gravando DemandResults e atualizando status para CONCLUIDO")
    void shouldCalculateAndConsolidateSuccessfully() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        ResidentialTypology tipologia = new ResidentialTypology(project, "Tipo 1", new BigDecimal("70.00"), 40, false, new BigDecimal("1.50"), new BigDecimal("60.00"));
        when(residentialTypologyRepository.findByProjectIdOrderByIdAsc(10L)).thenReturn(List.of(tipologia));

        ResidentialCalculationResult resCalc = new ResidentialCalculationResult(
                40,
                new BigDecimal("60.00"),
                new BigDecimal("0.7380"),
                new BigDecimal("44.28"),
                new BigDecimal("1.00"),
                new BigDecimal("44.28"),
                false,
                List.of(new ResidentialTypologyOutput("Tipo 1", new BigDecimal("70.00"), 40, new BigDecimal("1.50"), new BigDecimal("60.00")))
        );
        when(residentialCalculationEngine.calculate(any(List.class))).thenReturn(resCalc);

        CommonAreaLoad load = new CommonAreaLoad();
        load.setDemandaCalculadaKva(new BigDecimal("20.00"));
        load.setSubmodulo("MOTORES");
        when(commonAreaLoadRepository.findByProjectId(10L)).thenReturn(List.of(load));

        CommercialUnit cu = new CommercialUnit();
        cu.setDemandaCalculadaKva(new BigDecimal("15.00"));
        when(commercialUnitRepository.findByProjectId(10L)).thenReturn(List.of(cu));

        SpecialLoads specialLoads = new SpecialLoads();
        specialLoads.setDemandaVeKva(new BigDecimal("10.00"));
        specialLoads.setDemandaBombaIncendioKva(new BigDecimal("5.00"));
        when(specialLoadsRepository.findByProjectId(10L)).thenReturn(Optional.of(specialLoads));

        when(demandResultsRepository.findByProjectId(10L)).thenReturn(Optional.empty());

        ServiceEntranceDimensioning se = new ServiceEntranceDimensioning(
                "380/220 V", new BigDecimal("135.00"), 1, "1 x 95 mm²", "1 x 95 mm²",
                "1 x 110 mm", "1 x 100 mm", 200, "250 A", 200, 10, "DIS-NOR-053 Anexo II Tabela 2"
        );
        BusbarDimensioning bb = new BusbarDimensioning(
                new BigDecimal("104.00"), "25 x 5 mm", "1\" x 3/16\"", 340, "DIS-NOR-053 Anexo II Tabela 3"
        );
        ConsolidatedCalculationResult engineResult = new ConsolidatedCalculationResult(
                new BigDecimal("94.28"),
                new BigDecimal("89.28"),
                new BigDecimal("5.00"),
                new BigDecimal("135.65"),
                VoltageSystem.TRIFASICO_380_220V,
                SupplyVoltageLevel.BAIXA_TENSAO_SECUNDARIA,
                se,
                bb,
                true,
                true,
                List.of("Nota teste")
        );
        when(consolidatedEngine.calculate(any())).thenReturn(engineResult);

        DemandCalculationResponse response = service.calculateAndConsolidate(10L, 1L);

        assertNotNull(response);
        assertEquals(10L, response.projectId());
        assertEquals("PRJ-2026-0001", response.protocolo());
        assertEquals("CONCLUIDO", response.status());
        assertEquals("CONFORME_DIS_NOR_053", response.seloConformidade());

        assertEquals(new BigDecimal("44.28"), response.resumoDemandas().demandaResidencialFinalDrfKva());
        assertEquals(new BigDecimal("20.00"), response.resumoDemandas().demandaServicosDsKva());
        assertEquals(new BigDecimal("15.00"), response.resumoDemandas().demandaComercialDcKva());
        assertEquals(new BigDecimal("10.00"), response.resumoDemandas().demandaVeDveKva());
        assertEquals(new BigDecimal("5.00"), response.resumoDemandas().demandaIncendioDmbiKva());
        assertEquals(new BigDecimal("94.28"), response.resumoDemandas().demandaGlobalDedKva());
        assertEquals(new BigDecimal("89.28"), response.resumoDemandas().demandaQdgKva());

        assertEquals(200, response.dimensionamentoEntrada().disjuntorGeralA());
        assertEquals("250 A", response.dimensionamentoEntrada().disjuntorCarcaca());
        assertEquals("1 x 95 mm²", response.dimensionamentoEntrada().caboFaseMm2());
        assertEquals("25 x 5 mm", response.dimensionamentoEntrada().barramentoCobreQdg());
        assertEquals(340, response.dimensionamentoEntrada().capacidadeBarramentoA());

        // Verifica salvamento no repositório
        ArgumentCaptor<DemandResults> captor = ArgumentCaptor.forClass(DemandResults.class);
        verify(demandResultsRepository).save(captor.capture());
        DemandResults saved = captor.getValue();
        assertEquals(new BigDecimal("94.28"), saved.getDemandaGlobalKva());
        assertEquals("TRIFASICO_380_220V_BT", saved.getTipoFornecimento());
        assertTrue(saved.getMemoriaCalculoJson().contains("totalApartamentos"));

        // Verifica atualização do projeto
        assertEquals(ProjectStatus.CONCLUIDO, project.getStatus());
        assertEquals(6, project.getEtapaAtual());
        verify(projectRepository).save(project);
    }

    @Test
    @DisplayName("Deve ser idempotente no recálculo atualizando DemandResults existente")
    void shouldBeIdempotentOnRecalculate() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        ResidentialTypology tipologia = new ResidentialTypology(project, "Tipo 1", new BigDecimal("70.00"), 40, false, new BigDecimal("1.50"), new BigDecimal("60.00"));
        when(residentialTypologyRepository.findByProjectIdOrderByIdAsc(10L)).thenReturn(List.of(tipologia));

        ResidentialCalculationResult resCalc = new ResidentialCalculationResult(
                40, new BigDecimal("60.00"), new BigDecimal("0.7380"), new BigDecimal("44.28"),
                new BigDecimal("1.00"), new BigDecimal("44.28"), false, Collections.emptyList()
        );
        when(residentialCalculationEngine.calculate(any(List.class))).thenReturn(resCalc);

        DemandResults existing = new DemandResults();
        existing.setId(99L);
        existing.setProject(project);
        when(demandResultsRepository.findByProjectId(10L)).thenReturn(Optional.of(existing));

        ConsolidatedCalculationResult engineResult = new ConsolidatedCalculationResult(
                new BigDecimal("44.28"), new BigDecimal("44.28"), BigDecimal.ZERO,
                new BigDecimal("67.28"), VoltageSystem.TRIFASICO_380_220V,
                SupplyVoltageLevel.BAIXA_TENSAO_SECUNDARIA, null, null, false, false, List.of()
        );
        when(consolidatedEngine.calculate(any())).thenReturn(engineResult);

        DemandCalculationResponse response = service.calculateAndConsolidate(10L, 1L);

        assertNotNull(response);
        verify(demandResultsRepository).save(existing);
        assertEquals(99L, existing.getId());
    }

    @Test
    @DisplayName("Deve classificar como Média Tensão quando demanda superar 300 kVA")
    void shouldClassifyMtWhenDemandExceeds300Kva() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        ResidentialTypology tipologia = new ResidentialTypology(project, "Tipo 1", new BigDecimal("70.00"), 300, false, new BigDecimal("1.50"), new BigDecimal("450.00"));
        when(residentialTypologyRepository.findByProjectIdOrderByIdAsc(10L)).thenReturn(List.of(tipologia));

        ResidentialCalculationResult resCalc = new ResidentialCalculationResult(
                300, new BigDecimal("450.00"), new BigDecimal("0.6823"), new BigDecimal("307.04"),
                new BigDecimal("1.00"), new BigDecimal("307.04"), false, Collections.emptyList()
        );
        when(residentialCalculationEngine.calculate(any(List.class))).thenReturn(resCalc);

        ConsolidatedCalculationResult engineResult = new ConsolidatedCalculationResult(
                new BigDecimal("307.04"), new BigDecimal("307.04"), BigDecimal.ZERO,
                new BigDecimal("466.52"), VoltageSystem.TRIFASICO_380_220V,
                SupplyVoltageLevel.MEDIA_TENSAO_PRIMARIA, null, null, false, false, List.of("Subestação Abrigada")
        );
        when(consolidatedEngine.calculate(any())).thenReturn(engineResult);

        DemandCalculationResponse response = service.calculateAndConsolidate(10L, 1L);

        assertEquals("SUBESTACAO_ABRIGADA_13_8KV", response.dimensionamentoEntrada().tipoFornecimento());
    }

    @Test
    @DisplayName("Deve lançar UnprocessableEntityException quando projeto não tiver tipologias residenciais")
    void shouldThrowUnprocessableEntityWhenNoResidentialTypologies() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(residentialTypologyRepository.findByProjectIdOrderByIdAsc(10L)).thenReturn(Collections.emptyList());

        UnprocessableEntityException ex = assertThrows(
                UnprocessableEntityException.class,
                () -> service.calculateAndConsolidate(10L, 1L)
        );

        assertTrue(ex.getMessage().contains("Nenhuma tipologia residencial"));
    }

    @Test
    @DisplayName("Deve lançar ForbiddenException quando usuário não for dono do projeto")
    void shouldThrowForbiddenWhenUserNotOwner() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        assertThrows(
                ForbiddenException.class,
                () -> service.calculateAndConsolidate(10L, 999L)
        );
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando projeto não for encontrado")
    void shouldThrowResourceNotFoundWhenProjectNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.calculateAndConsolidate(999L, 1L)
        );
    }
}
