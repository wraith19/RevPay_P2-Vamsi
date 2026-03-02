# RevPay - Secure Digital Payments and Money Management

RevPay is a full-stack monolithic financial web application built with Spring Boot. It supports secure wallet operations for personal and business users, including payments, requests, invoices, loans, notifications, and analytics.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Backend | Spring Boot 3.4.3 |
| Web MVC | Spring Web, Thymeleaf |
| Security | Spring Security, BCrypt |
| Validation | Jakarta Bean Validation |
| Persistence | Spring Data JPA, Hibernate |
| Database | Oracle (runtime), H2 (tests) |
| Logging | Log4j2 |
| Frontend | HTML, CSS, Bootstrap 5, Bootstrap Icons |
| Export | OpenCSV, iText PDF |
| Build/Tooling | Maven, Maven Wrapper, Lombok, DevTools |
| Testing | JUnit 5, Mockito, Spring Boot Test, Spring Security Test |

## Core Features

### Personal Account
- Registration and login (email/phone + password)
- Wallet dashboard with balance and quick actions
- Send money and request money
- Incoming/outgoing request management (accept/decline/cancel)
- Payment method management (add/list/delete/set default)
- Transaction history with filters/search and CSV/PDF export
- Add funds and withdraw funds (simulated)
- In-app notifications with preference management
- Profile update, password change, transaction PIN

### Business Account
- Business registration details and verification workflow (simulated)
- Invoice lifecycle (create/send/mark paid/cancel) with line items and tax
- Loan application and repayment workflow (simulated)
- Business analytics dashboard (summaries, top customers, invoice stats)

### Security
- Role-based access control (`PERSONAL`, `BUSINESS`, `ADMIN`)
- Form login with secure logout
- CSRF enabled (default Spring Security)
- Password and PIN hashing with BCrypt
- Server-side validation and ownership checks on sensitive actions

## Project Structure

```text
src/main/java/com/rev/app/
  config/         Security and startup config
  controller/     MVC controllers
  rest/           REST API controllers
  service/        Service interfaces
  service/impl/   Business logic implementations
  repository/     JPA repositories
  entity/         Domain entities and enums
  dto/            API and request/response DTOs
  mapper/         Entity-to-DTO mappers
  exception/      Custom exceptions and handlers

src/main/resources/
  templates/      Thymeleaf views
  static/css/     Global stylesheet
  application.properties
  log4j2.xml
```

## Database Configuration (Oracle)

Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/XEPDB1
spring.datasource.username=<your_user>
spring.datasource.password=<your_password>
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
```

## Running the Application

### Prerequisites
- Java 21+
- Oracle database

### Commands

```bash
# Compile
./mvnw clean compile

# Run app
./mvnw spring-boot:run

# Run tests
./mvnw test
```

Application URL: `http://localhost:8080`

## Testing

Current tests cover key service behavior, including user, wallet, and money request flows.

## Notes

- Card and fund operations are simulated for project scope.
- Loan and business verification document flows are simulated.
- For production fintech systems, use payment tokenization providers and stricter compliance controls.
