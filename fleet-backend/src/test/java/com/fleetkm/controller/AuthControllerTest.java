package com.fleetkm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetkm.dto.AuthRequest;
import com.fleetkm.dto.AuthResponse;
import com.fleetkm.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    com.fleetkm.security.JwtAuthFilter jwtAuthFilter;

    @Test
    void login_success_returns_token() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setEmail("test@example.com");
        req.setPassword("pass");

        when(authService.login(eq("test@example.com"), eq("pass")))
                .thenReturn(new AuthResponse("tok123"));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(Objects.requireNonNull(mapper.writeValueAsString(req))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("tok123"));
    }
}
