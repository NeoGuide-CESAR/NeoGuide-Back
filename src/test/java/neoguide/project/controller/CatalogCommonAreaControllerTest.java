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
class CatalogCommonAreaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("GET /api/v1/catalogo/motores-padrao deve retornar 200 OK com 12 potências padrão ordenadas")
    void deveRetornarMotoresPadraoOrdenados() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/motores-padrao")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(12))
                .andExpect(jsonPath("$[0].potenciaCv").value(0.50))
                .andExpect(jsonPath("$[0].demandaUnitariakVa").value(0.75))
                .andExpect(jsonPath("$[0].chavePartidaRecomendada").value("DIRETA"))
                .andExpect(jsonPath("$[11].potenciaCv").value(15.00))
                .andExpect(jsonPath("$[11].demandaUnitariakVa").value(14.40));
    }

    @Test
    @DisplayName("GET /api/v1/catalogo/aparelhos-climatizacao deve retornar 200 OK com aparelhos comerciais da Tabela 11")
    void deveRetornarAparelhosClimatizacao() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/aparelhos-climatizacao")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(8))
                .andExpect(jsonPath("$[0].capacidadeBtu").value(7500))
                .andExpect(jsonPath("$[0].potenciaAtivaW").value(900.00))
                .andExpect(jsonPath("$[0].corrente220vA").value(5.00))
                .andExpect(jsonPath("$[7].capacidadeBtu").value(30000))
                .andExpect(jsonPath("$[7].potenciaAtivaW").value(3800.00));
    }

    @Test
    @DisplayName("GET /api/v1/catalogo/fatores-ar-condicionado deve retornar 200 OK com wrapper da Tabela 12")
    void deveRetornarFatoresArCondicionadoComWrapper() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/fatores-ar-condicionado")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tabela").value("DIS-NOR-030 Tabela 12"))
                .andExpect(jsonPath("$.fatorPotencia").value(0.92))
                .andExpect(jsonPath("$.faixas").isArray())
                .andExpect(jsonPath("$.faixas.length()").value(8))
                .andExpect(jsonPath("$.faixas[0].faixaAparelhos").value("1 a 10"))
                .andExpect(jsonPath("$.faixas[0].fatorComercial").value(1.00))
                .andExpect(jsonPath("$.faixas[0].fatorResidencial").value(1.00));
    }

    @Test
    @DisplayName("GET /api/v1/catalogo/fatores-fogao-cooktop deve retornar 200 OK com faixas da Tabela 10")
    void deveRetornarFatoresCooktopComWrapper() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/fatores-fogao-cooktop")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tabela").value("DIS-NOR-030 Tabela 10"))
                .andExpect(jsonPath("$.descricao").value("Fogões Elétricos e Cooktops de Indução"))
                .andExpect(jsonPath("$.fatorPotencia").value(1.00))
                .andExpect(jsonPath("$.faixas").isArray())
                .andExpect(jsonPath("$.faixas[0].quantidade").value(1))
                .andExpect(jsonPath("$.faixas[0].fatorDemanda").value(1.00))
                .andExpect(jsonPath("$.faixas[3].quantidade").value(4))
                .andExpect(jsonPath("$.faixas[3].fatorDemanda").value(0.40));
    }

    @Test
    @DisplayName("GET /api/v1/catalogo/fatores-boilers deve retornar 200 OK com faixas da Tabela 8")
    void deveRetornarFatoresBoilersComWrapper() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/fatores-boilers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tabela").value("DIS-NOR-030 Tabela 8"))
                .andExpect(jsonPath("$.descricao").value("Aquecedores Centrais / Boilers de Acumulação"))
                .andExpect(jsonPath("$.fatorPotencia").value(1.00))
                .andExpect(jsonPath("$.faixas").isArray())
                .andExpect(jsonPath("$.faixas.length()").value(3))
                .andExpect(jsonPath("$.faixas[0].quantidadeBoilers").value(1))
                .andExpect(jsonPath("$.faixas[0].fatorDemanda").value(1.00))
                .andExpect(jsonPath("$.faixas[1].quantidadeBoilers").value(2))
                .andExpect(jsonPath("$.faixas[1].fatorDemanda").value(0.72));
    }

    @Test
    @DisplayName("GET /api/v1/catalogo/fatores-aquecedores-passagem deve retornar 200 OK com faixas da Tabela 7")
    void deveRetornarFatoresAquecedoresPassagemComWrapper() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/fatores-aquecedores-passagem")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tabela").value("DIS-NOR-030 Tabela 7"))
                .andExpect(jsonPath("$.descricao").value("Chuveiros Elétricos e Aquecedores de Passagem"))
                .andExpect(jsonPath("$.fatorPotencia").value(1.00))
                .andExpect(jsonPath("$.faixas").isArray())
                .andExpect(jsonPath("$.faixas[0].quantidadeAparelhos").value(1))
                .andExpect(jsonPath("$.faixas[0].fatorDemanda").value(1.00));
    }

    @Test
    @DisplayName("Endpoints de catálogo de áreas comuns devem permitir acesso público anônimo (permitAll)")
    void devePermitirAcessoPublicoSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/catalogo/motores-padrao"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/catalogo/aparelhos-climatizacao"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/catalogo/fatores-ar-condicionado"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/catalogo/fatores-fogao-cooktop"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/catalogo/fatores-boilers"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/catalogo/fatores-aquecedores-passagem"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Endpoints de catálogo de áreas comuns devem funcionar com Bearer token JWT")
    void devePermitirAcessoComTokenValido() throws Exception {
        User user = userRepository.save(new User("Projetista Catalogo", "catalogo@teste.com", passwordEncoder.encode("Senha123")));
        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/v1/catalogo/motores-padrao")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
