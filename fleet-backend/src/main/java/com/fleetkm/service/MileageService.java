package com.fleetkm.service;

import com.fleetkm.dto.MileageRequest;
import com.fleetkm.entity.AppUser;
import com.fleetkm.entity.MonthlyMileage;
import com.fleetkm.entity.MonthlyMileageHistory;
import com.fleetkm.entity.Vehicle;
import com.fleetkm.exception.BusinessException;
import com.fleetkm.repository.AppUserRepository;
import com.fleetkm.repository.MonthlyMileageHistoryRepository;
import com.fleetkm.repository.MonthlyMileageRepository;
import com.fleetkm.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Service for managing monthly mileage declarations.
 * Handles mileage tracking, validation, and modification history.
 */
@Service
@SuppressWarnings("null")
public class MileageService {

    /** Constant for December month number. */
    private static final int DECEMBER = 12;
    /** Constant for January month number. */
    private static final int JANUARY = 1;

    /** Repository for monthly mileage records. */
    private final MonthlyMileageRepository mileageRepo;
    /** Repository for vehicle data. */
    private final VehicleRepository vehicleRepo;
    /** Repository for user data. */
    private final AppUserRepository userRepo;
    /** Repository for mileage history records. */
    private final MonthlyMileageHistoryRepository historyRepo;

    /**
     * Constructs MileageService with required repositories.
     *
     * @param mileageRepository the monthly mileage repository
     * @param vehicleRepository the vehicle repository (must not be null)
     * @param userRepository the application user repository
     * @param historyRepository the mileage history repository
     */
    public MileageService(
            final MonthlyMileageRepository mileageRepository,
            final VehicleRepository vehicleRepository,
            final AppUserRepository userRepository,
            final MonthlyMileageHistoryRepository historyRepository) {
        this.mileageRepo = mileageRepository;
        this.vehicleRepo = vehicleRepository;
        this.userRepo = userRepository;
        this.historyRepo = historyRepository;
    }

    /**
     * Declares or modifies mileage for a vehicle in a specific month.
     * Validates business rules and maintains history of changes.
     *
     * @param userId the ID of the user declaring mileage (must not be null)
     * @param req the mileage request with vehicle and declaration details
     * @return the saved or updated monthly mileage record
     * @throws BusinessException if validation fails
     */
    @Transactional
    public MonthlyMileage declareMileage(final Long userId,
                                         final MileageRequest req) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "L'identifiant utilisateur doit être fourni", "userId");
        }
        if (req == null || req.getVehicleId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "L'identifiant du véhicule doit être fourni", "vehicleId");
        }
        AppUser user = userRepo.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "Utilisateur non trouvé",
                        "userId"));
        Vehicle vehicle = vehicleRepo.findById(req.getVehicleId())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "Véhicule non trouvé",
                        "vehicleId"));

        Optional<MonthlyMileage> existing =
                mileageRepo.findByVehicleAndYearAndMonth(
                        vehicle, req.getYear(), req.getMonth());
        if (existing.isPresent()) {
            // modification -> must be traced and strictly increasing
            MonthlyMileage m = existing.get();
            if (req.getMileage() <= m.getMileage()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "Le nouveau kilométrage doit être strictement supérieur au"
                        + " kilométrage enregistré précédemment pour le même mois",
                        "mileage");
            }
            // trace history
            MonthlyMileageHistory hist = new MonthlyMileageHistory();
            hist.setMileageRef(m);
            hist.setPreviousMileage(m.getMileage());
            hist.setNewMileage(req.getMileage());
            hist.setChangedAt(OffsetDateTime.now());
            hist.setChangedBy(user);
            historyRepo.save(hist);

            m.setMileage(req.getMileage());
            m.setLastModifiedAt(OffsetDateTime.now());
            m.setLastModifiedBy(user);
            return mileageRepo.save(m);
        } else {
            // Need to ensure strictly greater than previous month
            int prevYear = req.getMonth() == JANUARY
                    ? req.getYear() - 1 : req.getYear();
            int prevMonth = req.getMonth() == JANUARY
                    ? DECEMBER : req.getMonth() - 1;
            Optional<MonthlyMileage> prev =
                    mileageRepo.findByVehicleAndYearAndMonth(
                            vehicle, prevYear, prevMonth);
            if (prev.isPresent()
                    && req.getMileage() <= prev.get().getMileage()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "Le kilométrage doit être strictement supérieur au"
                        + " kilométrage du mois précédent", "mileage");
            }

            MonthlyMileage m = new MonthlyMileage();
            m.setVehicle(vehicle);
            m.setYear(req.getYear());
            m.setMonth(req.getMonth());
            m.setMileage(req.getMileage());
            m.setDeclaredAt(OffsetDateTime.now());
            m.setDeclaredBy(user);
            return mileageRepo.save(m);
        }
    }
}
