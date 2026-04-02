package com.fleetkm.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BusinessWorkflowE2ETest extends BaseE2ETest {

    @Test
    void shouldCompleteAdminToDriverMileageWorkflow() throws Exception {
                String timestamp = runId();

                String adminEmail = emailFor("admin.workflow", timestamp);
                String driverEmail = emailFor("driver.workflow", timestamp);
        String password = testPassword();

                String vin = vinFor("VIN", timestamp);
                String plate = plateFor("WF-", timestamp);

        ensureUserExists(adminEmail, password, "ROLE_ADMIN");

        String adminToken = loginAndGetToken(adminEmail, password);
        long driverId = createUserAsAdmin(adminToken, driverEmail, password,
                "Driver", "Workflow", "ROLE_DRIVER");
        long vehicleId = createVehicleAsAdmin(adminToken, vin, "Tesla",
                "Model 3", plate, "2025-01-15");
        assignVehicleAsAdmin(adminToken, driverId, vehicleId);

        bootstrapSessionForRole(driverEmail, password, "ROLE_DRIVER", "/driver/vehicles");

        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        longWait.until(ExpectedConditions.urlContains("/driver/vehicles"));
        longWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//td[contains(.,'" + plate + "')]")
        ));

        driver.findElement(By.xpath(
                "//tr[.//td[contains(.,'" + plate + "')]]//button[contains(.,'Déclarer le kilométrage')]"
        )).click();

        longWait.until(ExpectedConditions.urlContains("/driver/mileage"));
        longWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("mileage-value")));

        driver.findElement(By.id("mileage-value")).clear();
        driver.findElement(By.id("mileage-value")).sendKeys("1234");
        driver.findElement(By.xpath("//button[contains(.,'Soumettre le kilométrage')]")).click();

        longWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Kilométrage enregistré avec succès.')]")
        ));

        assertTrue(driver.getCurrentUrl().contains("/driver/mileage"));
    }
}
