package neoguide.project.exception;

import neoguide.project.dto.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;

/** Rota inexistente deve responder 404 (antes o handler genérico transformava em 500). */
class GlobalExceptionHandlerNotFoundTest {

    @Test
    @DisplayName("NoResourceFoundException deve virar 404, não 500")
    void rotaInexistenteRetorna404() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ErrorResponse> resposta =
                handler.handleNoResource(new NoResourceFoundException(HttpMethod.GET, "api/v1/auth/me", "NoResourceFoundException"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody()).isNotNull();
        assertThat(resposta.getBody().getStatus()).isEqualTo(404);
    }
}
