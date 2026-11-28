package neoguide.project.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import neoguide.project.model.User;
import neoguide.project.repository.DemandResultRepository;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.UserRepository;
import neoguide.project.security.jwt.JwtService;
import neoguide.project.service.PremissasNormativas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Página "Normas técnicas": endpoints de normas e as tabelas do catálogo, com os valores de conferência das normas. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NormasETabelasTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private DemandResultRepository resultadoRepository;
    @Autowired private neoguide.project.repository.CommercialUnitRepository espacoRepository;
    @Autowired private neoguide.project.repository.ResidentialTypologyRepository tipologiaRepository;
    @Autowired private neoguide.project.repository.CommonAreaLoadRepository cargaRepository;
    @Autowired private neoguide.project.repository.SpecialLoadsRepository especiaisRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private final ObjectMapper json = new ObjectMapper();
    private String token;

    @BeforeEach
    void setUp() {
        resultadoRepository.deleteAll();
        espacoRepository.deleteAll();
        tipologiaRepository.deleteAll();
        cargaRepository.deleteAll();
        especiaisRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        User u = userRepository.save(new User("Leitor Normas", "leitor@normas.com", passwordEncoder.encode("SenhaForte123")));
        token = jwtService.generateToken(u);
    }

    private JsonNode catalogo(String caminho) throws Exception {
        return json.readTree(mockMvc.perform(get("/api/v1/catalogo/" + caminho)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    private JsonNode autenticado(String caminho) throws Exception {
        return json.readTree(mockMvc.perform(get("/api/v1/normas" + caminho).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    private static JsonNode linha(JsonNode lista, Predicate<JsonNode> criterio) {
        for (JsonNode l : lista) if (criterio.test(l)) return l;
        fail("linha não encontrada");
        return null;
    }

    private static void igual(String esperado, JsonNode valor) {
        assertEquals(0, new BigDecimal(esperado).compareTo(valor.decimalValue()), "esperado " + esperado + " mas veio " + valor);
    }

    // ---------------------------------------------------------------- normas

    @Test
    @DisplayName("Os endpoints de normas exigem login")
    void exigemLogin() throws Exception {
        mockMvc.perform(get("/api/v1/normas")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/normas/premissas")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/normas/DIS-NOR-053/tabelas")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /normas: DIS-NOR-053 Rev. 06 e DIS-NOR-030 Rev. 07, com a data de aprovação lida do cabeçalho do PDF")
    void listaDeNormas() throws Exception {
        JsonNode normas = autenticado("");
        assertEquals(2, normas.size());
        JsonNode n053 = linha(normas, n -> "DIS-NOR-053".equals(n.get("codigo").asText()));
        assertEquals("06", n053.get("revisao").asText());
        assertEquals("2025-09-09", n053.get("dataAprovacao").asText());
        assertEquals("PRINCIPAL", n053.get("papel").asText());
        JsonNode n030 = linha(normas, n -> "DIS-NOR-030".equals(n.get("codigo").asText()));
        assertEquals("07", n030.get("revisao").asText());
        assertEquals("2026-04-17", n030.get("dataAprovacao").asText());
        assertEquals("COMPLEMENTAR", n030.get("papel").asText());
        // Sem URL configurada, não há link inventado.
        assertTrue(n053.get("urlDocumento").isNull());
    }

    @Test
    @DisplayName("GET /normas/{codigo}/tabelas: as tabelas da norma, com âncora, página do PDF e etapas; Tabela 13 ainda sem catálogo")
    void tabelasDaNorma() throws Exception {
        JsonNode t053 = autenticado("/dis-nor-053/tabelas");
        assertEquals(List.of("quadro-35", "quadro-36", "quadro-37", "quadro-33", "tabela-2", "tabela-1", "tabela-3"), t053.findValuesAsText("ancora"));
        JsonNode q35 = linha(t053, t -> "quadro-35".equals(t.get("ancora").asText()));
        assertEquals("/api/v1/catalogo/faixas-area", q35.get("endpointCatalogo").asText());
        assertEquals(107, q35.get("paginaPdf").asInt());
        assertEquals(2, q35.get("etapasWizard").get(0).asInt());
        assertTrue(linha(t053, t -> "tabela-2".equals(t.get("ancora").asText())).get("usadaNoResultado").asBoolean());

        JsonNode t030 = autenticado("/DIS-NOR-030/tabelas");
        assertEquals(14, t030.size());
        JsonNode t13 = linha(t030, t -> "tabela-13".equals(t.get("ancora").asText()));
        assertFalse(t13.get("disponivel").asBoolean());
        assertTrue(t13.get("endpointCatalogo").isNull());
        assertTrue(linha(t030, t -> "tabela-10".equals(t.get("ancora").asText())).get("disponivel").asBoolean());

        mockMvc.perform(get("/api/v1/normas/dis-nor-999/tabelas").header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Todos os endpoints de catálogo citados pelas normas existem e respondem")
    void endpointsDeCatalogoExistem() throws Exception {
        for (String codigo : List.of("dis-nor-053", "dis-nor-030")) {
            for (JsonNode t : autenticado("/" + codigo + "/tabelas")) {
                if (!t.get("disponivel").asBoolean()) continue;
                mockMvc.perform(get(t.get("endpointCatalogo").asText())).andExpect(status().isOk());
            }
        }
    }

    @Test
    @DisplayName("Premissas: lista única de 7 itens, a mesma usada no memorial")
    void premissas() throws Exception {
        JsonNode p = autenticado("/premissas");
        assertEquals(PremissasNormativas.TODAS.size(), p.size());
        assertEquals(PremissasNormativas.TODAS.get(0).texto(), p.get(0).get("texto").asText());
        assertTrue(p.get(0).get("texto").asText().contains("iluminação LED 0,80"));
    }

    // ---------------------------------------------------------------- valores de conferência

    @Test
    @DisplayName("Quadro 35: 61–65 m² → 1,47 kVA; 66–70 m² → 1,57 kVA; até 40 m² → 1,00 kVA; sem faixas sobrepostas")
    void quadro35() throws Exception {
        JsonNode faixas = catalogo("faixas-area");
        igual("1.47", linha(faixas, f -> "FAIXA_61_65".equals(f.get("faixa").asText())).get("demandaUnitariakVa"));
        igual("1.57", linha(faixas, f -> "FAIXA_66_70".equals(f.get("faixa").asText())).get("demandaUnitariakVa"));
        igual("1.00", linha(faixas, f -> "FAIXA_ATE_40".equals(f.get("faixa").asText())).get("demandaUnitariakVa"));
        BigDecimal fimAnterior = null;
        List<String> sobrepostas = new ArrayList<>();
        for (JsonNode f : faixas) {
            BigDecimal ini = f.get("minAreaM2").decimalValue();
            if (fimAnterior != null && ini.compareTo(fimAnterior) <= 0) sobrepostas.add(f.get("faixa").asText());
            fimAnterior = f.get("maxAreaM2").decimalValue();
        }
        assertTrue(sobrepostas.isEmpty(), "faixas sobrepostas: " + sobrepostas);
    }

    @Test
    @DisplayName("Quadro 36: 20 apartamentos → 87,20%; 50 → 70,68%; 60 ou mais → 68,23%")
    void quadro36() throws Exception {
        JsonNode tabela = catalogo("fatores-coincidencia-residencial").get("tabela");
        igual("0.8720", linha(tabela, l -> l.get("totalApartamentos").asInt() == 20).get("fatorCoincidencia"));
        igual("0.7068", linha(tabela, l -> l.get("totalApartamentos").asInt() == 50).get("fatorCoincidencia"));
        igual("0.6823", linha(tabela, l -> l.get("totalApartamentos").asInt() == 60).get("fatorCoincidencia"));
    }

    @Test
    @DisplayName("Quadro 33: 1 a 10 → 1,00; 11 a 20 → 0,86; acima de 100 → 0,60")
    void quadro33() throws Exception {
        JsonNode faixas = catalogo("normas/fatores-recarga-ve");
        assertEquals(8, faixas.size());
        igual("1.00", linha(faixas, l -> l.get("maxVagas").asInt() == 10).get("fatorSimultaneidade"));
        igual("0.86", linha(faixas, l -> l.get("maxVagas").asInt() == 20).get("fatorSimultaneidade"));
        igual("0.60", linha(faixas, l -> l.get("maxVagas").asInt() == 9999).get("fatorSimultaneidade"));
    }

    @Test
    @DisplayName("Tabela 7: 2 aparelhos → 1,00; 3 → 0,84; 4 → 0,76")
    void tabela7() throws Exception {
        JsonNode faixas = catalogo("fatores-aquecedores-passagem").get("faixas");
        igual("1.00", linha(faixas, l -> l.get("quantidadeAparelhos").asInt() == 2).get("fatorDemanda"));
        igual("0.84", linha(faixas, l -> l.get("quantidadeAparelhos").asInt() == 3).get("fatorDemanda"));
        igual("0.76", linha(faixas, l -> l.get("quantidadeAparelhos").asInt() == 4).get("fatorDemanda"));
    }

    @Test
    @DisplayName("Tabela 16: 3 bombas → 0,47; acima de 3 → 0,39")
    void tabela16() throws Exception {
        JsonNode linhas = catalogo("normas/fatores-bombas-piscina");
        igual("0.47", linha(linhas, l -> l.get("quantidadeBombas").asInt() == 3).get("fatorDemanda"));
        igual("0.39", linha(linhas, l -> l.get("quantidadeBombas").asInt() == 4).get("fatorDemanda"));
    }

    @Test
    @DisplayName("Tabela 19: 1 cv → 1.520 VA; 2 cv → 2.700 VA; 5 cv → 6.020 VA")
    void tabela19() throws Exception {
        JsonNode motores = catalogo("motores-padrao");
        igual("1.52", linha(motores, l -> l.get("potenciaCv").decimalValue().compareTo(BigDecimal.ONE) == 0).get("demandaUnitariakVa"));
        igual("2.70", linha(motores, l -> l.get("potenciaCv").decimalValue().compareTo(new BigDecimal("2")) == 0).get("demandaUnitariakVa"));
        igual("6.02", linha(motores, l -> l.get("potenciaCv").decimalValue().compareTo(new BigDecimal("5")) == 0).get("demandaUnitariakVa"));
    }

    @Test
    @DisplayName("Tabela 2: 165 < De ≤ 211 kVA → 240 mm², disjuntor 320 A; 220/127 V tem 10 linhas")
    void tabela2() throws Exception {
        JsonNode linhas = catalogo("normas/dimensionamento-entrada/380v");
        JsonNode l211 = linha(linhas, l -> l.get("maxDemandaKva").decimalValue().compareTo(new BigDecimal("211")) == 0);
        assertEquals("240 mm²", l211.get("secaoCondutor").asText());
        assertEquals(320, l211.get("disjuntorCorrenteA").asInt());
        assertEquals(10, catalogo("normas/dimensionamento-entrada/220v").size());
    }

    @Test
    @DisplayName("Tabela 3: 260 a 346 kVA → 50 × 5 mm, 595 A")
    void tabela3() throws Exception {
        JsonNode linhas = catalogo("normas/barramentos-qdg");
        JsonNode l346 = linha(linhas, l -> l.get("maxDemandaKva").decimalValue().compareTo(new BigDecimal("346")) == 0);
        assertEquals("50 x 5", l346.get("dimensaoMm").asText());
        assertEquals(595, l346.get("correnteNominalA").asInt());
    }
}
