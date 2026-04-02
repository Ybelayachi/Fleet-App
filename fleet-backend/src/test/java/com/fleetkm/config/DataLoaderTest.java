package com.fleetkm.config;

import com.fleetkm.entity.AppUser;
import com.fleetkm.entity.Vehicle;
import com.fleetkm.repository.AppUserRepository;
import com.fleetkm.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;

@SuppressWarnings("null")
public class DataLoaderTest {

    @Test
    void run_inserts_defaults_when_repos_empty() throws Exception {
        AppUserRepository userRepo = mock(AppUserRepository.class);
        VehicleRepository vehicleRepo = mock(VehicleRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        when(userRepo.count()).thenReturn(0L);
        when(vehicleRepo.count()).thenReturn(0L);
        when(encoder.encode(any())).thenReturn("enc");

        DataLoader loader = new DataLoader(userRepo, vehicleRepo, encoder);
        loader.run();

        verify(userRepo).save(isA(AppUser.class));
        verify(vehicleRepo).save(isA(Vehicle.class));
    }
}
