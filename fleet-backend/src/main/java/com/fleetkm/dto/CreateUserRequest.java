package com.fleetkm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

/**
 * User creation request DTO.
 * Contains information required to create a new user.
 */
@Data
public final class CreateUserRequest {
    /** User email address (must be unique and valid). */
    @Email
    @NotBlank
    private String email;

    /** User password (must not be blank). */
    @NotBlank
    @ToString.Exclude
    private String password;

    /** User first name. */
    private String firstName;

    /** User last name. */
    private String lastName;

    /** User role (ROLE_ADMIN, ROLE_DRIVER, ROLE_FLEET_MANAGER). */
    private String role;
}
