import {
  FiHome,
  FiUsers,
  FiBook,
  FiClipboard,
  FiAward,
  FiDollarSign,
  FiGrid,
  FiCalendar,
  FiSettings,
  FiCheckSquare,
  FiTrendingUp,
  FiUserPlus,
  FiBell,
  FiFileText,
  FiActivity,
  FiUser,
} from 'react-icons/fi';

// Shared between Sidebar (nav links) and Navbar (page title lookup by
// current route) - one source of truth instead of two menu lists drifting
// apart, which is exactly what happened with the old Sidebar.jsx (its list
// lived only there, Navbar had no idea what page it was on).
// Labels are Vietnamese (A6) - this is a Vietnamese lower/upper-secondary
// school system and every page's own content is already in Vietnamese; the
// English menu labels were the last holdout. These strings double as the
// page title shown in Navbar (pageTitleForPath), so they read as page
// headings, not just nav links.
export const NAV_ITEMS = [
  // DashboardController.getStats is ADMIN/PRINCIPAL only - TEACHER/STUDENT/
  // LIBRARIAN/ACCOUNTANT used to be listed here too but every one of them
  // 403s on the only request the page makes. TEACHER is kept (they have a
  // real landing need and the page degrades to empty tiles rather than
  // erroring hard); the self-service roles get "Trang của tôi" below
  // instead, and LIBRARIAN/ACCOUNTANT fall through to Library/Fees.
  { label: 'Tổng quan', href: '/', icon: FiHome, roles: ['ADMIN', 'PRINCIPAL', 'TEACHER'] },
  // C3 - the STUDENT/PARENT self-service portal (điểm, điểm danh, học phí,
  // hạnh kiểm of themselves / their linked children). First item for both
  // roles so it's where defaultPathForRole() lands them after login.
  { label: 'Trang của tôi', href: '/portal', icon: FiUser, roles: ['STUDENT', 'PARENT'] },
  { label: 'Quản lý nhân sự', href: '/staff', icon: FiUsers, roles: ['ADMIN', 'PRINCIPAL'] },
  { label: 'Quản lý học sinh', href: '/students', icon: FiUsers, roles: ['ADMIN', 'PRINCIPAL', 'TEACHER'] },
  // TEACHER can view (SchoolClassController's GET is ADMIN/PRINCIPAL/
  // TEACHER) but not manage (create/update/delete/assign are ADMIN/
  // PRINCIPAL only) - excluded here since this page is full CRUD, same
  // scoping choice as Staff Management just above.
  { label: 'Quản lý lớp học', href: '/classes', icon: FiGrid, roles: ['ADMIN', 'PRINCIPAL'] },
  { label: 'Thư viện', href: '/library', icon: FiBook, roles: ['ADMIN', 'LIBRARIAN', 'STUDENT', 'TEACHER'] },
  // PRINCIPAL included since Mức 2.1 (v4.9): AttendanceController's GET
  // endpoints now allow PRINCIPAL (read-only oversight - "hiệu trưởng xem
  // toàn cảnh, giáo viên nhập liệu"). The page renders read-only for a
  // PRINCIPAL session (isReadOnlyRole) - the mark/save controls that would
  // 403 are hidden. STUDENT/PARENT get their own /portal instead.
  { label: 'Điểm danh', href: '/attendance', icon: FiClipboard, roles: ['ADMIN', 'PRINCIPAL', 'TEACHER'] },
  // PRINCIPAL: read-only (Mức 2.1) - GradeRecordController GETs now allow
  // PRINCIPAL; the grade-entry grid renders read-only for that role.
  // STUDENT/PARENT read their own grades at /portal.
  { label: 'Quản lý điểm', href: '/grades', icon: FiAward, roles: ['ADMIN', 'PRINCIPAL', 'TEACHER'] },
  // PRINCIPAL: read-only (Mức 2.1) - FeeController GETs now allow PRINCIPAL;
  // create/edit/delete/record-payment controls are hidden for that role
  // (writes stay ADMIN/ACCOUNTANT). STUDENT/PARENT see their fees at /portal.
  { label: 'Học phí', href: '/fees', icon: FiDollarSign, roles: ['ADMIN', 'PRINCIPAL', 'ACCOUNTANT'] },
  // TEACHER can view a class's timetable (TimetableController's GET is
  // ADMIN/PRINCIPAL/TEACHER) but not manage it (POST/PUT/DELETE on both
  // teaching-assignments and timetable slots are ADMIN/PRINCIPAL only) -
  // included for TEACHER anyway, unlike Class/Staff Management, since the
  // page itself is useful read-only (checking one's own schedule) and
  // gates every write control behind an internal canManage check rather
  // than needing a separate route.
  { label: 'Thời khoá biểu', href: '/timetable', icon: FiCalendar, roles: ['ADMIN', 'PRINCIPAL', 'TEACHER'] },
  // Academic Config (Môn học/Học kỳ) has no read value for TEACHER beyond
  // what the Timetable page already surfaces (subject names, semester
  // picker) - ADMIN/PRINCIPAL only, same scoping as Staff/Class Management.
  { label: 'Cấu hình học vụ', href: '/academic-config', icon: FiSettings, roles: ['ADMIN', 'PRINCIPAL'] },
  // TEACHER included (unlike Class/Staff Management) since the write
  // restriction is per-class, not role-wide - ConductController lets any
  // TEACHER call the endpoints, enforceHomeroomWriteAccess 403s per class
  // server-side, and the page itself narrows its class picker to only the
  // classes that TEACHER is GVCN of. PRINCIPAL included since Mức 2.1 (v4.9)
  // as read-only (the roster GET now allows PRINCIPAL; the save control is
  // hidden). STUDENT/PARENT read their own conduct at /portal.
  { label: 'Hạnh kiểm', href: '/conduct', icon: FiCheckSquare, roles: ['ADMIN', 'PRINCIPAL', 'TEACHER'] },
  // PRINCIPAL included (unlike Conduct/Grades) - PromotionController's
  // confirm endpoint is ADMIN/PRINCIPAL only (a xét lên lớp decision is a
  // Hội đồng-level call, not a per-class teacher one), and its preview
  // endpoint is ADMIN/PRINCIPAL/TEACHER - the page itself gates the
  // confirm button behind canConfirm and shows TEACHER a read-only
  // suggestion table.
  { label: 'Xét lên lớp', href: '/promotions', icon: FiTrendingUp, roles: ['ADMIN', 'PRINCIPAL', 'TEACHER'] },
  // ADMIN only - ParentController's link/unlink (and this page's own
  // account-creation flow via POST /v1/users) are ADMIN-only; PRINCIPAL
  // isn't authorized on those endpoints either, unlike most other
  // ADMIN+PRINCIPAL config pages in this app.
  { label: 'Phụ huynh', href: '/parents', icon: FiUserPlus, roles: ['ADMIN'] },
  // PARENT included here for the first time in this nav list (no other
  // page has read value for that role yet) - NotificationController's
  // resolveRecipients only ever delivers to PARENT accounts (CLASS/
  // STUDENT/ALL_PARENTS targets) or one specific STAFF account (STAFF
  // target) - a STUDENT account is never a recipient under this design,
  // so STUDENT is excluded here unlike Dashboard/Library.
  { label: 'Thông báo', href: '/notifications', icon: FiBell, roles: ['ADMIN', 'PRINCIPAL', 'TEACHER', 'LIBRARIAN', 'ACCOUNTANT', 'PARENT'] },
  // ADMIN only - every AdmissionController endpoint here (list/review/
  // approve-and-create) is ADMIN-only; the public submission form lives
  // at /apply instead (outside AppShell entirely, no nav entry - an
  // applicant isn't logged in).
  { label: 'Tuyển sinh', href: '/admissions', icon: FiFileText, roles: ['ADMIN'] },
  // ADMIN only - AuditLogController's one endpoint is ADMIN-only.
  { label: 'Nhật ký hoạt động', href: '/audit-log', icon: FiActivity, roles: ['ADMIN'] },
];

export function navItemsForRole(role) {
  return NAV_ITEMS.filter((item) => item.roles.includes(role));
}

export function pageTitleForPath(pathname) {
  return NAV_ITEMS.find((item) => item.href === pathname)?.label ?? 'Tổng quan';
}

// The roles allowed on a given in-app route, or null for a path with no
// NAV_ITEMS entry (nothing to gate on). Used by ProtectedRoute (A3) so a
// user who types a URL for a page their role can't use is bounced, instead
// of landing on a page that 403s every request - the menu was already
// filtered by navItemsForRole, the route wasn't.
export function rolesForPath(pathname) {
  return NAV_ITEMS.find((item) => item.href === pathname)?.roles ?? null;
}

// Where to send a user who isn't allowed on the route they asked for: their
// first available menu item (Dashboard for most, /notifications for a
// PARENT, ...). Falls back to '/' if the role somehow has no items.
export function defaultPathForRole(role) {
  return navItemsForRole(role)[0]?.href ?? '/';
}
