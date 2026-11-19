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
class CatalogSpecialtiesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("GET /api/v1/catalogo/motores-monofasicos deve retornar 200 OK com 11 registros ordenados por potencia")
    void deveRetornarMotoresMonofasicosOrdenados() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/motores-monofasicos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(11))
                .andExpect(jsonPath("$[0].potenciaCv").value("1/4"))
                .andExpect(jsonPath("$[0].potenciaW").value(420.00))
                .andExpect(jsonPath("$[0].fatorPotencia").value(0.63))
                .andExpect(jsonPath("$[10].potenciaCv").value("10.0"))
                .andExpect(jsonPath("$[10].potenciaVa").value(11640.00));
    }

    @Test
    @DisplayName("GET /api/v1/catalogo/dispositivos-partida-motores deve retornar 200 OK com 4 chaves da Tabela 17")
    void deveRetornarDispositivosPartida() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/dispositivos-partida-motores")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].tipoPartida").value("DIRETA"))
                .andExpect(jsonPath("$[0].maxCv220v").value(5.00))
                .andExpect(jsonPath("$[0].maxCv380v").value(7.50))
                .andExpect(jsonPath("$[1].tipoPartida").value("ESTRELA_TRIANGULO"))
                .andExpect(jsonPath("$[2].tipoPartida").value("COMPENSADORA"))
                .andExpect(jsonPath("$[3].tipoPartida").value("SOFT_STARTER"));
    }

    @Test
    @DisplayName("GET /api/v1/catalogo/tomadas-minimas-residenciais deve retornar 200 OK com 7 faixas da Tabela 5")
    void deveRetornarTomadasMinimas() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/tomadas-minimas-residenciais")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(7))
                .andExpect(jsonPath("$[0].faixaDescricao").value("Até 30 m²"))
                .andExpect(jsonPath("$[0].minTug100w").value(3))
                .andExpect(jsonPath("$[0].minTug600w").value(2))
                .andExpect(jsonPath("$[6].faixaDescricao").value("Acima de 120 m²"))
                .andExpect(jsonPath("$[6].minTug100w").value(15))
                .andExpect(jsonPath("$[6].minTug600w").value(6));
    }

    @Test
    @DisplayName("Endpoints de catálogo de especialidades devem permitir acesso público (permitAll)")
    void devePermitirAcessoPublicoSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/motores-monofasicos"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/catalogo/dispositivos-partida-motores"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/catalogo/tomadas-minimas-residenciais"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Endpoints de catálogo de especialidades devem funcionar com Bearer token JWT")
    void devePermitirAcessoComTokenValido() throws Exception {
        User user = userRepository.save(new User("Projetista US16", "projetista.us16@teste.com", passwordEncoder.encode("Senha123")));
        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/v1/catalogo/motores-monofasicos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
