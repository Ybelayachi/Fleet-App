package com.fleetkm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

/**
 * Request DTO for resetting a password using a reset token.
 */
@Data
public final class ResetPasswordRequest {

    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8)
    @ToString.Exclude
    private String newPassword;
}
