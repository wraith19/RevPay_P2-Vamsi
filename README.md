<<<<<<< HEAD
# RevPay - Secure Digital Payments & Money Management

A full-stack monolithic financial web application built with Spring Boot, enabling secure digital payments and money management for both Personal and Business users.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Java 17, Spring Boot 3.2.3 |
| **Framework** | Spring MVC, Spring Data JPA, Spring Security |
| **Frontend** | Thymeleaf, HTML5, CSS3, JavaScript |
| **Database** | Oracle SQL (configurable) |
| **Testing** | JUnit 5, Mockito |
| **Logging** | Log4J2 |
| **Build** | Maven |
| **Export** | OpenCSV, iText PDF |

## Architecture

```
┌────────────────────────────────────────────────────┐
│                  Presentation Layer                 │
│        (Thymeleaf Views + CSS + JavaScript)         │
├────────────────────────────────────────────────────┤
│                  Controller Layer                   │
│  AuthController, DashboardController, Transaction   │
│  MoneyRequest, PaymentMethod, Wallet, Notification  │
│  Invoice, Loan, Profile, Analytics Controllers      │
├────────────────────────────────────────────────────┤
│                   Service Layer                     │
│  UserService, WalletService, TransactionService     │
│  PaymentMethodService, MoneyRequestService          │
│  NotificationService, InvoiceService, LoanService   │
│  AnalyticsService                                   │
├────────────────────────────────────────────────────┤
│                 Security Layer                      │
│    Spring Security + BCrypt + Role-Based Access     │
├────────────────────────────────────────────────────┤
│                 Repository Layer                    │
│              Spring Data JPA Repositories           │
├────────────────────────────────────────────────────┤
│                  Database Layer                     │
│               Oracle SQL (JPA/Hibernate)            │
└────────────────────────────────────────────────────┘
```

## Features

### Personal Account
- ✅ Registration with security questions
- ✅ Login with email/phone
- ✅ Wallet management (add funds, withdraw)
- ✅ Send money (by email, phone, or user ID)
- ✅ Request money (create, accept, decline, cancel)
- ✅ Payment method management (add, delete, set default)
- ✅ Transaction history with filters and search
- ✅ Export transactions to CSV/PDF
- ✅ In-app notifications with preferences
- ✅ Profile management (update profile, change password, set PIN)

### Business Account (all Personal features +)
- ✅ Business registration with tax ID and business info
- ✅ Invoice management (create, send, mark paid, cancel)
- ✅ Line item support with tax calculation
- ✅ Loan applications with EMI calculation
- ✅ Loan repayment tracking
- ✅ Business analytics dashboard
- ✅ Transaction summaries and revenue reports
- ✅ Top customers by transaction volume

### Security
- ✅ BCrypt password encryption
- ✅ Spring Security form-based authentication
- ✅ Role-based access control (PERSONAL / BUSINESS)
- ✅ Transaction PIN for sensitive operations
- ✅ CSRF protection
- ✅ Input validation (server-side + client-side)
- ✅ Session management with secure logout

## Database Setup (Oracle)

1. Create Oracle user:
```sql
CREATE USER revpay IDENTIFIED BY revpay123;
GRANT CONNECT, RESOURCE, DBA TO revpay;
```

2. Update `src/main/resources/application.properties` with your Oracle connection:
```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:xe
spring.datasource.username=revpay
spring.datasource.password=revpay123
```

## Running the Application

### Prerequisites
- Java 17+
- Maven 3.8+
- Oracle Database (XE or higher)

### Steps
```bash
# Clone the repository
git clone <repository-url>
cd RevPay_p2

# Build the project
./mvnw clean compile

# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test
```

The application will start at: **http://localhost:8080**

## Project Structure

```
src/main/java/com/rev/revpay_p2/
├── RevPayP2Application.java          # Main application
├── model/                             # JPA Entities & Enums
│   ├── User.java, Role.java
│   ├── Wallet.java
│   ├── Transaction.java, TransactionType.java, TransactionStatus.java
│   ├── PaymentMethod.java, PaymentMethodType.java
│   ├── MoneyRequest.java, RequestStatus.java
│   ├── Notification.java, NotificationType.java
│   ├── Invoice.java, InvoiceItem.java, InvoiceStatus.java
│   ├── Loan.java, LoanStatus.java
│   └── SecurityQuestion.java
├── repository/                        # Spring Data JPA Repositories
├── service/                           # Business Logic Layer
├── controller/                        # MVC Controllers
└── security/                          # Spring Security Configuration

src/main/resources/
├── application.properties             # App configuration
├── log4j2.xml                         # Logging configuration
├── static/css/styles.css              # Stylesheet
└── templates/                         # Thymeleaf views
    ├── fragments/layout.html
    ├── auth/ (login, register)
    ├── dashboard.html
    ├── transactions/ (send, history)
    ├── requests/ (create, incoming, outgoing)
    ├── payment-methods/ (list, add)
    ├── wallet/ (add-funds, withdraw)
    ├── notifications/ (list, preferences)
    ├── invoices/ (list, create, view)
    ├── loans/ (apply, list, detail)
    ├── analytics/ (dashboard)
    └── profile/ (edit, change-password, set-pin)
```

## Testing

Unit tests are provided for core services:
- `UserServiceTest` - Registration, password, PIN management
- `WalletServiceTest` - Fund operations, balance checks
- `MoneyRequestServiceTest` - Request lifecycle, authorization

Run tests: `./mvnw test`

## Entity Relationship Summary

| Entity | Relationships |
|--------|--------------|
| User | 1:1 Wallet, 1:N PaymentMethods, 1:N SecurityQuestions |
| Transaction | N:1 Sender (User), N:1 Receiver (User) |
| MoneyRequest | N:1 Requester (User), N:1 Requestee (User) |
| Notification | N:1 User |
| Invoice | N:1 BusinessUser, 1:N InvoiceItems |
| Loan | N:1 BusinessUser |

## Author

Developed as a full-stack monolithic web application project demonstrating enterprise-level Java development with Spring Boot.
=======
# RevPay_P2-Vamsi
RevPay is a full-stack monolithic financial web application that enables secure digital payments and money management for both personal and business users. Personal users can send/request money, manage payment methods, and track transactions through an intuitive web interface.
>>>>>>> 246f85f3ba5ca9351ce925c11ccf4f7b505532c5
