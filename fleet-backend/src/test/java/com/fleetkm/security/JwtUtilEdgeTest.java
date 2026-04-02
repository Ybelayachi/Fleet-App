package com.fleetkm.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilEdgeTest {

    @Test
    void expired_token_throws_ExpiredJwtException() {
        JwtUtil util = new JwtUtil("01234567890123456789012345678901", -1000);
        String token = util.generateToken("user1");
        assertThrows(ExpiredJwtException.class, () -> util.validate(token));
    }

    @Test
    void invalid_signature_throws() {
        JwtUtil util1 = new JwtUtil("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1000000);
        JwtUtil util2 = new JwtUtil("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", 1000000);
        String token = util1.generateToken("user2");
        assertThrows(Exception.class, () -> util2.validate(token));
    }
}
