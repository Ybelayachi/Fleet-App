package com.fleetkm.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleBasedMenuVisibilityE2ETest extends BaseE2ETest {

    @Test
    void shouldShowAdminMenuAndHideDriverMenuForAdminRole() throws Exception {
        String email = "admin.nav." + System.currentTimeMillis() + "@fleet.local";
        String password = testPassword();
        ensureUserExists(email, password, "ROLE_ADMIN");

        loginViaUi(email, password, "ROLE_ADMIN");
        wait.until(ExpectedConditions.urlContains("/admin/users"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(.,'Utilisateurs')]")));

        assertTrue(driver.findElements(By.xpath("//a[contains(@class,'shell__nav-link')]//span[contains(.,'Utilisateurs')]"))
                .stream().anyMatch(element -> element.isDisplayed()));
        assertTrue(driver.findElements(By.xpath("//a[contains(@class,'shell__nav-link')]//span[contains(.,'Tableau de bord flotte')]"))
                .stream().anyMatch(element -> element.isDisplayed()));
        assertFalse(driver.findElements(By.xpath("//a[contains(@class,'shell__nav-link')]//span[contains(.,'Mes véhicules')]"))
                .stream().anyMatch(element -> element.isDisplayed()));
    }

    @Test
    void shouldShowDriverMenuAndHideAdminMenuForDriverRole() throws Exception {
        String email = "driver.nav." + System.currentTimeMillis() + "@fleet.local";
        String password = testPassword();
        ensureUserExists(email, password, "ROLE_DRIVER");

        loginViaUi(email, password, "ROLE_DRIVER");
        wait.until(ExpectedConditions.urlContains("/driver/vehicles"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(.,'Mes véhicules')]")));

        assertTrue(driver.findElements(By.xpath("//a[contains(@class,'shell__nav-link')]//span[contains(.,'Mes véhicules')]"))
                .stream().anyMatch(element -> element.isDisplayed()));
        assertTrue(driver.findElements(By.xpath("//a[contains(@class,'shell__nav-link')]//span[contains(.,'Déclarer le kilométrage')]"))
                .stream().anyMatch(element -> element.isDisplayed()));
        assertFalse(driver.findElements(By.xpath("//a[contains(@class,'shell__nav-link')]//span[contains(.,'Utilisateurs')]"))
                .stream().anyMatch(element -> element.isDisplayed()));
    }
}
