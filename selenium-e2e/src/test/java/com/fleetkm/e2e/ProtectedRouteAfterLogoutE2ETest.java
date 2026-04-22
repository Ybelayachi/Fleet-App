package com.fleetkm.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProtectedRouteAfterLogoutE2ETest extends BaseE2ETest {

    @Test
    void shouldRedirectToLoginWhenAccessingProtectedRouteAfterLogout() throws Exception {
        String email = "driver.protected." + System.currentTimeMillis() + "@fleet.local";
        String password = testPassword();
        ensureUserExists(email, password, "ROLE_DRIVER");

        loginViaUi(email, password, "ROLE_DRIVER");
        wait.until(ExpectedConditions.urlContains("/driver/vehicles"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(.,'Mes véhicules')]")));

        driver.findElement(By.xpath("//button[contains(.,'Déconnexion')]")) .click();
        wait.until(ExpectedConditions.urlContains("/login"));

        driver.get(baseUrl() + "/driver/vehicles");
        wait.until(ExpectedConditions.urlContains("/login"));

        Object storedToken = ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return localStorage.getItem('fleet_token');");
        Object storedRole = ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return localStorage.getItem('fleet_role');");

        assertTrue(storedToken == null || storedToken.toString().isBlank());
        assertTrue(storedRole == null || storedRole.toString().isBlank());
    }
}
