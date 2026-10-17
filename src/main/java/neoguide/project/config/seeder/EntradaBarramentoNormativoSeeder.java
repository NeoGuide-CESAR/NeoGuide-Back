package neoguide.project.config.seeder;

import neoguide.project.model.normativo.BusbarReference;
import neoguide.project.model.normativo.ServiceEntranceReference;
import neoguide.project.repository.normativo.BusbarReferenceRepository;
import neoguide.project.repository.normativo.ServiceEntranceReferenceRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class EntradaBarramentoNormativoSeeder {

    private final ServiceEntranceReferenceRepository serviceEntranceRepository;
    private final BusbarReferenceRepository busbarRepository;

    public EntradaBarramentoNormativoSeeder(ServiceEntranceReferenceRepository serviceEntranceRepository,
                                            BusbarReferenceRepository busbarRepository) {
        this.serviceEntranceRepository = serviceEntranceRepository;
        this.busbarRepository = busbarRepository;
    }

    public void seed() {
        seedServiceEntrance();
        seedBusbars();
    }

    private void seedServiceEntrance() {
        if (serviceEntranceRepository.count() > 0) return;

        String norma220 = "DIS-NOR-053 Anexo II Tabela 1";
        String norma380 = "DIS-NOR-053 Anexo II Tabela 2";

        List<ServiceEntranceReference> entrances = List.of(
                // Tabela 1 - 220/127 V
                new ServiceEntranceReference("220_127V", new BigDecimal("26.00"), 1, "16 mm²", "1 x 110", "1 x 100", 70, "160 A", 70, norma220),
                new ServiceEntranceReference("220_127V", new BigDecimal("30.00"), 1, "25 mm²", "1 x 110", "1 x 100", 80, "160 A", 80, norma220),
                new ServiceEntranceReference("220_127V", new BigDecimal("38.00"), 1, "35 mm²", "1 x 110", "1 x 100", 100, "160 A", 100, norma220),
                new ServiceEntranceReference("220_127V", new BigDecimal("47.00"), 1, "50 mm²", "1 x 110", "1 x 100", 125, "160 A", 125, norma220),
                new ServiceEntranceReference("220_127V", new BigDecimal("57.00"), 1, "70 mm²", "1 x 110", "1 x 100", 150, "160 A", 150, norma220),
                new ServiceEntranceReference("220_127V", new BigDecimal("76.00"), 1, "95 mm²", "1 x 110", "1 x 100", 200, "250 A", 200, norma220),
                new ServiceEntranceReference("220_127V", new BigDecimal("95.00"), 1, "150 mm²", "1 x 110", "1 x 100", 250, "250 A", 250, norma220),
                new ServiceEntranceReference("220_127V", new BigDecimal("122.00"), 1, "240 mm²", "1 x 110", "1 x 100", 320, "400 A", 320, norma220),
                new ServiceEntranceReference("220_127V", new BigDecimal("152.00"), 2, "2 x 150 mm²", "2 x 110", "2 x 100", 400, "400 A", 400, norma220),
                new ServiceEntranceReference("220_127V", new BigDecimal("173.00"), 2, "2 x 185 mm²", "2 x 110", "2 x 100", 450, "630 A", 450, norma220),

                // Tabela 2 - 380/220 V
                new ServiceEntranceReference("380_220V", new BigDecimal("46.00"), 1, "16 mm²", "1 x 110", "1 x 100", 70, "160 A", 70, norma380),
                new ServiceEntranceReference("380_220V", new BigDecimal("53.00"), 1, "25 mm²", "1 x 110", "1 x 100", 80, "160 A", 80, norma380),
                new ServiceEntranceReference("380_220V", new BigDecimal("66.00"), 1, "35 mm²", "1 x 110", "1 x 100", 100, "160 A", 100, norma380),
                new ServiceEntranceReference("380_220V", new BigDecimal("82.00"), 1, "50 mm²", "1 x 110", "1 x 100", 125, "160 A", 125, norma380),
                new ServiceEntranceReference("380_220V", new BigDecimal("99.00"), 1, "70 mm²", "1 x 110", "1 x 100", 150, "160 A", 150, norma380),
                new ServiceEntranceReference("380_220V", new BigDecimal("135.00"), 1, "95 mm²", "1 x 110", "1 x 100", 200, "250 A", 200, norma380),
                new ServiceEntranceReference("380_220V", new BigDecimal("165.00"), 1, "150 mm²", "1 x 110", "1 x 100", 250, "250 A", 250, norma380),
                new ServiceEntranceReference("380_220V", new BigDecimal("211.00"), 1, "240 mm²", "1 x 110", "1 x 100", 320, "400 A", 320, norma380),
                new ServiceEntranceReference("380_220V", new BigDecimal("263.00"), 2, "2 x 150 mm²", "2 x 110", "2 x 100", 400, "400 A", 400, norma380),
                new ServiceEntranceReference("380_220V", new BigDecimal("300.00"), 2, "2 x 185 mm²", "2 x 110", "2 x 100", 450, "630 A", 450, norma380)
        );
        serviceEntranceRepository.saveAll(entrances);
    }

    private void seedBusbars() {
        if (busbarRepository.count() > 0) return;

        String norma = "DIS-NOR-053 Anexo II Tabela 3";
        List<BusbarReference> busbars = List.of(
                new BusbarReference(new BigDecimal("40.00"), "19 x 3", "3/4\" x 1/8\"", 208, norma),
                new BusbarReference(new BigDecimal("74.00"), "25 x 3", "1\" x 1/8\"", 250, norma),
                new BusbarReference(new BigDecimal("104.00"), "25 x 5", "1\" x 3/16\"", 340, norma),
                new BusbarReference(new BigDecimal("207.00"), "30 x 5", "1 1/4\" x 3/16\"", 449, norma),
                new BusbarReference(new BigDecimal("259.00"), "40 x 5", "1 1/2\" x 3/16\"", 460, norma),
                new BusbarReference(new BigDecimal("346.00"), "50 x 5", "2\" x 3/16\"", 595, norma),
                new BusbarReference(new BigDecimal("432.00"), "40 x 10", null, 994, norma),
                new BusbarReference(new BigDecimal("519.00"), "50 x 10", null, 1312, norma),
                new BusbarReference(new BigDecimal("605.00"), "60 x 10", null, 1640, norma),
                new BusbarReference(new BigDecimal("778.00"), "80 x 10", null, 1968, norma),
                new BusbarReference(new BigDecimal("951.00"), "100 x 10", null, 2625, norma),
                new BusbarReference(new BigDecimal("1211.00"), "120 x 10", null, 3281, norma)
        );
        busbarRepository.saveAll(busbars);
    }
}
