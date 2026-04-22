package com.fleetkm.repository;

import com.fleetkm.entity.AppUser;
import com.fleetkm.entity.Vehicle;
import com.fleetkm.entity.VehicleAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration pour VehicleAssignmentRepository.
 * Vérifie les requêtes d'affectations actives et la détection de conflit.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
class VehicleAssignmentRepositoryIT {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private VehicleAssignmentRepository assignmentRepository;

    private AppUser driver;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        driver = new AppUser();
        driver.setEmail("driver-assign@it.com");
        driver.setPassword("encoded");
        driver.setRole("ROLE_DRIVER");
        driver.setActive(true);
        em.persist(driver);

        vehicle = new Vehicle();
        vehicle.setVin("VIN-ASSIGN-IT-001");
        vehicle.setBrand("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setActive(true);
        em.persist(vehicle);

        em.flush();
    }

    @Test
    void findByUserAndEndDateIsNull_retourneAffectationActive() {
        VehicleAssignment active = new VehicleAssignment();
        active.setUser(driver);
        active.setVehicle(vehicle);
        active.setStartDate(LocalDate.now().minusDays(10));
        em.persist(active);
        em.flush();

        List<VehicleAssignment> result = assignmentRepository.findByUserAndEndDateIsNull(driver);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVehicle().getVin()).isEqualTo("VIN-ASSIGN-IT-001");
    }

    @Test
    void findByUserAndEndDateIsNull_exclutAffectationTerminee() {
        VehicleAssignment closed = new VehicleAssignment();
        closed.setUser(driver);
        closed.setVehicle(vehicle);
        closed.setStartDate(LocalDate.now().minusMonths(2));
        closed.setEndDate(LocalDate.now().minusMonths(1));
        em.persist(closed);
        em.flush();

        List<VehicleAssignment> result = assignmentRepository.findByUserAndEndDateIsNull(driver);

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserAndEndDateIsNull_retourneSeulementCellesDeLetUtilisateur() {
        // Autre conducteur
        AppUser autreDriver = new AppUser();
        autreDriver.setEmail("autre-driver@it.com");
        autreDriver.setPassword("encoded");
        autreDriver.setRole("ROLE_DRIVER");
        autreDriver.setActive(true);
        em.persist(autreDriver);

        Vehicle autreVehicle = new Vehicle();
        autreVehicle.setVin("VIN-AUTRE-DRIVER-001");
        autreVehicle.setBrand("Ford");
        autreVehicle.setModel("Focus");
        autreVehicle.setActive(true);
        em.persist(autreVehicle);

        VehicleAssignment affectationAutre = new VehicleAssignment();
        affectationAutre.setUser(autreDriver);
        affectationAutre.setVehicle(autreVehicle);
        affectationAutre.setStartDate(LocalDate.now());
        em.persist(affectationAutre);
        em.flush();

        List<VehicleAssignment> result = assignmentRepository.findByUserAndEndDateIsNull(driver);

        assertThat(result).isEmpty();
    }

    @Test
    void existsByVehicleAndEndDateIsNull_retourneTrueSiAffectationActive() {
        VehicleAssignment active = new VehicleAssignment();
        active.setUser(driver);
        active.setVehicle(vehicle);
        active.setStartDate(LocalDate.now());
        em.persist(active);
        em.flush();

        boolean result = assignmentRepository.existsByVehicleAndEndDateIsNull(vehicle);

        assertThat(result).isTrue();
    }

    @Test
    void existsByVehicleAndEndDateIsNull_retourneFalseSiAucuneAffectationActive() {
        boolean result = assignmentRepository.existsByVehicleAndEndDateIsNull(vehicle);

        assertThat(result).isFalse();
    }

    @Test
    void existsByVehicleAndEndDateIsNull_retourneFalseSiAffectationTerminee() {
        VehicleAssignment closed = new VehicleAssignment();
        closed.setUser(driver);
        closed.setVehicle(vehicle);
        closed.setStartDate(LocalDate.now().minusMonths(1));
        closed.setEndDate(LocalDate.now().minusDays(1));
        em.persist(closed);
        em.flush();

        boolean result = assignmentRepository.existsByVehicleAndEndDateIsNull(vehicle);

        assertThat(result).isFalse();
    }
}
