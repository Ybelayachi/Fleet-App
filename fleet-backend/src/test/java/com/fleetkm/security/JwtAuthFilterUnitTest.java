package com.fleetkm.security;

import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"null", "unchecked"})
public class JwtAuthFilterUnitTest {

    @BeforeEach
    void clearContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void valid_token_sets_authentication() throws Exception {
        JwtUtil jwt = mock(JwtUtil.class);
        CustomUserDetailsService uds = mock(CustomUserDetailsService.class);
        JwtAuthFilter filter = new JwtAuthFilter(jwt, uds);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getHeader("Authorization")).thenReturn("Bearer token123");
        Jws<Claims> jws = mock(Jws.class);
        Claims claims = mock(Claims.class);
        when(jws.getBody()).thenReturn(claims);
        when(claims.getSubject()).thenReturn("alice");
        when(jwt.validate("token123")).thenReturn(jws);

        UserDetails ud = mock(UserDetails.class);
        when(uds.loadUserByUsername("alice")).thenReturn(ud);
        when(ud.getAuthorities()).thenReturn(java.util.Collections.emptyList());

        filter.doFilterInternal(req, (HttpServletResponse) resp, chain);

        assertNotNull(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(req, resp);
    }

    @Test
    void invalid_token_does_not_set_authentication() throws Exception {
        JwtUtil jwt = mock(JwtUtil.class);
        CustomUserDetailsService uds = mock(CustomUserDetailsService.class);
        JwtAuthFilter filter = new JwtAuthFilter(jwt, uds);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getHeader("Authorization")).thenReturn("Bearer badtoken");
        when(jwt.validate("badtoken")).thenThrow(new io.jsonwebtoken.JwtException("bad"));

        filter.doFilterInternal(req, (HttpServletResponse) resp, chain);

        assertNull(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(req, resp);
    }

    @Test
    void username_not_found_does_not_throw() throws Exception {
        JwtUtil jwt = mock(JwtUtil.class);
        CustomUserDetailsService uds = mock(CustomUserDetailsService.class);
        JwtAuthFilter filter = new JwtAuthFilter(jwt, uds);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getHeader("Authorization")).thenReturn("Bearer tokenX");
        Jws<Claims> jws = mock(Jws.class);
        Claims claims = mock(Claims.class);
        when(jws.getBody()).thenReturn(claims);
        when(claims.getSubject()).thenReturn("bob");
        when(jwt.validate("tokenX")).thenReturn(jws);

        when(uds.loadUserByUsername("bob")).thenThrow(new UsernameNotFoundException("nope"));

        filter.doFilterInternal(req, resp, (FilterChain) chain);

        assertNull(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(req, resp);
    }
}
