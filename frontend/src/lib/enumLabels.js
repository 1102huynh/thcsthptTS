// Vietnamese display labels for backend enums, shared across every
// *Management page (Tuần 3+) so labels stay consistent instead of each
// page inventing its own. Values must match the Java enum constants
// exactly (StaffPosition.java, EmploymentStatus.java, Role.java, ...).

export const STAFF_POSITION_LABELS = {
  PRINCIPAL: 'Hiệu trưởng',
  VICE_PRINCIPAL: 'Phó hiệu trưởng',
  TEACHER: 'Giáo viên',
  LIBRARIAN: 'Thủ thư',
  ACCOUNTANT: 'Kế toán',
  ADMINISTRATOR: 'Quản trị viên',
  COUNSELOR: 'Tư vấn viên',
  NURSE: 'Y tá',
  MAINTENANCE: 'Bảo trì',
};

export const EMPLOYMENT_STATUS_LABELS = {
  ACTIVE: 'Đang làm việc',
  INACTIVE: 'Ngừng làm việc',
  ON_LEAVE: 'Đang nghỉ phép',
  TERMINATED: 'Đã chấm dứt HĐ',
  RETIRED: 'Đã nghỉ hưu',
};

export const ROLE_LABELS = {
  ADMIN: 'Quản trị viên',
  PRINCIPAL: 'Hiệu trưởng',
  TEACHER: 'Giáo viên',
  STUDENT: 'Học sinh',
  PARENT: 'Phụ huynh',
  LIBRARIAN: 'Thủ thư',
  ACCOUNTANT: 'Kế toán',
};

// Roles a staff account can plausibly hold (excludes STUDENT/PARENT, which
// go through the admissions flow, not "add staff").
export const STAFF_ROLE_OPTIONS = ['ADMIN', 'PRINCIPAL', 'TEACHER', 'LIBRARIAN', 'ACCOUNTANT'];

export const STUDENT_STATUS_LABELS = {
  ACTIVE: 'Đang học',
  INACTIVE: 'Tạm nghỉ',
  TRANSFERRED: 'Đã chuyển trường',
  EXPELLED: 'Đã bị đuổi học',
  GRADUATED: 'Đã tốt nghiệp',
};

// Student.gender is a free-text column, not a backend enum - these are
// just the options offered in the form, not values validated server-side.
export const GENDER_LABELS = {
  MALE: 'Nam',
  FEMALE: 'Nữ',
  OTHER: 'Khác',
};

export const BOOK_CATEGORY_LABELS = {
  FICTION: 'Tiểu thuyết',
  NON_FICTION: 'Phi hư cấu',
  REFERENCE: 'Tham khảo',
  ACADEMIC: 'Học thuật',
  BIOGRAPHY: 'Tiểu sử',
  HISTORY: 'Lịch sử',
  SCIENCE: 'Khoa học',
  MATHEMATICS: 'Toán học',
  LITERATURE: 'Văn học',
  LANGUAGE: 'Ngôn ngữ',
  ARTS: 'Nghệ thuật',
  SPORTS: 'Thể thao',
  OTHER: 'Khác',
};

export const BOOK_STATUS_LABELS = {
  AVAILABLE: 'Còn sách',
  BORROWED: 'Đã cho mượn hết',
  DAMAGED: 'Hư hỏng',
  LOST: 'Thất lạc',
  ARCHIVED: 'Lưu trữ',
  RESERVED: 'Đã đặt trước',
};

export const ATTENDANCE_STATUS_LABELS = {
  PRESENT: 'Có mặt',
  ABSENT: 'Vắng',
  LATE: 'Đi muộn',
  SICK_LEAVE: 'Nghỉ ốm',
  LEAVE_APPROVED: 'Nghỉ có phép',
  LEAVE_PENDING: 'Chờ duyệt nghỉ',
};

export const FEE_STATUS_LABELS = {
  PENDING: 'Chưa nộp',
  PARTIAL_PAID: 'Nộp một phần',
  PAID: 'Đã nộp đủ',
  OVERDUE: 'Quá hạn',
  EXEMPTED: 'Miễn giảm',
  CANCELLED: 'Đã hủy',
};

// Fee.feeType is a free-text column, not a backend enum - these are just
// the presets offered when creating a fee, any string is otherwise valid.
export const FEE_TYPE_PRESETS = ['Học phí', 'Bảo hiểm y tế', 'Đồng phục', 'Sách vở', 'Ăn bán trú', 'Khác'];

export const PAYMENT_METHOD_LABELS = {
  CASH: 'Tiền mặt',
  BANK_TRANSFER: 'Chuyển khoản',
  CARD: 'Thẻ',
  ONLINE: 'Thanh toán online',
};

export function toOptions(labelMap, keys = Object.keys(labelMap)) {
  return keys.map((value) => ({ value, label: labelMap[value] ?? value }));
}
