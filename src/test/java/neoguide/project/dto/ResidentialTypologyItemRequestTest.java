package neoguide.project.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ResidentialTypologyItemRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Desserializacao com campo isSmartStudio omitido deve assumir false sem lancar excecao")
    void deserializacao_campoOmitido_deveAssumirFalse() throws Exception {
        String json = """
                {
                  "areaUtilM2": 65.00,
                  "quantidadeUnidades": 20
                }
                """;

        ResidentialTypologyItemRequest request = objectMapper.readValue(json, ResidentialTypologyItemRequest.class);

        assertNotNull(request);
        assertEquals(new BigDecimal("65.00"), request.getAreaUtilM2());
        assertEquals(20, request.getQuantidadeUnidades());
        assertFalse(request.isSmartStudio());
    }

    @Test
    @DisplayName("Desserializacao com isSmartStudio explicito deve respeitar valor")
    void deserializacao_isSmartStudioExplicito_deveRespeitarValor() throws Exception {
        String jsonTrue = """
                {
                  "areaUtilM2": 32.50,
                  "quantidadeUnidades": 15,
                  "isSmartStudio": true
                }
                """;

        ResidentialTypologyItemRequest reqTrue = objectMapper.readValue(jsonTrue, ResidentialTypologyItemRequest.class);
        assertTrue(reqTrue.isSmartStudio());

        String jsonFalse = """
                {
                  "areaUtilM2": 32.50,
                  "quantidadeUnidades": 15,
                  "isSmartStudio": false
                }
                """;

        ResidentialTypologyItemRequest reqFalse = objectMapper.readValue(jsonFalse, ResidentialTypologyItemRequest.class);
        assertFalse(reqFalse.isSmartStudio());
    }

    @Test
    @DisplayName("Desserializacao com alias smartStudio deve respeitar valor")
    void deserializacao_smartStudioAlias_deveRespeitarValor() throws Exception {
        String json = """
                {
                  "areaUtilM2": 30.00,
                  "quantidadeUnidades": 10,
                  "smartStudio": true
                }
                """;

        ResidentialTypologyItemRequest req = objectMapper.readValue(json, ResidentialTypologyItemRequest.class);
        assertTrue(req.isSmartStudio());
    }

    @Test
    @DisplayName("Construtores parametrizados devem garantir compatibilidade e defaults")
    void construtores_devemGarantirCompatibilidade() {
        ResidentialTypologyItemRequest itemPadrao = new ResidentialTypologyItemRequest(new BigDecimal("50.00"), 10);
        assertFalse(itemPadrao.isSmartStudio());

        ResidentialTypologyItemRequest itemComNull = new ResidentialTypologyItemRequest(new BigDecimal("50.00"), 10, null);
        assertFalse(itemComNull.isSmartStudio());

        ResidentialTypologyItemRequest itemComTrue = new ResidentialTypologyItemRequest(new BigDecimal("50.00"), 10, true);
        assertTrue(itemComTrue.isSmartStudio());
    }
}
