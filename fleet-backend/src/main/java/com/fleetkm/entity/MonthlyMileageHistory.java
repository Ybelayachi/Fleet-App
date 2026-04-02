package com.fleetkm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;

/**
 * Monthly mileage history entity tracking changes to mileage records.
 * Maintains audit trail of mileage modifications.
 */
@Entity
@Table(name = "MONTHLY_MILEAGE_HISTORY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class MonthlyMileageHistory {
    /** Unique identifier for the history record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Reference to the mileage record (required). */
    @ManyToOne(optional = false)
    @JoinColumn(name = "MILEAGE_ID", nullable = false)
    private MonthlyMileage mileageRef;

    /** Previous mileage value before the change. */
    @Column(name = "PREVIOUS_MILEAGE")
    private Long previousMileage;

    /** New mileage value after the change. */
    @Column(name = "NEW_MILEAGE")
    private Long newMileage;

    /** Timestamp when the change occurred. */
    @Column(name = "CHANGED_AT")
    private OffsetDateTime changedAt;

    /** User who made the change. */
    @ManyToOne
    @JoinColumn(name = "CHANGED_BY")
    private AppUser changedBy;
}
