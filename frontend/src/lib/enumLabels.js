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

export function toOptions(labelMap, keys = Object.keys(labelMap)) {
  return keys.map((value) => ({ value, label: labelMap[value] ?? value }));
}
