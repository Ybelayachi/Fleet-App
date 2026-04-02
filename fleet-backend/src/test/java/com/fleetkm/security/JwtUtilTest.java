package com.fleetkm.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtUtilTest {

    @Test
    void generate_and_validate_token() {
        String secret = "01234567890123456789012345678901"; // 32 chars
        JwtUtil util = new JwtUtil(secret, 3600000);

        String token = util.generateToken("alice");
        Jws<Claims> parsed = util.validate(token);
        assertThat(parsed.getBody().getSubject()).isEqualTo("alice");
    }
}
