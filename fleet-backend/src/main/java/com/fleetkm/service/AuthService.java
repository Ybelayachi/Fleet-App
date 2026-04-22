package com.fleetkm.service;

import com.fleetkm.dto.AuthResponse;
import com.fleetkm.dto.ChangePasswordRequest;
import com.fleetkm.dto.CreateUserRequest;
import com.fleetkm.dto.ForgotPasswordRequest;
import com.fleetkm.dto.ResetPasswordRequest;
import com.fleetkm.entity.AppUser;
import com.fleetkm.entity.PasswordResetToken;
import com.fleetkm.repository.AppUserRepository;
import com.fleetkm.repository.PasswordResetTokenRepository;
import com.fleetkm.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
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
    /** Repository for password reset tokens. */
    private final PasswordResetTokenRepository resetTokenRepo;

    /**
     * Constructs AuthService with required dependencies.
     *
     * @param authenticationManager the authentication manager
     * @param userRepository the user repository
     * @param jwtUtility the JWT utility
     * @param encoder the password encoder
     * @param resetTokenRepository the password reset token repository
     */
    public AuthService(final AuthenticationManager authenticationManager,
            final AppUserRepository userRepository,
            final JwtUtil jwtUtility,
            final PasswordEncoder encoder,
            final PasswordResetTokenRepository resetTokenRepository) {
        this.authManager = authenticationManager;
        this.userRepo = userRepository;
        this.jwtUtil = jwtUtility;
        this.passwordEncoder = encoder;
        this.resetTokenRepo = resetTokenRepository;
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
     * Changes the password for the authenticated user.
     *
     * @param email the authenticated user's email
     * @param request the change password request
     * @throws ResponseStatusException with 400 if current password is wrong
     * @throws ResponseStatusException with 404 if user not found
     */
    public void changePassword(final String email, final ChangePasswordRequest request) {
        AppUser user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Mot de passe actuel incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);
    }

    /**
     * Generates a password reset token for the given email.
     * The token is valid for 1 hour. Any previous tokens for the user are deleted.
     * NOTE: In production, send this token by email instead of returning it in the response.
     *
     * @param request the forgot password request containing the email
     * @return the reset token string
     * @throws ResponseStatusException with 404 if email is not found
     */
    @Transactional
    public String forgotPassword(final ForgotPasswordRequest request) {
        AppUser user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Aucun compte associé à cet email"));
        resetTokenRepo.deleteByUserId(user.getId());
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        resetTokenRepo.save(new PasswordResetToken(token, user, LocalDateTime.now().plusHours(1)));
        return token;
    }

    /**
     * Resets the password using a valid reset token.
     *
     * @param request the reset password request (token + newPassword)
     * @throws ResponseStatusException with 400 if the token is invalid, expired or already used
     */
    @Transactional
    public void resetPassword(final ResetPasswordRequest request) {
        PasswordResetToken prt = resetTokenRepo.findByToken(request.getToken())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Token invalide ou expiré"));
        if (prt.isUsed() || prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token invalide ou expiré");
        }
        prt.getUser().setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(prt.getUser());
        prt.setUsed(true);
        resetTokenRepo.save(prt);
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
