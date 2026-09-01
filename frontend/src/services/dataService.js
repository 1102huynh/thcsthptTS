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
  getByClass: (className, section) => api.get(`/v1/students/class/${className}?section=${section}`),
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
  getByClass: (className, section) => api.get(`/v1/attendance/class/${className}?section=${section}`),
  getBetweenDates: (startDate, endDate) =>
    api.get('/v1/attendance/between', { params: { startDate, endDate } }),
  markAttendance: (data) => api.post('/v1/attendance', data),
  updateAttendance: (id, data) => api.put(`/v1/attendance/${id}`, data),
  getPercentage: (studentId) => api.get(`/v1/attendance/student/${studentId}/percentage`),
};

// Grade Service
export const gradeService = {
  getByStudent: (studentId) => api.get(`/v1/grades/student/${studentId}`),
  getByClass: (className, section) => api.get(`/v1/grades/class/${className}?section=${section}`),
  createGrade: (data) => api.post('/v1/grades', data),
  updateGrade: (id, data) => api.put(`/v1/grades/${id}`, data),
  deleteGrade: (id) => api.delete(`/v1/grades/${id}`),
};

// Fee Service
export const feeService = {
  getByStudent: (studentId) => api.get(`/v1/fees/student/${studentId}`),
  getByClass: (className) => api.get(`/v1/fees/class/${className}`),
  getByYear: (academicYear) => api.get(`/v1/fees/year/${academicYear}`),
  createFee: (data) => api.post('/v1/fees', data),
  updateFee: (id, data) => api.put(`/v1/fees/${id}`, data),
  procesPayment: (feeId, amount) => api.post(`/v1/fees/${feeId}/payment`, { amount }),
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

// Audit Log Service (ADMIN only - see AuditLogController)
export const auditLogService = {
  getRecent: (size = 5) => api.get('/v1/audit-logs', { params: { page: 0, size } }),
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
  auditLogService,
};
