package com.fleetkm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Mileage declaration request DTO.
 * Contains vehicle mileage data for a specific month.
 */
@Data
public final class MileageRequest {
    /** The vehicle ID. */
    @NotNull
    private Long vehicleId;

    /** The year for mileage declaration. */
    @NotNull
    private Integer year;

    /** The month for mileage declaration. */
    @NotNull
    private Integer month;

    /** The mileage in kilometers (must be non-negative). */
    @NotNull
    @Min(0)
    private Long mileage;
}
