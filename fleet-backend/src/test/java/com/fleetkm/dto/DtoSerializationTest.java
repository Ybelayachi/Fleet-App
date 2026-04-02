package com.fleetkm.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DtoSerializationTest {

    @Test
    void auth_response_serializes_and_getters_work() throws Exception {
        AuthResponse r = new AuthResponse("tok123");
        assertEquals("tok123", r.getToken());

        ObjectMapper m = new ObjectMapper();
        String json = m.writeValueAsString(r);
        assertTrue(json.contains("tok123"));
    }

    @Test
    void auth_request_and_create_user_have_accessors_and_json() throws Exception {
        AuthRequest a = new AuthRequest();
        a.setEmail("e@x.com");
        a.setPassword("p");

        CreateUserRequest c = new CreateUserRequest();
        c.setEmail("u@x.com");
        c.setPassword("pw");
        c.setFirstName("F");

        ObjectMapper m = new ObjectMapper();
        String ja = m.writeValueAsString(a);
        String jc = m.writeValueAsString(c);

        assertTrue(ja.contains("e@x.com"));
        assertTrue(jc.contains("u@x.com"));
        assertEquals("F", c.getFirstName());
    }
}
