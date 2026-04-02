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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class RegisterControllerTest {

    MockMvc mvc;

    @Mock
    AuthService authService;

    @InjectMocks
    RegisterController controller;

    ObjectMapper mapper;

    @BeforeEach
    void setup() {
        this.mapper = new ObjectMapper();
        jakarta.validation.Validator jakartaValidator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .setValidator(new SpringValidatorAdapter(jakartaValidator))
                .build();
    }

    @Test
    void registering_existing_user_returns_bad_request() throws Exception {
        CreateUserRequest r = new CreateUserRequest();
        r.setEmail("a@b.com");
        r.setPassword("pwd");

        when(authService.register(any())).thenThrow(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'utilisateur existe déjà"));

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(r)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registering_new_user_returns_ok_and_hides_password() throws Exception {
        CreateUserRequest r = new CreateUserRequest();
        r.setEmail("new@x.com");
        r.setPassword("pwd");
        r.setFirstName("F");

        AppUser saved = new AppUser();
        saved.setEmail("new@x.com");
        saved.setFirstName("F");
        when(authService.register(any())).thenReturn(saved);

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(r)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("new@x.com")));

        verify(authService).register(any());
    }
}
