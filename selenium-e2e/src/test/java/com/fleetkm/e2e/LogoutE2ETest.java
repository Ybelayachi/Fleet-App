package com.fleetkm.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LogoutE2ETest extends BaseE2ETest {

    @Test
    void shouldLogoutAndReturnToLogin() throws Exception {
        String email = "logout.smoke." + System.currentTimeMillis() + "@fleet.local";
        String password = testPassword();
        ensureUserExists(email, password, "ROLE_DRIVER");
        bootstrapSessionForRole(email, password, "ROLE_DRIVER", "/driver/vehicles");

        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));

        longWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(.,'Mes véhicules')]")));

        driver.findElement(By.xpath("//button[contains(.,'Déconnexion')]")).click();
        wait.until(ExpectedConditions.urlContains("/login"));

        Object storedToken = ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return localStorage.getItem('fleet_token');");
        assertTrue(storedToken == null || storedToken.toString().isBlank());
    }
}
