# Finance Dashboard Backend

A production-ready **Finance Data Processing and Access Control Backend** built with Spring Boot. Features JWT authentication, role-based access control (RBAC), financial records management, and real-time dashboard analytics.

---

## 🏗️ Tech Stack

| Technology | Purpose |
|---|---|
| **Java 17+** | Runtime (built with Java 21) |
| **Spring Boot 3.2** | Application framework |
| **Spring Security** | Authentication & authorization |
| **Spring Data JPA** | Database access |
| **MySQL 8.0** | Production database |
| **H2** | Development / testing database |
| **JWT (jjwt 0.12)** | Token-based authentication |
| **Lombok** | Boilerplate reduction |
| **Hibernate Validator** | Input validation |
| **SpringDoc OpenAPI** | API documentation (Swagger UI) |

| **JUnit 5 + Mockito** | Testing |

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
```

**Design Principles:**
- Layered architecture with clear separation of concerns
- DTOs for all API communication (no entity leakage)
- Global exception handling with consistent error responses
- Repository pattern for database operations
- Service layer for business logic

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

- Java 17 or higher
- Maven 3.8+ (or use the included wrapper)
- MySQL 8.0 (optional — H2 is the default)

### Quick Start (H2 — zero config)

```bash
# Clone the repository
git clone <repo-url>
cd finance-dashboard-backend

# Build and run
mvnw.cmd spring-boot:run      # Windows
./mvnw spring-boot:run          # Linux/Mac
```

The app starts at `http://localhost:8080` with an embedded H2 database.

### With MySQL

```bash
# 1. Create the database
mysql -u root -p -e "CREATE DATABASE finance_dashboard;"

# 2. Run with MySQL profile
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
```



### Default Credentials

| Role | Email | Password |
|---|---|---|
| Admin | admin@finance.com | admin123 |
| Analyst | analyst@finance.com | analyst123 |
| Viewer | viewer@finance.com | viewer123 |

---

## 📡 API Endpoints

### Authentication (Public)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and get JWT token |

### User Management (Admin only)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users` | Create a user |
| GET | `/api/users` | List all users |
| GET | `/api/users/{id}` | Get user by ID |
| PUT | `/api/users/{id}` | Update user |
| PATCH | `/api/users/{id}/status` | Activate/deactivate user |
| DELETE | `/api/users/{id}` | Delete user |

### Financial Records (Admin + Analyst)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/records` | Create a record |
| GET | `/api/records` | List records (paginated, filterable) |
| GET | `/api/records/{id}` | Get record by ID |
| PUT | `/api/records/{id}` | Update record (Admin only) |
| DELETE | `/api/records/{id}` | Soft-delete record (Admin only) |

**Filtering Parameters:**
- `type` — INCOME or EXPENSE
- `category` — Category name
- `startDate` / `endDate` — Date range (yyyy-MM-dd)
- `search` — Description keyword search
- `page`, `size`, `sort` — Pagination

### Dashboard Analytics (All authenticated users)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/dashboard/summary` | Total income, expense, net balance |
| GET | `/api/dashboard/category-summary` | Spending by category |
| GET | `/api/dashboard/monthly-trends` | Monthly income vs expense |
| GET | `/api/dashboard/recent-activity` | 10 most recent transactions |

---

## 📋 Example API Requests

### Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "secure123"
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@finance.com",
    "password": "admin123"
  }'
```

### Create Financial Record

```bash
curl -X POST http://localhost:8080/api/records \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 5000.00,
    "type": "INCOME",
    "category": "Salary",
    "date": "2025-03-01",
    "description": "March monthly salary"
  }'
```

### Get Records with Filters

```bash
curl "http://localhost:8080/api/records?type=EXPENSE&category=Food&page=0&size=10" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### Dashboard Summary

```bash
curl http://localhost:8080/api/dashboard/summary \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

## 📖 API Documentation

Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON: `http://localhost:8080/api-docs`

---

## 🧪 Running Tests

```bash
mvnw.cmd test           # Windows
./mvnw test              # Linux/Mac
```

Test coverage includes:
- **AuthServiceTest** — Registration, login, duplicate email handling
- **UserServiceTest** — Full CRUD, status management, validation
- **DashboardServiceTest** — Summary, category breakdown, monthly trends
- **AuthControllerTest** — Integration tests with MockMvc

---

## 🔮 Future Improvements

- [ ] Refresh token mechanism
- [ ] Audit logging (who changed what, when)
- [ ] Export records to CSV/PDF
- [ ] File attachment support for receipts
- [ ] Budget tracking and alerts
- [ ] Multi-currency support
- [ ] Rate limiting with Bucket4j
- [ ] Caching with Redis
- [ ] Email notifications
- [ ] Two-factor authentication (2FA)
- [ ] Docker containerization
- [ ] Integration tests with Testcontainers

---

## 📄 License

This project is licensed under the MIT License.
