package com.fleetkm.integration;

import com.fleetkm.entity.AppUser;
import com.fleetkm.repository.AppUserRepository;
import com.fleetkm.security.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration de la couche de sécurité.
 *
 * Vérifie que les règles @PreAuthorize et le filtre JWT sont bien appliqués :
 *   - Pas de token         → 401 Unauthorized
 *   - Mauvais token        → 401 Unauthorized
 *   - Bon token, mauvais rôle → 403 Forbidden
 *   - Bon token, bon rôle  → 200 OK
 *
 * admin@example.com (ROLE_ADMIN) est seedé par DataLoader au démarrage.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AppUser driverUser;
    private AppUser fleetManagerUser;

    @BeforeEach
    void setUp() {
        driverUser = new AppUser();
        driverUser.setEmail("driver-sec-it@example.com");
        driverUser.setPassword(passwordEncoder.encode("pass"));
        driverUser.setRole("ROLE_DRIVER");
        driverUser.setActive(true);
        userRepository.save(driverUser);

        fleetManagerUser = new AppUser();
        fleetManagerUser.setEmail("fm-sec-it@example.com");
        fleetManagerUser.setPassword(passwordEncoder.encode("pass"));
        fleetManagerUser.setRole("ROLE_FLEET_MANAGER");
        fleetManagerUser.setActive(true);
        userRepository.save(fleetManagerUser);
    }

    @AfterEach
    void tearDown() {
        userRepository.findByEmail("driver-sec-it@example.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("fm-sec-it@example.com").ifPresent(userRepository::delete);
    }

    // ─── Requêtes sans token → 403 (Http403ForbiddenEntryPoint par défaut) ─

    @Test
    void adminUsers_sansToken_retourne403() throws Exception {
        mvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void fleetVehicles_sansToken_retourne403() throws Exception {
        mvc.perform(get("/api/fleet/vehicles"))
                .andExpect(status().isForbidden());
    }

    @Test
    void driverVehicles_sansToken_retourne403() throws Exception {
        mvc.perform(get("/api/driver/vehicles"))
                .andExpect(status().isForbidden());
    }

    // ─── Token invalide → 403 (JwtAuthFilter swallow l'erreur) ─────────────

    @Test
    void adminUsers_avecTokenInvalide_retourne403() throws Exception {
        mvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer token.invalide.ici"))
                .andExpect(status().isForbidden());
    }

    // ─── Bon token, mauvais rôle → 403 ──────────────────────────────────────

    @Test
    void adminUsers_avecRoleDriver_retourne403() throws Exception {
        String token = jwtUtil.generateToken("driver-sec-it@example.com");

        mvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void fleetVehicles_avecRoleDriver_retourne403() throws Exception {
        String token = jwtUtil.generateToken("driver-sec-it@example.com");

        mvc.perform(get("/api/fleet/vehicles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void fleetVehicles_avecRoleFleetManager_retourne403() throws Exception {
        // GET /api/fleet/vehicles exige ROLE_ADMIN uniquement
        String token = jwtUtil.generateToken("fm-sec-it@example.com");

        mvc.perform(get("/api/fleet/vehicles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ─── Bon token, bon rôle → 200 ──────────────────────────────────────────

    @Test
    void adminUsers_avecRoleAdmin_retourne200() throws Exception {
        // admin@example.com seedé par DataLoader
        String token = jwtUtil.generateToken("admin@example.com");

        mvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void fleetVehicles_avecRoleAdmin_retourne200() throws Exception {
        String token = jwtUtil.generateToken("admin@example.com");

        mvc.perform(get("/api/fleet/vehicles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void fleetMissing_avecRoleFleetManager_retourne200() throws Exception {
        // GET /api/fleet/missing accepte ROLE_ADMIN ou ROLE_FLEET_MANAGER
        String token = jwtUtil.generateToken("fm-sec-it@example.com");

        mvc.perform(get("/api/fleet/missing")
                        .param("year", "2024")
                        .param("month", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void driverVehicles_avecRoleDriver_retourne200() throws Exception {
        String token = jwtUtil.generateToken("driver-sec-it@example.com");

        mvc.perform(get("/api/driver/vehicles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void authMe_avecRoleAdmin_retourneProfile() throws Exception {
        String token = jwtUtil.generateToken("admin@example.com");

        mvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
