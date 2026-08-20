# KẾ HOẠCH CHỈNH SỬA & NÂNG CẤP HỆ THỐNG QUẢN LÝ TRƯỜNG THCS-THPT (thcsthptTS)

*Phiên bản 2 — cập nhật theo yêu cầu: triển khai Backend và Frontend **song song** (không tuần tự), và chọn phương án UI **"Mạnh"**: chuyển hẳn sang Tailwind CSS + shadcn/ui, thiết kế lại toàn bộ theo hướng tối giản – hiện đại, tự dựng data table/date picker từ đầu.*

*Dựa trên báo cáo phân tích `PROJECT_ANALYSIS_SUMMARY.md`. Đây vẫn là tài liệu kế hoạch để duyệt trước khi đụng vào code thật.*

---

## 0. Nguyên tắc thực hiện (cập nhật cho mô hình song song)

- **Hai track chạy song song, không chờ nhau**: **Track Backend** (bảo mật → hoàn thiện → module VN) và **Track Frontend** (tái thiết kế UI bằng Tailwind + shadcn/ui) khởi động cùng lúc từ Tuần 1. Track Frontend gần như không phụ thuộc Track Backend ở giai đoạn xây nền tảng UI (component dùng chung, layout, design system) — chỉ cần phối hợp ở các **điểm đồng bộ (sync point)** khi frontend cần một API cụ thể đã sẵn sàng (xem bảng lịch trình song song ở mục 4).
- **Thống nhất hợp đồng API (API contract) sớm**: vì 2 track làm song song, cần dùng Swagger/OpenAPI đã có sẵn (`springdoc-openapi`) làm nguồn sự thật duy nhất về request/response. Đề xuất backend cập nhật Swagger annotation đầy đủ ngay từ Giai đoạn 1, và frontend có thể dùng **Mock Service Worker (MSW)** để giả lập API theo đúng contract khi backend chưa xong, tránh bị chặn tiến độ.
- **Đổi tất cả secret đã lộ (JWT secret, mật khẩu DB) trước khi làm bất cứ việc gì khác** ở Track Backend — việc này không phụ thuộc frontend nên vẫn nên làm đầu tiên, song song với việc Track Frontend dựng nền tảng UI.
- **Track Backend vẫn giữ thứ tự nội bộ Giai đoạn 1 → 2 → 3** (vá bảo mật trước, xong mới đến hoàn thiện, xong mới đến module VN) — chỉ có Track Frontend là chạy song song với toàn bộ 3 giai đoạn này, không phải chờ Track Backend xong mới bắt đầu.
- **Không sửa trực tiếp trên nhánh chính đang chạy production/DB thật.** Backend dùng nhánh `feature/security-hardening`, `feature/vn-academic-model`...; Frontend dùng nhánh riêng `feature/ui-redesign-tailwind` tách biệt hoàn toàn khỏi code Bootstrap cũ (lý do kỹ thuật ở mục 3.0).
- Mỗi giai đoạn/track nên có **tiêu chí hoàn thành (Definition of Done)** riêng và **build/test pass** trước khi merge.

---

## TRACK BACKEND

### GIAI ĐOẠN 1 — VÁ BẢO MẬT (3-5 ngày làm việc)

| # | Việc cần làm | File/khu vực | Cách làm | DoD |
|---|---|---|---|---|
| 1.1 | Xoá toàn bộ `System.out.println` in mật khẩu/hash trong luồng login | `AuthenticationService.login()` | Xoá các dòng debug; nếu cần log, dùng `Logger` (SLF4J) ở mức DEBUG và **không bao giờ log password/hash** | Không còn thông tin nhạy cảm nào xuất hiện trong console/log khi login |
| 1.2 | Chặn client tự đặt `role` khi đăng ký | `AuthController.register`, `AuthenticationService.register`, thêm `RegisterRequest` DTO mới (không có field `role`) | Tạo DTO `RegisterRequest` (username, email, password, firstName, lastName, phoneNumber) thay vì nhận thẳng `User`; service luôn set `Role.STUDENT` khi tự đăng ký công khai; tạo tài khoản ADMIN/TEACHER/STAFF phải qua endpoint riêng `POST /v1/users` yêu cầu `@PreAuthorize("hasRole('ADMIN')")` | Test: đăng ký với `role: ADMIN` trong payload → tài khoản tạo ra vẫn là STUDENT |
| 1.3 | Chuyển JWT secret + cấu hình DB ra biến môi trường | `application.yml` → `${JWT_SECRET}`, `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}` | Thêm hướng dẫn `.env.example`/README; **đổi JWT secret mới + đổi mật khẩu DB trên Aiven ngay** | `application.yml` trong repo không còn chứa secret thật |
| 1.4 | Tách cấu hình theo môi trường | `application-dev.yml`, `application-prod.yml` | Dùng Spring profile; dev trỏ DB local/docker, prod dùng Aiven qua env var | Chạy được với profile `dev` bằng DB local |
| 1.5 | Chuyển `ddl-auto` sang Flyway | `pom.xml` thêm `flyway-core`; `ddl-auto: validate`; `V1__baseline.sql` | Dump schema hiện tại → viết migration baseline khớp entity | App khởi động thành công, không drift schema |
| 1.6 | Không lộ chi tiết lỗi hệ thống ra client | `GlobalExceptionHandler.handleGeneralException` | Trả message chung, log đầy đủ ở server | Response 500 không còn chứa `ex.getMessage()` gốc |
| 1.7 | Bật validation cho toàn bộ input từ client | Toàn bộ DTO request | `@NotBlank`, `@Email`, `@Size`, `@Positive` + `@Valid`; tách Request DTO khỏi Entity | `MethodArgumentNotValidException` trả đúng khi thiếu field |
| 1.8 | Hoàn thiện Swagger annotation đầy đủ cho mọi endpoint | Toàn bộ Controller | Thêm `@Schema`, ví dụ request/response mẫu | **Cần ưu tiên xong sớm trong tuần 1** vì Track Frontend dùng đây làm API contract |
| 1.9 | `.gitignore` + kiểm tra lịch sử git | `backend/.gitignore` | Thêm `application-local.yml`, `.env`; cân nhắc xoá secret khỏi lịch sử git | Secret không còn truy xuất được |

**Lưu ý phối hợp**: mục 1.2 (đổi cấu trúc request đăng ký) và 1.8 (Swagger đầy đủ) nên xong sớm nhất trong Giai đoạn 1, vì Track Frontend cần các API này ổn định để rebuild `LoginPage` ở Tuần 2.

### GIAI ĐOẠN 2 — HOÀN THIỆN BACKEND (2-3 tuần)

#### 2.1. Quản lý lớp học
- `SchoolClassRepository`, `SchoolClassService`, `SchoolClassController` (`/v1/classes`) — CRUD, `@PreAuthorize` theo vai trò ADMIN/PRINCIPAL (đọc cho TEACHER).
- Chưa đổi khoá ngoại `Student.className` ngay (để Giai đoạn 3.1 xử lý cùng mô hình Năm học/Học kỳ, tránh migrate 2 lần).

#### 2.2. Endpoint tổng hợp cho Dashboard
- `GET /v1/dashboard/stats`: tổng số học sinh/nhân sự đang hoạt động, tỉ lệ chuyên cần trung bình, tổng công nợ chưa thu, số sách đang mượn — để Track Frontend nối vào Dashboard thật ở Tuần 3.

#### 2.3. Chuẩn hoá API danh sách
- Thêm phân trang (`Pageable`, `Page<T>`) cho các endpoint `getAll...`, giữ tương thích ngược bằng param `page`/`size` optional.

#### 2.4. Refresh token
- Đảm bảo endpoint `/v1/auth/refresh-token` hoạt động ổn định, đúng format để frontend tích hợp interceptor tự động refresh.

**Deliverable Track Backend Giai đoạn 2**: API cho lớp học, dashboard, phân trang, refresh token đã sẵn sàng và có Swagger đầy đủ để Track Frontend nối vào.

### GIAI ĐOẠN 3 — MODULE ĐẶC THÙ GIÁO DỤC VIỆT NAM (6-10 tuần, chia module độc lập)

*(Nội dung entity/API giữ nguyên như bản kế hoạch trước — tóm tắt lại, chi tiết đầy đủ xem các mục 3.1-3.9 gốc)*

| Module | Entity chính | API | Ghi chú phối hợp Frontend |
|---|---|---|---|
| 3.1 Năm học/Học kỳ/Môn học *(nền tảng, làm trước)* | `AcademicYear`, `Semester`, `Subject` | `/v1/academic-years`, `/v1/semesters`, `/v1/subjects` | Frontend dựng trang "Cấu hình năm học" ngay khi API xong, dùng bộ component đã có sẵn từ Track Frontend (không cần xây lại UI framework) |
| 3.2 Phân công giảng dạy & Thời khoá biểu | `TeachingAssignment`, `TimetableSlot` | `/v1/teaching-assignments`, `/v1/timetable` | Cần `DataTable`/lưới thời khoá biểu — dùng chung component `DataTable` đã xây ở Track Frontend Tuần 2 |
| 3.3 Hệ thống điểm TT22/58 | `GradeRecord`, `GradeComponentConfig`, `GradeClassification` | `/v1/grades`, `/v1/grades/student/{id}/summary` | Bảng nhập điểm dùng `DataTable` + `Form` (React Hook Form + Zod) đã có sẵn |
| 3.4 Hạnh kiểm | `ConductRecord` | `/v1/conduct` | Form đơn giản, tái dùng `Form` component |
| 3.5 Xét lên lớp | `PromotionRecord` | `/v1/promotions` | Bulk-action trên `DataTable` |
| 3.6 Phụ huynh & Sổ liên lạc điện tử | `ParentStudentRelation`, `Notification` | `/v1/parents/*`, `/v1/notifications` | Dashboard riêng cho PARENT dùng lại layout đã redesign |
| 3.7 Tuyển sinh đầu cấp | `AdmissionApplication` | `/v1/admissions` | Form công khai dùng `Form` component, không cần đăng nhập |
| 3.8 Xuất báo cáo PDF/Excel | — | `/v1/reports/*` | Chủ yếu backend; frontend chỉ cần nút tải xuống |
| 3.9 Hạ tầng dùng chung | `DocumentAttachment`, `AuditLog` | — | Upload component dùng shadcn `Input[type=file]` + preview |

**Điểm mấu chốt**: vì bộ component UI dùng chung (DataTable, DatePicker, Form, Toast, Dialog...) đã được xây xong ở Track Frontend từ Tuần 1-6, mỗi module ở Giai đoạn 3 từ đây trở đi làm **backend + frontend song song trong cùng module**, không còn phần "xây nền tảng UI" tốn thời gian riêng nữa — giúp rút ngắn đáng kể so với làm tuần tự.

---

## TRACK FRONTEND — TÁI THIẾT KẾ UI (Tailwind CSS + shadcn/ui), 5-6 tuần

### 3.0. Nguyên tắc & rủi ro kỹ thuật cần lưu ý trước khi bắt đầu

- **Tailwind CSS và Bootstrap không nên chạy song song trong cùng 1 app đang chạy** — cả hai đều có global reset/preflight riêng, dễ xung đột style (Bootstrap `.container`, `.row` versus Tailwind utility classes). Vì đây là redesign toàn bộ ("Mạnh"), khuyến nghị làm trên **nhánh riêng tách biệt hoàn toàn** (`feature/ui-redesign-tailwind`), gỡ bỏ `bootstrap`/`react-bootstrap` khỏi `package.json` **ngay từ đầu nhánh này**, thay vì để tồn tại song song rồi dọn sau — tránh việc phải debug xung đột CSS giữa chừng.
- **shadcn/ui không phải là một package cài qua npm** — nó là các file component (copy vào source code của mình qua CLI `npx shadcn@latest add ...`) dựa trên Radix UI primitives. Nghĩa là code các component này sẽ nằm trong repo, nhóm dev **tự chịu trách nhiệm bảo trì/cập nhật** — cần thống nhất quy ước không sửa tuỳ tiện các file trong `components/ui/` để dễ đồng bộ sau này.
- **shadcn/ui CLI hỗ trợ chính thức Vite hoặc Next.js, không hỗ trợ Create React App** — do đó bắt buộc phải **migrate build tool từ CRA (`react-scripts`) sang Vite** như bước đầu tiên của track này (việc này vốn cũng nằm trong khuyến nghị hiện đại hoá đã trao đổi trước đó).

### 3.1. Tuần 1 — Nền tảng

- Migrate CRA → **Vite** (giữ nguyên cấu trúc `src/`, cập nhật entrypoint, env var `VITE_*` thay `REACT_APP_*`).
- Cài **Tailwind CSS**, cấu hình `tailwind.config.js` với design token riêng (màu thương hiệu, bán kính bo góc, font family) thay cho gradient tím lặp lại thủ công hiện tại.
- Init **shadcn/ui** (`npx shadcn@latest init`), chọn base color trung tính, style "minimal".
- Cài font **Be Vietnam Pro** (tối ưu dấu tiếng Việt) qua self-host hoặc Google Fonts.
- Dựng lại **Layout shell** mới: Navbar + Sidebar — dùng shadcn `Sheet` cho sidebar dạng drawer trên mobile (thay vì `position: absolute` thủ công như hiện tại).
- Setup **dark mode** đúng chuẩn (class strategy của Tailwind + toggle lưu preference trong `localStorage`) — áp dụng nhất quán toàn app, khắc phục tình trạng chỉ có `Sidebar.css` hỗ trợ dark mode nửa vời như hiện tại.

### 3.2. Tuần 2 — Bộ component dùng chung + Auth

- Xây `DataTable` generic (TanStack Table v8 + shadcn Table) hỗ trợ sort/filter/phân trang — dùng lại cho **mọi** module (Staff, Student, Library, Grade, Fee, Timetable...) thay vì mỗi trang tự viết bảng HTML như hiện tại.
- Xây `DatePicker`/`DateRangePicker` (react-day-picker + shadcn, tận dụng `date-fns` đã có sẵn trong `package.json`).
- Xây `Form` wrapper chuẩn (React Hook Form + Zod + shadcn Form components) — dùng chung cho toàn bộ form sắp tới (điểm, hạnh kiểm, tuyển sinh...).
- Tích hợp **Toast** (`sonner`, đi kèm chuẩn shadcn/ui) thay cho `<Alert>` tĩnh hiện tại.
- Tích hợp **TanStack Query** cho toàn bộ data fetching, thay `useState`/`useEffect` thủ công lặp lại ở mỗi trang.
- Rebuild `LoginPage` theo thiết kế mới, bỏ tài khoản test hard-code (đồng bộ với Track Backend 1.2).

### 3.3. Tuần 3 — Dashboard + Staff/Student Management

- Dashboard: thiết kế lại stat card, thêm biểu đồ thật bằng **Recharts**, nối `GET /v1/dashboard/stats` (Track Backend 2.2) — bỏ hoàn toàn số liệu giả lập hiện tại.
- `StaffManagement`, `StudentManagement`: chuyển sang `DataTable` mới; thay `Modal` bằng shadcn `Dialog`; hoàn thiện form thêm/sửa nối API thật (hiện nút Save trong `StaffManagement` chưa gọi API).

### 3.4. Tuần 4 — Hoàn thiện 4 module còn placeholder

- `LibraryManagement`, `AttendanceManagement`, `GradeManagement`, `FeeManagement` — dựng UI hoàn toàn mới bằng bộ component đã có (không viết lại bằng react-bootstrap trước rồi đổi sau), nối các API đã tồn tại sẵn từ trước (không cần chờ Track Backend Giai đoạn 2/3).

### 3.5. Tuần 5 — Trang mới + Polish

- Trang "Quản lý lớp học" (nối API `/v1/classes` từ Track Backend 2.1).
- Rà soát responsive/mobile toàn bộ ứng dụng.
- Rà soát accessibility (độ tương phản màu, điều hướng bàn phím, `aria-label`).
- Thay spinner toàn trang bằng **skeleton loading** cho cảm giác mượt hơn.

### 3.6. Tuần 6 — Dọn dẹp & QA

- Gỡ hẳn `bootstrap`, `react-bootstrap` khỏi `package.json` và mọi import CSS liên quan.
- Kiểm tra bundle size, code-split theo route (`React.lazy`).
- Visual QA toàn bộ so với thiết kế, kiểm tra trên nhiều kích thước màn hình.
- Viết test cơ bản (**Vitest** + React Testing Library, thay Jest mặc định của CRA) cho các component dùng chung (`DataTable`, `Form`).

**Deliverable Track Frontend**: toàn bộ ứng dụng chạy trên Tailwind CSS + shadcn/ui, không còn Bootstrap; có bộ component dùng chung (DataTable, DatePicker, Form, Toast, Dialog) sẵn sàng để các module Giai đoạn 3 dùng lại ngay mà không tốn công dựng UI mới mỗi lần.

---

## 4. LỊCH TRÌNH SONG SONG TỔNG HỢP

| Tuần | Track Backend | Track Frontend | Điểm đồng bộ cần chú ý |
|---|---|---|---|
| 1 | Giai đoạn 1: vá bảo mật (1.1–1.9), ưu tiên xong 1.2 + 1.8 sớm | Nền tảng: Vite, Tailwind, shadcn/ui, layout shell, dark mode | Frontend cần biết cấu trúc `RegisterRequest` mới trước khi rebuild LoginPage ở Tuần 2 |
| 2 | Giai đoạn 2.1 (SchoolClass API) + bắt đầu 2.2 (dashboard stats) | Bộ component dùng chung (DataTable, DatePicker, Form, Toast) + rebuild LoginPage + tích hợp React Query | Backend cần API `/v1/auth/*` mới ổn định để frontend nối login |
| 3 | Hoàn thiện 2.2 (dashboard stats); 2.3 phân trang | Dashboard (biểu đồ thật) + Staff/Student Management (DataTable mới) | Backend cần `/v1/dashboard/stats` sẵn sàng đúng hạn tuần này |
| 4 | 2.4 refresh token; bắt đầu Giai đoạn 3.1 (Năm học/Học kỳ/Môn học) | 4 module placeholder (Library/Attendance/Grade/Fee) — dùng API đã có sẵn từ trước, không phụ thuộc backend đang làm | Không có phụ thuộc chặn — có thể làm độc lập |
| 5 | Tiếp tục 3.1; bắt đầu 3.2 (thời khoá biểu) | Trang Quản lý lớp học (nối `/v1/classes`) + responsive/accessibility pass | Backend cần `/v1/classes` sẵn sàng (đã xong từ Tuần 2) |
| 6 | Tiếp tục 3.2 | Dọn dẹp, gỡ Bootstrap, QA, viết test Vitest | — |
| 7+ | Giai đoạn 3.3 → 3.9 lần lượt theo từng module | Xây UI cho từng module **ngay trong cùng đợt** bằng bộ component đã có sẵn (không cần dựng nền tảng UI nữa) | Backend + Frontend làm cặp theo từng module, release cùng nhau |

---

## 5. Kế hoạch migration dữ liệu

*(Không đổi — vẫn là công việc riêng của Track Backend, không liên quan Track Frontend)*

1. **`SchoolClass`**: từ dữ liệu `Student.className` + `Student.section` hiện có, sinh danh sách lớp duy nhất → tạo bản ghi `SchoolClass` tương ứng.
2. **`Student.currentClass_FK`**: đối chiếu chuỗi cũ với `SchoolClass` để gán FK; ghi log bản ghi không khớp được để admin xử lý thủ công.
3. **`Subject`**: quét giá trị `Grade.subject` hiện có → sinh danh mục `Subject`.
4. **`AcademicYear`/`Semester`**: quét chuỗi `academicYear` hiện có trong `Grade`/`Fee`/`SchoolClass` → tạo bản ghi tương ứng, mặc định gán "Học kỳ 1", cho phép admin sửa lại.
5. Viết dưới dạng script Flyway riêng, **chạy thử trên bản sao dữ liệu trước**, không chạy thẳng trên Aiven production.
6. Giữ field cũ (`@Deprecated`) ít nhất 1 phiên bản để rollback an toàn.

---

## 6. Kế hoạch kiểm thử

- **Track Backend Giai đoạn 1**: test cho `AuthenticationService` (không cho set role tuỳ ý, không lộ log), test `GlobalExceptionHandler`.
- **Track Backend Giai đoạn 2-3**: test tích hợp cho các controller mới; **bắt buộc unit test cho service tính điểm TT22/58** với bộ dữ liệu mẫu đối chiếu tay, review bởi người hiểu nghiệp vụ giáo dục trước khi release.
- **Track Frontend**: dùng **Vitest** + React Testing Library (thay Jest/CRA mặc định) cho các component dùng chung (`DataTable`, `Form`, `DatePicker`); cân nhắc thêm **Playwright** cho vài kịch bản E2E quan trọng (đăng nhập, tạo học sinh, điểm danh) sau khi UI ổn định.
- CI (`.github/workflows/build.yml`) chạy song song 2 job: `mvn test` (backend) và `npm run test` (frontend), chặn merge nếu 1 trong 2 fail.

---

## 7. Timeline tổng thể ước lượng (mô hình song song)

Với 1 backend dev + 1 frontend dev chạy song song từ Tuần 1:

- **Tuần 1-6**: nền tảng hoàn chỉnh — bảo mật xong, backend hoàn thiện phần dở dang + bắt đầu module VN đầu tiên, frontend hoàn tất redesign toàn bộ UI (Tailwind + shadcn/ui) và đã nối xong các module hiện có.
- **Tuần 7 trở đi**: các module Giai đoạn 3 còn lại (3.2 → 3.9) triển khai theo từng cặp backend/frontend song song, mỗi module ước lượng 1-3 tuần như bảng ở mục "Track Backend – Giai đoạn 3".

**Tổng ước lượng: ~3.5–4.5 tháng** (rút ngắn so với ước lượng tuần tự trước đây ~4-5 tháng nhờ chạy song song 2 track).

---

## 8. Rủi ro & phụ thuộc cần lưu ý

- **Rủi ro đồng bộ API contract**: vì 2 track làm song song, nếu backend đổi response shape mà không cập nhật Swagger kịp, frontend sẽ bị lệch — cần kỷ luật cập nhật Swagger annotation ngay khi đổi API, và ưu tiên dùng MSW để frontend không bị block cứng.
- **Rủi ro xung đột CSS Bootstrap/Tailwind**: đã xử lý bằng cách làm trên nhánh riêng, cutover toàn bộ một lần thay vì để chạy song song trong cùng ứng dụng.
- **Rủi ro bảo trì shadcn/ui**: vì component nằm trong source code (không phải dependency npm), cần quy ước rõ ràng để nhiều người sửa không đụng nhau, và cần theo dõi thủ công khi Radix UI/shadcn có bản cập nhật bảo mật.
- **Rủi ro dữ liệu** (giữ nguyên từ bản trước): migration từ chuỗi sang FK có thể lệch nếu dữ liệu gốc không sạch — cần rà soát thủ công sau migration tự động.
- **Rủi ro nghiệp vụ** (giữ nguyên): công thức tính điểm/xếp loại phải được người có chuyên môn giáo dục xác nhận trước khi dùng thật.
- **Phụ thuộc bên ngoài** (giữ nguyên): module SMS/Zalo OA (3.6) cần đăng ký tài khoản doanh nghiệp, có chi phí vận hành — cần quyết định ngân sách trước khi triển khai.
- **Không có test hiện tại** ở cả 2 phía — ưu tiên viết test cho phần đang sửa, không cần phủ toàn bộ hệ thống cũ ngay từ đầu.

---

*Tài liệu này là kế hoạch tổng thể cho mô hình triển khai song song; trước khi bắt đầu code từng phần, nên review chi tiết API contract giữa 2 track (đặc biệt ở các điểm đồng bộ tại mục 4) trước khi triển khai.*
