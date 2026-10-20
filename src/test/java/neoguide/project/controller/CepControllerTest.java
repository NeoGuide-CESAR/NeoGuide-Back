package neoguide.project.controller;

import neoguide.project.dto.CepResponse;
import neoguide.project.exception.ResourceNotFoundException;
import neoguide.project.exception.ValidationException;
import neoguide.project.service.CepService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CepControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CepService cepService;

    @Test
    @DisplayName("GET /api/v1/cep/{cep} público com CEP válido deve retornar 200 OK sem autenticação")
    void consultarCep_publico_sucesso() throws Exception {
        when(cepService.consultarCep("50030-000"))
                .thenReturn(new CepResponse("50030-000", "Avenida Cais do Apolo", "Bairro do Recife", "Recife", "PE", true));

        mockMvc.perform(get("/api/v1/cep/50030-000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep", is("50030-000")))
                .andExpect(jsonPath("$.logradouro", is("Avenida Cais do Apolo")))
                .andExpect(jsonPath("$.cidade", is("Recife")))
                .andExpect(jsonPath("$.uf", is("PE")))
                .andExpect(jsonPath("$.atendidoNeoenergia", is(true)));
    }

    @Test
    @DisplayName("GET /api/v1/cep/{cep} com CEP fora da área deve retornar atendidoNeoenergia false")
    void consultarCep_foraDePernambuco() throws Exception {
        when(cepService.consultarCep("01310-100"))
                .thenReturn(new CepResponse("01310-100", "Avenida Paulista", "Bela Vista", "São Paulo", "SP", false));

        mockMvc.perform(get("/api/v1/cep/01310-100")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cidade", is("São Paulo")))
                .andExpect(jsonPath("$.uf", is("SP")))
                .andExpect(jsonPath("$.atendidoNeoenergia", is(false)));
    }

    @Test
    @DisplayName("GET /api/v1/cep/{cep} com formato inválido deve retornar 400 Bad Request")
    void consultarCep_formatoInvalido_retorna400() throws Exception {
        when(cepService.consultarCep("123"))
                .thenThrow(new ValidationException(Map.of("cep", "CEP inválido: deve conter 8 dígitos numéricos")));

        mockMvc.perform(get("/api/v1/cep/123")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.cep").exists());
    }

    @Test
    @DisplayName("GET /api/v1/cep/{cep} inexistente deve retornar 404 Not Found")
    void consultarCep_inexistente_retorna404() throws Exception {
        when(cepService.consultarCep("99999-999"))
                .thenThrow(new ResourceNotFoundException("CEP não encontrado: 99999-999"));

        mockMvc.perform(get("/api/v1/cep/99999-999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("CEP não encontrado: 99999-999")));
    }
}
