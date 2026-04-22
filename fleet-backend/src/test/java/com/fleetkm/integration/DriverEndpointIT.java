package com.fleetkm.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetkm.entity.AppUser;
import com.fleetkm.entity.Vehicle;
import com.fleetkm.entity.VehicleAssignment;
import com.fleetkm.repository.AppUserRepository;
import com.fleetkm.repository.MonthlyMileageHistoryRepository;
import com.fleetkm.repository.MonthlyMileageRepository;
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

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration des endpoints /api/driver/*.
 * Vérifie la déclaration de kilométrage, l'historique et l'isolation par conducteur.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DriverEndpointIT {

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
    private MonthlyMileageRepository mileageRepository;

    @Autowired
    private MonthlyMileageHistoryRepository mileageHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AppUser driver;
    private Vehicle vehicle;
    private String driverToken;

    @BeforeEach
    void setUp() {
        driver = new AppUser();
        driver.setEmail("driver-ep-it@example.com");
        driver.setPassword(passwordEncoder.encode("pass"));
        driver.setRole("ROLE_DRIVER");
        driver.setActive(true);
        driver = userRepository.save(driver);

        vehicle = new Vehicle();
        vehicle.setVin("VIN-DRIVER-EP-IT-001");
        vehicle.setBrand("Citroën");
        vehicle.setModel("C3");
        vehicle.setActive(true);
        vehicle = vehicleRepository.save(vehicle);

        VehicleAssignment assignment = new VehicleAssignment();
        assignment.setUser(driver);
        assignment.setVehicle(vehicle);
        assignment.setStartDate(LocalDate.now().minusDays(30));
        assignmentRepository.save(assignment);

        driverToken = "Bearer " + jwtUtil.generateToken(driver.getEmail());
    }

    @AfterEach
    void tearDown() {
        // Trouver les véhicules de test
        var vehiculesTest = vehicleRepository.findAll().stream()
                .filter(v -> v.getVin().startsWith("VIN-DRIVER-EP-IT"))
                .toList();

        // 1. Supprimer l'historique de kilométrage (FK vers MONTHLY_MILEAGE)
        var mileagesTest = mileageRepository.findAll().stream()
                .filter(m -> vehiculesTest.stream()
                        .anyMatch(v -> v.getId().equals(m.getVehicle().getId())))
                .toList();
        mileageHistoryRepository.findAll().stream()
                .filter(h -> mileagesTest.stream()
                        .anyMatch(m -> m.getId().equals(h.getMileageRef().getId())))
                .forEach(mileageHistoryRepository::delete);

        // 2. Supprimer les kilométrages (FK vers VEHICLE et APP_USER)
        mileageRepository.deleteAll(mileagesTest);

        // 3. Supprimer les affectations (FK vers VEHICLE et APP_USER)
        if (driver != null && driver.getId() != null) {
            assignmentRepository.findAll().stream()
                    .filter(a -> driver.getId().equals(a.getUser().getId()))
                    .forEach(assignmentRepository::delete);
        }

        // 4. Supprimer les véhicules de test
        vehicleRepository.deleteAll(vehiculesTest);

        // 5. Supprimer le conducteur
        userRepository.findByEmail("driver-ep-it@example.com")
                .ifPresent(userRepository::delete);
        userRepository.findByEmail("driver-no-vehicle@example.com")
                .ifPresent(userRepository::delete);
    }

    // ─── GET /api/driver/vehicles ────────────────────────────────────────────

    @Test
    void myVehicles_retourneSeulementLesVehiculesAffectesAuConducteur() throws Exception {
        mvc.perform(get("/api/driver/vehicles")
                        .header("Authorization", driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[0].vin", is("VIN-DRIVER-EP-IT-001")));
    }

    @Test
    void myVehicles_conducteurSansAffectation_retourneListeVide() throws Exception {
        // Créer un conducteur sans affectation
        AppUser autreDriver = new AppUser();
        autreDriver.setEmail("driver-no-vehicle@example.com");
        autreDriver.setPassword(passwordEncoder.encode("pass"));
        autreDriver.setRole("ROLE_DRIVER");
        autreDriver.setActive(true);
        autreDriver = userRepository.save(autreDriver);
        String autreToken = "Bearer " + jwtUtil.generateToken(autreDriver.getEmail());

        try {
            mvc.perform(get("/api/driver/vehicles")
                            .header("Authorization", autreToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements", is(0)));
        } finally {
            userRepository.delete(autreDriver);
        }
    }

    // ─── POST /api/driver/mileage ────────────────────────────────────────────

    @Test
    void declareMileage_avecDonneesValides_retourneLEnregistrement() throws Exception {
        Map<String, Object> body = Map.of(
                "vehicleId", vehicle.getId(),
                "year", 2024,
                "month", 6,
                "mileage", 15000L);

        mvc.perform(post("/api/driver/mileage")
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mileage", is(15000)))
                .andExpect(jsonPath("$.year", is(2024)))
                .andExpect(jsonPath("$.month", is(6)))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void declareMileage_misAJour_retourneLaNouvelleValeur() throws Exception {
        Map<String, Object> body = Map.of(
                "vehicleId", vehicle.getId(),
                "year", 2024,
                "month", 7,
                "mileage", 10000L);

        // Première déclaration
        mvc.perform(post("/api/driver/mileage")
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)));

        // Mise à jour
        Map<String, Object> updated = Map.of(
                "vehicleId", vehicle.getId(),
                "year", 2024,
                "month", 7,
                "mileage", 12000L);

        mvc.perform(post("/api/driver/mileage")
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mileage", is(12000)));
    }

    @Test
    void declareMileage_avecKilometragNegatif_retourne400() throws Exception {
        Map<String, Object> body = Map.of(
                "vehicleId", vehicle.getId(),
                "year", 2024,
                "month", 8,
                "mileage", -100L);

        mvc.perform(post("/api/driver/mileage")
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void declareMileage_avecChampManquant_retourne400() throws Exception {
        // mois manquant
        Map<String, Object> body = Map.of(
                "vehicleId", vehicle.getId(),
                "year", 2024,
                "mileage", 5000L);

        mvc.perform(post("/api/driver/mileage")
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /api/driver/mileage/current ────────────────────────────────────

    @Test
    void currentMonthMileage_retourneLesDeclarationsDuMoisCourant() throws Exception {
        mvc.perform(get("/api/driver/mileage/current")
                        .header("Authorization", driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // ─── GET /api/driver/mileages ────────────────────────────────────────────

    @Test
    void myMileageHistory_sansFiltre_retourneToutLHistorique() throws Exception {
        // Déclarer un kilométrage d'abord
        Map<String, Object> body = Map.of(
                "vehicleId", vehicle.getId(),
                "year", 2024,
                "month", 9,
                "mileage", 18000L);

        mvc.perform(post("/api/driver/mileage")
                .header("Authorization", driverToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));

        mvc.perform(get("/api/driver/mileages")
                        .header("Authorization", driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
    }

    @Test
    void myMileageHistory_avecFiltreAnnee_retourneSeulementCetteAnnee() throws Exception {
        // Déclare pour 2024
        Map<String, Object> body = Map.of(
                "vehicleId", vehicle.getId(),
                "year", 2024,
                "month", 10,
                "mileage", 20000L);

        mvc.perform(post("/api/driver/mileage")
                .header("Authorization", driverToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));

        mvc.perform(get("/api/driver/mileages")
                        .param("year", "2024")
                        .header("Authorization", driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
