package com.fleetkm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetkm.entity.AppUser;
import com.fleetkm.entity.MonthlyMileage;
import com.fleetkm.entity.Vehicle;
import com.fleetkm.service.FleetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FleetController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("null")
public class FleetControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockitoBean FleetService fleetService;

    @MockitoBean com.fleetkm.security.JwtUtil jwtUtil;
    @MockitoBean org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @MockitoBean com.fleetkm.security.JwtAuthFilter jwtAuthFilter;

    @Test
    void vehicles_returns_list() throws Exception {
        Vehicle v1 = new Vehicle(1L, "VIN1", "B", "M", "PL", null, true);
        Vehicle v2 = new Vehicle(2L, "VIN2", "B2", "M2", "PL2", null, true);
        when(fleetService.getAllVehicles(any())).thenReturn(new PageImpl<>(List.of(v1, v2)));

        mvc.perform(get("/api/fleet/vehicles").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].vin").value("VIN1"));
    }

    @Test
    void mileage_returns_entries() throws Exception {
        Vehicle v = new Vehicle(1L, "VINX", null, null, null, null, true);
        AppUser u = new AppUser(5L, "u@e.com", "p", null, null, "DRIVER", true);
        MonthlyMileage mm = new MonthlyMileage(11L, v, 2026, 1, 12345L, OffsetDateTime.now(), u, null, null);
        when(fleetService.getMileage(eq(2026), eq(1), any())).thenReturn(new PageImpl<>(List.of(mm)));

        mvc.perform(get("/api/fleet/mileage?year=2026&month=1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].mileage").value(12345));
    }

    @Test
    void missing_returns_vehicles_without_entries() throws Exception {
        Vehicle v2 = new Vehicle(2L, "VIN2", null, null, null, null, true);
        when(fleetService.getMissingVehicles(eq(2026), eq(1), any())).thenReturn(new PageImpl<>(List.of(v2)));

        mvc.perform(get("/api/fleet/missing?year=2026&month=1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(2));
    }

    @Test
    void export_returns_csv() throws Exception {
        when(fleetService.exportCsv(eq(2026), eq(1))).thenReturn(
                "vehicleId;vin;year;month;mileage;declaredBy;declaredAt\n1;VINCSV;2026;1;7777;u@e.com;2026-01-10T10:00:00Z\n");

        mvc.perform(get("/api/fleet/export?year=2026&month=1").accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("VINCSV")));
    }
}
