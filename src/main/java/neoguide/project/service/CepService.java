package neoguide.project.service;

import neoguide.project.dto.CepResponse;
import neoguide.project.dto.ViaCepResponse;
import neoguide.project.exception.BusinessException;
import neoguide.project.exception.ResourceNotFoundException;
import neoguide.project.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Service
public class CepService {

    private static final String VIACEP_URL = "https://viacep.com.br/ws/{cep}/json/";

    private final RestClient restClient;

    public CepService(RestClient restClient) {
        this.restClient = restClient;
    }

    public CepResponse consultarCep(String rawCep) {
        if (rawCep == null || rawCep.isBlank()) {
            throw new ValidationException(Map.of("cep", "O CEP é obrigatório"));
        }

        String sanitized = rawCep.replaceAll("\\D", "");
        if (sanitized.length() != 8) {
            throw new ValidationException(Map.of("cep", "CEP inválido: deve conter 8 dígitos numéricos"));
        }

        ViaCepResponse viaCepResponse;
        try {
            viaCepResponse = restClient.get()
                    .uri(VIACEP_URL, sanitized)
                    .retrieve()
                    .body(ViaCepResponse.class);
        } catch (RestClientException ex) {
            throw new BusinessException("Serviço de consulta de CEP temporariamente indisponível: " + ex.getMessage());
        }

        if (viaCepResponse == null || Boolean.TRUE.equals(viaCepResponse.getErro())) {
            throw new ResourceNotFoundException("CEP não encontrado: " + rawCep);
        }

        String uf = viaCepResponse.getUf();
        boolean atendidoNeoenergia = uf != null && uf.trim().equalsIgnoreCase("PE");

        String cepFormatado = viaCepResponse.getCep() != null ? viaCepResponse.getCep() : sanitized;
        String logradouro = viaCepResponse.getLogradouro() != null ? viaCepResponse.getLogradouro() : "";
        String bairro = viaCepResponse.getBairro() != null ? viaCepResponse.getBairro() : "";
        String cidade = viaCepResponse.getLocalidade() != null ? viaCepResponse.getLocalidade() : "";
        String ufFormatada = uf != null ? uf.trim().toUpperCase() : "";

        return new CepResponse(cepFormatado, logradouro, bairro, cidade, ufFormatada, atendidoNeoenergia);
    }
}
