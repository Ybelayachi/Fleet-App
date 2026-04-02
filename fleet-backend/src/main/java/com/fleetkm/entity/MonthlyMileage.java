package com.fleetkm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;

/**
 * Monthly mileage record for a vehicle.
 * Stores mileage declarations and modifications with timestamps.
 */
@Entity
@Table(name = "MONTHLY_MILEAGE", uniqueConstraints = {
        @UniqueConstraint(
                columnNames = {"VEHICLE_ID", "YEAR", "MONTH"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class MonthlyMileage {
    /** Unique identifier for the mileage record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MILEAGE_ID")
    private Long id;

    /** The vehicle for this mileage record (required). */
    @ManyToOne(optional = false)
    @JoinColumn(name = "VEHICLE_ID", nullable = false)
    private Vehicle vehicle;

    /** Year of the mileage record (required). */
    @Column(name = "YEAR", nullable = false)
    private Integer year;

    /** Month of the mileage record (required). */
    @Column(name = "MONTH", nullable = false)
    private Integer month;

    /** Mileage value in kilometers (required). */
    @Column(name = "MILEAGE", nullable = false)
    private Long mileage;

    /** Timestamp when mileage was declared. */
    @Column(name = "DECLARED_AT")
    private OffsetDateTime declaredAt;

    /** User who declared the mileage (required). */
    @ManyToOne(optional = false)
    @JoinColumn(name = "DECLARED_BY", nullable = false)
    private AppUser declaredBy;

    /** Timestamp of last modification. */
    @Column(name = "LAST_MODIFIED_AT")
    private OffsetDateTime lastModifiedAt;

    /** User who last modified this record. */
    @ManyToOne
    @JoinColumn(name = "LAST_MODIFIED_BY")
    private AppUser lastModifiedBy;
}
