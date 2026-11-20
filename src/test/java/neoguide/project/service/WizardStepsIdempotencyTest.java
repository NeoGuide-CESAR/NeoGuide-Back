package neoguide.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import neoguide.project.dto.CommercialStageRequest;
import neoguide.project.dto.CommercialStageResponse;
import neoguide.project.dto.CommercialUnitItemRequest;
import neoguide.project.dto.CommonAreaLoadItemRequest;
import neoguide.project.dto.CommonAreaStageRequest;
import neoguide.project.dto.CommonAreaStageResponse;
import neoguide.project.dto.ResidentialStageRequest;
import neoguide.project.dto.ResidentialStageResponse;
import neoguide.project.dto.ResidentialTypologyItemRequest;
import neoguide.project.dto.SpecialLoadsStageRequest;
import neoguide.project.dto.SpecialLoadsStageResponse;
import neoguide.project.model.Project;
import neoguide.project.model.SpecialLoads;
import neoguide.project.model.User;
import neoguide.project.repository.CommercialUnitRepository;
import neoguide.project.repository.CommonAreaLoadRepository;
import neoguide.project.repository.DemandResultsRepository;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.ResidentialTypologyRepository;
import neoguide.project.repository.SpecialLoadsRepository;
import neoguide.project.repository.UserRepository;
import neoguide.project.security.jwt.JwtService;
import org.junit.jupiter.api.AfterEach;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WizardStepsIdempotencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ResidentialTypologyRepository residentialTypologyRepository;

    @Autowired
    private CommonAreaLoadRepository commonAreaLoadRepository;

    @Autowired
    private CommercialUnitRepository commercialUnitRepository;

    @Autowired
    private SpecialLoadsRepository specialLoadsRepository;

    @Autowired
    private DemandResultsRepository demandResultsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String tokenDono;
    private Project projeto;

    @BeforeEach
    void setUp() {
        limparBanco();

        User dono = userRepository.save(new User(
                "Engenheiro Idempotencia",
                "idempotencia@neoguide.com",
                passwordEncoder.encode("SenhaForte@123")
        ));
        tokenDono = jwtService.generateToken(dono);

        projeto = new Project();
        projeto.setUser(dono);
        projeto.setProtocolo("PRJ-2026-9001");
        projeto.setTitulo("Edifício Teste Idempotência Wizard");
        projeto.setMunicipio("Recife");
        projeto.setTensaoFornecimento("380_220V");
        projeto.setEtapaAtual(1);
        projeto.setAtivo(true);
        projeto = projectRepository.save(projeto);
    }

    @AfterEach
    void tearDown() {
        limparBanco();
    }

    private void limparBanco() {
        demandResultsRepository.deleteAll();
        commonAreaLoadRepository.deleteAll();
        specialLoadsRepository.deleteAll();
        commercialUnitRepository.deleteAll();
        residentialTypologyRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("PUT /etapa-residencial disparado 3 vezes consecutivas deve manter mesmo Drf e 0 duplicação")
    void residentialStage_threeConsecutiveIdenticalPuts_shouldBeIdempotent() throws Exception {
        ResidentialStageRequest request = new ResidentialStageRequest(List.of(
                new ResidentialTypologyItemRequest(new BigDecimal("65.00"), 16, false),
                new ResidentialTypologyItemRequest(new BigDecimal("35.00"), 8, true)
        ));
        String payload = objectMapper.writeValueAsString(request);

        BigDecimal expectedDrf = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            MvcResult result = mockMvc.perform(put("/api/v1/projects/" + projeto.getId() + "/etapa-residencial")
                            .header("Authorization", "Bearer " + tokenDono)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isOk())
                    .andReturn();

            ResidentialStageResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    ResidentialStageResponse.class
            );
            assertTrue(response.isSucesso());
            assertNotNull(response.getDemandaResidencialFinalDrfKva());

            if (expectedDrf == null) {
                expectedDrf = response.getDemandaResidencialFinalDrfKva();
            } else {
                assertEquals(0, expectedDrf.compareTo(response.getDemandaResidencialFinalDrfKva()),
                        "Drf divergiu na tentativa " + attempt);
            }

            assertEquals(2, residentialTypologyRepository.findByProjectIdOrderByIdAsc(projeto.getId()).size(),
                    "Linhas duplicadas em residential_typologies na tentativa " + attempt);
            assertEquals(2L, residentialTypologyRepository.count());
        }
    }

    @Test
    @DisplayName("PUT /etapa-area-comum disparado 3 vezes consecutivas deve manter mesmo Ds e 0 duplicação")
    void commonAreaStage_threeConsecutiveIdenticalPuts_shouldBeIdempotent() throws Exception {
        CommonAreaLoadItemRequest carga1 = new CommonAreaLoadItemRequest(
                "PERSONALIZADO", "ILUMINACAO_TUG", null, null, null,
                "Iluminação Hall e Garagem", "Luminárias LED", 1,
                new BigDecimal("6.50"), new BigDecimal("0.92"), new BigDecimal("1.00"), false
        );
        CommonAreaLoadItemRequest carga2 = new CommonAreaLoadItemRequest(
                "PERSONALIZADO", "MOTORES", null, null, null,
                "Bomba Recalque", "Motor Trifásico 5CV", 2,
                new BigDecimal("3.70"), new BigDecimal("0.85"), new BigDecimal("1.00"), false
        );
        CommonAreaStageRequest request = new CommonAreaStageRequest(List.of(carga1, carga2));
        String payload = objectMapper.writeValueAsString(request);

        BigDecimal expectedDs = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            MvcResult result = mockMvc.perform(put("/api/v1/projects/" + projeto.getId() + "/etapa-area-comum")
                            .header("Authorization", "Bearer " + tokenDono)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isOk())
                    .andReturn();

            CommonAreaStageResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    CommonAreaStageResponse.class
            );
            assertTrue(response.isSucesso());
            assertEquals(2, response.getTotalCargas());
            assertNotNull(response.getDemandaServicosDsKva());

            if (expectedDs == null) {
                expectedDs = response.getDemandaServicosDsKva();
            } else {
                assertEquals(0, expectedDs.compareTo(response.getDemandaServicosDsKva()),
                        "Ds divergiu na tentativa " + attempt);
            }

            assertEquals(2, commonAreaLoadRepository.findByProjectId(projeto.getId()).size(),
                    "Linhas duplicadas em common_area_loads na tentativa " + attempt);
            assertEquals(2L, commonAreaLoadRepository.count());
        }
    }

    @Test
    @DisplayName("PUT /etapa-comercial disparado 3 vezes consecutivas deve manter mesmo Dc e 0 duplicação")
    void commercialStage_threeConsecutiveIdenticalPuts_shouldBeIdempotent() throws Exception {
        CommercialStageRequest request = new CommercialStageRequest(List.of(
                new CommercialUnitItemRequest("Loja 01", "Padaria", new BigDecimal("80.00"), new BigDecimal("25.00")),
                new CommercialUnitItemRequest("Loja 02", "Farmácia", new BigDecimal("60.00"), new BigDecimal("15.00"))
        ));
        String payload = objectMapper.writeValueAsString(request);

        BigDecimal expectedDc = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            MvcResult result = mockMvc.perform(put("/api/v1/projects/" + projeto.getId() + "/etapa-comercial")
                            .header("Authorization", "Bearer " + tokenDono)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isOk())
                    .andReturn();

            CommercialStageResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    CommercialStageResponse.class
            );
            assertTrue(response.isSucesso());
            assertEquals(2, response.getTotalUnidades());
            assertNotNull(response.getDemandaComercialDcKva());

            if (expectedDc == null) {
                expectedDc = response.getDemandaComercialDcKva();
            } else {
                assertEquals(0, expectedDc.compareTo(response.getDemandaComercialDcKva()),
                        "Dc divergiu na tentativa " + attempt);
            }

            assertEquals(2, commercialUnitRepository.findByProjectId(projeto.getId()).size(),
                    "Linhas duplicadas em commercial_units na tentativa " + attempt);
            assertEquals(2L, commercialUnitRepository.count());
        }
    }

    @Test
    @DisplayName("PUT /etapa-cargas-especiais disparado 3 vezes consecutivas deve fazer upsert sem duplicar linhas")
    void specialLoadsStage_threeConsecutiveIdenticalPuts_shouldBeIdempotent() throws Exception {
        SpecialLoadsStageRequest request = new SpecialLoadsStageRequest(
                4, new BigDecimal("7.40"), new BigDecimal("15.00"), new BigDecimal("10.00")
        );
        String payload = objectMapper.writeValueAsString(request);

        BigDecimal expectedDve = null;
        BigDecimal expectedDmbi = null;
        BigDecimal expectedTotal = null;
        Long persistedId = null;

        for (int attempt = 1; attempt <= 3; attempt++) {
            MvcResult result = mockMvc.perform(put("/api/v1/projects/" + projeto.getId() + "/etapa-cargas-especiais")
                            .header("Authorization", "Bearer " + tokenDono)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isOk())
                    .andReturn();

            SpecialLoadsStageResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    SpecialLoadsStageResponse.class
            );
            assertTrue(response.isSucesso());
            assertNotNull(response.getDemandaEspecialTotalKva());

            if (expectedTotal == null) {
                expectedDve = response.getDemandaVeDveKva();
                expectedDmbi = response.getDemandaIncendioDmbiKva();
                expectedTotal = response.getDemandaEspecialTotalKva();
            } else {
                assertEquals(0, expectedDve.compareTo(response.getDemandaVeDveKva()),
                        "Dve divergiu na tentativa " + attempt);
                assertEquals(0, expectedDmbi.compareTo(response.getDemandaIncendioDmbiKva()),
                        "Dmbi divergiu na tentativa " + attempt);
                assertEquals(0, expectedTotal.compareTo(response.getDemandaEspecialTotalKva()),
                        "Total especial divergiu na tentativa " + attempt);
            }

            assertEquals(1L, specialLoadsRepository.count(),
                    "Linhas duplicadas em special_loads na tentativa " + attempt);
            SpecialLoads entity = specialLoadsRepository.findByProjectId(projeto.getId()).orElseThrow();
            if (persistedId == null) {
                persistedId = entity.getId();
            } else {
                assertEquals(persistedId, entity.getId(), "Upsert deve preservar o registro existente");
            }
        }
    }
}
