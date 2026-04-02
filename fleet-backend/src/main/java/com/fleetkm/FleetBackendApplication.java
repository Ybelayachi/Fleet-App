package com.fleetkm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Fleet Backend.
 * Entry point for the Spring Boot application.
 */
@SpringBootApplication
public final class FleetBackendApplication {
    /**
     * Private constructor to prevent direct instantiation.
     */
    private FleetBackendApplication() {
    }

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(FleetBackendApplication.class, args);
    }
}
