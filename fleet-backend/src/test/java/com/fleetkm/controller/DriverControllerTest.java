package com.fleetkm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetkm.dto.MileageRequest;
import com.fleetkm.entity.MonthlyMileage;
import com.fleetkm.entity.Vehicle;
import com.fleetkm.service.DriverService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DriverController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("null")
public class DriverControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockitoBean
    DriverService driverService;
    @MockitoBean
    com.fleetkm.security.JwtUtil jwtUtil;
    @MockitoBean
    org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @MockitoBean
    com.fleetkm.security.JwtAuthFilter jwtAuthFilter;

    @Test
    @WithMockUser(username = "u@e.com")
    void declareMileage_for_assigned_vehicle_calls_service() throws Exception {
        MonthlyMileage mm = new MonthlyMileage();
        mm.setId(1L);
        Vehicle v = new Vehicle(); v.setId(1L);
        mm.setVehicle(v);
        mm.setMileage(1234L);
        when(driverService.declareMileage(eq("u@e.com"), any())).thenReturn(mm);

        MileageRequest req = new MileageRequest(); req.setVehicleId(1L); req.setYear(2026); req.setMonth(1); req.setMileage(1234L);

        mvc.perform(post("/api/driver/mileage").contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk());
    }
}
