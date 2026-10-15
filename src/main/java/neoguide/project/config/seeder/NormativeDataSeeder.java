package neoguide.project.config.seeder;

import org.springframework.stereotype.Component;

@Component
public class NormativeDataSeeder {

    private final ResidencialNormativoSeeder residencialSeeder;
    private final EntradaBarramentoNormativoSeeder entradaBarramentoSeeder;
    private final EquipamentosClimaCargasNormativoSeeder equipamentosClimaCargasSeeder;
    private final MotoresPartidasNormativoSeeder motoresPartidasSeeder;
    private final VeNormativoSeeder veSeeder;

    public NormativeDataSeeder(ResidencialNormativoSeeder residencialSeeder,
                               EntradaBarramentoNormativoSeeder entradaBarramentoSeeder,
                               EquipamentosClimaCargasNormativoSeeder equipamentosClimaCargasSeeder,
                               MotoresPartidasNormativoSeeder motoresPartidasSeeder,
                               VeNormativoSeeder veSeeder) {
        this.residencialSeeder = residencialSeeder;
        this.entradaBarramentoSeeder = entradaBarramentoSeeder;
        this.equipamentosClimaCargasSeeder = equipamentosClimaCargasSeeder;
        this.motoresPartidasSeeder = motoresPartidasSeeder;
        this.veSeeder = veSeeder;
    }

    public void seedAll() {
        residencialSeeder.seed();
        entradaBarramentoSeeder.seed();
        equipamentosClimaCargasSeeder.seed();
        motoresPartidasSeeder.seed();
        veSeeder.seed();
    }
}
