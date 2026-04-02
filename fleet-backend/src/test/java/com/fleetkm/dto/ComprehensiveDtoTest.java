package com.fleetkm.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ComprehensiveDtoTest {

    private final Validator validator;
    private final ObjectMapper mapper = new ObjectMapper();

    public ComprehensiveDtoTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    void mileageRequest_getters_setters_and_validation() {
        MileageRequest m = new MileageRequest();
        m.setVehicleId(1L);
        m.setYear(2026);
        m.setMonth(1);
        m.setMileage(1234L);

        assertEquals(1L, m.getVehicleId());
        assertEquals(2026, m.getYear());
        assertEquals(1, m.getMonth());
        assertEquals(1234L, m.getMileage());

        Set<ConstraintViolation<MileageRequest>> ok = validator.validate(m);
        assertTrue(ok.isEmpty());

        m.setMileage(-5L);
        Set<ConstraintViolation<MileageRequest>> violations = validator.validate(m);
        assertFalse(violations.isEmpty());
    }

    @Test
    void createUserRequest_validation() {
        CreateUserRequest r = new CreateUserRequest();
        r.setEmail("not-an-email");
        r.setPassword("");

        Set<ConstraintViolation<CreateUserRequest>> v = validator.validate(r);
        assertFalse(v.isEmpty());
    }

    @Test
    void authrequest_and_response_serialization_and_equals() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setEmail("a@b.com");
        req.setPassword("secret");

        String json = mapper.writeValueAsString(req);
        assertTrue(json.contains("a@b.com"));

        AuthResponse r1 = new AuthResponse("token-123");
        AuthResponse r2 = new AuthResponse("token-123");
        AuthResponse r3 = new AuthResponse("other");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);

        String respJson = mapper.writeValueAsString(r1);
        // AuthResponse has an all-args constructor (no default ctor) so deserialize into tree
        String token = mapper.readTree(respJson).get("token").asText();
        assertEquals("token-123", token);
    }
}
