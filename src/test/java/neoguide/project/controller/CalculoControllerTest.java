package neoguide.project.controller;

import neoguide.project.model.CommercialUnit;
import neoguide.project.model.DemandResult;
import neoguide.project.model.Project;
import neoguide.project.model.ResidentialTypology;
import neoguide.project.model.TipoEdificacao;
import neoguide.project.model.User;
import neoguide.project.repository.CommercialUnitRepository;
import neoguide.project.repository.DemandResultRepository;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.ResidentialTypologyRepository;
import neoguide.project.repository.UserRepository;
import neoguide.project.security.jwt.JwtService;
import neoguide.project.support.TestDatabaseCleaner;
import org.junit.jupiter.api.AfterEach;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CalculoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ResidentialTypologyRepository residentialTypologyRepository;
    @Autowired
    private CommercialUnitRepository commercialUnitRepository;
    @Autowired
    private DemandResultRepository demandResultRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private TestDatabaseCleaner testDatabaseCleaner;

    private User dono;
    private User outro;
    private String tokenDono;
    private String tokenOutro;

    @BeforeEach
    void setUp() {
        testDatabaseCleaner.cleanOperationalTables();

        dono = userRepository.save(new User("Projetista Dono", "dono@calculo.com", passwordEncoder.encode("SenhaForte@123")));
        outro = userRepository.save(new User("Outro Projetista", "outro@calculo.com", passwordEncoder.encode("SenhaForte@123")));
        tokenDono = jwtService.generateToken(dono);
        tokenOutro = jwtService.generateToken(outro);
    }

    @AfterEach
    void tearDown() {
        testDatabaseCleaner.cleanOperationalTables();
    }

    private Project novoProjeto(User usuario, TipoEdificacao tipo, String protocolo) {
        Project p = new Project();
        p.setUser(usuario);
        p.setProtocolo(protocolo);
        p.setTitulo("Edifício de teste");
        p.setTipoEdificacao(tipo);
        p.setTensaoFornecimento("380_220V");
        p.setMunicipio("Recife");
        p.setCep("50050000");
        p.setEndereco("Rua do Exemplo, 120");
        return projectRepository.save(p);
    }

    private ResidentialTypology tipologia(Project p, String area, int unidades) {
        return residentialTypologyRepository.save(new ResidentialTypology(p, "Tipologia " + area + "m²",
                new BigDecimal(area), unidades, false, null, null));
    }

    @Test
    @DisplayName("Calcular sem tipologia residencial: 422 RFC 7807 com a regra e o campo")
    void calcularSemEtapaObrigatoria_422() throws Exception {
        Project p = novoProjeto(dono, TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, "PRJ-2026-0101");

        mockMvc.perform(post("/api/v1/projects/" + p.getId() + "/calcular")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.regra").value("etapas_obrigatorias"))
                .andExpect(jsonPath("$.erros[0].campo").value("tipologias"))
                .andExpect(jsonPath("$.erros[0].etapa").value("residencial"));
    }

    @Test
    @DisplayName("Validação normativa com etapa obrigatória ausente: situação ERRO e nenhum cálculo gravado")
    void validacaoComErro() throws Exception {
        Project p = novoProjeto(dono, TipoEdificacao.MISTO, "PRJ-2026-0102");

        mockMvc.perform(get("/api/v1/projects/" + p.getId() + "/validacao-normativa")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("ERRO"))
                .andExpect(jsonPath("$.quantidadeErros").value(1))
                .andExpect(jsonPath("$.itens[0].nivel").value("ERRO"));
    }

    @Test
    @DisplayName("Fluxo: calcular 200 CONCLUIDO/CALCULADO, resultado, memorial PDF; depois de alterar, DESATUALIZADO e memorial 409")
    void fluxoCompletoResidencial() throws Exception {
        Project p = novoProjeto(dono, TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, "PRJ-2026-0103");
        ResidentialTypology t = tipologia(p, "60", 10);

        mockMvc.perform(get("/api/v1/projects/" + p.getId() + "/resultado")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.regra").value("nao_calculado"));

        mockMvc.perform(post("/api/v1/projects/" + p.getId() + "/calcular")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"))
                .andExpect(jsonPath("$.situacaoCalculo").value("CALCULADO"))
                .andExpect(jsonPath("$.protocolo").value("PRJ-2026-0103"))
                .andExpect(jsonPath("$.enquadramento.tipoFornecimento").value("BT_380_220V"))
                .andExpect(jsonPath("$.enquadramento.limiteKva").value(300))
                .andExpect(jsonPath("$.resumoDemandas.ded").isNumber());

        mockMvc.perform(get("/api/v1/projects/" + p.getId() + "/resultado")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacaoCalculo").value("CALCULADO"))
                .andExpect(jsonPath("$.resumoDemandas.drf").isNumber());

        mockMvc.perform(get("/api/v1/projects/" + p.getId() + "/memorial-pdf")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", containsString("memorial-PRJ-2026-0103.pdf")));

        // Alteração depois do cálculo: a tipologia passa de 10 para 20 unidades.
        t.setQuantidadeUnidades(20);
        residentialTypologyRepository.save(t);

        mockMvc.perform(get("/api/v1/projects/" + p.getId() + "/resultado")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacaoCalculo").value("DESATUALIZADO"));

        mockMvc.perform(get("/api/v1/projects/" + p.getId() + "/memorial-pdf")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.regra").value("calculo_desatualizado"));

        // Recalcular devolve o memorial a ficar disponível.
        mockMvc.perform(post("/api/v1/projects/" + p.getId() + "/calcular")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacaoCalculo").value("CALCULADO"));
        mockMvc.perform(get("/api/v1/projects/" + p.getId() + "/memorial-pdf")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Projeto em média tensão: calcular 200 com atendimentoMT e memorial 409 com regra atendimento_mt")
    void projetoEmMedia_tensao() throws Exception {
        Project p = novoProjeto(dono, TipoEdificacao.COMERCIAL, "PRJ-2026-0104");
        commercialUnitRepository.save(new CommercialUnit(p, 1, "LOJAS", "Loja grande", "Loja",
                new BigDecimal("500"), new BigDecimal("300"), BigDecimal.ONE, new BigDecimal("326.09")));

        mockMvc.perform(post("/api/v1/projects/" + p.getId() + "/calcular")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enquadramento.tipoFornecimento").value("MT"))
                .andExpect(jsonPath("$.enquadramento.atendimentoMT").value(true))
                .andExpect(jsonPath("$.dimensionamentoEntrada").value(nullValue()))
                .andExpect(jsonPath("$.barramentoQdg").value(nullValue()))
                .andExpect(jsonPath("$.seloConformidade").value("ATENDIMENTO_MT"))
                .andExpect(jsonPath("$.alertas[?(@.codigo == 'atendimento_mt')].nivel").value(hasItem("ALERTA")));

        mockMvc.perform(get("/api/v1/projects/" + p.getId() + "/memorial-pdf")
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.regra").value("atendimento_mt"));
    }

    @Test
    @DisplayName("Ownership: outro usuário não calcula, não lê resultado e não baixa o memorial (404)")
    void outroUsuarioRecebe404() throws Exception {
        Project p = novoProjeto(dono, TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, "PRJ-2026-0105");
        tipologia(p, "60", 10);
        mockMvc.perform(post("/api/v1/projects/" + p.getId() + "/calcular")
                .header("Authorization", "Bearer " + tokenDono)).andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/projects/" + p.getId() + "/calcular")
                        .header("Authorization", "Bearer " + tokenOutro))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/projects/" + p.getId() + "/resultado")
                        .header("Authorization", "Bearer " + tokenOutro))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/projects/" + p.getId() + "/memorial-pdf")
                        .header("Authorization", "Bearer " + tokenOutro))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cálculo gravado mantém só o último registro por projeto")
    void somenteUltimoCalculoGravado() throws Exception {
        Project p = novoProjeto(dono, TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, "PRJ-2026-0106");
        tipologia(p, "60", 10);
        mockMvc.perform(post("/api/v1/projects/" + p.getId() + "/calcular").header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/projects/" + p.getId() + "/calcular").header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk());

        assertEquals(1, demandResultRepository.findAll().stream()
                .filter(d -> d.getProject().getId().equals(p.getId())).count());
        assertTrue(demandResultRepository.findAll().stream()
                .map(DemandResult::getFingerprint).allMatch(f -> f.length() == 64));
    }

    @Test
    @DisplayName("Etapas gravadas: devolve as tipologias do projeto e 404 para outro usuário")
    void lerEtapasGravadas() throws Exception {
        Project p = novoProjeto(dono, TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, "PRJ-2026-0107");
        tipologia(p, "65", 24);

        mockMvc.perform(get("/api/v1/projects/" + p.getId() + "/etapas").header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.residencial.tipologias[0].quantidadeUnidades").value(24))
                .andExpect(jsonPath("$.areaComum.cargas").isEmpty())
                .andExpect(jsonPath("$.cargasEspeciais").value(nullValue()));
        mockMvc.perform(get("/api/v1/projects/" + p.getId() + "/etapas").header("Authorization", "Bearer " + tokenOutro))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Memorial traz as premissas, a mesma lista da página Normas técnicas")
    void memorialTemPremissas() throws Exception {
        Project p = novoProjeto(dono, TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR, "PRJ-2026-0109");
        tipologia(p, "65", 10);
        mockMvc.perform(post("/api/v1/projects/" + p.getId() + "/calcular").header("Authorization", "Bearer " + tokenDono)).andExpect(status().isOk());
        byte[] pdf = mockMvc.perform(get("/api/v1/projects/" + p.getId() + "/memorial-pdf").header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        String texto = new String(pdf, java.nio.charset.StandardCharsets.ISO_8859_1);
        assertTrue(texto.contains("Premissas adotadas"));
        for (neoguide.project.dto.normas.PremissaResponse premissa : neoguide.project.service.PremissasNormativas.TODAS) {
            String inicio = premissa.texto().substring(0, 18);
            assertTrue(texto.contains(inicio), "premissa ausente do memorial: " + inicio);
        }
    }
}
