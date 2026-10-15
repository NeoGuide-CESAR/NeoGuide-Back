package neoguide.project;

import neoguide.project.config.seeder.NormativeDataSeeder;
import neoguide.project.model.normativo.AreaRangeReference;
import neoguide.project.model.normativo.MotorReference;
import neoguide.project.repository.normativo.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NormativeDataSeederTest {

    @Autowired
    private NormativeDataSeeder normativeDataSeeder;

    @Autowired
    private AreaRangeReferenceRepository areaRangeRepository;

    @Autowired
    private CoincidenceFactorReferenceRepository coincidenceFactorRepository;

    @Autowired
    private SafetyFactorReferenceRepository safetyFactorRepository;

    @Autowired
    private VeFactorReferenceRepository veFactorRepository;

    @Autowired
    private ServiceEntranceReferenceRepository serviceEntranceRepository;

    @Autowired
    private BusbarReferenceRepository busbarRepository;

    @Autowired
    private MinimumOutletsReferenceRepository minimumOutletsRepository;

    @Autowired
    private LightingResidentialDemandFactorRepository lightingResidentialDemandFactorRepository;

    @Autowired
    private WaterHeaterInstantFactorRepository waterHeaterRepository;

    @Autowired
    private BoilerDemandFactorRepository boilerRepository;

    @Autowired
    private AppliancesDemandFactorRepository appliancesRepository;

    @Autowired
    private CooktopDemandFactorRepository cooktopRepository;

    @Autowired
    private AirConditionerCatalogRepository airConditionerRepository;

    @Autowired
    private AcDemandFactorRepository acDemandFactorRepository;

    @Autowired
    private MotorDemandFactorRepository motorDemandFactorRepository;

    @Autowired
    private SpecialEquipmentDemandFactorRepository specialEquipmentRepository;

    @Autowired
    private PoolPumpDemandFactorRepository poolPumpRepository;

    @Autowired
    private MotorStarterReferenceRepository motorStarterRepository;

    @Autowired
    private SinglePhaseMotorReferenceRepository singlePhaseMotorRepository;

    @Autowired
    private MotorReferenceRepository motorRepository;

    @Autowired
    private OccupancyLoadReferenceRepository occupancyLoadRepository;

    @Test
    @DisplayName("Garante que todas as 21 tabelas normativas oficiais são povoadas com as quantidades esperadas")
    void shouldSeedAll21NormativeTables() {
        assertEquals(13, areaRangeRepository.count(), "Quadro 35: 13 faixas de área");
        assertEquals(11, coincidenceFactorRepository.count(), "Quadro 36: 11 fatores de coincidência");
        assertEquals(4, safetyFactorRepository.count(), "Quadro 37: 4 fatores de segurança");
        assertEquals(8, veFactorRepository.count(), "Quadro 33: 8 faixas de simultaneidade VE");
        assertEquals(20, serviceEntranceRepository.count(), "Tabelas 1 e 2: 20 dimensionamentos de entrada (10 para 220V, 10 para 380V)");
        assertEquals(12, busbarRepository.count(), "Tabela 3: 12 barramentos QDG");
        assertEquals(7, minimumOutletsRepository.count(), "Tabela 5: 7 faixas de tomadas mínimas");
        assertEquals(11, lightingResidentialDemandFactorRepository.count(), "Tabela 6: 11 fatores de demanda residencial");
        assertEquals(24, waterHeaterRepository.count(), "Tabela 7: 24 fatores de aquecedores de passagem");
        assertEquals(3, boilerRepository.count(), "Tabela 8: 3 fatores de boilers centrais");
        assertEquals(4, appliancesRepository.count(), "Tabela 9: 4 fatores de eletrodomésticos");
        assertEquals(12, cooktopRepository.count(), "Tabela 10: 12 fatores de cooktops e fogões");
        assertEquals(8, airConditionerRepository.count(), "Tabela 11: 8 capacidades de ar-condicionado");
        assertEquals(8, acDemandFactorRepository.count(), "Tabela 12: 8 faixas de fatores de ar-condicionado");
        assertEquals(3, motorDemandFactorRepository.count(), "Tabela 14: 3 regras de fatores de motores");
        assertEquals(2, specialEquipmentRepository.count(), "Tabela 15: 2 regras de equipamentos especiais e elevadores");
        assertEquals(4, poolPumpRepository.count(), "Tabela 16: 4 faixas de bombas de piscina");
        assertEquals(4, motorStarterRepository.count(), "Tabela 17: 4 tipos de chaves de partida");
        assertEquals(11, singlePhaseMotorRepository.count(), "Tabela 18: 11 motores monofásicos");
        assertEquals(12, motorRepository.count(), "Tabela 19: 12 motores trifásicos");
        assertEquals(6, occupancyLoadRepository.count(), "Tabela 22: 6 ocupações comerciais/gerais");
    }

    @Test
    @DisplayName("Valida exatidão dos dados normativos para amostras críticas de cálculo")
    void shouldValidateNormativeValuesAccuracy() {
        AreaRangeReference faixaAte40 = areaRangeRepository.findByFaixa("FAIXA_ATE_40").orElseThrow();
        assertEquals(new BigDecimal("1.20"), faixaAte40.getDemandaUnitariaKva());

        AreaRangeReference faixaAcima300 = areaRangeRepository.findByFaixa("FAIXA_ACIMA_300").orElseThrow();
        assertEquals(new BigDecimal("7.20"), faixaAcima300.getDemandaUnitariaKva());

        MotorReference motor5cv = motorRepository.findByPotenciaCv(new BigDecimal("5.00")).orElseThrow();
        assertEquals(new BigDecimal("5.27"), motor5cv.getDemandaUnitariaKva());
        assertEquals(new BigDecimal("8.00"), motor5cv.getCorrenteNominal380vA());

        assertEquals(10, serviceEntranceRepository.findByTensaoFornecimento("220_127V").size());
        assertEquals(10, serviceEntranceRepository.findByTensaoFornecimento("380_220V").size());
    }

    @Test
    @DisplayName("Garante a idempotência do DataSeeder ao executar a carga sucessivas vezes")
    void shouldBeIdempotentOnSubsequentRuns() {
        long countAntes = areaRangeRepository.count() + motorRepository.count() + busbarRepository.count();

        // Executa novamente o seeder
        normativeDataSeeder.seedAll();

        long countDepois = areaRangeRepository.count() + motorRepository.count() + busbarRepository.count();
        assertEquals(countAntes, countDepois, "A contagem deve permanecer rigorosamente idêntica após re-execução do seeder");
    }
}
