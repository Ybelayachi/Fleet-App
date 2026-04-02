package com.fleetkm.e2e;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

abstract class BaseE2ETest {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\\"token\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern ID_PATTERN = Pattern.compile("\\\"id\\\"\\s*:\\s*(\\d+)");
    private static final long RETRY_DELAY_MS = 1200L;

    protected WebDriver driver;
    protected WebDriverWait wait;

    protected String baseUrl() {
        return System.getProperty("baseUrl", "http://localhost:30200");
    }

    protected String testEmail() {
        return System.getProperty("e2e.email", "admin.hpa@fleet.local");
    }

    protected String testPassword() {
        return System.getProperty("e2e.password", "Demo123!");
    }

    protected String apiRootUrl() {
        return System.getProperty("e2e.apiRootUrl", baseUrl());
    }

    private boolean isRetriableStatus(final int status) {
        return status == 502 || status == 504 || (status >= 500 && status <= 599);
    }

    private void waitForApiAvailability() {
        HttpClient client = HttpClient.newHttpClient();
        String body = "{" +
                "\"email\":\"warmup@fleet.local\"," +
                "\"password\":\"warmup\"" +
                "}";

        int maxAttempts = 8;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiRootUrl() + "/api/auth/login"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            try {
                HttpResponse<String> response = client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());
                if (!isRetriableStatus(response.statusCode())) {
                    return;
                }
            } catch (IOException ioException) {
                if (attempt == maxAttempts) {
                    throw new IllegalStateException(
                            "Unable to reach API root " + apiRootUrl(),
                            ioException);
                }
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for API availability", interruptedException);
            }

            try {
                Thread.sleep(RETRY_DELAY_MS);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for API availability", interruptedException);
            }
        }

        throw new IllegalStateException("API root is unreachable after retries: " + apiRootUrl());
    }

    protected String runId() {
        return String.valueOf(System.currentTimeMillis());
    }

    protected String emailFor(final String prefix,
            final String id) {
        return prefix + "." + id + "@fleet.local";
    }

    protected String vinFor(final String prefix,
            final String id) {
        return prefix + id;
    }

    protected String plateFor(final String prefix,
            final String id) {
        int suffixStart = Math.max(0, id.length() - 6);
        return prefix + id.substring(suffixStart);
    }

    @BeforeEach
    void setUp() {
        waitForApiAvailability();
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        boolean headless = Boolean.parseBoolean(System.getProperty("e2e.headless", "true"));
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1440,900");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            long pauseMs = Long.parseLong(System.getProperty("e2e.pauseMs", "0"));
            if (pauseMs > 0) {
                try {
                    Thread.sleep(pauseMs);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
            driver.quit();
        }
    }

    protected void ensureUserExists(final String email,
            final String password,
            final String role) throws IOException, InterruptedException {
        String body = "{" +
                "\"email\":\"" + email + "\"," +
                "\"password\":\"" + password + "\"," +
                "\"firstName\":\"Demo\"," +
                "\"lastName\":\"User\"," +
                "\"role\":\"" + role + "\"" +
                "}";

        HttpClient client = HttpClient.newHttpClient();
        int maxAttempts = 4;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiRootUrl() + "/api/auth/register"))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response;
            try {
                response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());
            } catch (IOException ioException) {
                if (attempt == maxAttempts) {
                    throw ioException;
                }
                Thread.sleep(RETRY_DELAY_MS);
                continue;
            }
            int status = response.statusCode();
            if (status == 200 || status == 400) {
            return;
            }
            if (attempt == maxAttempts || !isRetriableStatus(status)) {
            throw new IllegalStateException("Unable to ensure test user. HTTP "
                + status + " - " + response.body());
            }
            Thread.sleep(RETRY_DELAY_MS);
        }
    }

    protected String loginAndGetToken(final String email,
            final String password) throws IOException, InterruptedException {
        String body = "{" +
                "\"email\":\"" + email + "\"," +
                "\"password\":\"" + password + "\"" +
                "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        int maxAttempts = 8;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiRootUrl() + "/api/auth/login"))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException ioException) {
                if (attempt == maxAttempts) {
                    throw ioException;
                }
                Thread.sleep(RETRY_DELAY_MS);
                continue;
            }
            if (response.statusCode() == 200) {
            break;
            }
            if (attempt == maxAttempts
                || !isRetriableStatus(response.statusCode())) {
            break;
            }
            Thread.sleep(RETRY_DELAY_MS);
        }

        if (response == null || response.statusCode() != 200) {
            int status = response == null ? -1 : response.statusCode();
            String responseBody = response == null ? "" : response.body();
            throw new IllegalStateException("Unable to login test user. HTTP "
                + status + " - " + responseBody);
        }

        Matcher matcher = TOKEN_PATTERN.matcher(response.body());
        if (!matcher.find()) {
            throw new IllegalStateException("Token not found in login response");
        }
        return matcher.group(1);
    }

    protected long createUserAsAdmin(final String token,
            final String email,
            final String password,
            final String firstName,
            final String lastName,
            final String role) throws IOException, InterruptedException {
        return createUserAsAdmin(token, email, password, firstName,
            lastName, role, true);
        }

        protected long createUserAsAdmin(final String token,
            final String email,
            final String password,
            final String firstName,
            final String lastName,
            final String role,
            final boolean active) throws IOException, InterruptedException {
        String body = "{" +
            "\"email\":\"" + email + "\"," +
            "\"password\":\"" + password + "\"," +
            "\"firstName\":\"" + firstName + "\"," +
            "\"lastName\":\"" + lastName + "\"," +
            "\"role\":\"" + role + "\"," +
            "\"active\":" + active +
            "}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiRootUrl() + "/api/admin/users"))
            .timeout(Duration.ofSeconds(20))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        int maxAttempts = 4;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return extractId(response.body(), "user");
            }
            if (attempt == maxAttempts
                || (response.statusCode() < 500 || response.statusCode() > 599)) {
                break;
            }
            Thread.sleep(1200L);
        }

        int status = response == null ? -1 : response.statusCode();
        String responseBody = response == null ? "" : response.body();
        throw new IllegalStateException("Unable to create user. HTTP "
                + status + " - " + responseBody);
    }

    protected long createVehicleAsAdmin(final String token,
            final String vin,
            final String brand,
            final String model,
            final String licensePlate,
            final String inServiceDate) throws IOException, InterruptedException {
        String body = "{" +
            "\"vin\":\"" + vin + "\"," +
            "\"brand\":\"" + brand + "\"," +
            "\"model\":\"" + model + "\"," +
            "\"licensePlate\":\"" + licensePlate + "\"," +
            "\"inServiceDate\":\"" + inServiceDate + "\"," +
            "\"active\":true" +
            "}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiRootUrl() + "/api/admin/vehicles"))
            .timeout(Duration.ofSeconds(20))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        int maxAttempts = 4;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return extractId(response.body(), "vehicle");
            }
            if (attempt == maxAttempts
                || (response.statusCode() < 500 || response.statusCode() > 599)) {
                break;
            }
            Thread.sleep(1200L);
        }

        int status = response == null ? -1 : response.statusCode();
        String responseBody = response == null ? "" : response.body();
        throw new IllegalStateException("Unable to create vehicle. HTTP "
                + status + " - " + responseBody);
    }

    protected void assignVehicleAsAdmin(final String token,
            final long userId,
            final long vehicleId) throws IOException, InterruptedException {
        String body = "{" +
            "\"userId\":" + userId + "," +
            "\"vehicleId\":" + vehicleId +
            "}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiRootUrl() + "/api/admin/assignments"))
            .timeout(Duration.ofSeconds(20))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        int maxAttempts = 4;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return;
            }
            if (attempt == maxAttempts
                || (response.statusCode() < 500 || response.statusCode() > 599)) {
                break;
            }
            Thread.sleep(1200L);
        }

        int status = response == null ? -1 : response.statusCode();
        String responseBody = response == null ? "" : response.body();
        throw new IllegalStateException("Unable to assign vehicle. HTTP "
                + status + " - " + responseBody);
    }

        protected void declareMileageAsDriver(final String token,
            final long vehicleId,
            final int year,
            final int month,
            final long mileage) throws IOException, InterruptedException {
        String body = "{" +
            "\"vehicleId\":" + vehicleId + "," +
            "\"year\":" + year + "," +
            "\"month\":" + month + "," +
            "\"mileage\":" + mileage +
            "}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiRootUrl() + "/api/driver/mileage"))
            .timeout(Duration.ofSeconds(20))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        int maxAttempts = 4;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return;
            }
            if (attempt == maxAttempts
                || (response.statusCode() < 500 || response.statusCode() > 599)) {
                break;
            }
            Thread.sleep(1200L);
        }

        int status = response == null ? -1 : response.statusCode();
        String responseBody = response == null ? "" : response.body();
        throw new IllegalStateException("Unable to declare mileage. HTTP "
                + status + " - " + responseBody);
        }

    protected long extractId(final String json,
            final String entity) {
        Matcher matcher = ID_PATTERN.matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("Unable to parse " + entity
                + " id from response: " + json);
        }
        return Long.parseLong(matcher.group(1));
    }

    protected void openAuthenticatedPath(final String token,
            final String role,
            final String path) {
        driver.get(baseUrl() + "/login");
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "localStorage.setItem('fleet_token', arguments[0]);" +
                        "localStorage.setItem('fleet_role', arguments[1]);",
                token,
                role);
        driver.get(baseUrl() + path);
    }

    protected void loginViaUi(final String email,
            final String password) {
        driver.get(baseUrl() + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));
        driver.findElement(By.id("login-email")).clear();
        driver.findElement(By.id("login-email")).sendKeys(email);
        driver.findElement(By.id("login-password")).clear();
        driver.findElement(By.id("login-password")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }

    protected void bootstrapSessionForRole(final String email,
            final String password,
            final String role,
            final String path) throws IOException, InterruptedException {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            loginViaUi(email, password);
            try {
                new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                        ExpectedConditions.not(ExpectedConditions.urlContains("/login"))
                );
            } catch (org.openqa.selenium.TimeoutException timeoutException) {
                if (attempt == maxAttempts) {
                    throw timeoutException;
                }
            }

            if (!driver.getCurrentUrl().contains("/login")) {
                return;
            }

            Thread.sleep(800L);
        }

        throw new IllegalStateException("Unable to bootstrap authenticated session for role " + role);
    }
}
