package neoguide.project.service;

import neoguide.project.dto.catalogo.BusbarCatalogResponse;
import neoguide.project.dto.catalogo.ServiceEntranceCatalogResponse;
import neoguide.project.model.normativo.BusbarReference;
import neoguide.project.model.normativo.ServiceEntranceReference;
import neoguide.project.repository.normativo.BusbarReferenceRepository;
import neoguide.project.repository.normativo.OccupancyLoadReferenceRepository;
import neoguide.project.repository.normativo.PoolPumpDemandFactorRepository;
import neoguide.project.repository.normativo.ServiceEntranceReferenceRepository;
import neoguide.project.repository.normativo.VeFactorReferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogoEntradaServiceTest {

    @Mock
    private ServiceEntranceReferenceRepository serviceEntranceRepository;

    @Mock
    private BusbarReferenceRepository busbarRepository;

    @Mock
    private VeFactorReferenceRepository veFactorRepository;

    @Mock
    private OccupancyLoadReferenceRepository occupancyLoadRepository;

    @Mock
    private PoolPumpDemandFactorRepository poolPumpRepository;

    private CatalogoNormativoService service;

    @BeforeEach
    void setUp() {
        service = new CatalogoNormativoService(
                veFactorRepository,
                occupancyLoadRepository,
                poolPumpRepository,
                serviceEntranceRepository,
                busbarRepository
        );
    }

    @Test
    @DisplayName("Deve retornar catálogo de dimensionamento de entrada 380V mapeado em DTO e ordenado")
    void shouldReturnEntrance380vMappedAndSorted() {
        ServiceEntranceReference e1 = new ServiceEntranceReference("380_220V", new BigDecimal("46.00"), 1, "16 mm²", "1 x 110", "1 x 100", 70, "160 A", 70, "DIS-NOR-053");
        ServiceEntranceReference e2 = new ServiceEntranceReference("380_220V", new BigDecimal("300.00"), 2, "2 x 185 mm²", "2 x 110", "2 x 100", 450, "630 A", 450, "DIS-NOR-053");
        when(serviceEntranceRepository.findByTensaoFornecimentoOrderByMaxDemandaKvaAsc("380_220V")).thenReturn(List.of(e1, e2));

        List<ServiceEntranceCatalogResponse> result = service.getDimensionamentoEntrada380v();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(new BigDecimal("46.00"), result.get(0).maxDemandaKva());
        assertEquals("16 mm²", result.get(0).condutorMm2());
        assertEquals(70, result.get(0).disjuntorIn());
        assertEquals(new BigDecimal("300.00"), result.get(1).maxDemandaKva());
        assertEquals(2, result.get(1).circuitos());

        verify(serviceEntranceRepository, times(1)).findByTensaoFornecimentoOrderByMaxDemandaKvaAsc("380_220V");
    }

    @Test
    @DisplayName("Deve retornar catálogo de dimensionamento de entrada 220V mapeado em DTO e ordenado")
    void shouldReturnEntrance220vMappedAndSorted() {
        ServiceEntranceReference e1 = new ServiceEntranceReference("220_127V", new BigDecimal("26.00"), 1, "16 mm²", "1 x 110", "1 x 100", 70, "160 A", 70, "DIS-NOR-053");
        ServiceEntranceReference e2 = new ServiceEntranceReference("220_127V", new BigDecimal("173.00"), 2, "2 x 185 mm²", "2 x 110", "2 x 100", 450, "630 A", 450, "DIS-NOR-053");
        when(serviceEntranceRepository.findByTensaoFornecimentoOrderByMaxDemandaKvaAsc("220_127V")).thenReturn(List.of(e1, e2));

        List<ServiceEntranceCatalogResponse> result = service.getDimensionamentoEntrada220v();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(new BigDecimal("26.00"), result.get(0).maxDemandaKva());
        assertEquals(new BigDecimal("173.00"), result.get(1).maxDemandaKva());

        verify(serviceEntranceRepository, times(1)).findByTensaoFornecimentoOrderByMaxDemandaKvaAsc("220_127V");
    }

    @Test
    @DisplayName("Deve retornar catálogo de barramentos do QDG mapeado em DTO e ordenado")
    void shouldReturnBusbarsMappedAndSorted() {
        BusbarReference b1 = new BusbarReference(new BigDecimal("40.00"), "19 x 3", "3/4\" x 1/8\"", 208, "DIS-NOR-053");
        BusbarReference b2 = new BusbarReference(new BigDecimal("1211.00"), "120 x 10", null, 3281, "DIS-NOR-053");
        when(busbarRepository.findAllByOrderByMaxDemandaKvaAsc()).thenReturn(List.of(b1, b2));

        List<BusbarCatalogResponse> result = service.getBarramentosQdg();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(new BigDecimal("40.00"), result.get(0).maxDemandaKva());
        assertEquals("19 x 3", result.get(0).barraMm());
        assertEquals(208, result.get(0).correnteNominalA());
        assertEquals(new BigDecimal("1211.00"), result.get(1).maxDemandaKva());
        assertEquals(3281, result.get(1).correnteNominalA());

        verify(busbarRepository, times(1)).findAllByOrderByMaxDemandaKvaAsc();
    }
}
