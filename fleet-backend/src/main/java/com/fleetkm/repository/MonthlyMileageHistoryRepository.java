package com.fleetkm.repository;

import com.fleetkm.entity.MonthlyMileageHistory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for MonthlyMileageHistory entities.
 * Provides database access for mileage modification history records.
 */
public interface MonthlyMileageHistoryRepository
        extends JpaRepository<MonthlyMileageHistory, Long> {
}
