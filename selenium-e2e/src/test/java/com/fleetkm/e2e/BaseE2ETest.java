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
import org.openqa.selenium.Keys;
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
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-zygote");
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
        org.openqa.selenium.JavascriptExecutor js =
                (org.openqa.selenium.JavascriptExecutor) driver;

        driver.get(baseUrl() + "/login");
        js.executeScript(
                "localStorage.setItem('fleet_token', arguments[0]);" +
                        "localStorage.setItem('fleet_role', arguments[1]);",
                token,
                role);

        // Verify localStorage was set before navigation
        Object tokenBefore = js.executeScript("return localStorage.getItem('fleet_token');");
        Object roleBefore  = js.executeScript("return localStorage.getItem('fleet_role');");
        System.out.printf("[E2E-DEBUG] localStorage BEFORE nav: token=%s role=%s%n",
                tokenBefore != null ? "PRESENT(" + tokenBefore.toString().length() + "ch)" : "NULL",
                roleBefore);

        driver.get(baseUrl() + path);

        // Verify localStorage after navigation (same origin — must persist)
        Object tokenAfter = js.executeScript("return localStorage.getItem('fleet_token');");
        Object roleAfter  = js.executeScript("return localStorage.getItem('fleet_role');");
        System.out.printf("[E2E-DEBUG] localStorage AFTER nav to %s: token=%s role=%s  url=%s%n",
                path,
                tokenAfter != null ? "PRESENT(" + tokenAfter.toString().length() + "ch)" : "NULL",
                roleAfter,
                driver.getCurrentUrl());
    }

    /**
     * Waits until the PrimeNG p-select for the vehicle has a selected value,
     * i.e. the label no longer carries the p-placeholder CSS class.
     * This is reliable in production builds where __ngContext__ is not available.
     */
    protected void waitForVehicleSelected(final WebDriverWait longWait) {
        longWait.until(d -> {
            try {
                org.openqa.selenium.WebElement label =
                        d.findElement(By.cssSelector("p-select .p-select-label"));
                String cls = label.getAttribute("class");
                String text = label.getText();
                boolean isPlaceholder = cls != null && cls.contains("p-placeholder");
                boolean isBlankOrHint = text == null || text.trim().isEmpty()
                        || text.contains("Sélectionner");
                return !isPlaceholder && !isBlankOrHint;
            } catch (org.openqa.selenium.NoSuchElementException e) {
                return false;
            }
        });
    }

    /**
     * Fills the mileage input on /driver/mileage reliably in headless Chrome.
     *
     * The Angular app is zoneless (provideZonelessChangeDetection), so DOM input
     * events from Selenium do not automatically trigger Angular's change detection.
     * The DriverMileageComponent has a (blur) handler that explicitly calls
     * form.patchValue({ mileage }) from the raw DOM value when the field loses
     * focus.  Sending Keys.TAB reliably fires the blur event, which Angular's
     * event listener picks up and uses to update the FormControl.
     */
    protected void fillMileageInput(final String mileageValue) {
        org.openqa.selenium.JavascriptExecutor js =
                (org.openqa.selenium.JavascriptExecutor) driver;
        org.openqa.selenium.WebElement el =
                driver.findElement(By.id("mileage-value"));
        js.executeScript("arguments[0].scrollIntoView(true);", el);
        // Click to focus, select any pre-existing content, type the new value.
        el.click();
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        el.sendKeys(mileageValue);
        // TAB fires the blur event → Angular's (blur) handler patches the FormControl.
        el.sendKeys(Keys.TAB);
        System.out.printf("[E2E-DEBUG] mileage fill: value='%s' classes='%s'%n",
                el.getAttribute("value"), el.getAttribute("class"));
        // Wait until the form itself has ng-valid (both vehicleId and mileage controls valid).
        // This is necessary in zoneless Angular where CD is asynchronous.
        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10))
                .until(org.openqa.selenium.support.ui.ExpectedConditions
                        .presenceOfElementLocated(By.cssSelector("form.ng-valid")));
    }

    /**
     * Injects a real JWT token directly into localStorage and navigates to the
     * role's default landing page, bypassing the login form entirely.
     *
     * This approach is necessary in headless Chrome (Docker) because Angular's
     * zoneless reactive-form controls do not reliably process synthetic DOM events
     * dispatched by Selenium's JavaScript executor.
     */
    protected void loginViaUi(final String email,
            final String password,
            final String role) throws IOException, InterruptedException {
        String token = loginAndGetToken(email, password);
        org.openqa.selenium.JavascriptExecutor js =
                (org.openqa.selenium.JavascriptExecutor) driver;
        // Navigate to the login page first so localStorage is set on the
        // correct origin before navigating to the protected path.
        driver.get(baseUrl() + "/login");
        js.executeScript(
                "localStorage.setItem('fleet_token', arguments[0]);" +
                "localStorage.setItem('fleet_role', arguments[1]);",
                token, role);
        driver.get(baseUrl() + defaultPathForRole(role));
    }

    private String defaultPathForRole(final String role) {
        if ("ROLE_ADMIN".equals(role)) {
            return "/admin/users";
        } else if ("ROLE_FLEET_MANAGER".equals(role)) {
            return "/fleet/dashboard";
        } else {
            return "/driver/vehicles";
        }
    }

    protected void bootstrapSessionForRole(final String email,
            final String password,
            final String role,
            final String path) throws IOException, InterruptedException {
        // Obtain a real JWT via the API, inject it into localStorage, then
        // navigate directly to the target path so Angular's guards find both
        // fleet_token and fleet_role in localStorage on boot (no form involved).
        String token = loginAndGetToken(email, password);
        openAuthenticatedPath(token, role, path);
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
        if (!driver.getCurrentUrl().contains(path)) {
            driver.get(baseUrl() + path);
            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
        }
    }
}
