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

export const SUBJECT_CATEGORY_LABELS = {
  BAT_BUOC: 'Bắt buộc',
  TU_CHON: 'Tự chọn',
};

export const SEMESTER_NAME_LABELS = {
  HK1: 'Học kỳ 1',
  HK2: 'Học kỳ 2',
};

// AdmissionApplication.status
export const ADMISSION_STATUS_LABELS = {
  PENDING: 'Chờ duyệt',
  REVIEWING: 'Đang xét duyệt',
  APPROVED: 'Đã duyệt',
  REJECTED: 'Từ chối',
};

// ParentStudentRelation.relationship
export const PARENT_RELATIONSHIP_LABELS = {
  CHA: 'Cha',
  ME: 'Mẹ',
  NGUOI_GIAM_HO: 'Người giám hộ',
};

// Notification.targetType - who the recipients are resolved from at send
// time (see NotificationTargetType's Javadoc for how targetId is
// interpreted per type).
export const NOTIFICATION_TARGET_TYPE_LABELS = {
  CLASS: 'Cả lớp',
  STUDENT: 'Một học sinh',
  ALL_PARENTS: 'Tất cả phụ huynh',
  STAFF: 'Một nhân viên',
};

// Notification.channel - SMS/ZALO exist as vocabulary but return 501
// (NotificationChannelUnavailableException) until a vendor/Zalo OA
// decision is made, per IMPLEMENTATION_PLAN.md 3.6 - the compose UI only
// offers APP/EMAIL as selectable, these two labels exist for display only
// (e.g. showing a channel on an already-sent notification).
export const NOTIFICATION_CHANNEL_LABELS = {
  APP: 'Trong ứng dụng',
  EMAIL: 'Email',
  SMS: 'SMS',
  ZALO: 'Zalo',
};

export const NOTIFICATION_STATUS_LABELS = {
  SENT: 'Đã gửi',
  PARTIALLY_SENT: 'Gửi một phần',
  FAILED: 'Gửi thất bại',
};

// PromotionRecord.decision - LEN_LOP/O_LAI/TOT_NGHIEP are the only ones the
// system ever suggests (PromotionService); RA_TRUONG (chuyển trường/thôi
// học...) is always a manual pick, never a suggestion.
export const PROMOTION_DECISION_LABELS = {
  LEN_LOP: 'Lên lớp',
  O_LAI: 'Ở lại',
  TOT_NGHIEP: 'Tốt nghiệp',
  RA_TRUONG: 'Ra trường',
};

// ConductRecord.rating - xếp loại hạnh kiểm/rèn luyện theo học kỳ, luôn đi
// kèm song song với học lực trong học bạ (IMPLEMENTATION_PLAN.md 3.4).
export const CONDUCT_RATING_LABELS = {
  TOT: 'Tốt',
  KHA: 'Khá',
  TRUNG_BINH: 'Trung bình',
  YEU: 'Yếu',
};

// GradeRecord.componentType, theo Thông tư 22/2021 (tương thích TT58) -
// mỗi loại có hệ số riêng, xem GradeComponentConfig / GradeManagement.
export const GRADE_COMPONENT_TYPE_LABELS = {
  MIENG: 'Miệng',
  MUOI_LAM_PHUT: '15 phút',
  MOT_TIET: '1 tiết',
  GIUA_KY: 'Giữa kỳ',
  CUOI_KY: 'Cuối kỳ',
};

// TimetableSlot.dayOfWeek follows the Vietnamese calendar convention: Thứ
// Hai (Monday) = 2 ... Thứ Bảy (Saturday) = 7 (no Sunday classes).
export const DAY_OF_WEEK_LABELS = {
  2: 'Thứ 2',
  3: 'Thứ 3',
  4: 'Thứ 4',
  5: 'Thứ 5',
  6: 'Thứ 6',
  7: 'Thứ 7',
};

export function toOptions(labelMap, keys = Object.keys(labelMap)) {
  return keys.map((value) => ({ value, label: labelMap[value] ?? value }));
}
