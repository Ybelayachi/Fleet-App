package com.fleetkm.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        // no-op
    }

    @Test
    void mileage_request_invalid_when_nulls_or_negative() {
        MileageRequest m = new MileageRequest();
        m.setVehicleId(null);
        m.setYear(null);
        m.setMonth(null);
        m.setMileage(-5L);

        Set<ConstraintViolation<MileageRequest>> violations = validator.validate(m);
        assertFalse(violations.isEmpty());
    }

    @Test
    void create_user_request_requires_email_and_password() {
        CreateUserRequest r = new CreateUserRequest();
        r.setEmail("not-an-email");
        r.setPassword("");

        Set<ConstraintViolation<CreateUserRequest>> viol = validator.validate(r);
        assertFalse(viol.isEmpty());
    }
}
