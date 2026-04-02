package com.fleetkm.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DriverPagesE2ETest extends BaseE2ETest {

    @Test
    void shouldAccessDriverPages() throws Exception {
        String email = "driver.smoke." + System.currentTimeMillis() + "@fleet.local";
        String password = testPassword();
        ensureUserExists(email, password, "ROLE_DRIVER");
        bootstrapSessionForRole(email, password, "ROLE_DRIVER", "/driver/vehicles");

        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));

        longWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(.,'Mes véhicules')]")));
        assertTrue(driver.getCurrentUrl().contains("/driver/vehicles"));

        driver.findElement(By.xpath("//a[contains(.,'Déclarer le kilométrage') and contains(@class,'shell__nav-link')]")).click();
        wait.until(ExpectedConditions.urlContains("/driver/mileage"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(.,'Déclarer le kilométrage mensuel')]")));
    }
}
