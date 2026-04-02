package com.fleetkm.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT utility class for token generation and validation.
 * Provides methods to create and verify JWT tokens.
 */
@Component
public class JwtUtil {

    /** Signing key for JWT token generation. */
    private final Key key;
    /** Token expiration time in milliseconds. */
    private final long expirationMillis;

    /**
     * Constructs JwtUtil with JWT configuration.
     *
     * @param secret the JWT secret key
     * @param expirationMs token expiration time in milliseconds
     */
    public JwtUtil(
            @Value("${app.jwt.secret}")
            final String secret,
            @Value("${app.jwt.expiration-ms:86400000}")
            final long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMillis = expirationMs;
    }

    /**
     * Generates a JWT token for the given username.
     *
     * @param username the username to encode in the token
     * @return the generated JWT token
     */
    public String generateToken(final String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key)
                .compact();
    }

    /**
     * Validates and parses the JWT token.
     *
     * @param token the JWT token to validate
     * @return the parsed claims
     */
    public Jws<Claims> validate(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}
