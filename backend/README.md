# School Management System - Backend API

REST API for a Vietnamese THCS/THPT (secondary/high school) management system, built with Spring Boot, Spring Security, and MySQL.

## 🚀 Quick Start

```bash
# 1. Copy env template and fill in real local values
cp .env.example .env

# 2. Run (env vars must be exported in the shell, or use an EnvFile plugin in your IDE)
export $(grep -v '^#' .env | xargs)   # bash/macOS/Linux; on Windows set them manually
mvn spring-boot:run

# 3. Swagger UI
http://localhost:8080/api/swagger-ui.html
```

`.env` is git-ignored — never commit real credentials. See [Configuration](#-configuration) below for what's required.

## 🔧 Technology Stack

- **Java 17**, **Spring Boot 3.1.5**, **Spring Security 6** (JWT, stateless)
- **Spring Data JPA** + **MySQL 8.0** (local)
- **Flyway** — versioned schema migrations (`src/main/resources/db/migration`)
- **Bean Validation** (Jakarta Validation) on all create/update endpoints
- **Lombok**, **SpringDoc OpenAPI 3.0** (Swagger)

## 📋 Modules

| Module | Endpoints | Notes |
|---|---|---|
| Auth | `/v1/auth/*` | JWT login/register/refresh |
| Users (admin) | `/v1/users` | ADMIN-only account creation with an explicit role |
| Staff | `/v1/staff/*` | Employee records |
| Students | `/v1/students/*` | Student profiles |
| Classes | `/v1/classes/*` | Homeroom class CRUD, GVCN assignment, roster |
| Academic Years | `/v1/academic-years/*` | Năm học CRUD + close |
| Semesters | `/v1/semesters/*` | Học kỳ (HK1/HK2) CRUD |
| Subjects | `/v1/subjects/*` | Môn học CRUD |
| Teaching Assignments | `/v1/teaching-assignments/*` | Phân công giảng dạy (teacher × subject × class × semester) |
| Timetable | `/v1/timetable/*` | Thời khoá biểu — class/teacher schedule, slot CRUD with teacher/room/class conflict checks |
| Attendance | `/v1/attendance/*` | Daily attendance |
| Grades | `/v1/grades/*` | Assessments |
| Fees | `/v1/fees/*` | Student fees & payments |
| Library | `/v1/library/*` | Book catalog & borrowing |
| Dashboard | `/v1/dashboard/stats` | Admin summary stats |

See [Swagger UI](http://localhost:8080/api/swagger-ui.html) for the full, current contract (request/response shapes, required fields) — the table above is just an index.

## 🗄️ Database

- **MySQL 8.0**, local. Charset **`utf8mb4`** / collation **`utf8mb4_unicode_ci`** is required (plain `utf8` only supports 3-byte characters and mangles Vietnamese diacritics).
- Schema is managed by **Flyway** (`src/main/resources/db/migration/`) — `ddl-auto` is `validate`, never `update`. To change the schema, add a new `V{n}__description.sql` migration; don't hand-edit the DB or rely on Hibernate to create tables.
- `V3__academic_structure.sql` added `AcademicYear`/`Semester`/`Subject` and backfilled them from the old free-text data (`classes.academic_year`, `grades.subject`). The old columns this replaces (`SchoolClass.academicYear` String, `Student.className`/`section`) are kept and marked `@Deprecated` on the entity — not dropped — so nothing already built against them breaks; new code should read/write `SchoolClass.academicYearRef` and `Student.currentClass` instead.
- `V4__teaching_timetable.sql` added `TeachingAssignment`/`TimetableSlot` (no backfill — nothing pre-3.2 represented this data).
- Create the database once:
  ```sql
  CREATE DATABASE school_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  ```

### Test data

```bash
mysql -u <DB_USERNAME> -p <DB_NAME> < TEST_DATA_CORRECTED.sql
```

Resets the seeded tables and inserts sample data (6 students, 6 staff, 4 classes, grades, fees, library loans...). All seeded accounts use password `Test@123`: `admin`, `principal`, `teacher1`/`teacher2`/`teacher3`, `librarian`, `accountant`, `student1`-`student6`.

## ⚙️ Configuration

Nothing is hard-coded — everything sensitive comes from environment variables (see `.env.example`):

| Variable | Required | Notes |
|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME` | dev: no (default `localhost:3306/school_management`) · prod: yes | |
| `DB_USERNAME`, `DB_PASSWORD` | yes | don't use `root` for day-to-day app use |
| `JWT_SECRET` | **yes, no default** | app fails fast at startup if missing — generate with `openssl rand -base64 64` |
| `JWT_EXPIRATION`, `JWT_REFRESH_EXPIRATION` | no | default 24h / 7d (ms) |
| `SPRING_PROFILES_ACTIVE` | no | `dev` (default, local MySQL, DEBUG logging, SQL logging on) or `prod` (strict env vars, INFO logging, SQL logging off) — see `application-dev.yml` / `application-prod.yml` |

## 🛡️ Security

### Roles
`ADMIN`, `PRINCIPAL`, `TEACHER`, `STUDENT`, `LIBRARIAN`, `ACCOUNTANT`, `PARENT` (defined; not yet wired into any module).

### Auth flow
1. `POST /v1/auth/login` → `{ accessToken, refreshToken, ... }`
2. Send `Authorization: Bearer <accessToken>` on every other request.
3. `POST /v1/auth/refresh-token` with `Authorization: Bearer <refreshToken>` once the access token expires.

### Registration
- `POST /v1/auth/register` is public and **always creates a STUDENT account** — the request DTO has no `role` field, so there is no way for a client to self-assign a privileged role.
- To create an account with any other role, an authenticated **ADMIN** calls `POST /v1/users` with an explicit `role`.

### Input validation & error responses
- Every create/update endpoint validates its body (`@Valid` + Bean Validation) and returns `400` with a field-level message on failure.
- Every response — success or failure — uses a consistent shape; errors look like:
  ```json
  {
    "status": "BAD_REQUEST",
    "message": "email: must be a well-formed email address",
    "code": 400,
    "path": "/api/v1/auth/register",
    "timestamp": "2026-08-20T10:30:00"
  }
  ```
- Unexpected server errors (`500`) never leak internal detail (SQL, stack traces, class names) to the client — they're logged server-side and the client gets a fixed generic message.

## 📄 Pagination

List endpoints that can return many rows accept optional `page` (0-indexed) and `size` query params: `GET /v1/students?page=0&size=20`. Supplying both returns that page (body stays a plain JSON array) plus an `X-Total-Count` response header with the total row count. Omitting either param returns the full, unpaginated list — existing callers are unaffected. Currently on: `/v1/students`, `/v1/staff`, `/v1/library/books`, `/v1/classes`, `/v1/grades/year/{academicYear}`, `/v1/fees/year/{academicYear}`.

## 🔧 Development

### Prerequisites
- Java 17+, Maven 3.6+, MySQL 8.0+ (local instance), Git

### Setup
```bash
git clone <repository-url> && cd backend
cp .env.example .env   # fill in DB credentials + JWT_SECRET
mvn clean install
mvn spring-boot:run
```

### Build
```bash
mvn spring-boot:run          # dev
mvn clean package            # production jar
java -jar target/school-management-system-1.0.0.jar
```

### Tests
```bash
mvn test
```
Runs against your local MySQL (via the `test` Spring profile — see `src/test/resources/application-test.yml`), so `DB_*`/`JWT_SECRET` env vars must be set the same as for `spring-boot:run`.

## 📖 More docs

- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html` — always up to date, source of truth for the API contract.
- **API docs (OpenAPI JSON)**: `http://localhost:8080/api/v3/api-docs`
- [ARCHITECTURE.md](./ARCHITECTURE.md) — system design
- [DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md) — local environment setup in detail
- [QUICKSTART.md](./QUICKSTART.md) — condensed getting-started
- [../IMPLEMENTATION_PLAN.md](../IMPLEMENTATION_PLAN.md) — the project's phased implementation plan (source of truth for what's built vs. planned)

## 📦 Key dependencies

Spring Boot Starter Web/Security/Data JPA/Validation, MySQL Connector/J, Flyway (`flyway-core` + `flyway-mysql`), JWT (`jjwt`), Lombok, SpringDoc OpenAPI.

## 📁 Project structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/schoolmanagement/
│   │   │   ├── config/          # Spring/security configuration
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Request/response DTOs
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── exception/       # Custom exceptions + GlobalExceptionHandler
│   │   │   ├── repository/      # Spring Data JPA repositories
│   │   │   ├── security/        # JWT filter/provider
│   │   │   ├── service/         # Business logic
│   │   │   └── util/            # Shared helpers (e.g. pagination)
│   │   └── resources/
│   │       ├── application.yml           # shared config
│   │       ├── application-dev.yml       # local dev profile (default)
│   │       ├── application-prod.yml      # production profile
│   │       └── db/migration/             # Flyway migrations
│   └── test/
│       ├── java/com/schoolmanagement/    # unit tests
│       └── resources/application-test.yml
├── TEST_DATA_CORRECTED.sql
├── pom.xml
└── README.md
```

## 🚀 Future enhancements

See the "Giai đoạn 3" section of [IMPLEMENTATION_PLAN.md](../IMPLEMENTATION_PLAN.md) for the full roadmap — Thông tư 22/58 grading & xếp loại học lực, hạnh kiểm, promotion workflow, parent portal & sổ liên lạc điện tử, admissions, PDF/Excel reports, audit log. (3.1 Năm học/Học kỳ/Môn học and 3.2 Phân công giảng dạy & Thời khoá biểu are done, above.)
