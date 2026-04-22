package com.fleetkm.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetkm.entity.AppUser;
import com.fleetkm.entity.MonthlyMileage;
import com.fleetkm.entity.Vehicle;
import com.fleetkm.repository.AppUserRepository;
import com.fleetkm.repository.MonthlyMileageRepository;
import com.fleetkm.repository.VehicleRepository;
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

import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration des endpoints /api/fleet/*.
 * Vérifie les listes paginées, les véhicules manquants et l'export CSV.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FleetEndpointIT {

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
    private MonthlyMileageRepository mileageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String fleetManagerToken;
    private AppUser fleetManager;
    private Vehicle vehicleAvecMileage;
    private Vehicle vehicleSansMileage;
    private MonthlyMileage mileage;

    private static final int TEST_YEAR  = 2020;
    private static final int TEST_MONTH = 1;

    @BeforeEach
    void setUp() {
        // admin@example.com seedé par DataLoader
        adminToken = "Bearer " + jwtUtil.generateToken("admin@example.com");

        // Fleet manager
        fleetManager = new AppUser();
        fleetManager.setEmail("fm-fleet-ep-it@example.com");
        fleetManager.setPassword(passwordEncoder.encode("pass"));
        fleetManager.setRole("ROLE_FLEET_MANAGER");
        fleetManager.setActive(true);
        fleetManager = userRepository.save(fleetManager);
        fleetManagerToken = "Bearer " + jwtUtil.generateToken(fleetManager.getEmail());

        // Véhicule qui aura un kilométrage déclaré
        vehicleAvecMileage = new Vehicle();
        vehicleAvecMileage.setVin("VIN-FLEET-EP-WITH-KM");
        vehicleAvecMileage.setBrand("Renault");
        vehicleAvecMileage.setModel("Megane");
        vehicleAvecMileage.setActive(true);
        vehicleAvecMileage = vehicleRepository.save(vehicleAvecMileage);

        // Véhicule sans kilométrage déclaré pour TEST_YEAR/TEST_MONTH
        vehicleSansMileage = new Vehicle();
        vehicleSansMileage.setVin("VIN-FLEET-EP-NO-KM");
        vehicleSansMileage.setBrand("Peugeot");
        vehicleSansMileage.setModel("308");
        vehicleSansMileage.setActive(true);
        vehicleSansMileage = vehicleRepository.save(vehicleSansMileage);

        // Déclaration de kilométrage pour vehicleAvecMileage
        AppUser adminUser = userRepository.findByEmail("admin@example.com").orElseThrow();
        mileage = new MonthlyMileage();
        mileage.setVehicle(vehicleAvecMileage);
        mileage.setYear(TEST_YEAR);
        mileage.setMonth(TEST_MONTH);
        mileage.setMileage(50000L);
        mileage.setDeclaredAt(OffsetDateTime.now());
        mileage.setDeclaredBy(adminUser);
        mileage = mileageRepository.save(mileage);
    }

    @AfterEach
    void tearDown() {
        mileageRepository.findById(mileage.getId()).ifPresent(mileageRepository::delete);
        vehicleRepository.findAll().stream()
                .filter(v -> v.getVin().startsWith("VIN-FLEET-EP"))
                .forEach(vehicleRepository::delete);
        userRepository.findByEmail("fm-fleet-ep-it@example.com")
                .ifPresent(userRepository::delete);
    }

    // ─── GET /api/fleet/vehicles ─────────────────────────────────────────────

    @Test
    void allVehicles_avecRoleAdmin_retourneListePaginee() throws Exception {
        mvc.perform(get("/api/fleet/vehicles")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(2)));
    }

    @Test
    void allVehicles_avecPagination_retourneLaBonneTaille() throws Exception {
        mvc.perform(get("/api/fleet/vehicles")
                        .param("size", "1")
                        .param("page", "0")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", is(1)))
                .andExpect(jsonPath("$.number", is(0)));
    }

    // ─── GET /api/fleet/mileage ───────────────────────────────────────────────

    @Test
    void mileage_avecPeriodeConnue_retourneLesDeclarations() throws Exception {
        mvc.perform(get("/api/fleet/mileage")
                        .param("year",  String.valueOf(TEST_YEAR))
                        .param("month", String.valueOf(TEST_MONTH))
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
    }

    @Test
    void mileage_avecPeriodeVide_retourneListeVide() throws Exception {
        mvc.perform(get("/api/fleet/mileage")
                        .param("year",  "1900")
                        .param("month", "1")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test
    void mileage_sansParamYear_retourne400() throws Exception {
        mvc.perform(get("/api/fleet/mileage")
                        .param("month", "1")
                        .header("Authorization", adminToken))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /api/fleet/missing ───────────────────────────────────────────────

    @Test
    void missing_avecRoleAdmin_retourneVehiculesSansMileage() throws Exception {
        mvc.perform(get("/api/fleet/missing")
                        .param("year",  String.valueOf(TEST_YEAR))
                        .param("month", String.valueOf(TEST_MONTH))
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                // vehicleSansMileage doit figurer dans les résultats
                .andExpect(jsonPath(
                        "$.content[?(@.vin == 'VIN-FLEET-EP-NO-KM')]").exists());
    }

    @Test
    void missing_netExcluePasLeVehiculeAvecDeclaration() throws Exception {
        mvc.perform(get("/api/fleet/missing")
                        .param("year",  String.valueOf(TEST_YEAR))
                        .param("month", String.valueOf(TEST_MONTH))
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                // vehicleAvecMileage ne doit PAS figurer
                .andExpect(jsonPath(
                        "$.content[?(@.vin == 'VIN-FLEET-EP-WITH-KM')]").doesNotExist());
    }

    @Test
    void missing_avecRoleFleetManager_retourne200() throws Exception {
        mvc.perform(get("/api/fleet/missing")
                        .param("year",  String.valueOf(TEST_YEAR))
                        .param("month", String.valueOf(TEST_MONTH))
                        .header("Authorization", fleetManagerToken))
                .andExpect(status().isOk());
    }

    // ─── GET /api/fleet/export ────────────────────────────────────────────────

    @Test
    void exportCsv_avecDonnees_retourneFichierCsv() throws Exception {
        mvc.perform(get("/api/fleet/export")
                        .param("year",  String.valueOf(TEST_YEAR))
                        .param("month", String.valueOf(TEST_MONTH))
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/csv")));
    }

    @Test
    void exportCsv_contientEnTetesCsv() throws Exception {
        mvc.perform(get("/api/fleet/export")
                        .param("year",  String.valueOf(TEST_YEAR))
                        .param("month", String.valueOf(TEST_MONTH))
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("VIN")));
    }

    @Test
    void exportCsv_periodeVide_retourneFichierAvecSeulementEntetes() throws Exception {
        mvc.perform(get("/api/fleet/export")
                        .param("year",  "1900")
                        .param("month", "1")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/csv")));
    }
}
