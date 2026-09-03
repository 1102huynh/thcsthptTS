// Zero-dependency mock of the parts of the thcsthptTS backend the frontend
// needs to explore the PUBLIC PORTAL and its CMS without a real
// Spring Boot + MySQL stack.
//
//   node scripts/mock-api.mjs        # listens on http://localhost:8080
//
// Then run the frontend (cd frontend && npm run dev) and open
// http://localhost:3000 . Login with anything -> you get an ADMIN session,
// so the CMS pages ("Tin tức (công khai)" / "Sự kiện (công khai)") work too;
// create + publish updates the in-memory store and shows up on the public
// pages immediately. State resets when you stop the process.
//
// NOT a substitute for the real backend - no persistence, no validation,
// no auth checks, no HTML sanitizing. Dev convenience only.

import http from 'node:http';

const PORT = 8080;
const now = () => new Date().toISOString();
const daysFromNow = (d) => new Date(Date.now() + d * 86400000).toISOString();

// ---- seed data -----------------------------------------------------------
let seq = 100;
const nextId = () => ++seq;

const categories = [
  { id: 1, name: 'Tuyển sinh', slug: 'tuyen-sinh', displayOrder: 1 },
  { id: 2, name: 'Hoạt động', slug: 'hoat-dong', displayOrder: 2 },
  { id: 3, name: 'Thông báo', slug: 'thong-bao', displayOrder: 3 },
];

const body = (title) =>
  `<p>${title}. Đây là nội dung mẫu do <strong>mock-api.mjs</strong> tạo ra để kiểm thử giao diện.</p>` +
  `<h2>Mục 1</h2><p>Nhà trường thông báo tới toàn thể phụ huynh và học sinh về kế hoạch trong thời gian tới.</p>` +
  `<ul><li>Điểm thứ nhất</li><li>Điểm thứ hai</li><li>Điểm thứ ba</li></ul>` +
  `<h2>Mục 2</h2><p>Mọi thắc mắc xin liên hệ văn phòng nhà trường trong giờ hành chính.</p>`;

let articles = [
  ['Thông báo tuyển sinh lớp 6 năm học 2026–2027', 'tuyen-sinh', true, -1],
  ['Hướng dẫn nộp hồ sơ trực tuyến đầu cấp', 'tuyen-sinh', false, -3],
  ['Lễ khai giảng năm học mới', 'hoat-dong', true, -5],
  ['Học sinh nhà trường đạt giải Học sinh giỏi cấp tỉnh', 'hoat-dong', false, -8],
  ['Lịch nghỉ lễ và kế hoạch dạy bù', 'thong-bao', false, -10],
  ['Thông báo họp phụ huynh học kỳ I', 'thong-bao', false, -12],
].map(([title, catSlug, featured, ageDays], i) => {
  const cat = categories.find((c) => c.slug === catSlug);
  return {
    id: nextId(),
    title,
    slug: slugify(title),
    summary: `${title} — tóm tắt ngắn hiển thị ở thẻ danh sách và thẻ chia sẻ mạng xã hội.`,
    content: body(title),
    coverImageUrl: `/v1/public/media/${(i % 4) + 1}`,
    categoryId: cat.id,
    categoryName: cat.name,
    categorySlug: cat.slug,
    status: 'PUBLISHED',
    publishedAt: daysFromNow(ageDays),
    isFeatured: featured,
    viewCount: Math.floor(Math.random() * 400),
    authorName: 'Ban Truyền thông',
    createdAt: daysFromNow(ageDays - 1),
    updatedAt: daysFromNow(ageDays),
  };
});
// one draft so the CMS list shows a non-published row
articles.push({
  id: nextId(), title: '(Bản nháp) Bài chưa đăng', slug: 'ban-nhap-bai-chua-dang',
  summary: 'Nháp', content: '<p>nháp</p>', coverImageUrl: null,
  categoryId: 3, categoryName: 'Thông báo', categorySlug: 'thong-bao',
  status: 'DRAFT', publishedAt: null, isFeatured: false, viewCount: 0,
  authorName: 'Ban Truyền thông', createdAt: now(), updatedAt: now(),
});

let events = [
  ['Ngày hội tư vấn tuyển sinh', 'Sân trường', 3, true],
  ['Giải bóng đá học sinh khối THPT', 'Nhà thi đấu', 10, false],
  ['Hội thảo hướng nghiệp lớp 12', 'Hội trường A', 18, true],
  ['Lễ tổng kết học kỳ I (đã qua)', 'Hội trường A', -6, false],
].map(([title, location, startDays, featured]) => ({
  id: nextId(),
  title,
  slug: slugify(title),
  description: body(title),
  coverImageUrl: `/v1/public/media/2`,
  location,
  startAt: daysFromNow(startDays),
  endAt: daysFromNow(startDays + 0.25),
  status: 'PUBLISHED',
  publishedAt: daysFromNow(-1),
  isFeatured: featured,
  createdAt: daysFromNow(-2),
  updatedAt: daysFromNow(-1),
}));

const contactMessages = [
  { id: nextId(), fullName: 'Nguyễn Văn A', email: 'a@example.com', phone: '0900000000',
    subject: 'Hỏi lịch tuyển sinh', message: 'Cho hỏi khi nào bắt đầu nhận hồ sơ ạ?',
    handled: false, createdAt: daysFromNow(-1) },
];

// ---- helpers -----------------------------------------------------------
function slugify(s) {
  return s.toLowerCase()
    .normalize('NFD').replace(/[̀-ͯ]/g, '')
    .replace(/đ/g, 'd')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/(^-|-$)/g, '');
}

function published(list) {
  const t = Date.now();
  return list.filter((x) => x.status === 'PUBLISHED' && x.publishedAt && new Date(x.publishedAt).getTime() <= t);
}

function publicNews(a, withContent) {
  return {
    slug: a.slug, title: a.title, summary: a.summary,
    content: withContent ? a.content : null,
    coverImageUrl: a.coverImageUrl, categoryName: a.categoryName, categorySlug: a.categorySlug,
    isFeatured: a.isFeatured, viewCount: a.viewCount, publishedAt: a.publishedAt,
  };
}

function svgPlaceholder(id) {
  const hue = (Number(id) * 67) % 360;
  return `<svg xmlns="http://www.w3.org/2000/svg" width="1200" height="630">
    <rect width="1200" height="630" fill="hsl(${hue} 60% 85%)"/>
    <text x="50%" y="50%" font-family="sans-serif" font-size="48" fill="hsl(${hue} 50% 35%)"
      text-anchor="middle" dominant-baseline="middle">Ảnh minh hoạ #${id}</text>
  </svg>`;
}

function fakeJwt(payload) {
  const b64 = (o) => Buffer.from(JSON.stringify(o)).toString('base64url');
  return `${b64({ alg: 'none', typ: 'JWT' })}.${b64(payload)}.mock`;
}

// ---- server ----------------------------------------------------------
const server = http.createServer(async (req, res) => {
  const url = new URL(req.url, `http://localhost:${PORT}`);
  const path = url.pathname.replace(/^\/api/, ''); // frontend baseURL is .../api
  const send = (status, obj, headers = {}) => {
    res.writeHead(status, {
      'Content-Type': 'application/json; charset=utf-8',
      'Access-Control-Allow-Origin': req.headers.origin || '*',
      'Access-Control-Allow-Credentials': 'true',
      'Access-Control-Allow-Headers': '*',
      'Access-Control-Allow-Methods': 'GET,POST,PUT,DELETE,OPTIONS',
      'Access-Control-Expose-Headers': 'X-Total-Count',
      ...headers,
    });
    res.end(obj === undefined ? '' : JSON.stringify(obj));
  };
  const readBody = () =>
    new Promise((resolve) => {
      let b = '';
      req.on('data', (c) => (b += c));
      req.on('end', () => { try { resolve(b ? JSON.parse(b) : {}); } catch { resolve({}); } });
    });

  if (req.method === 'OPTIONS') return send(204);

  const m = (method, re) => req.method === method && re.exec(path);
  let g;

  try {
    // ----- auth (any credentials -> ADMIN) -----
    if (m('POST', /^\/v1\/auth\/login$/)) {
      const { username = 'admin' } = await readBody();
      return send(200, {
        userId: 1, username, email: `${username}@school.local`,
        firstName: 'Quản', lastName: 'Trị', role: 'ADMIN',
        accessToken: fakeJwt({ sub: username, role: 'ADMIN', exp: Math.floor(Date.now() / 1000) + 86400 }),
        refreshToken: fakeJwt({ sub: username, type: 'refresh', exp: Math.floor(Date.now() / 1000) + 604800 }),
        tokenType: 'Bearer', issuedAt: now(), expiresAt: daysFromNow(1),
      });
    }
    if (m('POST', /^\/v1\/auth\/refresh-token$/)) {
      return send(200, {
        userId: 1, username: 'admin', role: 'ADMIN',
        accessToken: fakeJwt({ sub: 'admin', role: 'ADMIN', exp: Math.floor(Date.now() / 1000) + 86400 }),
        refreshToken: fakeJwt({ sub: 'admin', type: 'refresh', exp: Math.floor(Date.now() / 1000) + 604800 }),
        tokenType: 'Bearer', issuedAt: now(), expiresAt: daysFromNow(1),
      });
    }

    // ----- media placeholder -----
    if ((g = m('GET', /^\/v1\/public\/media\/(\d+)$/))) {
      res.writeHead(200, { 'Content-Type': 'image/svg+xml', 'Cache-Control': 'public, max-age=60',
        'Access-Control-Allow-Origin': '*' });
      return res.end(svgPlaceholder(g[1]));
    }

    // ----- public: home -----
    if (m('GET', /^\/v1\/public\/home$/)) {
      const pub = published(articles).sort((a, b) => b.publishedAt.localeCompare(a.publishedAt));
      return send(200, {
        featuredNews: pub.filter((a) => a.isFeatured).slice(0, 4).map((a) => publicNews(a, false)),
        latestNews: pub.slice(0, 6).map((a) => publicNews(a, false)),
        upcomingEvents: published(events)
          .filter((e) => new Date(e.startAt).getTime() >= Date.now())
          .sort((a, b) => a.startAt.localeCompare(b.startAt)).slice(0, 4),
      });
    }

    // ----- public: news -----
    if (m('GET', /^\/v1\/public\/news\/categories$/)) return send(200, categories);
    if (m('GET', /^\/v1\/public\/news$/)) {
      const cat = url.searchParams.get('category');
      const page = Number(url.searchParams.get('page') ?? 0);
      const size = Number(url.searchParams.get('size') ?? 12);
      let list = published(articles);
      if (cat) list = list.filter((a) => a.categorySlug === cat);
      list.sort((a, b) => (b.isFeatured - a.isFeatured) || b.publishedAt.localeCompare(a.publishedAt));
      const slice = list.slice(page * size, page * size + size).map((a) => publicNews(a, false));
      return send(200, slice, { 'X-Total-Count': String(list.length) });
    }
    if ((g = m('GET', /^\/v1\/public\/news\/([^/]+)$/))) {
      const a = published(articles).find((x) => x.slug === g[1]);
      if (!a) return send(404, { message: 'Bài viết không tồn tại hoặc chưa được đăng' });
      a.viewCount++;
      return send(200, publicNews(a, true));
    }

    // ----- public: events -----
    if (m('GET', /^\/v1\/public\/events$/)) {
      const when = url.searchParams.get('when');
      const page = Number(url.searchParams.get('page') ?? 0);
      const size = Number(url.searchParams.get('size') ?? 12);
      let list = published(events);
      const t = Date.now();
      if (when === 'upcoming') list = list.filter((e) => new Date(e.startAt).getTime() >= t);
      if (when === 'past') list = list.filter((e) => new Date(e.startAt).getTime() < t);
      list.sort((a, b) => a.startAt.localeCompare(b.startAt));
      return send(200, list.slice(page * size, page * size + size), { 'X-Total-Count': String(list.length) });
    }
    if ((g = m('GET', /^\/v1\/public\/events\/([^/]+)$/))) {
      const e = published(events).find((x) => x.slug === g[1]);
      return e ? send(200, e) : send(404, { message: 'Sự kiện không tồn tại hoặc chưa được đăng' });
    }

    // ----- public: contact -----
    if (m('POST', /^\/v1\/public\/contact$/)) {
      const b = await readBody();
      contactMessages.unshift({ id: nextId(), ...b, handled: false, createdAt: now() });
      return send(201, { message: 'Đã gửi liên hệ. Nhà trường sẽ phản hồi sớm.' });
    }

    // ----- CMS: news -----
    if (m('GET', /^\/v1\/news$/)) {
      return send(200, articles.map(cmsArticle), { 'X-Total-Count': String(articles.length) });
    }
    if ((g = m('GET', /^\/v1\/news\/(\d+)$/))) {
      const a = articles.find((x) => x.id === Number(g[1]));
      return a ? send(200, cmsArticle(a)) : send(404, { message: 'not found' });
    }
    if (m('POST', /^\/v1\/news$/)) {
      const b = await readBody();
      const cat = categories.find((c) => c.id === Number(b.categoryId));
      const a = {
        id: nextId(), title: b.title, slug: slugify(b.title || 'bai-viet'),
        summary: b.summary ?? null, content: b.content ?? null, coverImageUrl: b.coverImageUrl ?? null,
        categoryId: cat?.id ?? null, categoryName: cat?.name ?? null, categorySlug: cat?.slug ?? null,
        status: 'DRAFT', publishedAt: null, isFeatured: !!b.isFeatured, viewCount: 0,
        authorName: 'Quản Trị', createdAt: now(), updatedAt: now(),
      };
      articles.unshift(a);
      return send(201, cmsArticle(a));
    }
    if ((g = m('PUT', /^\/v1\/news\/(\d+)\/(publish|unpublish)$/))) {
      const a = articles.find((x) => x.id === Number(g[1]));
      if (!a) return send(404, {});
      if (g[2] === 'publish') { a.status = 'PUBLISHED'; a.publishedAt = a.publishedAt || now(); }
      else a.status = 'ARCHIVED';
      a.updatedAt = now();
      return send(200, cmsArticle(a));
    }
    if ((g = m('PUT', /^\/v1\/news\/(\d+)$/))) {
      const a = articles.find((x) => x.id === Number(g[1]));
      if (!a) return send(404, {});
      const b = await readBody();
      const cat = categories.find((c) => c.id === Number(b.categoryId));
      Object.assign(a, {
        title: b.title ?? a.title, summary: b.summary ?? null, content: b.content ?? null,
        coverImageUrl: b.coverImageUrl ?? null,
        categoryId: cat?.id ?? null, categoryName: cat?.name ?? null, categorySlug: cat?.slug ?? null,
        isFeatured: b.isFeatured ?? a.isFeatured, updatedAt: now(),
      });
      return send(200, cmsArticle(a));
    }
    if ((g = m('DELETE', /^\/v1\/news\/(\d+)$/))) {
      articles = articles.filter((x) => x.id !== Number(g[1]));
      return send(204);
    }

    // ----- CMS: news categories -----
    if (m('GET', /^\/v1\/news-categories$/)) return send(200, categories);
    if (m('POST', /^\/v1\/news-categories$/)) {
      const b = await readBody();
      const c = { id: nextId(), name: b.name, slug: slugify(b.name), displayOrder: b.displayOrder ?? 0 };
      categories.push(c);
      return send(201, c);
    }

    // ----- CMS: events -----
    if (m('GET', /^\/v1\/events$/)) return send(200, events, { 'X-Total-Count': String(events.length) });
    if (m('POST', /^\/v1\/events$/)) {
      const b = await readBody();
      const e = {
        id: nextId(), title: b.title, slug: slugify(b.title || 'su-kien'),
        description: b.description ?? null, coverImageUrl: b.coverImageUrl ?? null,
        location: b.location ?? null, startAt: b.startAt, endAt: b.endAt ?? null,
        status: 'DRAFT', publishedAt: null, isFeatured: !!b.isFeatured,
        createdAt: now(), updatedAt: now(),
      };
      events.unshift(e);
      return send(201, e);
    }
    if ((g = m('PUT', /^\/v1\/events\/(\d+)\/(publish|unpublish)$/))) {
      const e = events.find((x) => x.id === Number(g[1]));
      if (!e) return send(404, {});
      if (g[2] === 'publish') { e.status = 'PUBLISHED'; e.publishedAt = e.publishedAt || now(); }
      else e.status = 'ARCHIVED';
      return send(200, e);
    }
    if ((g = m('PUT', /^\/v1\/events\/(\d+)$/))) {
      const e = events.find((x) => x.id === Number(g[1]));
      if (!e) return send(404, {});
      Object.assign(e, await readBody(), { updatedAt: now() });
      return send(200, e);
    }
    if ((g = m('DELETE', /^\/v1\/events\/(\d+)$/))) {
      events = events.filter((x) => x.id !== Number(g[1]));
      return send(204);
    }

    // ----- CMS: contact messages -----
    if (m('GET', /^\/v1\/contact-messages$/)) {
      return send(200, contactMessages, { 'X-Total-Count': String(contactMessages.length) });
    }
    if ((g = m('PUT', /^\/v1\/contact-messages\/(\d+)\/handled$/))) {
      const c = contactMessages.find((x) => x.id === Number(g[1]));
      if (c) c.handled = url.searchParams.get('handled') !== 'false';
      return send(200, c ?? {});
    }

    // ----- media upload (CMS) -----
    if (m('POST', /^\/v1\/media$/)) {
      const id = nextId();
      return send(201, { id, url: `/v1/public/media/${id}`, fileName: 'upload.png',
        contentType: 'image/png', sizeBytes: 12345 });
    }

    // ----- misc so the authed shell doesn't hard-error -----
    if (m('GET', /^\/v1\/dashboard\/stats$/)) {
      return send(200, { totalStudents: 0, totalStaff: 0, totalClasses: 0, recentAuditLogs: [] });
    }

    return send(404, { message: `mock-api: no handler for ${req.method} ${path}` });
  } catch (err) {
    return send(500, { message: String(err) });
  }
});

function cmsArticle(a) {
  return { ...a };
}

server.listen(PORT, () => {
  console.log(`mock-api listening on http://localhost:${PORT}  (frontend baseURL: http://localhost:${PORT}/api)`);
  console.log(`  ${published(articles).length} tin đã đăng, ${published(events).length} sự kiện đã đăng, +1 bản nháp`);
  console.log('  Đăng nhập với BẤT KỲ tài khoản nào -> phiên ADMIN. Ctrl+C để dừng (mất dữ liệu).');
});
