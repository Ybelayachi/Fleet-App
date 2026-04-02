package com.fleetkm.service;

import com.fleetkm.dto.MileageRequest;
import com.fleetkm.entity.AppUser;
import com.fleetkm.entity.MonthlyMileage;
import com.fleetkm.entity.Vehicle;
import com.fleetkm.repository.AppUserRepository;
import com.fleetkm.repository.MonthlyMileageRepository;
import com.fleetkm.repository.VehicleAssignmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

/**
 * Service for driver-specific operations.
 * Handles retrieval of assigned vehicles and driver mileage history.
 */
@Service
@SuppressWarnings("null")
public class DriverService {

    /** Repository for vehicle assignment data. */
    private final VehicleAssignmentRepository assignmentRepo;
    /** Repository for user data. */
    private final AppUserRepository userRepo;
    /** Repository for monthly mileage data. */
    private final MonthlyMileageRepository mileageRepo;
    /** Service for mileage declaration business logic. */
    private final MileageService mileageService;

    /**
     * Constructs DriverService with required dependencies.
     *
     * @param assignmentRepository the vehicle assignment repository
     * @param userRepository the user repository
     * @param monthlyMileageRepository the monthly mileage repository
     * @param mileageServiceBean the mileage service
     */
    public DriverService(final VehicleAssignmentRepository assignmentRepository,
            final AppUserRepository userRepository,
            final MonthlyMileageRepository monthlyMileageRepository,
            final MileageService mileageServiceBean) {
        this.assignmentRepo = assignmentRepository;
        this.userRepo = userRepository;
        this.mileageRepo = monthlyMileageRepository;
        this.mileageService = mileageServiceBean;
    }

    /**
     * Returns a paginated list of vehicles currently assigned to the driver.
     *
     * @param email the driver's email address
     * @param pageable paging parameters
     * @return page of vehicles assigned to the driver
     */
    public Page<Vehicle> getAssignedVehicles(final String email,
            final Pageable pageable) {
        AppUser user = userRepo.findByEmail(email).orElseThrow();
        List<Vehicle> vehicles = assignmentRepo
                .findByUserAndEndDateIsNull(user).stream()
                .map(a -> Objects.requireNonNull(a.getVehicle()))
                .toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), vehicles.size());
        return new PageImpl<>(vehicles.subList(start, end), pageable, vehicles.size());
    }

    /**
     * Returns paginated mileage history for the driver's assigned vehicles.
     * Optionally filtered by year and/or month.
     *
     * @param email the driver's email address
     * @param year optional year filter
     * @param month optional month filter
     * @param pageable paging parameters
     * @return page of mileage records
     */
    public Page<MonthlyMileage> getMileageHistory(final String email,
            final Integer year, final Integer month, final Pageable pageable) {
        AppUser user = userRepo.findByEmail(email).orElseThrow();
        List<Vehicle> assignedVehicles = assignmentRepo
                .findByUserAndEndDateIsNull(user).stream()
                .map(a -> Objects.requireNonNull(a.getVehicle()))
                .distinct()
                .toList();

        List<MonthlyMileage> allEntries = assignedVehicles.isEmpty()
                ? List.of()
                : mileageRepo.findByVehicleInOrderByYearDescMonthDesc(assignedVehicles);

        List<MonthlyMileage> filtered = allEntries.stream()
                .filter(e -> year == null || year.equals(e.getYear()))
                .filter(e -> month == null || month.equals(e.getMonth()))
                .toList();

        int start = (int) pageable.getOffset();
        if (start >= filtered.size()) {
            return new PageImpl<>(List.of(), pageable, filtered.size());
        }
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }

    /**
     * Validates that the vehicle belongs to the driver and declares mileage.
     *
     * @param email the driver's email address
     * @param mileageReq the mileage declaration request
     * @return the saved mileage record
     * @throws ResponseStatusException with 403 if vehicle is not assigned to the driver
     */
    public MonthlyMileage declareMileage(final String email,
            final MileageRequest mileageReq) {
        AppUser user = userRepo.findByEmail(email).orElseThrow();
        boolean assigned = assignmentRepo.findByUserAndEndDateIsNull(user).stream()
                .anyMatch(a -> Objects.requireNonNull(a.getVehicle())
                        .getId().equals(mileageReq.getVehicleId()));
        if (!assigned) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Le véhicule n'est pas assigné à l'utilisateur");
        }
        return mileageService.declareMileage(user.getId(), mileageReq);
    }
}
