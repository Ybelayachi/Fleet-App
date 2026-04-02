package com.fleetkm.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class SecurityConfigUnitTest {

    @Test
    void passwordEncoder_is_bcrypt() {
        SecurityConfig cfg = new SecurityConfig(mock(com.fleetkm.security.JwtAuthFilter.class));
        assertTrue(cfg.passwordEncoder() instanceof BCryptPasswordEncoder);
    }
}
