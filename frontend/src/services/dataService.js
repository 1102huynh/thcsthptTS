import api from './api';

// Every path below is prefixed with /v1 to match the real backend
// (@RequestMapping("/v1/...") on every controller - StaffController,
// StudentController, LibraryController, AttendanceController,
// GradeController, FeeController, DashboardController, all confirmed).
// This file predated the backend's /v1 versioning and was never updated,
// so every single call in it 404'd - the root cause of "Failed to load
// statistics" on the Dashboard and (silently) every list page in the app.
// Found while wiring the Dashboard to real data (Tuần 3 Ngày 1-2).

// User Service (ADMIN-only account creation - POST /v1/users, see
// UserController/CreateUserRequest. StaffController's createStaff expects
// an existing user.id, not inline account fields, so creating a new staff
// member is a 2-step flow: create the account here first, then create the
// Staff record pointing at the returned userId.)
export const userService = {
  create: (data) => api.post('/v1/users', data),
  // ADMIN-only - added for ParentManagement.jsx to list existing PARENT
  // accounts when linking a child (UserRepository.findByRole existed
  // server-side already but was never exposed over HTTP before this).
  getByRole: (role) => api.get('/v1/users', { params: { role } }),
};

// Staff Service
export const staffService = {
  getAll: () => api.get('/v1/staff'),
  getById: (id) => api.get(`/v1/staff/${id}`),
  create: (data) => api.post('/v1/staff', data),
  update: (id, data) => api.put(`/v1/staff/${id}`, data),
  delete: (id) => api.delete(`/v1/staff/${id}`),
};

// Student Service
export const studentService = {
  getAll: () => api.get('/v1/students'),
  getById: (id) => api.get(`/v1/students/${id}`),
  // The calling STUDENT's own record (GET /v1/students/me) - the C3
  // self-service portal has the user id from the JWT but not the student
  // id every per-student endpoint needs.
  getMe: () => api.get('/v1/students/me'),
  getByClass: (className, section) => api.get(`/v1/students/class/${className}/section/${section}`),
  create: (data) => api.post('/v1/students', data),
  update: (id, data) => api.put(`/v1/students/${id}`, data),
  delete: (id) => api.delete(`/v1/students/${id}`),
};

// Library Service
export const libraryService = {
  getBooks: () => api.get('/v1/library/books'),
  getBookById: (id) => api.get(`/v1/library/books/${id}`),
  createBook: (data) => api.post('/v1/library/books', data),
  updateBook: (id, data) => api.put(`/v1/library/books/${id}`, data),
  deleteBook: (id) => api.delete(`/v1/library/books/${id}`),
  // Self-service only (LibraryController: hasAnyRole('TEACHER', 'STUDENT'))
  // - the backend reads the borrower from the JWT principal, not a body
  // field, and there is no "librarian lends book to student X" endpoint.
  // borrowDays is a query param, not a body field.
  borrowBook: (bookId, borrowDays = 14) =>
    api.post(`/v1/library/books/${bookId}/borrow`, null, { params: { borrowDays } }),
  returnBook: (bookId) => api.post(`/v1/library/books/${bookId}/return`),
  // ADMIN/LIBRARIAN: every currently-outstanding borrow, for circulation
  // visibility. Added alongside GET /v1/library/transactions{,/me} - see
  // LibraryController's own comment for why (nothing exposed
  // BookTransaction at all before).
  getTransactions: () => api.get('/v1/library/transactions'),
  // TEACHER/STUDENT: the caller's own borrow/return history, used to know
  // which books they currently have out (drives the Mượn/Trả button state).
  getMyTransactions: () => api.get('/v1/library/transactions/me'),
};

// Attendance Service
export const attendanceService = {
  getByStudent: (studentId) => api.get(`/v1/attendance/student/${studentId}`),
  // GET /v1/attendance/class/... never existed on the backend - only a
  // POST at that path (mark, see markClass below). School-wide-by-date is
  // the only "list" endpoint available; AttendanceManagement filters it to
  // one class's roster client-side.
  getByDate: (date) => api.get(`/v1/attendance/date/${date}`),
  getBetweenDates: (startDate, endDate) =>
    api.get('/v1/attendance/between', { params: { startDate, endDate } }),
  markAttendance: (data) => api.post('/v1/attendance', data),
  updateAttendance: (id, data) => api.put(`/v1/attendance/${id}`, data),
  deleteAttendance: (id) => api.delete(`/v1/attendance/${id}`),
  getPercentage: (studentId, startDate, endDate) =>
    api.get(`/v1/attendance/student/${studentId}/percentage`, { params: { startDate, endDate } }),
  // Bulk mark for a whole class - re-submitting the same class+date
  // replaces the previous rows (backend fix, Tuần 4 Ngày 2) rather than
  // duplicating them. presentStudentIds must be a repeated query param
  // (Spring's @RequestParam List<Long> expects `?presentStudentIds=1&
  // presentStudentIds=2`), not a JSON body - axios's *default* array param
  // serialization instead produces `presentStudentIds[]=1&...`, which
  // Spring silently binds to an EMPTY list (different param name) rather
  // than erroring, so this needs `paramsSerializer: { indexes: null }` to
  // suppress the brackets. Confirmed empirically via axios.getUri(), not
  // assumed.
  markClass: ({ className, section, date, presentStudentIds, status = 'ABSENT' }) =>
    api.post('/v1/attendance/class', null, {
      params: { className, section, date, presentStudentIds, status },
      paramsSerializer: { indexes: null },
    }),
};

export const schoolClassService = {
  getAll: () => api.get('/v1/classes'),
  create: (data) => api.post('/v1/classes', data),
  update: (id, data) => api.put(`/v1/classes/${id}`, data),
  delete: (id) => api.delete(`/v1/classes/${id}`),
  assignTeacher: (classId, staffId) => api.put(`/v1/classes/${classId}/teacher/${staffId}`),
  getStudents: (classId) => api.get(`/v1/classes/${classId}/students`),
};

// Grade Service
export const gradeService = {
  getByStudent: (studentId) => api.get(`/v1/grades/student/${studentId}`),
  // GET /v1/grades/class/... never existed on the backend (GradeController
  // only has per-student and school-wide-by-year queries) - getByYear is
  // what GradeManagement actually filters client-side to build a
  // by-class-and-subject view.
  getByYear: (academicYear) => api.get(`/v1/grades/year/${academicYear}`),
  createGrade: (data) => api.post('/v1/grades', data),
  updateGrade: (id, data) => api.put(`/v1/grades/${id}`, data),
  deleteGrade: (id) => api.delete(`/v1/grades/${id}`),
};

// Fee Service
export const feeService = {
  getByStudent: (studentId) => api.get(`/v1/fees/student/${studentId}`),
  // GET /v1/fees/class/... never existed on the backend - getByYear is the
  // real school-wide list endpoint FeeManagement uses.
  getByYear: (academicYear) => api.get(`/v1/fees/year/${academicYear}`),
  getByStatus: (status) => api.get(`/v1/fees/status/${status}`),
  createFee: (data) => api.post('/v1/fees', data),
  updateFee: (id, data) => api.put(`/v1/fees/${id}`, data),
  deleteFee: (id) => api.delete(`/v1/fees/${id}`),
  // processPayment's amount/paymentMethod are query params
  // (@RequestParam), not a JSON body - the old procesPayment (also
  // misspelled) sent `{ amount }` as a body, which Spring would just
  // ignore, leaving amount as null and 400ing.
  processPayment: (feeId, amount, paymentMethod = 'ONLINE') =>
    api.post(`/v1/fees/${feeId}/payment`, null, { params: { amount, paymentMethod } }),
  getTotalDues: (studentId) => api.get(`/v1/fees/student/${studentId}/total-dues`),
};

// Dashboard Service
export const dashboardService = {
  getStats: () => api.get('/v1/dashboard/stats'),
};

// Academic Year Service
export const academicYearService = {
  getAll: () => api.get('/v1/academic-years'),
};

// Subject Service (Môn học)
export const subjectService = {
  getAll: () => api.get('/v1/subjects'),
  create: (data) => api.post('/v1/subjects', data),
  update: (id, data) => api.put(`/v1/subjects/${id}`, data),
  delete: (id) => api.delete(`/v1/subjects/${id}`),
};

// Semester Service (Học kỳ)
export const semesterService = {
  getAll: () => api.get('/v1/semesters'),
  getByAcademicYear: (academicYearId) => api.get(`/v1/semesters/academic-year/${academicYearId}`),
  create: (data) => api.post('/v1/semesters', data),
  update: (id, data) => api.put(`/v1/semesters/${id}`, data),
  delete: (id) => api.delete(`/v1/semesters/${id}`),
};

// Teaching Assignment Service (Phân công giảng dạy) - no filtered-by-class/
// semester endpoint on the backend (only GET all), so callers filter the
// full list client-side, same pattern as gradeService/feeService's
// getByYear + client-side class filtering.
export const teachingAssignmentService = {
  getAll: () => api.get('/v1/teaching-assignments'),
  create: (data) => api.post('/v1/teaching-assignments', data),
  update: (id, data) => api.put(`/v1/teaching-assignments/${id}`, data),
  delete: (id) => api.delete(`/v1/teaching-assignments/${id}`),
};

// Timetable Service (Thời khoá biểu)
export const timetableService = {
  getByClass: (classId, semesterId) =>
    api.get(`/v1/timetable/class/${classId}`, { params: semesterId ? { semesterId } : {} }),
  getByTeacher: (teacherId, semesterId) =>
    api.get(`/v1/timetable/teacher/${teacherId}`, { params: semesterId ? { semesterId } : {} }),
  createSlot: (data) => api.post('/v1/timetable/slots', data),
  updateSlot: (id, data) => api.put(`/v1/timetable/slots/${id}`, data),
  deleteSlot: (id) => api.delete(`/v1/timetable/slots/${id}`),
};

// Grade Record Service (Điểm số theo TT22/2021, thang điểm 10) - supersedes
// gradeService's percentage-based model above (kept untouched for Phase
// 1-2 compatibility, not used by GradeManagement any more). No
// by-class/by-subject bulk endpoint exists (GradeRecordController only has
// per-student queries) - GradeManagement fetches per-student in parallel
// for the selected class roster, same "no bulk endpoint, fetch+filter"
// pattern as teachingAssignmentService above.
export const gradeRecordService = {
  create: (data) => api.post('/v1/grade-records', data),
  update: (id, data) => api.put(`/v1/grade-records/${id}`, data),
  delete: (id) => api.delete(`/v1/grade-records/${id}`),
  getById: (id) => api.get(`/v1/grade-records/${id}`),
  getStudentSemesterGrades: (studentId, semesterId) =>
    api.get(`/v1/grade-records/student/${studentId}/semester/${semesterId}`),
  getStudentSemesterSummary: (studentId, semesterId) =>
    api.get(`/v1/grade-records/student/${studentId}/summary`, { params: { semesterId } }),
  getStudentYearSummary: (studentId, academicYearId) =>
    api.get(`/v1/grade-records/student/${studentId}/year-summary`, { params: { academicYearId } }),
};

// Grade Component Config Service (hệ số điểm miệng/15 phút/1 tiết/giữa kỳ/
// cuối kỳ, theo năm học áp dụng) - ADMIN only, see GradeConfigController.
export const gradeConfigService = {
  getAll: () => api.get('/v1/grade-config'),
  create: (data) => api.post('/v1/grade-config', data),
  update: (id, data) => api.put(`/v1/grade-config/${id}`, data),
  delete: (id) => api.delete(`/v1/grade-config/${id}`),
};

// Conduct Service (Hạnh kiểm/rèn luyện) - no delete endpoint exists on the
// backend (ConductController only has create/update/read), matching the
// real workflow: a conduct evaluation is corrected via PUT, not removed.
// getClassSemesterRoster is a real bulk endpoint (unlike gradeRecordService
// above) - one row per student already in the class, rating/remarks
// pre-filled or null, no per-student N+1 fetching needed here.
export const conductService = {
  create: (data) => api.post('/v1/conduct', data),
  update: (id, data) => api.put(`/v1/conduct/${id}`, data),
  getByStudent: (studentId) => api.get(`/v1/conduct/student/${studentId}`),
  getClassSemesterRoster: (classId, semesterId) => api.get(`/v1/conduct/class/${classId}/semester/${semesterId}`),
};

// Promotion Service (Xét lên lớp/ở lại/tốt nghiệp) - getClassPreview is a
// live, unsaved computation (nothing persisted until confirm); confirm
// accepts an array so a whole class can be decided in one call
// ("hỗ trợ ghi đè hàng loạt" per the plan - confirming again for the same
// student+year overwrites the previous decision, it's not an error).
export const promotionService = {
  getClassPreview: (classId, academicYearId) =>
    api.get(`/v1/promotions/class/${classId}/preview`, { params: { academicYearId } }),
  confirm: (records) => api.post('/v1/promotions/confirm', records),
  getStudentHistory: (studentId) => api.get(`/v1/promotions/student/${studentId}`),
};

// Promotion Threshold Config Service (Ngưỡng xét lên lớp) - ADMIN/PRINCIPAL
// only, see PromotionThresholdController.
export const promotionThresholdService = {
  getAll: () => api.get('/v1/promotion-thresholds'),
  create: (data) => api.post('/v1/promotion-thresholds', data),
  update: (id, data) => api.put(`/v1/promotion-thresholds/${id}`, data),
  delete: (id) => api.delete(`/v1/promotion-thresholds/${id}`),
};

// Parent Service (Phụ huynh - links a PARENT account to their children)
export const parentService = {
  linkChild: (parentId, studentId, relationship, isPrimaryContact = false) =>
    api.post(`/v1/parents/${parentId}/children/${studentId}`, null, { params: { relationship, isPrimaryContact } }),
  unlinkChild: (parentId, studentId) => api.delete(`/v1/parents/${parentId}/children/${studentId}`),
  getChildren: (parentId) => api.get(`/v1/parents/${parentId}/children`),
};

// Notification Service (Sổ liên lạc điện tử) - created and sent in the same
// request; SMS/ZALO channels return 501 (pending vendor decision, see
// NotificationChannel's Javadoc) - the compose UI only offers APP/EMAIL.
export const notificationService = {
  createAndSend: (data) => api.post('/v1/notifications', data),
  getMy: () => api.get('/v1/notifications/my'),
  markAsRead: (recipientId) => api.put(`/v1/notifications/${recipientId}/read`),
};

// Admission Service (Tuyển sinh đầu cấp) - submit is public (no auth
// header needed, though `api`'s interceptor attaches one anyway if a
// session happens to be logged in - harmless, the endpoint ignores it);
// everything else is ADMIN-only.
export const admissionService = {
  submit: (data) => api.post('/v1/admissions', data),
  getAll: (status) => api.get('/v1/admissions', { params: status ? { status } : {} }),
  getById: (id) => api.get(`/v1/admissions/${id}`),
  updateStatus: (id, data) => api.put(`/v1/admissions/${id}/status`, data),
  approveAndCreate: (id, data) => api.post(`/v1/admissions/${id}/approve-and-create`, data),
};

// Report Service (Xuất báo cáo PDF/Excel) - every call needs
// `responseType: 'blob'` since these endpoints return raw file bytes with a
// Content-Disposition header, not JSON (see lib/download.js's
// triggerBlobDownload, which every caller uses instead of `.then(r => r.data)`).
export const reportService = {
  studentTranscript: (studentId, academicYearId) =>
    api.get(`/v1/reports/student/${studentId}/transcript`, { params: { academicYearId }, responseType: 'blob' }),
  classAttendance: (classId, from, to) =>
    api.get(`/v1/reports/class/${classId}/attendance`, { params: { from, to }, responseType: 'blob' }),
  feeReceipt: (feeId) => api.get(`/v1/reports/fees/receipt/${feeId}`, { responseType: 'blob' }),
};

// Document Service (Tệp đính kèm - hồ sơ học sinh/nhân sự/tuyển sinh) -
// upload sends multipart/form-data.
export const documentService = {
  upload: (file, ownerType, ownerId) => {
    const formData = new FormData();
    formData.append('file', file);
    // api.js's instance sets a default Content-Type: application/json
    // header - turns out (confirmed live, 500 "Content-Type 'application/
    // json' is not supported") that this app's axios setup does NOT
    // auto-strip it for a FormData body the way axios's docs describe,
    // so it silently overrides whatever boundary the browser would have
    // set. Explicitly clearing it here lets the browser generate the
    // correct multipart/form-data; boundary=... header itself.
    return api.post('/v1/documents', formData, {
      params: { ownerType, ownerId },
      headers: { 'Content-Type': undefined },
    });
  },
  listByOwner: (ownerType, ownerId) => api.get('/v1/documents', { params: { ownerType, ownerId } }),
  getById: (id) => api.get(`/v1/documents/${id}`),
  download: (id) => api.get(`/v1/documents/${id}/download`, { responseType: 'blob' }),
  delete: (id) => api.delete(`/v1/documents/${id}`),
};

// Audit Log Service (ADMIN only - see AuditLogController)
export const auditLogService = {
  getRecent: (size = 5) => api.get('/v1/audit-logs', { params: { page: 0, size } }),
  // Full paginated search (AuditLogManagement.jsx) - entityType/actorId are
  // optional server-side filters, page/size default to 0/20 there too if
  // omitted here.
  search: ({ entityType, actorId, page = 0, size = 20 } = {}) =>
    api.get('/v1/audit-logs', { params: { entityType, actorId, page, size } }),
};

export default {
  userService,
  staffService,
  studentService,
  libraryService,
  attendanceService,
  gradeService,
  feeService,
  dashboardService,
  academicYearService,
  subjectService,
  semesterService,
  teachingAssignmentService,
  timetableService,
  gradeRecordService,
  gradeConfigService,
  conductService,
  promotionService,
  promotionThresholdService,
  parentService,
  notificationService,
  admissionService,
  reportService,
  documentService,
  auditLogService,
};
