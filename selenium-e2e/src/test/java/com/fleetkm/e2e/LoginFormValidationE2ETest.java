package com.fleetkm.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginFormValidationE2ETest extends BaseE2ETest {

    @Test
    void shouldDisplayRequiredFieldErrorsWhenSubmittingEmptyForm() {
        driver.get(baseUrl() + "/login");

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),\"L'e-mail est requis\")]")
        ));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Le mot de passe est requis')]")
        ));

        assertTrue(driver.getCurrentUrl().contains("/login"));
    }
}
