package com.fleetkm.repository;

import com.fleetkm.entity.AppUser;
import com.fleetkm.entity.MonthlyMileage;
import com.fleetkm.entity.Vehicle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration pour la requête JPQL custom de VehicleRepository.
 * Vérifie que findVehiclesWithoutMileage fonctionne correctement.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
class VehicleRepositoryIT {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Test
    void findVehiclesWithoutMileage_retourneVehicleSansDeclaration() {
        Vehicle vehicleSansMileage = buildVehicle("VIN-SANS-KM-001", "Renault", "Clio");
        em.persist(vehicleSansMileage);
        em.flush();

        Page<Vehicle> result = vehicleRepository.findVehiclesWithoutMileage(2024, 1, Pageable.unpaged());

        assertThat(result.getContent())
                .extracting(Vehicle::getVin)
                .contains("VIN-SANS-KM-001");
    }

    @Test
    void findVehiclesWithoutMileage_excludeVehicleAvecDeclaration() {
        AppUser user = buildUser("driver-repo@test.com");
        em.persist(user);

        Vehicle v = buildVehicle("VIN-AVEC-KM-001", "Peugeot", "208");
        em.persist(v);

        MonthlyMileage mileage = new MonthlyMileage();
        mileage.setVehicle(v);
        mileage.setYear(2024);
        mileage.setMonth(1);
        mileage.setMileage(12000L);
        mileage.setDeclaredAt(OffsetDateTime.now());
        mileage.setDeclaredBy(user);
        em.persist(mileage);

        em.flush();

        Page<Vehicle> result = vehicleRepository.findVehiclesWithoutMileage(2024, 1, Pageable.unpaged());

        assertThat(result.getContent())
                .extracting(Vehicle::getVin)
                .doesNotContain("VIN-AVEC-KM-001");
    }

    @Test
    void findVehiclesWithoutMileage_autresPeriodesSontIndependantes() {
        AppUser user = buildUser("driver-periode@test.com");
        em.persist(user);

        Vehicle v = buildVehicle("VIN-PERIODE-001", "Toyota", "Yaris");
        em.persist(v);

        // Déclaration pour janvier 2024
        MonthlyMileage mileage = new MonthlyMileage();
        mileage.setVehicle(v);
        mileage.setYear(2024);
        mileage.setMonth(1);
        mileage.setMileage(5000L);
        mileage.setDeclaredAt(OffsetDateTime.now());
        mileage.setDeclaredBy(user);
        em.persist(mileage);

        em.flush();

        // Pour février 2024, le véhicule n'a pas encore déclaré → doit apparaître
        Page<Vehicle> result = vehicleRepository.findVehiclesWithoutMileage(2024, 2, Pageable.unpaged());

        assertThat(result.getContent())
                .extracting(Vehicle::getVin)
                .contains("VIN-PERIODE-001");
    }

    // --- helpers ---

    private Vehicle buildVehicle(final String vin, final String brand, final String model) {
        Vehicle v = new Vehicle();
        v.setVin(vin);
        v.setBrand(brand);
        v.setModel(model);
        v.setActive(true);
        return v;
    }

    private AppUser buildUser(final String email) {
        AppUser u = new AppUser();
        u.setEmail(email);
        u.setPassword("encoded");
        u.setRole("ROLE_DRIVER");
        u.setActive(true);
        return u;
    }
}
