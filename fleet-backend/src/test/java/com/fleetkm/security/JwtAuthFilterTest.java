package com.fleetkm.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.User;
import static org.assertj.core.api.Assertions.assertThat;

public class JwtAuthFilterTest {

    @AfterEach
    void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void filter_sets_authentication_when_token_valid() throws Exception {
        String secret = "01234567890123456789012345678901";
        JwtUtil util = new JwtUtil(secret, 3600000);

        // mock user details service
        CustomUserDetailsService uds = org.mockito.Mockito.mock(CustomUserDetailsService.class);
        org.springframework.security.core.userdetails.UserDetails u = User.withUsername("bob").password("p").roles("DRIVER").build();
        org.mockito.Mockito.when(uds.loadUserByUsername("bob")).thenReturn(u);

        JwtAuthFilter filter = new JwtAuthFilter(util, uds);

        String token = util.generateToken("bob");

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(req, resp, chain);

        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("bob");
    }
}
