# KẾ HOẠCH CHI TIẾT TRIỂN KHAI HỆ THỐNG QUẢN LÝ TRƯỜNG THCS-THPT (thcsthptTS)

*Phiên bản 3.1 — cập nhật theo yêu cầu: (1) chi tiết hoá từng giai đoạn xuống mức có thể giao việc trực tiếp cho dev (ngày/tuần, file cụ thể, endpoint cụ thể, field entity cụ thể); (2) đổi hạ tầng CSDL từ PostgreSQL (Aiven Cloud) sang **MySQL chạy local** (đã cài đặt và cấu hình sẵn), xoá hoàn toàn cấu hình PostgreSQL/Aiven khỏi repo; (3) đã xác nhận dữ liệu trên Aiven chỉ là dữ liệu test — bỏ mọi bước export/backup dữ liệu thật.*

*Mô hình vẫn giữ 2 track song song: Track Backend và Track Frontend (Tailwind CSS + shadcn/ui), khởi động cùng lúc từ Tuần 1. Đây vẫn là tài liệu kế hoạch để duyệt trước khi đụng vào code thật.*

---

## NGUYÊN TẮC XUYÊN SUỐT: Bám sát hệ thống giáo dục THCS-THPT Việt Nam

Nguyên tắc này áp dụng cho **mọi giai đoạn**, không riêng Giai đoạn 3 — kể cả các module tưởng chừng "chung chung" (quản lý lớp, học sinh, nhân sự...) cũng phải thiết kế theo đúng cách một trường THCS/THPT Việt Nam thật sự vận hành, không theo mô hình trường học chung chung/quốc tế:

- **Cấp học & khối lớp**: THCS (lớp 6-9), THPT (lớp 10-12); một trường thực tế thường chỉ dạy một cấp (THCS *hoặc* THPT) hoặc liên cấp — field/entity liên quan tới khối lớp (`gradeLevel`) phải giới hạn đúng phạm vi 6-12, không để tự do.
- **Năm học & học kỳ**: năm học chạy khoảng tháng 9 → tháng 5 năm sau (không trùng năm dương lịch), chia **Học kỳ 1 / Học kỳ 2** (không phải "Fall/Spring" hay quarter/semester kiểu Mỹ).
- **Hệ thống điểm & xếp loại học lực**: theo đúng **Thông tư 22/2021/TT-BGDĐT** (khối đã áp dụng chương trình GDPT 2018: xếp loại Tốt/Khá/Đạt/Chưa đạt) hoặc **Thông tư 58** (khối chưa chuyển đổi: Giỏi/Khá/Trung bình/Yếu/Kém) — hệ số điểm miệng/15 phút/1 tiết/giữa kỳ/cuối kỳ, cách tính điểm trung bình môn học kỳ/cả năm phải đúng công thức quy định, **không tự sáng tạo công thức khác**.
- **Hạnh kiểm/rèn luyện**: luôn đi kèm song song với học lực (Tốt/Khá/Trung bình/Yếu), đây là phần bắt buộc trong học bạ Việt Nam, không phải tính năng phụ.
- **Cơ cấu lớp/môn học**: sĩ số lớp, giáo viên chủ nhiệm (GVCN), phân công giảng dạy theo môn, thời khoá biểu theo tiết (thường 4-5 tiết/buổi) — đúng cách vận hành lớp học Việt Nam (một lớp cố định, nhiều giáo viên bộ môn dạy luân phiên theo tiết, không phải học sinh tự chọn phòng học như đại học).
- **Vai trò nhân sự**: Hiệu trưởng, Hiệu phó, GVCN, Giáo viên bộ môn, Tổng phụ trách Đội (nếu có), Thư viện, Kế toán, Y tế học đường, Bảo vệ — đặt tên field/enum theo đúng vai trò thực tế trong trường Việt Nam.
- **Học phí & khoản thu**: các khoản thu đặc trưng Việt Nam (học phí, bảo hiểm y tế học sinh (BHYT), bán trú, đồng phục, quỹ phụ huynh/quỹ lớp, dạy thêm-học thêm nếu trường có tổ chức) — không chỉ "tuition fee" chung chung kiểu Mỹ.
- **Phụ huynh & sổ liên lạc**: mô hình liên lạc nhà trường-phụ huynh Việt Nam qua **sổ liên lạc điện tử** (thông báo điểm/điểm danh/học phí qua app/SMS/Zalo — Zalo là kênh phổ biến nhất, không phải email như phương Tây).
- **Tuyển sinh, chuyển trường, xét lên lớp/ở lại/tốt nghiệp**: đúng quy trình hành chính giáo dục Việt Nam (hồ sơ nhập học, xét tuyển đầu cấp, quyết định lên lớp/ở lại theo ngưỡng điểm + hạnh kiểm + số buổi nghỉ theo quy định).
- **Ngôn ngữ & thuật ngữ**: UI, tên field có ý nghĩa nghiệp vụ (không phải kỹ thuật thuần tuý), thông báo lỗi hướng tới người dùng nên dùng tiếng Việt và đúng thuật ngữ ngành giáo dục (học kỳ, hạnh kiểm, học bạ, khối, môn học...), tránh dịch word-by-word từ mô hình trường học nước ngoài.

**Khi thiết kế bất kỳ entity/API/UI mới nào**: nếu có điểm mơ hồ về nghiệp vụ (ví dụ ngưỡng điểm xếp loại, quy định nghỉ học tối đa), nên hỏi lại người có chuyên môn giáo dục thay vì tự suy đoán theo mô hình nước ngoài — đã ghi trong mục "Rủi ro nghiệp vụ điểm số" cuối tài liệu.

---

## 0. THAY ĐỔI HẠ TẦNG CSDL: PostgreSQL (Aiven Cloud) → MySQL (Local)

Đây là việc làm **đầu tiên** trong Track Backend (gộp vào Ngày 1 của Giai đoạn 1), vì mọi thứ sau đó (Flyway baseline, migration dữ liệu Giai đoạn 3) đều phải viết theo đúng cú pháp MySQL ngay từ đầu — đổi muộn sẽ phải viết lại migration.

### 0.1. `backend/pom.xml`
- **Xoá** dependency `org.postgresql:postgresql` (không còn cần).
- **Giữ** dependency `com.mysql:mysql-connector-j` (đã có sẵn, version `8.0.33`).
- Thêm dependency `org.flywaydb:flyway-mysql` (Flyway cần module riêng cho MySQL từ Flyway 7+, khác với module core dùng cho Postgres).

### 0.2. `backend/src/main/resources/application.yml`
- **Xoá hẳn** khối comment "MySQL Configuration (commented out...)" và khối đang active "PostgreSQL Configuration (Aiven Cloud)" — không giữ lại dạng comment, xoá sạch để không còn dấu vết host/thông tin Aiven trong repo.
- Thay bằng cấu hình MySQL local, đọc qua biến môi trường (đồng bộ với việc vá bảo mật mục 1.3):

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:school_management}?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true
    show-sql: false
    open-in-view: false
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false
```

- Lưu ý: Hibernate 6 (dùng trong Spring Boot 3) tự nhận diện dialect MySQL từ driver, khai báo `MySQLDialect` chỉ để tường minh, không dùng `MySQL8Dialect` (đã deprecated).

### 0.3. Database MySQL local — **đã có sẵn, không cần cài mới**

MySQL đã được cài đặt và cấu hình sẵn trên máy — bỏ qua bước cài đặt. Chỉ cần xác nhận:

- Database `school_management` tồn tại và dùng charset `utf8mb4`/collation `utf8mb4_unicode_ci` (bắt buộc `utf8mb4`, không phải `utf8` mặc định của MySQL vốn chỉ hỗ trợ 3-byte, thiếu một số ký tự) để đảm bảo hiển thị đúng dấu tiếng Việt. Nếu database hiện có đang dùng charset khác, đổi lại bằng:
  ```sql
  ALTER DATABASE school_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  ```
- Có sẵn user/password ứng dụng dùng để kết nối (không dùng `root` cho ứng dụng chạy thường ngày) — lấy đúng `host`, `port`, `database`, `username`, `password` đã cấu hình để điền vào `.env` ở mục 0.2 (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`). Nếu chưa có user riêng cho ứng dụng, tạo bằng:
  ```sql
  CREATE USER 'school_app'@'localhost' IDENTIFIED BY '<mật khẩu mạnh>';
  GRANT ALL PRIVILEGES ON school_management.* TO 'school_app'@'localhost';
  FLUSH PRIVILEGES;
  ```

### 0.4. `docker-compose.yml`
Đổi service `postgres` → `mysql`:

```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: school-db
    environment:
      MYSQL_DATABASE: school_management
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
      MYSQL_USER: ${DB_USERNAME}
      MYSQL_PASSWORD: ${DB_PASSWORD}
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - school-network

  backend:
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/school_management?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
    depends_on:
      - mysql
    # ...giữ nguyên phần còn lại

volumes:
  mysql_data:
```

- Xoá biến `JWT_SECRET` hard-code sẵn trong file hiện tại (`mySecretKeyForSchoolManagementSystem2024...`) — chuyển ra `.env` không commit (đồng bộ mục 1.3).

### 0.5. Flyway baseline theo MySQL
- Viết `V1__baseline.sql` bằng cú pháp MySQL: `AUTO_INCREMENT` thay cho `SERIAL`/`IDENTITY`, `ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`, kiểu `DATETIME`/`DATE` chuẩn MySQL, khoá ngoại `FOREIGN KEY ... REFERENCES ...`.
- Dựng schema baseline bằng cách cho Hibernate tạo bảng 1 lần trên DB MySQL trống với `ddl-auto: update` tạm thời, sau đó dump bằng `mysqldump --no-data school_management > V1__baseline.sql`, dọn lại cho sạch, rồi mới chuyển `ddl-auto: validate` vĩnh viễn.

### 0.6. Rà soát `TEST_DATA_CORRECTED.sql`
- File hiện có (~20KB) nhiều khả năng được viết cho Postgres — cần rà soát và chuyển đổi các điểm khác biệt cú pháp phổ biến giữa Postgres và MySQL trước khi dùng lại:
  - `SERIAL`/`BIGSERIAL` → bỏ, dùng `AUTO_INCREMENT` đã khai trong `CREATE TABLE` hoặc để Hibernate tự sinh ID.
  - Định danh có dấu ngoặc kép `"columnName"` (Postgres) → dấu backtick `` `columnName` `` (MySQL) nếu có.
  - `RETURNING` clause (Postgres) không tồn tại ở MySQL — nếu có dùng, đổi cách lấy ID vừa insert.
  - `ON CONFLICT ... DO UPDATE` (Postgres) → `ON DUPLICATE KEY UPDATE` (MySQL) nếu có.
  - Kiểm tra kiểu `BOOLEAN`/`TIMESTAMP` — MySQL dùng `TINYINT(1)`/`DATETIME`, JPA/Hibernate xử lý tương thích 2 chiều nên thường không cần sửa tay, nhưng cần chạy thử insert để chắc chắn.
- *Vì chưa đọc được toàn bộ nội dung file này (giới hạn truy cập lúc lập kế hoạch), bước này nên được xác nhận lại bằng cách chạy thử script trên MySQL local trước khi coi là hoàn tất.*

### 0.7. Dọn dẹp tài liệu
- `README.md`: xoá phần "Database: PostgreSQL (Aiven Cloud)", "Host: school-clinicbooking.c.aivencloud.com..." — đây chính là thông tin bị lộ đã nêu trong báo cáo bảo mật — thay bằng hướng dẫn cài MySQL local + biến môi trường.
- `ARCHITECTURE.md`, `DEVELOPMENT_GUIDE.md`, `QUICKSTART.md`: cập nhật mọi đoạn nhắc tới PostgreSQL/psql thành MySQL/mysql client tương ứng (ví dụ lệnh import test data đổi từ `psql -U avnadmin -h ... -f TEST_DATA_CORRECTED.sql` sang `mysql -u school_app -p school_management < TEST_DATA_CORRECTED.sql`).
- **Sau khi migrate xong và xác nhận ổn định, đóng/xoá hẳn cluster Aiven Cloud cũ** để loại bỏ hoàn toàn bề mặt rò rỉ đã ghi nhận trong báo cáo bảo mật trước đó.

**Tiêu chí hoàn thành (DoD) mục 0**: `mvn spring-boot:run` chạy thành công, kết nối được MySQL local đã có sẵn, Flyway áp baseline không lỗi; không còn bất kỳ tham chiếu nào tới `postgresql`/`aivencloud.com` trong toàn bộ repo (kiểm bằng `grep -ri "postgres\|aivencloud" -r .`).

---

## TRACK BACKEND — CHI TIẾT THEO NGÀY/TUẦN

### GIAI ĐOẠN 1 — VÁ BẢO MẬT + ĐỔI HẠ TẦNG CSDL (5 ngày làm việc)

*(Checklist bên dưới được xác nhận hoàn tất khi rà lại toàn bộ plan sau khi
Giai đoạn 3 xong — không có checkbox nào bị đánh dấu mà chưa kiểm tra trực
tiếp trong code: không còn `System.out.println` lộ mật khẩu, `RegisterRequest`
+ test chặn leo thang quyền khi đăng ký, `JWT_SECRET` không có giá trị mặc
định, `application-dev.yml`/`application-prod.yml`/`.env.example` đều tồn
tại, `@Valid` trên các controller nhạy cảm, `GlobalExceptionHandler` +
test không rò rỉ nội dung exception, `LoginPage` không còn tài khoản mẫu.)*

**Ngày 1 — Hạ tầng CSDL + dọn log lộ mật khẩu**
- [x] Thực hiện toàn bộ mục 0 (đổi CSDL sang MySQL local) ở trên.
- [x] Xoá các dòng `System.out.println` trong `AuthenticationService.login()` (dòng in username, password hash đầy đủ, fresh hash so sánh, kết quả match) — thay bằng `private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);` và chỉ log ở mức DEBUG các thông tin không nhạy cảm (username, kết quả thành công/thất bại, **không log password/hash**).
- [x] Chạy thử `mvn spring-boot:run`, xác nhận app khởi động với DB MySQL local, đăng nhập thử với `TEST_DATA_CORRECTED.sql` đã import.

**Ngày 2 — Chặn leo thang đặc quyền khi đăng ký**
- [x] Tạo `dto/RegisterRequest.java`:
  ```java
  public class RegisterRequest {
      @NotBlank @Size(min = 4, max = 50) private String username;
      @NotBlank @Email private String email;
      @NotBlank @Size(min = 8) private String password;
      @NotBlank private String firstName;
      @NotBlank private String lastName;
      private String phoneNumber; // optional
  }
  ```
- [x] Sửa `AuthController.register(@Valid @RequestBody RegisterRequest request)` — không nhận `User` trực tiếp nữa.
- [x] Sửa `AuthenticationService.register(RegisterRequest request)` — luôn `user.setRole(Role.STUDENT)`, build `User` entity nội bộ từ DTO, không cho client set `role`/`enabled`/`id`.
- [x] Tạo endpoint mới `POST /v1/users` (trong `UserController` mới hoặc gộp vào `StaffController`) yêu cầu `@PreAuthorize("hasRole('ADMIN')")` để ADMIN tạo tài khoản với role tuỳ chọn (ADMIN/TEACHER/LIBRARIAN/ACCOUNTANT/PARENT).
- [x] Viết unit test: đăng ký qua `/v1/auth/register` với payload chứa `"role": "ADMIN"` → assert tài khoản tạo ra có `role == STUDENT`.

**Ngày 3 — Externalize secrets + Spring profile**
- [x] `application.yml` (phần chung) dùng `${JWT_SECRET}` bắt buộc (không có giá trị mặc định) — app phải fail-fast khi thiếu biến này thay vì chạy với secret yếu.
- [x] Tạo `application-dev.yml` (trỏ MySQL local, log level DEBUG) và `application-prod.yml` (trỏ DB thật qua env var, log level INFO, tắt `show-sql`).
- [x] Tạo `.env.example` liệt kê đủ biến cần thiết (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`, `JWT_REFRESH_EXPIRATION`).
- [x] **Sinh JWT secret mới** (ví dụ `openssl rand -base64 64`), cập nhật vào `.env` local (không commit), vô hiệu hoá secret cũ trong lịch sử git.
- [x] Thêm `application-local.yml`, `.env` vào `backend/.gitignore`.

**Ngày 4 — Validation + ẩn lỗi hệ thống**
- [x] Rà toàn bộ Controller nhận `@RequestBody` entity trực tiếp (`Staff`, `Student`, `Fee`, `Grade`, `LibraryBook`, `Attendance`) — với các endpoint tạo/sửa, thêm `@Valid` và annotation validation tương ứng (`@NotBlank`, `@Email`, `@Positive`, `@PastOrPresent` cho ngày sinh...). Nếu thời gian hạn chế, ưu tiên validate `Student`, `Staff`, `Fee` trước (dữ liệu nhạy/tài chính).
- [x] `GlobalExceptionHandler.handleGeneralException`: đổi message trả về client thành `"Đã có lỗi xảy ra, vui lòng thử lại sau."`, log đầy đủ `ex` (kèm stack trace) bằng `log.error(...)` ở server.
- [x] Viết test cho `GlobalExceptionHandler` xác nhận response 500 không chứa nội dung exception gốc.

**Ngày 5 — Swagger, dọn frontend, review tổng thể**
- [x] Bổ sung `@Schema`/`@Operation` mô tả đầy đủ cho các controller còn thiếu, đảm bảo Swagger UI (`/api/swagger-ui.html`) phản ánh đúng request/response mới nhất (đặc biệt `RegisterRequest` mới) — đây là hợp đồng API để Track Frontend dựa vào.
- [x] Frontend: `LoginPage.js` — bỏ `useState('admin')`/`useState('Test@123')` mặc định, bỏ khối "Test Credentials" hiển thị công khai.
- [x] Chạy lại toàn bộ checklist Giai đoạn 1, review chéo (nếu có 2 người), mở PR `feature/security-hardening` + `feature/mysql-migration`.

---

### GIAI ĐOẠN 2 — HOÀN THIỆN BACKEND (2-3 tuần)

**Tuần 1 — Module Quản lý lớp học (`SchoolClass`)**

| File cần tạo | Nội dung |
|---|---|
| `dto/SchoolClassDTO.java` | id, className, section, capacity, classTeacherId, classTeacherName, academicYear, roomNumber, studentCount (tính động) |
| `repository/SchoolClassRepository.java` | `findByAcademicYear`, `findByClassTeacher`, kế thừa `JpaRepository<SchoolClass, Long>` |
| `service/SchoolClassService.java` | `createClass`, `updateClass`, `getClassById`, `getAllClasses`, `getClassesByAcademicYear`, `assignClassTeacher`, `getStudentsInClass` (join `StudentRepository.findByClassNameAndSection`), `deleteClass` (chặn xoá nếu còn học sinh) |
| `controller/SchoolClassController.java` | xem bảng endpoint dưới |

**Danh sách endpoint `/v1/classes`:**

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| POST | `/v1/classes` | ADMIN, PRINCIPAL | Tạo lớp mới |
| PUT | `/v1/classes/{id}` | ADMIN, PRINCIPAL | Sửa thông tin lớp |
| GET | `/v1/classes/{id}` | ADMIN, PRINCIPAL, TEACHER | Xem chi tiết 1 lớp |
| GET | `/v1/classes` | ADMIN, PRINCIPAL, TEACHER | Danh sách lớp (có phân trang, xem Tuần 2) |
| GET | `/v1/classes/year/{academicYear}` | ADMIN, PRINCIPAL | Danh sách lớp theo năm học |
| GET | `/v1/classes/{id}/students` | ADMIN, PRINCIPAL, TEACHER | Danh sách học sinh trong lớp |
| PUT | `/v1/classes/{id}/teacher/{staffId}` | ADMIN, PRINCIPAL | Gán/đổi giáo viên chủ nhiệm |
| DELETE | `/v1/classes/{id}` | ADMIN, PRINCIPAL | Xoá lớp (chặn nếu còn học sinh) |

**Tuần 2 — Dashboard stats + Phân trang**

- Tạo `controller/DashboardController.java` + `service/DashboardService.java`:
  - `GET /v1/dashboard/stats` trả về: `activeStudentCount`, `activeStaffCount`, `averageAttendanceRate` (30 ngày gần nhất, tính từ `AttendanceService`), `totalOutstandingFees` (tổng `remainingAmount` các fee chưa `PAID`/`EXEMPTED`), `booksBorrowedCount` (từ `BookTransactionRepository`).
- Thêm phân trang cho các endpoint danh sách hiện có, giữ tương thích ngược bằng param optional `page`/`size` (mặc định trả toàn bộ nếu không truyền):
  - `StudentController.getAllStudents(@RequestParam(required=false) Integer page, @RequestParam(required=false) Integer size)`
  - `StaffController.getAllStaff(...)` — tương tự
  - `LibraryController.getAllBooks(...)` — tương tự
  - `SchoolClassController.getAllClasses(...)` — áp dụng luôn từ đầu vì mới tạo Tuần 1
  - `GradeController.getGradesByAcademicYear(...)`, `FeeController.getFeesByAcademicYear(...)` — áp dụng khi danh sách theo năm học có thể dài

**Tuần 3 — Refresh token & rà soát**
- Kiểm tra/refactor `AuthController.refreshToken` trả đúng format `AuthResponse` nhất quán với `/login`.
- Viết integration test cho `SchoolClassController` (tạo/sửa/xoá/gán giáo viên).
- Review code, merge `feature/backend-phase2`.

---

### GIAI ĐOẠN 3 — MODULE ĐẶC THÙ GIÁO DỤC VIỆT NAM (chi tiết theo từng module)

#### 3.1. Năm học / Học kỳ / Môn học *(làm trước, ~1 tuần)*

**Entity mới:**

`AcademicYear`: `id`, `name` (VD "2025-2026"), `startDate`, `endDate`, `status` (enum `ACTIVE`/`CLOSED`).
`Semester`: `id`, `academicYear_FK`, `name` (enum `HK1`/`HK2`), `startDate`, `endDate`.
`Subject`: `id`, `code` (VD "TOAN"), `name` (VD "Toán học"), `gradeLevels` (chuỗi CSV khối áp dụng, VD "6,7,8,9" hoặc dùng bảng con `SubjectGradeLevel` nếu cần chuẩn hoá), `category` (enum `BAT_BUOC`/`TU_CHON`).

**Thay đổi entity hiện có:** `SchoolClass.academicYear` (String) → `academicYear_FK`; thêm `SchoolClass.gradeLevel` (Integer 6-12). `Student` thêm `currentClass_FK` (giữ `className`/`section` cũ song song, đánh dấu `@Deprecated`).

**Migration dữ liệu** (script Flyway riêng, xem mục migration chi tiết cuối tài liệu): sinh `SchoolClass` từ dữ liệu `className`+`section` hiện có, gán `currentClass_FK` cho từng `Student`.

**Endpoint:** `/v1/academic-years` (CRUD + `PUT /v1/academic-years/{id}/close`), `/v1/semesters`, `/v1/subjects` (CRUD, quyền ADMIN/PRINCIPAL).

**Ước lượng**: 4 ngày backend (entity + migration + API) + 1 ngày test.

#### 3.2. Phân công giảng dạy & Thời khoá biểu *(~1-1.5 tuần)*

**Entity mới:** `TeachingAssignment` (`id`, `schoolClass_FK`, `subject_FK`, `teacher_FK`, `semester_FK`); `TimetableSlot` (`id`, `teachingAssignment_FK`, `dayOfWeek` 2-7, `period` 1-10, `room`).

**Endpoint:** `POST/GET/PUT/DELETE /v1/teaching-assignments`, `GET /v1/timetable/class/{classId}`, `GET /v1/timetable/teacher/{teacherId}`, `POST /v1/timetable/slots` (kiểm tra trùng lịch giáo viên/phòng khi tạo).

**Ước lượng**: 5 ngày backend (bao gồm logic kiểm tra trùng lịch) + 3-4 ngày frontend (lưới thời khoá biểu).

#### 3.3. Hệ thống điểm theo Thông tư 22/2021 (và tương thích TT58) *(~1.5-2 tuần)*

**Entity:** đổi `Grade` → `GradeRecord` (`id`, `student_FK`, `subject_FK`, `semester_FK`, `componentType` enum `MIENG`/`MUOI_LAM_PHUT`/`MOT_TIET`/`GIUA_KY`/`CUOI_KY`, `score` (0-10, `@DecimalMin/@DecimalMax`), `teacher_FK`, `remarks`).
`GradeComponentConfig`: `id`, `componentType`, `weight` (hệ số 1/2/3), `appliesFrom` (năm học bắt đầu áp dụng, để đổi quy định qua từng năm mà không sửa code).
`GradeClassification` (enum, cấu hình theo `academicYear`/`gradeLevel`: TT22 dùng `TOT`/`KHA`/`DAT`/`CHUA_DAT`; TT58 dùng `GIOI`/`KHA`/`TRUNG_BINH`/`YEU`/`KEM`).

**Logic tính toán (service, không cần lưu bảng riêng)**:
- Điểm TB môn học kỳ = Σ(score × weight) / Σ(weight).
- Điểm TB môn cả năm = (ĐTB HK1 + ĐTB HK2 × 2) / 3.
- Xếp loại học lực dựa trên điểm TB các môn + điều kiện môn Toán/Ngữ văn theo đúng ngưỡng quy định (cần người có chuyên môn xác nhận bảng ngưỡng cụ thể trước khi code).

**Endpoint:** `POST/PUT/DELETE /v1/grades`, `GET /v1/grades/student/{id}/semester/{semesterId}`, `GET /v1/grades/student/{id}/summary?semester=...` (trả điểm TB + xếp loại), `GET /v1/grade-config` (ADMIN cấu hình hệ số/ngưỡng).

**Ước lượng**: 6 ngày backend (bao gồm unit test đối chiếu tay công thức) + 4-5 ngày frontend (bảng nhập điểm + bảng tổng hợp).

#### 3.4. Hạnh kiểm / Rèn luyện *(~3-4 ngày)*

**Entity:** `ConductRecord` (`id`, `student_FK`, `semester_FK`, `rating` enum `TOT`/`KHA`/`TRUNG_BINH`/`YEU`, `remarks`, `evaluatedBy_FK`).

**Endpoint:** `POST/PUT /v1/conduct`, `GET /v1/conduct/student/{id}`, `GET /v1/conduct/class/{classId}/semester/{semesterId}` (bảng đánh giá hàng loạt cho GVCN) — quyền TEACHER chỉ ghi được cho lớp mình chủ nhiệm (kiểm tra `classTeacher_FK == currentUser`).

#### 3.5. Xét lên lớp / Ở lại / Tốt nghiệp *(~1 tuần)*

**Entity:** `PromotionRecord` (`id`, `student_FK`, `academicYear_FK`, `academicResultSnapshot` JSON hoặc các field rời (điểm TB, xếp loại), `conductResult`, `attendanceRate`, `decision` enum `LEN_LOP`/`O_LAI`/`TOT_NGHIEP`/`RA_TRUONG`, `decisionDate`, `decidedBy_FK`).

**Endpoint:** `GET /v1/promotions/class/{classId}/preview?academicYear=...` (tính đề xuất tự động dựa ngưỡng cấu hình, chưa lưu), `POST /v1/promotions/confirm` (lưu quyết định cuối, hỗ trợ ghi đè hàng loạt).

**Phụ thuộc**: cần 3.3 (điểm) và 3.4 (hạnh kiểm) xong trước.

#### 3.6. Phụ huynh – Học sinh & Sổ liên lạc điện tử *(~2-3 tuần, tốn công nhất)*

**Entity:** `ParentStudentRelation` (`id`, `parent_FK`, `student_FK`, `relationship` enum `CHA`/`ME`/`NGUOI_GIAM_HO`, `isPrimaryContact`); `Notification` (`id`, `title`, `content`, `targetType` enum `CLASS`/`STUDENT`/`ALL_PARENTS`/`STAFF`, `targetId`, `channel` enum `APP`/`EMAIL`/`SMS`/`ZALO`, `createdBy_FK`, `sentAt`, `status`); `NotificationRecipient` (`id`, `notification_FK`, `recipient_FK`, `readAt`).

**Thiết kế `NotificationSender` (interface/strategy)**:
```java
public interface NotificationSender {
    boolean send(String recipientContact, String title, String content);
    NotificationChannel getChannel();
}
// Implementations: EmailNotificationSender (SMTP), SmsNotificationSender (eSMS/FPT SMS), ZaloOaNotificationSender
```

**Endpoint:** `POST/DELETE /v1/parents/{parentId}/children/{studentId}`, `GET /v1/parents/{parentId}/children`, `POST /v1/notifications` (tạo + gửi), `GET /v1/notifications/my` (cho PARENT/STAFF xem thông báo của mình). Các endpoint điểm/điểm danh/học phí hiện có cần thêm `@PreAuthorize` cho `hasRole('PARENT')` kèm kiểm tra quan hệ `ParentStudentRelation` ở tầng service (không cho phụ huynh xem con người khác).

**Quyết định cần chốt trước khi code**: chọn nhà cung cấp SMS (eSMS/FPT SMS) và có đăng ký Zalo OA hay chưa — nếu chưa có ngân sách, có thể làm trước kênh `APP` (thông báo trong ứng dụng) và `EMAIL` (SMTP), để `SMS`/`ZALO` làm sau khi có quyết định ngân sách.

#### 3.7. Tuyển sinh đầu cấp *(~1-1.5 tuần)*

**Entity:** `AdmissionApplication` (`id`, `applicantName`, `dateOfBirth`, `contactPhone`, `desiredGradeLevel`, `priorSchool`, `status` enum `PENDING`/`REVIEWING`/`APPROVED`/`REJECTED`, `submittedAt`, `reviewedBy_FK`, `note`).

**Endpoint:** `POST /v1/admissions` (công khai, không cần đăng nhập, có rate-limit chống spam), `GET /v1/admissions` (ADMIN), `PUT /v1/admissions/{id}/status`, `POST /v1/admissions/{id}/approve-and-create` (tự động tạo `User` + `Student` từ hồ sơ đã duyệt, tránh nhập tay 2 lần).

#### 3.8. Xuất báo cáo PDF/Excel *(~1-1.5 tuần)*

- Thêm dependency `com.itextpdf:itext7-core` (hoặc `org.openpdf:openpdf` bản miễn phí) và `org.apache.poi:poi-ooxml`.
- `GET /v1/reports/student/{id}/transcript?academicYear=...` → PDF học bạ/bảng điểm.
- `GET /v1/reports/class/{id}/attendance?from=...&to=...` → Excel điểm danh.
- `GET /v1/reports/fees/receipt/{feeId}` → PDF biên lai.
- Ưu tiên thứ tự: bảng điểm/học bạ trước (nhu cầu mỗi kỳ), biên lai sau.

#### 3.9. Hạ tầng dùng chung *(~1 tuần)*

- `DocumentAttachment` (`id`, `ownerType`, `ownerId`, `fileName`, `fileUrl`, `uploadedBy_FK`, `uploadedAt`, `fileType`) — lưu file lên filesystem local hoặc MinIO (S3-compatible tự host, phù hợp môi trường không dùng cloud).
- `AuditLog` (`id`, `actor_FK`, `action`, `entityType`, `entityId`, `timestamp`, `detailJson`) — ghi qua Spring AOP `@Around` bọc các method `create/update/delete` trong service, hoặc gọi thủ công tại các thao tác nhạy cảm (sửa điểm, xoá học sinh).
- `POST /v1/auth/forgot-password` (gửi email link reset, token hết hạn 15 phút), `POST /v1/auth/reset-password`.

---

## TRACK FRONTEND — CHI TIẾT THEO NGÀY (Tailwind CSS + shadcn/ui, 6 tuần)

### Cấu trúc thư mục đề xuất

```
frontend/src/
  components/ui/          # shadcn/ui components (Button, Dialog, Table, Form...)
  components/shared/       # DataTable, DatePicker, PageHeader, StatCard... (tự xây, dùng lại toàn app)
  components/layout/       # Navbar, Sidebar, AppShell
  features/
    auth/                  # LoginPage, hooks (useAuth), authService
    dashboard/
    students/
    staff/
    classes/
    library/
    attendance/
    grades/
    fees/
    academic-config/       # Giai đoạn 3.1: năm học/học kỳ/môn học
    timetable/             # Giai đoạn 3.2
    parents/                # Giai đoạn 3.6
  lib/                      # axios instance, query client, utils, zod schemas
  stores/                   # zustand stores (ui state: sidebar, theme, currentUser)
```

### Tuần 1 — Nền tảng

- **Ngày 1-2**: Migrate CRA → Vite (`npm create vite@latest` cấu trúc tương đương, chuyển `.env` `REACT_APP_*` → `VITE_*`, sửa `import.meta.env` thay `process.env`, kiểm tra build chạy được).
- **Ngày 3**: Cài Tailwind CSS, viết `tailwind.config.js` với theme token (màu chính, bán kính bo góc, font). Cài + init shadcn/ui (`npx shadcn@latest init`), cài font Be Vietnam Pro.
- **Ngày 4**: Dựng `AppShell` (Navbar + Sidebar mới) dùng shadcn `Sheet` cho mobile.
- **Ngày 5**: Setup dark mode (Tailwind class strategy + `ThemeProvider` context + toggle), kiểm tra toàn bộ layout ở cả 2 theme.

### Tuần 2 — Bộ component dùng chung + Auth

- **Ngày 1-2**: Xây `components/shared/DataTable.tsx` (TanStack Table v8 + shadcn Table): props `columns`, `data`, `pagination`, `onPageChange`, hỗ trợ sort/filter cột.
- **Ngày 3**: Xây `DatePicker`/`DateRangePicker` (react-day-picker + shadcn Popover), dùng `date-fns` sẵn có.
- **Ngày 4**: Xây `Form` wrapper chuẩn (React Hook Form + Zod resolver + shadcn Form components: `FormField`, `FormMessage`...).
- **Ngày 5**: Tích hợp `sonner` (toast), tích hợp TanStack Query (`QueryClientProvider` ở root), rebuild `LoginPage` (bỏ tài khoản demo, thiết kế mới).

### Tuần 3 — Dashboard + Staff/Student Management

- **Ngày 1-2**: Dashboard: stat card mới (Tailwind), tích hợp Recharts cho biểu đồ chuyên cần/thu học phí, nối `GET /v1/dashboard/stats` thật.
- **Ngày 3-4**: `StaffManagement` — chuyển bảng sang `DataTable`, `Dialog` thay Modal, hoàn thiện form thêm/sửa nối `staffService` thật.
- **Ngày 5**: `StudentManagement` — tương tự StaffManagement.

### Tuần 4 — Hoàn thiện 4 module còn placeholder

- **Ngày 1**: `LibraryManagement` — DataTable sách + tìm kiếm, Dialog mượn/trả.
- **Ngày 2**: `AttendanceManagement` — chọn lớp/ngày, điểm danh hàng loạt (checkbox list), xem % chuyên cần.
- **Ngày 3-4**: `GradeManagement` — bảng nhập điểm theo lớp/môn (dùng model điểm hiện tại trước, refactor khi Track Backend 3.3 xong).
- **Ngày 5**: `FeeManagement` — danh sách khoản thu, form thanh toán, xem công nợ.

### Tuần 5 — Trang mới + Polish

- **Ngày 1-2**: Trang "Quản lý lớp học" (nối `/v1/classes`).
- **Ngày 3**: Rà soát responsive/mobile toàn bộ.
- **Ngày 4**: Rà soát accessibility (contrast, keyboard nav, `aria-label`, focus visible).
- **Ngày 5**: Thay spinner toàn trang bằng skeleton loading (`components/shared/Skeleton`).

### Tuần 6 — Dọn dẹp & QA

- **Ngày 1**: Gỡ `bootstrap`, `react-bootstrap` khỏi `package.json`, xoá mọi import CSS liên quan.
- **Ngày 2**: Code-split theo route (`React.lazy` + `Suspense`), kiểm tra bundle size (`vite build --report` hoặc `rollup-plugin-visualizer`).
- **Ngày 3-4**: Visual QA toàn bộ, so sánh checklist thiết kế, test trên nhiều kích thước màn hình.
- **Ngày 5**: Viết test Vitest + React Testing Library cho `DataTable`, `Form`, `DatePicker`; merge `feature/ui-redesign-tailwind`.

---

## LỊCH TRÌNH SONG SONG TỔNG HỢP (cập nhật)

| Tuần | Track Backend | Track Frontend | Điểm đồng bộ |
|---|---|---|---|
| 1 | Giai đoạn 1: đổi CSDL sang MySQL (mục 0) + vá bảo mật theo 5 ngày | Nền tảng: Vite, Tailwind, shadcn/ui, layout, dark mode | Frontend cần `RegisterRequest` mới ổn định trước khi rebuild LoginPage ở Tuần 2 |
| 2 | Giai đoạn 2 Tuần 1: `SchoolClass` module | Bộ component dùng chung + rebuild LoginPage + React Query | Backend cần `/v1/auth/*` mới sẵn sàng |
| 3 | Giai đoạn 2 Tuần 2: dashboard stats + phân trang | Dashboard (biểu đồ thật) + Staff/Student Management | Backend cần `/v1/dashboard/stats` đúng hạn |
| 4 | Giai đoạn 2 Tuần 3: refresh token; bắt đầu Giai đoạn 3.1 | 4 module placeholder (Library/Attendance/Grade/Fee) — không phụ thuộc | Không có phụ thuộc chặn |
| 5 | Tiếp tục 3.1 (Năm học/Học kỳ/Môn học) | Trang Quản lý lớp học (nối `/v1/classes`) + responsive/accessibility | Backend cần `/v1/classes` sẵn sàng (đã xong Tuần 2) |
| 6 | Bắt đầu 3.2 (thời khoá biểu) | Dọn dẹp, gỡ Bootstrap, QA, test Vitest | — |
| 7-8 | Hoàn thiện 3.2 | Xây UI thời khoá biểu (dùng `DataTable`/lưới có sẵn) | Làm cặp cùng module |
| 9-10 | 3.3 Hệ thống điểm TT22/58 | UI bảng nhập điểm + bảng tổng hợp | Làm cặp cùng module, **cần người chuyên môn giáo dục duyệt công thức trước khi release** |
| 11 | 3.4 Hạnh kiểm | UI đánh giá hạnh kiểm | Làm cặp cùng module |
| 12 | 3.5 Xét lên lớp | UI trang xét lên lớp cuối năm | Cần 3.3 + 3.4 xong trước |
| 13-15 | 3.6 Phụ huynh & sổ liên lạc | UI dashboard phụ huynh + trang gửi thông báo | Cần quyết định ngân sách SMS/Zalo trước Tuần 13 |
| 16-17 | 3.7 Tuyển sinh đầu cấp | UI form nộp hồ sơ + duyệt hồ sơ | Làm cặp cùng module |
| 18-19 | 3.8 Xuất báo cáo PDF/Excel | Nút tải xuống trên các trang liên quan | Chủ yếu backend |
| 20 | 3.9 Audit log, quên mật khẩu, upload tài liệu | UI trang quên mật khẩu + upload | Làm cặp cùng module |

---

## KẾ HOẠCH MIGRATION DỮ LIỆU

1. **`SchoolClass`**: từ `Student.className` + `Student.section` hiện có → sinh danh sách lớp duy nhất, tạo bản ghi `SchoolClass`.
2. **`Student.currentClass_FK`**: đối chiếu chuỗi cũ với `SchoolClass` vừa tạo; ghi log bản ghi không khớp để admin xử lý thủ công.
3. **`Subject`**: quét `Grade.subject` hiện có → sinh danh mục `Subject`.
4. **`AcademicYear`/`Semester`**: quét chuỗi `academicYear` trong `Grade`/`Fee`/`SchoolClass` → tạo bản ghi tương ứng, mặc định "Học kỳ 1", cho phép sửa lại thủ công.
5. Viết dưới dạng Flyway migration riêng (`V5__migrate_class_data.sql` hoặc Java callback), **chạy thử trên bản sao dữ liệu MySQL local trước**.
6. Giữ field cũ (`@Deprecated`) ít nhất 1 phiên bản để rollback an toàn.

---

## KẾ HOẠCH KIỂM THỬ

- **Giai đoạn 1**: test `AuthenticationService` (không cho set role tuỳ ý, không lộ log), test `GlobalExceptionHandler`, test kết nối MySQL local qua Flyway.
- **Giai đoạn 2-3**: test tích hợp cho controller mới; **bắt buộc unit test cho service tính điểm TT22/58** đối chiếu tay, review bởi người hiểu nghiệp vụ giáo dục trước khi release.
- **Track Frontend**: Vitest + React Testing Library cho component dùng chung; Playwright cho vài kịch bản E2E quan trọng sau khi UI ổn định (Tuần 6 trở đi).
- CI chạy song song `mvn test` (dùng MySQL test container qua Testcontainers, không cần Aiven) và `npm run test`, chặn merge nếu fail.

---

## TIMELINE TỔNG THỂ ƯỚC LƯỢNG

- **Tuần 1-6**: nền tảng hoàn chỉnh — CSDL chuyển hẳn sang MySQL local, bảo mật xong, `SchoolClass`/dashboard/phân trang xong, frontend hoàn tất redesign Tailwind + shadcn/ui.
- **Tuần 7-20**: các module Giai đoạn 3 triển khai theo cặp backend/frontend song song như bảng lịch trình ở trên.

**Tổng ước lượng: ~4.5-5 tháng** (20 tuần) với 1 backend dev + 1 frontend dev làm song song toàn thời gian; có thể rút ngắn nếu tăng nhân lực cho các module độc lập (VD: làm song song 3.6 và 3.7 nếu có thêm người).

---

## RỦI RO & PHỤ THUỘC

- ~~Rủi ro chuyển đổi dữ liệu thật Postgres → MySQL~~ — **đã xác nhận: dữ liệu trên Aiven chỉ là dữ liệu test, không có dữ liệu thật**, nên bỏ qua bước export/backup từ Aiven. Chỉ cần rà soát `TEST_DATA_CORRECTED.sql` (mục 0.6) để import thẳng vào MySQL local đã có sẵn.
- **Rủi ro cú pháp SQL khác biệt**: `TEST_DATA_CORRECTED.sql` và mọi migration cần chạy thử thực tế trên MySQL local, không chỉ dựa vào rà soát bằng mắt.
- **Rủi ro đồng bộ API contract**: cần kỷ luật cập nhật Swagger annotation ngay khi đổi API, dùng MSW để frontend không bị block.
- **Rủi ro xung đột CSS Bootstrap/Tailwind**: làm trên nhánh riêng, cutover một lần.
- **Rủi ro bảo trì shadcn/ui**: component nằm trong source code, cần quy ước rõ ràng khi nhiều người sửa.
- **Rủi ro nghiệp vụ điểm số**: công thức tính điểm/xếp loại phải được người có chuyên môn giáo dục xác nhận trước khi dùng thật.
- **Phụ thuộc bên ngoài**: module SMS/Zalo OA (3.6) cần đăng ký tài khoản doanh nghiệp, có chi phí — cần quyết định ngân sách trước Tuần 13.
- **Không có test hiện tại** ở cả 2 phía — ưu tiên viết test cho phần đang sửa, không cần phủ toàn bộ hệ thống cũ ngay từ đầu.

---

*Tài liệu này là kế hoạch chi tiết cho mô hình triển khai song song với CSDL MySQL local (đã cài đặt và cấu hình sẵn). Trước khi bắt đầu code từng phần, chỉ còn cần xác nhận: (1) `TEST_DATA_CORRECTED.sql` chạy được trên MySQL sau khi rà soát cú pháp, (2) bảng ngưỡng xếp loại học lực TT22/58 với người có chuyên môn giáo dục.*
