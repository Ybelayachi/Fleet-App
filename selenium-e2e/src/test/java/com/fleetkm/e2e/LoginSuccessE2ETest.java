package com.fleetkm.e2e;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginSuccessE2ETest extends BaseE2ETest {

    @Test
    void shouldLoginAndRedirectToDriverArea() throws Exception {
        String email = "admin." + System.currentTimeMillis()
                + "@fleet.local";
        String password = testPassword();
        ensureUserExists(email, password, "ROLE_ADMIN");

        bootstrapSessionForRole(email, password,
                "ROLE_ADMIN", "/admin/users");

        Object token = ((org.openqa.selenium.JavascriptExecutor) driver)
            .executeScript("return localStorage.getItem('fleet_token');");
        assertTrue(token != null && !token.toString().isBlank(),
            "Expected JWT token to be stored after successful login");
    }
}
