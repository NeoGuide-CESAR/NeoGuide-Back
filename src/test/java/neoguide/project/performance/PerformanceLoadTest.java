package neoguide.project.performance;

import neoguide.project.model.Project;
import neoguide.project.model.ProjectStatus;
import neoguide.project.model.ProjectType;
import neoguide.project.model.ResidentialTypology;
import neoguide.project.model.User;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.ResidentialTypologyRepository;
import neoguide.project.repository.UserRepository;
import neoguide.project.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PerformanceLoadTest {

    private static final int WARMUP_ITERATIONS = 10;
    private static final int SAMPLE_ITERATIONS = 20;
    private static final int CALC_SAMPLE_ITERATIONS = 10;
    private static final int CONCURRENT_REQUESTS = 50;

    private static final String SIMULATION_PAYLOAD = """
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
                  "tipoEquipamento": "Iluminação LED Garagem",
                  "potenciaNominalKw": 5.00,
                  "quantidade": 1,
                  "fatorPotencia": 0.90,
                  "fatorDemanda": 1.00
                }
              ],
              "unidadesComerciais": [
                {
                  "identificacao": "Loja 1",
                  "atividadeComercial": "LOJAS",
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ResidentialTypologyRepository residentialTypologyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String token;
    private Project project;

    @BeforeEach
    void setUp() {
        User user = userRepository.findByEmail("perf.sla@neoguide.com")
                .orElseGet(() -> userRepository.save(new User(
                        "Projetista Performance",
                        "perf.sla@neoguide.com",
                        passwordEncoder.encode("SenhaForte@123")
                )));
        token = jwtService.generateToken(user);

        project = projectRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .filter(p -> "PRJ-PERF-SLA-001".equals(p.getProtocolo()))
                .findFirst()
                .orElseGet(() -> {
            Project created = new Project();
            created.setUser(user);
            created.setProtocolo("PRJ-PERF-SLA-001");
            created.setTitulo("Residencial SLA Performance");
            created.setTensaoFornecimento("380_220V");
            created.setTipo(ProjectType.LIGACAO_NOVA);
            created.setStatus(ProjectStatus.RASCUNHO);
            created.setEtapaAtual(2);
            created.setMunicipio("Recife");
            created.setTipoEdificacao(neoguide.project.model.TipoEdificacao.RESIDENCIAL_MULTIFAMILIAR);
            created.setCep("50050000");
            created.setEndereco("Rua do Exemplo, 120");
            created.setAtivo(true);
            Project saved = projectRepository.save(created);

            ResidentialTypology tipologia = new ResidentialTypology(
                    saved,
                    "Apartamento 70m2",
                    new BigDecimal("70.00"),
                    50,
                    false,
                    new BigDecimal("1.50"),
                    new BigDecimal("75.00")
            );
            residentialTypologyRepository.save(tipologia);
            return saved;
        });
    }

    @Test
    @DisplayName("SLA: Catálogo GET /api/v1/catalogo/faixas-area deve ter latência média pós-warmup < 20ms")
    void catalogEndpoint_shouldMeet20msSla() throws Exception {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            mockMvc.perform(get("/api/v1/catalogo/faixas-area")).andExpect(status().isOk());
        }

        List<Double> latenciesMs = new ArrayList<>(SAMPLE_ITERATIONS);
        for (int i = 0; i < SAMPLE_ITERATIONS; i++) {
            long start = System.nanoTime();
            mockMvc.perform(get("/api/v1/catalogo/faixas-area")).andExpect(status().isOk());
            latenciesMs.add((System.nanoTime() - start) / 1_000_000.0);
        }

        double avgMs = average(latenciesMs);
        double medianMs = percentile(latenciesMs, 50);
        System.out.printf("[SLA-CATALOGO] avg=%.2fms median=%.2fms samples=%d%n", avgMs, medianMs, SAMPLE_ITERATIONS);
        assertTrue(avgMs < 20.0, () -> String.format("Latência média do catálogo (%.2f ms) excedeu SLA de 20 ms", avgMs));
    }

    @Test
    @DisplayName("SLA: Simulação stateless POST /api/v1/simulacao/pre-calculo-etapa deve ter latência média pós-warmup < 20ms")
    void simulationEndpoint_shouldMeet20msSla() throws Exception {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            mockMvc.perform(post("/api/v1/simulacao/motor/pre-calculo-etapa")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(SIMULATION_PAYLOAD))
                    .andExpect(status().isOk());
        }

        List<Double> latenciesMs = new ArrayList<>(SAMPLE_ITERATIONS);
        for (int i = 0; i < SAMPLE_ITERATIONS; i++) {
            long start = System.nanoTime();
            mockMvc.perform(post("/api/v1/simulacao/motor/pre-calculo-etapa")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(SIMULATION_PAYLOAD))
                    .andExpect(status().isOk());
            latenciesMs.add((System.nanoTime() - start) / 1_000_000.0);
        }

        double avgMs = average(latenciesMs);
        double medianMs = percentile(latenciesMs, 50);
        System.out.printf("[SLA-SIMULACAO] avg=%.2fms median=%.2fms samples=%d%n", avgMs, medianMs, SAMPLE_ITERATIONS);
        assertTrue(avgMs < 20.0, () -> String.format("Latência média da simulação (%.2f ms) excedeu SLA de 20 ms", avgMs));
    }

    @Test
    @DisplayName("SLA: Cálculo oficial POST /api/v1/projects/{id}/calcular deve ter latência média pós-warmup < 100ms")
    void officialCalculationEndpoint_shouldMeet100msSla() throws Exception {
        String url = "/api/v1/projects/" + project.getId() + "/calcular";

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            mockMvc.perform(post(url).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        List<Double> latenciesMs = new ArrayList<>(CALC_SAMPLE_ITERATIONS);
        for (int i = 0; i < CALC_SAMPLE_ITERATIONS; i++) {
            long start = System.nanoTime();
            mockMvc.perform(post(url).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
            latenciesMs.add((System.nanoTime() - start) / 1_000_000.0);
        }

        double avgMs = average(latenciesMs);
        double p95Ms = percentile(latenciesMs, 95);
        System.out.printf("[SLA-CALCULO-OFICIAL] avg=%.2fms p95=%.2fms samples=%d%n", avgMs, p95Ms, CALC_SAMPLE_ITERATIONS);
        assertTrue(avgMs < 100.0, () -> String.format("Latência média do cálculo oficial (%.2f ms) excedeu SLA de 100 ms", avgMs));
    }

    @Test
    @DisplayName("SLA: 50 requisições concorrentes devem concluir com 0% de erro e p95 < 100ms")
    void concurrentLoad50Requests_shouldHaveZeroErrorsAndP95Under100ms() throws Exception {
        // Warm-up prévio para evitar jitter de compilação JIT durante rajada concorrente
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            mockMvc.perform(get("/api/v1/catalogo/faixas-area")).andExpect(status().isOk());
            mockMvc.perform(post("/api/v1/simulacao/motor/pre-calculo-etapa")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(SIMULATION_PAYLOAD))
                    .andExpect(status().isOk());
        }

        int concurrencyLevel = 10;
        ExecutorService executor = Executors.newFixedThreadPool(concurrencyLevel);
        CountDownLatch readyLatch = new CountDownLatch(concurrencyLevel);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger errorCount = new AtomicInteger(0);
        List<Double> latenciesMs = Collections.synchronizedList(new ArrayList<>(CONCURRENT_REQUESTS));
        List<Future<?>> futures = new ArrayList<>(CONCURRENT_REQUESTS);

        try {
            for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
                final int idx = i;
                futures.add(executor.submit(() -> {
                    readyLatch.countDown();
                    try {
                        startLatch.await(10, TimeUnit.SECONDS);
                        long start = System.nanoTime();
                        MvcResult result;
                        if (idx % 2 == 0) {
                            result = mockMvc.perform(get("/api/v1/catalogo/faixas-area")).andReturn();
                        } else {
                            result = mockMvc.perform(post("/api/v1/simulacao/motor/pre-calculo-etapa")
                                            .header("Authorization", "Bearer " + token)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(SIMULATION_PAYLOAD))
                                    .andReturn();
                        }
                        double elapsedMs = (System.nanoTime() - start) / 1_000_000.0;
                        latenciesMs.add(elapsedMs);
                        if (result.getResponse().getStatus() != 200) {
                            errorCount.incrementAndGet();
                        }
                    } catch (Exception ex) {
                        errorCount.incrementAndGet();
                    }
                }));
            }

            assertTrue(readyLatch.await(10, TimeUnit.SECONDS), "As threads concorrentes devem estar prontas para a rajada");
            startLatch.countDown();

            for (Future<?> future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }

        double p95Ms = percentile(latenciesMs, 95);
        double avgMs = average(latenciesMs);
        System.out.printf("[SLA-CONCORRENCIA-50] errors=%d/%d avg=%.2fms p95=%.2fms%n",
                errorCount.get(), CONCURRENT_REQUESTS, avgMs, p95Ms);

        assertEquals(0, errorCount.get(), "Taxa de erro sob 50 requisições concorrentes deve ser 0%");
        assertEquals(CONCURRENT_REQUESTS, latenciesMs.size(), "Todas as 50 requisições devem registrar latência");
        assertTrue(p95Ms < 100.0, () -> String.format("p95 concorrente (%.2f ms) excedeu SLA de 100 ms", p95Ms));
    }

    private double average(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
    }

    private double percentile(List<Double> values, double percentile) {
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int index = (int) Math.ceil((percentile / 100.0) * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }
}
