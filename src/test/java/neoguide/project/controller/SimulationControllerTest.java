package neoguide.project.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/v1/simulacao/pre-calculo-etapa deve retornar 200 OK com resultados de pre-calculo em RAM")
    void devePreCalcularComSucessoNaRotaPadrao() throws Exception {
        String payload = """
        {
          "tensaoFornecimento": "380_220V",
          "tipologias": [
            {
              "nome": "Tipo A",
              "areaUtilM2": 65.00,
              "quantidadeUnidades": 40,
              "isSmartStudio": false
            }
          ],
          "cargasServicos": [
            {
              "origem": "PERSONALIZADO",
              "submodulo": "ILUMINACAO_TUG",
              "tipoEquipamento": "Lâmpadas LED Garagem",
              "potenciaNominalKw": 5.00,
              "quantidade": 1,
              "fatorPotencia": 0.90,
              "fatorDemanda": 1.00
            }
          ],
          "unidadesComerciais": [
            {
              "identificacao": "Loja 1",
              "atividadeComercial": "PADARIA",
              "areaM2": 80.00,
              "potenciaInstaladaKw": 15.00
            }
          ],
          "cargasEspeciais": {
            "qtdPontosVe": 2,
            "potenciaVeKw": 7.40,
            "potenciaBombaIncendioCv": 2.00
          }
        }
        """;

        mockMvc.perform(post("/api/v1/simulacao/pre-calculo-etapa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tempoProcessamentoMs", notNullValue()))
                .andExpect(jsonPath("$.demandaGlobalDedKva", greaterThanOrEqualTo(0.0)))
                .andExpect(jsonPath("$.demandaQdgKva", greaterThanOrEqualTo(0.0)))
                .andExpect(jsonPath("$.disjuntorGeralSugeridoA", notNullValue()))
                .andExpect(jsonPath("$.caboFaseSugeridoMm2", notNullValue()))
                .andExpect(jsonPath("$.barramentoQdgSugeridoMm", notNullValue()))
                .andExpect(jsonPath("$.faixaAtendimento", notNullValue()));
    }

    @Test
    @DisplayName("POST /simulacao/pre-calculo-etapa deve responder no alias com 200 OK")
    void devePreCalcularComSucessoNoAlias() throws Exception {
        String payload = """
        {
          "tensaoFornecimento": "380_220V",
          "tipologias": [],
          "cargasServicos": [],
          "unidadesComerciais": [],
          "cargasEspeciais": {
            "qtdPontosVe": 0,
            "potenciaVeKw": 0.0,
            "potenciaBombaIncendioCv": 0.0
          }
        }
        """;

        mockMvc.perform(post("/simulacao/pre-calculo-etapa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tempoProcessamentoMs", notNullValue()))
                .andExpect(jsonPath("$.demandaGlobalDedKva").value(0.0))
                .andExpect(jsonPath("$.demandaQdgKva").value(0.0));
    }
}
