# PHÂN TÍCH DỰ ÁN: Hệ thống Quản lý Trường THCS-THPT (thcsthptTS)

*Tài liệu này do Claude đọc và phân tích trực tiếp toàn bộ mã nguồn (backend Spring Boot + frontend React) vào ngày 20/08/2026. Mục tiêu: đánh giá hiện trạng, chỉ ra các điểm chưa phù hợp cần sửa, và đề xuất các tính năng còn thiếu so với nhu cầu thực tế của một trường THCS/THPT tại Việt Nam.*

---

## 1. Tổng quan dự án

Dự án gồm hai phần:

- **Backend**: Java 17, Spring Boot 3.1.5, Spring Security 6 (JWT, stateless), Spring Data JPA, PostgreSQL (đang host trên Aiven Cloud), Swagger/OpenAPI 3, Lombok. Mã nguồn nằm ở `backend/src/main/java/com/schoolmanagement/` theo các gói `entity`, `repository`, `service`, `controller`, `dto`, `security`, `config`, `exception`.
- **Frontend**: React 18 (Create React App), React Router 6, React Bootstrap, Axios, `jwt-decode`, `zustand` (đã cài nhưng chưa thấy dùng ở đâu). Mã nguồn ở `frontend/src/`.

Các module nghiệp vụ đã có API backend đầy đủ (entity + repository + service + controller): **Auth, Staff (nhân sự), Student (học sinh), Attendance (điểm danh), Grade (điểm số), Fee (học phí), Library (thư viện)**. Ngoài ra có entity `SchoolClass` (lớp học) nhưng **không có controller/service riêng** — đây là một lỗ hổng chức năng, nêu chi tiết ở mục 3.3.

Phía frontend, chỉ 3/7 module có giao diện thực sự (Dashboard, Staff Management, Student Management dạng bảng CRUD cơ bản); 4 module còn lại (Library, Attendance, Grade, Fee) chỉ là trang placeholder "Component coming soon...".

---

## 2. Đánh giá kiến trúc & chất lượng mã nguồn

### 2.1. Điểm tốt
- Phân lớp rõ ràng (entity/repository/service/controller/dto), dùng đúng pattern Spring Boot chuẩn.
- Áp dụng RBAC với `@PreAuthorize` khá nhất quán theo vai trò (ADMIN, PRINCIPAL, TEACHER, STUDENT, LIBRARIAN, ACCOUNTANT).
- Có xử lý lỗi tập trung (`GlobalExceptionHandler`), có Swagger UI, có JWT access/refresh token.
- Entity dùng Lombok, chuẩn hoá timestamp `createdAt/updatedAt` qua `@PreUpdate`.

### 2.2. Vấn đề bảo mật cần sửa **ngay** (mức độ nghiêm trọng cao)

| # | Vấn đề | Vị trí | Rủi ro |
|---|---|---|---|
| 1 | JWT secret và thông tin kết nối PostgreSQL cloud (host, user, database) được **hard-code thẳng trong `application.yml`** và commit vào repo (README còn công khai cả host Aiven) | `backend/src/main/resources/application.yml`, `backend/README.md` | Bất kỳ ai có mã nguồn đều có thể giả mạo token JWT hợp lệ hoặc truy cập thẳng database. Cần chuyển sang biến môi trường / secret manager và **đổi ngay JWT secret + mật khẩu DB hiện tại** vì đã lộ. |
| 2 | Endpoint `POST /v1/auth/register` cho phép client tự gửi `role` trong body `User`, và service chỉ gán mặc định `STUDENT` khi `role == null` — **không ép buộc role** | `AuthController.register`, `AuthenticationService.register` | Bất kỳ người dùng chưa xác thực nào cũng có thể tự đăng ký tài khoản với `role: ADMIN` → chiếm toàn quyền hệ thống. Đây là lỗ hổng leo thang đặc quyền nghiêm trọng nhất trong dự án. |
| 3 | `AuthenticationService.login()` in ra console **mật khẩu người dùng nhập, toàn bộ password hash trong DB, và kết quả so khớp** ở dạng plain text (dùng `System.out.println` để debug) | `AuthenticationService.java` dòng ~168-192 | Rò rỉ thông tin nhạy cảm vào log server; vi phạm nguyên tắc bảo mật cơ bản, cần xoá toàn bộ trước khi lên production. |
| 4 | `spring.jpa.hibernate.ddl-auto: update` chạy trên DB thật | `application.yml` | Rủi ro mất dữ liệu/đổi schema ngoài ý muốn khi deploy; nên dùng migration tool (Flyway/Liquibase) và tắt `ddl-auto` ở production. |
| 5 | `GlobalExceptionHandler.handleGeneralException` trả `ex.getMessage()` (thông điệp lỗi gốc, có thể chứa chi tiết nội bộ/SQL) thẳng ra client với HTTP 500 | `GlobalExceptionHandler.java` | Rò rỉ thông tin hệ thống, nên trả thông điệp chung và log chi tiết ở server. |
| 6 | Không có annotation validation (`@NotNull`, `@Size`, `@Email`...) trên entity/DTO dù đã có dependency `spring-boot-starter-validation` — validation gần như không được dùng | Toàn bộ entity | Dữ liệu đầu vào (email, số điện thoại, điểm số âm/vượt quá thang điểm...) không được kiểm tra tại tầng API. |
| 7 | Frontend hiển thị sẵn tài khoản test (`admin/Test@123`...) ngay trên trang đăng nhập và tự điền sẵn username/password | `LoginPage.js` | Chấp nhận được khi demo nội bộ nhưng **bắt buộc phải gỡ bỏ** trước khi triển khai cho trường thật. |

### 2.3. Vấn đề thiết kế dữ liệu / logic nghiệp vụ

- **`Student.className` / `Student.section` là chuỗi tự do**, không tham chiếu tới entity `SchoolClass` (dù entity này đã tồn tại). Hậu quả: đổi tên lớp không đồng bộ, không kiểm soát sĩ số (`capacity`), không thể truy vấn "danh sách học sinh của lớp X" một cách đáng tin cậy (so sánh chuỗi dễ sai chính tả/khoảng trắng). Tương tự, `Grade.subject`, `Fee.feeType` cũng là chuỗi tự do thay vì bảng danh mục (master data), dễ sinh dữ liệu rác kiểu "Toán" và "toán " là hai giá trị khác nhau.
- **Không có entity `SchoolClass` Controller/Service/Repository nào được expose** — không thể tạo/sửa/xoá lớp học qua API, dù frontend Sidebar hoàn toàn chưa có mục "Quản lý lớp học".
- **`Grade` chỉ có 1 điểm số + `examType` dạng chuỗi tự do**, tính trung bình đơn giản bằng `average()` không trọng số. Hệ thống điểm của THCS/THPT Việt Nam (theo Thông tư 22/2021/TT-BGDĐT hoặc Thông tư 58 với các khối chưa áp dụng TT22) yêu cầu **hệ số điểm** (điểm miệng/15 phút hệ số 1, giữa kỳ hệ số 2, cuối kỳ hệ số 3), tính theo **học kỳ và cả năm**, và xếp loại học lực (Giỏi/Khá/Đạt/Chưa đạt hoặc thang cũ Giỏi/Khá/TB/Yếu/Kém). Hiện dự án không mô hình hoá được việc này.
- **Không có khái niệm Năm học (AcademicYear)/Học kỳ (Semester) như một entity riêng** — `academicYear` chỉ là chuỗi rải rác ở nhiều bảng (`Grade`, `Fee`, `SchoolClass`), dễ gõ sai và không thể quản lý vòng đời năm học (mở/khoá năm học, chuyển lớp cuối năm).
- **Không có Hạnh kiểm/Đạo đức (Conduct)** — học bạ Việt Nam luôn có xếp loại hạnh kiểm song song với học lực, hiện chưa có entity nào cho việc này.
- **`AuthResponse` không có field `permissions`**, nhưng frontend (`authService.hasPermission`) lại đọc `user?.permissions` — tính năng kiểm tra quyền chi tiết ở frontend **thực chất luôn trả về `false`** (dead code/bug tiềm ẩn), do backend không trả field này khi login.
- **Danh sách (staff, student...) không phân trang** (`getAllStaff()`, `getAllStudents()` trả toàn bộ list) — sẽ chậm khi trường có vài nghìn học sinh nhiều năm học.
- Thư viện: hạn mức phạt trả sách trễ hard-code `10.0`/ngày trong code, không cấu hình được; không giới hạn số sách một học sinh được mượn cùng lúc; `BookStatus.RESERVED` và `TransactionType.RESERVE` được định nghĩa nhưng **không có logic đặt trước sách nào sử dụng** — tính năng dở dang.
- Không thấy test nghiệp vụ nào ngoài 1 file test mặc định trống (`SchoolManagementApplicationTests`) — không có unit test/integration test cho service (tính điểm, tính học phí, mượn trả sách...).

### 2.4. Vấn đề frontend

- 4/7 trang quản lý (Thư viện, Điểm danh, Điểm số, Học phí) chỉ là khung rỗng — đây là phần **thiếu nhiều nhất về khối lượng công việc còn lại**, vì backend đã có API sẵn nhưng chưa được nối vào UI.
- `Dashboard.js` dùng dữ liệu **giả lập cứng** (`attendanceRate: 85`, `totalRevenue: 125000`, hoạt động gần đây, "Active Users: 24"...) thay vì gọi API thật — số liệu hiển thị không phản ánh đúng tình trạng hệ thống, dễ gây hiểu nhầm cho hiệu trưởng/ban giám hiệu khi xem dashboard.
- `API_BASE_URL` hard-code `http://localhost:8080/api` trong `api.js`, không đọc từ biến môi trường (`.env` / `REACT_APP_API_URL`) — không thể build cho nhiều môi trường (dev/staging/production) mà không sửa code.
- Modal "Thêm/Sửa nhân viên" trong `StaffManagement.js` chỉ có UI, nút "Save" **chưa gọi API thực sự** để tạo/cập nhật — chức năng CRUD nhân sự chưa hoàn thiện dù có sẵn API.
- Chưa có xử lý refresh token tự động khi access token hết hạn (interceptor Axios chỉ redirect về trang login khi 401, chưa thử `/v1/auth/refresh-token` trước).

---

## 3. Tính năng còn thiếu so với nhu cầu thực tế trường THCS-THPT Việt Nam

Đây là phần quan trọng nhất theo yêu cầu — các tính năng dưới đây thường **bắt buộc hoặc rất phổ biến** ở phần mềm quản lý trường học THCS/THPT tại Việt Nam nhưng **chưa có trong dự án**:

### 3.1. Học vụ theo chuẩn Việt Nam
- **Quản lý Môn học (Subject)** dạng bảng danh mục, gắn với khối lớp (6-12), thay vì chuỗi tự do.
- **Quản lý Năm học/Học kỳ (AcademicYear/Semester)** như entity riêng, có trạng thái mở/đóng.
- **Hệ thống tính điểm theo Thông tư 22/2021 (THCS, THPT từ 2022 trở đi) hoặc Thông tư 58 (khối chưa chuyển đổi)**: điểm hệ số 1/2/3, điểm trung bình môn học kỳ, điểm trung bình môn cả năm, điểm trung bình các môn học kỳ/cả năm, xếp loại học lực (Tốt/Khá/Đạt/Chưa đạt theo TT22 hoặc Giỏi/Khá/TB/Yếu/Kém theo TT58).
- **Xếp loại Hạnh kiểm/Rèn luyện** (Tốt/Khá/Trung bình/Yếu) song song với học lực.
- **Sổ điểm điện tử / Học bạ điện tử** — xuất được học bạ, bảng điểm theo mẫu quy định.
- **Xét lên lớp / ở lại / tốt nghiệp cuối năm** (promotion workflow) dựa trên kết quả học tập + hạnh kiểm + số buổi nghỉ.
- **Thời khoá biểu (Timetable)** theo tiết học/phòng học/giáo viên, phân công giảng dạy (giáo viên nào dạy môn nào ở lớp nào) — hiện `Grade.teacher` chỉ gắn 1-1 với điểm, không có bảng phân công độc lập.
- **Điểm danh theo tiết học** (không chỉ theo ngày) — với THPT đặc biệt quan trọng vì học sinh học nhiều môn/nhiều giáo viên mỗi ngày.

### 3.2. Tuyển sinh & Hồ sơ học sinh
- **Quy trình tuyển sinh đầu cấp (nhập học)** — hiện chỉ có "tạo học sinh" trực tiếp, chưa có luồng nộp hồ sơ, xét tuyển, phân lớp đầu năm.
- **Chuyển trường đi/đến**, lưu lịch sử quá trình học tại các trường trước (`StudentStatus.TRANSFERRED` có nhưng không có form/luồng xử lý).
- **Hồ sơ sức khỏe học đường** mở rộng hơn `bloodGroup` (tiêm chủng, dị ứng, bệnh mãn tính, khám sức khỏe định kỳ theo quy định y tế học đường).
- **Liên kết Phụ huynh - Học sinh chính thức**: `Role.PARENT` đã được định nghĩa trong enum nhưng **không có bất kỳ entity/API/UI nào sử dụng** — cần bảng quan hệ Parent-Student, cho phép phụ huynh đăng nhập xem điểm/điểm danh/học phí con mình (đang chỉ lưu tên/SĐT cha mẹ dạng text trong `Student`).

### 3.3. Quản lý lớp học (cần bổ sung API/UI ngay)
- Hoàn thiện `SchoolClassController` + `SchoolClassService` + `SchoolClassRepository` (hiện thiếu hoàn toàn dù entity đã có) để: tạo/sửa/xoá lớp, gán giáo viên chủ nhiệm, xem sĩ số, danh sách học sinh theo lớp dùng khoá ngoại thay vì so chuỗi.
- Trang "Quản lý lớp học" trên frontend (Sidebar hiện chưa có mục này).

### 3.4. Học phí & Tài chính
- **Danh mục các khoản thu** (học phí, đồng phục, bán trú, xe đưa đón, ngoại khóa, bảo hiểm y tế học sinh...) — hiện `feeType` là chuỗi tự do, không có cấu hình khoản thu theo khối/lớp/năm học.
- **Đóng theo đợt/trả góp** (installment) — hiện Fee chỉ có amount/paidAmount tổng, không hỗ trợ lịch đóng nhiều lần có ngày hạn riêng từng đợt.
- **Miễn giảm học phí có lý do** (hộ nghèo, con thương binh liệt sĩ, học bổng...) — `FeeStatus.EXEMPTED` có nhưng không lưu lý do/quyết định miễn giảm.
- **Tích hợp cổng thanh toán** (VNPay, Momo, ngân hàng...) — hiện `processPayment` chỉ nhận số tiền qua form, không có webhook xác nhận giao dịch thật.
- **Biên lai/hoá đơn điện tử** xuất PDF.

### 3.5. Giao tiếp & Thông báo
- **Sổ liên lạc điện tử** gửi thông báo điểm/điểm danh/học phí tới phụ huynh qua SMS, email hoặc Zalo OA — đây là tính năng gần như "phải có" ở phần mềm trường học Việt Nam hiện nay, hiện dự án hoàn toàn chưa có module thông báo/notification nào.
- **Thông báo nội bộ** (thông báo của nhà trường, lịch nghỉ, sự kiện) cho giáo viên/học sinh/phụ huynh.

### 3.6. Báo cáo & Xuất dữ liệu
- **Xuất báo cáo PDF/Excel**: bảng điểm, học bạ, danh sách điểm danh, báo cáo học phí theo lớp/khối — hiện Permission enum có `GENERATE_REPORT`/`VIEW_REPORT` nhưng không có bất kỳ endpoint nào triển khai.
- **Dashboard thật** dựa trên dữ liệu thực (không phải số liệu giả lập như hiện tại).

### 3.7. Vận hành hệ thống
- **Nhật ký hoạt động/audit log** (`Permission.VIEW_LOGS` đã định nghĩa nhưng chưa triển khai) — cần thiết để tra soát khi sửa điểm, xoá học sinh...
- **Quên mật khẩu / đặt lại mật khẩu qua email** — hiện không có luồng nào.
- **Quản lý tài liệu/đính kèm** (ảnh học sinh, giấy khai sinh, học bạ scan, hợp đồng lao động giáo viên...) — chưa có upload file.
- **Phân trang, tìm kiếm, lọc nâng cao** cho các danh sách lớn.
- **Môi trường cấu hình tách biệt** (`application-dev.yml`, `application-prod.yml`), CI/CD kiểm thử tự động (đã có `.github/workflows/build.yml` nhưng nên bổ sung chạy test + kiểm tra bảo mật cơ bản, ví dụ secret scanning).

---

## 4. Đề xuất lộ trình ưu tiên

**Ưu tiên 1 – Phải sửa trước khi dùng thật (bảo mật):**
1. Xoá log debug lộ mật khẩu trong `AuthenticationService`.
2. Chặn client tự set `role` khi đăng ký (`AuthController`/`AuthenticationService`), luôn ép `STUDENT` hoặc yêu cầu ADMIN duyệt/gán vai trò riêng.
3. Chuyển JWT secret + thông tin DB ra biến môi trường, **đổi ngay** các giá trị đã lộ trong repo hiện tại.
4. Bỏ hard-code tài khoản test trên trang đăng nhập.
5. Bật `@Valid` + validation annotation cho toàn bộ DTO nhận từ client.

**Ưu tiên 2 – Hoàn thiện phần đã làm dở (để hệ thống dùng được):**
1. Nối 4 trang frontend còn placeholder (Thư viện, Điểm danh, Điểm số, Học phí) với API đã có sẵn.
2. Bổ sung `SchoolClass` Controller/Service + trang Quản lý lớp học.
3. Chuyển `className`/`subject`/`feeType` từ chuỗi tự do sang bảng danh mục có khoá ngoại.
4. Dashboard dùng dữ liệu thật thay vì số giả lập.
5. Thêm phân trang cho các API danh sách.

**Ưu tiên 3 – Bổ sung tính năng đặc thù trường học Việt Nam:**
1. Module tính điểm theo Thông tư 22/58, hạnh kiểm, xét lên lớp.
2. Liên kết Phụ huynh–Học sinh + sổ liên lạc điện tử (SMS/email/Zalo).
3. Thời khoá biểu và phân công giảng dạy.
4. Xuất học bạ/bảng điểm/biên lai PDF.
5. Audit log, quên mật khẩu, upload tài liệu.

---

*Tài liệu này được tạo tự động bởi Claude dựa trên việc đọc toàn bộ mã nguồn backend và frontend tại thời điểm phân tích. Khi dự án thay đổi, nên cập nhật lại tài liệu này.*
