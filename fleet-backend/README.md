# Fleet Backend

Backend Spring Boot application for the fleet-km app.

## Requirements

- Java 21
- Maven 3.9+
- IBM DB2 datasource

## Configuration

Edit `src/main/resources/application.yml` and set the DB2 connection values:

```yaml
spring:
	datasource:
		url: jdbc:db2://<host>:<port>/<db>
		username: <user>
		password: <pass>
```

JWT config (change for production):

```yaml
app:
	jwt:
		secret: change-this-secret
		expiration-ms: 86400000
```

## Run locally

```bash
cd "C:\Youness\Projet angular\fleet-backend"
mvn spring-boot:run
```

Default port: `http://localhost:8080`

## Run locally with H2 (profile e2e)

Pour exécuter le backend sans écrire dans DB2 (utile pour Selenium/E2E), utilisez le profil `e2e`:

```bash
cd "C:\Youness\Projet angular\fleet-backend"
mvn spring-boot:run "-Dspring-boot.run.profiles=e2e"
```

Le profil `e2e` utilise une base H2 en mémoire (`create-drop`) définie dans `src/main/resources/application-e2e.yml`.

## Run with Docker profile

When started from Docker Compose, backend runs with profile `docker`:

```bash
SPRING_PROFILES_ACTIVE=docker
```

Configuration file used: `src/main/resources/application-docker.yml`

## Tests

Run all tests:

```bash
mvn test
```

Run full build (includes Checkstyle and JaCoCo):

```bash
mvn clean verify
```

## API Endpoints

Auth:

- `POST /api/auth/register`
- `POST /api/auth/login`

Fleet:

- `GET /api/fleet/vehicles`
- `GET /api/fleet/mileage`
- `GET /api/fleet/missing`
- `GET /api/fleet/export`

Driver:

- `POST /api/driver/mileage`

Admin:

- `POST /api/admin/assign`

## Notes

- JWT tokens are stateless; implement revocation if needed.
