package com.fleetkm.service;

import com.fleetkm.dto.CreateUserRequest;
import com.fleetkm.entity.AppUser;
import com.fleetkm.entity.Vehicle;
import com.fleetkm.entity.VehicleAssignment;
import com.fleetkm.repository.AppUserRepository;
import com.fleetkm.repository.VehicleAssignmentRepository;
import com.fleetkm.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

/**
 * Service for admin operations.
 * Handles user management, vehicle management and vehicle assignments.
 */
@Service
public class AdminService {

    /** Repository for user data. */
    private final AppUserRepository userRepo;
    /** Repository for vehicle data. */
    private final VehicleRepository vehicleRepo;
    /** Password encoder for secure password storage. */
    private final PasswordEncoder passwordEncoder;
    /** Repository for vehicle assignment data. */
    private final VehicleAssignmentRepository assignmentRepo;

    /**
     * Constructs AdminService with required dependencies.
     *
     * @param userRepository the user repository
     * @param vehicleRepository the vehicle repository
     * @param encoder the password encoder
     * @param assignmentRepository the vehicle assignment repository
     */
    public AdminService(final AppUserRepository userRepository,
            final VehicleRepository vehicleRepository,
            final PasswordEncoder encoder,
            final VehicleAssignmentRepository assignmentRepository) {
        this.userRepo = userRepository;
        this.vehicleRepo = vehicleRepository;
        this.passwordEncoder = encoder;
        this.assignmentRepo = assignmentRepository;
    }

    /**
     * Returns a paginated list of all users.
     *
     * @param pageable paging parameters
     * @return page of users
     */
    public Page<AppUser> listUsers(@NonNull final Pageable pageable) {
        return userRepo.findAll(pageable);
    }

    /**
     * Creates a new user with an encoded password.
     *
     * @param request the user creation request
     * @return the saved user
     */
    public AppUser createUser(final CreateUserRequest request) {
        AppUser user = new AppUser();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole() == null ? "ROLE_DRIVER" : request.getRole());
        user.setActive(true);
        return userRepo.save(user);
    }

    /**
     * Returns a paginated list of all vehicles.
     *
     * @param pageable paging parameters
     * @return page of vehicles
     */
    public Page<Vehicle> listVehicles(@NonNull final Pageable pageable) {
        return vehicleRepo.findAll(pageable);
    }

    /**
     * Saves a new vehicle.
     *
     * @param vehicle the vehicle to save
     * @return the saved vehicle
     */
    public Vehicle createVehicle(@NonNull final Vehicle vehicle) {
        return vehicleRepo.save(vehicle);
    }

    /**
     * Assigns a vehicle to a user starting from today.
     *
     * @param userId the ID of the user
     * @param vehicleId the ID of the vehicle
     * @return the created vehicle assignment
     * @throws ResponseStatusException if user or vehicle is not found
     */
    @Transactional
    public VehicleAssignment assignVehicle(@NonNull final Long userId,
            @NonNull final Long vehicleId) {
        AppUser user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Utilisateur introuvable"));
        Vehicle vehicle = vehicleRepo.findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Véhicule introuvable"));
        VehicleAssignment assignment = new VehicleAssignment();
        assignment.setUser(user);
        assignment.setVehicle(vehicle);
        assignment.setStartDate(LocalDate.now());
        assignment.setEndDate(null);
        return assignmentRepo.save(assignment);
    }
}
