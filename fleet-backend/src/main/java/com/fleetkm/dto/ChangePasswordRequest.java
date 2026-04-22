package com.fleetkm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

/**
 * Request DTO for changing the authenticated user's password.
 */
@Data
public final class ChangePasswordRequest {

    /** Current password to verify identity. */
    @NotBlank
    @ToString.Exclude
    private String currentPassword;

    /** New password (minimum 8 characters). */
    @NotBlank
    @Size(min = 8)
    @ToString.Exclude
    private String newPassword;
}
