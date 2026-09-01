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

export function toOptions(labelMap, keys = Object.keys(labelMap)) {
  return keys.map((value) => ({ value, label: labelMap[value] ?? value }));
}
