# KẾ HOẠCH PHÁT TRIỂN & NÂNG CẤP HỆ THỐNG QUẢN LÝ TRƯỜNG THCS-THPT (thcsthptTS)

**Phiên bản 4.6 — ngày 02/09/2026**
*(v4.6: **Giai đoạn A hoàn tất** — thêm A5 (dọn file rác + `.gitignore` gốc), A6 (Việt hoá nhãn menu), A7 (test `navigation`), A8 (gỡ `hasPermission` dead-code). v4.5: nhóm bảo mật frontend (A2/A3/A4/H.1 #2). Còn chặn duy nhất: B0 (xếp loại học lực — cần bảng ngưỡng). Xem "Nhật ký thay đổi".)*

> **Tài liệu này là kế hoạch nâng cấp DUY NHẤT** — thay cho `IMPLEMENTATION_PLAN.md` (v3.1) và đã gộp cả 2 file phân quyền lẻ trước đây.

> **⚡ Việc gấp nhất còn lại (chốt v4.6):** Giai đoạn A đã xong toàn bộ (A1–A9). Việc *chặn* duy nhất còn lại là **B0 — xếp loại học lực TT22/58** (`classification = null`), cần **bảng ngưỡng chuyên môn** (Quyết định E.2). Không chặn: gỡ `Grade` cũ (Quyết định E.1), C3 (cổng tự phục vụ HS/PH), D2 (nút tải báo cáo).

*Tài liệu này được lập sau khi review lại **toàn bộ** mã nguồn hiện tại trên máy (`D:\sources\thcsthptTS` là **nguồn sự thật chính** — không dựa vào bản sao trong Claude Project). Khác với kế hoạch v3.1 (`IMPLEMENTATION_PLAN.md`) vốn là kế hoạch **xây mới từ đầu**, phiên bản 4.x xuất phát từ thực tế: **phần lớn kế hoạch v3.1 đã được hiện thực hoá ở backend**. Trọng tâm mới là (1) đưa năng lực backend đã có lên giao diện người dùng, (2) **hoàn tất phần backend còn dở** (xếp loại học lực), (3) trả nợ kỹ thuật, (4) nâng cấp lên mức vận hành thật cho một trường.*

> **Lưu ý về tài liệu repo:** `IMPLEMENTATION_PLAN.md` (v3.1) và `PROJECT_ANALYSIS_SUMMARY.md` (20/08/2026) hiện **đã lỗi thời** — chúng mô tả trạng thái *trước khi* code (SchoolClass chưa có controller, 4 trang placeholder, chưa có test...), trong khi mã nguồn thực tế đã làm xong phần lớn. Nên **lưu trữ (archive) hoặc cập nhật lại** 2 file này để tránh hiểu nhầm. Tài liệu v4.x này thay vai trò kế hoạch hiện hành.

---

## PHẦN A — TÓM TẮT HIỆN TRẠNG (kết quả review, đã xác minh trên repo)

### A.1. Backend: gần hoàn tất kế hoạch v3.1 — còn 1 mảng dở (xếp loại học lực)

| Nhóm | Đã có ở backend | Trạng thái |
|---|---|---|
| Bảo mật | `RegisterRequest` (chặn client tự set role), `UserController` tạo tài khoản chỉ ADMIN, JWT secret fail-fast (không default), `GlobalExceptionHandler` tiếng Việt, `StudentAccessGuard`, rate-limit filter (tuyển sinh + quên mật khẩu) | **Xong** |
| Hạ tầng CSDL | MySQL + Flyway `V1`→`V10`, `ddl-auto: validate`, profile `dev/prod/test`, `.env` đã gitignore đúng | **Xong** |
| Lớp học | `SchoolClass` CRUD + gán GVCN + danh sách HS theo lớp, có phân trang | **Xong** |
| Dashboard | `DashboardController` + `DashboardService` (số liệu thật) | **Xong** |
| Năm học/Học kỳ/Môn học | `AcademicYear`, `Semester`, `Subject` + controller | **Xong** |
| Phân công + TKB | `TeachingAssignment`, `TimetableSlot` + kiểm tra trùng lịch/phòng | **Xong** |
| Điểm TT22 — nhập điểm & tính TB | `GradeRecord`, `GradeComponentConfig`, `GradeRecordService`: nhập điểm theo loại (miệng/15'/1 tiết/giữa kỳ/cuối kỳ), tính **ĐTB môn HK** = Σ(điểm×hệ số)/Σ(hệ số) và **ĐTB môn cả năm** = (HK1 + HK2×2)/3 | **Xong** |
| Điểm TT22 — **xếp loại học lực** | Enum `GradeClassification` (TOT/KHA/DAT/CHUA_DAT & GIOI/KHA/TB/YEU/KEM) đã có, **nhưng phần tính xếp loại CỐ TÌNH chưa code** — mọi nơi trả `classification = null`; ghi rõ trong Javadoc "cần người chuyên môn xác nhận bảng ngưỡng trước khi code" | **CHƯA XONG (backend)** |
| Hạnh kiểm | `ConductRecord` + roster đánh giá hàng loạt cho GVCN | **Xong** |
| Xét lên lớp | `PromotionRecord`, `PromotionThresholdConfig`, preview + confirm. **Lưu ý:** hiện so **điểm TB thô** + hạnh kiểm HK2 + tỷ lệ chuyên cần với ngưỡng ADMIN cấu hình — **không dùng xếp loại học lực chính thức TT22/58** (vì phần đó chưa code) | **Xong (một phần)** |
| Phụ huynh + Sổ liên lạc | `ParentStudentRelation`, `Notification`, `NotificationRecipient`, sender APP + EMAIL chạy thật; **SMS + Zalo là stub throw lỗi** | **Xong (một phần)** |
| Tuyển sinh | `AdmissionApplication` + approve-and-create + rate-limit | **Xong** |
| Báo cáo | `ReportService` (28KB): học bạ/bảng điểm PDF (OpenPDF + font tiếng Việt), điểm danh Excel (POI), biên lai | **Xong** |
| Tài liệu đính kèm | `DocumentAttachment` + `FileStorageService` (lưu filesystem, giới hạn 10MB) | **Xong** |
| Audit log | `AuditLog` + `AuditLogService` + controller (có phân trang) | **Xong** |
| Quên mật khẩu | `PasswordResetService` + `forgot/reset` endpoint + email + rate-limit | **Xong** |
| Kiểm thử | ~20 lớp integration test (Grade, GradeRecord, Conduct, Promotion, Admission, Report, Parent/Notification, Document, PasswordReset, Timetable, Library, Attendance, Staff, AcademicStructure, AuditLog, RateLimit...) | **Tốt** |

**Kết luận backend:** việc *xây mới* gần như đã xong. Phần backend còn lại gồm: (1) **tính xếp loại học lực TT22/58** (đang trả `null`) — chặn bởi quyết định ngưỡng chuyên môn; (2) **thống nhất mô hình điểm** (bỏ `Grade` cũ, chỉ giữ `GradeRecord` — xem A.3.1); (3) SMS/Zalo thật; (4) các nâng cấp mới (Giai đoạn E).

### A.2. Frontend: đã phủ phần lớn năng lực backend *(cập nhật v4.4 — tiến bộ lớn)*

Vite + Tailwind + shadcn/ui (bỏ CRA), dark mode, React Query, sonner, bộ component dùng chung. **Đã bổ sung kể từ v4.3:** `React.lazy` **code-splitting per-route** + **Skeleton loading**; **Vitest + React Testing Library** (`DataTable/DatePicker/Form.test.jsx`) + `rollup-plugin-visualizer`. Số trang tăng mạnh **từ 8 → 16 trang** trong `App.jsx`:

`Dashboard, Staff, Student, Class, Library, Attendance, Grade (đã chuyển sang TT22 /v1/grade-records), Fee, AcademicConfig (năm học/HK/môn), Timetable, Conduct (có scoping GVCN), Promotions, Parents (+ tạo tài khoản PARENT qua /v1/users), NotificationCenter, Admissions (duyệt), AuditLog` — cộng **3 trang công khai**: `Apply (nộp hồ sơ tuyển sinh), ForgotPassword, ResetPassword`. Có `DocumentsDialog` (upload/xem tài liệu đính kèm).

→ Phần lớn Giai đoạn B–D của kế hoạch **đã có UI**: B1 (AcademicConfig), B2 (Grade→TT22), B3 (Conduct), B4 (Timetable), B5 (Promotions), C1 (Parents), C2 (Notifications), D1 (Admissions), D3 (Documents), D4 (AuditLog), D5 (Quên/Đặt lại mật khẩu) — đều đã lên trang.

**Còn thiếu ở frontend:**
- **Cổng tự phục vụ Học sinh & Phụ huynh (C3)** — `navigation.js` ghi rõ STUDENT/PARENT "chưa có trang nào route tới" cho điểm/hạnh kiểm/học phí của mình/con; PARENT hiện chỉ vào được Notifications.
- **Nút tải báo cáo** (học bạ PDF/điểm danh Excel/biên lai) — cần rà đã gắn vào trang chưa (D2).
- ~~**`ProtectedRoute` chặn route theo vai trò**~~ ✅ **XONG (v4.5)** — `components/auth/ProtectedRoute.jsx` tra `rolesForPath()` trong `config/navigation.js`; sai vai trò → redirect về `defaultPathForRole()` (Dashboard, hoặc `/notifications` cho PARENT). `App.jsx` bọc mọi route qua `guarded()`. Có test `ProtectedRoute.test.jsx`.
- ~~Nhãn menu tiếng Anh~~ ✅ **XONG (v4.6)** — đã Việt hoá toàn bộ `NAV_ITEMS` + `pageTitleForPath`.

### A.3. Nợ kỹ thuật — trạng thái cập nhật (v4.4)

**✅ ĐÃ XỬ LÝ kể từ v4.3** (developer đã bắt tay vá theo kế hoạch, xác minh trên code Sep 2):

- **Bootstrap đã gỡ hẳn** — `main.jsx` chỉ còn `index.css`; `package.json` **không còn** `bootstrap`/`react-bootstrap`; `App.jsx` dùng Skeleton thay spinner Bootstrap.
- **Test frontend + code-splitting** — Vitest + RTL (`*.test.jsx`), `React.lazy` per-route, `rollup-plugin-visualizer`.
- **IDOR hồ sơ học sinh — VÁ XONG** — `StudentController` inject `StudentAccessGuard`; GET `/{id}` & `/roll/{roll}` nhận `Authentication` + gọi `enforceCanAccessStudent`; thêm PARENT. (G.4/H.1 #1)
- **STUDENT bị loại khỏi danh bạ nhân sự** — `GET /v1/staff*` giờ ADMIN/PRINCIPAL/TEACHER (comment dẫn chiếu Phần G.4 mục 3).
- **GradeManagement đã chuyển sang TT22** (`/v1/grade-records`) — model điểm cũ không còn dùng ở FE.
- **Hạnh kiểm có scoping theo GVCN** — `enforceHomeroomWriteAccess` 403 theo lớp (một phần Mức 3.1).

**✅ ĐÃ XỬ LÝ kể từ v4.4 (nhóm bảo mật frontend, code đổi 02/09):**

- **[A2] Gỡ log lộ token** — `authService.login` bỏ toàn bộ `console.log('Login response'...)`, `console.log` URL, `console.error('Error response'...)`; chỉ còn re-throw payload lỗi cho toast. `grep console\. frontend/src` sạch (chỉ còn 1 dòng comment tham chiếu). `handleLogout` trong `App.jsx` nay xoá cả `refreshToken`.
- **[A4] Refresh-token interceptor** — `api.js` response interceptor: 401 (có token, không phải `/v1/auth/*`, chưa `_retry`) → gọi 1 lần `POST /v1/auth/refresh-token` với `Authorization: Bearer <refreshToken>`; các request 401 khác trong lúc refresh xếp `pendingQueue` chờ rồi retry; refresh hỏng → `clearSessionAndRedirect()` (có chặn vòng lặp redirect). Lưu lại `accessToken`+`refreshToken`+`user` từ AuthResponse.
- **[A3] `ProtectedRoute`** — xem A.2 ở trên.
- **[H.1 #2] Redact `StaffDTO`** — `StaffController` 3 GET cho TEACHER (`/{id}`, `/employee/{id}`, `GET /v1/staff`) nhận `Authentication`; `redactSensitiveFields()` null hoá `salary`/`address`/`city`/`state`/`postalCode`/`emergencyContactName`/`emergencyContactPhone` cho vai trò ≠ ADMIN/PRINCIPAL. Thêm test `StaffIntegrationTest`: ADMIN thấy đủ, TEACHER bị redact (đơn + danh sách).

**✅ ĐÃ XỬ LÝ kể từ v4.5 (đóng nốt Giai đoạn A, code đổi 02/09):**

- **[A8] Gỡ `hasPermission`** — hàm dead-code (đọc `user.permissions`, luôn `false` vì `AuthResponse` không có field đó) đã bỏ khỏi `authService.js`; UI phân quyền qua `getUserRole()` + `navigation.js`. Không nơi nào gọi `hasPermission` (đã grep).
- **[A6] Việt hoá nhãn menu** — `NAV_ITEMS` trong `config/navigation.js`: "Staff Management"→"Quản lý nhân sự", "Grades"→"Quản lý điểm", "Audit Log"→"Nhật ký hoạt động", ... `pageTitleForPath` fallback "Dashboard"→"Tổng quan". Không còn nhãn tiếng Anh.
- **[A5] Dọn file rác** — xoá 4 file 0 byte gốc (`DOCUMENTATION_CLEANUP_SUMMARY.md`, `MYSQL_FINAL_SUMMARY.txt`, `PROJECT_INDEX.md`, `SETUP_COMPLETE.html`); `git rm --cached .idea/ thcsthptTS.iml`; thêm `.gitignore` ở repo gốc (`.idea/`, `*.iml`, `.vscode/`, `.env`, `/backend/uploads/`...). *(Ghi chú: `backend/uploads/**` PDF thực ra **chưa từng** bị track — `backend/.gitignore` đã có `/uploads/`; `IMPLEMENTATION_PLAN.md`/`PROJECT_ANALYSIS_SUMMARY.md` đã ở `archive/` từ v4.4.)*
- **[A7] Test** — thêm `config/navigation.test.js` (5 ca: nhãn không còn tiếng Anh, `navItemsForRole`, `rolesForPath`, `defaultPathForRole`, `pageTitleForPath`). `npm test` xanh **28/28** (5 file).

**⚠️ CÒN MỞ:**

1. **Xếp loại học lực TT22/58 vẫn `null`** — `GradeRecordService`/`PromotionService` không đổi → **B0 chưa làm** (chặn bởi bảng ngưỡng chuyên môn — Quyết định E.2). *Đây là việc chặn duy nhất còn lại.*
2. **Hai mô hình điểm vẫn song song** — `Grade` cũ chưa `@Deprecated`/gỡ dù FE đã dùng `GradeRecord` (Quyết định E.1).
3. **Token `localStorage`** (XSS) → cân nhắc cookie `HttpOnly` (F3).
4. `lucide-react ^1.37.0` version lạ; **SMS/Zalo vẫn stub** (E1).

---

## PHẦN B — ĐỊNH HƯỚNG PHÁT TRIỂN v4.x

Ba nhóm mục tiêu, xếp theo ưu tiên:

1. **HOÀN THIỆN (ưu tiên 1).** Đưa toàn bộ năng lực backend đã xây lên giao diện + hoàn tất phần backend còn dở (xếp loại học lực). Giá trị nhanh nhất vì API đã sẵn sàng và đã có test.
2. **CỦNG CỐ (ưu tiên 2).** Trả nợ kỹ thuật ở A.3, thống nhất mô hình điểm, bảo mật frontend, dọn dẹp, thêm test frontend.
3. **NÂNG CẤP (ưu tiên 3).** Cổng thanh toán, SMS/Zalo thật, tự phục vụ HS/PH, điểm danh theo tiết, vận hành/CI-CD/observability.

**Nguyên tắc xuyên suốt** (kế thừa v3.1): bám sát trường THCS/THPT Việt Nam thật — khối 6–12, năm học tháng 9→5, HK1/HK2, điểm & xếp loại TT22 (hoặc TT58), hạnh kiểm song song học lực, sổ liên lạc ưu tiên Zalo/SMS, thuật ngữ tiếng Việt ngành giáo dục. **Ngưỡng xếp loại học lực và quy định nghỉ học phải được người có chuyên môn giáo dục xác nhận trước khi phát hành** (đây chính là thứ đang chặn phần backend còn dở).

---

## PHẦN C — LỘ TRÌNH CHI TIẾT

> Quy ước: mỗi giai đoạn có **Việc cần làm** (đủ chi tiết để giao dev) + **DoD** (định nghĩa hoàn thành). Ước lượng theo 1 FE dev + 1 BE dev song song.

### GIAI ĐOẠN A — Củng cố nền tảng Frontend & dọn nợ (1–1.5 tuần)

Làm trước tất cả vì mọi trang mới đều dựa trên nền này.

- **A1.** ✅ **XONG** — Bootstrap đã gỡ khỏi `main.jsx`, `App.jsx`, `package.json`.
- **A2.** ✅ **XONG (v4.5)** — `authService.js` sạch `console.log/error`; `api.js` không log.
- **A3.** ✅ **XONG (v4.5)** — `components/auth/ProtectedRoute.jsx` + `rolesForPath()`/`defaultPathForRole()` trong `config/navigation.js`; `App.jsx` bọc route qua `guarded()`; có `ProtectedRoute.test.jsx`.
- **A4.** ✅ **XONG (v4.5)** — interceptor `api.js` gọi `POST /v1/auth/refresh-token`, queue request khi đang refresh, chỉ logout khi refresh hỏng.
- **A5.** ✅ **XONG (v4.6)** — xoá 4 file 0 byte gốc; `git rm --cached .idea/ thcsthptTS.iml`; thêm `.gitignore` repo gốc.
- **A6.** ✅ **XONG (v4.6)** — nhãn `NAV_ITEMS` + fallback `pageTitleForPath` đã Việt hoá toàn bộ.
- **A7.** ✅ **XONG (v4.6)** — Vitest + RTL: `DataTable`/`DatePicker`/`Form`/`ProtectedRoute`/`navigation` (28 test).
- **A8.** ✅ **XONG (v4.6)** — gỡ hẳn `hasPermission` (dead code), UI phân quyền theo `role`.
- **A9. (Backend — bảo mật) Vá các lỗ hổng phân quyền ở Phần G:** trước hết là **IDOR hồ sơ học sinh** (G.2 mục 1) — `GET /v1/students/{id}` và `/roll/{rollNumber}` cho STUDENT nhưng **không áp `StudentAccessGuard`**; thêm guard hoặc bỏ STUDENT khỏi 2 endpoint này. Kèm rà `PUT /v1/notifications/{recipientId}/read` và `POST /v1/documents` (chỉ `authenticated()`, cần kiểm tra chủ sở hữu ở tầng service).

**DoD:** ~~build sạch không còn Bootstrap; đăng nhập không lộ token ở console; sai vai trò bị chặn route; token hết hạn tự refresh; `npm test` xanh; repo không còn file rác; **học sinh không đọc được hồ sơ học sinh khác**.~~ → ✅ **ĐẠT (v4.6)** — tất cả các mục trên đã xong (IDOR vá ở v4.4; A2/A3/A4/H.1#2 ở v4.5; A5/A6/A7/A8 ở v4.6). *Còn thiếu bằng chứng: integration test khẳng định STUDENT gọi `GET /v1/students/{idNgườiKhác}` → 403 đã có (`StudentAccessSecurityTest`); cần chạy trên CI có MySQL.*

### GIAI ĐOẠN B — Hoàn tất học vụ TT22 (backend còn dở) + phủ UI học vụ (3–4 tuần)

- **B0. (Backend) Tính xếp loại học lực TT22/58 (2–3 ngày, sau khi có bảng ngưỡng).**
  - Hiện thực phần đang trả `classification = null` trong `GradeRecordService` (điểm summary HK/cả năm) theo đúng ngưỡng TT22 (TOT/KHA/DAT/CHUA_DAT) hoặc TT58 (GIOI/KHA/TB/YEU/KEM), kèm điều kiện môn Toán/Ngữ văn.
  - Cân nhắc cho `PromotionService` dùng xếp loại học lực chính thức thay cho điểm TB thô, khi B0 xong.
  - **Bắt buộc:** unit test đối chiếu tay + review bởi người chuyên môn giáo dục. **Chặn bởi quyết định E.2.**

- **B1. Năm học / Học kỳ / Môn học (3–4 ngày).** Trang `features/academic-config`: 3 tab CRUD nối `/v1/academic-years` (+ "Đóng năm học" `PUT .../{id}/close`), `/v1/semesters`, `/v1/subjects`. Selector "Năm học/Học kỳ hiện hành" toàn cục (zustand) cho các trang điểm/học phí/TKB lọc theo. Quyền ADMIN/PRINCIPAL.

- **B2. Chuyển GradeManagement sang TT22 (5–6 ngày, phụ thuộc B0 + B1).**
  - Đổi `gradeService` sang `/v1/grade-records`: nhập điểm theo `componentType` cho lớp × môn × học kỳ.
  - Trang **Bảng tổng hợp điểm** gọi đúng endpoint đã có: `GET /v1/grade-records/student/{id}/summary?semesterId=...` (ĐTB môn HK) và `GET /v1/grade-records/student/{id}/year-summary?academicYearId=...` (ĐTB môn cả năm) — hiển thị thêm cột xếp loại sau khi B0 xong. Trang cấu hình hệ số `/v1/grade-config` cho ADMIN.
  - **Chốt kỹ thuật:** thống nhất `GradeRecord`, đánh dấu `Grade` cũ `@Deprecated` ở backend; nếu có dữ liệu điểm cũ, migration Flyway `V11` chuyển sang `grade_records`; gỡ `gradeService` cũ khỏi frontend.

- **B3. Hạnh kiểm (2–3 ngày).** Lưới đánh giá hàng loạt cho GVCN: `GET /v1/conduct/class/{classId}/semester/{semesterId}` → nhập rating (Tốt/Khá/TB/Yếu) + ghi chú; `POST/PUT /v1/conduct`. TEACHER chỉ lớp chủ nhiệm (backend đã chặn — FE ẩn nút).

- **B4. Phân công giảng dạy & TKB (4–5 ngày).** CRUD `/v1/teaching-assignments`; lưới TKB `GET /v1/timetable/class/{classId}` và `/teacher/{teacherId}` dạng thứ (2–7) × tiết (1–10); tạo tiết `POST /v1/timetable/slots` hiển thị lỗi trùng lịch từ backend.

- **B5. Xét lên lớp (3–4 ngày, phụ thuộc B2 + B3).** `GET /v1/promotions/class/{classId}/preview` → đề xuất tự động (điểm + hạnh kiểm + chuyên cần), cho ghi đè, `POST /v1/promotions/confirm` lưu hàng loạt. Trang cấu hình ngưỡng (`PromotionThresholdConfig`). Khi B0 xong, đồng bộ để preview dùng xếp loại học lực chính thức.

**DoD:** GVBM nhập điểm TT22, hệ thống tính ĐTB **và xếp loại**; GVCN đánh giá hạnh kiểm; xem/tạo TKB; chạy xét lên lớp end-to-end trên dữ liệu mẫu.

### GIAI ĐOẠN C — Cổng phụ huynh, Sổ liên lạc & Tự phục vụ (2–3 tuần)

- **C1.** Liên kết Phụ huynh–Học sinh (2 ngày): `POST/DELETE /v1/parents/{parentId}/children/{studentId}`, `GET /v1/parents/{parentId}/children` + tạo tài khoản PARENT qua `POST /v1/users`.
- **C2.** Sổ liên lạc / Thông báo (4–5 ngày): soạn & gửi `POST /v1/notifications` (targetType CLASS/STUDENT/ALL_PARENTS/STAFF; channel APP/EMAIL — **SMS/ZALO hiện báo "chưa khả dụng"**, xem E1); "Thông báo của tôi" `GET /v1/notifications/my`, đánh dấu đã đọc.
- **C3.** Cổng tự phục vụ HS & PH (5–6 ngày): layout/route riêng theo role STUDENT/PARENT — xem điểm, điểm danh + %, học phí + công nợ, sổ liên lạc; PARENT chọn giữa các con đã liên kết. Thêm mục điều hướng riêng cho 2 vai trò trong `navigation.js`.

**DoD:** phụ huynh xem đúng dữ liệu **con mình**; nhà trường gửi được thông báo APP/EMAIL và người nhận thấy trong "Thông báo của tôi".

### GIAI ĐOẠN D — Tuyển sinh, Báo cáo, Tài liệu, Vận hành người dùng (2–3 tuần)

- **D1.** Tuyển sinh (4–5 ngày): form nộp hồ sơ **công khai** `POST /v1/admissions` + trang ADMIN duyệt + `POST /v1/admissions/{id}/approve-and-create`; thông báo rate-limit thân thiện.
- **D2.** Nút tải báo cáo (2–3 ngày): học bạ PDF (`/v1/reports/student/{id}/transcript`), điểm danh Excel (`/v1/reports/class/{id}/attendance`), biên lai PDF (`/v1/reports/fees/receipt/{feeId}`) — xử lý tải blob + tên file tiếng Việt.
- **D3.** Upload/xem tài liệu đính kèm (3 ngày): component upload dùng lại (multipart ≤10MB), gắn hồ sơ HS/giáo viên (`/v1/documents`).
- **D4.** Trang Nhật ký hoạt động đầy đủ (1–2 ngày): `/v1/audit-logs` phân trang + lọc actor/entity/thời gian (ADMIN).
- **D5.** Trang Quên/Đặt lại mật khẩu (1–2 ngày): `/forgot-password` + `/reset-password?token=` khớp `FRONTEND_RESET_PASSWORD_URL`; link ở LoginPage.
- **D6.** Trang Quản lý tài khoản người dùng (2 ngày): ADMIN tạo/khoá tài khoản với role tuỳ chọn (`POST /v1/users`).

**DoD:** phụ huynh nộp hồ sơ online → ADMIN duyệt → tài khoản HS tạo tự động; tải học bạ PDF & điểm danh Excel đúng dấu tiếng Việt; đặt lại mật khẩu qua email end-to-end.

### GIAI ĐOẠN E — Nâng cấp tính năng (theo ngân sách/nhu cầu, 3–5 tuần)

- **E1.** Kênh SMS & Zalo OA thật (1–1.5 tuần): hiện thực `SmsNotificationSender` (eSMS/FPT SMS) & `ZaloOaNotificationSender` (Zalo OA) đang là stub throw. **Cần chốt nhà cung cấp + ngân sách.** Cấu hình qua `.env`, retry/log, trạng thái gửi.
- **E2.** Cổng thanh toán học phí (1.5–2 tuần): VNPay/Momo/chuyển khoản (tạo yêu cầu + webhook xác nhận + đối soát `Fee.paidAmount`); **đóng theo đợt/trả góp**; **danh mục khoản thu VN** (học phí, BHYT, bán trú, đồng phục, quỹ lớp); miễn giảm có lý do.
- **E3.** Điểm danh theo tiết (1 tuần): mở rộng theo tiết/môn (quan trọng với THPT) bên cạnh theo ngày; liên kết TKB (B4).
- **E4.** Hồ sơ sức khoẻ học đường (3–4 ngày): tiêm chủng, dị ứng, bệnh mãn tính, khám định kỳ.
- **E5.** Chuyển trường đi/đến (3–4 ngày): luồng `StudentStatus.TRANSFERRED` + lịch sử quá trình học.

### GIAI ĐOẠN F — Vận hành, bảo mật nâng cao & CI/CD (song song, ~1–1.5 tuần)

- **F1.** CI/CD: mở rộng `.github/workflows/build.yml` chạy `mvn test` (Testcontainers MySQL) + `npm build/test`, chặn merge khi fail; thêm secret scanning (gitleaks).
- **F2.** Đóng gói/triển khai: `docker-compose` prod (MySQL + backend + frontend nginx), env qua `.env` không commit, healthcheck.
- **F3.** Bảo mật nâng cao: cân nhắc token cookie `HttpOnly` + CSRF; account lockout sau N lần sai; 2FA cho ADMIN/PRINCIPAL; rà `@PreAuthorize` các thao tác nhạy cảm đã ghi audit.
- **F4.** Observability & sao lưu: actuator health/metrics (đang `show-actuator: false`), log tập trung, backup MySQL định kỳ + kiểm thử phục hồi.
- **F5.** Hiệu năng: xác nhận `DataTable` dùng **server-side pagination** (backend đã hỗ trợ `page/size` + `X-Total-Count`) thay vì tải toàn bộ; cache danh mục ít đổi (môn học, năm học).

---

## PHẦN D — LỊCH TRÌNH TỔNG HỢP

| Tuần | Trọng tâm | Kết quả bàn giao |
|---|---|---|
| 1 | Giai đoạn A | Bỏ Bootstrap, route guard, refresh token, test khung |
| 2–3 | B0 + B1 + B2 | Xếp loại học lực TT22; nhập điểm TT22 + bảng tổng hợp |
| 4 | B3 + B4 | Hạnh kiểm; lưới TKB |
| 5 | B5 | Quy trình cuối năm end-to-end |
| 6–7 | C1–C3 | Cổng phụ huynh + thông báo APP/EMAIL |
| 8–9 | D1–D6 | Hệ thống *dùng được đầy đủ* cho một trường |
| 10–13 | E1–E5 | Tính năng nâng cấp theo ngân sách |
| song song | F1–F5 | Sẵn sàng chạy thật |

**Ước lượng:** ~**9 tuần** để dùng được đầy đủ (hết Giai đoạn D, gồm B0 hoàn tất xếp loại học lực); +**3–5 tuần** cho nâng cấp E/F. Ngắn hơn nhiều so với 20 tuần kế hoạch cũ vì backend đã xong phần lớn.

---

## PHẦN E — QUYẾT ĐỊNH CẦN CHỐT

1. **Mô hình điểm:** bỏ hẳn `Grade` cũ, chỉ dùng `GradeRecord` (TT22)? (ảnh hưởng B2 + migration `V11`).
2. **Bảng ngưỡng xếp loại học lực TT22/TT58 + điều kiện môn Toán/Ngữ văn + quy định nghỉ học tối đa** — **chặn B0 và B5**, phải do người chuyên môn giáo dục xác nhận. Đây là hạng mục *chặn* quan trọng nhất hiện nay.
3. **Kênh liên lạc:** nhà cung cấp SMS (eSMS/FPT) + Zalo OA + ngân sách (E1).
4. **Cổng thanh toán:** VNPay/Momo/khác + tài khoản merchant (E2).
5. **Bảo mật token:** giữ `localStorage` hay chuyển cookie `HttpOnly` (F3, nên chốt sớm vì ảnh hưởng toàn bộ tầng auth frontend).
6. **`AuthResponse.permissions`:** bổ sung field permissions hay bỏ hẳn `hasPermission` và phân quyền UI theo `role` (A8).

---

## PHẦN F — RỦI RO

- **Nghiệp vụ điểm/xếp loại:** sai một ngưỡng là sai học bạ cả trường → bắt buộc review chuyên môn + unit test đối chiếu tay (B0).
- **Hai hệ thống điểm:** phải dứt điểm chọn `GradeRecord` ở B2, nếu không dữ liệu điểm phân mảnh.
- **Phụ thuộc bên ngoài (SMS/Zalo/thanh toán):** cần tài khoản doanh nghiệp + chi phí + thời gian duyệt → đăng ký sớm.
- **Bảo mật frontend:** chưa gỡ log lộ token (A2) + chưa route-guard (A3) thì **chưa đưa ra môi trường thật**.
- **Tài liệu lệch code:** giữ kỷ luật cập nhật Swagger + archive tài liệu cũ để tránh hiểu nhầm hiện trạng.

---

## PHẦN G — PHÂN QUYỀN THEO VAI TRÒ (RBAC): HIỆN TRẠNG

*Trích trực tiếp từ `@PreAuthorize` trên toàn bộ 26 controller + `SecurityConfig` (148 endpoint). 7 vai trò: `ADMIN, PRINCIPAL, TEACHER, STUDENT, PARENT, LIBRARIAN, ACCOUNTANT`. Cơ chế: JWT stateless; **công khai (không cần đăng nhập)**: `/v1/auth/**` và `POST /v1/admissions`; mọi endpoint khác `authenticated()`; phân quyền chi tiết bằng `@PreAuthorize` theo vai trò; riêng STUDENT/PARENT còn bị `StudentAccessGuard` thu hẹp về đúng bản thân/con mình ở tầng service. `enum Permission` + bảng `UserPermission` đã có nhưng **chưa dùng** (chưa phân quyền theo permission mịn).*

### G.1. Phạm vi mỗi vai trò (trên tổng 148 endpoint)

| Vai trò | Số quyền | Tính chất |
|---|---|---|
| **ADMIN** | 145 | Toàn quyền (chỉ thiếu 3 endpoint self-service thư viện vì admin không phải người mượn) |
| **TEACHER** | 79 | Giảng dạy + nhập liệu học tập (điểm, điểm danh, hạnh kiểm) |
| **PRINCIPAL** | 76 | Quản trị cơ cấu + hành chính học vụ (nhưng **không xem dữ liệu học tập**) |
| **STUDENT** | 50 | Tự phục vụ (dữ liệu của mình) + thư viện |
| **PARENT** | 37 | Theo dõi con |
| **ACCOUNTANT** | 20 | Chỉ học phí |
| **LIBRARIAN** | 18 | Chỉ thư viện |

### G.2. Chi tiết từng vai trò được làm gì

- **ADMIN** — mọi chức năng mọi module. Độc quyền: quản lý tài khoản (`POST /v1/users`), cấu hình hệ số điểm (`/v1/grade-config`), nhật ký hoạt động (`/v1/audit-logs`), duyệt tuyển sinh.
- **PRINCIPAL (Hiệu trưởng)** — CÓ: dashboard; **CRUD** lớp/học sinh/nhân sự/môn học/học kỳ/năm học/phân công/thời khoá biểu; xét lên lớp (**confirm**) + ngưỡng xét; tạo thông báo; tài liệu (upload/xem/xoá). **KHÔNG:** điểm (cả 2 model), điểm danh, hạnh kiểm, học phí, báo cáo, thư viện, cấu hình điểm, audit, tuyển sinh, quản lý user → **mù dữ liệu học tập (G.4 mục 2).**
- **TEACHER (Giáo viên)** — **nhập & sửa** điểm (cũ + TT22), điểm danh, hạnh kiểm; **đọc** cơ cấu (năm học/HK/môn/lớp/HS/nhân sự/phân công/TKB); xem trước xét lên lớp (**chỉ preview**); tạo thông báo; thư viện (tự mượn/trả + xem sách); tài liệu (upload/xem); tải học bạ + Excel điểm danh. **KHÔNG:** tạo/sửa/xoá cơ cấu, học phí, cấu hình điểm, **confirm** xét lên lớp, tuyển sinh, audit, user, dashboard.
- **STUDENT (Học sinh)** — (đã guard về chính mình) điểm/điểm danh/học phí/hạnh kiểm/học bạ/xét lên lớp **của mình**; **đóng học phí** của mình; thư viện (mượn/trả/tìm/xem sách + lịch sử của mình); tài liệu; thông báo của mình. **⚠️ Bất thường (chưa guard):** đọc được hồ sơ **mọi** học sinh (`GET /v1/students/{id}`, `/roll/...`) và **toàn bộ danh bạ nhân sự** (`GET /v1/staff*`) — G.4 mục 1 & 3.
- **PARENT (Phụ huynh)** — (đã guard về đúng con) điểm/điểm danh/hạnh kiểm/học phí/học bạ/biên lai/xét lên lớp **của con**; **đóng học phí** cho con; danh sách con; tài liệu; thông báo của mình. **KHÔNG:** mọi quản lý; thư viện; danh bạ HS/nhân sự.
- **LIBRARIAN (Thư viện)** — CRUD sách + xem **toàn bộ giao dịch đang mượn của trường**. **KHÔNG:** tự mượn/trả (self-service dành cho người mượn thật), **và chưa có luồng "ghi mượn/trả hộ"** (G.4 mục 5); mọi module khác.
- **ACCOUNTANT (Kế toán)** — CRUD học phí + xử lý thanh toán + danh sách theo năm/trạng thái + công nợ + biên lai. **KHÔNG:** **đọc danh sách học sinh** để biết thu của ai (G.4 mục 6); mọi module khác.

*(Ma trận đầy đủ 148 endpoint × 7 vai trò xem **Phụ lục A** cuối tài liệu.)*

### G.3. Lưu ý về `StudentAccessGuard`

Dù bảng phân quyền đánh dấu STUDENT/PARENT được vào endpoint điểm/điểm danh/học phí/hạnh kiểm/xét lên lớp, **dữ liệu trả về đã được lọc về đúng người đó** nhờ `StudentAccessGuard` (STUDENT chỉ của mình, PARENT chỉ của con). **Ngoại lệ chưa guard:** `GET /v1/students/{id}`, `/roll`, `/staff*` → xem G.4.

### G.4. Bất thường & lỗ hổng phân quyền (đã xác minh trên code)

1. **✅ [ĐÃ VÁ — v4.4] IDOR hồ sơ học sinh.** `GET /v1/students/{id}` & `/roll/{rollNumber}` giờ inject `StudentAccessGuard` + nhận `Authentication` + gọi `enforceCanAccessStudent` (STUDENT chỉ của mình, PARENT chỉ của con). Đã thêm PARENT vào danh sách vai trò. *Còn lại: viết test khẳng định STUDENT gọi id người khác → 403.*
2. **[Thiết kế — CHƯA] Hiệu trưởng mù dữ liệu học tập.** Không xem được điểm/điểm danh/hạnh kiểm/học phí/báo cáo. `navigation.js` vẫn loại PRINCIPAL khỏi Attendance/Grades/Conduct vì mọi request 403. → Bổ sung quyền **đọc** cho PRINCIPAL (H.2.1).
3. **✅ [ĐÃ VÁ — v4.5] Danh bạ nhân sự.** STUDENT đã bị loại khỏi `GET /v1/staff*` (nay ADMIN/PRINCIPAL/TEACHER) **và** `StaffController.redactSensitiveFields()` null hoá `salary` + địa chỉ + liên hệ khẩn cho mọi vai trò ≠ ADMIN/PRINCIPAL (hiện chỉ TEACHER). Có test `StaffIntegrationTest` (ADMIN thấy đủ / TEACHER bị redact, cả GET đơn lẫn danh sách).
4. **[Rà service] Endpoint chỉ `authenticated()`:** `PUT /v1/notifications/{recipientId}/read` và `POST /v1/documents` → cần service kiểm tra chủ sở hữu.
5. **[Chức năng] Thư viện thiếu luồng mượn/trả hộ** (borrow/return chỉ TEACHER,STUDENT). → Thêm endpoint cho LIBRARIAN.
6. **[Chức năng] Kế toán không đọc được danh sách học sinh.** → Cân nhắc cho ACCOUNTANT đọc (read-only) HS.
7. **[Frontend] Chưa chặn route theo vai trò** (A3) — backend chặn nhưng UI vẫn cho bấm rồi mới 403.

> `Permission` enum + `UserPermission` **đã định nghĩa nhưng chưa dùng** — hệ thống phân quyền theo `Role`. Xem hướng xử lý ở H.3.2.

---

## PHẦN H — ĐỀ XUẤT MA TRẬN PHÂN QUYỀN MỤC TIÊU

*Đề xuất để duyệt trước khi sửa `@PreAuthorize`/service. **Nguyên tắc:** least privilege; tách "xem" và "sửa" (lãnh đạo xem toàn cảnh, giáo viên nhập liệu); dữ liệu cá nhân phải guard nhất quán; không lộ dữ liệu nhạy cảm chéo vai trò; endpoint chỉ `authenticated()` phải kiểm tra chủ sở hữu ở service.*

### H.1. MỨC 1 — PHẢI SỬA (bảo mật, trong Giai đoạn A / mục A9)

| # | Endpoint | Hiện tại | Đề xuất | Lý do |
|---|---|---|---|---|
| 1 | `GET /v1/students/{id}`, `/roll/{roll}` | ~~ADMIN, PRINCIPAL, TEACHER, STUDENT (không guard)~~ | ✅ **ĐÃ LÀM (v4.4)**: thêm PARENT + áp `StudentAccessGuard` | Vá IDOR hồ sơ học sinh — *chỉ còn thiếu test* |
| 2 | `GET /v1/staff`, `/staff/{id}`, `/employee/{id}` | ~~+ STUDENT; TEACHER thấy lương~~ | ✅ **XONG (v4.5)**: bỏ STUDENT + `StaffController.redactSensitiveFields()` null hoá `salary`/`address`/`city`/`state`/`postalCode`/`emergencyContact*` cho vai trò ≠ ADMIN/PRINCIPAL; có test | Đã bịt |
| 3 | `PUT /v1/notifications/{recipientId}/read` | 🔓 authenticated | Giữ authenticated + **service kiểm tra `recipientId` thuộc người gọi** | Tránh IDOR |
| 4 | `POST /v1/documents` | 🔓 authenticated | Giữ authenticated + **service kiểm tra quyền với `ownerType/ownerId`** | Tránh gắn tài liệu vào hồ sơ người khác |

### H.2. MỨC 2 — NÊN SỬA (nghiệp vụ, Giai đoạn B–D)

- **2.1. PRINCIPAL — thêm quyền XEM (chỉ đọc):** thêm `PRINCIPAL` vào các GET của điểm danh, điểm (cũ + TT22), hạnh kiểm, học phí, báo cáo (học bạ/điểm danh/biên lai), thư viện; tuỳ chọn audit log. **Không** thêm vào POST/PUT/DELETE (giữ việc nhập cho TEACHER, học phí cho ACCOUNTANT).
- **2.2. ACCOUNTANT — thêm đọc học sinh:** thêm ACCOUNTANT (read-only) vào `GET /v1/students`, `/students/{id}`; tuỳ chọn `GET /v1/classes`.
- **2.3. LIBRARIAN — luồng mượn/trả hộ:** thêm `POST /v1/library/books/{bookId}/lend?studentId=` và `.../return-for?studentId=` (ADMIN, LIBRARIAN); giữ self-service borrow/return.
- **2.4. Tách thanh toán học phí:** `POST /v1/fees/{feeId}/payment` (ghi sổ thủ công) → **chỉ ADMIN, ACCOUNTANT**; thêm `POST /v1/fees/{feeId}/pay-online` (khởi tạo cổng thanh toán, tiền vào qua webhook) → STUDENT, PARENT (gắn E2).
- **2.5. PARENT — hồ sơ con** (đã gộp ở Mức 1 #1); tuỳ chọn cho PARENT xem sách thư viện.

### H.3. MỨC 3 — NÂNG CAO (dài hạn)

- **3.1. Thu hẹp quyền giáo viên theo phân công:** GVBM chỉ nhập điểm môn mình dạy (có `TeachingAssignment`), GVCN chỉ hạnh kiểm lớp mình, điểm danh chỉ lớp có tiết. Thay đổi logic ở service — làm sau khi phân công (B4) ổn định; bật dần (cảnh báo trước, chặn sau).
- **3.2. Quyết định `Permission`/`UserPermission`:** (a) **bỏ** nếu chỉ cần phân quyền theo `Role` (khuyến nghị) + sửa `hasPermission` ở FE; hoặc (b) **kích hoạt** phân quyền mịn (trả `permissions` trong `AuthResponse`, dùng `hasAuthority`).
- **3.3. Bảo mật tài khoản nâng cao** (đồng bộ F3): account lockout, 2FA cho ADMIN/PRINCIPAL, cookie `HttpOnly`.

### H.4. Ma trận mục tiêu (tóm tắt sau khi áp Mức 1–2)

| Nhóm chức năng | ADMIN | PRINCIPAL | TEACHER | STUDENT | PARENT | LIBRARIAN | ACCOUNTANT |
|---|:--:|:--:|:--:|:--:|:--:|:--:|:--:|
| Cơ cấu (năm học/HK/môn/lớp/nhân sự/phân công/TKB) | CRUD | CRUD | đọc | – | – | – | – |
| Học sinh — quản lý | CRUD | CRUD | đọc | *của mình* | *của con* | – | **đọc** |
| Điểm (TT22) — nhập / xem | ✔ / ✔ | – / **✔** | ✔ / ✔ | – / *mình* | – / *con* | – | – |
| Điểm danh — nhập / xem | ✔ / ✔ | – / **✔** | ✔ / ✔ | – / *mình* | – / *con* | – | – |
| Hạnh kiểm — nhập / xem | ✔ / ✔ | – / **✔** | ✔ / ✔ | – / *mình* | – / *con* | – | – |
| Xét lên lớp (preview / confirm) | ✔ / ✔ | ✔ / ✔ | ✔ / – | *mình* | *con* | – | – |
| Học phí — quản lý / xem | ✔ / ✔ | – / **✔** | – | – / *mình* | – / *con* | – | ✔ / ✔ |
| Đóng học phí online | – | – | – | *mình* | *con* | – | ghi sổ |
| Thư viện — quản lý / mượn-trả | ✔ / – | *xem* | – / *tự* | – / *tự* | *(tuỳ chọn xem)* | ✔ / **hộ** | – |
| Báo cáo | ✔ | **✔** | học bạ+điểm danh | *của mình* | *của con* | – | biên lai |
| Thông báo — gửi / nhận | ✔ / ✔ | ✔ / ✔ | ✔ / ✔ | – / ✔ | – / ✔ | – / ✔ | – / ✔ |
| Tài liệu | ✔ | ✔ | ✔ | *của mình* | *của con* | – | – |
| Cấu hình điểm / Quản lý user / Tuyển sinh | ✔ | *(audit tuỳ chọn)* | – | – | – | – | – |

*In nghiêng* = bị `StudentAccessGuard` giới hạn về mình/con. **In đậm** = quyền **mới bổ sung**.

### H.5. Thứ tự triển khai

1. **Ngay (A9):** Mức 1 #1–#4 + test: STUDENT gọi `GET /v1/students/{idNgườiKhác}` → **403**.
2. **Giai đoạn B–D:** Mức 2.1 (PRINCIPAL xem), 2.2 (kế toán đọc HS), 2.3 (thư viện mượn/trả hộ).
3. **Giai đoạn E2:** Mức 2.4 (tách thanh toán online / ghi sổ).
4. **Dài hạn:** Mức 3.

> Mỗi thay đổi `@PreAuthorize` phải **cập nhật đồng thời** integration test tương ứng và `config/navigation.js` (menu theo vai trò) để backend và UI không lệch.

---

## PHỤ LỤC A — MA TRẬN ĐẦY ĐỦ ENDPOINT × VAI TRÒ

Ký hiệu: **✅** được phép · **🌐** công khai (không cần đăng nhập) · **🔓** mọi user đã đăng nhập · *(trống)* = bị chặn (403). ⚠️ = endpoint có rủi ro phân quyền (xem G.4).

| Chức năng | Endpoint | ADMIN | PRINCIPAL | TEACHER | STUDENT | PARENT | LIBRARIAN | ACCOUNTANT |
|---|---|:--:|:--:|:--:|:--:|:--:|:--:|:--:|
| Xác thực | `POST /v1/auth/login,register,refresh-token,forgot/reset-password` | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 |
| Quản lý tài khoản | `POST /v1/users` | ✅ | | | | | | |
| Dashboard | `GET /v1/dashboard/stats` | ✅ | ✅ | | | | | |
| Nhật ký | `GET /v1/audit-logs` | ✅ | | | | | | |
| Năm học | `POST,PUT,DELETE /v1/academic-years`, `/{id}/close` | ✅ | ✅ | | | | | |
| Năm học | `GET /v1/academic-years`, `/{id}` | ✅ | ✅ | ✅ | | | | |
| Học kỳ | `POST,PUT,DELETE /v1/semesters` | ✅ | ✅ | | | | | |
| Học kỳ | `GET /v1/semesters`, `/{id}`, `/academic-year/{id}` | ✅ | ✅ | ✅ | | | | |
| Môn học | `POST,PUT,DELETE /v1/subjects` | ✅ | ✅ | | | | | |
| Môn học | `GET /v1/subjects`, `/{id}` | ✅ | ✅ | ✅ | | | | |
| Lớp học | `POST,PUT,DELETE /v1/classes`, `/{id}/teacher/{s}`, `/year/{y}` | ✅ | ✅ | | | | | |
| Lớp học | `GET /v1/classes`, `/{id}`, `/{id}/students` | ✅ | ✅ | ✅ | | | | |
| Học sinh | `POST,PUT,DELETE /v1/students`, `/active`, `/class/**` | ✅ | ✅ | *đọc lớp* | | | | |
| Học sinh | `GET /v1/students` | ✅ | ✅ | ✅ | | | | |
| Học sinh | `GET /v1/students/{id}`, `/roll/{roll}` ⚠️ | ✅ | ✅ | ✅ | ✅ | | | |
| Nhân sự | `POST,PUT,DELETE /v1/staff`, `/position/**`, `/department/**`, `/active` | ✅ | ✅ | | | | | |
| Nhân sự | `GET /v1/staff`, `/{id}`, `/employee/{id}` ⚠️ | ✅ | ✅ | ✅ | ✅ | | | |
| Phân công GD | `POST,PUT,DELETE /v1/teaching-assignments` | ✅ | ✅ | | | | | |
| Phân công GD | `GET /v1/teaching-assignments`, `/{id}` | ✅ | ✅ | ✅ | | | | |
| Thời khoá biểu | `POST,PUT,DELETE /v1/timetable/slots` | ✅ | ✅ | | | | | |
| Thời khoá biểu | `GET /v1/timetable/class/{id}`, `/teacher/{id}` | ✅ | ✅ | ✅ | | | | |
| Điểm danh | `POST,PUT,DELETE /v1/attendance`, `/class`, `/date/{d}`, `/between` | ✅ | | ✅ | | | | |
| Điểm danh | `GET /v1/attendance/{id}`, `/student/{id}**`, `/percentage` | ✅ | | ✅ | *mình* | *con* | | |
| Điểm (cũ) | `POST,PUT,DELETE /v1/grades`, `/year/{y}` | ✅ | | ✅ | | | | |
| Điểm (cũ) | `GET /v1/grades/{id}`, `/student/{id}**`, `/average**` | ✅ | | ✅ | *mình* | *con* | | |
| Điểm TT22 | `POST,PUT,DELETE /v1/grade-records` | ✅ | | ✅ | | | | |
| Điểm TT22 | `GET /v1/grade-records/{id}`, `/student/{id}/semester,summary,year-summary` | ✅ | | ✅ | *mình* | *con* | | |
| Cấu hình điểm | `POST,PUT,GET,DELETE /v1/grade-config` | ✅ | | | | | | |
| Hạnh kiểm | `POST,PUT /v1/conduct`, `GET /class/{c}/semester/{s}` | ✅ | | ✅ | | | | |
| Hạnh kiểm | `GET /v1/conduct/student/{id}` | ✅ | | ✅ | *mình* | *con* | | |
| Xét lên lớp | `GET /v1/promotions/class/{id}/preview` | ✅ | ✅ | ✅ | | | | |
| Xét lên lớp | `POST /v1/promotions/confirm` | ✅ | ✅ | | | | | |
| Xét lên lớp | `GET /v1/promotions/student/{id}` | ✅ | ✅ | ✅ | *mình* | *con* | | |
| Ngưỡng xét | `POST,PUT,GET,DELETE /v1/promotion-thresholds` | ✅ | ✅ | | | | | |
| Học phí | `POST,PUT,DELETE /v1/fees`, `/status/{s}`, `/year/{y}` | ✅ | | | | | | ✅ |
| Học phí | `GET /v1/fees/{id}`, `/student/{id}**`, `POST /{id}/payment` | ✅ | | | *mình* | *con* | | ✅ |
| Thư viện | `POST,PUT,DELETE /v1/library/books`, `GET /transactions` | ✅ | | | | | ✅ | |
| Thư viện | `GET /v1/library/books**` (danh sách/tìm/xem) | ✅ | | ✅ | ✅ | | ✅ | |
| Thư viện | `POST /v1/library/books/{id}/borrow,return`, `GET /transactions/me` | | | ✅ | ✅ | | | |
| Báo cáo | `GET /v1/reports/student/{id}/transcript` | ✅ | | ✅ | *mình* | *con* | | |
| Báo cáo | `GET /v1/reports/class/{id}/attendance` | ✅ | | ✅ | | | | |
| Báo cáo | `GET /v1/reports/fees/receipt/{feeId}` | ✅ | | | *mình* | *con* | | ✅ |
| Tài liệu | `POST /v1/documents` 🔓, `GET /v1/documents**` | ✅ | ✅ | ✅ | ✅ | ✅ | | |
| Tài liệu | `DELETE /v1/documents/{id}` | ✅ | ✅ | | | | | |
| Thông báo | `POST /v1/notifications` | ✅ | ✅ | ✅ | | | | |
| Thông báo | `GET /my`, `PUT /{id}/read` 🔓 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Phụ huynh–HS | `POST,DELETE /v1/parents/{p}/children/{s}` | ✅ | | | | | | |
| Phụ huynh–HS | `GET /v1/parents/{p}/children` | ✅ | | | | ✅ | | |
| Tuyển sinh | `POST /v1/admissions` 🌐 | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 |
| Tuyển sinh | `GET /v1/admissions**`, status, approve-and-create | ✅ | | | | | | |

*In nghiêng (mình/con)* = `StudentAccessGuard` lọc về đúng người gọi/con họ. Các ô ✅ gộp nhiều endpoint cùng quyền để rút gọn.

---

## NHẬT KÝ THAY ĐỔI

- **v4.6 (02/09/2026):** đóng nốt **Giai đoạn A** (A5–A8).
  - **A8** — gỡ hẳn `authService.hasPermission` (dead code: đọc `user.permissions` không tồn tại → luôn `false`). Thêm ghi chú trỏ H.3.2. UI phân quyền theo `role` + `navigation.js`.
  - **A6** — Việt hoá toàn bộ nhãn `NAV_ITEMS` (`config/navigation.js`) + fallback `pageTitleForPath` ("Dashboard"→"Tổng quan"). Không còn nhãn tiếng Anh.
  - **A5** — xoá 4 file 0 byte gốc; `git rm --cached .idea/` (12 file) + `thcsthptTS.iml`; thêm `.gitignore` repo gốc. `backend/uploads` PDF vốn đã không bị track; 2 file tài liệu cũ đã ở `archive/` từ v4.4.
  - **A7** — thêm `config/navigation.test.js` (5 ca). `npm test` xanh **28/28** (5 file); `npm run build` sạch.
  - **DoD Giai đoạn A: ĐẠT.** Việc chặn duy nhất còn lại toàn kế hoạch: **B0** (bảng ngưỡng xếp loại học lực — Quyết định E.2).
- **v4.5 (02/09/2026):** hiện thực xong nhóm bảo mật frontend còn mở của v4.4.
  - **A2** — `authService.login` gỡ hết `console.log`/`console.error` in token/response; `App.jsx handleLogout` xoá thêm `refreshToken`.
  - **A4** — `api.js` thêm refresh-token interceptor: 401 có token & không phải `/v1/auth/*` → 1 lần `POST /v1/auth/refresh-token` (`Authorization: Bearer <refreshToken>`), request đồng thời xếp `pendingQueue` chờ + retry; refresh hỏng → clear + redirect (chặn vòng lặp). Lưu `accessToken`/`refreshToken`/`user` mới.
  - **A3** — `components/auth/ProtectedRoute.jsx` + `rolesForPath()`/`defaultPathForRole()` trong `config/navigation.js`; `App.jsx` bọc mọi route qua `guarded(path, element)`; sai vai trò → `<Navigate>` về trang mặc định của role. Test `ProtectedRoute.test.jsx` (4 ca). `npm test` xanh (23 test / 4 file); `npm run build` sạch.
  - **H.1 #2** — `StaffController`: 3 GET cho TEACHER nhận `Authentication`; `redactSensitiveFields()` null hoá `salary`/`address`/`city`/`state`/`postalCode`/`emergencyContact*` cho vai trò ≠ ADMIN/PRINCIPAL. Thêm 2 test `StaffIntegrationTest` (compile OK; integration test cần MySQL cục bộ / CI để chạy).
  - **Còn chặn:** B0 (xếp loại học lực TT22 — cần bảng ngưỡng, Quyết định E.2). Kế tiếp: A8 (`hasPermission`), gỡ `Grade` cũ.
- **v4.4 (02/09/2026):** re-review sau khi developer đã bắt tay sửa (code đổi Sep 2). Cập nhật trạng thái: **frontend 8→16 trang** (AcademicConfig/Timetable/Conduct/Promotions/Parents/Notifications/Admissions/AuditLog/Forgot-Reset + code-splitting + Skeleton + Vitest); **Bootstrap đã gỡ hẳn**; **GradeManagement chuyển sang TT22**; **IDOR hồ sơ học sinh đã vá** (StudentAccessGuard); **STUDENT bị loại khỏi danh bạ nhân sự**; thêm `GET /v1/users?role=`; `GradeConfig` GET nay cho TEACHER đọc. **Còn mở:** `console.log` lộ token (A2), refresh-token (A4), ProtectedRoute (A3), `hasPermission`/permissions (A8), redact `salary` (H.1#2), xếp loại học lực `null` (B0), gỡ `Grade` cũ. Bổ sung callout "việc gấp nhất" đầu tài liệu.
- **v4.3 (01/09/2026):** gom toàn bộ nội dung phân quyền vào một file. Mở rộng **Phần G** (catalog chi tiết từng vai trò + số quyền + `StudentAccessGuard`); thêm **Phần H — Đề xuất ma trận phân quyền mục tiêu** (3 mức: bảo mật/nghiệp vụ/nâng cao, có ma trận mục tiêu + thứ tự triển khai); thêm **Phụ lục A — ma trận đầy đủ 148 endpoint × 7 vai trò**. Gộp 2 file lẻ (`PHAN_QUYEN_CHI_TIET.md`, `DE_XUAT_PHAN_QUYEN.md`) vào đây.
- **v4.2 (01/09/2026):** thêm **Phần G — Ma trận phân quyền theo vai trò (RBAC)** trích trực tiếp từ `@PreAuthorize` toàn bộ controller; phát hiện & ghi nhận: IDOR hồ sơ học sinh (STUDENT đọc được hồ sơ mọi HS), PRINCIPAL mù dữ liệu học tập, STUDENT đọc toàn bộ danh bạ nhân sự, 2 endpoint chỉ `authenticated()` cần rà chủ sở hữu, thư viện thiếu mượn/trả hộ, kế toán không đọc được danh sách HS. Thêm mục A9 (vá IDOR) vào Giai đoạn A.
- **v4.1 (01/09/2026):** re-review lại repo theo quy trình phase-planning. Sửa: (1) module điểm TT22 — nhập điểm & tính ĐTB **đã xong**, nhưng **xếp loại học lực cố tình chưa code** (`classification = null`), tách thành hạng mục B0; (2) `PromotionService` hiện dùng điểm TB thô chứ không dùng xếp loại chính thức; (3) xác nhận `AuthResponse` **không có** `permissions` → `hasPermission` là dead code (A.3.6, A8); (4) sửa lại đúng đường dẫn endpoint tổng hợp điểm (`/v1/grade-records/.../summary`, `/year-summary`); (5) ghi chú tài liệu repo (`IMPLEMENTATION_PLAN.md`, `PROJECT_ANALYSIS_SUMMARY.md`) đã lỗi thời.
- **v4.0 (01/09/2026):** bản đầu, lập sau khi review toàn bộ mã nguồn; ghi nhận backend đã hiện thực gần hết kế hoạch v3.1, frontend mới phủ ~40%.

*Tài liệu này thay vai trò "kế hoạch hiện hành" cho `IMPLEMENTATION_PLAN.md` (v3.1). Khi hoàn thành từng giai đoạn, cập nhật lại bảng Phần A để phản ánh hiện trạng mới.*
