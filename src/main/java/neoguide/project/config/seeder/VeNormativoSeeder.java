package neoguide.project.config.seeder;

import neoguide.project.engine.normativo.TabelasNormativas;
import neoguide.project.model.normativo.VeFactorReference;
import neoguide.project.repository.normativo.VeFactorReferenceRepository;
import org.springframework.stereotype.Component;

/**
 * Quadro 33 (DIS-NOR-053 Rev. 06): fator de demanda de recarga de VE por agrupamento.
 * Upsert pela chave {@code maxVagas}. Linhas antigas sem correspondência (1, 2, 4, 8, 12) não são apagadas
 * aqui: apagar dados exige autorização (ver docs/AUDITORIA-INTEGRACAO.md, pergunta P15).
 */
@Component
public class VeNormativoSeeder {

    private final VeFactorReferenceRepository veFactorRepository;

    public VeNormativoSeeder(VeFactorReferenceRepository veFactorRepository) {
        this.veFactorRepository = veFactorRepository;
    }

    public void seed() {
        String norma = TabelasNormativas.NORMA_QUADRO_33;
        for (TabelasNormativas.FatorVe f : TabelasNormativas.QUADRO_33) {
            veFactorRepository.findByMaxVagas(f.maxVagas()).ifPresentOrElse(existente -> {
                existente.setFatorSimultaneidade(f.fator());
                existente.setFaixaDescricao(f.descricao());
                existente.setNormaOrigem(norma);
                veFactorRepository.save(existente);
            }, () -> veFactorRepository.save(
                    new VeFactorReference(f.maxVagas(), f.fator(), f.descricao(), norma)));
        }
    }
}
