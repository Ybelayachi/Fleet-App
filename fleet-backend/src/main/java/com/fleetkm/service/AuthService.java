package com.fleetkm.service;

import com.fleetkm.dto.AuthResponse;
import com.fleetkm.dto.CreateUserRequest;
import com.fleetkm.entity.AppUser;
import com.fleetkm.repository.AppUserRepository;
import com.fleetkm.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Service for authentication and user registration operations.
 * Handles login, registration and profile retrieval.
 */
@Service
public class AuthService {

    /** Authentication manager for credentials validation. */
    private final AuthenticationManager authManager;
    /** Repository for user data. */
    private final AppUserRepository userRepo;
    /** JWT utility for token generation. */
    private final JwtUtil jwtUtil;
    /** Password encoder for secure password storage. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs AuthService with required dependencies.
     *
     * @param authenticationManager the authentication manager
     * @param userRepository the user repository
     * @param jwtUtility the JWT utility
     * @param encoder the password encoder
     */
    public AuthService(final AuthenticationManager authenticationManager,
            final AppUserRepository userRepository,
            final JwtUtil jwtUtility,
            final PasswordEncoder encoder) {
        this.authManager = authenticationManager;
        this.userRepo = userRepository;
        this.jwtUtil = jwtUtility;
        this.passwordEncoder = encoder;
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param email the user email
     * @param password the user password
     * @return authentication response containing the JWT token
     * @throws ResponseStatusException with 401 if credentials are invalid
     */
    public AuthResponse login(final String email, final String password) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        AppUser user = userRepo.findByEmail(email).orElseThrow();
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token);
    }

    /**
     * Registers a new user account.
     *
     * @param request the registration request with user details
     * @return the created user (password cleared)
     * @throws ResponseStatusException with 400 if email already in use
     */
    public AppUser register(final CreateUserRequest request) {
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'utilisateur existe déjà");
        }
        AppUser user = new AppUser();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole() == null ? "ROLE_DRIVER" : request.getRole());
        user.setActive(true);
        return userRepo.save(user);
    }

    /**
     * Returns the profile (email and role) for the given email address.
     *
     * @param email the authenticated user's email
     * @return map with email and role keys
     * @throws ResponseStatusException with 404 if user not found
     */
    public Map<String, String> getProfile(final String email) {
        AppUser user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        return Map.of("email", user.getEmail(), "role", user.getRole());
    }
}
