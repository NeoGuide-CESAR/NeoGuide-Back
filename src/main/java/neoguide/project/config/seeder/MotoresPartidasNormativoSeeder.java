package neoguide.project.config.seeder;

import neoguide.project.model.normativo.*;
import neoguide.project.repository.normativo.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MotoresPartidasNormativoSeeder {

    private final MotorDemandFactorRepository motorDemandFactorRepository;
    private final SpecialEquipmentDemandFactorRepository specialEquipmentRepository;
    private final MotorStarterReferenceRepository motorStarterRepository;
    private final SinglePhaseMotorReferenceRepository singlePhaseMotorRepository;
    private final MotorReferenceRepository motorRepository;

    public MotoresPartidasNormativoSeeder(MotorDemandFactorRepository motorDemandFactorRepository,
                                         SpecialEquipmentDemandFactorRepository specialEquipmentRepository,
                                         MotorStarterReferenceRepository motorStarterRepository,
                                         SinglePhaseMotorReferenceRepository singlePhaseMotorRepository,
                                         MotorReferenceRepository motorRepository) {
        this.motorDemandFactorRepository = motorDemandFactorRepository;
        this.specialEquipmentRepository = specialEquipmentRepository;
        this.motorStarterRepository = motorStarterRepository;
        this.singlePhaseMotorRepository = singlePhaseMotorRepository;
        this.motorRepository = motorRepository;
    }

    public void seed() {
        seedMotorDemandFactors();
        seedSpecialEquipmentDemandFactors();
        seedMotorStarters();
        seedSinglePhaseMotors();
        seedThreePhaseMotors();
    }

    private void seedMotorDemandFactors() {
        if (motorDemandFactorRepository.count() > 0) return;

        String norma = "DIS-NOR-030 Anexo II Tabela 14";
        List<MotorDemandFactor> factors = List.of(
                new MotorDemandFactor("MAIOR_MOTOR", "Maior motor da instalação condominial", new BigDecimal("1.00"), norma),
                new MotorDemandFactor("DEMAIS_MOTORES", "Demais motores elétricos da instalação", new BigDecimal("0.50"), norma),
                new MotorDemandFactor("RESERVA_REVEZAMENTO", "Motor reserva em regime de revezamento/alternância", new BigDecimal("0.00"), norma)
        );
        motorDemandFactorRepository.saveAll(factors);
    }

    private void seedSpecialEquipmentDemandFactors() {
        if (specialEquipmentRepository.count() > 0) return;

        String norma = "DIS-NOR-030 Anexo II Tabela 15";
        BigDecimal fp = new BigDecimal("0.92");
        List<SpecialEquipmentDemandFactor> factors = List.of(
                new SpecialEquipmentDemandFactor("MAIOR_ELEVADOR", "Maior elevador ou equipamento especial", new BigDecimal("1.00"), fp, norma),
                new SpecialEquipmentDemandFactor("DEMAIS_ELEVADORES", "Demais elevadores e equipamentos especiais", new BigDecimal("0.60"), fp, norma)
        );
        specialEquipmentRepository.saveAll(factors);
    }

    private void seedMotorStarters() {
        if (motorStarterRepository.count() > 0) return;

        String norma = "DIS-NOR-030 Anexo II Tabela 17";
        List<MotorStarterReference> starters = List.of(
                new MotorStarterReference("DIRETA", null, new BigDecimal("5.00"), null, new BigDecimal("7.50"),
                        "Partida direta plena à rede", norma),
                new MotorStarterReference("ESTRELA_TRIANGULO", new BigDecimal("5.10"), new BigDecimal("15.00"), new BigDecimal("7.60"), new BigDecimal("25.00"),
                        "Chave estrela-triângulo manual ou automática", norma),
                new MotorStarterReference("COMPENSADORA", new BigDecimal("5.10"), new BigDecimal("15.00"), new BigDecimal("7.60"), new BigDecimal("25.00"),
                        "Chave compensadora com taps de 50%, 65% e 80%", norma),
                new MotorStarterReference("SOFT_STARTER", new BigDecimal("5.10"), new BigDecimal("60.00"), new BigDecimal("7.60"), new BigDecimal("60.00"),
                        "Chave eletrônica de estado sólido (Soft-Starter ou Inversor de Frequência)", norma)
        );
        motorStarterRepository.saveAll(starters);
    }

    private void seedSinglePhaseMotors() {
        if (singlePhaseMotorRepository.count() > 0) return;

        String norma = "DIS-NOR-030 Anexo II Tabela 18";
        List<SinglePhaseMotorReference> motors = List.of(
                new SinglePhaseMotorReference("1/4", new BigDecimal("0.25"), new BigDecimal("420.00"), new BigDecimal("660.00"), new BigDecimal("5.90"), new BigDecimal("3.00"), new BigDecimal("0.63"), norma),
                new SinglePhaseMotorReference("1/3", new BigDecimal("0.33"), new BigDecimal("510.00"), new BigDecimal("770.00"), new BigDecimal("7.10"), new BigDecimal("3.50"), new BigDecimal("0.66"), norma),
                new SinglePhaseMotorReference("1/2", new BigDecimal("0.50"), new BigDecimal("790.00"), new BigDecimal("1180.00"), new BigDecimal("11.60"), new BigDecimal("5.40"), new BigDecimal("0.67"), norma),
                new SinglePhaseMotorReference("3/4", new BigDecimal("0.75"), new BigDecimal("900.00"), new BigDecimal("1340.00"), new BigDecimal("12.20"), new BigDecimal("6.10"), new BigDecimal("0.67"), norma),
                new SinglePhaseMotorReference("1.0", new BigDecimal("1.00"), new BigDecimal("1140.00"), new BigDecimal("1560.00"), new BigDecimal("14.20"), new BigDecimal("7.10"), new BigDecimal("0.73"), norma),
                new SinglePhaseMotorReference("1.5", new BigDecimal("1.50"), new BigDecimal("1670.00"), new BigDecimal("2350.00"), new BigDecimal("21.40"), new BigDecimal("10.70"), new BigDecimal("0.71"), norma),
                new SinglePhaseMotorReference("2.0", new BigDecimal("2.00"), new BigDecimal("2170.00"), new BigDecimal("2970.00"), new BigDecimal("27.00"), new BigDecimal("13.50"), new BigDecimal("0.73"), norma),
                new SinglePhaseMotorReference("3.0", new BigDecimal("3.00"), new BigDecimal("3220.00"), new BigDecimal("4070.00"), new BigDecimal("37.00"), new BigDecimal("18.50"), new BigDecimal("0.79"), norma),
                new SinglePhaseMotorReference("5.0", new BigDecimal("5.00"), new BigDecimal("5110.00"), new BigDecimal("6160.00"), null, new BigDecimal("28.00"), new BigDecimal("0.83"), norma),
                new SinglePhaseMotorReference("7.5", new BigDecimal("7.50"), new BigDecimal("7070.00"), new BigDecimal("8840.00"), null, new BigDecimal("40.20"), new BigDecimal("0.80"), norma),
                new SinglePhaseMotorReference("10.0", new BigDecimal("10.00"), new BigDecimal("9310.00"), new BigDecimal("11640.00"), null, new BigDecimal("52.90"), new BigDecimal("0.80"), norma)
        );
        singlePhaseMotorRepository.saveAll(motors);
    }

    private void seedThreePhaseMotors() {
        if (motorRepository.count() > 0) return;

        String norma = "DIS-NOR-030 Anexo II Tabela 19";
        List<MotorReference> motors = List.of(
                new MotorReference(new BigDecimal("0.50"), new BigDecimal("0.37"), new BigDecimal("0.68"), new BigDecimal("0.72"), new BigDecimal("0.75"), new BigDecimal("1.10"), "DIRETA", norma),
                new MotorReference(new BigDecimal("0.75"), new BigDecimal("0.55"), new BigDecimal("0.71"), new BigDecimal("0.75"), new BigDecimal("1.03"), new BigDecimal("1.60"), "DIRETA", norma),
                new MotorReference(new BigDecimal("1.00"), new BigDecimal("0.74"), new BigDecimal("0.73"), new BigDecimal("0.77"), new BigDecimal("1.31"), new BigDecimal("2.00"), "DIRETA", norma),
                new MotorReference(new BigDecimal("1.50"), new BigDecimal("1.10"), new BigDecimal("0.75"), new BigDecimal("0.79"), new BigDecimal("1.86"), new BigDecimal("2.80"), "DIRETA", norma),
                new MotorReference(new BigDecimal("2.00"), new BigDecimal("1.47"), new BigDecimal("0.77"), new BigDecimal("0.81"), new BigDecimal("2.36"), new BigDecimal("3.60"), "DIRETA", norma),
                new MotorReference(new BigDecimal("3.00"), new BigDecimal("2.21"), new BigDecimal("0.79"), new BigDecimal("0.83"), new BigDecimal("3.37"), new BigDecimal("5.10"), "DIRETA", norma),
                new MotorReference(new BigDecimal("4.00"), new BigDecimal("2.94"), new BigDecimal("0.81"), new BigDecimal("0.84"), new BigDecimal("4.32"), new BigDecimal("6.60"), "DIRETA", norma),
                new MotorReference(new BigDecimal("5.00"), new BigDecimal("3.68"), new BigDecimal("0.82"), new BigDecimal("0.85"), new BigDecimal("5.27"), new BigDecimal("8.00"), "DIRETA", norma),
                new MotorReference(new BigDecimal("7.50"), new BigDecimal("5.52"), new BigDecimal("0.84"), new BigDecimal("0.86"), new BigDecimal("7.64"), new BigDecimal("11.60"), "DIRETA", norma),
                new MotorReference(new BigDecimal("10.00"), new BigDecimal("7.36"), new BigDecimal("0.85"), new BigDecimal("0.87"), new BigDecimal("9.95"), new BigDecimal("15.10"), "ESTRELA_TRIANGULO", norma),
                new MotorReference(new BigDecimal("12.50"), new BigDecimal("9.19"), new BigDecimal("0.86"), new BigDecimal("0.88"), new BigDecimal("12.14"), new BigDecimal("18.40"), "ESTRELA_TRIANGULO", norma),
                new MotorReference(new BigDecimal("15.00"), new BigDecimal("11.03"), new BigDecimal("0.87"), new BigDecimal("0.88"), new BigDecimal("14.40"), new BigDecimal("21.90"), "ESTRELA_TRIANGULO", norma)
        );
        motorRepository.saveAll(motors);
    }
}
