package com.fleetkm.repository;

import com.fleetkm.entity.AppUser;
import com.fleetkm.entity.MonthlyMileage;
import com.fleetkm.entity.Vehicle;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration pour MonthlyMileageRepository.
 * Vérifie la recherche par véhicule/période et par période uniquement.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
class MonthlyMileageRepositoryIT {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private MonthlyMileageRepository mileageRepository;

    private AppUser user;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        user = new AppUser();
        user.setEmail("driver-mileage@it.com");
        user.setPassword("encoded");
        user.setRole("ROLE_DRIVER");
        user.setActive(true);
        em.persist(user);

        vehicle = new Vehicle();
        vehicle.setVin("VIN-MILEAGE-IT-001");
        vehicle.setBrand("Volkswagen");
        vehicle.setModel("Golf");
        vehicle.setActive(true);
        em.persist(vehicle);

        em.flush();
    }

    @Test
    void findByVehicleAndYearAndMonth_retourneLEnregistrement() {
        MonthlyMileage m = buildMileage(vehicle, 2024, 3, 5000L);
        em.persist(m);
        em.flush();

        Optional<MonthlyMileage> result =
                mileageRepository.findByVehicleAndYearAndMonth(vehicle, 2024, 3);

        assertThat(result).isPresent();
        assertThat(result.get().getMileage()).isEqualTo(5000L);
    }

    @Test
    void findByVehicleAndYearAndMonth_retourneVideSiPeriodeInconnue() {
        Optional<MonthlyMileage> result =
                mileageRepository.findByVehicleAndYearAndMonth(vehicle, 2023, 12);

        assertThat(result).isEmpty();
    }

    @Test
    void findByVehicleAndYearAndMonth_retourneVideSiMauvaisVehicule() {
        MonthlyMileage m = buildMileage(vehicle, 2024, 6, 8000L);
        em.persist(m);

        Vehicle autreVehicle = new Vehicle();
        autreVehicle.setVin("VIN-AUTRE-MILEAGE-001");
        autreVehicle.setBrand("Seat");
        autreVehicle.setModel("Ibiza");
        autreVehicle.setActive(true);
        em.persist(autreVehicle);

        em.flush();

        Optional<MonthlyMileage> result =
                mileageRepository.findByVehicleAndYearAndMonth(autreVehicle, 2024, 6);

        assertThat(result).isEmpty();
    }

    @Test
    void findByYearAndMonth_retourneTousLesEnregistrementsDeLaPeriode() {
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setVin("VIN-MILEAGE-IT-002");
        vehicle2.setBrand("Citroën");
        vehicle2.setModel("C3");
        vehicle2.setActive(true);
        em.persist(vehicle2);

        em.persist(buildMileage(vehicle,  2024, 5, 10000L));
        em.persist(buildMileage(vehicle2, 2024, 5, 20000L));
        // Autre période — ne doit pas apparaître
        em.persist(buildMileage(vehicle,  2024, 4, 9500L));
        em.flush();

        List<MonthlyMileage> result = mileageRepository.findByYearAndMonth(2024, 5);

        assertThat(result).hasSize(2)
                .extracting(MonthlyMileage::getMileage)
                .containsExactlyInAnyOrder(10000L, 20000L);
    }

    @Test
    void findByYearAndMonth_page_retourneDonneesPagees() {
        em.persist(buildMileage(vehicle, 2024, 7, 15000L));
        em.flush();

        Page<MonthlyMileage> result = mileageRepository.findByYearAndMonth(2024, 7, Pageable.unpaged());

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getMileage()).isEqualTo(15000L);
    }

    @Test
    void findByVehicleInOrderByYearDescMonthDesc_triCorrectement() {
        em.persist(buildMileage(vehicle, 2023, 12, 1000L));
        em.persist(buildMileage(vehicle, 2024,  1, 2000L));
        em.persist(buildMileage(vehicle, 2024,  2, 3000L));
        em.flush();

        List<MonthlyMileage> result =
                mileageRepository.findByVehicleInOrderByYearDescMonthDesc(List.of(vehicle));

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getMileage()).isEqualTo(3000L); // 2024-02
        assertThat(result.get(1).getMileage()).isEqualTo(2000L); // 2024-01
        assertThat(result.get(2).getMileage()).isEqualTo(1000L); // 2023-12
    }

    // --- helper ---

    private MonthlyMileage buildMileage(final Vehicle v, final int year,
            final int month, final long mileage) {
        MonthlyMileage m = new MonthlyMileage();
        m.setVehicle(v);
        m.setYear(year);
        m.setMonth(month);
        m.setMileage(mileage);
        m.setDeclaredAt(OffsetDateTime.now());
        m.setDeclaredBy(user);
        return m;
    }
}
