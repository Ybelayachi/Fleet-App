package com.fleetkm.e2e;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FleetExportE2ETest extends BaseE2ETest {

    @Test
    void shouldExportFleetCsvForSelectedPeriod() throws Exception {
        String id = runId();
        String password = testPassword();

        String adminEmail = emailFor("admin.export", id);
        String driverEmail = emailFor("driver.export", id);
        String vin = vinFor("VINEXP", id);
        String plate = plateFor("EX-", id);

        ensureUserExists(adminEmail, password, "ROLE_ADMIN");

        String adminToken = loginAndGetToken(adminEmail, password);
        long driverId = createUserAsAdmin(adminToken, driverEmail, password,
                "Driver", "Export", "ROLE_DRIVER");
        long vehicleId = createVehicleAsAdmin(adminToken, vin, "Peugeot",
                "e-208", plate, "2025-01-15");
        assignVehicleAsAdmin(adminToken, driverId, vehicleId);

        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        String driverToken = loginAndGetToken(driverEmail, password);
        declareMileageAsDriver(driverToken, vehicleId, year, month, 3456L);

        HttpRequest exportRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiRootUrl() + "/api/fleet/export?year=" + year + "&month=" + month))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + adminToken)
                .GET()
                .build();

        HttpResponse<String> exportResponse = HttpClient.newHttpClient()
                .send(exportRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, exportResponse.statusCode());
        assertTrue(exportResponse.headers().firstValue("Content-Type")
                .orElse("")
                .contains("text/csv"));

        String csv = exportResponse.body();
        assertTrue(csv.contains("vehicleId;vin;year;month;mileage;declaredBy;declaredAt"));
        assertTrue(csv.contains(vin));
        assertTrue(csv.contains(driverEmail));
        assertTrue(csv.contains(";" + year + ";" + month + ";3456;"));
    }
}
