package com.fleetkm.security;

import com.fleetkm.entity.AppUser;
import com.fleetkm.repository.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Custom user details service for Spring Security.
 * Loads user details from the database based on email.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    /** Repository for accessing user data. */
    private final AppUserRepository userRepository;

    /**
     * Constructs CustomUserDetailsService with required dependencies.
     *
     * @param repository the user repository
     */
    public CustomUserDetailsService(final AppUserRepository repository) {
        this.userRepository = repository;
    }

    @Override
    public final UserDetails loadUserByUsername(final String username)
            throws UsernameNotFoundException {
        AppUser user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur non trouvé : " + username));
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(user.getRole()));
        return User.withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.getActive())
                .build();
    }
}
