package neoguide.project.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

import neoguide.project.dto.CommonAreaLoadItemRequest;
import neoguide.project.dto.simulation.CommercialUnitItemDto;
import neoguide.project.dto.simulation.CommonAreaLoadItemDto;
import neoguide.project.dto.simulation.PreCalculationRequest;
import neoguide.project.dto.simulation.PreCalculationResponse;
import neoguide.project.dto.simulation.ResidentialTypologyItemDto;
import neoguide.project.dto.simulation.SpecialLoadsItemDto;
import neoguide.project.engine.CommercialCalculationEngine;
import neoguide.project.engine.CommonAreaCalculationEngine;
import neoguide.project.engine.ConsolidatedCalculationEngine;
import neoguide.project.engine.ResidentialCalculationEngine;
import neoguide.project.engine.SpecialLoadsCalculationEngine;
import neoguide.project.engine.enums.SupplyVoltageLevel;
import neoguide.project.engine.enums.VoltageSystem;
import neoguide.project.engine.model.BusbarDimensioning;
import neoguide.project.engine.model.CommercialCalculationInput;
import neoguide.project.engine.model.CommercialCalculationResult;
import neoguide.project.engine.model.CommercialUnitInput;
import neoguide.project.engine.model.ConsolidatedCalculationInput;
import neoguide.project.engine.model.ConsolidatedCalculationResult;
import neoguide.project.engine.model.ElectricVehicleLoadInput;
import neoguide.project.engine.model.FirePumpLoadInput;
import neoguide.project.engine.model.ResidentialCalculationResult;
import neoguide.project.engine.model.ResidentialTypologyItem;
import neoguide.project.engine.model.ServiceEntranceDimensioning;
import neoguide.project.engine.model.SpecialLoadsCalculationInput;
import neoguide.project.engine.model.SpecialLoadsCalculationResult;
import neoguide.project.engine.model.commonarea.CommonAreaCalculationResult;

@Service
public class PreCalculationService {

    private final ResidentialCalculationEngine residentialEngine;
    private final CommonAreaCalculationEngine commonAreaEngine;
    private final CommercialCalculationEngine commercialEngine;
    private final SpecialLoadsCalculationEngine specialLoadsEngine;
    private final ConsolidatedCalculationEngine consolidatedEngine;

    public PreCalculationService(
        ResidentialCalculationEngine residentialEngine,
        CommonAreaCalculationEngine commonAreaEngine,
        CommercialCalculationEngine commercialEngine,
        SpecialLoadsCalculationEngine specialLoadsEngine,
        ConsolidatedCalculationEngine consolidatedEngine
    ) {
        this.residentialEngine = residentialEngine;
        this.commonAreaEngine = commonAreaEngine;
        this.commercialEngine = commercialEngine;
        this.specialLoadsEngine = specialLoadsEngine;
        this.consolidatedEngine = consolidatedEngine;
    }

    public PreCalculationResponse preCalcular(PreCalculationRequest request) {
        long startNanos = System.nanoTime();

        if (request == null) {
            request = new PreCalculationRequest("380_220V", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), SpecialLoadsItemDto.empty());
        }

        // 1. Processar Motor Residencial (D_rf)
        BigDecimal drKva = BigDecimal.ZERO;
        BigDecimal fc = BigDecimal.ONE;
        BigDecimal fr = BigDecimal.ONE;
        BigDecimal drfKva = BigDecimal.ZERO;

        if (request.tipologias() != null && !request.tipologias().isEmpty()) {
            List<ResidentialTypologyItem> items = request.tipologias().stream()
                .map(t -> new ResidentialTypologyItem(
                    t.nome(),
                    t.areaUtilM2(),
                    t.quantidadeUnidades() != null ? t.quantidadeUnidades() : 0,
                    null,
                    Boolean.TRUE.equals(t.isSmartStudio())
                ))
                .toList();
            boolean isSmart = request.tipologias().stream()
                .anyMatch(t -> Boolean.TRUE.equals(t.isSmartStudio()));

            ResidentialCalculationResult resResult = residentialEngine.calculate(items, isSmart);
            drKva = resResult.demandaBrutaDr();
            fc = resResult.fatorCoincidenciaFc();
            fr = resResult.fatorSegurancaFr();
            drfKva = resResult.demandaFinalDrf();
        }

        // 2. Processar Motor de Áreas Comuns (D_s)
        BigDecimal dsKva = BigDecimal.ZERO;
        if (request.cargasServicos() != null && !request.cargasServicos().isEmpty()) {
            List<CommonAreaLoadItemRequest> cargas = request.cargasServicos().stream()
                .map(c -> {
                    CommonAreaLoadItemRequest r = new CommonAreaLoadItemRequest();
                    r.setOrigem(c.origem());
                    r.setSubmodulo(c.submodulo());
                    r.setTipoEquipamento(c.tipoEquipamento());
                    r.setDescricao(c.tipoEquipamento());
                    r.setPotenciaNominalKw(c.potenciaNominalKw());
                    r.setQuantidade(c.quantidade() != null ? c.quantidade() : 1);
                    r.setFatorPotencia(c.fatorPotencia());
                    r.setFatorDemanda(c.fatorDemanda());
                    r.setIsReserva(c.isReserva());
                    r.setCatalogoMotorId(c.catalogoMotorId());
                    r.setCatalogoClimaId(c.catalogoClimaId());
                    r.setCatalogoEquipamentoId(c.catalogoEquipamentoId());
                    return r;
                })
                .toList();

            CommonAreaCalculationResult comAreaResult = commonAreaEngine.calculate(cargas);
            dsKva = comAreaResult.demandaTotalDsKva();
        }

        // 3. Processar Motor Comercial (D_c)
        BigDecimal dcKva = BigDecimal.ZERO;
        if (request.unidadesComerciais() != null && !request.unidadesComerciais().isEmpty()) {
            List<CommercialUnitInput> comUnits = request.unidadesComerciais().stream()
                .map(u -> new CommercialUnitInput(
                    u.identificacao(),
                    u.atividadeComercial(),
                    u.areaM2(),
                    u.potenciaInstaladaKw()
                ))
                .toList();

            CommercialCalculationResult comResult = commercialEngine.calcularDemandaComercial(
                new CommercialCalculationInput(true, comUnits)
            );
            dcKva = comResult.demandaComercialDcKva();
        }

        // 4. Processar Motor de Cargas Especiais (D_ve e D_mbi)
        BigDecimal dveKva = BigDecimal.ZERO;
        BigDecimal dmbiKva = BigDecimal.ZERO;
        SpecialLoadsItemDto specDto = request.cargasEspeciais();

        if (specDto != null && ((specDto.qtdPontosVe() != null && specDto.qtdPontosVe() > 0)
                || (specDto.potenciaBombaIncendioCv() != null && specDto.potenciaBombaIncendioCv().compareTo(BigDecimal.ZERO) > 0))) {
            ElectricVehicleLoadInput veInput = ElectricVehicleLoadInput.padrao(
                specDto.qtdPontosVe() != null ? specDto.qtdPontosVe() : 0,
                specDto.potenciaVeKw() != null ? specDto.potenciaVeKw() : BigDecimal.ZERO
            );
            FirePumpLoadInput fireInput = FirePumpLoadInput.padrao(
                specDto.potenciaBombaIncendioCv() != null ? specDto.potenciaBombaIncendioCv() : BigDecimal.ZERO
            );

            SpecialLoadsCalculationResult specResult = specialLoadsEngine.calculate(
                new SpecialLoadsCalculationInput(veInput, fireInput, null, dsKva)
            );
            dveKva = specResult.recargaVeicular() != null ? specResult.recargaVeicular().demandaDveKva() : BigDecimal.ZERO;
            dmbiKva = specResult.motobombaIncendio() != null ? specResult.motobombaIncendio().demandaDmbiKva() : BigDecimal.ZERO;
        }

        // 5. Orquestrar Motor Mestre Consolidado e Dimensionamento de Entrada (D_ed e D_QDG)
        VoltageSystem voltSystem = VoltageSystem.fromString(request.tensaoFornecimento());
        BigDecimal dedKva = drfKva.add(dsKva).add(dcKva).add(dveKva).add(dmbiKva).setScale(2, RoundingMode.HALF_UP);
        BigDecimal dqdgKva = dedKva.subtract(dmbiKva).setScale(2, RoundingMode.HALF_UP);

        String faixaAtendimento = dedKva.compareTo(new BigDecimal("300.00")) <= 0
            ? "BAIXA_TENSAO_SECUNDARIA_380V"
            : "MEDIA_TENSAO_PRIMARIA_13_8KV";

        Integer disjuntorGeralA = null;
        String caboFase = null;
        String caboNeutro = null;
        String barramentoQdg = null;
        Integer capacidadeBarramentoA = null;

        if (dedKva.compareTo(BigDecimal.ZERO) > 0) {
            ConsolidatedCalculationInput consolidatedInput = new ConsolidatedCalculationInput(
                drfKva, dsKva, dcKva, dveKva, dmbiKva, voltSystem, null
            );
            ConsolidatedCalculationResult consolidatedResult = consolidatedEngine.calculate(consolidatedInput);

            faixaAtendimento = consolidatedResult.nivelFornecimento() == SupplyVoltageLevel.BAIXA_TENSAO_SECUNDARIA
                ? (voltSystem == VoltageSystem.TRIFASICO_380_220V ? "BAIXA_TENSAO_SECUNDARIA_380V" : "BAIXA_TENSAO_SECUNDARIA_220V")
                : "MEDIA_TENSAO_PRIMARIA_13_8KV";

            ServiceEntranceDimensioning pe = consolidatedResult.padraoEntrada();
            if (pe != null) {
                disjuntorGeralA = pe.disjuntorCorrenteInA();
                caboFase = pe.secaoCondutorFase();
                caboNeutro = pe.secaoCondutorNeutro();
            }

            BusbarDimensioning bb = consolidatedResult.barramentoQdg();
            if (bb != null) {
                barramentoQdg = bb.barraDimensaoMm() + " (" + bb.barraDimensaoPol() + ")";
                capacidadeBarramentoA = bb.correnteNominalA();
            }
        }

        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
        if (elapsedMs == 0) elapsedMs = 1;

        // 6. Alertas normativos preliminares
        List<String> alertas = new ArrayList<>();
        String alertaPrincipal = null;

        if (dedKva.compareTo(new BigDecimal("300.00")) > 0) {
            alertaPrincipal = "ALERTA: Demanda Global acima de 300 kVA exige atendimento em Média Tensão Primária (13,8 kV) com Subestação Própria.";
            alertas.add(alertaPrincipal);
        } else if (dedKva.compareTo(BigDecimal.ZERO) > 0 && dedKva.compareTo(new BigDecimal("50.00")) < 0) {
            alertas.add("Edificação de uso coletivo com demanda abaixo de 50 kVA possui regime de medição agrupada simplificada.");
        }

        if (dmbiKva.compareTo(BigDecimal.ZERO) > 0) {
            alertas.add("A motobomba de incêndio (" + dmbiKva.setScale(2, RoundingMode.HALF_UP) + " kVA) foi deduzida da barra do QDG (D_QDG = " + dqdgKva.setScale(2, RoundingMode.HALF_UP) + " kVA) por segurança contra desarmes acidentais (DIS-NOR-053 Item 6.2).");
        }

        return new PreCalculationResponse(
            elapsedMs,
            drKva.setScale(2, RoundingMode.HALF_UP),
            fc.setScale(4, RoundingMode.HALF_UP),
            fr.setScale(2, RoundingMode.HALF_UP),
            drfKva.setScale(2, RoundingMode.HALF_UP),
            dsKva.setScale(2, RoundingMode.HALF_UP),
            dcKva.setScale(2, RoundingMode.HALF_UP),
            dveKva.setScale(2, RoundingMode.HALF_UP),
            dmbiKva.setScale(2, RoundingMode.HALF_UP),
            dedKva.setScale(2, RoundingMode.HALF_UP),
            dqdgKva.setScale(2, RoundingMode.HALF_UP),
            faixaAtendimento,
            disjuntorGeralA,
            caboFase,
            caboNeutro,
            barramentoQdg,
            capacidadeBarramentoA,
            alertaPrincipal,
            alertas
        );
    }
}
