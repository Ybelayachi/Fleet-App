package com.fleetkm.controller;

import com.fleetkm.dto.MileageRequest;
import com.fleetkm.entity.MonthlyMileage;
import com.fleetkm.entity.Vehicle;
import com.fleetkm.service.DriverService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for driver operations.
 * Provides endpoints for managing driver vehicles and mileage data.
 */
@RestController
@RequestMapping("/api/driver")
public final class DriverController {
    /** Logger for driver controller operations. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DriverController.class);
    /** Service for driver operations. */
    private final DriverService driverService;

    /**
     * Constructs DriverController with required dependencies.
     *
     * @param driverServiceBean the driver service
     */
    public DriverController(final DriverService driverServiceBean) {
        this.driverService = driverServiceBean;
    }

    /**
     * Retrieves vehicles assigned to the current driver.
     *
     * @param userDetails the current user details
     * @param pageable the paging information
     * @return response entity with page of vehicles
     */
    @GetMapping("/vehicles")
    public ResponseEntity<Page<Vehicle>> myVehicles(
            @AuthenticationPrincipal final UserDetails userDetails,
            final Pageable pageable) {
        return ResponseEntity.ok(driverService.getAssignedVehicles(userDetails.getUsername(), pageable));
    }

    /**
     * Retrieves current month mileage for the driver.
     *
     * @param userDetails the current user details
     * @param pageable the paging information
     * @return response entity with page of monthly mileage entries
     */
    @GetMapping("/mileage/current")
    public ResponseEntity<Page<MonthlyMileage>> currentMonthMileage(
            @AuthenticationPrincipal final UserDetails userDetails,
            final Pageable pageable) {
        LocalDate now = LocalDate.now();
        return ResponseEntity.ok(
                driverService.getMileageHistory(userDetails.getUsername(), now.getYear(),
                        now.getMonthValue(), pageable));
    }

    /**
     * Retrieves mileage history for all vehicles assigned to current driver.
     * Optional year/month filters can be provided.
     *
     * @param userDetails the current user details
     * @param year optional year filter
     * @param month optional month filter
     * @param pageable the paging information
     * @return response entity with page of monthly mileage history
     */
    @GetMapping("/mileages")
    public ResponseEntity<Page<MonthlyMileage>> myMileageHistory(
            @AuthenticationPrincipal final UserDetails userDetails,
            @RequestParam(name = "year", required = false)
            final Integer year,
            @RequestParam(name = "month", required = false)
            final Integer month,
            final Pageable pageable) {
        return ResponseEntity.ok(
                driverService.getMileageHistory(userDetails.getUsername(), year, month, pageable));
    }

    /**
     * Declares mileage for a vehicle.
     *
     * @param userDetails the current user details
     * @param mileageReq the mileage request
     * @return response entity with saved mileage
     */
    @PostMapping("/mileage")
    public ResponseEntity<?> declareMileage(
            @AuthenticationPrincipal final UserDetails userDetails,
            @Validated @RequestBody final MileageRequest mileageReq) {
        LOGGER.info("declareMileage called by {} with payload {}",
                userDetails.getUsername(), mileageReq);
        try {
            return ResponseEntity.ok(
                    driverService.declareMileage(userDetails.getUsername(), mileageReq));
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(ex.getReason());
        }
    }
}
