package com.fleetkm.repository;

import com.fleetkm.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    /**
     * Finds an application user by email address.
     *
     * @param email the email address (must not be null)
     * @return an Optional containing the user if found
     */
    Optional<AppUser> findByEmail(String email);

}
