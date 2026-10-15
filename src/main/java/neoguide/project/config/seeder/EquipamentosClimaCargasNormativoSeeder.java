package neoguide.project.config.seeder;

import neoguide.project.model.normativo.*;
import neoguide.project.repository.normativo.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class EquipamentosClimaCargasNormativoSeeder {

    private final WaterHeaterInstantFactorRepository waterHeaterRepository;
    private final BoilerDemandFactorRepository boilerRepository;
    private final AppliancesDemandFactorRepository appliancesRepository;
    private final CooktopDemandFactorRepository cooktopRepository;
    private final AirConditionerCatalogRepository airConditionerRepository;
    private final AcDemandFactorRepository acDemandFactorRepository;
    private final PoolPumpDemandFactorRepository poolPumpRepository;
    private final OccupancyLoadReferenceRepository occupancyLoadRepository;

    public EquipamentosClimaCargasNormativoSeeder(WaterHeaterInstantFactorRepository waterHeaterRepository,
                                                 BoilerDemandFactorRepository boilerRepository,
                                                 AppliancesDemandFactorRepository appliancesRepository,
                                                 CooktopDemandFactorRepository cooktopRepository,
                                                 AirConditionerCatalogRepository airConditionerRepository,
                                                 AcDemandFactorRepository acDemandFactorRepository,
                                                 PoolPumpDemandFactorRepository poolPumpRepository,
                                                 OccupancyLoadReferenceRepository occupancyLoadRepository) {
        this.waterHeaterRepository = waterHeaterRepository;
        this.boilerRepository = boilerRepository;
        this.appliancesRepository = appliancesRepository;
        this.cooktopRepository = cooktopRepository;
        this.airConditionerRepository = airConditionerRepository;
        this.acDemandFactorRepository = acDemandFactorRepository;
        this.poolPumpRepository = poolPumpRepository;
        this.occupancyLoadRepository = occupancyLoadRepository;
    }

    public void seed() {
        seedWaterHeaters();
        seedBoilers();
        seedAppliances();
        seedCooktops();
        seedAirConditioners();
        seedAcDemandFactors();
        seedPoolPumps();
        seedOccupancyLoads();
    }

    private void seedWaterHeaters() {
        if (waterHeaterRepository.count() > 0) return;

        String norma = "DIS-NOR-030 Anexo II Tabela 7";
        BigDecimal fp = new BigDecimal("1.00");
        List<WaterHeaterInstantFactor> factors = List.of(
                new WaterHeaterInstantFactor(1, new BigDecimal("1.00"), fp, norma),
                new WaterHeaterInstantFactor(2, new BigDecimal("1.00"), fp, norma),
                new WaterHeaterInstantFactor(3, new BigDecimal("0.84"), fp, norma),
                new WaterHeaterInstantFactor(4, new BigDecimal("0.76"), fp, norma),
                new WaterHeaterInstantFactor(5, new BigDecimal("0.70"), fp, norma),
                new WaterHeaterInstantFactor(6, new BigDecimal("0.65"), fp, norma),
                new WaterHeaterInstantFactor(7, new BigDecimal("0.60"), fp, norma),
                new WaterHeaterInstantFactor(8, new BigDecimal("0.57"), fp, norma),
                new WaterHeaterInstantFactor(9, new BigDecimal("0.54"), fp, norma),
                new WaterHeaterInstantFactor(10, new BigDecimal("0.52"), fp, norma),
                new WaterHeaterInstantFactor(11, new BigDecimal("0.49"), fp, norma),
                new WaterHeaterInstantFactor(12, new BigDecimal("0.48"), fp, norma),
                new WaterHeaterInstantFactor(13, new BigDecimal("0.46"), fp, norma),
                new WaterHeaterInstantFactor(14, new BigDecimal("0.45"), fp, norma),
                new WaterHeaterInstantFactor(15, new BigDecimal("0.44"), fp, norma),
                new WaterHeaterInstantFactor(16, new BigDecimal("0.43"), fp, norma),
                new WaterHeaterInstantFactor(17, new BigDecimal("0.42"), fp, norma),
                new WaterHeaterInstantFactor(18, new BigDecimal("0.41"), fp, norma),
                new WaterHeaterInstantFactor(19, new BigDecimal("0.40"), fp, norma),
                new WaterHeaterInstantFactor(20, new BigDecimal("0.40"), fp, norma),
                new WaterHeaterInstantFactor(21, new BigDecimal("0.39"), fp, norma),
                new WaterHeaterInstantFactor(22, new BigDecimal("0.39"), fp, norma),
                new WaterHeaterInstantFactor(23, new BigDecimal("0.39"), fp, norma),
                new WaterHeaterInstantFactor(24, new BigDecimal("0.38"), fp, norma)
        );
        waterHeaterRepository.saveAll(factors);
    }

    private void seedBoilers() {
        if (boilerRepository.count() > 0) return;

        String norma = "DIS-NOR-030 Anexo II Tabela 8";
        BigDecimal fp = new BigDecimal("1.00");
        List<BoilerDemandFactor> factors = List.of(
                new BoilerDemandFactor(1, new BigDecimal("1.00"), fp, norma),
                new BoilerDemandFactor(2, new BigDecimal("0.72"), fp, norma),
                new BoilerDemandFactor(3, new BigDecimal("0.62"), fp, norma)
        );
        boilerRepository.saveAll(factors);
    }

    private void seedAppliances() {
        if (appliancesRepository.count() > 0) return;

        String norma = "DIS-NOR-030 Anexo II Tabela 9";
        BigDecimal fp = new BigDecimal("0.92");
        List<AppliancesDemandFactor> factors = List.of(
                new AppliancesDemandFactor(1, 1, "1 aparelho", new BigDecimal("1.00"), fp, norma),
                new AppliancesDemandFactor(2, 4, "2 a 4 aparelhos", new BigDecimal("0.70"), fp, norma),
                new AppliancesDemandFactor(5, 6, "5 a 6 aparelhos", new BigDecimal("0.60"), fp, norma),
                new AppliancesDemandFactor(7, 999, "Acima de 6 aparelhos", new BigDecimal("0.50"), fp, norma)
        );
        appliancesRepository.saveAll(factors);
    }

    private void seedCooktops() {
        if (cooktopRepository.count() > 0) return;

        String norma = "DIS-NOR-030 Anexo II Tabela 10";
        BigDecimal fp = new BigDecimal("1.00");
        List<CooktopDemandFactor> factors = List.of(
                new CooktopDemandFactor(1, new BigDecimal("1.00"), fp, norma),
                new CooktopDemandFactor(2, new BigDecimal("0.60"), fp, norma),
                new CooktopDemandFactor(3, new BigDecimal("0.48"), fp, norma),
                new CooktopDemandFactor(4, new BigDecimal("0.40"), fp, norma),
                new CooktopDemandFactor(5, new BigDecimal("0.37"), fp, norma),
                new CooktopDemandFactor(6, new BigDecimal("0.35"), fp, norma),
                new CooktopDemandFactor(7, new BigDecimal("0.33"), fp, norma),
                new CooktopDemandFactor(8, new BigDecimal("0.32"), fp, norma),
                new CooktopDemandFactor(9, new BigDecimal("0.31"), fp, norma),
                new CooktopDemandFactor(11, new BigDecimal("0.30"), fp, norma),
                new CooktopDemandFactor(15, new BigDecimal("0.28"), fp, norma),
                new CooktopDemandFactor(999, new BigDecimal("0.26"), fp, norma)
        );
        cooktopRepository.saveAll(factors);
    }

    private void seedAirConditioners() {
        if (airConditionerRepository.count() > 0) return;

        String norma = "DIS-NOR-030 Anexo II Tabela 11";
        BigDecimal fp = new BigDecimal("0.92");
        List<AirConditionerCatalog> units = List.of(
                new AirConditionerCatalog(7500, new BigDecimal("900.00"), new BigDecimal("1100.00"), 220, new BigDecimal("5.00"), fp, norma),
                new AirConditionerCatalog(9000, new BigDecimal("1300.00"), new BigDecimal("1550.00"), 220, new BigDecimal("7.00"), fp, norma),
                new AirConditionerCatalog(10000, new BigDecimal("1400.00"), new BigDecimal("1650.00"), 220, new BigDecimal("7.50"), fp, norma),
                new AirConditionerCatalog(12000, new BigDecimal("1600.00"), new BigDecimal("1900.00"), 220, new BigDecimal("8.50"), fp, norma),
                new AirConditionerCatalog(15000, new BigDecimal("1900.00"), new BigDecimal("2100.00"), 220, new BigDecimal("9.50"), fp, norma),
                new AirConditionerCatalog(18000, new BigDecimal("2300.00"), new BigDecimal("2600.00"), 220, new BigDecimal("11.80"), fp, norma),
                new AirConditionerCatalog(24000, new BigDecimal("3000.00"), new BigDecimal("3400.00"), 220, new BigDecimal("15.50"), fp, norma),
                new AirConditionerCatalog(30000, new BigDecimal("3800.00"), new BigDecimal("4300.00"), 220, new BigDecimal("19.50"), fp, norma)
        );
        airConditionerRepository.saveAll(units);
    }

    private void seedAcDemandFactors() {
        if (acDemandFactorRepository.count() > 0) return;

        String norma = "DIS-NOR-030 Anexo II Tabela 12";
        BigDecimal fp = new BigDecimal("0.92");
        List<AcDemandFactor> factors = List.of(
                new AcDemandFactor(1, 10, "1 a 10", new BigDecimal("1.00"), new BigDecimal("1.00"), fp, norma),
                new AcDemandFactor(11, 20, "11 a 20", new BigDecimal("0.90"), new BigDecimal("0.86"), fp, norma),
                new AcDemandFactor(21, 30, "21 a 30", new BigDecimal("0.82"), new BigDecimal("0.80"), fp, norma),
                new AcDemandFactor(31, 40, "31 a 40", new BigDecimal("0.80"), new BigDecimal("0.78"), fp, norma),
                new AcDemandFactor(41, 50, "41 a 50", new BigDecimal("0.77"), new BigDecimal("0.75"), fp, norma),
                new AcDemandFactor(51, 75, "51 a 75", new BigDecimal("0.75"), new BigDecimal("0.70"), fp, norma),
                new AcDemandFactor(76, 100, "76 a 100", new BigDecimal("0.75"), new BigDecimal("0.65"), fp, norma),
                new AcDemandFactor(101, 9999, "Mais de 100", new BigDecimal("0.75"), new BigDecimal("0.60"), fp, norma)
        );
        acDemandFactorRepository.saveAll(factors);
    }

    private void seedPoolPumps() {
        if (poolPumpRepository.count() > 0) return;

        String norma = "DIS-NOR-030 Anexo II Tabela 16";
        BigDecimal fp = new BigDecimal("0.80");
        List<PoolPumpDemandFactor> factors = List.of(
                new PoolPumpDemandFactor(1, new BigDecimal("1.00"), fp, norma),
                new PoolPumpDemandFactor(2, new BigDecimal("0.56"), fp, norma),
                new PoolPumpDemandFactor(3, new BigDecimal("0.47"), fp, norma),
                new PoolPumpDemandFactor(4, new BigDecimal("0.39"), fp, norma)
        );
        poolPumpRepository.saveAll(factors);
    }

    private void seedOccupancyLoads() {
        if (occupancyLoadRepository.count() > 0) return;

        String norma = "DIS-NOR-030 Anexo II Tabela 22";
        List<OccupancyLoadReference> loads = List.of(
                new OccupancyLoadReference("ADMINISTRACAO_EDIFICIO", "Administração Predial / Zeladoria",
                        new BigDecimal("5.00"), null, new BigDecimal("1.00"), new BigDecimal("0.50"), new BigDecimal("0.80"),
                        "1,00 iluminação / 0,50 TUG (FP = 0,80)", norma),
                new OccupancyLoadReference("SALAO_FESTAS_AUDITORIO", "Salões de Festas e Auditórios",
                        new BigDecimal("10.00"), null, new BigDecimal("1.00"), new BigDecimal("1.00"), new BigDecimal("0.80"),
                        "1,00 iluminação / 1,00 TUG (FP = 0,80)", norma),
                new OccupancyLoadReference("GARAGEM_CIRCULACAO", "Garagens e Circulação",
                        new BigDecimal("5.00"), null, new BigDecimal("1.00"), new BigDecimal("1.00"), new BigDecimal("0.80"),
                        "1,00 iluminação / 1,00 TUG (FP = 0,80)", norma),
                new OccupancyLoadReference("ESCRITORIOS", "Escritórios e Salas Comerciais",
                        new BigDecimal("30.00"), null, new BigDecimal("1.00"), new BigDecimal("0.70"), new BigDecimal("0.92"),
                        "1,00 até 20 kW / 0,70 excedente (FP = 0,92)", norma),
                new OccupancyLoadReference("LOJAS", "Lojas e Vitrines",
                        new BigDecimal("30.00"), new BigDecimal("700.00"), new BigDecimal("1.00"), new BigDecimal("1.00"), new BigDecimal("0.92"),
                        "30 W/m² + 700 W/m de vitrine, Fd = 1,00 (FP = 0,92)", norma),
                new OccupancyLoadReference("RESTAURANTES_BARES", "Restaurantes e Bares",
                        new BigDecimal("20.00"), null, new BigDecimal("1.00"), new BigDecimal("1.00"), new BigDecimal("0.92"),
                        "20 W/m², Fd = 1,00 (FP = 0,92)", norma)
        );
        occupancyLoadRepository.saveAll(loads);
    }
}
