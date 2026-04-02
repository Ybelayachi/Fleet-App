package com.fleetkm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetkm.dto.CreateUserRequest;
import com.fleetkm.entity.AppUser;
import com.fleetkm.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.lang.NonNull;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class RegisterControllerUnitTest {

    MockMvc mvc;

    @NonNull
    ObjectMapper mapper = new ObjectMapper();

    @Mock
    AuthService authService;

    @InjectMocks
    RegisterController controller;

    @BeforeEach
    void setup() {
        jakarta.validation.Validator jakartaValidator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .setValidator(new SpringValidatorAdapter(jakartaValidator))
                .build();
    }

    @Test
    void register_success_sets_default_role_and_hides_password() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("new@user.com");
        req.setPassword("pw");

        AppUser saved = new AppUser();
        saved.setId(42L);
        saved.setEmail("new@user.com");
        saved.setRole("ROLE_DRIVER");
        when(authService.register(any())).thenReturn(saved);

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@user.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.role").value("ROLE_DRIVER"));

        verify(authService).register(any());
    }

    @Test
    void register_fails_when_user_exists() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("exists@user.com");
        req.setPassword("pw");

        when(authService.register(any())).thenThrow(
                new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "L'utilisateur existe déjà"));

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("utilisateur existe")));
    }

    @Test
    void register_validation_errors_return_bad_request() throws Exception {
        // invalid email and blank password
        String badJson = "{\"email\":\"not-email\",\"password\":\"\"}";

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }
}
