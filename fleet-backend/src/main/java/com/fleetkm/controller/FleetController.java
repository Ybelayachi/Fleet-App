package com.fleetkm.controller;

import com.fleetkm.service.FleetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for fleet management.
 * Provides endpoints for accessing fleet statistics and reports.
 */
@RestController
@RequestMapping("/api/fleet")
public class FleetController {

    /** Service for fleet operations. */
    private final FleetService fleetService;

    /**
     * Constructs FleetController with required dependencies.
     *
     * @param fleetServiceBean the fleet service
     */
    public FleetController(final FleetService fleetServiceBean) {
        this.fleetService = fleetServiceBean;
    }

    /**
     * Retrieves all vehicles.
     *
     * @param pageable the paging information
     * @return page of all vehicles
     */
    @GetMapping("/vehicles")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<?> allVehicles(@NonNull final Pageable pageable) {
        return fleetService.getAllVehicles(pageable);
    }

    /**
     * Retrieves mileage data for a specific period.
     *
     * @param year the year
     * @param month the month
     * @param pageable the paging information
     * @return page of mileage data
     */
    @GetMapping("/mileage")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<?> mileage(@RequestParam("year") final Integer year,
            @RequestParam("month") final Integer month,
            final Pageable pageable) {
        return fleetService.getMileage(year, month, pageable);
    }

    /**
     * Retrieves vehicles with missing mileage data.
     *
     * @param year the year
     * @param month the month
     * @param pageable the paging information
     * @return page of vehicles missing data
     */
    @GetMapping("/missing")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FLEET_MANAGER')")
    public Page<?> missing(@RequestParam("year") final Integer year,
            @RequestParam("month") final Integer month,
            final Pageable pageable) {
        return fleetService.getMissingVehicles(year, month, pageable);
    }

    /**
     * Exports mileage data as CSV.
     *
     * @param year the year
     * @param month the month
     * @return response entity with CSV data
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> exportCsv(
            @RequestParam("year") final Integer year,
            @RequestParam("month") final Integer month) {
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .body(fleetService.exportCsv(year, month));
    }
}
