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
} from 'react-icons/fi';

// Shared between Sidebar (nav links) and Navbar (page title lookup by
// current route) - one source of truth instead of two menu lists drifting
// apart, which is exactly what happened with the old Sidebar.jsx (its list
// lived only there, Navbar had no idea what page it was on).
export const NAV_ITEMS = [
  { label: 'Dashboard', href: '/', icon: FiHome, roles: ['ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT', 'LIBRARIAN', 'ACCOUNTANT'] },
  { label: 'Staff Management', href: '/staff', icon: FiUsers, roles: ['ADMIN', 'PRINCIPAL'] },
  { label: 'Student Management', href: '/students', icon: FiUsers, roles: ['ADMIN', 'PRINCIPAL', 'TEACHER'] },
  // TEACHER can view (SchoolClassController's GET is ADMIN/PRINCIPAL/
  // TEACHER) but not manage (create/update/delete/assign are ADMIN/
  // PRINCIPAL only) - excluded here since this page is full CRUD, same
  // scoping choice as Staff Management just above.
  { label: 'Class Management', href: '/classes', icon: FiGrid, roles: ['ADMIN', 'PRINCIPAL'] },
  { label: 'Library', href: '/library', icon: FiBook, roles: ['ADMIN', 'LIBRARIAN', 'STUDENT', 'TEACHER'] },
  // PRINCIPAL deliberately excluded: AttendanceController's endpoints -
  // including the GET ones - are all hasAnyRole('ADMIN', 'TEACHER', ...)
  // with no PRINCIPAL, so a PRINCIPAL session would land on a page that
  // 403s on every single request. Found while building the page itself
  // (Tuần 4 Ngày 2), same root cause as the Dashboard stats bug (Tuần 3).
  { label: 'Attendance', href: '/attendance', icon: FiClipboard, roles: ['ADMIN', 'TEACHER'] },
  // STUDENT excluded for the same reason as PRINCIPAL was on Attendance
  // above: /grades is a grade-entry table (rebuilt Tuần 7 on the TT22/2021
  // GradeRecord model, GradeRecordController write endpoints are
  // hasAnyRole('ADMIN', 'TEACHER') only) - a STUDENT/PARENT can read their
  // own grades via GET /v1/grade-records/student/{id}/..., but nothing in
  // the frontend routes to it yet (a self-service view is a real future
  // addition, not this one), so there's no page here for them to land on.
  { label: 'Grades', href: '/grades', icon: FiAward, roles: ['ADMIN', 'TEACHER'] },
  // STUDENT excluded for now, same reasoning pattern as Attendance/Grades
  // above: unlike those two, FeeController genuinely does let a STUDENT
  // view/pay their *own* fees (GET .../student/{id}, POST .../payment both
  // allow it) - but the page built today (Tuần 4 Ngày 5) is the
  // ADMIN/ACCOUNTANT-facing "danh sách khoản thu" management view (GET
  // .../year/{year}, ADMIN/ACCOUNTANT only), matching every other page
  // this week. A student self-service "my fees" view is a real, valid
  // future addition, just not this one.
  { label: 'Fees', href: '/fees', icon: FiDollarSign, roles: ['ADMIN', 'ACCOUNTANT'] },
  // TEACHER can view a class's timetable (TimetableController's GET is
  // ADMIN/PRINCIPAL/TEACHER) but not manage it (POST/PUT/DELETE on both
  // teaching-assignments and timetable slots are ADMIN/PRINCIPAL only) -
  // included for TEACHER anyway, unlike Class/Staff Management, since the
  // page itself is useful read-only (checking one's own schedule) and
  // gates every write control behind an internal canManage check rather
  // than needing a separate route.
  { label: 'Timetable', href: '/timetable', icon: FiCalendar, roles: ['ADMIN', 'PRINCIPAL', 'TEACHER'] },
  // Academic Config (Môn học/Học kỳ) has no read value for TEACHER beyond
  // what the Timetable page already surfaces (subject names, semester
  // picker) - ADMIN/PRINCIPAL only, same scoping as Staff/Class Management.
  { label: 'Academic Config', href: '/academic-config', icon: FiSettings, roles: ['ADMIN', 'PRINCIPAL'] },
  // TEACHER included (unlike Class/Staff Management) since the write
  // restriction is per-class, not role-wide - ConductController lets any
  // TEACHER call the endpoints, enforceHomeroomWriteAccess 403s per class
  // server-side, and the page itself narrows its class picker to only the
  // classes that TEACHER is GVCN of. STUDENT/PARENT excluded for the same
  // reason as Grades: they can read their own conduct via
  // GET /v1/conduct/student/{id}, but no self-service page routes to it yet.
  { label: 'Conduct', href: '/conduct', icon: FiCheckSquare, roles: ['ADMIN', 'TEACHER'] },
];

export function navItemsForRole(role) {
  return NAV_ITEMS.filter((item) => item.roles.includes(role));
}

export function pageTitleForPath(pathname) {
  return NAV_ITEMS.find((item) => item.href === pathname)?.label ?? 'Dashboard';
}
