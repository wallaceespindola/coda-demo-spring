![Java](https://cdn.icon-icons.com/icons2/2699/PNG/512/java_logo_icon_168609.png)

# CODA Demo (Spring Boot + Swagger + Lombok + Actuator + DevTools)

![Apache 2.0 License](https://img.shields.io/badge/License-Apache2.0-orange)
![Java](https://img.shields.io/badge/Built_with-Java21-blue)
![Junit5](https://img.shields.io/badge/Tested_with-Junit5-teal)
![Spring](https://img.shields.io/badge/Structured_by-SpringBoot-lemon)
![Maven](https://img.shields.io/badge/Powered_by-Maven-pink)
![Swagger](https://img.shields.io/badge/Docs_by-Swagger-yellow)
![OpenAPI](https://img.shields.io/badge/Specs_by-OpenAPI-purple)
[![CI](https://github.com/wallaceespindola/coda-demo/actions/workflows/ci.yml/badge.svg)](https://github.com/wallaceespindola/coda-demo/actions/workflows/ci.yml)

## Introduction

Generate and preview/download a **Belgian CODA-style** bank statement file via REST.

### Features:

- Spring Boot 3 (Web + Actuator)
- OpenAPI/Swagger UI (springdoc)
- Lombok models
- Validation + JSON schema docs
- Static test dashboard at `/`
- Unit tests
- Docker + Compose
- Postman collection

## Run

Requirements:

- Java 21 (Temurin recommended)
- Maven 3.9+

Run locally (Maven):

```bash
mvn spring-boot:run
```

Run with Docker (build and run):

```bash
# Build container image
docker build -t coda-demo:latest .

# Run container
docker run --rm -p 8080:8080 coda-demo:latest
```

Run with Docker Compose:

```bash
docker compose up --build
```

The app will be available at http://localhost:8080

## API

Controller: `src/main/java/com/example/coda/web/CodaController.java`

- `GET /coda`
    - Query params:
        - `bankName` (default: `BELFIUS`)
        - `account` (default: `BE68 5390 0754 7034`)
        - `currency` (default: `EUR`)
        - `date` (optional; `yyyy-MM-dd`. If omitted, today)
        - `opening` (default: `1200.00`)
    - Produces: `text/plain` CODA-like content

Example:

```bash
curl -i "http://localhost:8080/coda?bankName=BELFIUS&account=BE68%205390%200754%207034&currency=EUR&opening=1200.00"
```

- `POST /coda/json`
    - Body: JSON
    - Produces: `text/plain`

Example:

```bash
curl -i -X POST \
  -H "Content-Type: application/json" \
  -d '{
        "bankName": "BELFIUS",
        "account": "BE68 5390 0754 7034",
        "currency": "EUR",
        "date": "2025-01-15",
        "opening": 1200.00
      }' \
  http://localhost:8080/coda/json
```

## Actuator & Docs

Config: `src/main/resources/application.yml`

Exposed endpoints (by default):

- `GET /actuator/health`
- `GET /actuator/info`

Docs:

- Swagger UI: `GET /swagger-ui` (or `/swagger-ui.html` if configured)
- OpenAPI JSON: `GET /v3/api-docs`

If you want more Actuator endpoints (metrics, env, etc.), add them to `management.endpoints.web.exposure.include`.

## Postman

Collections in `postman/`:

- `CODA_Demo.postman_collection.json` — Organized requests for API and Actuator with basic tests.

Import into Postman and set the variable:

- `baseUrl` (default: `http://localhost:8080`)

## Makefile targets

Location: `Makefile`

- `make run` — `mvn spring-boot:run`
- `make test` — run unit tests
- `make build` — build JAR with Maven
- `make docker-build` — build Docker image
- `make docker-run` — run Docker image
- `make compose-up` / `make compose-down` — Docker Compose
- `make clean` — Maven clean

## Build a JAR

```bash
mvn -q -DskipTests package
ls -1 target/
```

Run the JAR:

```bash
java -jar target/coda-demo-*.jar
```

## Testing

Tests live in `src/test/java/`.

Example added: `CodaGeneratorTest#testGenerateWithBelgianIban` validates generation with a Belgian IBAN and checks
record markers and date formatting.

Run tests:

```bash
mvn test
```

## Tech stack

- Spring Boot 3
- Java 21 (Temurin)
- springdoc-openapi
- Lombok
- Maven
- Docker / Docker Compose

## Author

- Wallace Espindola, Sr. Software Engineer / Solution Architect / Java & Python Dev
- **LinkedIn:** [linkedin.com/in/wallaceespindola/](https://www.linkedin.com/in/wallaceespindola/)
- **GitHub:** [github.com/wallaceespindola](https://github.com/wallaceespindola)
- **E-mail:** [wallace.espindola@gmail.com](mailto:wallace.espindola@gmail.com)
- **Twitter:** [@wsespindola](https://twitter.com/wsespindola)
- **Gravatar:** [gravatar.com/wallacese](https://gravatar.com/wallacese)
- **Dev Community:** [dev.to/wallaceespindola](https://dev.to/wallaceespindola)
- **DZone Articles:** [DZone Profile](https://dzone.com/users/1254611/wallacese.html)
- **Pulse Articles:** [LinkedIn Articles](https://www.linkedin.com/in/wallaceespindola/recent-activity/articles/)
- **Website:** [W-Tech IT Solutions](https://www.wtechitsolutions.com/)
- **Presentation Slides:** [Speakerdeck](https://speakerdeck.com/wallacese)

## License

- This project is released under the Apache 2.0 License.
- See the [LICENSE](LICENSE) file for details.
- Copyright © 2025 [Wallace Espindola](https://github.com/wallaceespindola/).