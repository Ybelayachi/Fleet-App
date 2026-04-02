package com.fleetkm.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class MileageRequestValidationTest {
    private static ValidatorFactory vf;
    private static Validator validator;

    @BeforeAll
    static void init() {
        vf = Validation.buildDefaultValidatorFactory();
        validator = vf.getValidator();
    }

    @AfterAll
    static void cleanup() {
        vf.close();
    }

    @Test
    void validRequest_hasNoViolations() {
        MileageRequest r = new MileageRequest();
        r.setVehicleId(1L);
        r.setYear(2026);
        r.setMonth(1);
        r.setMileage(0L);

        Set<ConstraintViolation<MileageRequest>> v = validator.validate(r);
        assertThat(v).isEmpty();
    }

    @Test
    void negativeMileage_violatesMin() {
        MileageRequest r = new MileageRequest();
        r.setVehicleId(1L);
        r.setYear(2026);
        r.setMonth(1);
        r.setMileage(-5L);

        Set<ConstraintViolation<MileageRequest>> v = validator.validate(r);
        assertThat(v).isNotEmpty();
        assertThat(v).anyMatch(cv -> cv.getPropertyPath().toString().equals("mileage"));
    }

    @Test
    void missingFields_violateNotNull() {
        MileageRequest r = new MileageRequest();

        Set<ConstraintViolation<MileageRequest>> v = validator.validate(r);
        assertThat(v).isNotEmpty();
        assertThat(v).anyMatch(cv -> cv.getPropertyPath().toString().equals("vehicleId"));
        assertThat(v).anyMatch(cv -> cv.getPropertyPath().toString().equals("year"));
        assertThat(v).anyMatch(cv -> cv.getPropertyPath().toString().equals("month"));
    }
}
