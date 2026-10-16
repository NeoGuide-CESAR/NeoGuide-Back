package neoguide.project.controller;

import neoguide.project.model.User;
import neoguide.project.repository.UserRepository;
import neoguide.project.security.jwt.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("GET /api/v1/catalogo/faixas-area deve retornar 200 OK com as 13 faixas do Quadro 35 ordenadas")
    void deveRetornarFaixasAreaComSucesso() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/faixas-area")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(13))
                .andExpect(jsonPath("$[0].faixa").value("FAIXA_ATE_40"))
                .andExpect(jsonPath("$[0].rotulo").value("Até 40 m²"))
                .andExpect(jsonPath("$[0].minAreaM2").value(0.0))
                .andExpect(jsonPath("$[0].maxAreaM2").value(40.0))
                .andExpect(jsonPath("$[0].demandaUnitariakVa").value(1.2))
                .andExpect(jsonPath("$[0].normaOrigem").value("DIS-NOR-053 Quadro 35"))
                .andExpect(jsonPath("$[12].faixa").value("FAIXA_ACIMA_300"))
                .andExpect(jsonPath("$[12].demandaUnitariakVa").value(7.2));
    }

    @Test
    @DisplayName("GET /api/v1/catalogo/fatores-coincidencia-residencial deve retornar 200 OK com wrapper do Quadro 36")
    void deveRetornarFatoresCoincidenciaComWrapper() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/fatores-coincidencia-residencial")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.norma").value("DIS-NOR-053 Quadro 36"))
                .andExpect(jsonPath("$.regraEspecialStudios").value("Para edifícios tipo Smart/Studio acima de 15 unidades, adota-se 90% (0,90) fixo"))
                .andExpect(jsonPath("$.tabela").isArray())
                .andExpect(jsonPath("$.tabela.length()").value(11))
                .andExpect(jsonPath("$.tabela[0].totalApartamentos").value(10))
                .andExpect(jsonPath("$.tabela[0].fatorCoincidencia").value(0.9640))
                .andExpect(jsonPath("$.tabela[10].totalApartamentos").value(60))
                .andExpect(jsonPath("$.tabela[10].fatorCoincidencia").value(0.6823));
    }

    @Test
    @DisplayName("GET /api/v1/catalogo/fatores-seguranca-residencial deve retornar 200 OK com fatores do Quadro 37")
    void deveRetornarFatoresSegurancaComSucesso() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/fatores-seguranca-residencial")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].minDrKva").value(0.0))
                .andExpect(jsonPath("$[0].maxDrKva").value(25.0))
                .andExpect(jsonPath("$[0].fatorSegurancaFr").value(1.5))
                .andExpect(jsonPath("$[1].minDrKva").value(25.01))
                .andExpect(jsonPath("$[1].maxDrKva").value(50.0))
                .andExpect(jsonPath("$[1].fatorSegurancaFr").value(1.3))
                .andExpect(jsonPath("$[2].fatorSegurancaFr").value(1.2))
                .andExpect(jsonPath("$[3].fatorSegurancaFr").value(1.1));
    }

    @Test
    @DisplayName("Endpoints de catálogo devem permitir acesso público sem token JWT (permitAll)")
    void devePermitirAcessoPublicoSemToken() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/faixas-area"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/catalogo/fatores-coincidencia-residencial"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/catalogo/fatores-seguranca-residencial"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Endpoints de catálogo devem permitir acesso também quando autenticado com Bearer token")
    void devePermitirAcessoComTokenAutenticado() throws Exception {
        User user = new User("Engenheiro Catalogo", "catalogo@neoguide.com", passwordEncoder.encode("senha123"));
        user = userRepository.save(user);
        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/v1/catalogo/faixas-area")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
