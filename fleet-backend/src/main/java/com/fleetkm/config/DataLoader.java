package com.fleetkm.config;

import com.fleetkm.entity.AppUser;
import com.fleetkm.entity.Vehicle;
import com.fleetkm.repository.AppUserRepository;
import com.fleetkm.repository.VehicleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data loader component that initializes default admin user and test vehicle.
 * Runs on application startup if database is empty.
 */
@Component
public class DataLoader implements CommandLineRunner {

    /** Repository for managing application users. */
    private final AppUserRepository userRepo;
    /** Repository for managing vehicles. */
    private final VehicleRepository vehicleRepo;
    /** Password encoder for user passwords. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a DataLoader with required dependencies.
     *
     * @param userRepository the user repository (must not be null)
     * @param vehicleRepository the vehicle repository (must not be null)
     * @param encoder the password encoder (must not be null)
     */
    public DataLoader(
            final AppUserRepository userRepository,
            final VehicleRepository vehicleRepository,
            final PasswordEncoder encoder) {
        this.userRepo = userRepository;
        this.vehicleRepo = vehicleRepository;
        this.passwordEncoder = encoder;
    }

    /**
     * Loads initial data if database is empty.
     *
     * @param args command line arguments (not used)
     * @throws Exception if data loading fails
     */
    @Override
    public void run(final String... args) throws Exception {
        if (userRepo.count() == 0) {
            AppUser admin = new AppUser();
            admin.setEmail("admin@example.com");
            admin.setFirstName("System");
            admin.setLastName("Admin");
            admin.setPassword(passwordEncoder.encode("Admin123!"));
            admin.setRole("ROLE_ADMIN");
            admin.setActive(true);
            userRepo.save(admin);
        }

        if (vehicleRepo.count() == 0) {
            Vehicle v = new Vehicle();
            v.setVin("VINTEST1234567890");
            v.setBrand("TestBrand");
            v.setModel("ModelX");
            v.setLicensePlate("TEST-123");
            v.setActive(true);
            vehicleRepo.save(v);
        }
    }
}
