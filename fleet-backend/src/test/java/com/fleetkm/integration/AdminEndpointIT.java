package com.fleetkm.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetkm.entity.AppUser;
import com.fleetkm.entity.Vehicle;
import com.fleetkm.repository.AppUserRepository;
import com.fleetkm.repository.VehicleAssignmentRepository;
import com.fleetkm.repository.VehicleRepository;
import com.fleetkm.security.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration des endpoints /api/admin/*.
 * Vérifie la logique métier complète : création, liste, affectation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminEndpointIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleAssignmentRepository assignmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void setUp() {
        // admin@example.com est seedé par DataLoader
        adminToken = "Bearer " + jwtUtil.generateToken("admin@example.com");
    }

    @AfterEach
    void tearDown() {
        // 1. Supprimer les affectations liées aux véhicules de test
        vehicleRepository.findAll().stream()
                .filter(v -> v.getVin().startsWith("VIN-ADMIN-IT"))
                .forEach(v -> assignmentRepository.findAll().stream()
                        .filter(a -> v.getId().equals(a.getVehicle().getId()))
                        .forEach(assignmentRepository::delete));
        // 2. Supprimer les véhicules de test
        vehicleRepository.findAll().stream()
                .filter(v -> v.getVin().startsWith("VIN-ADMIN-IT"))
                .forEach(vehicleRepository::delete);
        // 3. Supprimer les utilisateurs de test
        userRepository.findByEmail("nouveau-admin-it@example.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("driver-admin-it@example.com").ifPresent(userRepository::delete);
    }

    // ─── GET /api/admin/users ────────────────────────────────────────────────

    @Test
    void listUsers_retournePageAvecAuMoinsLAdmin() throws Exception {
        mvc.perform(get("/api/admin/users")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
    }

    @Test
    void listUsers_avecPagination_retourneLaBonnePage() throws Exception {
        mvc.perform(get("/api/admin/users")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", is(5)))
                .andExpect(jsonPath("$.number", is(0)));
    }

    // ─── POST /api/admin/users ───────────────────────────────────────────────

    @Test
    void createUser_avecDonneesValides_retourneLUtilisateurCree() throws Exception {
        Map<String, Object> body = Map.of(
                "email", "nouveau-admin-it@example.com",
                "password", "Pass1234!",
                "firstName", "Jean",
                "lastName", "Dupont",
                "role", "ROLE_DRIVER");

        mvc.perform(post("/api/admin/users")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("nouveau-admin-it@example.com")))
                .andExpect(jsonPath("$.firstName", is("Jean")))
                .andExpect(jsonPath("$.role", is("ROLE_DRIVER")))
                // le mot de passe ne doit jamais être exposé
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void createUser_avecEmailInvalide_retourne400() throws Exception {
        Map<String, Object> body = Map.of(
                "email", "pas-un-email",
                "password", "Pass1234!");

        mvc.perform(post("/api/admin/users")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_sansPassword_retourne400() throws Exception {
        Map<String, Object> body = Map.of(
                "email", "test-no-pass@example.com");

        mvc.perform(post("/api/admin/users")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /api/admin/vehicles ─────────────────────────────────────────────

    @Test
    void listVehicles_retournePageDeVehicules() throws Exception {
        mvc.perform(get("/api/admin/vehicles")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // ─── POST /api/admin/vehicles ────────────────────────────────────────────

    @Test
    void createVehicle_avecDonneesValides_retourneLaVehicleCree() throws Exception {
        Map<String, Object> body = Map.of(
                "vin", "VIN-ADMIN-IT-CREATE1",
                "brand", "Renault",
                "model", "Clio",
                "licensePlate", "AB-123-CD",
                "active", true);

        mvc.perform(post("/api/admin/vehicles")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vin", is("VIN-ADMIN-IT-CREATE1")))
                .andExpect(jsonPath("$.brand", is("Renault")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    // ─── POST /api/admin/assignments ─────────────────────────────────────────

    @Test
    void assignVehicle_avecIdsValides_retourneLAffectation() throws Exception {
        // Créer un conducteur
        AppUser driver = new AppUser();
        driver.setEmail("driver-admin-it@example.com");
        driver.setPassword(passwordEncoder.encode("pass"));
        driver.setRole("ROLE_DRIVER");
        driver.setActive(true);
        AppUser savedDriver = userRepository.save(driver);

        // Créer un véhicule
        Vehicle vehicle = new Vehicle();
        vehicle.setVin("VIN-ADMIN-IT-ASSIGN1");
        vehicle.setBrand("Peugeot");
        vehicle.setModel("208");
        vehicle.setActive(true);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        Map<String, Long> body = Map.of(
                "userId", savedDriver.getId(),
                "vehicleId", savedVehicle.getId());

        mvc.perform(post("/api/admin/assignments")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void assignVehicle_dejaAffecte_retourne409() throws Exception {
        // Créer conducteur + véhicule
        AppUser driver = new AppUser();
        driver.setEmail("driver-admin-it@example.com");
        driver.setPassword(passwordEncoder.encode("pass"));
        driver.setRole("ROLE_DRIVER");
        driver.setActive(true);
        AppUser savedDriver = userRepository.save(driver);

        Vehicle vehicle = new Vehicle();
        vehicle.setVin("VIN-ADMIN-IT-CONFLICT1");
        vehicle.setBrand("Toyota");
        vehicle.setModel("Yaris");
        vehicle.setActive(true);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        Map<String, Long> body = Map.of(
                "userId", savedDriver.getId(),
                "vehicleId", savedVehicle.getId());

        // Première affectation — OK
        mvc.perform(post("/api/admin/assignments")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        // Deuxième affectation du même véhicule — 409 Conflict
        mvc.perform(post("/api/admin/assignments")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    @Test
    void assignVehicle_avecUserIdInexistant_retourne400() throws Exception {
        Map<String, Long> body = Map.of(
                "userId", 99999L,
                "vehicleId", 99999L);

        mvc.perform(post("/api/admin/assignments")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
