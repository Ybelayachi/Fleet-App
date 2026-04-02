package com.fleetkm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetkm.entity.MonthlyMileage;
import com.fleetkm.entity.Vehicle;
import com.fleetkm.service.DriverService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DriverController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("null")
public class DriverControllerMoreTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Autowired DriverService driverService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public DriverService driverService() { return mock(DriverService.class); }

        @Bean
        public com.fleetkm.security.JwtUtil jwtUtil() { return mock(com.fleetkm.security.JwtUtil.class); }

        @Bean
        public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() { return mock(org.springframework.security.crypto.password.PasswordEncoder.class); }

        @Bean
        public com.fleetkm.security.JwtAuthFilter jwtAuthFilter() { return mock(com.fleetkm.security.JwtAuthFilter.class); }
    }

    @Test
    @WithMockUser(username = "d@e.com")
    void myVehicles_returns_assigned_list() throws Exception {
        Vehicle v = new Vehicle(); v.setId(5L); v.setVin("VINX");
        when(driverService.getAssignedVehicles(eq("d@e.com"), any())).thenReturn(new PageImpl<>(List.of(v)));

        mvc.perform(get("/api/driver/vehicles").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].vin").value("VINX"));
    }

    @Test
    @WithMockUser(username = "d@e.com")
    void currentMonthMileage_returns_assigned_list() throws Exception {
        MonthlyMileage mm = new MonthlyMileage();
        mm.setId(1L);
        when(driverService.getMileageHistory(eq("d@e.com"), any(), any(), any()))
            .thenReturn(new PageImpl<>(List.of(mm)));

        mvc.perform(get("/api/driver/mileage/current").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1));
    }
}
