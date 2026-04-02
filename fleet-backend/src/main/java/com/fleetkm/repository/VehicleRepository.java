package com.fleetkm.repository;

import com.fleetkm.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    /**
     * Returns vehicles that have no mileage entry for the given year and month.
     *
     * @param year     the year
     * @param month    the month
     * @param pageable paging parameters
     * @return page of vehicles missing a mileage declaration
     */
    @Query("SELECT v FROM Vehicle v WHERE v.id NOT IN "
            + "(SELECT m.vehicle.id FROM MonthlyMileage m "
            + "WHERE m.year = :year AND m.month = :month)")
    Page<Vehicle> findVehiclesWithoutMileage(
            @Param("year") int year,
            @Param("month") int month,
            Pageable pageable);
}
