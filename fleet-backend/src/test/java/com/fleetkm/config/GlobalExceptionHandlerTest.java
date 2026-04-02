package com.fleetkm.config;

import com.fleetkm.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalExceptionHandlerTest {

    @Test
    void handle_business_exception_returns_reason_and_field() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/x");
        BusinessException ex = new BusinessException(org.springframework.http.HttpStatus.BAD_REQUEST, "Nope", "vehicleId");

        ResponseEntity<?> resp = h.handleBusinessException(ex, req);
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> body = (java.util.Map<String, Object>) java.util.Objects.requireNonNull(resp.getBody());
        assertThat(body.get("reason")).isEqualTo("Nope");
        assertThat(body.get("field")).isEqualTo("vehicleId");
        assertThat(body.get("path")).isEqualTo("/api/x");
    }

    @Test
    void handle_generic_exception_returns_500() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/boom");
        RuntimeException ex = new RuntimeException("bad");

        ResponseEntity<?> resp = h.handleGeneric(ex, req);
        assertThat(resp.getStatusCode().value()).isEqualTo(500);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> body = (java.util.Map<String, Object>) java.util.Objects.requireNonNull(resp.getBody());
        assertThat(body.get("error")).isEqualTo("Erreur interne du serveur");
        assertThat(body.get("path")).isEqualTo("/api/boom");
    }
}
