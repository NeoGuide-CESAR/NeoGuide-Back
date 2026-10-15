package neoguide.project.config.seeder;

import neoguide.project.model.normativo.VeFactorReference;
import neoguide.project.repository.normativo.VeFactorReferenceRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class VeNormativoSeeder {

    private final VeFactorReferenceRepository veFactorRepository;

    public VeNormativoSeeder(VeFactorReferenceRepository veFactorRepository) {
        this.veFactorRepository = veFactorRepository;
    }

    public void seed() {
        if (veFactorRepository.count() > 0) return;

        String norma = "DIS-NOR-053 Quadro 33";
        List<VeFactorReference> factors = List.of(
                new VeFactorReference(1, new BigDecimal("1.00"), "1 vaga", norma),
                new VeFactorReference(2, new BigDecimal("0.90"), "2 vagas", norma),
                new VeFactorReference(4, new BigDecimal("0.80"), "3 a 4 vagas", norma),
                new VeFactorReference(8, new BigDecimal("0.70"), "5 a 8 vagas", norma),
                new VeFactorReference(12, new BigDecimal("0.60"), "9 a 12 vagas", norma),
                new VeFactorReference(20, new BigDecimal("0.50"), "13 a 20 vagas", norma),
                new VeFactorReference(30, new BigDecimal("0.45"), "21 a 30 vagas", norma),
                new VeFactorReference(9999, new BigDecimal("0.40"), "Mais de 30 vagas", norma)
        );
        veFactorRepository.saveAll(factors);
    }
}
