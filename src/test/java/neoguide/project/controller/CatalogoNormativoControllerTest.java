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
class CatalogoNormativoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("GET /api/v1/catalogo/fatores-recarga-ve deve retornar 200 OK com 8 faixas ordenadas por maxVagas")
    void deveRetornarFatoresRecargaVeOrdenados() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/fatores-recarga-ve")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(8))
                .andExpect(jsonPath("$[0].maxVagas").value(1))
                .andExpect(jsonPath("$[0].fatorSimultaneidade").value(1.00))
                .andExpect(jsonPath("$[1].maxVagas").value(2))
                .andExpect(jsonPath("$[1].fatorSimultaneidade").value(0.90))
                .andExpect(jsonPath("$[2].maxVagas").value(4))
                .andExpect(jsonPath("$[2].fatorSimultaneidade").value(0.80))
                .andExpect(jsonPath("$[3].maxVagas").value(8))
                .andExpect(jsonPath("$[3].fatorSimultaneidade").value(0.70))
                .andExpect(jsonPath("$[4].maxVagas").value(12))
                .andExpect(jsonPath("$[4].fatorSimultaneidade").value(0.60))
                .andExpect(jsonPath("$[5].maxVagas").value(20))
                .andExpect(jsonPath("$[5].fatorSimultaneidade").value(0.50))
                .andExpect(jsonPath("$[6].maxVagas").value(30))
                .andExpect(jsonPath("$[6].fatorSimultaneidade").value(0.45))
                .andExpect(jsonPath("$[7].maxVagas").value(9999))
                .andExpect(jsonPath("$[7].fatorSimultaneidade").value(0.40));
    }

    @Test
    @DisplayName("GET /api/v1/catalogo/cargas-minimas-ocupacao deve retornar 200 OK com 6 atividades da Tabela 22")
    void deveRetornarCargasMinimasOcupacao() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/cargas-minimas-ocupacao")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[?(@.ocupacao == 'LOJAS')].cargaMinimaWm2").value(hasItem(30.00)))
                .andExpect(jsonPath("$[?(@.ocupacao == 'LOJAS')].vitrineWMetro").value(hasItem(700.00)))
                .andExpect(jsonPath("$[?(@.ocupacao == 'LOJAS')].fatorPotencia").value(hasItem(0.92)))
                .andExpect(jsonPath("$[?(@.ocupacao == 'ESCRITORIOS')].cargaMinimaWm2").value(hasItem(30.00)))
                .andExpect(jsonPath("$[?(@.ocupacao == 'ADMINISTRACAO_EDIFICIO')].cargaMinimaWm2").value(hasItem(5.00)));
    }

    @Test
    @DisplayName("GET /api/v1/catalogo/fatores-bombas-piscina deve retornar 200 OK com 4 registros da Tabela 16")
    void deveRetornarFatoresBombasPiscina() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/fatores-bombas-piscina")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].quantidadeBombas").value(1))
                .andExpect(jsonPath("$[0].fatorDemanda").value(1.00))
                .andExpect(jsonPath("$[0].fatorPotencia").value(0.80))
                .andExpect(jsonPath("$[1].quantidadeBombas").value(2))
                .andExpect(jsonPath("$[1].fatorDemanda").value(0.56))
                .andExpect(jsonPath("$[2].quantidadeBombas").value(3))
                .andExpect(jsonPath("$[2].fatorDemanda").value(0.47))
                .andExpect(jsonPath("$[3].quantidadeBombas").value(4))
                .andExpect(jsonPath("$[3].fatorDemanda").value(0.39));
    }

    @Test
    @DisplayName("Endpoints de catálogo normativo de cargas especiais e ocupação devem permitir acesso público (permitAll)")
    void devePermitirAcessoPublicoSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/fatores-recarga-ve"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/catalogo/cargas-minimas-ocupacao"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/catalogo/fatores-bombas-piscina"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Endpoints de catálogo de cargas especiais e ocupação devem funcionar com Bearer token JWT")
    void devePermitirAcessoComTokenValido() throws Exception {
        User user = userRepository.save(new User("Projetista US15", "projetista.us15@teste.com", passwordEncoder.encode("Senha123")));
        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/v1/catalogo/fatores-recarga-ve")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
