import {
  FiHome,
  FiUsers,
  FiBook,
  FiClipboard,
  FiAward,
  FiDollarSign,
} from 'react-icons/fi';

// Shared between Sidebar (nav links) and Navbar (page title lookup by
// current route) - one source of truth instead of two menu lists drifting
// apart, which is exactly what happened with the old Sidebar.jsx (its list
// lived only there, Navbar had no idea what page it was on).
export const NAV_ITEMS = [
  { label: 'Dashboard', href: '/', icon: FiHome, roles: ['ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT', 'LIBRARIAN', 'ACCOUNTANT'] },
  { label: 'Staff Management', href: '/staff', icon: FiUsers, roles: ['ADMIN', 'PRINCIPAL'] },
  { label: 'Student Management', href: '/students', icon: FiUsers, roles: ['ADMIN', 'PRINCIPAL', 'TEACHER'] },
  { label: 'Library', href: '/library', icon: FiBook, roles: ['ADMIN', 'LIBRARIAN', 'STUDENT', 'TEACHER'] },
  // PRINCIPAL deliberately excluded: AttendanceController's endpoints -
  // including the GET ones - are all hasAnyRole('ADMIN', 'TEACHER', ...)
  // with no PRINCIPAL, so a PRINCIPAL session would land on a page that
  // 403s on every single request. Found while building the page itself
  // (Tuần 4 Ngày 2), same root cause as the Dashboard stats bug (Tuần 3).
  { label: 'Attendance', href: '/attendance', icon: FiClipboard, roles: ['ADMIN', 'TEACHER'] },
  // STUDENT excluded for the same reason as PRINCIPAL was on Attendance
  // above: the only thing at /grades right now is a grade-entry table
  // (Tuần 4 Ngày 3-4) built on GET /v1/grades/year/{year}, which is
  // hasAnyRole('ADMIN', 'TEACHER') only - a STUDENT has their own
  // /v1/grades/student/{id} endpoint, but nothing in the frontend routes
  // to it yet, so there's no page here for them to land on today.
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
];

export function navItemsForRole(role) {
  return NAV_ITEMS.filter((item) => item.roles.includes(role));
}

export function pageTitleForPath(pathname) {
  return NAV_ITEMS.find((item) => item.href === pathname)?.label ?? 'Dashboard';
}
