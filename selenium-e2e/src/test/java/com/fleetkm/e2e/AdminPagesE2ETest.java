package com.fleetkm.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminPagesE2ETest extends BaseE2ETest {

    @Test
    void shouldAccessAllAdminAndFleetPages() throws Exception {
        String email = "admin.smoke." + System.currentTimeMillis() + "@fleet.local";
        String password = testPassword();
        ensureUserExists(email, password, "ROLE_ADMIN");
        bootstrapSessionForRole(email, password, "ROLE_ADMIN", "/admin/users");

        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));

        longWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(.,'Utilisateurs')]")));
        assertTrue(driver.getCurrentUrl().contains("/admin/users"));

        driver.findElement(By.xpath("//a[contains(.,'Véhicules') and contains(@class,'shell__nav-link')]")).click();
        wait.until(ExpectedConditions.urlContains("/admin/vehicles"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(.,'Véhicules')]")));

        driver.findElement(By.xpath("//a[contains(.,'Affectations') and contains(@class,'shell__nav-link')]")).click();
        wait.until(ExpectedConditions.urlContains("/admin/assignments"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(.,'Affectations')]")));

        driver.findElement(By.xpath("//a[contains(.,'Tableau de bord flotte') and contains(@class,'shell__nav-link')]")).click();
        wait.until(ExpectedConditions.urlContains("/fleet/dashboard"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(.,'Tableau de bord flotte')]")));
    }
}
