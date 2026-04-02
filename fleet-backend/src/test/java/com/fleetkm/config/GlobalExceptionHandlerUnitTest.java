package com.fleetkm.config;

import com.fleetkm.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerUnitTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBusinessException_returns_status_and_body() {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/x");
        BusinessException ex = new BusinessException(HttpStatus.CONFLICT, "Nope", "vehicleId");

        var resp = handler.handleBusinessException(ex, req);
        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        assertTrue(resp.getBody() instanceof Map);
        Map<?,?> body = (Map<?,?>) Objects.requireNonNull(resp.getBody());
        assertEquals("Nope", body.get("reason"));
        assertEquals("vehicleId", body.get("field"));
    }

    @Test
    void handleHttpMessageNotReadable_and_validation_and_missing_param_and_generic() {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/test");

        var j = new HttpMessageNotReadableException("bad", null, new MockHttpInputMessage(new byte[0]));
        var r1 = handler.handleHttpMessageNotReadable(j, req);
        assertEquals(HttpStatus.BAD_REQUEST, r1.getStatusCode());

        BindException bind = new BindException(new Object(), "obj");
        var r2 = handler.handleValidationException(bind, req);
        assertEquals(HttpStatus.BAD_REQUEST, r2.getStatusCode());

        MissingServletRequestParameterException miss = new MissingServletRequestParameterException("id", "long");
        var r3 = handler.handleMissingParam(miss, req);
        assertEquals(HttpStatus.BAD_REQUEST, r3.getStatusCode());

        var gen = new RuntimeException("boom");
        var r4 = handler.handleGeneric(gen, req);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r4.getStatusCode());
    }
}
