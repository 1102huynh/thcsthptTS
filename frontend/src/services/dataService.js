import api from './api';

// Staff Service
export const staffService = {
  getAll: () => api.get('/staff'),
  getById: (id) => api.get(`/staff/${id}`),
  create: (data) => api.post('/staff', data),
  update: (id, data) => api.put(`/staff/${id}`, data),
  delete: (id) => api.delete(`/staff/${id}`),
};

// Student Service
export const studentService = {
  getAll: () => api.get('/students'),
  getById: (id) => api.get(`/students/${id}`),
  getByClass: (className, section) => api.get(`/students/class/${className}?section=${section}`),
  create: (data) => api.post('/students', data),
  update: (id, data) => api.put(`/students/${id}`, data),
  delete: (id) => api.delete(`/students/${id}`),
};

// Library Service
export const libraryService = {
  getBooks: () => api.get('/library/books'),
  getBookById: (id) => api.get(`/library/books/${id}`),
  createBook: (data) => api.post('/library/books', data),
  updateBook: (id, data) => api.put(`/library/books/${id}`, data),
  deleteBook: (id) => api.delete(`/library/books/${id}`),
  borrowBook: (bookId, userId) => api.post(`/library/books/${bookId}/borrow`, { userId }),
  returnBook: (bookId, userId) => api.post(`/library/books/${bookId}/return`, { userId }),
  getTransactions: () => api.get('/library/transactions'),
};

// Attendance Service
export const attendanceService = {
  getByStudent: (studentId) => api.get(`/attendance/student/${studentId}`),
  getByClass: (className, section) => api.get(`/attendance/class/${className}?section=${section}`),
  markAttendance: (data) => api.post('/attendance', data),
  updateAttendance: (id, data) => api.put(`/attendance/${id}`, data),
  getPercentage: (studentId) => api.get(`/attendance/student/${studentId}/percentage`),
};

// Grade Service
export const gradeService = {
  getByStudent: (studentId) => api.get(`/grades/student/${studentId}`),
  getByClass: (className, section) => api.get(`/grades/class/${className}?section=${section}`),
  createGrade: (data) => api.post('/grades', data),
  updateGrade: (id, data) => api.put(`/grades/${id}`, data),
  deleteGrade: (id) => api.delete(`/grades/${id}`),
};

// Fee Service
export const feeService = {
  getByStudent: (studentId) => api.get(`/fees/student/${studentId}`),
  getByClass: (className) => api.get(`/fees/class/${className}`),
  createFee: (data) => api.post('/fees', data),
  updateFee: (id, data) => api.put(`/fees/${id}`, data),
  procesPayment: (feeId, amount) => api.post(`/fees/${feeId}/payment`, { amount }),
  getTotalDues: (studentId) => api.get(`/fees/student/${studentId}/total-dues`),
};

export default {
  staffService,
  studentService,
  libraryService,
  attendanceService,
  gradeService,
  feeService,
};

