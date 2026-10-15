package neoguide.project.config.seeder;

import neoguide.project.model.normativo.*;
import neoguide.project.repository.normativo.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ResidencialNormativoSeeder {

    private final AreaRangeReferenceRepository areaRangeRepository;
    private final CoincidenceFactorReferenceRepository coincidenceFactorRepository;
    private final SafetyFactorReferenceRepository safetyFactorRepository;
    private final MinimumOutletsReferenceRepository minimumOutletsRepository;
    private final LightingResidentialDemandFactorRepository lightingResidentialDemandFactorRepository;

    public ResidencialNormativoSeeder(AreaRangeReferenceRepository areaRangeRepository,
                                      CoincidenceFactorReferenceRepository coincidenceFactorRepository,
                                      SafetyFactorReferenceRepository safetyFactorRepository,
                                      MinimumOutletsReferenceRepository minimumOutletsRepository,
                                      LightingResidentialDemandFactorRepository lightingResidentialDemandFactorRepository) {
        this.areaRangeRepository = areaRangeRepository;
        this.coincidenceFactorRepository = coincidenceFactorRepository;
        this.safetyFactorRepository = safetyFactorRepository;
        this.minimumOutletsRepository = minimumOutletsRepository;
        this.lightingResidentialDemandFactorRepository = lightingResidentialDemandFactorRepository;
    }

    public void seed() {
        seedAreaRanges();
        seedCoincidenceFactors();
        seedSafetyFactors();
        seedMinimumOutlets();
        seedLightingResidentialDemandFactors();
    }

    private void seedAreaRanges() {
        if (areaRangeRepository.count() > 0) return;

        String norma = "DIS-NOR-053 Quadro 35";
        List<AreaRangeReference> ranges = List.of(
                new AreaRangeReference("FAIXA_ATE_40", "Até 40 m²", new BigDecimal("0.00"), new BigDecimal("40.00"), new BigDecimal("1.20"), norma),
                new AreaRangeReference("FAIXA_41_50", "40,01 a 50 m²", new BigDecimal("40.01"), new BigDecimal("50.00"), new BigDecimal("1.35"), norma),
                new AreaRangeReference("FAIXA_51_60", "50,01 a 60 m²", new BigDecimal("50.01"), new BigDecimal("60.00"), new BigDecimal("1.50"), norma),
                new AreaRangeReference("FAIXA_61_70", "60,01 a 70 m²", new BigDecimal("60.01"), new BigDecimal("70.00"), new BigDecimal("1.65"), norma),
                new AreaRangeReference("FAIXA_71_80", "70,01 a 80 m²", new BigDecimal("70.01"), new BigDecimal("80.00"), new BigDecimal("1.80"), norma),
                new AreaRangeReference("FAIXA_81_90", "80,01 a 90 m²", new BigDecimal("80.01"), new BigDecimal("90.00"), new BigDecimal("1.95"), norma),
                new AreaRangeReference("FAIXA_91_100", "90,01 a 100 m²", new BigDecimal("90.01"), new BigDecimal("100.00"), new BigDecimal("2.15"), norma),
                new AreaRangeReference("FAIXA_101_120", "100,01 a 120 m²", new BigDecimal("100.01"), new BigDecimal("120.00"), new BigDecimal("2.45"), norma),
                new AreaRangeReference("FAIXA_121_140", "120,01 a 140 m²", new BigDecimal("120.01"), new BigDecimal("140.00"), new BigDecimal("2.80"), norma),
                new AreaRangeReference("FAIXA_141_170", "140,01 a 170 m²", new BigDecimal("140.01"), new BigDecimal("170.00"), new BigDecimal("3.30"), norma),
                new AreaRangeReference("FAIXA_171_200", "170,01 a 200 m²", new BigDecimal("170.01"), new BigDecimal("200.00"), new BigDecimal("3.90"), norma),
                new AreaRangeReference("FAIXA_201_300", "200,01 a 300 m²", new BigDecimal("200.01"), new BigDecimal("300.00"), new BigDecimal("5.40"), norma),
                new AreaRangeReference("FAIXA_ACIMA_300", "Acima de 300 m²", new BigDecimal("300.01"), new BigDecimal("9999.00"), new BigDecimal("7.20"), norma)
        );
        areaRangeRepository.saveAll(ranges);
    }

    private void seedCoincidenceFactors() {
        if (coincidenceFactorRepository.count() > 0) return;

        String norma = "DIS-NOR-053 Quadro 36";
        List<CoincidenceFactorReference> factors = List.of(
                new CoincidenceFactorReference(10, new BigDecimal("0.9640"), new BigDecimal("96.40"), norma),
                new CoincidenceFactorReference(15, new BigDecimal("0.9027"), new BigDecimal("90.27"), norma),
                new CoincidenceFactorReference(20, new BigDecimal("0.8560"), new BigDecimal("85.60"), norma),
                new CoincidenceFactorReference(25, new BigDecimal("0.8184"), new BigDecimal("81.84"), norma),
                new CoincidenceFactorReference(30, new BigDecimal("0.7827"), new BigDecimal("78.27"), norma),
                new CoincidenceFactorReference(35, new BigDecimal("0.7580"), new BigDecimal("75.80"), norma),
                new CoincidenceFactorReference(40, new BigDecimal("0.7380"), new BigDecimal("73.80"), norma),
                new CoincidenceFactorReference(45, new BigDecimal("0.7231"), new BigDecimal("72.31"), norma),
                new CoincidenceFactorReference(50, new BigDecimal("0.7080"), new BigDecimal("70.80"), norma),
                new CoincidenceFactorReference(55, new BigDecimal("0.6935"), new BigDecimal("69.35"), norma),
                new CoincidenceFactorReference(60, new BigDecimal("0.6823"), new BigDecimal("68.23"), norma)
        );
        coincidenceFactorRepository.saveAll(factors);
    }

    private void seedSafetyFactors() {
        if (safetyFactorRepository.count() > 0) return;

        String norma = "DIS-NOR-053 Quadro 37";
        List<SafetyFactorReference> factors = List.of(
                new SafetyFactorReference(new BigDecimal("0.00"), new BigDecimal("25.00"), new BigDecimal("1.50"), norma),
                new SafetyFactorReference(new BigDecimal("25.01"), new BigDecimal("50.00"), new BigDecimal("1.30"), norma),
                new SafetyFactorReference(new BigDecimal("50.01"), new BigDecimal("100.00"), new BigDecimal("1.20"), norma),
                new SafetyFactorReference(new BigDecimal("100.01"), new BigDecimal("9999.00"), new BigDecimal("1.10"), norma)
        );
        safetyFactorRepository.saveAll(factors);
    }

    private void seedMinimumOutlets() {
        if (minimumOutletsRepository.count() > 0) return;

        String norma = "DIS-NOR-030 Anexo II Tabela 5";
        List<MinimumOutletsReference> outlets = List.of(
                new MinimumOutletsReference(new BigDecimal("0.00"), new BigDecimal("30.00"), "Até 30 m²", 3, 2, norma),
                new MinimumOutletsReference(new BigDecimal("30.01"), new BigDecimal("45.00"), "30 a 45 m²", 4, 3, norma),
                new MinimumOutletsReference(new BigDecimal("45.01"), new BigDecimal("60.00"), "45 a 60 m²", 6, 4, norma),
                new MinimumOutletsReference(new BigDecimal("60.01"), new BigDecimal("80.00"), "60 a 80 m²", 8, 4, norma),
                new MinimumOutletsReference(new BigDecimal("80.01"), new BigDecimal("100.00"), "80 a 100 m²", 10, 5, norma),
                new MinimumOutletsReference(new BigDecimal("100.01"), new BigDecimal("120.00"), "100 a 120 m²", 12, 5, norma),
                new MinimumOutletsReference(new BigDecimal("120.01"), new BigDecimal("9999.00"), "Acima de 120 m²", 15, 6, norma)
        );
        minimumOutletsRepository.saveAll(outlets);
    }

    private void seedLightingResidentialDemandFactors() {
        if (lightingResidentialDemandFactorRepository.count() > 0) return;

        String norma = "DIS-NOR-030 Anexo II Tabela 6";
        List<LightingResidentialDemandFactor> factors = List.of(
                new LightingResidentialDemandFactor(new BigDecimal("0.00"), new BigDecimal("1.00"), new BigDecimal("0.86"), norma),
                new LightingResidentialDemandFactor(new BigDecimal("1.01"), new BigDecimal("2.00"), new BigDecimal("0.75"), norma),
                new LightingResidentialDemandFactor(new BigDecimal("2.01"), new BigDecimal("3.00"), new BigDecimal("0.66"), norma),
                new LightingResidentialDemandFactor(new BigDecimal("3.01"), new BigDecimal("4.00"), new BigDecimal("0.59"), norma),
                new LightingResidentialDemandFactor(new BigDecimal("4.01"), new BigDecimal("5.00"), new BigDecimal("0.52"), norma),
                new LightingResidentialDemandFactor(new BigDecimal("5.01"), new BigDecimal("6.00"), new BigDecimal("0.45"), norma),
                new LightingResidentialDemandFactor(new BigDecimal("6.01"), new BigDecimal("7.00"), new BigDecimal("0.40"), norma),
                new LightingResidentialDemandFactor(new BigDecimal("7.01"), new BigDecimal("8.00"), new BigDecimal("0.35"), norma),
                new LightingResidentialDemandFactor(new BigDecimal("8.01"), new BigDecimal("9.00"), new BigDecimal("0.31"), norma),
                new LightingResidentialDemandFactor(new BigDecimal("9.01"), new BigDecimal("10.00"), new BigDecimal("0.27"), norma),
                new LightingResidentialDemandFactor(new BigDecimal("10.01"), new BigDecimal("9999.00"), new BigDecimal("0.24"), norma)
        );
        lightingResidentialDemandFactorRepository.saveAll(factors);
    }
}
