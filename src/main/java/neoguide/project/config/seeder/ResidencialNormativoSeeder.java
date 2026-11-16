package neoguide.project.config.seeder;

import neoguide.project.engine.normativo.TabelasNormativas;
import neoguide.project.model.normativo.*;
import neoguide.project.repository.normativo.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    /**
     * Quadro 35 (Di por área). Upsert pela chave {@code faixa}: linhas existentes recebem os valores normativos,
     * linhas ausentes são criadas. Linhas antigas que não existem mais na norma NÃO são apagadas aqui
     * (apagar dados exige autorização; ver docs/AUDITORIA-INTEGRACAO.md, pergunta P15).
     */
    private void seedAreaRanges() {
        String norma = TabelasNormativas.NORMA_QUADRO_35;
        for (TabelasNormativas.FaixaArea f : TabelasNormativas.QUADRO_35) {
            areaRangeRepository.findByFaixa(f.codigo()).ifPresentOrElse(existente -> {
                existente.setRotulo(f.rotulo());
                existente.setMinAreaM2(f.minM2());
                existente.setMaxAreaM2(f.maxM2());
                existente.setDemandaUnitariaKva(f.demandaKva());
                existente.setNormaOrigem(norma);
                areaRangeRepository.save(existente);
            }, () -> areaRangeRepository.save(
                    new AreaRangeReference(f.codigo(), f.rotulo(), f.minM2(), f.maxM2(), f.demandaKva(), norma)));
        }
    }

    /**
     * Quadro 36 (Fc por número de apartamentos), tabela discreta 1..60 ("60 ou +"). Upsert pela chave
     * {@code totalApartamentos}. Mesma política de não apagar linhas obsoletas.
     */
    private void seedCoincidenceFactors() {
        String norma = TabelasNormativas.NORMA_QUADRO_36;
        for (TabelasNormativas.FatorCoincidenciaLinha linha : TabelasNormativas.linhasQuadro36()) {
            BigDecimal percentual = linha.fator().movePointRight(2).setScale(2, RoundingMode.HALF_UP);
            coincidenceFactorRepository.findByTotalApartamentos(linha.totalApartamentos()).ifPresentOrElse(existente -> {
                existente.setFatorCoincidencia(linha.fator());
                existente.setFatorCoincidenciaPercent(percentual);
                existente.setNormaOrigem(norma);
                coincidenceFactorRepository.save(existente);
            }, () -> coincidenceFactorRepository.save(
                    new CoincidenceFactorReference(linha.totalApartamentos(), linha.fator(), percentual, norma)));
        }
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
