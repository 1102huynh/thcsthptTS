import api from './api';

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
  getByUserId: (userId) => api.get(`/v1/students/user/${userId}`),
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
  borrowBook: (bookId, userId) => api.post(`/v1/library/books/${bookId}/borrow`, { userId }),
  returnBook: (bookId, userId) => api.post(`/v1/library/books/${bookId}/return`, { userId }),
  getTransactions: () => api.get('/v1/library/transactions'),
};

// Attendance Service
export const attendanceService = {
  getByStudent: (studentId) => api.get(`/v1/attendance/student/${studentId}`),
  getByClass: (className, section) => api.get(`/v1/attendance/class/${className}?section=${section}`),
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
  createFee: (data) => api.post('/v1/fees', data),
  updateFee: (id, data) => api.put(`/v1/fees/${id}`, data),
  procesPayment: (feeId, amount) => api.post(`/v1/fees/${feeId}/payment`, { amount }),
  getTotalDues: (studentId) => api.get(`/v1/fees/student/${studentId}/total-dues`),
};

export default {
  staffService,
  studentService,
  libraryService,
  attendanceService,
  gradeService,
  feeService,
};

