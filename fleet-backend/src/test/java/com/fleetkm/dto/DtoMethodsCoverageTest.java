package com.fleetkm.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DtoMethodsCoverageTest {

    @Test
    void authResponse_methods_cover_toString_equals_hash() {
        AuthResponse a = new AuthResponse("tkn");
        AuthResponse b = new AuthResponse("tkn");
        assertThat(a.toString()).isNotNull();
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void authRequest_methods_cover_getters_setters() {
        AuthRequest r1 = new AuthRequest();
        r1.setEmail("x@x.com");
        r1.setPassword("p");
        AuthRequest r2 = new AuthRequest();
        r2.setEmail("x@x.com");
        r2.setPassword("p");
        assertThat(r1.getEmail()).isEqualTo("x@x.com");
        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        assertThat(r1.toString()).contains("x@x.com");
    }

    @Test
    void createUser_request_methods() {
        CreateUserRequest c1 = new CreateUserRequest();
        c1.setEmail("a@b.com");
        c1.setPassword("pw");
        CreateUserRequest c2 = new CreateUserRequest();
        c2.setEmail("a@b.com");
        c2.setPassword("pw");
        assertThat(c1).isEqualTo(c2);
        assertThat(c1.toString()).contains("a@b.com");
    }

    @Test
    void mileage_request_methods() {
        MileageRequest m1 = new MileageRequest();
        m1.setVehicleId(2L);
        m1.setYear(2026);
        m1.setMonth(2);
        m1.setMileage(100L);
        MileageRequest m2 = new MileageRequest();
        m2.setVehicleId(2L);
        m2.setYear(2026);
        m2.setMonth(2);
        m2.setMileage(100L);
        assertThat(m1).isEqualTo(m2);
        assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        assertThat(m1.toString()).contains("vehicleId");
    }
}
