package com.fleetkm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Authentication response DTO.
 * Contains JWT token for authenticated user.
 */
@Data
@AllArgsConstructor
public final class AuthResponse {
    /** JWT token for the authenticated session. */
    private String token;
}
