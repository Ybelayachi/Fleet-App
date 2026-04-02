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

public class CreateUserRequestValidationTest {
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
    void validUser_noViolations() {
        CreateUserRequest r = new CreateUserRequest();
        r.setEmail("test@example.com");
        r.setPassword("secret");

        Set<ConstraintViolation<CreateUserRequest>> v = validator.validate(r);
        assertThat(v).isEmpty();
    }

    @Test
    void invalidEmail_andBlankPassword_violations() {
        CreateUserRequest r = new CreateUserRequest();
        r.setEmail("not-an-email");
        r.setPassword("");

        Set<ConstraintViolation<CreateUserRequest>> v = validator.validate(r);
        assertThat(v).isNotEmpty();
        assertThat(v).anyMatch(cv -> cv.getPropertyPath().toString().equals("email"));
        assertThat(v).anyMatch(cv -> cv.getPropertyPath().toString().equals("password"));
    }
}
