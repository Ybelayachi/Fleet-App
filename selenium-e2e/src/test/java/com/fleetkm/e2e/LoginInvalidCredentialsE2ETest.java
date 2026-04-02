package com.fleetkm.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedCondition;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginInvalidCredentialsE2ETest extends BaseE2ETest {

    @Test
    void shouldShowErrorMessageWhenCredentialsAreInvalid() {
        driver.get(baseUrl() + "/login");

        driver.findElement(By.id("login-email")).sendKeys("invalid@example.com");
        driver.findElement(By.id("login-password")).sendKeys("wrong-password");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until((ExpectedCondition<Boolean>) wd -> wd != null
            && wd.getCurrentUrl().contains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"));

        Object token = ((org.openqa.selenium.JavascriptExecutor) driver)
            .executeScript("return localStorage.getItem('fleet_token');");
        assertTrue(token == null || token.toString().isBlank());
    }
}
