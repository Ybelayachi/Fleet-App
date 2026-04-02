package com.fleetkm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JWT authentication filter.
 * Extracts and validates JWT tokens from requests.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    /** Logger for JWT authentication events. */
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthFilter.class);

    /** Length of "Bearer " prefix for JWT token headers. */
    private static final int BEARER_PREFIX_LENGTH = 7;

    /** JWT utility for token validation. */
    private final JwtUtil jwtUtil;
    /** User details service for loading user information. */
    private final CustomUserDetailsService userDetailsService;

    /**
     * Constructs JwtAuthFilter with required dependencies.
     *
     * @param utility the JWT utility
     * @param userService the user details service
     */
    public JwtAuthFilter(final JwtUtil utility,
            final CustomUserDetailsService userService) {
        this.jwtUtil = utility;
        this.userDetailsService = userService;
    }

    @Override
    protected final void doFilterInternal(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(BEARER_PREFIX_LENGTH);
            try {
                String username = jwtUtil.validate(token).getBody()
                        .getSubject();
                if (username != null && SecurityContextHolder.getContext()
                        .getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService
                            .loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null,
                                    userDetails.getAuthorities());
                    auth.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));
                    SecurityContextHolder.getContext()
                            .setAuthentication(auth);
                }
            } catch (Exception ex) {
                LOGGER.warn("Invalid JWT token: {}", ex.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
