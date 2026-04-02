package com.fleetkm.controller;

import com.fleetkm.dto.CreateUserRequest;
import com.fleetkm.entity.AppUser;
import com.fleetkm.entity.Vehicle;
import com.fleetkm.entity.VehicleAssignment;
import com.fleetkm.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for admin operations.
 * Provides endpoints for managing users, vehicles, and assignments.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    /** Service for admin operations. */
    private final AdminService adminService;

    /**
     * Constructs AdminController with required dependencies.
     *
     * @param adminServiceBean the admin service
     */
    public AdminController(final AdminService adminServiceBean) {
        this.adminService = adminServiceBean;
    }

    /**
     * Retrieves all users.
     *
     * @param pageable the paging information
     * @return page of users
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AppUser> listUsers(@NonNull final Pageable pageable) {
        return adminService.listUsers(pageable);
    }

    /**
     * Creates a new user.
     *
     * @param request the user creation request
     * @return response entity with created user
     */
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(
            @jakarta.validation.Valid @RequestBody final CreateUserRequest request) {
        return ResponseEntity.ok(adminService.createUser(request));
    }

    /**
     * Retrieves all vehicles.
     *
     * @param pageable the paging information
     * @return page of vehicles
     */
    @GetMapping("/vehicles")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<Vehicle> listVehicles(@NonNull final Pageable pageable) {
        return adminService.listVehicles(pageable);
    }

    /**
     * Creates a new vehicle.
     *
     * @param vehicle the vehicle to create
     * @return response entity with created vehicle
     */
    @PostMapping("/vehicles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createVehicle(@RequestBody @NonNull final Vehicle vehicle) {
        return ResponseEntity.ok(adminService.createVehicle(vehicle));
    }

    /**
     * Assigns a vehicle to a user.
     *
     * @param request the assignment request
     * @return response entity with assigned vehicle
     */
    @PostMapping("/assignments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignVehicle(
            @RequestBody @NonNull final AssignRequest request) {
        VehicleAssignment assignment = adminService.assignVehicle(
                request.userId(), request.vehicleId());
        return ResponseEntity.ok(assignment);
    }

    /**
     * Request DTO for vehicle assignment.
     *
     * @param userId    the user ID
     * @param vehicleId the vehicle ID
     */
    public record AssignRequest(@NonNull Long userId, @NonNull Long vehicleId) { }
}
