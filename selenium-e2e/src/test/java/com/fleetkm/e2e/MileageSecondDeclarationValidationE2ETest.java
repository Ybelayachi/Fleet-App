package com.fleetkm.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MileageSecondDeclarationValidationE2ETest extends BaseE2ETest {

    @Test
    void shouldRejectSecondMonthlyDeclarationWhenMileageIsNotIncreasing() throws Exception {
                String timestamp = runId();

                String adminEmail = emailFor("admin.mileage", timestamp);
                String driverEmail = emailFor("driver.mileage", timestamp);
        String password = testPassword();

                String vin = vinFor("VINM", timestamp);
                String plate = plateFor("MV-", timestamp);

        ensureUserExists(adminEmail, password, "ROLE_ADMIN");

        String adminToken = loginAndGetToken(adminEmail, password);
        long driverId = createUserAsAdmin(adminToken, driverEmail, password,
                "Driver", "Validation", "ROLE_DRIVER");
        long vehicleId = createVehicleAsAdmin(adminToken, vin, "Renault",
                "Megane E-Tech", plate, "2025-01-15");
        assignVehicleAsAdmin(adminToken, driverId, vehicleId);

        loginViaUi(driverEmail, password, "ROLE_DRIVER");

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
        waitForVehicleSelected(longWait);

        fillMileageInput("2000");
        driver.findElement(By.xpath("//button[contains(.,'Soumettre le kilométrage')]")).click();

        longWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Kilométrage enregistré avec succès.')]")
        ));

        fillMileageInput("1500");
        driver.findElement(By.xpath("//button[contains(.,'Soumettre le kilométrage')]")).click();

        By errorSelector = By.cssSelector(".panel__messages .error");
        longWait.until(ExpectedConditions.visibilityOfElementLocated(errorSelector));

        String errorMessage = driver.findElement(errorSelector).getText();
        assertTrue(errorMessage.toLowerCase().contains("strictement supérieur"));
    }

    @Test
    void shouldAcceptSecondMonthlyDeclarationWhenMileageIsIncreasing() throws Exception {
                String timestamp = runId();

                String adminEmail = emailFor("admin.mileage.ok", timestamp);
                String driverEmail = emailFor("driver.mileage.ok", timestamp);
        String password = testPassword();

                String vin = vinFor("VINMOK", timestamp);
                String plate = plateFor("MO-", timestamp);

        ensureUserExists(adminEmail, password, "ROLE_ADMIN");

        String adminToken = loginAndGetToken(adminEmail, password);
        long driverId = createUserAsAdmin(adminToken, driverEmail, password,
                "Driver", "Validation", "ROLE_DRIVER");
        long vehicleId = createVehicleAsAdmin(adminToken, vin, "Renault",
                "Megane E-Tech", plate, "2025-01-15");
        assignVehicleAsAdmin(adminToken, driverId, vehicleId);

        loginViaUi(driverEmail, password, "ROLE_DRIVER");

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
        waitForVehicleSelected(longWait);

        fillMileageInput("2100");
        driver.findElement(By.xpath("//button[contains(.,'Soumettre le kilométrage')]"))
                .click();

        By successSelector = By.cssSelector(".panel__messages .success");
        longWait.until(ExpectedConditions.visibilityOfElementLocated(successSelector));

        fillMileageInput("2600");
        driver.findElement(By.xpath("//button[contains(.,'Soumettre le kilométrage')]"))
                .click();

        longWait.until(ExpectedConditions.visibilityOfElementLocated(successSelector));

        boolean hasVisibleError = driver.findElements(By.cssSelector(".panel__messages .error"))
                .stream()
                .anyMatch(element -> element.isDisplayed() && !element.getText().isBlank());

        assertFalse(hasVisibleError);
    }
}
