// Zero-dependency in-memory mock of the thcsthptTS backend, enough to click
// through the WHOLE app (public portal + every admin page) without a real
// Spring Boot + MySQL stack.
//
//   node scripts/mock-api.mjs        # http://localhost:8080
//   cd frontend && npm run dev       # http://localhost:3000
//
// Login with any credentials -> ADMIN session. State is in memory and
// resets on restart. No validation, no auth checks, no HTML sanitizing -
// dev convenience only, NOT a substitute for the real backend.

import http from 'node:http';

const PORT = 8080;
const now = () => new Date().toISOString();
const dPlus = (d) => new Date(Date.now() + d * 86400000).toISOString();
const dateOnly = (iso) => iso.slice(0, 10);
let seq = 1000;
const nid = () => ++seq;
const pick = (arr, i) => arr[i % arr.length];

// ===================================================================
// SEED
// ===================================================================
const FIRST = ['An', 'Bình', 'Châu', 'Dũng', 'Giang', 'Hà', 'Hải', 'Hoa', 'Hùng', 'Khánh', 'Lan', 'Linh', 'Mai', 'Minh', 'Nam', 'Nga', 'Ngọc', 'Phong', 'Phúc', 'Quân', 'Quỳnh', 'Sơn', 'Thảo', 'Trang', 'Trung', 'Tú', 'Vy', 'Yến'];
const LAST = ['Nguyễn Văn', 'Trần Thị', 'Lê Hoàng', 'Phạm Minh', 'Hoàng Thị', 'Vũ Đình', 'Đặng Thu', 'Bùi Quang'];
const nameAt = (i) => ({ firstName: pick(FIRST, i * 7 + 3), lastName: pick(LAST, i) });

const academicYears = [
  { id: 1, name: '2025-2026', startDate: '2025-09-05', endDate: '2026-05-31', status: 'ACTIVE' },
  { id: 2, name: '2024-2025', startDate: '2024-09-05', endDate: '2025-05-31', status: 'CLOSED' },
];
const semesters = [
  { id: 11, name: 'HK1', academicYearId: 1, academicYearName: '2025-2026', startDate: '2025-09-05', endDate: '2026-01-15' },
  { id: 12, name: 'HK2', academicYearId: 1, academicYearName: '2025-2026', startDate: '2026-01-16', endDate: '2026-05-31' },
  { id: 21, name: 'HK1', academicYearId: 2, academicYearName: '2024-2025', startDate: '2024-09-05', endDate: '2025-01-15' },
  { id: 22, name: 'HK2', academicYearId: 2, academicYearName: '2024-2025', startDate: '2025-01-16', endDate: '2025-05-31' },
];
let subjects = [
  ['TOAN', 'Toán'], ['VAN', 'Ngữ văn'], ['ANH', 'Tiếng Anh'], ['LY', 'Vật lí'],
  ['HOA', 'Hoá học'], ['SINH', 'Sinh học'], ['SU', 'Lịch sử'], ['DIA', 'Địa lí'],
].map(([code, name], i) => ({ id: i + 1, code, name, gradeLevels: '6,7,8,9,10,11,12', category: 'BAT_BUOC' }));

let staff = Array.from({ length: 8 }).map((_, i) => {
  const nm = nameAt(i + 40);
  return {
    id: i + 1,
    employeeId: `GV${String(i + 1).padStart(3, '0')}`,
    position: i === 0 ? 'PRINCIPAL' : 'TEACHER',
    department: pick(['Toán - Tin', 'Ngữ văn', 'Ngoại ngữ', 'KHTN', 'KHXH'], i),
    status: 'ACTIVE',
    dateOfBirth: '1985-06-15',
    dateOfJoining: '2015-08-01',
    qualification: 'Cử nhân Sư phạm',
    subjectSpecialization: pick(subjects, i).name,
    salary: 15000000 + i * 500000,
    user: { id: 500 + i, username: `gv${i + 1}`, email: `gv${i + 1}@school.local`, role: 'TEACHER', enabled: true, ...nm },
  };
});

let classes = ['6A', '6B', '7A', '8A', '9A', '10A'].map((cn, i) => ({
  id: i + 1,
  className: cn,
  section: 'A',
  academicYear: '2025-2026',
  academicYearId: 1,
  roomNumber: `P.${101 + i}`,
  classTeacherId: staff[(i % 6) + 1].id,
  classTeacherName: `${staff[(i % 6) + 1].user.lastName} ${staff[(i % 6) + 1].user.firstName}`,
  studentCount: 5,
}));

let students = [];
classes.forEach((c, ci) => {
  for (let k = 0; k < 5; k++) {
    const idx = ci * 5 + k;
    const nm = nameAt(idx);
    students.push({
      id: 100 + idx,
      rollNumber: `HS${String(idx + 1).padStart(4, '0')}`,
      admissionNumber: `TS${String(idx + 1).padStart(4, '0')}`,
      status: 'ACTIVE',
      className: c.className,
      section: c.section,
      dateOfBirth: '2013-04-10',
      gender: idx % 2 ? 'FEMALE' : 'MALE',
      dateOfAdmission: '2025-08-20',
      fatherName: `${pick(LAST, idx)} ${pick(FIRST, idx + 1)}`,
      fatherPhone: `090${String(1000000 + idx).slice(-7)}`,
      motherName: `${pick(LAST, idx + 2)} ${pick(FIRST, idx + 3)}`,
      motherPhone: `091${String(2000000 + idx).slice(-7)}`,
      address: 'Số 1, đường ABC',
      city: 'Đà Nẵng',
      user: { id: 1000 + idx, username: `hs${idx + 1}`, email: `hs${idx + 1}@school.local`, role: 'STUDENT', enabled: true, ...nm },
    });
  }
});
const studentsInClass = (className, section) =>
  students.filter((s) => s.className === className && (!section || s.section === section));

const parentUsers = [
  { id: 700, username: 'ph1', email: 'ph1@example.com', firstName: 'Nguyễn Văn', lastName: 'Phụ Huynh', role: 'PARENT', enabled: true },
  { id: 701, username: 'ph2', email: 'ph2@example.com', firstName: 'Trần Thị', lastName: 'Phụ Huynh', role: 'PARENT', enabled: true },
];
let parentRelations = [
  { id: nid(), parentId: 700, studentId: students[0].id, studentName: full(students[0]), rollNumber: students[0].rollNumber, relationship: 'CHA', isPrimaryContact: true },
  { id: nid(), parentId: 700, studentId: students[1].id, studentName: full(students[1]), rollNumber: students[1].rollNumber, relationship: 'CHA', isPrimaryContact: false },
  { id: nid(), parentId: 701, studentId: students[6].id, studentName: full(students[6]), rollNumber: students[6].rollNumber, relationship: 'ME', isPrimaryContact: true },
];

let libraryBooks = [
  ['978-604-1', 'Toán 9 - Tập 1', 'Bộ GD&ĐT', 'ACADEMIC'],
  ['978-604-2', 'Ngữ văn 9 - Tập 2', 'Bộ GD&ĐT', 'LITERATURE'],
  ['978-604-3', 'Dế Mèn phiêu lưu ký', 'Tô Hoài', 'FICTION'],
  ['978-604-4', 'Lịch sử Việt Nam bằng tranh', 'Trần Bạch Đằng', 'HISTORY'],
  ['978-604-5', 'Từ điển Anh - Việt', 'NXB Giáo dục', 'REFERENCE'],
  ['978-604-6', 'Vũ trụ trong vỏ hạt dẻ', 'Stephen Hawking', 'SCIENCE'],
  ['978-604-7', 'Bài tập Vật lí 9', 'Nhiều tác giả', 'ACADEMIC'],
  ['978-604-8', 'Hoàng tử bé', 'A. de Saint-Exupéry', 'FICTION'],
].map(([isbn, title, author, category], i) => ({
  id: i + 1, isbn, title, author, category,
  totalCopies: 5, availableCopies: i % 3 === 0 ? 3 : 5,
  status: i % 3 === 0 ? 'AVAILABLE' : 'AVAILABLE',
  publisher: 'NXB Giáo dục', publishedYear: 2020 + (i % 4), shelfLocation: `K${i + 1}`,
}));
let libraryTx = [
  { id: nid(), bookId: 1, bookTitle: libraryBooks[0].title, borrowerId: students[0].user.id, borrowerName: full(students[0]),
    transactionType: 'BORROW', borrowDate: dateOnly(dPlus(-5)), dueDate: dateOnly(dPlus(9)), returnDate: null, fineAmount: null },
  { id: nid(), bookId: 4, bookTitle: libraryBooks[3].title, borrowerId: students[6].user.id, borrowerName: full(students[6]),
    transactionType: 'BORROW', borrowDate: dateOnly(dPlus(-2)), dueDate: dateOnly(dPlus(12)), returnDate: null, fineAmount: null },
];

let gradeConfigs = [
  ['MIENG', 1], ['MUOI_LAM_PHUT', 1], ['MOT_TIET', 2], ['GIUA_KY', 2], ['CUOI_KY', 3],
].map(([componentType, weight], i) => ({ id: i + 1, componentType, weight, appliesFrom: '2025-2026' }));

let promotionThresholds = [
  { id: 1, name: 'Ngưỡng mặc định', minSubjectAverage: 3.5, minConduct: 'TRUNG_BINH', maxAbsenceRate: 0.25, academicYearId: 1 },
];

let teachingAssignments = [];
classes.forEach((c) => {
  subjects.slice(0, 5).forEach((sub, si) => {
    const t = staff[(si % 7) + 1];
    teachingAssignments.push({
      id: nid(), classId: c.id, className: c.className,
      subjectId: sub.id, subjectName: sub.name,
      teacherId: t.id, teacherName: full(t),
      academicYearId: 1, academicYearName: '2025-2026', semesterId: 11,
    });
  });
});

const timetableByClass = {};
classes.forEach((c) => {
  const slots = [];
  for (let day = 2; day <= 6; day++) {
    for (let p = 1; p <= 4; p++) {
      const sub = pick(subjects, day + p);
      const t = staff[((day + p) % 7) + 1];
      slots.push({
        id: nid(), classId: c.id, className: c.className,
        dayOfWeek: day, period: p, subjectId: sub.id, subjectName: sub.name,
        teacherId: t.id, teacherName: full(t), room: c.roomNumber, semesterId: 11,
      });
    }
  }
  timetableByClass[c.id] = slots;
});

let fees = [];
students.forEach((s, i) => {
  const paid = i % 4 === 0;
  const partial = i % 4 === 1;
  fees.push({
    id: nid(), studentId: s.id, studentName: full(s), academicYear: '2025-2026',
    feeType: 'Học phí học kỳ 1', amount: 1500000,
    paidAmount: paid ? 1500000 : partial ? 800000 : 0,
    remainingAmount: paid ? 0 : partial ? 700000 : 1500000,
    status: paid ? 'PAID' : partial ? 'PARTIAL_PAID' : i % 4 === 2 ? 'OVERDUE' : 'PENDING',
    dueDate: '2025-10-15', paidDate: paid ? '2025-10-01' : null,
    paymentMethod: paid ? 'Tiền mặt' : null, transactionId: paid ? `TXN${1000 + i}` : null,
  });
  if (i % 3 === 0) {
    fees.push({
      id: nid(), studentId: s.id, studentName: full(s), academicYear: '2025-2026',
      feeType: 'Bảo hiểm y tế', amount: 700000, paidAmount: 0, remainingAmount: 700000,
      status: 'PENDING', dueDate: '2025-11-30', paidDate: null,
    });
  }
});

const conductByKey = {}; // `${classId}:${semesterId}` -> roster
function conductRoster(classId, semesterId) {
  const key = `${classId}:${semesterId}`;
  if (!conductByKey[key]) {
    const cls = classes.find((c) => c.id === Number(classId));
    conductByKey[key] = studentsInClass(cls?.className).map((s, i) => ({
      id: nid(), studentId: s.id, studentName: full(s), rollNumber: s.rollNumber,
      semesterId: Number(semesterId),
      rating: i % 3 === 0 ? 'TOT' : i % 3 === 1 ? 'KHA' : null,
      remarks: i % 3 === 2 ? null : 'Ngoan, tích cực',
    }));
  }
  return conductByKey[key];
}

const gradeRecordsByStudentSem = {}; // `${studentId}:${semesterId}` -> [records]
function gradeRecords(studentId, semesterId) {
  const key = `${studentId}:${semesterId}`;
  if (!gradeRecordsByStudentSem[key]) {
    const recs = [];
    subjects.slice(0, 5).forEach((sub) => {
      ['MIENG', 'MUOI_LAM_PHUT', 'MOT_TIET', 'GIUA_KY', 'CUOI_KY'].forEach((ct) => {
        recs.push({
          id: nid(), studentId: Number(studentId), subjectId: sub.id, subjectName: sub.name,
          semesterId: Number(semesterId), componentType: ct,
          score: Math.round((5.5 + Math.random() * 4) * 10) / 10,
          teacherId: staff[1].id, teacherName: full(staff[1]), classification: null,
        });
      });
    });
    gradeRecordsByStudentSem[key] = recs;
  }
  return gradeRecordsByStudentSem[key];
}
function semesterSummary(studentId, semesterId) {
  const recs = gradeRecords(studentId, semesterId);
  const bySub = {};
  for (const r of recs) (bySub[r.subjectId] ??= []).push(r);
  const wOf = (ct) => gradeConfigs.find((g) => g.componentType === ct)?.weight ?? 1;
  return Object.values(bySub).map((list) => {
    const wsum = list.reduce((a, r) => a + wOf(r.componentType), 0);
    const s = list.reduce((a, r) => a + r.score * wOf(r.componentType), 0);
    return { subjectId: list[0].subjectId, subjectName: list[0].subjectName, semesterId: Number(semesterId),
      semesterLabel: 'Học kỳ', average: Math.round((s / wsum) * 10) / 10, classification: null };
  });
}

let admissions = Array.from({ length: 5 }).map((_, i) => {
  const nm = nameAt(i + 90);
  return {
    id: nid(), applicantName: `${nm.lastName} ${nm.firstName}`,
    dateOfBirth: '2014-03-12', contactPhone: `098${String(3000000 + i).slice(-7)}`,
    desiredGradeLevel: 6, priorSchool: `Tiểu học số ${i + 1}`,
    status: i === 0 ? 'APPROVED' : i === 1 ? 'REVIEWING' : 'PENDING',
    submittedAt: dPlus(-i - 1), note: null, createdStudentId: i === 0 ? students[0].id : null,
    createdAt: dPlus(-i - 1), updatedAt: dPlus(-i),
  };
});

let auditLogs = Array.from({ length: 14 }).map((_, i) => ({
  id: nid(),
  actorId: 1, actorName: 'Quản Trị',
  action: pick(['CREATE', 'UPDATE', 'DELETE', 'APPROVE'], i),
  entityType: pick(['Student', 'Fee', 'GradeRecord', 'AdmissionApplication', 'SchoolClass'], i),
  entityId: 100 + i,
  detailJson: JSON.stringify({ field: 'sample', by: 'mock' }),
  occurredAt: dPlus(-i * 0.3),
}));

let notifications = [
  { id: nid(), title: 'Lịch nghỉ Tết Nguyên đán', content: '<p>Nhà trường thông báo lịch nghỉ Tết...</p>',
    channel: 'APP', createdByName: 'Quản Trị', status: 'SENT', readAt: null, sentAt: dPlus(-1) },
  { id: nid(), title: 'Họp phụ huynh học kỳ I', content: '<p>Kính mời quý phụ huynh...</p>',
    channel: 'EMAIL', createdByName: 'Quản Trị', status: 'SENT', readAt: dPlus(-2), sentAt: dPlus(-3) },
];

let contactMessages = [
  { id: nid(), fullName: 'Nguyễn Văn A', email: 'a@example.com', phone: '0900000000',
    subject: 'Hỏi lịch tuyển sinh', message: 'Cho hỏi khi nào bắt đầu nhận hồ sơ ạ?', handled: false, createdAt: dPlus(-1) },
];

// ---- public portal content ----
const newsCategories = [
  { id: 1, name: 'Tuyển sinh', slug: 'tuyen-sinh', displayOrder: 1 },
  { id: 2, name: 'Hoạt động', slug: 'hoat-dong', displayOrder: 2 },
  { id: 3, name: 'Thông báo', slug: 'thong-bao', displayOrder: 3 },
];
const artBody = (t) =>
  `<p>${t}. Nội dung mẫu do mock-api tạo.</p><h2>Chi tiết</h2><p>Nhà trường thông báo tới toàn thể phụ huynh và học sinh.</p><ul><li>Ý 1</li><li>Ý 2</li></ul>`;
let articles = [
  ['Thông báo tuyển sinh lớp 6 năm học 2026–2027', 'tuyen-sinh', true, -1],
  ['Hướng dẫn nộp hồ sơ trực tuyến đầu cấp', 'tuyen-sinh', false, -3],
  ['Lễ khai giảng năm học mới', 'hoat-dong', true, -5],
  ['Học sinh đạt giải Học sinh giỏi cấp tỉnh', 'hoat-dong', false, -8],
  ['Lịch nghỉ lễ và kế hoạch dạy bù', 'thong-bao', false, -10],
  ['Thông báo họp phụ huynh học kỳ I', 'thong-bao', false, -12],
].map(([title, slug, feat, age], i) => {
  const cat = newsCategories.find((c) => c.slug === slug);
  return { id: nid(), title, slug: slugify(title), summary: `${title} — tóm tắt ngắn.`, content: artBody(title),
    coverImageUrl: `/v1/public/media/${(i % 4) + 1}`, categoryId: cat.id, categoryName: cat.name, categorySlug: cat.slug,
    status: 'PUBLISHED', publishedAt: dPlus(age), isFeatured: feat, viewCount: (i * 37) % 400,
    authorName: 'Ban Truyền thông', createdAt: dPlus(age - 1), updatedAt: dPlus(age) };
});
articles.push({ id: nid(), title: '(Bản nháp) Bài chưa đăng', slug: 'ban-nhap', summary: 'Nháp', content: '<p>nháp</p>',
  coverImageUrl: null, categoryId: 3, categoryName: 'Thông báo', categorySlug: 'thong-bao',
  status: 'DRAFT', publishedAt: null, isFeatured: false, viewCount: 0, authorName: 'Ban Truyền thông', createdAt: now(), updatedAt: now() });
let events = [
  ['Ngày hội tư vấn tuyển sinh', 'Sân trường', 3, true],
  ['Giải bóng đá học sinh THPT', 'Nhà thi đấu', 10, false],
  ['Hội thảo hướng nghiệp lớp 12', 'Hội trường A', 18, true],
  ['Lễ tổng kết học kỳ I', 'Hội trường A', -6, false],
].map(([title, location, start, feat]) => ({
  id: nid(), title, slug: slugify(title), description: artBody(title), coverImageUrl: '/v1/public/media/2',
  location, startAt: dPlus(start), endAt: dPlus(start + 0.2), status: 'PUBLISHED', publishedAt: dPlus(-1),
  isFeatured: feat, createdAt: dPlus(-2), updatedAt: dPlus(-1),
}));

// ===================================================================
// helpers
// ===================================================================
function full(x) {
  const u = x.user ?? x;
  return `${u.lastName ?? ''} ${u.firstName ?? ''}`.trim();
}
function slugify(s) {
  return String(s).toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '')
    .replace(/đ/g, 'd').replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '');
}
function pubList(list) {
  const t = Date.now();
  return list.filter((x) => x.status === 'PUBLISHED' && x.publishedAt && new Date(x.publishedAt).getTime() <= t);
}
function publicNews(a, withContent) {
  return { slug: a.slug, title: a.title, summary: a.summary, content: withContent ? a.content : null,
    coverImageUrl: a.coverImageUrl, categoryName: a.categoryName, categorySlug: a.categorySlug,
    isFeatured: a.isFeatured, viewCount: a.viewCount, publishedAt: a.publishedAt };
}
function svg(id) {
  const h = (Number(id) * 67) % 360;
  return `<svg xmlns="http://www.w3.org/2000/svg" width="1200" height="630"><rect width="1200" height="630" fill="hsl(${h} 60% 85%)"/><text x="50%" y="50%" font-family="sans-serif" font-size="48" fill="hsl(${h} 50% 35%)" text-anchor="middle" dominant-baseline="middle">Ảnh #${id}</text></svg>`;
}
function fakeJwt(p) {
  const b = (o) => Buffer.from(JSON.stringify(o)).toString('base64url');
  return `${b({ alg: 'none', typ: 'JWT' })}.${b(p)}.mock`;
}
function paged(arr, url) {
  const size = Number(url.searchParams.get('size') ?? 20);
  const number = Number(url.searchParams.get('page') ?? 0);
  const content = arr.slice(number * size, number * size + size);
  return { content, totalElements: arr.length, totalPages: Math.max(1, Math.ceil(arr.length / size)), number, size };
}

// ===================================================================
// routing
// ===================================================================
const server = http.createServer(async (req, res) => {
  const url = new URL(req.url, `http://localhost:${PORT}`);
  const p = url.pathname.replace(/^\/api/, '');
  const method = req.method;
  const cors = {
    'Access-Control-Allow-Origin': req.headers.origin || '*',
    'Access-Control-Allow-Credentials': 'true',
    'Access-Control-Allow-Headers': '*',
    'Access-Control-Allow-Methods': 'GET,POST,PUT,DELETE,OPTIONS',
    'Access-Control-Expose-Headers': 'X-Total-Count',
  };
  const json = (status, obj, headers = {}) => {
    res.writeHead(status, { 'Content-Type': 'application/json; charset=utf-8', ...cors, ...headers });
    res.end(obj === undefined ? '' : JSON.stringify(obj));
  };
  const listH = (arr) => json(200, arr, { 'X-Total-Count': String(arr.length) });
  const readBody = () => new Promise((r) => { let b = ''; req.on('data', (c) => (b += c)); req.on('end', () => { try { r(b ? JSON.parse(b) : {}); } catch { r({}); } }); });
  const M = (mm, re) => method === mm && re.exec(p);
  let g;

  if (method === 'OPTIONS') return json(204);

  try {
    // ---------- auth ----------
    if (M('POST', /^\/v1\/auth\/(login|refresh-token)$/)) {
      const b = M('POST', /login$/) ? await readBody() : {};
      const username = b.username || 'admin';
      return json(200, {
        userId: 1, username, email: `${username}@school.local`, firstName: 'Quản', lastName: 'Trị', role: 'ADMIN',
        accessToken: fakeJwt({ sub: username, role: 'ADMIN', exp: Math.floor(Date.now() / 1000) + 86400 }),
        refreshToken: fakeJwt({ sub: username, type: 'refresh', exp: Math.floor(Date.now() / 1000) + 604800 }),
        tokenType: 'Bearer', issuedAt: now(), expiresAt: dPlus(1),
      });
    }

    // ---------- media (public) ----------
    if ((g = M('GET', /^\/v1\/public\/media\/(\d+)$/))) {
      res.writeHead(200, { 'Content-Type': 'image/svg+xml', 'Cache-Control': 'public,max-age=60', 'Access-Control-Allow-Origin': '*' });
      return res.end(svg(g[1]));
    }

    // ---------- public portal ----------
    if (M('GET', /^\/v1\/public\/home$/)) {
      const pub = pubList(articles).sort((a, b) => b.publishedAt.localeCompare(a.publishedAt));
      return json(200, {
        featuredNews: pub.filter((a) => a.isFeatured).slice(0, 4).map((a) => publicNews(a, false)),
        latestNews: pub.slice(0, 6).map((a) => publicNews(a, false)),
        upcomingEvents: pubList(events).filter((e) => new Date(e.startAt) >= new Date()).sort((a, b) => a.startAt.localeCompare(b.startAt)).slice(0, 4),
      });
    }
    if (M('GET', /^\/v1\/public\/news\/categories$/)) return json(200, newsCategories);
    if (M('GET', /^\/v1\/public\/news$/)) {
      const cat = url.searchParams.get('category');
      let list = pubList(articles);
      if (cat) list = list.filter((a) => a.categorySlug === cat);
      list.sort((a, b) => (b.isFeatured - a.isFeatured) || b.publishedAt.localeCompare(a.publishedAt));
      return listH(list.map((a) => publicNews(a, false)));
    }
    if ((g = M('GET', /^\/v1\/public\/news\/([^/]+)$/))) {
      const a = pubList(articles).find((x) => x.slug === g[1]);
      if (!a) return json(404, { message: 'Không tìm thấy' });
      a.viewCount++;
      return json(200, publicNews(a, true));
    }
    if (M('GET', /^\/v1\/public\/events$/)) {
      const when = url.searchParams.get('when');
      let list = pubList(events);
      const t = Date.now();
      if (when === 'upcoming') list = list.filter((e) => new Date(e.startAt).getTime() >= t);
      if (when === 'past') list = list.filter((e) => new Date(e.startAt).getTime() < t);
      list.sort((a, b) => a.startAt.localeCompare(b.startAt));
      return listH(list);
    }
    if ((g = M('GET', /^\/v1\/public\/events\/([^/]+)$/))) {
      const e = pubList(events).find((x) => x.slug === g[1]);
      return e ? json(200, e) : json(404, { message: 'Không tìm thấy' });
    }
    if (M('POST', /^\/v1\/public\/contact$/)) {
      contactMessages.unshift({ id: nid(), ...(await readBody()), handled: false, createdAt: now() });
      return json(201, { message: 'Đã gửi liên hệ.' });
    }

    // ---------- dashboard ----------
    if (M('GET', /^\/v1\/dashboard\/stats$/)) {
      return json(200, {
        activeStaffCount: staff.filter((s) => s.status === 'ACTIVE').length,
        activeStudentCount: students.filter((s) => s.status === 'ACTIVE').length,
        booksBorrowedCount: libraryTx.filter((t) => !t.returnDate).length,
        averageAttendanceRate: 94.3,
        totalOutstandingFees: fees.reduce((a, f) => a + (f.remainingAmount ?? 0), 0),
      });
    }

    // ---------- audit logs (Spring Page) ----------
    if (M('GET', /^\/v1\/audit-logs$/)) {
      const et = url.searchParams.get('entityType');
      let list = [...auditLogs].sort((a, b) => b.occurredAt.localeCompare(a.occurredAt));
      if (et) list = list.filter((x) => x.entityType.toLowerCase().includes(et.toLowerCase()));
      return json(200, paged(list, url));
    }

    // ---------- users ----------
    if (M('GET', /^\/v1\/users$/)) {
      const role = url.searchParams.get('role');
      const all = [...parentUsers, ...staff.map((s) => s.user)];
      return json(200, role ? all.filter((u) => u.role === role) : all);
    }
    if (M('POST', /^\/v1\/users$/)) {
      const b = await readBody();
      const u = { id: nid(), enabled: true, ...b };
      if (b.role === 'PARENT') parentUsers.push(u);
      return json(201, { ...u, accessToken: null, refreshToken: fakeJwt({ sub: b.username, exp: 0 }) });
    }

    // ---------- academic structure ----------
    if (M('GET', /^\/v1\/academic-years$/)) return json(200, academicYears);
    if (M('GET', /^\/v1\/semesters$/)) return json(200, semesters);
    if ((g = M('GET', /^\/v1\/semesters\/academic-year\/(\d+)$/)))
      return json(200, semesters.filter((s) => s.academicYearId === Number(g[1])));
    if (M('GET', /^\/v1\/subjects$/)) return json(200, subjects);

    // ---------- staff ----------
    if (M('GET', /^\/v1\/staff$/)) return listH(staff);
    if ((g = M('GET', /^\/v1\/staff\/(\d+)$/))) return json(200, staff.find((s) => s.id === +g[1]) ?? {});

    // ---------- students ----------
    if (M('GET', /^\/v1\/students$/)) return listH(students);
    if ((g = M('GET', /^\/v1\/students\/class\/([^/]+)\/section\/([^/]+)$/)))
      return json(200, studentsInClass(decodeURIComponent(g[1]), decodeURIComponent(g[2])));
    if ((g = M('GET', /^\/v1\/students\/(\d+)$/))) return json(200, students.find((s) => s.id === +g[1]) ?? {});
    if (M('GET', /^\/v1\/students\/me$/)) return json(200, students[0]);

    // ---------- classes ----------
    if (M('GET', /^\/v1\/classes$/)) return listH(classes);
    if ((g = M('GET', /^\/v1\/classes\/(\d+)\/students$/))) {
      const c = classes.find((x) => x.id === +g[1]);
      return json(200, studentsInClass(c?.className));
    }

    // ---------- library ----------
    if (M('GET', /^\/v1\/library\/books$/)) return listH(libraryBooks);
    if ((g = M('GET', /^\/v1\/library\/books\/(\d+)$/))) return json(200, libraryBooks.find((b) => b.id === +g[1]) ?? {});
    if (M('GET', /^\/v1\/library\/transactions$/)) return json(200, libraryTx.filter((t) => !t.returnDate));
    if (M('GET', /^\/v1\/library\/transactions\/me$/)) return json(200, []);

    // ---------- teaching assignments / timetable ----------
    if (M('GET', /^\/v1\/teaching-assignments$/)) return listH(teachingAssignments);
    if ((g = M('GET', /^\/v1\/timetable\/class\/(\d+)$/))) return json(200, timetableByClass[+g[1]] ?? []);
    if ((g = M('GET', /^\/v1\/timetable\/teacher\/(\d+)$/)))
      return json(200, Object.values(timetableByClass).flat().filter((s) => s.teacherId === +g[1]));

    // ---------- grade config / records ----------
    if (M('GET', /^\/v1\/grade-config$/)) return json(200, gradeConfigs);
    if ((g = M('GET', /^\/v1\/grade-records\/student\/(\d+)\/semester\/(\d+)$/)))
      return json(200, gradeRecords(g[1], g[2]));
    if ((g = M('GET', /^\/v1\/grade-records\/student\/(\d+)\/summary$/)))
      return json(200, semesterSummary(g[1], url.searchParams.get('semesterId') || 11));
    if ((g = M('GET', /^\/v1\/grade-records\/student\/(\d+)\/year-summary$/))) {
      const s1 = semesterSummary(g[1], 11);
      return json(200, s1.map((x) => ({ subjectId: x.subjectId, subjectName: x.subjectName, academicYearId: 1,
        academicYearName: '2025-2026', semester1Average: x.average, semester2Average: null, yearAverage: null, classification: null })));
    }

    // ---------- conduct ----------
    if ((g = M('GET', /^\/v1\/conduct\/class\/(\d+)\/semester\/(\d+)$/))) return json(200, conductRoster(g[1], g[2]));
    if ((g = M('GET', /^\/v1\/conduct\/student\/(\d+)$/))) return json(200, []);

    // ---------- attendance ----------
    if ((g = M('GET', /^\/v1\/attendance\/date\/([^/]+)$/))) return json(200, []);
    if (M('GET', /^\/v1\/attendance\/between$/)) return json(200, []);
    if ((g = M('GET', /^\/v1\/attendance\/student\/(\d+)\/percentage$/))) return json(200, 93.5);
    if ((g = M('GET', /^\/v1\/attendance\/student\/(\d+)$/))) return json(200, []);

    // ---------- fees ----------
    if ((g = M('GET', /^\/v1\/fees\/year\/([^/]+)$/))) return listH(fees.filter((f) => f.academicYear === decodeURIComponent(g[1])));
    if ((g = M('GET', /^\/v1\/fees\/status\/([^/]+)$/))) return json(200, fees.filter((f) => f.status === g[1]));
    if ((g = M('GET', /^\/v1\/fees\/student\/(\d+)\/total-dues$/)))
      return json(200, fees.filter((f) => f.studentId === +g[1]).reduce((a, f) => a + (f.remainingAmount ?? 0), 0));
    if ((g = M('GET', /^\/v1\/fees\/student\/(\d+)$/))) return json(200, fees.filter((f) => f.studentId === +g[1]));

    // ---------- promotions ----------
    if ((g = M('GET', /^\/v1\/promotions\/class\/(\d+)\/preview$/))) {
      const c = classes.find((x) => x.id === +g[1]);
      return json(200, studentsInClass(c?.className).map((s, i) => ({
        studentId: s.id, studentName: full(s), rollNumber: s.rollNumber,
        lowestSubjectAverage: Math.round((4 + Math.random() * 4) * 10) / 10,
        conduct: i % 3 === 0 ? 'TOT' : 'KHA', attendanceRate: 0.9 + Math.random() * 0.09,
        suggestedDecision: i % 5 === 0 ? 'O_LAI' : 'LEN_LOP',
        reasons: i % 5 === 0 ? ['Điểm TB thấp'] : [],
      })));
    }
    if ((g = M('GET', /^\/v1\/promotions\/student\/(\d+)$/))) return json(200, []);
    if (M('GET', /^\/v1\/promotion-thresholds$/)) return json(200, promotionThresholds);

    // ---------- parents ----------
    if ((g = M('GET', /^\/v1\/parents\/(\d+)\/children$/)))
      return json(200, parentRelations.filter((r) => r.parentId === +g[1]));

    // ---------- notifications ----------
    if (M('GET', /^\/v1\/notifications\/my$/)) return json(200, notifications);

    // ---------- admissions ----------
    if (M('GET', /^\/v1\/admissions$/)) {
      const st = url.searchParams.get('status');
      return json(200, st ? admissions.filter((a) => a.status === st) : admissions);
    }
    if ((g = M('GET', /^\/v1\/admissions\/(\d+)$/))) return json(200, admissions.find((a) => a.id === +g[1]) ?? {});

    // ---------- documents ----------
    if (M('GET', /^\/v1\/documents$/)) return json(200, []);

    // ---------- CMS: news / events / categories / contact / media ----------
    if (M('GET', /^\/v1\/news$/)) return listH(articles);
    if ((g = M('GET', /^\/v1\/news\/(\d+)$/))) return json(200, articles.find((a) => a.id === +g[1]) ?? {});
    if (M('GET', /^\/v1\/news-categories$/)) return json(200, newsCategories);
    if (M('GET', /^\/v1\/events$/)) return listH(events);
    if ((g = M('GET', /^\/v1\/events\/(\d+)$/))) return json(200, events.find((e) => e.id === +g[1]) ?? {});
    if (M('GET', /^\/v1\/contact-messages$/)) return listH(contactMessages);
    if (M('POST', /^\/v1\/media$/)) { const id = nid(); return json(201, { id, url: `/v1/public/media/${id}`, fileName: 'upload.png', contentType: 'image/png', sizeBytes: 1234 }); }

    if ((g = M('POST', /^\/v1\/news$/))) {
      const b = await readBody(); const cat = newsCategories.find((c) => c.id === +b.categoryId);
      const a = { id: nid(), title: b.title, slug: slugify(b.title || 'bai'), summary: b.summary ?? null, content: b.content ?? null,
        coverImageUrl: b.coverImageUrl ?? null, categoryId: cat?.id ?? null, categoryName: cat?.name ?? null, categorySlug: cat?.slug ?? null,
        status: 'DRAFT', publishedAt: null, isFeatured: !!b.isFeatured, viewCount: 0, authorName: 'Quản Trị', createdAt: now(), updatedAt: now() };
      articles.unshift(a); return json(201, a);
    }
    if ((g = M('PUT', /^\/v1\/news\/(\d+)\/(publish|unpublish)$/))) {
      const a = articles.find((x) => x.id === +g[1]); if (!a) return json(404, {});
      if (g[2] === 'publish') { a.status = 'PUBLISHED'; a.publishedAt = a.publishedAt || now(); } else a.status = 'ARCHIVED';
      return json(200, a);
    }
    if ((g = M('PUT', /^\/v1\/news\/(\d+)$/))) {
      const a = articles.find((x) => x.id === +g[1]); if (!a) return json(404, {});
      const b = await readBody(); const cat = newsCategories.find((c) => c.id === +b.categoryId);
      Object.assign(a, { title: b.title ?? a.title, summary: b.summary ?? null, content: b.content ?? null, coverImageUrl: b.coverImageUrl ?? null,
        categoryId: cat?.id ?? null, categoryName: cat?.name ?? null, categorySlug: cat?.slug ?? null, isFeatured: b.isFeatured ?? a.isFeatured, updatedAt: now() });
      return json(200, a);
    }
    if ((g = M('DELETE', /^\/v1\/news\/(\d+)$/))) { articles = articles.filter((x) => x.id !== +g[1]); return json(204); }
    if (M('POST', /^\/v1\/news-categories$/)) { const b = await readBody(); const c = { id: nid(), name: b.name, slug: slugify(b.name), displayOrder: b.displayOrder ?? 0 }; newsCategories.push(c); return json(201, c); }

    if ((g = M('POST', /^\/v1\/events$/))) {
      const b = await readBody();
      const e = { id: nid(), title: b.title, slug: slugify(b.title || 'sk'), description: b.description ?? null, coverImageUrl: b.coverImageUrl ?? null,
        location: b.location ?? null, startAt: b.startAt, endAt: b.endAt ?? null, status: 'DRAFT', publishedAt: null, isFeatured: !!b.isFeatured, createdAt: now(), updatedAt: now() };
      events.unshift(e); return json(201, e);
    }
    if ((g = M('PUT', /^\/v1\/events\/(\d+)\/(publish|unpublish)$/))) {
      const e = events.find((x) => x.id === +g[1]); if (!e) return json(404, {});
      if (g[2] === 'publish') { e.status = 'PUBLISHED'; e.publishedAt = e.publishedAt || now(); } else e.status = 'ARCHIVED';
      return json(200, e);
    }
    if ((g = M('PUT', /^\/v1\/events\/(\d+)$/))) { const e = events.find((x) => x.id === +g[1]); if (!e) return json(404, {}); Object.assign(e, await readBody(), { updatedAt: now() }); return json(200, e); }
    if ((g = M('DELETE', /^\/v1\/events\/(\d+)$/))) { events = events.filter((x) => x.id !== +g[1]); return json(204); }
    if ((g = M('PUT', /^\/v1\/contact-messages\/(\d+)\/handled$/))) {
      const c = contactMessages.find((x) => x.id === +g[1]); if (c) c.handled = url.searchParams.get('handled') !== 'false'; return json(200, c ?? {});
    }

    // ---------- generic writes for the rest: echo so dialogs don't error ----------
    if (method === 'POST' || method === 'PUT') {
      const b = await readBody();
      const idFromPath = (p.match(/\/(\d+)(?:\/[a-z-]+)?$/) || [])[1];
      return json(method === 'POST' ? 201 : 200, { id: idFromPath ? +idFromPath : nid(), ...b, _mock: true });
    }
    if (method === 'DELETE') return json(204);

    return json(404, { message: `mock-api: no handler for ${method} ${p}` });
  } catch (err) {
    return json(500, { message: String(err && err.stack || err) });
  }
});

server.listen(PORT, () => {
  console.log(`mock-api on http://localhost:${PORT}  (frontend VITE_API_BASE_URL: http://localhost:${PORT}/api)`);
  console.log(`  ${students.length} HS · ${classes.length} lớp · ${staff.length} GV · ${fees.length} khoản thu · ${pubList(articles).length} tin đã đăng`);
  console.log('  Đăng nhập BẤT KỲ tài khoản nào -> phiên ADMIN. Ctrl+C để dừng (mất dữ liệu).');
});
