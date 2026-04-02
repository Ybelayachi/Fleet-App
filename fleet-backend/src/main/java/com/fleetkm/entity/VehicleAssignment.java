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
import java.time.LocalDate;

/**
 * Vehicle assignment entity representing driver-vehicle relationships.
 * Tracks which user is assigned to which vehicle and for what period.
 */
@Entity
@Table(name = "VEHICLE_ASSIGNMENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class VehicleAssignment {
    /** Unique identifier for the assignment. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ASSIGNMENT_ID")
    private Long id;

    /** User assigned to the vehicle (required). */
    @ManyToOne(optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private AppUser user;

    /** Vehicle assigned to the user (required). */
    @ManyToOne(optional = false)
    @JoinColumn(name = "VEHICLE_ID", nullable = false)
    private Vehicle vehicle;

    /** Start date of the assignment. */
    @Column(name = "START_DATE")
    private LocalDate startDate;

    /** End date of the assignment (null if currently active). */
    @Column(name = "END_DATE")
    private LocalDate endDate;
}
