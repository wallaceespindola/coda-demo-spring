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

CODA (Coded Statement of Account) is a Belgian electronic format for exchanging financial data between banks and their
customers. This application provides a complete implementation for generating, parsing, and validating CODA files
according to the official specification.

### Features:

- **Full CODA Format Support**: Implements all record types (0, 1, 2.1, 2.2, 2.3, 3.1, 3.2, 8, 9)
- **IBAN Validation & Formatting**: Belgian IBAN auto-completion and validation
- **Spring Boot 3**: Modern Spring Boot application with Web + Actuator
- **OpenAPI/Swagger UI**: Interactive API documentation (springdoc)
- **Lombok Models**: Clean, concise data models
- **Validation**: JSON schema validation and input verification
- **Static Test Dashboard**: Interactive test interface at `/`
- **Comprehensive Unit Tests**: Full test coverage for parsing and generation
- **Docker Support**: Containerized deployment with Docker Compose
- **Postman Collection**: Ready-to-use API test collection

## CODA File Format

### Overview

CODA files are fixed-width text files where each line is exactly **128 characters**. The format uses multiple record
types to represent different aspects of a bank statement.

### Record Type Structure

| Record Type      | Line Position  | Class Name             | Key Fields                                         |
|------------------|----------------|------------------------|----------------------------------------------------|
| **0**            | Line 1         | `CodaHeaderRecord`     | Bank info, creation date, file reference, BIC      |
| **1**            | Line 2         | `CodaOldBalanceRecord` | Account number, old balance, balance date          |
| **2.1 (Global)** | Line 3         | `CodaRecord21`         | Global amount, globalisationCode="1", total of VCS |
| **2.1 (Detail)** | Multiple       | `CodaRecord21`         | Individual transaction amounts, references         |
| **2.2**          | Multiple       | `CodaRecord22`         | Counterparty name, BIC, transaction category       |
| **2.3**          | Multiple       | `CodaRecord23`         | Counterparty IBAN, account name                    |
| **3.1**          | Multiple       | `CodaRecord31`         | Structured communication, reference numbers        |
| **3.2**          | Multiple       | `CodaRecord32`         | Counterparty address, postal code, city            |
| **8**            | Second-to-last | `CodaNewBalanceRecord` | New balance, balance date, statement number        |
| **9**            | Last line      | `CodaTrailerRecord`    | Total record count, debit sum, credit sum          |

### File Structure Example

```
Line 1:  Record 0  - Header (bank info, date)
Line 2:  Record 1  - Old Balance
Line 3:  Record 2.1 - Global Transaction (globalisation code = "1")
Line 4:  Record 2.1 - Individual Transaction 1
Line 5:  Record 2.2 - Transaction 1 Counterparty Info
Line 6:  Record 2.3 - Transaction 1 Counterparty Account
Line 7:  Record 3.1 - Transaction 1 Structured Communication
Line 8:  Record 3.2 - Transaction 1 Address
Line 9:  Record 2.1 - Individual Transaction 2
...
Line N-1: Record 8  - New Balance
Line N:   Record 9  - Trailer (totals)
```

### Key Specifications

- **Line Length**: Exactly 128 characters per line
- **Character Encoding**: ASCII/ANSI
- **Date Format**: DDMMYY (6 digits)
- **Amount Format**: 15 digits with last 3 as decimals (e.g., 000000001234567 = 1234.567 EUR)
- **Position Indexing**: CODA spec uses 1-indexed positions
- **Field Padding**: Numeric fields are zero-padded left, alphanumeric fields are space-padded right

### Record Details

#### Record 0 - Header

- **Purpose**: Identifies the file and sender
- **Key Fields**: Bank ID (300), Application Code (05), File Reference, Addressee Name, BIC
- **Version**: "2" (position 128)

#### Record 1 - Old Balance

- **Purpose**: Opening balance of the account
- **Key Fields**: Account Number (37 chars with currency), Balance Sign, Old Balance Amount, Balance Date
- **Statement Number**: 3 digits (e.g., "024")

#### Record 2.1 - Transaction Data

- **Purpose**: Transaction details (global or individual)
- **Global Record**: When globalisation code = "1", represents sum of all transactions
- **Detail Records**: Individual transactions with globalisation code = "0"
- **Key Fields**: Reference Number, Movement Sign, Amount, Value Date, Transaction Code, Communication

#### Record 2.2 - Communication

- **Purpose**: Counterparty information
- **Key Fields**: Client Reference, Counterparty Name, Counterparty BIC
- **Transaction Category**: 1 char code

#### Record 2.3 - Counterparty Account

- **Purpose**: Counterparty IBAN details
- **Key Fields**: Counterparty IBAN (37 chars), Account Name (35 chars)
- **IBAN Format**: Belgian IBANs auto-completed with spaces

#### Record 3.1 - Structured Communication

- **Purpose**: Additional transaction details
- **Key Fields**: Reference Number, Transaction Code, Structured Communication (73 chars)
- **Format**: Often includes payment references like "+++123/4567/89012+++"

#### Record 3.2 - Counterparty Address

- **Purpose**: Geographic information
- **Key Fields**:
    - Address (35 chars, positions 11-45)
    - Postal Code (12 chars, positions 46-57)
    - City (35 chars, positions 58-92)

#### Record 8 - New Balance

- **Purpose**: Closing balance
- **Key Fields**: Account Number (37 chars), Balance Sign, New Balance, Balance Date
- **Filler**: Ends with "0" at position 128

#### Record 9 - Trailer

- **Purpose**: File summary and validation
- **Key Fields**: Number of Records (6 digits), Total Debit (15 digits), Total Credit (15 digits)
- **Trailer Marker**: Always "1" at position 128

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

### Key Components

#### Models (`model/`)

All CODA record types are implemented as Lombok `@Data` `@Builder` classes with:

- Complete field definitions matching CODA specification
- Javadoc tables showing field positions and descriptions
- Proper data types (String, BigDecimal, LocalDate, int)

#### Services (`service/`)

- **`CodaParser`**: Parses 128-character CODA lines into Java objects
    - Handles all record types (0, 1, 2.x, 3.x, 8, 9)
    - Extracts fields based on fixed positions
    - Converts dates and amounts
    - Completes Belgian IBANs

- **`CodaWriter`**: Writes Java objects to CODA format
    - Formats fields to exact widths
    - Pads with spaces or zeros as needed
    - Ensures 128-character lines
    - Handles ART grouping

- **`CodaGenerator`**: High-level CODA generation
    - Builds complete statements from parameters
    - Calculates balances
    - Generates sequence numbers
    - Creates all required records

#### Utilities (`util/`)

- **`IbanUtil`**: Belgian IBAN operations
    - Validates check digits
    - Formats with spaces (BE12 3456 7890 1234)
    - Completes partial IBANs

#### Controller (`controller/`)

- **`CodaController`**: REST endpoints
    - `GET /coda` - Generate with query parameters
    - `POST /coda/json` - Generate from JSON body
    - Returns CODA format as `text/plain`

## Testing

The project includes comprehensive unit tests for all CODA functionality.

### Test Structure

Tests are located in `src/test/java/com/example/coda/`:

#### Parser Tests

- **`CodaFileParserTest`** - Tests parsing of complete CODA files into Java objects
    - Validates all record types (0, 1, 2.x, 3.x, 8, 9)
    - Verifies field extraction and data integrity
    - Tests IBAN auto-completion
    - Validates balance calculations
    - Uses `coda_test.txt` reference file (35 lines, 128 chars each)

#### Writer Tests

- **`CodaWriterTest`** - Tests generation of CODA format from Java objects
    - Validates 128-character line length
    - Verifies trailing field markers (Record 8 ends "0", Record 9 ends "1")
    - Ensures proper field positioning

#### Generator Tests

- **`CodaGeneratorTest`** - Tests end-to-end CODA generation
    - Validates Belgian IBAN formatting
    - Tests balance calculations
    - Verifies header and trailer records
    - Tests with and without transactions

#### Parser-Writer Integration

- **`CodaParserWriterTest`** - Round-trip testing
    - Parse CODA → Modify → Write → Parse again
    - Ensures data integrity through full cycle

#### Utility Tests

- **`IbanUtilTest`** - Belgian IBAN validation and formatting
    - Tests IBAN completion with spaces
    - Validates check digits
    - Tests various IBAN formats

#### Controller Tests

- **`CodaControllerTest`** - REST API endpoint testing
    - Tests GET and POST endpoints
    - Validates request/response formats
    - Tests error handling

### Running Tests

Run all tests:

```bash
mvn test
```

Run specific test class:

```bash
mvn test -Dtest=CodaFileParserTest
```

Run specific test method:

```bash
mvn test -Dtest=CodaFileParserTest#parseCodaTestFileIntoJavaDataStructure
```

Run tests with Maven wrapper:

```bash
./mvnw test
```

Using Makefile:

```bash
make test
```

### Test Results

After running tests, results are available in:

- Console output
- `target/surefire-reports/` - Detailed XML and text reports
- `target/surefire-reports/*.txt` - Human-readable test summaries

### Test Coverage

The test suite covers:

- ✅ All 10 CODA record types
- ✅ Field extraction and validation
- ✅ Date parsing (DDMMYY format)
- ✅ Amount parsing (3-decimal precision)
- ✅ IBAN validation and formatting
- ✅ Balance calculations
- ✅ 128-character line formatting
- ✅ Trailing field markers
- ✅ Round-trip parsing and writing

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