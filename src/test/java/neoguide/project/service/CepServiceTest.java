package neoguide.project.service;

import neoguide.project.dto.CepResponse;
import neoguide.project.exception.BusinessException;
import neoguide.project.exception.ResourceNotFoundException;
import neoguide.project.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CepServiceTest {

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer mockServer;
    private CepService cepService;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        cepService = new CepService(restClientBuilder.build());
    }

    @Test
    @DisplayName("consultarCep com CEP de Pernambuco deve retornar atendidoNeoenergia true")
    void consultarCep_pernambuco_retornaAtendidoTrue() {
        String jsonResponse = """
                {
                    "cep": "50030-000",
                    "logradouro": "Avenida Cais do Apolo",
                    "complemento": "",
                    "bairro": "Bairro do Recife",
                    "localidade": "Recife",
                    "uf": "PE",
                    "ibge": "2611606"
                }
                """;

        mockServer.expect(requestTo("https://viacep.com.br/ws/50030000/json/"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        CepResponse response = cepService.consultarCep("50030-000");

        assertNotNull(response);
        assertEquals("50030-000", response.getCep());
        assertEquals("Avenida Cais do Apolo", response.getLogradouro());
        assertEquals("Bairro do Recife", response.getBairro());
        assertEquals("Recife", response.getCidade());
        assertEquals("PE", response.getUf());
        assertTrue(response.isAtendidoNeoenergia());
        mockServer.verify();
    }

    @Test
    @DisplayName("consultarCep com CEP sem hífen deve sanitizar e consultar corretamente")
    void consultarCep_semHifen_sucesso() {
        String jsonResponse = """
                {
                    "cep": "55000-000",
                    "logradouro": "Rua Central",
                    "complemento": "",
                    "bairro": "Centro",
                    "localidade": "Caruaru",
                    "uf": "PE"
                }
                """;

        mockServer.expect(requestTo("https://viacep.com.br/ws/55000000/json/"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        CepResponse response = cepService.consultarCep("55000000");

        assertNotNull(response);
        assertEquals("Caruaru", response.getCidade());
        assertEquals("PE", response.getUf());
        assertTrue(response.isAtendidoNeoenergia());
        mockServer.verify();
    }

    @Test
    @DisplayName("consultarCep com CEP de outro estado deve retornar atendidoNeoenergia false")
    void consultarCep_outroEstado_retornaAtendidoFalse() {
        String jsonResponse = """
                {
                    "cep": "01310-100",
                    "logradouro": "Avenida Paulista",
                    "complemento": "",
                    "bairro": "Bela Vista",
                    "localidade": "São Paulo",
                    "uf": "SP"
                }
                """;

        mockServer.expect(requestTo("https://viacep.com.br/ws/01310100/json/"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        CepResponse response = cepService.consultarCep("01310-100");

        assertNotNull(response);
        assertEquals("01310-100", response.getCep());
        assertEquals("São Paulo", response.getCidade());
        assertEquals("SP", response.getUf());
        assertFalse(response.isAtendidoNeoenergia());
        mockServer.verify();
    }

    @Test
    @DisplayName("consultarCep com retorno erro do ViaCEP deve lançar ResourceNotFoundException")
    void consultarCep_inexistente_lancaNotFound() {
        String jsonResponse = """
                {
                    "erro": true
                }
                """;

        mockServer.expect(requestTo("https://viacep.com.br/ws/99999999/json/"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        assertThrows(ResourceNotFoundException.class, () -> cepService.consultarCep("99999-999"));
        mockServer.verify();
    }

    @Test
    @DisplayName("consultarCep com formato inválido (menos de 8 dígitos) deve lançar ValidationException")
    void consultarCep_formatoInvalido_lancaValidationException() {
        ValidationException ex = assertThrows(ValidationException.class, () -> cepService.consultarCep("123"));
        assertTrue(ex.getErrors().containsKey("cep"));
    }

    @Test
    @DisplayName("consultarCep com formato inválido (mais de 8 dígitos) deve lançar ValidationException")
    void consultarCep_digitosExcedentes_lancaValidationException() {
        ValidationException ex = assertThrows(ValidationException.class, () -> cepService.consultarCep("50030-0000"));
        assertTrue(ex.getErrors().containsKey("cep"));
    }

    @Test
    @DisplayName("consultarCep nulo ou vazio deve lançar ValidationException")
    void consultarCep_nuloOuVazio_lancaValidationException() {
        assertThrows(ValidationException.class, () -> cepService.consultarCep(null));
        assertThrows(ValidationException.class, () -> cepService.consultarCep("   "));
    }

    @Test
    @DisplayName("consultarCep com erro no servidor externo deve lançar BusinessException")
    void consultarCep_erroServidorExterno_lancaBusinessException() {
        mockServer.expect(requestTo("https://viacep.com.br/ws/50030000/json/"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(BusinessException.class, () -> cepService.consultarCep("50030000"));
        mockServer.verify();
    }
}
