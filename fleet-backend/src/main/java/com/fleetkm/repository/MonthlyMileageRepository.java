package com.fleetkm.repository;

import com.fleetkm.entity.MonthlyMileage;
import com.fleetkm.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface MonthlyMileageRepository
        extends JpaRepository<MonthlyMileage, Long> {
    /**
     * Finds a monthly mileage record by vehicle, year, and month.
     *
     * @param vehicle the vehicle (must not be null)
     * @param year the year (must not be null)
     * @param month the month (must not be null)
     * @return an Optional containing the record if found
     */
    Optional<MonthlyMileage> findByVehicleAndYearAndMonth(
            Vehicle vehicle, Integer year, Integer month);

    /**
     * Finds all monthly mileage records for a list of vehicles,
     * ordered by year and month descending.
     *
     * @param vehicles the vehicles list (must not be null)
     * @return a list of mileage records
     */
    List<MonthlyMileage> findByVehicleInOrderByYearDescMonthDesc(
            List<Vehicle> vehicles);

    /**
     * Finds all monthly mileage records for a specific year and month.
     *
     * @param year the year (must not be null)
     * @param month the month (must not be null)
     * @return a list of mileage records
     */
    List<MonthlyMileage> findByYearAndMonth(
            Integer year, Integer month);

    /**
     * Returns paginated mileage records for a specific year and month.
     *
     * @param year the year (must not be null)
     * @param month the month (must not be null)
     * @param pageable paging parameters
     * @return page of mileage records
     */
    Page<MonthlyMileage> findByYearAndMonth(
            Integer year, Integer month, Pageable pageable);
}
