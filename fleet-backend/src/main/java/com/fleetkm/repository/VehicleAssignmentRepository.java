package com.fleetkm.repository;

import com.fleetkm.entity.VehicleAssignment;
import com.fleetkm.entity.AppUser;
import com.fleetkm.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VehicleAssignmentRepository
        extends JpaRepository<VehicleAssignment, Long> {
    /**
     * Finds all active vehicle assignments for a user.
     *
     * @param user the user (must not be null)
     * @return a list of active assignments (with null end date)
     */
    List<VehicleAssignment> findByUserAndEndDateIsNull(
            AppUser user);

    /**
     * Checks whether a vehicle already has an active assignment.
     *
     * @param vehicle the vehicle to check
     * @return true if the vehicle is currently assigned to a driver
     */
    boolean existsByVehicleAndEndDateIsNull(Vehicle vehicle);
}
