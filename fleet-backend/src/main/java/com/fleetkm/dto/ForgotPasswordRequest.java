package com.fleetkm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for initiating a password reset.
 */
@Data
public final class ForgotPasswordRequest {

    @Email
    @NotBlank
    private String email;
}
