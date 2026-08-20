# KẾ HOẠCH CHỈNH SỬA & NÂNG CẤP HỆ THỐNG QUẢN LÝ TRƯỜNG THCS-THPT (thcsthptTS)

*Tài liệu kế hoạch triển khai, dựa trên báo cáo phân tích `PROJECT_ANALYSIS_SUMMARY.md` (20/08/2026). Đây là bản kế hoạch để duyệt trước khi đụng vào code thật — chưa thực hiện thay đổi nào trong repo.*

Phạm vi: đầy đủ 3 giai đoạn — (1) vá bảo mật, (2) hoàn thiện phần dở dang, (3) bổ sung tính năng đặc thù giáo dục Việt Nam.

---

## 0. Nguyên tắc thực hiện

- **Không sửa trực tiếp trên nhánh chính đang chạy production/DB thật.** Tạo nhánh `feature/security-hardening`, `feature/vn-academic-model`... riêng, review kỹ trước khi merge, vì DB hiện là Aiven Cloud dùng chung.
- **Đổi tất cả secret đã lộ (JWT secret, mật khẩu DB) trước khi làm bất cứ việc gì khác** — vì phạm vi rò rỉ đã có sẵn trong lịch sử git, việc sửa code sau này không tự động vô hiệu hoá secret cũ.
- **Chuyển từ `ddl-auto: update` sang Flyway migration** ngay từ Giai đoạn 1 để mọi thay đổi schema từ Giai đoạn 3 trở đi (rất nhiều bảng mới) được kiểm soát phiên bản, có thể rollback.
- Mỗi giai đoạn nên có **tiêu chí hoàn thành (Definition of Done)** rõ ràng và **build/test pass** trước khi sang giai đoạn kế.
- Thứ tự bắt buộc: **Giai đoạn 1 → 2 → 3** (không nên làm tính năng mới trên nền tảng chưa vá bảo mật và còn nhiều dữ liệu dạng chuỗi tự do, vì Giai đoạn 3 phụ thuộc nhiều vào việc `SchoolClass`/`Subject` đã có ở Giai đoạn 2).

---

## GIAI ĐOẠN 1 — VÁ BẢO MẬT (ưu tiên cao nhất, nên làm trong 3-5 ngày làm việc)

| # | Việc cần làm | File/khu vực | Cách làm | DoD |
|---|---|---|---|---|
| 1.1 | Xoá toàn bộ `System.out.println` in mật khẩu/hash trong luồng login | `AuthenticationService.login()` | Xoá các dòng debug; nếu cần log, dùng `Logger` (SLF4J) ở mức DEBUG và **không bao giờ log password/hash** | Không còn thông tin nhạy cảm nào xuất hiện trong console/log khi login |
| 1.2 | Chặn client tự đặt `role` khi đăng ký | `AuthController.register`, `AuthenticationService.register`, thêm `RegisterRequest` DTO mới (không có field `role`) | Tạo DTO `RegisterRequest` (username, email, password, firstName, lastName, phoneNumber) thay vì nhận thẳng `User`; service luôn set `Role.STUDENT` (hoặc theo config) khi tự đăng ký công khai; nếu cần tạo tài khoản ADMIN/TEACHER/STAFF thì phải qua endpoint riêng `POST /v1/users` yêu cầu `@PreAuthorize("hasRole('ADMIN')")` | Test: đăng ký với `role: ADMIN` trong payload → tài khoản tạo ra vẫn là STUDENT; tạo tài khoản admin chỉ thành công khi gọi bằng token ADMIN |
| 1.3 | Chuyển JWT secret + cấu hình DB ra biến môi trường | `application.yml` → dùng `${JWT_SECRET}`, `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}` | Thêm `application.yml` mặc định đọc từ env var với fallback rõ ràng là lỗi khi thiếu (không fallback về giá trị mặc định kém an toàn); thêm `.env.example`/README hướng dẫn set biến môi trường; **đổi JWT secret mới + đổi mật khẩu DB trên Aiven ngay** | `application.yml` trong repo không còn chứa secret thật; secret cũ đã bị vô hiệu hoá |
| 1.4 | Tách cấu hình theo môi trường | Thêm `application-dev.yml`, `application-prod.yml`; `application.yml` chỉ chứa cấu hình chung | Dùng Spring profile (`spring.profiles.active`); dev có thể trỏ DB local/docker, prod dùng Aiven qua env var | Chạy được `mvn spring-boot:run -Dspring-boot.run.profiles=dev` với DB local |
| 1.5 | Chuyển `ddl-auto` sang Flyway | `pom.xml` thêm `flyway-core`; `application.yml` set `ddl-auto: validate`; tạo `V1__baseline.sql` từ schema hiện tại | Dump schema hiện tại (Aiven) → viết migration `V1__baseline.sql` khớp với entity hiện có; mọi thay đổi sau này là file `V2__...sql` mới | App khởi động thành công với `ddl-auto: validate`, không có drift giữa entity và schema |
| 1.6 | Không lộ chi tiết lỗi hệ thống ra client | `GlobalExceptionHandler.handleGeneralException` | Trả message chung ("Đã có lỗi xảy ra, vui lòng thử lại") cho client, log `ex` đầy đủ (stack trace) ở server bằng Logger | Response 500 không còn chứa `ex.getMessage()` gốc |
| 1.7 | Bật validation cho toàn bộ input từ client | Tất cả DTO nhận request (`RegisterRequest`, `AuthRequest`, các entity dùng trực tiếp làm request body) | Thêm `@NotBlank`, `@Email`, `@Size`, `@Positive`... + `@Valid` trên tham số controller; cân nhắc tách hẳn Request DTO khỏi Entity cho các endpoint tạo/sửa (Student, Staff, Fee, Grade...) để không cho client set các field hệ thống (`id`, `createdAt`, `status` nội bộ...) | `MethodArgumentNotValidException` được trả về đúng khi thiếu field bắt buộc; không endpoint nào nhận thẳng Entity làm request body nữa |
| 1.8 | Gỡ tài khoản test hard-code trên UI đăng nhập | `frontend/src/pages/LoginPage.js` | Bỏ giá trị mặc định `admin/Test@123` trong `useState`, bỏ khối hiển thị "Test Credentials" (hoặc ẩn sau flag `REACT_APP_SHOW_DEMO_CREDS=true` chỉ bật ở môi trường demo) | Trang login trống, không gợi ý tài khoản thật |
| 1.9 | `.gitignore` + kiểm tra lịch sử git | `backend/.gitignore`, git history | Thêm `application-local.yml`, `.env` vào `.gitignore`; cân nhắc dùng `git filter-repo`/BFG để xoá secret cũ khỏi lịch sử nếu repo sẽ public | Secret không còn truy xuất được kể cả qua lịch sử commit cũ (nếu áp dụng) |

**Deliverable Giai đoạn 1**: PR "security-hardening" build xanh, checklist trên đều pass, secret cũ đã được thu hồi.

---

## GIAI ĐOẠN 2 — HOÀN THIỆN PHẦN DỞ DANG (ước lượng 2-3 tuần)

### 2.1. Backend: hoàn thiện quản lý lớp học

- Tạo `SchoolClassRepository`, `SchoolClassService`, `SchoolClassController` (`/v1/classes`) theo đúng pattern các module hiện có (CRUD + `PreAuthorize` theo vai trò ADMIN/PRINCIPAL, đọc cho TEACHER).
- Bổ sung field còn thiếu nếu cần (sĩ số hiện tại tính động từ số học sinh liên kết, không lưu cứng).
- **Chưa đổi khoá ngoại `Student.className` ngay ở giai đoạn này** (để Giai đoạn 3 xử lý cùng lúc với mô hình Năm học/Học kỳ, tránh migrate 2 lần) — giai đoạn này chỉ thêm API quản lý `SchoolClass` độc lập.

### 2.2. Frontend: kết nối 4 trang còn placeholder với API có sẵn

| Trang | Việc cần làm |
|---|---|
| `LibraryManagement` | Danh sách sách (bảng + tìm kiếm theo tên/tác giả/thể loại), form thêm/sửa sách, nút mượn/trả, hiển thị số bản còn lại; gọi `libraryService` đã có sẵn trong `dataService.js` |
| `AttendanceManagement` | Chọn lớp + ngày → hiển thị danh sách học sinh để điểm danh hàng loạt (dùng API `markClassAttendance` đã có), xem lịch sử điểm danh theo học sinh/khoảng ngày, hiển thị % chuyên cần |
| `GradeManagement` | Chọn lớp/học sinh/môn → nhập điểm, xem bảng điểm, điểm trung bình; **lưu ý**: nên chờ mô hình điểm mới ở Giai đoạn 3 nếu muốn tránh làm lại UI 2 lần — có thể làm bản UI đơn giản trước bằng model hiện tại, refactor sau |
| `FeeManagement` | Danh sách khoản thu theo học sinh/lớp, xử lý thanh toán (form nhập số tiền + phương thức), xem công nợ, in biên lai đơn giản (chưa cần PDF ở giai đoạn này) |

- Hoàn thiện modal "Thêm/Sửa nhân viên" trong `StaffManagement.js` — nối nút Save vào `staffService.create/update` thật, tương tự với `StudentManagement.js` (hiện thiếu nút Add/Edit hoạt động thật).
- Thêm trang "Quản lý lớp học" mới + mục trong `Sidebar.js`.

### 2.3. Dashboard dùng dữ liệu thật

- Backend: thêm endpoint tổng hợp `GET /v1/dashboard/stats` (hoặc dùng lại các endpoint hiện có gọi song song) trả về: tổng số học sinh/nhân sự đang hoạt động, tỉ lệ chuyên cần trung bình (tính từ `AttendanceService`), tổng công nợ chưa thu (`FeeService`), số sách đang mượn.
- Frontend: `Dashboard.js` gọi endpoint thật, bỏ toàn bộ số liệu giả lập (`attendanceRate: 85`, `totalRevenue: 125000`, "Active Users: 24", hoạt động gần đây fake).

### 2.4. Chuẩn hoá API danh sách

- Thêm phân trang (`Pageable`, `Page<T>`) cho các endpoint `getAll...` (staff, student, fee, grade, library book, attendance) — giữ endpoint cũ tương thích ngược bằng cách thêm param `page`/`size` optional, mặc định trả toàn bộ nếu không truyền (tránh phá vỡ frontend đang dùng), rồi migrate dần frontend sang chế độ phân trang thật.

### 2.5. Refresh token tự động ở frontend

- `api.js`: interceptor 401 → thử gọi `/v1/auth/refresh-token` bằng refreshToken lưu trong localStorage trước khi logout; chỉ logout nếu refresh cũng thất bại.

**Deliverable Giai đoạn 2**: 7/7 module có UI hoạt động thật kết nối API thật, dashboard phản ánh dữ liệu thật, không còn trang "Coming soon".

---

## GIAI ĐOẠN 3 — BỔ SUNG TÍNH NĂNG ĐẶC THÙ GIÁO DỤC VIỆT NAM (ước lượng 6-10 tuần, chia theo module độc lập)

Đây là phần thay đổi kiến trúc dữ liệu lớn nhất. Đề xuất chia thành các module con để có thể triển khai/release độc lập.

### 3.1. Nền tảng học vụ: Năm học / Học kỳ / Môn học *(làm trước tiên, các module sau phụ thuộc vào đây)*

**Entity mới:**
- `AcademicYear` (id, name "2025-2026", startDate, endDate, status: ACTIVE/CLOSED)
- `Semester` (id, academicYear_FK, name: "Học kỳ 1"/"Học kỳ 2", startDate, endDate)
- `Subject` (id, code, name, appliesToGradeLevels: danh sách khối 6-12, category: bắt buộc/tự chọn)

**Thay đổi entity hiện có:**
- `SchoolClass`: đổi `academicYear` (String) → `academicYear_FK`; thêm `gradeLevel` (khối 6-12).
- `Student`: giữ `className`/`section` tạm thời cho tương thích, **thêm** `currentClass_FK` (SchoolClass) làm nguồn sự thật chính; viết migration đối chiếu chuỗi cũ sang FK (xem mục 4).
- `Grade.subject` (String) → `subject_FK`; `Grade.academicYear` (String) → liên kết qua `Semester_FK`.
- `Fee.academicYear` (String) → `academicYear_FK`.

**API mới:** `/v1/academic-years`, `/v1/semesters`, `/v1/subjects` (CRUD, quyền ADMIN/PRINCIPAL).

**UI mới:** trang "Cấu hình năm học" cho ADMIN (tạo năm học, mở/đóng học kỳ, danh mục môn học).

### 3.2. Phân công giảng dạy & Thời khoá biểu

**Entity mới:**
- `TeachingAssignment` (phân công giảng dạy): id, schoolClass_FK, subject_FK, teacher_FK (Staff), semester_FK.
- `TimetableSlot` (thời khoá biểu): id, teachingAssignment_FK, dayOfWeek (2-7 theo thứ VN), period (tiết 1-10), room.

**API:** `/v1/teaching-assignments`, `/v1/timetable` (theo lớp, theo giáo viên).

**UI:** trang phân công giảng dạy (ma trận lớp x môn x giáo viên) cho ADMIN/PRINCIPAL; trang xem thời khoá biểu dạng lưới cho TEACHER/STUDENT.

### 3.3. Hệ thống điểm theo Thông tư 22/2021 (và tương thích Thông tư 58 cho khối chưa chuyển đổi)

**Entity mới/đổi tên:**
- Đổi `Grade` → mô hình `GradeRecord`: id, student_FK, subject_FK, semester_FK, componentType (enum: `MIENG`, `MUOI_LAM_PHUT`, `MOT_TIET`, `GIUA_KY`, `CUOI_KY`), score, teacher_FK, remarks.
- `weight` (hệ số) suy ra từ `componentType` qua bảng cấu hình (`GradeComponentConfig`: componentType → weight), để linh hoạt đổi theo quy định từng năm mà không sửa code.
- Bảng tổng hợp/tính toán (có thể là view hoặc tính runtime trong service, không nhất thiết lưu bảng riêng): điểm trung bình môn học kỳ = tổng(score×weight)/tổng(weight); điểm trung bình môn cả năm = (ĐTB HK1 + ĐTB HK2×2)/3 (theo quy định hiện hành); điểm trung bình các môn.
- `GradeClassification` enum theo chuẩn đang áp dụng (cấu hình được: TT22 dùng Tốt/Khá/Đạt/Chưa đạt, TT58 dùng Giỏi/Khá/TB/Yếu/Kém) — service tính xếp loại dựa trên cấu hình chọn theo khối/năm học.

**API:** `/v1/grades` viết lại theo model mới (giữ endpoint cũ tương thích trong giai đoạn chuyển tiếp nếu cần); thêm `/v1/grades/student/{id}/summary?semester=...` trả điểm trung bình + xếp loại.

**UI:** bảng nhập điểm theo lớp/môn/loại điểm cho giáo viên bộ môn; bảng điểm tổng hợp theo học kỳ/năm cho học sinh & phụ huynh.

### 3.4. Hạnh kiểm / Rèn luyện

**Entity mới:** `ConductRecord`: id, student_FK, semester_FK, rating (TOT/KHA/TB/YEU), remarks, evaluatedBy_FK (GVCN).

**API:** `/v1/conduct` (CRUD, quyền TEACHER chỉ cho lớp mình chủ nhiệm, ADMIN toàn quyền).

**UI:** form đánh giá hạnh kiểm cuối kỳ cho GVCN, hiển thị trong bảng điểm/học bạ của học sinh.

### 3.5. Xét lên lớp / Ở lại / Tốt nghiệp

**Entity mới:** `PromotionRecord`: id, student_FK, academicYear_FK, academicResult (snapshot: điểm TB, xếp loại), conductResult, attendanceRate, decision (`LEN_LOP`/`O_LAI`/`TOT_NGHIEP`/`RA_TRUONG`), decisionDate, decidedBy.

**API:** `/v1/promotions` — endpoint tính toán tự động đề xuất (dựa trên ngưỡng cấu hình: điểm TB tối thiểu, số buổi nghỉ tối đa) + cho phép ADMIN/PRINCIPAL duyệt/ghi đè quyết định cuối.

**UI:** trang "Xét lên lớp cuối năm" — danh sách học sinh theo lớp kèm đề xuất tự động, cho phép chỉnh quyết định hàng loạt.

### 3.6. Liên kết Phụ huynh – Học sinh & Sổ liên lạc điện tử

**Entity mới:**
- `ParentStudentRelation`: id, parent_FK (User có Role.PARENT), student_FK, relationship (Cha/Mẹ/Người giám hộ), isPrimaryContact.
- `Notification`: id, title, content, targetType (CLASS/STUDENT/ALL_PARENTS/STAFF), targetId, channel (APP/EMAIL/SMS/ZALO), createdBy, sentAt, status.
- `NotificationRecipient` (nếu cần theo dõi trạng thái đọc từng người nhận): id, notification_FK, recipient_FK, readAt.

**Tích hợp bên ngoài (cần quyết định nhà cung cấp cụ thể sau khi có ngân sách/đối tác):**
- SMS: eSMS, FPT SMS, hoặc Twilio (quốc tế, đắt hơn cho SMS VN).
- Email: SMTP có sẵn hoặc SendGrid/Amazon SES.
- Zalo OA (Official Account) API — phổ biến nhất với phụ huynh VN, cần đăng ký Zalo OA riêng cho nhà trường.
- Thiết kế `NotificationSender` dạng interface/strategy để dễ thêm/đổi nhà cung cấp mà không sửa business logic.

**API:** `/v1/parents/{parentId}/children`, `/v1/notifications` (tạo & gửi thông báo), cho phép PARENT đăng nhập xem điểm/điểm danh/học phí của con qua các endpoint hiện có (chỉ cần thêm `@PreAuthorize` cho phép PARENT kèm kiểm tra `parent-student` relation ở tầng service).

**UI:** trang đăng ký/gán phụ huynh cho học sinh (ADMIN); dashboard riêng cho PARENT (xem điểm/điểm danh/học phí/thông báo của (các) con); trang gửi thông báo hàng loạt cho ADMIN/GVCN.

*Đây là module tốn công nhất về tích hợp bên thứ ba — nên làm sau cùng trong Giai đoạn 3 và tách thành dự án con riêng nếu cần.*

### 3.7. Tuyển sinh đầu cấp

**Entity mới:** `AdmissionApplication`: id, applicantName, dateOfBirth, contactPhone, desiredGradeLevel, priorSchool, status (`PENDING`/`REVIEWING`/`APPROVED`/`REJECTED`), submittedAt, reviewedBy, note; `AdmissionDocument` (đính kèm giấy tờ, liên kết `DocumentAttachment` ở mục 3.9).

**API:** `/v1/admissions` (nộp hồ sơ công khai có giới hạn, duyệt hồ sơ nội bộ); endpoint "Duyệt & tạo học sinh chính thức" chuyển `AdmissionApplication` đã APPROVED thành bản ghi `Student` + `User` (role STUDENT) tự động, tránh nhập tay 2 lần.

**UI:** form nộp hồ sơ công khai (không cần đăng nhập), trang duyệt hồ sơ cho ADMIN.

### 3.8. Xuất báo cáo (PDF/Excel)

- Thêm dependency xuất PDF (ví dụ `iText`/`OpenPDF` hoặc dùng template HTML → PDF) và Excel (`Apache POI`).
- Endpoint: `GET /v1/reports/student/{id}/transcript` (học bạ/bảng điểm PDF), `GET /v1/reports/class/{id}/attendance?...` (Excel), `GET /v1/reports/fees/receipt/{feeId}` (biên lai PDF).
- Có thể triển khai từng báo cáo một, ưu tiên: bảng điểm/học bạ trước (nhu cầu cao nhất mỗi kỳ), biên lai học phí sau.

### 3.9. Hạ tầng dùng chung cho Giai đoạn 3

- `DocumentAttachment`: id, ownerType, ownerId, fileName, fileUrl (lưu S3-compatible storage hoặc filesystem + endpoint serve file), uploadedBy, uploadedAt, fileType. Dùng chung cho ảnh học sinh, hồ sơ tuyển sinh, học bạ scan...
- `AuditLog`: id, actor_FK, action (CREATE/UPDATE/DELETE), entityType, entityId, timestamp, detailJson. Ghi log ở tầng service (AOP `@Around` hoặc gọi thủ công tại các thao tác sửa điểm/xoá học sinh...).
- Luồng quên mật khẩu: `POST /v1/auth/forgot-password` → gửi email link reset có token hết hạn ngắn hạn; `POST /v1/auth/reset-password`.

---

## 4. Kế hoạch migration dữ liệu

Vì chuyển nhiều field từ chuỗi tự do sang khoá ngoại, cần script migration dữ liệu (không chỉ migration schema):

1. **`SchoolClass`**: từ dữ liệu `Student.className` + `Student.section` hiện có, sinh danh sách lớp duy nhất → tạo bản ghi `SchoolClass` tương ứng (gán `academicYear` mặc định = năm học hiện tại nếu không xác định được).
2. **`Student.currentClass_FK`**: đối chiếu `className`+`section` với `SchoolClass` vừa tạo để gán FK; ghi log các bản ghi không khớp được (sai chính tả...) để admin xử lý thủ công.
3. **`Subject`**: quét toàn bộ giá trị `Grade.subject` hiện có (nếu có dữ liệu thật ngoài `TEST_DATA_CORRECTED.sql`) → sinh danh mục `Subject` tương ứng.
4. **`AcademicYear`/`Semester`**: quét các giá trị chuỗi `academicYear` hiện có trong `Grade`/`Fee`/`SchoolClass` → tạo bản ghi tương ứng; vì dữ liệu cũ không phân biệt học kỳ, gán mặc định vào "Học kỳ 1" và cho phép admin sửa lại thủ công.
5. Toàn bộ migration dữ liệu viết dưới dạng script Flyway riêng (`V5__migrate_class_data.sql` hoặc Java migration callback), **chạy thử trên bản sao dữ liệu trước**, không chạy thẳng trên Aiven production.
6. Giữ `Student.className`/`section` (đánh dấu `@Deprecated`) ít nhất 1 phiên bản sau khi có `currentClass_FK`, để rollback an toàn nếu migration có vấn đề; xoá hẳn ở phiên bản sau khi đã xác nhận ổn định.

---

## 5. Kế hoạch kiểm thử

- **Giai đoạn 1**: viết test cho `AuthenticationService` (đăng ký không cho set role tuỳ ý, login không lộ log), test `GlobalExceptionHandler` (không lộ message gốc).
- **Giai đoạn 2**: test tích hợp cho `SchoolClassController`; test frontend (React Testing Library) cho các trang vừa nối API thật, ít nhất test "render danh sách", "gọi API khi submit form".
- **Giai đoạn 3**: đây là phần rủi ro cao nhất về logic nghiệp vụ (công thức tính điểm, xếp loại) — bắt buộc có **unit test cho service tính điểm** với các bộ dữ liệu mẫu đối chiếu tay theo đúng công thức Thông tư 22/58, review bởi người hiểu nghiệp vụ giáo dục (giáo viên/hiệu phó chuyên môn) trước khi release.
- Thiết lập CI (`.github/workflows/build.yml`) chạy `mvn test` + `npm test` trên mọi PR, chặn merge nếu fail.

---

## 6. Timeline tổng thể (ước lượng, giả định 1 backend dev + 1 frontend dev làm song song)

| Giai đoạn | Thời gian ước lượng | Có thể release độc lập? |
|---|---|---|
| 1. Vá bảo mật | 3–5 ngày | Có, nên release/deploy ngay khi xong |
| 2. Hoàn thiện phần dở dang | 2–3 tuần | Có |
| 3.1 Năm học/Học kỳ/Môn học (nền tảng) | 1 tuần | Không (module sau phụ thuộc) |
| 3.2 Phân công giảng dạy & Thời khoá biểu | 1–1.5 tuần | Có |
| 3.3 Hệ thống điểm TT22/58 | 1.5–2 tuần | Có (nhưng cần 3.1 xong trước) |
| 3.4 Hạnh kiểm | 3–4 ngày | Có |
| 3.5 Xét lên lớp | 1 tuần | Có (cần 3.3 + 3.4 xong trước) |
| 3.6 Phụ huynh & Sổ liên lạc điện tử | 2–3 tuần (tuỳ tích hợp SMS/Zalo) | Có |
| 3.7 Tuyển sinh đầu cấp | 1–1.5 tuần | Có |
| 3.8 Xuất báo cáo PDF/Excel | 1–1.5 tuần | Có |
| 3.9 Audit log, quên mật khẩu, upload tài liệu | 1 tuần | Có |

**Tổng ước lượng: ~4-5 tháng** cho toàn bộ 3 giai đoạn với nhịp độ trên; có thể rút ngắn nếu tăng nhân lực hoặc kéo dài nếu chỉ có 1 người làm bán thời gian.

---

## 7. Rủi ro & phụ thuộc cần lưu ý

- **Rủi ro dữ liệu**: migration từ chuỗi sang FK có thể làm lệch dữ liệu nếu dữ liệu gốc không sạch (sai chính tả tên lớp/môn) — cần bước rà soát thủ công sau migration tự động, không nên tự động 100%.
- **Rủi ro nghiệp vụ**: công thức tính điểm/xếp loại phải được người có chuyên môn giáo dục xác nhận trước khi dùng thật cho học sinh — sai công thức ảnh hưởng trực tiếp đến kết quả học tập.
- **Phụ thuộc bên ngoài**: module SMS/Zalo OA cần đăng ký tài khoản doanh nghiệp, phê duyệt, và có chi phí vận hành liên tục — cần quyết định ngân sách trước khi triển khai 3.6.
- **Không có test hiện tại** đồng nghĩa mọi thay đổi ở Giai đoạn 1-2 đều có rủi ro regression nếu không viết test song song — nên ưu tiên viết test cho phần đang sửa, không cần test toàn bộ hệ thống cũ ngay từ đầu.

---

*Tài liệu này là kế hoạch tổng thể; trước khi bắt đầu code từng phần, nên đưa qua EnterPlanMode/review chi tiết cho từng giai đoạn cụ thể để chốt thiết kế API/schema trước khi triển khai.*
