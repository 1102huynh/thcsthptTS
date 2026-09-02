> **⚠️ ĐÃ LƯU TRỮ — đã gộp vào Phần G của `KE_HOACH_NANG_CAP_V4.md` (v4.3).**
> Giữ file này chỉ để tham khảo bản nháp gốc.

# CHỨC NĂNG CHI TIẾT THEO TỪNG VAI TRÒ NGƯỜI DÙNG — thcsthptTS

*Ngày 01/09/2026 — trích trực tiếp từ `@PreAuthorize` trên toàn bộ 26 controller + `SecurityConfig` (nguồn sự thật: mã nguồn `D:\sources\thcsthptTS`). Tài liệu này liệt kê **toàn bộ** những gì mỗi vai trò được phép làm ở tầng API. Các bất thường/lỗ hổng phân quyền xem Phần G của `KE_HOACH_NANG_CAP_V4.md`.*

## Cơ chế phân quyền

- **7 vai trò** (`enum Role`): `ADMIN`, `PRINCIPAL` (Hiệu trưởng), `TEACHER` (Giáo viên), `STUDENT` (Học sinh), `PARENT` (Phụ huynh), `LIBRARIAN` (Thư viện), `ACCOUNTANT` (Kế toán).
- Xác thực **JWT stateless**. `SecurityConfig` cho **công khai** (không cần đăng nhập): `/v1/auth/**` và `POST /v1/admissions` (nộp hồ sơ tuyển sinh). Mọi endpoint khác bắt buộc đăng nhập.
- Phân quyền chi tiết bằng `@PreAuthorize` **theo vai trò** trên từng method. `enum Permission` + bảng `UserPermission` đã có nhưng **chưa dùng** (chưa phân quyền theo permission mịn).
- **`StudentAccessGuard`** (tầng service) thu hẹp thêm: khi người gọi là STUDENT → chỉ xem được **hồ sơ/dữ liệu của chính mình**; là PARENT → chỉ của **con mình**. Áp cho điểm, điểm danh, học phí, hạnh kiểm, xét lên lớp. *(Lưu ý: hồ sơ học sinh `GET /v1/students/{id}` KHÔNG được guard — xem lỗ hổng G.2.1.)*

## Tổng quan số chức năng mỗi vai trò (trên tổng 148 endpoint)

| Vai trò | Số endpoint truy cập được | Tính chất |
|---|---|---|
| **ADMIN** | 145/148 | Toàn quyền (chỉ thiếu 3 endpoint self-service thư viện vì admin không phải người mượn) |
| **TEACHER** | 79/148 | Giảng dạy + nhập liệu học tập (điểm, điểm danh, hạnh kiểm) |
| **PRINCIPAL** | 76/148 | Quản trị cơ cấu + hành chính học vụ (nhưng không xem dữ liệu học tập) |
| **STUDENT** | 50/148 | Tự phục vụ (dữ liệu của mình) + thư viện |
| **PARENT** | 37/148 | Theo dõi con |
| **ACCOUNTANT** | 20/148 | Chỉ học phí |
| **LIBRARIAN** | 18/148 | Chỉ thư viện |

*(Con số bao gồm các endpoint công khai + endpoint chỉ cần đăng nhập mà mọi vai trò đều dùng.)*

---

## 1. ADMIN — Quản trị hệ thống (toàn quyền)

Làm được **mọi chức năng** của mọi module. Các quyền **độc quyền chỉ ADMIN có**:

- **Quản lý tài khoản người dùng:** `POST /v1/users` — tạo tài khoản với vai trò tuỳ chọn (đây là cách duy nhất tạo user PRINCIPAL/TEACHER/LIBRARIAN/ACCOUNTANT/PARENT).
- **Cấu hình hệ số điểm:** toàn bộ `/v1/grade-config` (tạo/sửa/xoá/xem hệ số điểm miệng/15'/1 tiết/giữa kỳ/cuối kỳ).
- **Nhật ký hoạt động:** `GET /v1/audit-logs` (xem toàn bộ audit log).
- **Duyệt tuyển sinh:** `GET /v1/admissions`, `PUT /v1/admissions/{id}/status`, `POST /v1/admissions/{id}/approve-and-create`.

Ngoài ra ADMIN có toàn bộ quyền của các vai trò khác (CRUD lớp/HS/nhân sự/môn/năm học/học kỳ/phân công/TKB, điểm, điểm danh, hạnh kiểm, học phí, thư viện quản lý, báo cáo, thông báo, tài liệu, xét lên lớp).
**Ngoại lệ (không có):** `borrow`/`return`/`transactions/me` của thư viện (chỉ dành cho người mượn thật là TEACHER/STUDENT).

## 2. PRINCIPAL — Hiệu trưởng (quản trị cơ cấu & hành chính học vụ)

**Được phép:**

- **Năm học / Học kỳ / Môn học:** toàn bộ CRUD (tạo/sửa/xoá/xem), kể cả "Đóng năm học".
- **Lớp học:** CRUD + gán/đổi giáo viên chủ nhiệm + xem danh sách HS trong lớp.
- **Học sinh:** CRUD (tạo/sửa/xoá/xem/danh sách/theo lớp/active).
- **Nhân sự:** CRUD (tạo/sửa/xoá/xem/danh sách/theo vị trí/phòng ban/active).
- **Phân công giảng dạy & Thời khoá biểu:** CRUD đầy đủ.
- **Xét lên lớp:** xem trước (`preview`) + **quyết định (confirm)** + cấu hình ngưỡng xét (`promotion-thresholds`).
- **Thông báo:** tạo & gửi (`POST /v1/notifications`).
- **Tài liệu đính kèm:** upload/xem/**xoá**.
- **Dashboard:** xem thống kê tổng quan.

**KHÔNG được (đáng chú ý — xem lỗ hổng G.2.2):** điểm (cả model cũ lẫn TT22), điểm danh, hạnh kiểm, học phí, báo cáo (học bạ/điểm danh/biên lai), thư viện, cấu hình điểm, tuyển sinh, nhật ký hoạt động, quản lý tài khoản. → Hiệu trưởng **không xem được một điểm số hay buổi điểm danh nào của học sinh**.

## 3. TEACHER — Giáo viên (giảng dạy & nhập liệu học tập)

**Được phép:**

- **Nhập liệu học tập (tạo/sửa/xoá):**
  - Điểm — cả model cũ (`/v1/grades`) và **TT22** (`/v1/grade-records`): nhập điểm theo loại, xem bảng tổng hợp ĐTB môn HK/cả năm.
  - Điểm danh (`/v1/attendance`): theo ngày, theo lớp (hàng loạt), sửa/xoá, xem % chuyên cần.
  - Hạnh kiểm (`/v1/conduct`): nhập/sửa + bảng đánh giá cả lớp.
- **Đọc (không sửa) dữ liệu cơ cấu:** Năm học, Học kỳ, Môn học, Lớp (+ danh sách HS trong lớp), Học sinh (danh sách + hồ sơ + theo lớp), Nhân sự (danh bạ), Phân công giảng dạy, Thời khoá biểu (của lớp và của giáo viên).
- **Xét lên lớp:** **chỉ xem trước** (`preview`) — không có quyền quyết định (confirm).
- **Thông báo:** tạo & gửi.
- **Thư viện:** xem/tìm sách + **tự mượn/trả** + xem lịch sử mượn của mình.
- **Tài liệu:** upload + xem (không xoá).
- **Báo cáo:** tải học bạ học sinh (PDF) + danh sách điểm danh lớp (Excel).

**KHÔNG được:** tạo/sửa/xoá cơ cấu (năm học/học kỳ/môn/lớp/học sinh/nhân sự/phân công/TKB), học phí (mọi thao tác), cấu hình hệ số điểm, **confirm** xét lên lớp, cấu hình ngưỡng, tuyển sinh, nhật ký, quản lý tài khoản, dashboard, xoá tài liệu, xem giao dịch thư viện toàn trường, tải biên lai học phí.

## 4. STUDENT — Học sinh (tự phục vụ)

**Được phép (dữ liệu của chính mình — `StudentAccessGuard`):**

- Xem **điểm** của mình (model cũ + tổng hợp TT22: summary học kỳ/cả năm).
- Xem **điểm danh** + tỷ lệ chuyên cần của mình.
- Xem **học phí** + công nợ của mình, xem **biên lai**, và **đóng học phí** (`POST /v1/fees/{feeId}/payment`).
- Xem **hạnh kiểm** của mình.
- Xem **kết quả xét lên lớp** của mình.
- Tải **học bạ** của mình (`/v1/reports/student/{id}/transcript`).
- **Thư viện:** xem/tìm sách + **tự mượn/trả** + lịch sử mượn của mình.
- **Tài liệu:** upload + xem.
- **Thông báo:** xem thông báo của mình + đánh dấu đã đọc.

**KHÔNG được:** mọi thao tác quản lý; danh sách điểm/điểm danh toàn trường/theo năm; tạo thông báo; báo cáo lớp; học phí toàn trường.

**⚠️ Bất thường (chưa guard — lỗ hổng cần vá):** hiện STUDENT **đọc được hồ sơ đầy đủ của mọi học sinh khác** (`GET /v1/students/{id}`, `/students/roll/{rollNumber}`) và **toàn bộ danh bạ nhân sự** (`GET /v1/staff*`). Xem G.2 mục 1 & 3.

## 5. PARENT — Phụ huynh (theo dõi con)

**Được phép (dữ liệu của con mình — `StudentAccessGuard` kiểm tra quan hệ):**

- Xem **điểm** của con (model cũ + TT22 summary).
- Xem **điểm danh** + % chuyên cần của con.
- Xem **hạnh kiểm** của con.
- Xem **học phí** + công nợ của con, xem **biên lai**, và **đóng học phí** cho con.
- Xem **kết quả xét lên lớp** của con.
- Tải **học bạ** của con.
- Xem **danh sách con** đã liên kết (`GET /v1/parents/{parentId}/children`).
- **Tài liệu:** upload + xem.
- **Thông báo:** xem thông báo của mình (sổ liên lạc) + đánh dấu đã đọc.

**KHÔNG được:** thư viện (phụ huynh không thuộc nhóm vai trò thư viện — không xem/mượn sách), danh bạ học sinh/nhân sự, mọi thao tác quản lý, tạo thông báo.

## 6. LIBRARIAN — Thư viện

**Được phép:**

- **Sách:** toàn bộ CRUD — tạo/sửa/xoá + xem/tìm kiếm/theo thể loại/theo tác giả/sách còn sẵn.
- **Giao dịch:** xem **toàn bộ giao dịch đang mượn của cả trường** (`GET /v1/library/transactions`).
- Thông báo của mình + tài liệu (theo mặc định authenticated) — không phải chức năng chính.

**KHÔNG được:** tự mượn/trả sách (theo thiết kế self-service, người mượn lấy từ JWT là TEACHER/STUDENT), **và chưa có luồng "thủ thư ghi mượn/trả hộ học sinh"** (lỗ hổng chức năng G.2.5); mọi module khác (học sinh, điểm, điểm danh, học phí, báo cáo...).

## 7. ACCOUNTANT — Kế toán

**Được phép:**

- **Học phí:** toàn bộ CRUD — tạo/sửa/xoá khoản thu + **xử lý thanh toán** + danh sách theo năm học/theo trạng thái + xem công nợ.
- **Biên lai học phí:** tải biên lai PDF (`/v1/reports/fees/receipt/{feeId}`).
- Thông báo của mình.

**KHÔNG được:** **đọc danh sách học sinh** để biết thu của ai (lỗ hổng chức năng G.2.6); mọi module khác (điểm, điểm danh, lớp, nhân sự, thư viện, báo cáo học bạ...).

---

## MA TRẬN ĐẦY ĐỦ: ENDPOINT × VAI TRÒ

Ký hiệu: **✅** được phép · **🌐** công khai (không cần đăng nhập) · **🔓** mọi user đã đăng nhập (chưa phân quyền chi tiết) · *(trống)* = bị chặn (403).

| Chức năng | Endpoint | ADMIN | PRINCIPAL | TEACHER | STUDENT | PARENT | LIBRARIAN | ACCOUNTANT |
|---|---|:--:|:--:|:--:|:--:|:--:|:--:|:--:|
| Xác thực | `POST /v1/auth/login` | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 |
| Xác thực | `POST /v1/auth/register` | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 |
| Xác thực | `POST /v1/auth/refresh-token` | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 |
| Xác thực | `POST /v1/auth/forgot-password` | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 |
| Xác thực | `POST /v1/auth/reset-password` | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 |
| Quản lý tài khoản | `POST /v1/users` | ✅ | | | | | | |
| Dashboard | `GET /v1/dashboard/stats` | ✅ | ✅ | | | | | |
| Nhật ký | `GET /v1/audit-logs` | ✅ | | | | | | |
| Năm học | `POST /v1/academic-years` | ✅ | ✅ | | | | | |
| Năm học | `PUT /v1/academic-years/{id}` | ✅ | ✅ | | | | | |
| Năm học | `PUT /v1/academic-years/{id}/close` | ✅ | ✅ | | | | | |
| Năm học | `GET /v1/academic-years` | ✅ | ✅ | ✅ | | | | |
| Năm học | `GET /v1/academic-years/{id}` | ✅ | ✅ | ✅ | | | | |
| Năm học | `DELETE /v1/academic-years/{id}` | ✅ | ✅ | | | | | |
| Học kỳ | `POST /v1/semesters` | ✅ | ✅ | | | | | |
| Học kỳ | `PUT /v1/semesters/{id}` | ✅ | ✅ | | | | | |
| Học kỳ | `GET /v1/semesters` | ✅ | ✅ | ✅ | | | | |
| Học kỳ | `GET /v1/semesters/{id}` | ✅ | ✅ | ✅ | | | | |
| Học kỳ | `GET /v1/semesters/academic-year/{id}` | ✅ | ✅ | ✅ | | | | |
| Học kỳ | `DELETE /v1/semesters/{id}` | ✅ | ✅ | | | | | |
| Môn học | `POST /v1/subjects` | ✅ | ✅ | | | | | |
| Môn học | `PUT /v1/subjects/{id}` | ✅ | ✅ | | | | | |
| Môn học | `GET /v1/subjects` | ✅ | ✅ | ✅ | | | | |
| Môn học | `GET /v1/subjects/{id}` | ✅ | ✅ | ✅ | | | | |
| Môn học | `DELETE /v1/subjects/{id}` | ✅ | ✅ | | | | | |
| Lớp học | `POST /v1/classes` | ✅ | ✅ | | | | | |
| Lớp học | `PUT /v1/classes/{id}` | ✅ | ✅ | | | | | |
| Lớp học | `GET /v1/classes` | ✅ | ✅ | ✅ | | | | |
| Lớp học | `GET /v1/classes/{id}` | ✅ | ✅ | ✅ | | | | |
| Lớp học | `GET /v1/classes/year/{year}` | ✅ | ✅ | | | | | |
| Lớp học | `GET /v1/classes/{id}/students` | ✅ | ✅ | ✅ | | | | |
| Lớp học | `PUT /v1/classes/{id}/teacher/{staffId}` | ✅ | ✅ | | | | | |
| Lớp học | `DELETE /v1/classes/{id}` | ✅ | ✅ | | | | | |
| Học sinh | `POST /v1/students` | ✅ | ✅ | | | | | |
| Học sinh | `PUT /v1/students/{id}` | ✅ | ✅ | | | | | |
| Học sinh | `GET /v1/students` | ✅ | ✅ | ✅ | | | | |
| Học sinh | `GET /v1/students/{id}` ⚠️ | ✅ | ✅ | ✅ | ✅ | | | |
| Học sinh | `GET /v1/students/roll/{roll}` ⚠️ | ✅ | ✅ | ✅ | ✅ | | | |
| Học sinh | `GET /v1/students/class/{className}` | ✅ | ✅ | ✅ | | | | |
| Học sinh | `GET /v1/students/class/{c}/section/{s}` | ✅ | ✅ | ✅ | | | | |
| Học sinh | `GET /v1/students/active` | ✅ | ✅ | | | | | |
| Học sinh | `DELETE /v1/students/{id}` | ✅ | ✅ | | | | | |
| Nhân sự | `POST /v1/staff` | ✅ | ✅ | | | | | |
| Nhân sự | `PUT /v1/staff/{id}` | ✅ | ✅ | | | | | |
| Nhân sự | `GET /v1/staff` ⚠️ | ✅ | ✅ | ✅ | ✅ | | | |
| Nhân sự | `GET /v1/staff/{id}` ⚠️ | ✅ | ✅ | ✅ | ✅ | | | |
| Nhân sự | `GET /v1/staff/employee/{empId}` ⚠️ | ✅ | ✅ | ✅ | ✅ | | | |
| Nhân sự | `GET /v1/staff/position/{position}` | ✅ | ✅ | | | | | |
| Nhân sự | `GET /v1/staff/department/{dept}` | ✅ | ✅ | | | | | |
| Nhân sự | `GET /v1/staff/active` | ✅ | ✅ | | | | | |
| Nhân sự | `DELETE /v1/staff/{id}` | ✅ | ✅ | | | | | |
| Phân công GD | `POST /v1/teaching-assignments` | ✅ | ✅ | | | | | |
| Phân công GD | `PUT /v1/teaching-assignments/{id}` | ✅ | ✅ | | | | | |
| Phân công GD | `GET /v1/teaching-assignments` | ✅ | ✅ | ✅ | | | | |
| Phân công GD | `GET /v1/teaching-assignments/{id}` | ✅ | ✅ | ✅ | | | | |
| Phân công GD | `DELETE /v1/teaching-assignments/{id}` | ✅ | ✅ | | | | | |
| Thời khoá biểu | `GET /v1/timetable/class/{classId}` | ✅ | ✅ | ✅ | | | | |
| Thời khoá biểu | `GET /v1/timetable/teacher/{id}` | ✅ | ✅ | ✅ | | | | |
| Thời khoá biểu | `POST /v1/timetable/slots` | ✅ | ✅ | | | | | |
| Thời khoá biểu | `PUT /v1/timetable/slots/{id}` | ✅ | ✅ | | | | | |
| Thời khoá biểu | `DELETE /v1/timetable/slots/{id}` | ✅ | ✅ | | | | | |
| Điểm danh | `POST /v1/attendance` | ✅ | | ✅ | | | | |
| Điểm danh | `POST /v1/attendance/class` | ✅ | | ✅ | | | | |
| Điểm danh | `PUT /v1/attendance/{id}` | ✅ | | ✅ | | | | |
| Điểm danh | `DELETE /v1/attendance/{id}` | ✅ | | ✅ | | | | |
| Điểm danh | `GET /v1/attendance/{id}` | ✅ | | ✅ | ✅ | ✅ | | |
| Điểm danh | `GET /v1/attendance/student/{id}` | ✅ | | ✅ | ✅ | ✅ | | |
| Điểm danh | `GET /v1/attendance/student/{id}/between` | ✅ | | ✅ | ✅ | ✅ | | |
| Điểm danh | `GET /v1/attendance/student/{id}/percentage` | ✅ | | ✅ | ✅ | ✅ | | |
| Điểm danh | `GET /v1/attendance/date/{date}` | ✅ | | ✅ | | | | |
| Điểm danh | `GET /v1/attendance/between` | ✅ | | ✅ | | | | |
| Điểm (cũ) | `POST /v1/grades` | ✅ | | ✅ | | | | |
| Điểm (cũ) | `PUT /v1/grades/{id}` | ✅ | | ✅ | | | | |
| Điểm (cũ) | `DELETE /v1/grades/{id}` | ✅ | | ✅ | | | | |
| Điểm (cũ) | `GET /v1/grades/{id}` | ✅ | | ✅ | ✅ | ✅ | | |
| Điểm (cũ) | `GET /v1/grades/student/{id}` | ✅ | | ✅ | ✅ | ✅ | | |
| Điểm (cũ) | `GET /v1/grades/student/{id}/year/{y}` | ✅ | | ✅ | ✅ | ✅ | | |
| Điểm (cũ) | `GET /v1/grades/student/{id}/subject/{s}` | ✅ | | ✅ | ✅ | ✅ | | |
| Điểm (cũ) | `GET /v1/grades/student/{id}/average` | ✅ | | ✅ | ✅ | ✅ | | |
| Điểm (cũ) | `GET /v1/grades/student/{id}/average/year/{y}` | ✅ | | ✅ | ✅ | ✅ | | |
| Điểm (cũ) | `GET /v1/grades/year/{year}` | ✅ | | ✅ | | | | |
| Điểm TT22 | `POST /v1/grade-records` | ✅ | | ✅ | | | | |
| Điểm TT22 | `PUT /v1/grade-records/{id}` | ✅ | | ✅ | | | | |
| Điểm TT22 | `DELETE /v1/grade-records/{id}` | ✅ | | ✅ | | | | |
| Điểm TT22 | `GET /v1/grade-records/{id}` | ✅ | | ✅ | ✅ | ✅ | | |
| Điểm TT22 | `GET /v1/grade-records/student/{id}/semester/{s}` | ✅ | | ✅ | ✅ | ✅ | | |
| Điểm TT22 | `GET /v1/grade-records/student/{id}/summary` | ✅ | | ✅ | ✅ | ✅ | | |
| Điểm TT22 | `GET /v1/grade-records/student/{id}/year-summary` | ✅ | | ✅ | ✅ | ✅ | | |
| Cấu hình điểm | `POST/PUT/GET/DELETE /v1/grade-config` | ✅ | | | | | | |
| Hạnh kiểm | `POST /v1/conduct` | ✅ | | ✅ | | | | |
| Hạnh kiểm | `PUT /v1/conduct/{id}` | ✅ | | ✅ | | | | |
| Hạnh kiểm | `GET /v1/conduct/student/{id}` | ✅ | | ✅ | ✅ | ✅ | | |
| Hạnh kiểm | `GET /v1/conduct/class/{c}/semester/{s}` | ✅ | | ✅ | | | | |
| Xét lên lớp | `GET /v1/promotions/class/{id}/preview` | ✅ | ✅ | ✅ | | | | |
| Xét lên lớp | `POST /v1/promotions/confirm` | ✅ | ✅ | | | | | |
| Xét lên lớp | `GET /v1/promotions/student/{id}` | ✅ | ✅ | ✅ | ✅ | ✅ | | |
| Ngưỡng xét | `POST/PUT/GET/DELETE /v1/promotion-thresholds` | ✅ | ✅ | | | | | |
| Học phí | `POST /v1/fees` | ✅ | | | | | | ✅ |
| Học phí | `PUT /v1/fees/{id}` | ✅ | | | | | | ✅ |
| Học phí | `DELETE /v1/fees/{id}` | ✅ | | | | | | ✅ |
| Học phí | `GET /v1/fees/{id}` | ✅ | | | ✅ | ✅ | | ✅ |
| Học phí | `GET /v1/fees/student/{id}` | ✅ | | | ✅ | ✅ | | ✅ |
| Học phí | `GET /v1/fees/student/{id}/year/{y}` | ✅ | | | ✅ | ✅ | | ✅ |
| Học phí | `GET /v1/fees/student/{id}/pending` | ✅ | | | ✅ | ✅ | | ✅ |
| Học phí | `GET /v1/fees/student/{id}/total-dues` | ✅ | | | ✅ | ✅ | | ✅ |
| Học phí | `POST /v1/fees/{feeId}/payment` | ✅ | | | ✅ | ✅ | | ✅ |
| Học phí | `GET /v1/fees/status/{status}` | ✅ | | | | | | ✅ |
| Học phí | `GET /v1/fees/year/{year}` | ✅ | | | | | | ✅ |
| Thư viện | `POST /v1/library/books` | ✅ | | | | | ✅ | |
| Thư viện | `PUT /v1/library/books/{id}` | ✅ | | | | | ✅ | |
| Thư viện | `DELETE /v1/library/books/{id}` | ✅ | | | | | ✅ | |
| Thư viện | `GET /v1/library/books` (+ search/category/author/available/{id}) | ✅ | | ✅ | ✅ | | ✅ | |
| Thư viện | `POST /v1/library/books/{id}/borrow` | | | ✅ | ✅ | | | |
| Thư viện | `POST /v1/library/books/{id}/return` | | | ✅ | ✅ | | | |
| Thư viện | `GET /v1/library/transactions/me` | | | ✅ | ✅ | | | |
| Thư viện | `GET /v1/library/transactions` | ✅ | | | | | ✅ | |
| Báo cáo | `GET /v1/reports/student/{id}/transcript` | ✅ | | ✅ | ✅ | ✅ | | |
| Báo cáo | `GET /v1/reports/class/{id}/attendance` | ✅ | | ✅ | | | | |
| Báo cáo | `GET /v1/reports/fees/receipt/{feeId}` | ✅ | | | ✅ | ✅ | | ✅ |
| Tài liệu | `POST /v1/documents` 🔓 | ✅ | ✅ | ✅ | ✅ | ✅ | | |
| Tài liệu | `GET /v1/documents` (+ /{id}, /download) | ✅ | ✅ | ✅ | ✅ | ✅ | | |
| Tài liệu | `DELETE /v1/documents/{id}` | ✅ | ✅ | | | | | |
| Thông báo | `POST /v1/notifications` | ✅ | ✅ | ✅ | | | | |
| Thông báo | `GET /v1/notifications/my` 🔓 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Thông báo | `PUT /v1/notifications/{id}/read` 🔓 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Phụ huynh–HS | `POST /v1/parents/{p}/children/{s}` | ✅ | | | | | | |
| Phụ huynh–HS | `DELETE /v1/parents/{p}/children/{s}` | ✅ | | | | | | |
| Phụ huynh–HS | `GET /v1/parents/{p}/children` | ✅ | | | | ✅ | | |
| Tuyển sinh | `POST /v1/admissions` 🌐 | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 | 🌐 |
| Tuyển sinh | `GET /v1/admissions` (+ /{id}, status, approve) | ✅ | | | | | | |

⚠️ = endpoint có rủi ro phân quyền (STUDENT đọc được hồ sơ HS khác / danh bạ nhân sự) — xem Phần G của kế hoạch.

---

## Ghi chú quan trọng

1. **STUDENT/PARENT chỉ thấy dữ liệu của mình/của con** ở các endpoint điểm/điểm danh/học phí/hạnh kiểm/xét lên lớp nhờ `StudentAccessGuard` — dù bảng trên đánh ✅, dữ liệu trả về đã được lọc về đúng người đó.
2. **Ngoại lệ nguy hiểm:** `GET /v1/students/{id}`, `/students/roll/{roll}`, `GET /v1/staff*` **không** qua guard → STUDENT xem được hồ sơ mọi HS + danh bạ nhân sự. Cần vá (Phần G.2.1 & G.2.3, mục A9 trong kế hoạch).
3. **PRINCIPAL không xem được dữ liệu học tập** (điểm/điểm danh/hạnh kiểm/học phí/báo cáo) — bất thường thiết kế (G.2.2).
4. **`POST /v1/documents`, `PUT /v1/notifications/{id}/read`** chỉ yêu cầu đăng nhập (🔓) → cần xác nhận tầng service kiểm tra chủ sở hữu (G.2.4).
5. **Frontend hiện chỉ có 8 trang** nên phần lớn quyền của các vai trò *chưa được surface lên giao diện* (đặc biệt PARENT, LIBRARIAN, ACCOUNTANT gần như chưa có trang riêng) — xem kế hoạch Phần A.2.
