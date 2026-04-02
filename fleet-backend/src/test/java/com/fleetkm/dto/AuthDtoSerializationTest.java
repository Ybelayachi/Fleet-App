package com.fleetkm.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class AuthDtoSerializationTest {

    @Autowired
    ObjectMapper mapper;

    @Test
    void authResponse_serializes_token() throws Exception {
        AuthResponse r = new AuthResponse("abc.123.token");
        String json = mapper.writeValueAsString(r);
        assertThat(json).contains("abc.123.token");
    }

    @Test
    void authRequest_serializes_and_deserializes() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setEmail("a@b.com");
        req.setPassword("p");
        String js = mapper.writeValueAsString(req);
        AuthRequest out = mapper.readValue(js, AuthRequest.class);
        assertThat(out.getEmail()).isEqualTo("a@b.com");
        assertThat(out.getPassword()).isEqualTo("p");
    }
}
