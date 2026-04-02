package com.fleetkm.service;

import com.fleetkm.entity.MonthlyMileage;
import com.fleetkm.entity.Vehicle;
import com.fleetkm.repository.MonthlyMileageRepository;
import com.fleetkm.repository.VehicleRepository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/**
 * Service for fleet-level operations.
 * Handles vehicle listings, mileage reports and CSV export.
 */
@Service
public class FleetService {

    /** Repository for vehicle data. */
    private final VehicleRepository vehicleRepo;
    /** Repository for monthly mileage data. */
    private final MonthlyMileageRepository mileageRepo;

    /**
     * Constructs FleetService with required repositories.
     *
     * @param vehicleRepository the vehicle repository
     * @param mileageRepository the monthly mileage repository
     */
    public FleetService(final VehicleRepository vehicleRepository,
            final MonthlyMileageRepository mileageRepository) {
        this.vehicleRepo = vehicleRepository;
        this.mileageRepo = mileageRepository;
    }

    /**
     * Returns a paginated list of all vehicles.
     *
     * @param pageable paging parameters
     * @return page of vehicles
     */
    public Page<Vehicle> getAllVehicles(@NonNull final Pageable pageable) {
        return vehicleRepo.findAll(pageable);
    }

    /**
     * Returns paginated mileage entries for the given year and month.
     *
     * @param year the year
     * @param month the month
     * @param pageable paging parameters
     * @return page of mileage entries
     */
    public Page<MonthlyMileage> getMileage(final Integer year,
            final Integer month, final Pageable pageable) {
        return mileageRepo.findByYearAndMonth(year, month, pageable);
    }

    /**
     * Returns paginated list of vehicles with no mileage entry for the period.
     *
     * @param year the year
     * @param month the month
     * @param pageable paging parameters
     * @return page of vehicles missing mileage declarations
     */
    public Page<Vehicle> getMissingVehicles(final Integer year,
            final Integer month, final Pageable pageable) {
        return vehicleRepo.findVehiclesWithoutMileage(year, month, pageable);
    }

    /**
     * Builds a CSV string of all mileage entries for the given period.
     *
     * @param year the year
     * @param month the month
     * @return CSV string with headers
     */
    public String exportCsv(final Integer year, final Integer month) {
        List<MonthlyMileage> entries = mileageRepo.findByYearAndMonth(year, month);
        StringBuilder sb = new StringBuilder(
                "vehicleId;vin;year;month;mileage;declaredBy;declaredAt\n");
        for (var entry : entries) {
            sb.append(entry.getVehicle().getId()).append(';')
                    .append(entry.getVehicle().getVin()).append(';')
                    .append(entry.getYear()).append(';')
                    .append(entry.getMonth()).append(';')
                    .append(entry.getMileage()).append(';')
                    .append(entry.getDeclaredBy().getEmail()).append(';')
                    .append(entry.getDeclaredAt()).append('\n');
        }
        return sb.toString();
    }
}
