package com.fleetkm.controller;

import com.fleetkm.dto.AuthRequest;
import com.fleetkm.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.security.Principal;

/**
 * REST controller for authentication operations.
 * Provides endpoints for user login and logout.
 */
@RestController
@RequestMapping("/api/auth")
public final class AuthController {

    /** Service for authentication operations. */
    private final AuthService authService;

    /**
     * Constructs AuthController with required dependencies.
     *
     * @param authServiceBean the authentication service
     */
    public AuthController(final AuthService authServiceBean) {
        this.authService = authServiceBean;
    }

    /**
     * Authenticates user with provided credentials.
     *
     * @param request the authentication request
     * @return response entity with JWT token or error
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody final AuthRequest request) {
        try {
            return ResponseEntity.ok(
                    authService.login(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Identifiants invalides"));
        }
    }

    /**
     * Logs out the current user.
     *
     * @return response entity with status
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Stateless JWT: recommend client delete token.
        // For production, implement token revocation if needed.
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    /**
     * Returns current authenticated user profile (email and role).
     *
     * @param principal current authenticated principal
     * @return profile information or 401 if unauthenticated
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(final Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Non authentifié"));
        }
        try {
            return ResponseEntity.ok(authService.getProfile(principal.getName()));
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("error", ex.getReason()));
        }
    }
}
