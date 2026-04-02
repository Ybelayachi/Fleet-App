package com.fleetkm.controller;

import com.fleetkm.dto.CreateUserRequest;
import com.fleetkm.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * REST controller for user registration.
 * Provides endpoint for creating new user accounts.
 */
@RestController
@RequestMapping("/api/auth")
public final class RegisterController {

    /** Service for authentication and registration operations. */
    private final AuthService authService;

    /**
     * Constructs RegisterController with required dependencies.
     *
     * @param authServiceBean the authentication service
     */
    public RegisterController(final AuthService authServiceBean) {
        this.authService = authServiceBean;
    }

    /**
     * Registers a new user.
     *
     * @param request the user registration request
     * @return response entity with registered user
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody final CreateUserRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            return ResponseEntity.badRequest().body(ex.getReason());
        }
    }
}
