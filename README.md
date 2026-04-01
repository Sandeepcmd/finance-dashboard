# Finance Dashboard Backend & Frontend

A production-ready **Finance Data Processing and Access Control Backend** built with Spring Boot, paired with an integrated **Vanilla HTML/JS Frontend Dashboard**. Features JWT authentication, robust role-based access control (RBAC), financial records management, and real-time dashboard analytics. The frontend is seamlessly served as static content by the Spring Boot application.

---

## 🏗️ Tech Stack

### Backend
| Technology | Purpose |
|---|---|
| **Java 21** | Primary language & runtime |
| **Spring Boot 3.4.4** | Core backend framework |
| **Spring Security** | Authentication & authorization |
| **Spring Data JPA** | Database access / ORM |
| **MySQL 8.0** | Production database |
| **H2** | Development / testing database |
| **JWT (jjwt 0.12.6)** | Token-based stateless authentication |
| **Lombok** | Boilerplate code reduction |
| **Hibernate Validator** | Robust input validation |
| **SpringDoc OpenAPI** | API documentation (Swagger UI) |
| **JUnit 5 + Mockito** | Backend Testing |

### Frontend
| Technology | Purpose |
|---|---|
| **Vanilla HTML5** | Page structure |
| **Vanilla CSS3** | Styling & layout |
| **Vanilla JavaScript (ES6)** | Logic, API fetching, DOM manipulation |
| **Chart.js** | Data visualization for the dashboard |

---

## 📁 Architecture

```
com.finance
├── controller/         # REST API endpoints
├── service/            # Business logic
├── repository/         # Database access (Spring Data JPA)
├── entity/             # JPA entities and enums
├── dto/                # Data Transfer Objects
│   ├── auth/           # Authentication DTOs
│   ├── user/           # User management DTOs
│   ├── record/         # Financial record DTOs
│   └── dashboard/      # Dashboard analytics DTOs
├── mapper/             # Entity ↔ DTO mappers
├── security/           # JWT service, filters, entry points
├── config/             # Security, OpenAPI, and app configuration
├── exception/          # Global exception handling
├── util/               # Utility classes
└── FinanceDashboardApplication.java

src/main/resources
├── static/             # 🌐 Frontend UI (HTML, CSS, JS)
├── application.yml     # Core configurations (H2 default)
└── application-prod.yml# Production configurations (MySQL)
```

**Design Principles:**
- Layered architecture with clear separation of concerns
- DTOs for all API communication (preventing entity leakage)
- Global exception handling providing consistent JSON error responses
- Full inclusion of the Frontend inside `resources/static` for simplified monolithic deployment

---

## 🗄️ Database Schema

### Users Table

| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| name | VARCHAR(100) | NOT NULL |
| email | VARCHAR(150) | NOT NULL, UNIQUE |
| password | VARCHAR(255) | NOT NULL (BCrypt hashed) |
| role | VARCHAR(20) | NOT NULL (ADMIN, ANALYST, VIEWER) |
| status | VARCHAR(20) | NOT NULL (ACTIVE, INACTIVE) |
| created_at | DATETIME | NOT NULL, AUTO |

### Financial Records Table

| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| amount | DECIMAL(15,2) | NOT NULL |
| type | VARCHAR(10) | NOT NULL (INCOME, EXPENSE) |
| category | VARCHAR(100) | NOT NULL |
| date | DATE | NOT NULL |
| description | VARCHAR(500) | NULLABLE |
| created_by | BIGINT | FK → users(id) |
| deleted | BOOLEAN | DEFAULT FALSE (soft delete) |
| created_at | DATETIME | NOT NULL, AUTO |
| updated_at | DATETIME | AUTO |

---

## 🔐 Role Permissions (RBAC)

| Feature | VIEWER | ANALYST | ADMIN |
|---|:---:|:---:|:---:|
| View Dashboard | ✅ | ✅ | ✅ |
| View Records | ❌ | ✅ | ✅ |
| Create Records | ❌ | ✅ | ✅ |
| Update Records | ❌ | ❌ | ✅ |
| Delete Records | ❌ | ❌ | ✅ |
| Manage Users | ❌ | ❌ | ✅ |

---

## 🚀 Setup Instructions

### Prerequisites

- Java 21
- Maven 3.9+ (or use the included wrapper)
- MySQL 8.0 (optional — H2 is the default)

### 1. Quick Start (H2 — zero config)

```bash
# Clone the repository
git clone <repo-url>
cd finance-dashboard-backend

# Build and run using the Maven wrapper
mvnw.cmd spring-boot:run      # Windows
./mvnw spring-boot:run        # Linux/Mac
```

The application (and the integrated Frontend UI) starts at `http://localhost:8080`.
By default, the UI will be accessible immediately upon loading the root URL.

### 2. Running with MySQL

```bash
# 1. Create the database in MySQL
mysql -u root -p -e "CREATE DATABASE finance_dashboard;"

# 2. Run with MySQL profile
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod
```
*Note: Ensure you have `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` set in your environment variables, or update `application-prod.yml` directly.*

### Default Credentials (Loaded with H2 / Initial DB Creation)

| Role | Email | Password |
|---|---|---|
| Admin | admin@finance.com | admin123 |
| Analyst | analyst@finance.com | analyst123 |
| Viewer | viewer@finance.com | viewer123 |

---

## 📡 API Endpoints Overview

For a comprehensive layout, use the integrated Swagger UI available at:
👉 **`http://localhost:8080/swagger-ui.html`**

### Summary
- **`/api/auth/**`**: Registration & Login (Returns JWT).
- **`/api/users/**`**: Manage users (Admin strictly).
- **`/api/records/**`**: CRUD on financial records.
- **`/api/dashboard/**`**: Analytics, summaries, and categorized spending data.

---

## 🧪 Running Tests

```bash
mvnw.cmd test           # Windows
./mvnw test             # Linux/Mac
```

Test coverage encompasses key services (`AuthService`, `UserService`, `DashboardService`) and robust integration tests (`AuthControllerTest`).

---

## 🔮 Future Improvements

- [ ] Docker containerization
- [ ] Refresh token mechanism for persistent sessions
- [ ] Comprehensive audit logging (who changed what, when, and from where)
- [ ] Export records to CSV/PDF functionality
- [ ] File attachment support for receipts
- [ ] Budget tracking and real-time alerts
- [ ] Multi-currency support
- [ ] Rate limiting (e.g., configuring Bucket4j for API endpoint protection)
- [ ] Redis layer for caching dashboard metrics
- [ ] Email notifications via Spring Mail
- [ ] Two-factor authentication (2FA)

---

## 📄 License

This project is licensed under the MIT License.
