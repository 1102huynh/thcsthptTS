# KẾ HOẠCH: CỔNG THÔNG TIN CÔNG KHAI (TIN TỨC – SỰ KIỆN – TUYỂN SINH) — thcsthptTS

**Phiên bản 1.1 — ngày 03/09/2026**
*(v1.1: **đã hiện thực P1 + P2 + P3** — backend `/v1/public/**` + CMS; portal công khai (`/` = trang chủ tin tức, `/login` = đăng nhập); trang quản trị News/Event. Chỉ còn **P4** (prerender + cache nâng cao). Xem "TRẠNG THÁI TRIỂN KHAI" cuối tài liệu.)*

*Module mới: trang tin tức/sự kiện/tuyển sinh của trường mà **người ngoài hệ thống xem được KHÔNG cần đăng nhập**. Là một phần của dự án `thcsthptTS`, tái sử dụng hạ tầng backend (Spring Boot + MySQL + Flyway + FileStorage) và frontend (Vite + Tailwind + shadcn) sẵn có. Bổ sung cho `KE_HOACH_NANG_CAP_V4.md` — không thay thế.*

> **Quyết định mục 11 đã chốt (v1.1, theo khuyến nghị của kế hoạch):** (1) `/` = cổng công khai, đăng nhập chuyển `/login` — ✅; (2) phạm vi 1.0 gồm cả Trang chủ + Giới thiệu + Liên hệ; (3) **có** form Liên hệ + `ContactMessage` + rate-limit; (4) editor: **textarea HTML + xem trước** (WYSIWYG để sau, tránh thêm dependency); (5) SEO: `<Seo>` (helmet-lite) + `sitemap.xml`/`robots.txt` trước, prerender ở P4; (6) quản trị: **ADMIN/PRINCIPAL** (chưa `CONTENT_EDITOR`); (7) domain: env `APP_CORS_ALLOWED_ORIGINS` (mặc định localhost). Flyway dùng **`V11`** (V11 không dành cho migration điểm).

---

## 1. MỤC TIÊU & PHẠM VI

**Mục tiêu:** một cổng thông tin công khai để phụ huynh, học sinh tương lai và người dân xem **tin tức, sự kiện, thông báo tuyển sinh** của trường mà không cần tài khoản; đồng thời có **trang quản trị nội dung (CMS)** cho nhà trường tự đăng/sửa bài.

**Trong phạm vi (bản 1.0):**
- Trang chủ công khai (giới thiệu ngắn + tin nổi bật + sự kiện sắp tới + CTA tuyển sinh).
- **Tin tức**: danh sách + chi tiết, phân theo **chuyên mục** (Tuyển sinh, Hoạt động, Thông báo…).
- **Sự kiện**: danh sách (sắp tới / đã qua) + chi tiết (thời gian, địa điểm).
- **Tuyển sinh**: trang thông tin tuyển sinh + nút dẫn tới form nộp hồ sơ **đã có** (`/apply` → `POST /v1/admissions`).
- **Giới thiệu** + **Liên hệ** (tĩnh; Liên hệ có form gửi liên hệ tuỳ chọn).
- **CMS quản trị** (đăng nhập, ADMIN/PRINCIPAL): CRUD tin/sự kiện/chuyên mục + upload ảnh + đăng/gỡ.

**Ngoài phạm vi (giai đoạn sau):** bình luận công khai, đăng ký tham gia sự kiện online, đa ngôn ngữ, tìm kiếm full-text nâng cao.

---

## 2. QUYẾT ĐỊNH KIẾN TRÚC QUAN TRỌNG NHẤT: ĐỊNH TUYẾN "/"

Hiện tại `App.jsx`: khi **chưa đăng nhập**, route `/` = **trang LoginPage**. Với cổng công khai, người ngoài vào `truongxyz.edu.vn` phải thấy **trang tin tức**, không phải form đăng nhập. Vì vậy cần tái cấu trúc định tuyến:

| Đường dẫn | Trước | Sau (đề xuất) |
|---|---|---|
| `/` | LoginPage (nếu chưa login) | **Cổng công khai — Trang chủ tin tức** |
| `/login` (hoặc `/dang-nhap`) | *(không có)* | **LoginPage** (nhân sự/HS/PH đăng nhập) |
| `/tin-tuc`, `/su-kien`, … | – | Trang công khai (xem mục 6) |
| `/apply`, `/forgot-password`, `/reset-password` | công khai (đã có) | giữ nguyên |
| Sau khi đăng nhập | AppShell (dashboard…) | giữ nguyên; thêm mục CMS "Tin tức" trong menu |

→ **Người đã đăng nhập** vẫn vào thẳng AppShell như cũ; **người chưa đăng nhập** thấy cổng công khai, và bấm "Đăng nhập" ở góc để tới `/login`. Đây là thay đổi có ảnh hưởng, cần chốt trước (mục 11).

---

## 3. MÔ HÌNH DỮ LIỆU (Flyway `V12+`, charset utf8mb4)

**Entity mới** (theo đúng pattern entity/repository/service/controller/dto sẵn có):

### `NewsCategory` — Chuyên mục tin
`id`, `name` (VD "Tuyển sinh"), `slug` (VD "tuyen-sinh", **duy nhất**, dùng cho URL), `displayOrder`, `createdAt`.

### `NewsArticle` — Bài tin tức
`id`, `title`, `slug` (**duy nhất**, sinh từ title + chống trùng), `summary` (mô tả ngắn, cho danh sách + thẻ SEO), `content` (**HTML rich text — BẮT BUỘC sanitize, xem mục 8**), `coverImageUrl`, `category_FK`, `status` (enum `DRAFT`/`PUBLISHED`/`ARCHIVED`), `publishedAt` (nullable — chỉ hiện công khai khi PUBLISHED và `publishedAt <= now`), `isFeatured` (tin nổi bật/ghim trang chủ), `viewCount`, `author_FK` (Staff/User), `createdAt`, `updatedAt`.

### `SchoolEvent` — Sự kiện
`id`, `title`, `slug` (**duy nhất**), `description` (HTML sanitized), `coverImageUrl`, `location`, `startAt`, `endAt`, `status` (`DRAFT`/`PUBLISHED`/`ARCHIVED`), `isFeatured`, `createdAt`, `updatedAt`.

### `MediaAsset` — Ảnh/tệp cho tin & sự kiện *(hoặc tái dùng `DocumentAttachment`)*
`id`, `fileName`, `storedPath`, `contentType`, `sizeBytes`, `uploadedBy_FK`, `createdAt`. Phục vụ công khai qua `GET /v1/public/media/{id}` với đúng `Content-Type` + cache header. *(Tái dùng `FileStorageService` đã có, thư mục `app.uploads.dir`.)*

### *(Tuỳ chọn)* `ContactMessage` — Liên hệ công khai
`id`, `fullName`, `email`, `phone`, `subject`, `message`, `createdAt`, `handled`. Nhận qua `POST /v1/public/contact` (rate-limit chống spam).

### *(Tuỳ chọn)* `HeroBanner` — Ảnh bìa/khẩu hiệu trang chủ
`id`, `imageUrl`, `headline`, `subheadline`, `ctaLabel`, `ctaUrl`, `displayOrder`, `active`.

**Chỉ mục (index) nên có:** `news_articles(status, published_at)`, `news_articles(slug)`, `news_articles(category_id)`, `school_events(status, start_at)`, `school_events(slug)`.

---

## 4. API — TẦNG CÔNG KHAI (permitAll, chỉ đọc nội dung đã đăng)

> Đặt dưới tiền tố **`/v1/public/**`** — `SecurityConfig` **đã có sẵn** `requestMatchers("/api/v1/public/**").permitAll()` (hiện bỏ trống) nên chỉ cần thêm controller là chạy. Mọi endpoint công khai **chỉ trả bản ghi `PUBLISHED` và `publishedAt <= now`** — không bao giờ lộ DRAFT.

| Method | Path | Mô tả |
|---|---|---|
| GET | `/v1/public/news` | Danh sách tin (phân trang `page/size`, lọc `?category=slug`, sắp xếp featured trước rồi `publishedAt` desc) — trả `summary`, ảnh bìa, không trả `content` đầy đủ |
| GET | `/v1/public/news/{slug}` | Chi tiết 1 tin (theo slug, chuẩn SEO) — tăng `viewCount` |
| GET | `/v1/public/news/categories` | Danh sách chuyên mục |
| GET | `/v1/public/events` | Danh sách sự kiện, lọc `?when=upcoming|past`, sắp theo `startAt` |
| GET | `/v1/public/events/{slug}` | Chi tiết sự kiện |
| GET | `/v1/public/media/{id}` | Trả ảnh/tệp (bytes) + `Content-Type` + `Cache-Control` |
| GET | `/v1/public/home` | Gộp dữ liệu trang chủ (tin nổi bật + sự kiện sắp tới + banner) — 1 request cho nhẹ |
| POST | `/v1/public/contact` *(tuỳ chọn)* | Gửi liên hệ — **rate-limit** (filter mới kiểu `AdmissionRateLimitFilter`) |

*(Form nộp hồ sơ tuyển sinh dùng lại `POST /v1/admissions` đã công khai + đã rate-limit.)*

---

## 5. API — TẦNG QUẢN TRỊ CMS (đăng nhập; ADMIN/PRINCIPAL)

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| POST/PUT/DELETE | `/v1/news` , `/{id}` | ADMIN, PRINCIPAL | CRUD bài tin (tạo dạng DRAFT) |
| PUT | `/v1/news/{id}/publish` , `/unpublish` | ADMIN, PRINCIPAL | Đăng / gỡ (đặt `status` + `publishedAt`) |
| GET | `/v1/news` , `/{id}` | ADMIN, PRINCIPAL | Danh sách/chi tiết **gồm cả DRAFT** (khác tầng public) |
| POST/PUT/DELETE | `/v1/news-categories` | ADMIN, PRINCIPAL | CRUD chuyên mục |
| POST/PUT/DELETE | `/v1/events` , `/{id}`, publish | ADMIN, PRINCIPAL | CRUD sự kiện |
| POST | `/v1/media` (multipart ≤10MB) | ADMIN, PRINCIPAL | Upload ảnh, trả `id` + `url` (`/v1/public/media/{id}`) |
| GET | `/v1/contact-messages` , `PUT /{id}/handled` *(tuỳ chọn)* | ADMIN, PRINCIPAL | Xem/đánh dấu đã xử lý liên hệ |

> **Phân quyền:** giữ ADMIN/PRINCIPAL cho gọn (khớp cách các trang quản trị khác đang làm). Nếu sau này muốn giao cho "cán bộ truyền thông" mà không phải hiệu trưởng → cân nhắc thêm vai trò `CONTENT_EDITOR` (đây là lúc hợp lý để kích hoạt cơ chế `Permission`/`UserPermission` đang bỏ trống — xem `KE_HOACH_NANG_CAP_V4.md` H.3.2).

---

## 6. FRONTEND CÔNG KHAI

**Layout riêng `PublicLayout`** (tách hẳn `AppShell` của phần đăng nhập): header (logo + tên trường + menu: Trang chủ / Tin tức / Sự kiện / Tuyển sinh / Giới thiệu / Liên hệ + nút "Đăng nhập" → `/login`), footer (địa chỉ, SĐT, mạng xã hội, bản đồ).

**Các trang (route công khai, ngoài AppShell):**

| Route | Trang | Nguồn dữ liệu |
|---|---|---|
| `/` | Trang chủ (hero + tin nổi bật + sự kiện sắp tới + CTA tuyển sinh) | `GET /v1/public/home` |
| `/tin-tuc` | Danh sách tin (lọc chuyên mục, phân trang) | `GET /v1/public/news` |
| `/tin-tuc/:slug` | Chi tiết tin (render HTML đã sanitize, ảnh, chia sẻ MXH) | `GET /v1/public/news/{slug}` |
| `/su-kien` | Danh sách sự kiện (sắp tới/đã qua) | `GET /v1/public/events` |
| `/su-kien/:slug` | Chi tiết sự kiện | `GET /v1/public/events/{slug}` |
| `/tuyen-sinh` | Thông tin tuyển sinh + nút "Nộp hồ sơ" → `/apply` | tin chuyên mục "tuyển sinh" |
| `/gioi-thieu` | Giới thiệu trường (tĩnh hoặc 1 bài tin cố định) | tĩnh |
| `/lien-he` | Thông tin + form liên hệ *(tuỳ chọn)* | `POST /v1/public/contact` |

**Yêu cầu:** **mobile-first** (đa số phụ huynh xem trên điện thoại), tái dùng `DataTable`/`Card`/shadcn, `React.lazy` cho từng trang, khớp dark/light theme.

**CMS (trong AppShell, đăng nhập):** thêm trang `NewsManagement.jsx` + `EventManagement.jsx` dùng `DataTable` + `Dialog` + **trình soạn thảo rich text** (TipTap hoặc react-quill) + upload ảnh; thêm mục "Tin tức"/"Sự kiện" vào `config/navigation.js` (ADMIN/PRINCIPAL).

---

## 7. SEO & CHIA SẺ MẠNG XÃ HỘI (rất quan trọng với trang công khai)

Frontend hiện là **SPA render phía client (Vite)** → mặc định **kém SEO** và **không có preview khi share link lên Facebook/Zalo**. Với trang tuyển sinh/tin tức thì tìm được trên Google và có ảnh preview khi chia sẻ là **thiết yếu**. Đề xuất theo mức tăng dần:

1. **Bắt buộc (rẻ):** `react-helmet-async` đặt `<title>`, `<meta name="description">`, **Open Graph** (`og:title/og:description/og:image/og:url`) + Twitter Card cho từng trang tin/sự kiện; tạo `sitemap.xml` + `robots.txt`; URL thân thiện bằng **slug**.
2. **Nên có:** **prerender** các route công khai lúc build (VD `vite-plugin-prerender`/react-snap) hoặc prerender động, để bot Google/Facebook nhận HTML có nội dung + thẻ OG thật (vì bot Zalo/Facebook không chạy JS).
3. **Cân nhắc dài hạn:** tách cổng công khai sang **SSR/SSG (Next.js)** nếu SEO là ưu tiên lớn — thay đổi lớn, không làm ở bản 1.0.

> Lưu ý: OG image phải là URL tuyệt đối (`https://domain/v1/public/media/{id}`), kích thước ~1200×630.

---

## 8. BẢO MẬT & HIỆU NĂNG TẦNG CÔNG KHAI (khác biệt cốt lõi so với phần nội bộ)

- **Chống XSS ở nội dung rich text (QUAN TRỌNG NHẤT):** `content`/`description` là HTML do người soạn nhập → **phải sanitize** trước khi lưu **và/hoặc** trước khi render (dùng **OWASP Java HTML Sanitizer** hoặc Jsoup ở backend; nếu render ở FE thì `DOMPurify`). Không bao giờ `dangerouslySetInnerHTML` với HTML thô chưa lọc.
- **Chỉ lộ nội dung đã đăng:** service tầng public luôn lọc `status = PUBLISHED AND publishedAt <= now`; DRAFT/ARCHIVED không bao giờ ra ngoài, kể cả đoán URL.
- **Rate-limit các POST công khai** (contact, apply): tái dùng `SlidingWindowRateLimiter` + filter kiểu `AdmissionRateLimitFilter` để chống spam/bot.
- **CORS:** hiện `SecurityConfig` chỉ cho `http://localhost:3000/3001` → **thêm domain thật của cổng công khai** vào `allowedOrigins` (đọc từ biến môi trường, không hard-code).
- **Upload ảnh:** kiểm tra MIME thật + đuôi + kích thước (đã có multipart 10MB); cân nhắc chuẩn hoá/nén ảnh + strip EXIF; đặt tên ngẫu nhiên (đã có UUID); phục vụ với `Content-Type` đúng.
- **Hiệu năng/chịu tải:** trang công khai có thể bị nhiều lượt truy cập → thêm **HTTP cache header** (`Cache-Control`, `ETag`) cho GET public + cân nhắc **Spring Cache** cho danh sách/trang chủ; ảnh phục vụ với cache dài. Đây là nội dung đọc-nhiều-ghi-ít nên rất hợp cache.
- **`viewCount`:** tăng bất đồng bộ/gộp để không khoá ghi mỗi lượt xem (tránh nghẽn khi nhiều người đọc cùng lúc).
- **Không rò rỉ thông tin nội bộ:** DTO công khai chỉ trả field cần cho hiển thị (không lộ `author` chi tiết/nội bộ nếu không cần).

---

## 9. LỘ TRÌNH TRIỂN KHAI

### Giai đoạn P1 — Backend nền tảng (4–5 ngày)
- Entity + Flyway `V12` (`news_categories`, `news_articles`, `school_events`, `media_assets`; tuỳ chọn `contact_messages`, `hero_banners`).
- Service + repository + **sinh slug chống trùng** + lọc PUBLISHED.
- **Sanitize HTML** (thêm dependency OWASP Java HTML Sanitizer).
- Controller **public** (`/v1/public/**`) + controller **CMS** (`/v1/news`, `/v1/events`, `/v1/media`).
- Cập nhật `SecurityConfig`: xác nhận `/v1/public/**` permitAll (đã có) + thêm domain thật vào CORS + rate-limit filter cho `/v1/public/contact`.
- Integration test: public chỉ thấy PUBLISHED; DRAFT ẩn; CMS cần ADMIN/PRINCIPAL; sanitize loại bỏ `<script>`.

### Giai đoạn P2 — Frontend công khai (5–6 ngày)
- `PublicLayout` + tái cấu trúc định tuyến (`/` = cổng công khai, `/login` = đăng nhập) — **mục 2**.
- Trang chủ, Tin tức (list + detail), Sự kiện (list + detail), Tuyển sinh (nối `/apply`), Giới thiệu, Liên hệ.
- `react-helmet-async` (title/meta/OG) + `sitemap.xml`/`robots.txt` + slug URL.
- Mobile-first, `React.lazy`, dark/light.

### Giai đoạn P3 — CMS quản trị nội dung (3–4 ngày)
- `NewsManagement.jsx` + `EventManagement.jsx` (DataTable + Dialog + rich editor + upload ảnh + đăng/gỡ + xem trước).
- Thêm mục menu ADMIN/PRINCIPAL trong `navigation.js`.

### Giai đoạn P4 — SEO nâng cao & hoàn thiện (2–3 ngày)
- Prerender route công khai (bot đọc được OG/nội dung).
- Cache header + Spring Cache cho danh sách/trang chủ.
- QA responsive + kiểm tra preview share Facebook/Zalo + tốc độ tải.

**Tổng ước lượng: ~3–3.5 tuần** (1 BE + 1 FE song song). Nếu bỏ CMS ở bản đầu và nhập tin trực tiếp qua API/Swagger thì rút ngắn ~1 tuần, nhưng nhà trường sẽ khó tự đăng bài.

---

## 10. RỦI RO

- **XSS từ nội dung rich text** — rủi ro bảo mật lớn nhất của trang công khai; bắt buộc sanitize, có test.
- **SEO của SPA** — nếu chỉ CSR + helmet mà không prerender, bot Zalo/Facebook có thể không đọc được OG → link share không có preview; cần P4.
- **Đổi định tuyến `/`** — ảnh hưởng luồng đăng nhập hiện tại; phải cập nhật mọi chỗ redirect (`api.js` interceptor 401 đang `window.location.href = '/'` → cần đổi sang `/login`).
- **Spam form công khai** (contact/apply) — cần rate-limit; cân nhắc thêm captcha nếu bị lạm dụng.
- **Chịu tải** — công khai nên lượng truy cập khó lường (đợt tuyển sinh cao điểm); cần cache + `viewCount` không khoá.
- **CORS/domain** — quên thêm domain thật sẽ khiến FE gọi API bị chặn.

---

## 11. QUYẾT ĐỊNH CẦN CHỐT TRƯỚC KHI CODE

1. **Định tuyến:** đồng ý `/` = cổng công khai và chuyển đăng nhập sang `/login`? (mục 2 — ảnh hưởng lớn nhất).
2. **Phạm vi bản 1.0:** chỉ Tin tức + Sự kiện + Tuyển sinh, hay làm luôn Trang chủ giới thiệu đầy đủ + Liên hệ?
3. **Có form Liên hệ / lưu `ContactMessage`** công khai không (kèm rate-limit)?
4. **Trình soạn thảo rich text:** TipTap (hiện đại, headless) hay react-quill (đơn giản)?
5. **Mức đầu tư SEO:** chỉ helmet + sitemap (rẻ) hay làm prerender (P4) ngay?
6. **Ai quản trị nội dung:** chỉ ADMIN/PRINCIPAL, hay thêm vai trò `CONTENT_EDITOR`?
7. **Domain công khai thật** (để cấu hình CORS + URL tuyệt đối cho OG image).

---

*Tài liệu này là kế hoạch cho module Cổng thông tin công khai, bổ sung cho `KE_HOACH_NANG_CAP_V4.md`.*

---

## TRẠNG THÁI TRIỂN KHAI (v1.1 — 03/09/2026)

### ✅ P1 — Backend nền tảng (XONG)
- **Entity + Flyway `V11__public_portal.sql`**: `news_categories`, `news_articles`, `school_events`, `media_assets`, `contact_messages` (utf8mb4; index `status,published_at` + `slug`; seed 3 chuyên mục "Tuyển sinh/Hoạt động/Thông báo"). Enum `ContentStatus` (DRAFT/PUBLISHED/ARCHIVED).
- **Service**: `SlugService` (slugify tiếng Việt + chống trùng), `HtmlSanitizerService` (**OWASP Java HTML Sanitizer** — làm sạch `content`/`description` lúc GHI, allow-list, bỏ `<script>`/`javascript:`/`on*`), `NewsService`/`SchoolEventService` (CRUD + publish/unpublish + lọc `PUBLISHED AND publishedAt<=now`), `MediaAssetService` (upload ảnh JPEG/PNG/WebP/GIF ≤10MB qua `FileStorageService`), `ContactMessageService`, `PublicPortalService` (gộp trang chủ).
- **Controller công khai** (`permitAll`, `/v1/public/**`): `GET /home`, `/news`(+`?category=`, phân trang, bare array + `X-Total-Count` + `Cache-Control`), `/news/{slug}` (tăng `viewCount`), `/news/categories`, `/events`(+`?when=upcoming|past`), `/events/{slug}`, `/media/{id}` (bytes + Content-Type + cache 30 ngày), `POST /contact`.
- **Controller CMS** (`@PreAuthorize` ADMIN/PRINCIPAL): `/v1/news`(+`/{id}/publish`,`/unpublish`), `/v1/news-categories`, `/v1/events`, `/v1/media`, `/v1/contact-messages`(+`/{id}/handled`).
- **SecurityConfig**: thêm `/v1/public/**` vào `permitAll` (cả bản `/api/`); CORS đọc từ `app.cors.allowed-origins` (env `APP_CORS_ALLOWED_ORIGINS`); `ContactRateLimitFilter` (SlidingWindowRateLimiter) cho `POST /v1/public/contact`.
- **Test**: `PublicPortalIntegrationTest` (public ẩn DRAFT + tin hẹn giờ; sanitize loại `<script>`/`javascript:`; CMS cần ADMIN; contact form; home aggregate). *(Chạy đầy đủ cần MySQL / CI.)*

### ✅ P2 — Frontend công khai (XONG)
- **Định tuyến lại** (mục 2): `App.jsx` — nhánh public (`PublicLayout` + `<Outlet>`) **luôn có** cho mọi khách; `/` = `PublicHome` khi chưa đăng nhập; `/login` = `LoginPage`. Nhánh đã-đăng-nhập giữ nguyên `AppShell`. `api.js`: 401 redirect `/` → `/login`.
- **Trang**: `PublicHome` (hero + tin nổi bật + tin mới + sự kiện sắp tới, 1 request `/home`), `NewsListPage` (lọc chuyên mục + phân trang), `NewsDetailPage`, `EventListPage` (sắp tới/đã qua), `EventDetailPage`, `AdmissionsInfoPage` (CTA → `/apply` + tin mục `tuyen-sinh`), `AboutPage` (tĩnh), `ContactPage` (form → `POST /v1/public/contact`, xử lý 429).
- **`publicService.js`**: axios trần (không dính interceptor auth).
- **SEO**: `components/public/Seo.jsx` — không phụ thuộc thư viện, set `<title>` + `description` + Open Graph + Twitter Card, tự revert khi unmount. `public/robots.txt` + `public/sitemap.xml` (route tĩnh). `RichHtml` render HTML đã sanitize qua `dangerouslySetInnerHTML` (an toàn vì backend đã lọc).
- **Test**: `Seo.test.jsx`.

### ✅ P3 — CMS quản trị nội dung (XONG)
- `pages/NewsManagement.jsx` + `pages/EventManagement.jsx` (trong `AppShell`): `DataTable` + `Dialog` + editor **textarea HTML + nút "Xem trước"** (render `RichHtml`) + upload ảnh bìa (`/v1/media`) + đăng/gỡ/xoá; News có ô quản lý chuyên mục.
- `dataService.js`: `newsCmsService` / `eventCmsService` / `mediaCmsService` / `contactMessageCmsService`.
- `config/navigation.js`: thêm **"Tin tức (công khai)"** (`/news`) + **"Sự kiện (công khai)"** (`/events`) — ADMIN/PRINCIPAL.
- `npm test` **36/36**; `npm run build` sạch.

### ⏳ P4 — SEO nâng cao & hoàn thiện (CHƯA)
- Prerender route công khai (bot Zalo/Facebook không chạy JS → hiện chưa có preview khi share).
- `sitemap.xml` động (mỗi bài/sự kiện một URL) — hiện chỉ có route tĩnh.
- Spring Cache cho danh sách/trang chủ (hiện đã có `Cache-Control` HTTP, chưa có cache tầng ứng dụng).
- `viewCount` hiện `@Modifying UPDATE ... +1` mỗi lượt xem (chưa gộp/bất đồng bộ).
- QA responsive thật + đo tốc độ + kiểm tra unfurl Facebook/Zalo.
- WYSIWYG editor (TipTap/quill) thay cho textarea.
- Điền thông tin thật: địa chỉ/SĐT/email trường trong `PublicLayout` + `ContactPage`; tên trường trong `Seo.jsx` (`SITE_NAME`).

### Việc cấu hình khi lên thật
- `APP_CORS_ALLOWED_ORIGINS` = domain cổng công khai (cho phép trình duyệt khách gọi API).
- Ảnh OG cần URL tuyệt đối — `publicService.mediaUrl()` đã ghép `VITE_API_BASE_URL`; đảm bảo biến này trỏ domain thật.
