package com.fleetkm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetkm.controller.AdminController.AssignRequest;
import com.fleetkm.entity.VehicleAssignment;
import com.fleetkm.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;

    @MockitoBean
    AdminService adminService;
    @MockitoBean
    com.fleetkm.security.JwtUtil jwtUtil;
    @MockitoBean
    com.fleetkm.security.JwtAuthFilter jwtAuthFilter;

    @Test
    void assignVehicle_success() throws Exception {
        AssignRequest req = new AssignRequest(1L, 2L);

        VehicleAssignment a = new VehicleAssignment();
        a.setId(99L);
        when(adminService.assignVehicle(eq(1L), eq(2L))).thenReturn(a);

        mvc.perform(post("/api/admin/assignments").contentType(MediaType.APPLICATION_JSON_VALUE).content(Objects.requireNonNull(mapper.writeValueAsString(req))))
                .andExpect(status().isOk());
    }
}
