package neoguide.project.service;

import neoguide.project.dto.EspacoComercialRequest;
import neoguide.project.engine.CommercialCalculationEngine;
import neoguide.project.engine.model.CommercialCalculationResult;
import neoguide.project.exception.ValidationException;
import neoguide.project.model.normativo.OccupancyLoadReference;
import neoguide.project.repository.normativo.OccupancyLoadReferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CommercialStageServiceTest {

    @Mock
    private OccupancyLoadReferenceRepository ocupacoes;

    private CommercialStageService service;

    @BeforeEach
    void setUp() {
        lenient().when(ocupacoes.findByOcupacaoIn(anyCollection())).thenAnswer(invocacao -> {
            List<OccupancyLoadReference> encontradas = new ArrayList<>();
            for (Object codigo : (java.util.Collection<?>) invocacao.getArgument(0)) {
                if ("LOJAS".equals(codigo)) encontradas.add(loja());
                if ("ESCRITORIOS".equals(codigo)) encontradas.add(escritorio());
            }
            return encontradas;
        });
        service = new CommercialStageService(ocupacoes, new CommercialCalculationEngine());
    }

    private static OccupancyLoadReference loja() {
        return new OccupancyLoadReference("LOJAS", "Lojas e Vitrines", new BigDecimal("30.00"), new BigDecimal("700.00"),
                new BigDecimal("1.00"), new BigDecimal("1.00"), new BigDecimal("0.92"), "regra", "DIS-NOR-030 Tabela 22");
    }

    private static OccupancyLoadReference escritorio() {
        return new OccupancyLoadReference("ESCRITORIOS", "Escritórios e Salas Comerciais", new BigDecimal("30.00"), null,
                new BigDecimal("1.00"), new BigDecimal("0.70"), new BigDecimal("0.92"), "regra", "DIS-NOR-030 Tabela 22");
    }

    private static EspacoComercialRequest espaco(String codigo, String area, String carga) {
        return new EspacoComercialRequest(codigo, area == null ? null : new BigDecimal(area),
                carga == null ? null : new BigDecimal(carga));
    }

    @Test
    @DisplayName("Loja de 20 kW e escritório de 30 kW somam 51,09 kVA, com ocupações resolvidas pelo catálogo")
    void calcular_resolveCatalogoESomaEspacos() {
        CommercialCalculationResult resultado = service.calcular(List.of(
                espaco("LOJAS", "120", "20"), espaco("ESCRITORIOS", "85", "30")));
        assertEquals(new BigDecimal("51.09"), resultado.demandaComercialDcKva());
        assertEquals("Lojas e Vitrines", resultado.unidadesDetalhadas().get(0).rotulo());
    }

    @Test
    @DisplayName("Lista vazia é válida e resulta em Dc zero, sem consultar o catálogo")
    void calcular_listaVazia_dcZero() {
        CommercialCalculationResult resultado = service.calcular(List.of());
        assertEquals(new BigDecimal("0.00"), resultado.demandaComercialDcKva());
    }

    @Test
    @DisplayName("Ocupação fora da Tabela 22 gera erro no campo ocupacaoCodigo do espaço")
    void calcular_ocupacaoDesconhecida_erroNoCampo() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.calcular(List.of(espaco("BARBEARIA_SALAO", "40", "5"))));
        assertTrue(ex.getErrors().containsKey("espacos[0].ocupacaoCodigo"));
    }

    @Test
    @DisplayName("Área abaixo de 1,0 m² gera erro no campo areaM2")
    void validar_areaMenorQueMinimo_erroNoCampo() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.validarEspacos(List.of(espaco("LOJAS", "0.5", "5"))));
        assertTrue(ex.getErrors().containsKey("espacos[0].areaM2"));
    }

    @Test
    @DisplayName("Carga abaixo de 0,5 kW gera erro no campo cargaInstaladaKw")
    void validar_cargaMenorQueMinimo_erroNoCampo() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.validarEspacos(List.of(espaco("LOJAS", "20", "0.2"))));
        assertTrue(ex.getErrors().containsKey("espacos[0].cargaInstaladaKw"));
    }

    @Test
    @DisplayName("Mais de 100 espaços é recusado")
    void validar_acimaDoLimite_erro() {
        List<EspacoComercialRequest> muitos = new ArrayList<>();
        for (int i = 0; i < 101; i++) muitos.add(espaco("LOJAS", "10", "5"));
        ValidationException ex = assertThrows(ValidationException.class, () -> service.validarEspacos(muitos));
        assertTrue(ex.getErrors().containsKey("espacos"));
    }
}
