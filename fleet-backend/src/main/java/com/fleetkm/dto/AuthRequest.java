package com.fleetkm.dto;

import lombok.Data;
import lombok.ToString;

/**
 * Authentication request DTO.
 * Contains credentials for user login.
 */
@Data
public final class AuthRequest {
    /** User email for authentication. */
    private String email;
    /** User password for authentication. */
    @ToString.Exclude
    private String password;
}
