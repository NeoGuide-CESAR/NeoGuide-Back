package neoguide.project.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** B-EP-23: GET /api/v1/avisos responde 200 com lista (vazia enquanto não houver aviso publicado). */
class AvisoControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AvisoController()).build();

    @Test
    @DisplayName("GET /api/v1/avisos deve retornar 200 com lista vazia quando não há aviso publicado")
    void deveRetornarListaVazia() throws Exception {
        mockMvc.perform(get("/api/v1/avisos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
