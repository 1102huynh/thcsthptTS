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
| Grades (legacy) | `/v1/grades/*` | Percentage-based assessments — kept for Phase 1-2 compatibility, not TT22-based |
| Grade Records | `/v1/grade-records/*` | Điểm theo Thông tư 22/2021 — thang điểm 10, per component type (miệng/15p/1 tiết/giữa kỳ/cuối kỳ); supersedes Grades above. Điểm TB học kỳ/cả năm per subject via `/student/{id}/summary` and `/student/{id}/year-summary` |
| Grade Config | `/v1/grade-config/*` | ADMIN-only: hệ số (weight) per component type, scoped by the academic year it starts applying from |
| Conduct (Hạnh kiểm) | `/v1/conduct/*` | Đánh giá hạnh kiểm/rèn luyện theo học kỳ (TOT/KHA/TRUNG_BINH/YEU), one per student per semester. TEACHER may only write for students in the class they are GVCN of; class/semester roster view for bulk entry |
| Promotion Thresholds | `/v1/promotion-thresholds/*` | ADMIN/PRINCIPAL-only: cutoffs (điểm TB môn thấp nhất, hạnh kiểm tối thiểu, tỷ lệ nghỉ tối đa) used to suggest xét lên lớp decisions, scoped by academic year |
| Promotions (Xét lên lớp) | `/v1/promotions/*` | Xét lên lớp/ở lại/tốt nghiệp — live preview per class (not persisted) + `POST /confirm` to save the final decision (bulk, overwrite-on-reconfirm). ADMIN/PRINCIPAL confirm; ADMIN/PRINCIPAL/TEACHER can preview |
| Parents (Phụ huynh) | `/v1/parents/*` | Links a PARENT-role account to their children (ADMIN-managed); a PARENT may only list their own children |
| Notifications (Sổ liên lạc điện tử) | `/v1/notifications/*` | Created and sent in the same request. `APP`/`EMAIL` channels are live; `SMS`/`ZALO` return 501 pending a vendor/Zalo OA decision. `GET /my` + `PUT /{id}/read` for any recipient (PARENT or staff) |
| Admissions (Tuyển sinh) | `/v1/admissions/*` | `POST` is public (no login), rate-limited per IP (see AdmissionRateLimitFilter). ADMIN reviews (`PUT /{id}/status`) then `POST /{id}/approve-and-create` turns an APPROVED application into a real STUDENT account without retyping name/DOB/phone |
| Reports (Báo cáo) | `/v1/reports/*` | PDF/Excel exports — `GET /student/{id}/transcript?academicYearId=` (bảng điểm/học bạ PDF), `GET /class/{id}/attendance?from=&to=` (điểm danh Excel), `GET /fees/receipt/{feeId}` (biên lai PDF, 400 if the fee has no payment recorded yet). Every endpoint requires login; STUDENT/PARENT are limited to their own/child's data (same `StudentAccessGuard`/`FeeService` checks the underlying data's own endpoints use) |
| Fees | `/v1/fees/*` | Student fees & payments |
| Library | `/v1/library/*` | Book catalog & borrowing |
| Dashboard | `/v1/dashboard/stats` | Admin summary stats |

See [Swagger UI](http://localhost:8080/api/swagger-ui.html) for the full, current contract (request/response shapes, required fields) — the table above is just an index.

## 🗄️ Database

- **MySQL 8.0**, local. Charset **`utf8mb4`** / collation **`utf8mb4_unicode_ci`** is required (plain `utf8` only supports 3-byte characters and mangles Vietnamese diacritics).
- Schema is managed by **Flyway** (`src/main/resources/db/migration/`) — `ddl-auto` is `validate`, never `update`. To change the schema, add a new `V{n}__description.sql` migration; don't hand-edit the DB or rely on Hibernate to create tables.
- `V3__academic_structure.sql` added `AcademicYear`/`Semester`/`Subject` and backfilled them from the old free-text data (`classes.academic_year`, `grades.subject`). The old columns this replaces (`SchoolClass.academicYear` String, `Student.className`/`section`) are kept and marked `@Deprecated` on the entity — not dropped — so nothing already built against them breaks; new code should read/write `SchoolClass.academicYearRef` and `Student.currentClass` instead.
- `V4__teaching_timetable.sql` added `TeachingAssignment`/`TimetableSlot` (no backfill — nothing pre-3.2 represented this data).
- `V5__grading_tt22.sql` added `grade_records`/`grade_component_configs` (Thông tư 22/2021 grading). No backfill from the old `grades` table — the two scoring models (percentage-of-total vs. thang điểm 10 by component type) don't map onto each other automatically — and no default weight rows are seeded; an ADMIN must configure them via `POST /v1/grade-config` before anyone can enter grades.
- `V6__conduct_records.sql` added `conduct_records` (hạnh kiểm/rèn luyện), one row per (student, semester) enforced by a unique constraint. No backfill — nothing pre-3.4 represented this data.
- `V7__promotion_records.sql` added `promotion_threshold_configs` (no default rows — an ADMIN/PRINCIPAL must configure cutoffs via `POST /v1/promotion-thresholds` before any suggestion can be computed) and `promotion_records` (one row per student per academic year, unique constraint enforced). No backfill.
- `V8__parents_notifications.sql` added `parent_student_relations` (unique per parent+student), `notifications`, `notification_recipients`. No backfill — nothing pre-3.6 represented this data.
- `V9__admission_applications.sql` added `admission_applications` (optimistic-locked via `@Version` — see the Security note on approve-and-create below). No backfill — nothing pre-3.7 represented this data.
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
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_SMTP_AUTH`, `MAIL_SMTP_STARTTLS` | no | EMAIL notification channel (3.6) — all have defaults; without a real SMTP server, sending just fails per-recipient (recorded, not a crash) |
| `MAIL_FROM` | no | default `no-reply@school.local` — the "From" address on outgoing EMAIL notifications |
| `ADMISSION_RATE_LIMIT_MAX`, `ADMISSION_RATE_LIMIT_WINDOW_MINUTES` | no | anti-spam limit on public `POST /v1/admissions` (3.7) — default 5 requests / 60 minutes per IP |
| `ADMISSION_TRUST_FORWARDED_FOR` | no | default `false` (rate-limits by the real TCP peer address) — only set `true` behind a reverse proxy that sets/overwrites `X-Forwarded-For` itself, otherwise a caller can spoof it to dodge the limit |

## 🛡️ Security

### Roles
`ADMIN`, `PRINCIPAL`, `TEACHER`, `STUDENT`, `LIBRARIAN`, `ACCOUNTANT`, `PARENT`. `PARENT` is wired into every per-student endpoint that has an object-level ownership check (see below) as of 3.6 — a parent can read (and, for fees, pay) their own children's grades/conduct/promotions/attendance/fees, resolved via `/v1/parents` (`ParentStudentRelation`).

### Auth flow
1. `POST /v1/auth/login` → `{ accessToken, refreshToken, ... }`
2. Send `Authorization: Bearer <accessToken>` on every other request.
3. `POST /v1/auth/refresh-token` with `Authorization: Bearer <refreshToken>` once the access token expires.

### Registration
- `POST /v1/auth/register` is public and **always creates a STUDENT account** — the request DTO has no `role` field, so there is no way for a client to self-assign a privileged role.
- To create an account with any other role, an authenticated **ADMIN** calls `POST /v1/users` with an explicit `role`.

### Object-level authorization (own-record access)
Endpoints scoped to `{studentId}` in the path/query — `/v1/grade-records/*`, `/v1/conduct/student/{id}`, `/v1/promotions/student/{id}`, and (since 3.6) the legacy `/v1/grades/*`, `/v1/fees/*`, `/v1/attendance/*` — additionally check, via the shared `StudentAccessGuard`: a `STUDENT` caller may only access their own id; a `PARENT` caller may only access a student they're linked to via `ParentStudentRelation` (`/v1/parents`). A nonexistent target id correctly reports 404, not 403. `ADMIN`/`TEACHER`/`PRINCIPAL`/`ACCOUNTANT` callers are unrestricted.

Fixed alongside this in 3.6: the legacy Grade/Fee/Attendance single-student read endpoints used to return the raw JPA entity, which threw `LazyInitializationException` (masked as a 500) for **every** role, not just the new PARENT one, because `open-in-view` is off and the lazy `student`/`teacher`/`markedBy` associations were never resolved before serialization — found live while adding the PARENT check, fixed by returning `GradeDTO`/`FeeDTO`/`AttendanceDTO` instead (same pattern already used for the multi-student `.../year/{academicYear}` listings).

`/v1/conduct` additionally enforces a GVCN (homeroom-teacher) check on writes: a `TEACHER` may only create/update a conduct record for a student in a class they are `classTeacher` of, and may only submit it under their own staff profile (`evaluatedBy` must match). `ADMIN` is unrestricted. Updating an existing record re-checks this against *both* the record's current student and the (possibly different) target student, so a teacher can't "steal" another GVCN's record by reassigning it to their own class.

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

Spring Boot Starter Web/Security/Data JPA/Validation/Mail, MySQL Connector/J, Flyway (`flyway-core` + `flyway-mysql`), JWT (`jjwt`), Lombok, SpringDoc OpenAPI, **OpenPDF** (PDF reports, 3.8 — LGPL/MPL, chosen over iText7 specifically because iText7 is AGPL and would require either open-sourcing this app or a commercial iText license), **Apache POI** (`poi-ooxml`, Excel reports, 3.8).

PDF reports embed **DejaVu Sans** (`src/main/resources/fonts/`, Bitstream Vera license — see `DejaVuSans-LICENSE.txt` alongside it) so Vietnamese diacritics render correctly; OpenPDF's built-in fonts only cover Latin-1 and silently mangle them otherwise.

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

See the "Giai đoạn 3" section of [IMPLEMENTATION_PLAN.md](../IMPLEMENTATION_PLAN.md) for the full roadmap — hạ tầng dùng chung (document attachments, audit log, forgot-password). (3.1 Năm học/Học kỳ/Môn học, 3.2 Phân công giảng dạy & Thời khoá biểu, 3.3 Điểm theo TT22, 3.4 Hạnh kiểm/Rèn luyện, 3.5 Xét lên lớp/Ở lại/Tốt nghiệp, 3.6 Phụ huynh & Sổ liên lạc điện tử, 3.7 Tuyển sinh đầu cấp, and 3.8 Xuất báo cáo PDF/Excel are done, above.)

**Note on 3.8**: the transcript PDF shows raw điểm trung bình (per the same formulas as `/v1/grade-records`) and hạnh kiểm per semester — it never shows xếp loại học lực, since that classification still isn't implemented (see the 3.3 note below). Its "Lớp" line is labelled "Lớp (hiện tại)" deliberately — `Student` has no per-academic-year class history, only the student's *current* class, so a transcript pulled for a past year can't show which class they were actually in back then. The class attendance Excel has one column per calendar day in the requested range — fine for a week/month; `[from, to]` is capped at 366 days (one academic year) and returns 400 past that, both to keep the sheet from exceeding Excel's column limit and because that's already far past any realistic use of this report. `GET /fees/receipt/{feeId}` rejects (400) a fee with no `paidAmount` recorded yet — a receipt only makes sense as proof of an actual payment. Every `/v1/reports/*` endpoint's role list deliberately matches the equivalent existing endpoint for the same data (e.g. the transcript matches `/v1/grade-records/student/{id}/year-summary`'s ADMIN/TEACHER/STUDENT/PARENT, not PRINCIPAL) — a report is a different shape of the same data, not a different access policy, so it doesn't unilaterally decide PRINCIPAL should see more than the underlying API already allows.

**Note on 3.7**: `POST /v1/admissions/{id}/approve-and-create` requires the ADMIN to supply `username`/`email`/`password`/`rollNumber`/`admissionNumber` explicitly — nothing in `AdmissionApplication` can populate those (no login was ever collected from a public applicant, and roll/admission numbers follow the school's own numbering scheme, not something to invent). Everything else (name, DOB, phone, priorSchool) is pulled from the application automatically. The application row is optimistic-locked (`@Version`) specifically to prevent a double-click/concurrent-request race from creating two separate accounts from the same application — a second concurrent call gets a clean 409, not a duplicate account.

**Note on 3.6**: only the `APP` and `EMAIL` notification channels are implemented — the plan explicitly requires choosing an SMS provider (eSMS/FPT SMS) and registering a Zalo OA before building `SMS`/`ZALO` for real, and there's no budget/vendor decision yet. Both channels exist in `NotificationChannel` and have a registered `NotificationSender` bean, but calling either returns `501 Not Implemented` with a clear message (see `Sms/ZaloOaNotificationSender`) rather than pretending to send. Recipients are also delivered to synchronously, one at a time, inside the single create-and-send request — fine at this school's scale (tens to a couple hundred parents), but a very large `ALL_PARENTS`/`CLASS` `EMAIL` broadcast would hold the DB connection open for as long as every SMTP round-trip takes; a real fix means an async queue/worker, out of this phase's scope.

**Note on 3.3**: `GradeClassification` (xếp loại học lực: Tốt/Giỏi/Khá/Đạt/Trung bình/Yếu/Chưa đạt/Kém) exists as vocabulary and the DTOs carry a `classification` field, but the actual TT22/58 threshold logic is **not implemented** — it's deliberately deferred pending confirmation from someone with education-domain expertise on the exact score cutoffs and the môn Toán/Ngữ văn condition. The field is always `null` (omitted from JSON) until that's implemented.

**Note on 3.4**: the class/semester roster (`GET /v1/conduct/class/{classId}/semester/{semesterId}`) matches students to a class via the same (deprecated) `className`/`section` string pair `SchoolClassService.getStudentsInClass` already uses codebase-wide — not scoped by academic year, so a reused className/section across years could over-match. Pre-existing limitation of that roster convention (Phase 1-2), not new to this endpoint; a real fix means wiring up `Student.currentClass` (the FK meant to replace it) everywhere roster membership is checked, which no code path currently maintains on write.

**Note on 3.5**: `PromotionThresholdConfig`'s `minSubjectAverage` is compared against the *lowest* of a student's per-subject điểm TB năm (not an invented cross-subject blended average) precisely because xếp loại học lực isn't computed yet (see 3.3 note) — TT22/58 define promotion criteria per-subject-plus-conditions, not one overall number, so this is a configurable approximation of the real criteria, not the official calculation. `previewClassPromotions` does one grade/conduct/attendance lookup per roster student (no batching) — fine for a class-sized roster, would need work to scale to a whole-school report.
