package com.fleetkm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Application user entity representing system users.
 * Stores authentication and user profile information.
 */
@Entity
@Table(name = "APP_USER", indexes = {
        @Index(columnList = "EMAIL", name = "IDX_USER_EMAIL")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class AppUser {
    /** Unique identifier for the user. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long id;

    /** Email address of the user (unique and required). */
    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    /** Encoded password of the user. */
    @JsonProperty(access = Access.WRITE_ONLY)
    @Column(name = "PASSWORD", nullable = false)
    private String password;

    /** First name of the user. */
    @Column(name = "FIRST_NAME")
    private String firstName;

    /** Last name of the user. */
    @Column(name = "LAST_NAME")
    private String lastName;

    /** Role assigned to the user (e.g., ROLE_ADMIN, ROLE_DRIVER). */
    @Column(name = "ROLE")
    private String role;

    /** Indicates whether the user account is active. */
    @Column(name = "ACTIVE")
    private Boolean active = true;
}
