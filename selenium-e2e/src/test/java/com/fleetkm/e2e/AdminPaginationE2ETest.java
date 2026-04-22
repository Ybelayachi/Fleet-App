package com.fleetkm.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminPaginationE2ETest extends BaseE2ETest {

    @Test
    void shouldPaginateAdminUsersList() throws Exception {
        String id = runId();
        String password = testPassword();

        String adminEmail = emailFor("admin.pagination", id);
        String userPrefix = "pagination.user." + id;

        ensureUserExists(adminEmail, password, "ROLE_ADMIN");

        String adminToken = loginAndGetToken(adminEmail, password);

        int createdUsers = 22;
        for (int index = 0; index < createdUsers; index++) {
            String email = userPrefix + "." + index + "@fleet.local";
            createUserAsAdmin(adminToken, email, password,
                    "User" + index, "Pagination", "ROLE_DRIVER");
        }

        loginViaUi(adminEmail, password, "ROLE_ADMIN");

        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        longWait.until(ExpectedConditions.urlContains("/admin/users"));
        longWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-search")));

        driver.findElement(By.id("user-search")).clear();
        driver.findElement(By.id("user-search")).sendKeys(userPrefix);

        By infoSelector = By.cssSelector(".pagination__info");
        longWait.until(ExpectedConditions.textToBePresentInElementLocated(infoSelector, "Page 1 sur 2"));
        longWait.until(ExpectedConditions.textToBePresentInElementLocated(infoSelector, "(22 utilisateurs)"));

        By rowsSelector = By.cssSelector(".users-table tbody tr");
        longWait.until((ExpectedCondition<Boolean>) wd -> wd != null
                && wd.findElements(rowsSelector).size() == 20);
        assertEquals(20, driver.findElements(rowsSelector).size());

        driver.findElement(By.xpath("(//button[.//span[contains(.,'Suivant')]])[1]")).click();
        longWait.until(ExpectedConditions.textToBePresentInElementLocated(infoSelector, "Page 2 sur 2"));

        longWait.until((ExpectedCondition<Boolean>) wd -> wd != null
                && wd.findElements(rowsSelector).size() == 2);
        List<?> secondPageRows = driver.findElements(rowsSelector);
        assertEquals(2, secondPageRows.size());

        driver.findElement(By.xpath("(//button[.//span[contains(.,'Précédent')]])[1]")).click();
        longWait.until(ExpectedConditions.textToBePresentInElementLocated(infoSelector, "Page 1 sur 2"));

        assertTrue(driver.findElement(infoSelector).getText().contains("(22 utilisateurs)"));
    }

        @Test
        void shouldPaginateFilteredUsersAndDisplayActiveAndInactiveStatuses() throws Exception {
        String id = runId();
        String password = testPassword();

        String adminEmail = emailFor("admin.pagination.status", id);
        String userPrefix = "pagination.status.user." + id;

        ensureUserExists(adminEmail, password, "ROLE_ADMIN");
        String adminToken = loginAndGetToken(adminEmail, password);

        int activeUsers = 21;
        int inactiveUsers = 4;

        for (int index = 0; index < activeUsers; index++) {
            String email = userPrefix + ".active." + index + "@fleet.local";
            createUserAsAdmin(adminToken, email, password,
                "Active" + index, "Pagination", "ROLE_DRIVER", true);
        }

        for (int index = 0; index < inactiveUsers; index++) {
            String email = userPrefix + ".inactive." + index + "@fleet.local";
            createUserAsAdmin(adminToken, email, password,
                "Inactive" + index, "Pagination", "ROLE_DRIVER", false);
        }

        loginViaUi(adminEmail, password, "ROLE_ADMIN");

        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        longWait.until(ExpectedConditions.urlContains("/admin/users"));
        longWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-search")));

        driver.findElement(By.id("user-search")).clear();
        driver.findElement(By.id("user-search")).sendKeys(userPrefix);

        By infoSelector = By.cssSelector(".pagination__info");
        longWait.until(ExpectedConditions.textToBePresentInElementLocated(infoSelector, "Page 1 sur 2"));
        longWait.until(ExpectedConditions.textToBePresentInElementLocated(infoSelector, "(25 utilisateurs)"));

        By rowsSelector = By.cssSelector(".users-table tbody tr");
        longWait.until((ExpectedCondition<Boolean>) wd -> wd != null
            && wd.findElements(rowsSelector).size() == 20);

        List<String> statuses = new ArrayList<>();
        driver.findElements(By.cssSelector(".users-table tbody tr td:nth-child(4)"))
            .forEach(cell -> statuses.add(cell.getText().trim()));

        driver.findElement(By.xpath("(//button[.//span[contains(.,'Suivant')]])[1]")).click();
        longWait.until(ExpectedConditions.textToBePresentInElementLocated(infoSelector, "Page 2 sur 2"));
        longWait.until((ExpectedCondition<Boolean>) wd -> wd != null
            && wd.findElements(rowsSelector).size() == 5);

        driver.findElements(By.cssSelector(".users-table tbody tr td:nth-child(4)"))
            .forEach(cell -> statuses.add(cell.getText().trim()));

        assertTrue(statuses.contains("Oui"));
        assertTrue(statuses.contains("Non"));
        }
}
