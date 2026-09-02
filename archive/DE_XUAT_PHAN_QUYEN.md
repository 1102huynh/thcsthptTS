> **⚠️ ĐÃ LƯU TRỮ — đã gộp vào Phần H của `KE_HOACH_NANG_CAP_V4.md` (v4.3).**
> Mục 1-2 (IDOR hồ sơ học sinh + danh bạ nhân sự lộ lương) đã được vá
> (02/09/2026, xem `StudentController`/`StaffController`). Giữ file này chỉ
> để tham khảo bản nháp gốc.

# ĐỀ XUẤT MA TRẬN PHÂN QUYỀN (RBAC) MỤC TIÊU — thcsthptTS

*Ngày 01/09/2026. Dựa trên bản review hiện trạng (`PHAN_QUYEN_CHI_TIET.md`) và các bất thường ở Phần G của `KE_HOACH_NANG_CAP_V4.md`. Đây là **đề xuất để duyệt** trước khi sửa `@PreAuthorize`/service ở backend — chưa đụng code.*

## Nguyên tắc thiết kế phân quyền mục tiêu

1. **Nguyên tắc tối thiểu (least privilege):** mỗi vai trò chỉ có đúng quyền cần cho công việc thực tế trong trường.
2. **Tách "xem" và "sửa":** lãnh đạo (Hiệu trưởng) cần **xem toàn cảnh** nhưng không nhất thiết được **sửa** dữ liệu tác nghiệp (giáo viên nhập).
3. **Dữ liệu cá nhân phải được guard:** STUDENT chỉ thấy của mình, PARENT chỉ thấy của con — áp `StudentAccessGuard` **nhất quán** cho mọi endpoint theo học sinh, không sót.
4. **Không lộ dữ liệu nhạy cảm chéo vai trò:** học sinh không xem hồ sơ học sinh khác; không thấy lương giáo viên.
5. **Endpoint không có `@PreAuthorize` (chỉ authenticated) phải có kiểm tra chủ sở hữu ở service.**

---

## MỨC 1 — PHẢI SỬA (bảo mật, làm trong Giai đoạn A)

| # | Endpoint | Hiện tại | Đề xuất | Lý do |
|---|---|---|---|---|
| 1 | `GET /v1/students/{id}`, `GET /v1/students/roll/{roll}` | ADMIN, PRINCIPAL, TEACHER, **STUDENT** (không guard) | Giữ ADMIN, PRINCIPAL, TEACHER; **thêm PARENT**; và **áp `StudentAccessGuard`** để STUDENT chỉ đọc hồ sơ **của chính mình**, PARENT chỉ **của con** | **Vá IDOR:** hiện học sinh đọc được hồ sơ đầy đủ của mọi HS khác. Truyền `Authentication` vào controller → gọi `guard.enforceCanAccessStudent(id, requester)` như grades/fees đã làm |
| 2 | `GET /v1/staff`, `/staff/{id}`, `/staff/employee/{id}` | ADMIN, PRINCIPAL, TEACHER, **STUDENT** | Bỏ **STUDENT** khỏi danh bạ nhân sự đầy đủ; nếu cần cho HS xem giáo viên thì tạo DTO rút gọn (họ tên, môn, không lương/lương/SĐT cá nhân) | Học sinh đang xem được **toàn bộ danh bạ nhân sự**. Đồng thời rà `StaffDTO` **không trả `salary`/thông tin nhạy cảm** cho vai trò không phải ADMIN/PRINCIPAL |
| 3 | `PUT /v1/notifications/{recipientId}/read` | 🔓 chỉ authenticated | Giữ authenticated **nhưng service kiểm tra `recipientId` thuộc về người gọi** (403 nếu không) | Tránh IDOR: bất kỳ ai đánh dấu đã đọc thông báo của người khác |
| 4 | `POST /v1/documents` | 🔓 chỉ authenticated | Giữ authenticated **nhưng service kiểm tra người gọi có quyền đính kèm cho `ownerType/ownerId`** (VD: HS chỉ đính kèm hồ sơ của mình; GV cho lớp mình) | Tránh tuỳ tiện gắn tài liệu vào hồ sơ người khác |

---

## MỨC 2 — NÊN SỬA (nghiệp vụ, làm trong Giai đoạn B–D)

### 2.1. Hiệu trưởng (PRINCIPAL) — bổ sung quyền **XEM** dữ liệu học tập

Hiệu trưởng phải xem được toàn cảnh (chỉ đọc), giữ quyền **sửa cho giáo viên**. Thêm `PRINCIPAL` vào các endpoint GET:

| Nhóm | Endpoint GET thêm PRINCIPAL | Ghi chú |
|---|---|---|
| Điểm danh | `/v1/attendance/**` (tất cả GET) | Chỉ xem, không thêm vào POST/PUT/DELETE |
| Điểm (cũ + TT22) | `/v1/grades/**`, `/v1/grade-records/**` (các GET) | Xem điểm & bảng tổng hợp toàn trường |
| Hạnh kiểm | `/v1/conduct/student/{id}`, `/v1/conduct/class/{c}/semester/{s}` | Xem đánh giá |
| Học phí | `/v1/fees/**` các GET (`/{id}`, `/student/{id}`, `/year/{y}`, `/status/{s}`, `total-dues`, `pending`) | Xem công nợ toàn trường (không thêm vào POST/PUT/DELETE/payment) |
| Báo cáo | `/v1/reports/**` (transcript, class attendance, receipt) | Tải mọi báo cáo |
| Thư viện | `/v1/library/books/**` (GET), `/v1/library/transactions` | Xem tình hình mượn (tuỳ chọn) |
| Nhật ký | `GET /v1/audit-logs` | **Tuỳ chọn** — cân nhắc cho PRINCIPAL xem audit hay giữ riêng ADMIN |

> **Không** thêm PRINCIPAL vào: nhập/sửa điểm-điểm danh-hạnh kiểm (việc của TEACHER), tạo/sửa/xoá học phí (ACCOUNTANT), cấu hình hệ số điểm và quản lý tài khoản (ADMIN).

### 2.2. Kế toán (ACCOUNTANT) — bổ sung quyền đọc học sinh

| Endpoint | Hiện tại | Đề xuất | Lý do |
|---|---|---|---|
| `GET /v1/students`, `GET /v1/students/{id}` | không có ACCOUNTANT | **Thêm ACCOUNTANT (chỉ đọc)** | Kế toán cần tra cứu học sinh để lập/đối chiếu khoản thu; hiện không đọc được danh sách HS |
| `GET /v1/classes` (danh sách lớp) | không có ACCOUNTANT | **Thêm ACCOUNTANT (chỉ đọc)** — tuỳ chọn | Lọc học phí theo lớp/khối |

### 2.3. Thư viện (LIBRARIAN) — bổ sung luồng mượn/trả hộ

| Endpoint mới | Quyền | Mô tả |
|---|---|---|
| `POST /v1/library/books/{bookId}/lend` (param `studentId`/`memberId`) | ADMIN, LIBRARIAN | Thủ thư **ghi mượn hộ** cho một học sinh/giáo viên tại quầy |
| `POST /v1/library/books/{bookId}/return-for` (param `studentId`) | ADMIN, LIBRARIAN | Thủ thư **ghi trả hộ** |

> Giữ nguyên self-service `borrow`/`return` (TEACHER, STUDENT lấy người mượn từ JWT). Đây là **bổ sung**, không thay thế.

### 2.4. Đóng học phí — tách "ghi nhận thủ công" và "thanh toán online"

| Endpoint | Hiện tại | Đề xuất |
|---|---|---|
| `POST /v1/fees/{feeId}/payment` (ghi nhận thanh toán thủ công vào sổ) | ADMIN, ACCOUNTANT, **STUDENT, PARENT** | **Chỉ ADMIN, ACCOUNTANT** (đây là thao tác ghi sổ, không nên để HS/PH tự ghi) |
| `POST /v1/fees/{feeId}/pay-online` (khởi tạo giao dịch cổng thanh toán) — **mới, thuộc E2** | — | **STUDENT, PARENT** (đã guard về mình/con) → tiền vào qua webhook VNPay/Momo, không tự set `paidAmount` |

> Điều chỉnh này gắn với Giai đoạn E2 (cổng thanh toán). Trước khi có cổng thanh toán, tối thiểu nên bỏ STUDENT/PARENT khỏi endpoint ghi sổ thủ công.

### 2.5. Phụ huynh (PARENT) — hồ sơ con & (tuỳ chọn) thư viện

- `GET /v1/students/{id}` cho PARENT (guard về đúng con) — đã gộp ở Mức 1 #1.
- Cân nhắc cho PARENT **xem sách thư viện** (`GET /v1/library/books/**`) nếu trường muốn phụ huynh tra cứu — tuỳ chọn, không bắt buộc.

---

## MỨC 3 — NÂNG CAO (tuỳ chọn, cân nhắc dài hạn)

### 3.1. Thu hẹp quyền giáo viên theo phân công (data-level scoping)

Hiện **mọi TEACHER nhập/sửa được điểm–điểm danh–hạnh kiểm của bất kỳ lớp/môn nào**, không giới hạn theo phân công. Đề xuất kiểm tra ở tầng service:

- Nhập điểm: chỉ cho phép nếu tồn tại `TeachingAssignment(teacher = current, subject, class, semester)`.
- Nhập/sửa hạnh kiểm: chỉ **GVCN của lớp** (`SchoolClass.classTeacher == current`).
- Điểm danh: chỉ giáo viên có tiết với lớp đó (hoặc GVCN).

> Đây là thay đổi lớn về logic (không chỉ `@PreAuthorize`), nên làm sau khi phân công giảng dạy (B4) ổn định. Có thể bật dần: cảnh báo trước, chặn sau.

### 3.2. Quyết định về `Permission` / `UserPermission`

`enum Permission` (CREATE_STAFF, MANAGE_USERS, VIEW_LOGS...) và bảng `UserPermission` **đã có nhưng chưa dùng**. Hai hướng:

- **(a) Bỏ đi** nếu chỉ cần phân quyền theo `Role` (đơn giản, khuyến nghị cho giai đoạn này) — dọn code chết + sửa `authService.hasPermission` ở frontend (đang luôn trả `false`).
- **(b) Kích hoạt** nếu cần phân quyền mịn (VD: cấp thêm quyền X cho một GV cụ thể) — cần trả `permissions` trong `AuthResponse`, load quyền vào `UserDetails`, dùng `@PreAuthorize("hasAuthority('...')")`. Công sức lớn hơn nhiều.

### 3.3. Bảo mật tài khoản nâng cao (đồng bộ Giai đoạn F3)

- Account lockout sau N lần đăng nhập sai; 2FA cho ADMIN/PRINCIPAL.
- Cân nhắc token cookie `HttpOnly` thay `localStorage`.

---

## MA TRẬN MỤC TIÊU (TÓM TẮT SAU KHI ÁP MỨC 1–2)

| Nhóm chức năng | ADMIN | PRINCIPAL | TEACHER | STUDENT | PARENT | LIBRARIAN | ACCOUNTANT |
|---|:--:|:--:|:--:|:--:|:--:|:--:|:--:|
| Cơ cấu (năm học/HK/môn/lớp/nhân sự/phân công/TKB) | CRUD | CRUD | đọc | – | – | – | – |
| Học sinh — quản lý | CRUD | CRUD | đọc | *của mình* | *của con* | – | **đọc** |
| Điểm (TT22) — nhập | ✔ | – | ✔ | – | – | – | – |
| Điểm — xem | ✔ | **✔ (xem)** | ✔ | *của mình* | *của con* | – | – |
| Điểm danh — nhập / xem | ✔ / ✔ | – / **✔** | ✔ / ✔ | – / *mình* | – / *con* | – | – |
| Hạnh kiểm — nhập / xem | ✔ / ✔ | – / **✔** | ✔ / ✔ | – / *mình* | – / *con* | – | – |
| Xét lên lớp (preview / confirm) | ✔ / ✔ | ✔ / ✔ | ✔ / – | *mình* | *con* | – | – |
| Học phí — quản lý / xem | ✔ / ✔ | – / **✔ (xem)** | – | – / *mình* | – / *con* | – | ✔ / ✔ |
| Đóng học phí (online) | – | – | – | *mình* | *con* | – | ghi sổ |
| Thư viện — quản lý sách | ✔ | *xem* | *mượn/trả* | *mượn/trả* | *(tuỳ chọn xem)* | ✔ + **mượn/trả hộ** | – |
| Báo cáo (học bạ/điểm danh/biên lai) | ✔ | **✔** | ✔ (học bạ, điểm danh) | *của mình* | *của con* | – | biên lai |
| Thông báo — gửi / nhận | ✔ / ✔ | ✔ / ✔ | ✔ / ✔ | – / ✔ | – / ✔ | – / ✔ | – / ✔ |
| Tài liệu đính kèm | ✔ | ✔ | ✔ | *của mình* | *của con* | – | – |
| Tuyển sinh (duyệt) | ✔ | *(tuỳ chọn)* | – | – | – | – | – |
| Cấu hình điểm / Quản lý user / Audit | ✔ | *(audit tuỳ chọn)* | – | – | – | – | – |

*In nghiêng* = bị `StudentAccessGuard` giới hạn về chính mình/con. **In đậm** = quyền **mới bổ sung** so với hiện trạng.

---

## THỨ TỰ TRIỂN KHAI ĐỀ XUẤT

1. **Ngay (Giai đoạn A / A9):** Mức 1 #1–#4 (vá IDOR hồ sơ HS, ẩn lương trong StaffDTO, ownership cho notifications/documents). Kèm test tích hợp: STUDENT gọi `GET /v1/students/{idNgườiKhác}` → **403**.
2. **Giai đoạn B–D:** Mức 2.1 (PRINCIPAL xem), 2.2 (ACCOUNTANT đọc HS), 2.3 (thư viện mượn/trả hộ), 2.5.
3. **Giai đoạn E2:** Mức 2.4 (tách thanh toán online / ghi sổ) khi làm cổng thanh toán.
4. **Dài hạn:** Mức 3 (scoping theo phân công, quyết định Permission, bảo mật nâng cao).

> Mỗi thay đổi `@PreAuthorize` cần **cập nhật đồng thời** integration test tương ứng và `config/navigation.js` ở frontend (hiện thị menu theo vai trò), để backend và UI không lệch.
