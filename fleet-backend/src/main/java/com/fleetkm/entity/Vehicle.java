package com.fleetkm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

/**
 * Vehicle entity representing a fleet vehicle.
 * Stores vehicle information and operational status.
 */
@Entity
@Table(name = "VEHICLE", indexes = {
        @Index(columnList = "VIN", name = "IDX_VIN")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class Vehicle {
    /** Unique identifier for the vehicle. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VEHICLE_ID")
    private Long id;

    /** Vehicle Identification Number (unique and required). */
    @Column(name = "VIN", unique = true, nullable = false)
    private String vin;

    /** Manufacturer/brand of the vehicle. */
    @Column(name = "BRAND")
    private String brand;

    /** Model name of the vehicle. */
    @Column(name = "MODEL")
    private String model;

    /** License plate number of the vehicle. */
    @Column(name = "LICENSE_PLATE")
    private String licensePlate;

    /** Date when vehicle entered service. */
    @Column(name = "IN_SERVICE_DATE")
    private LocalDate inServiceDate;

    /** Indicates whether the vehicle is active in the fleet. */
    @Column(name = "ACTIVE")
    private Boolean active = true;
}
