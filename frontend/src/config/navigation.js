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
  { label: 'Attendance', href: '/attendance', icon: FiClipboard, roles: ['ADMIN', 'PRINCIPAL', 'TEACHER'] },
  { label: 'Grades', href: '/grades', icon: FiAward, roles: ['ADMIN', 'TEACHER', 'STUDENT'] },
  { label: 'Fees', href: '/fees', icon: FiDollarSign, roles: ['ADMIN', 'ACCOUNTANT', 'STUDENT'] },
];

export function navItemsForRole(role) {
  return NAV_ITEMS.filter((item) => item.roles.includes(role));
}

export function pageTitleForPath(pathname) {
  return NAV_ITEMS.find((item) => item.href === pathname)?.label ?? 'Dashboard';
}
