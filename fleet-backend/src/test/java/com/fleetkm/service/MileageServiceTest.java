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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class MileageServiceTest {

    @Mock
    private MonthlyMileageRepository mileageRepo;
    @Mock
    private VehicleRepository vehicleRepo;
    @Mock
    private AppUserRepository userRepo;
    @Mock
    private MonthlyMileageHistoryRepository historyRepo;

    @InjectMocks
    private MileageService mileageService;

    private AppUser user;
    private Vehicle vehicle;

    @BeforeEach
    void setup() {
        user = new AppUser();
        user.setId(10L);
        user.setEmail("u@example.com");
        vehicle = new Vehicle();
        vehicle.setId(5L);
    }

    @Test
    void declareNewMileage_success_when_noPrevious() {
        MileageRequest req = new MileageRequest();
        req.setVehicleId(5L);
        req.setYear(2026);
        req.setMonth(1);
        req.setMileage(1000L);

        when(userRepo.findById(10L)).thenReturn(Optional.of(user));
        when(vehicleRepo.findById(5L)).thenReturn(Optional.of(vehicle));
        when(mileageRepo.findByVehicleAndYearAndMonth(vehicle, 2026, 1)).thenReturn(Optional.empty());
        when(mileageRepo.findByVehicleAndYearAndMonth(vehicle, 2025, 12)).thenReturn(Optional.empty());
        when(mileageRepo.save(any(MonthlyMileage.class))).thenAnswer(i -> (MonthlyMileage) i.getArgument(0));

        var saved = mileageService.declareMileage(10L, req);
        assertThat(saved).isNotNull();
        assertThat(saved.getMileage()).isEqualTo(1000L);
        verify(historyRepo, never()).save(any());
    }

    @Test
    void declareNewMileage_reject_when_prevMonthHigher() {
        MileageRequest req = new MileageRequest();
        req.setVehicleId(5L);
        req.setYear(2026);
        req.setMonth(1);
        req.setMileage(1000L);

        MonthlyMileage prev = new MonthlyMileage();
        prev.setMileage(2000L);

        when(userRepo.findById(10L)).thenReturn(Optional.of(user));
        when(vehicleRepo.findById(5L)).thenReturn(Optional.of(vehicle));
        when(mileageRepo.findByVehicleAndYearAndMonth(vehicle, 2026, 1)).thenReturn(Optional.empty());
        when(mileageRepo.findByVehicleAndYearAndMonth(vehicle, 2025, 12)).thenReturn(Optional.of(prev));

        assertThatThrownBy(() -> mileageService.declareMileage(10L, req))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void modifyExistingMileage_success_and_history_saved_when_increasing() {
        MileageRequest req = new MileageRequest();
        req.setVehicleId(5L);
        req.setYear(2026);
        req.setMonth(1);
        req.setMileage(3000L);

        MonthlyMileage existing = new MonthlyMileage();
        existing.setMileage(2000L);

        when(userRepo.findById(10L)).thenReturn(Optional.of(user));
        when(vehicleRepo.findById(5L)).thenReturn(Optional.of(vehicle));
        when(mileageRepo.findByVehicleAndYearAndMonth(vehicle, 2026, 1)).thenReturn(Optional.of(existing));
        when(mileageRepo.save(existing)).thenReturn(existing);

        var saved = mileageService.declareMileage(10L, req);
        assertThat(saved.getMileage()).isEqualTo(3000L);
        verify(historyRepo, times(1)).save(any(MonthlyMileageHistory.class));
    }

    @Test
    void modifyExistingMileage_reject_when_not_increasing() {
        MileageRequest req = new MileageRequest();
        req.setVehicleId(5L);
        req.setYear(2026);
        req.setMonth(1);
        req.setMileage(1000L);

        MonthlyMileage existing = new MonthlyMileage();
        existing.setMileage(2000L);

        when(userRepo.findById(10L)).thenReturn(Optional.of(user));
        when(vehicleRepo.findById(5L)).thenReturn(Optional.of(vehicle));
        when(mileageRepo.findByVehicleAndYearAndMonth(vehicle, 2026, 1)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> mileageService.declareMileage(10L, req))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void reject_when_user_not_found() {
        MileageRequest req = new MileageRequest();
        req.setVehicleId(5L);
        req.setYear(2026);
        req.setMonth(1);
        req.setMileage(100L);

        when(userRepo.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mileageService.declareMileage(10L, req))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void reject_when_vehicle_not_found() {
        MileageRequest req = new MileageRequest();
        req.setVehicleId(5L);
        req.setYear(2026);
        req.setMonth(1);
        req.setMileage(100L);

        when(userRepo.findById(10L)).thenReturn(Optional.of(user));
        when(vehicleRepo.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mileageService.declareMileage(10L, req))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
