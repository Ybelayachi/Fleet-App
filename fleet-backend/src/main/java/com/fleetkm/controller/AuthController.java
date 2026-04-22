package com.fleetkm.controller;

import com.fleetkm.dto.AuthRequest;
import com.fleetkm.dto.ChangePasswordRequest;
import com.fleetkm.dto.ForgotPasswordRequest;
import com.fleetkm.dto.ResetPasswordRequest;
import com.fleetkm.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    /**
     * Changes the password for the currently authenticated user.
     *
     * @param request the change password request (currentPassword + newPassword)
     * @param principal the authenticated user
     * @return 200 on success, 400 if current password is wrong
     */
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody final ChangePasswordRequest request,
            final Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Non authentifié"));
        }
        try {
            authService.changePassword(principal.getName(), request);
            return ResponseEntity.ok(Map.of("status", "Mot de passe modifié avec succès"));
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("error", ex.getReason()));
        }
    }

    /**
     * Initiates a password reset by generating a one-time token (valid 1 hour).
     * In production, this token would be sent by email.
     *
     * @param request the forgot password request containing the user email
     * @return 200 with the reset token, or 404 if email not found
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody final ForgotPasswordRequest request) {
        try {
            String token = authService.forgotPassword(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Token de réinitialisation généré",
                    "token", token));
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("error", ex.getReason()));
        }
    }

    /**
     * Resets the password using a valid reset token.
     *
     * @param request the reset password request (token + newPassword)
     * @return 200 on success, 400 if token is invalid or expired
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody final ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok(
                    Map.of("status", "Mot de passe réinitialisé avec succès"));
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("error", ex.getReason()));
        }
    }
}
