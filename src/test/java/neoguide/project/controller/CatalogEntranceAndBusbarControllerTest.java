package neoguide.project.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogEntranceAndBusbarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/catalogo/dimensionamento-entrada/380v deve retornar 200 OK com 10 faixas oficiais públicas")
    void shouldReturnEntrance380vCatalogPublicly() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/dimensionamento-entrada/380v")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].maxDemandaKva").value(46.0))
                .andExpect(jsonPath("$[0].condutorMm2").value("16 mm²"))
                .andExpect(jsonPath("$[0].disjuntorIn").value(70))
                .andExpect(jsonPath("$[0].tensaoFornecimento").value("380_220V"))
                .andExpect(jsonPath("$[9].maxDemandaKva").value(300.0))
                .andExpect(jsonPath("$[9].circuitos").value(2))
                .andExpect(jsonPath("$[9].condutorMm2").value("2 x 185 mm²"))
                .andExpect(jsonPath("$[9].disjuntorIn").value(450));
    }

    @Test
    @DisplayName("GET /api/v1/catalogo/dimensionamento-entrada/220v deve retornar 200 OK com 10 faixas oficiais públicas")
    void shouldReturnEntrance220vCatalogPublicly() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/dimensionamento-entrada/220v")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].maxDemandaKva").value(26.0))
                .andExpect(jsonPath("$[0].condutorMm2").value("16 mm²"))
                .andExpect(jsonPath("$[0].disjuntorIn").value(70))
                .andExpect(jsonPath("$[0].tensaoFornecimento").value("220_127V"))
                .andExpect(jsonPath("$[9].maxDemandaKva").value(173.0))
                .andExpect(jsonPath("$[9].circuitos").value(2))
                .andExpect(jsonPath("$[9].condutorMm2").value("2 x 185 mm²"))
                .andExpect(jsonPath("$[9].disjuntorIn").value(450));
    }

    @Test
    @DisplayName("GET /api/v1/catalogo/barramentos-qdg deve retornar 200 OK com 12 faixas oficiais públicas")
    void shouldReturnBusbarCatalogPublicly() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/barramentos-qdg")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(12))
                .andExpect(jsonPath("$[0].maxDemandaKva").value(40.0))
                .andExpect(jsonPath("$[0].barraMm").value("19 x 3"))
                .andExpect(jsonPath("$[0].barraPol").value("3/4\" x 1/8\""))
                .andExpect(jsonPath("$[0].correnteNominalA").value(208))
                .andExpect(jsonPath("$[11].maxDemandaKva").value(1211.0))
                .andExpect(jsonPath("$[11].barraMm").value("120 x 10"))
                .andExpect(jsonPath("$[11].correnteNominalA").value(3281));
    }
}
