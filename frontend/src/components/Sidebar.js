import React from 'react';
import { Nav } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { useLocation } from 'react-router-dom';
import {
  FiHome,
  FiUsers,
  FiBook,
  FiClipboard,
  FiAward,
  FiDollarSign,
  FiChevronRight,
  FiFileText,
} from 'react-icons/fi';
import './Sidebar.css';

function Sidebar({ isOpen, userRole }) {
  const location = useLocation();

  const menuItems = [
    { id: 1, label: 'Dashboard', href: '/', icon: FiHome, roles: ['ADMIN', 'PRINCIPAL', 'TEACHER', 'STUDENT', 'LIBRARIAN', 'ACCOUNTANT'] },
    { id: 2, label: 'Staff Management', href: '/staff', icon: FiUsers, roles: ['ADMIN', 'PRINCIPAL'] },
    { id: 3, label: 'Student Management', href: '/students', icon: FiUsers, roles: ['ADMIN', 'PRINCIPAL', 'TEACHER'] },
    { id: 4, label: 'News Management', href: '/news', icon: FiFileText, roles: ['ADMIN', 'PRINCIPAL'] },
    { id: 5, label: 'Library', href: '/library', icon: FiBook, roles: ['ADMIN', 'LIBRARIAN', 'STUDENT', 'TEACHER'] },
    { id: 6, label: 'Attendance', href: '/attendance', icon: FiClipboard, roles: ['ADMIN', 'PRINCIPAL', 'TEACHER'] },
    { id: 7, label: 'Grades', href: '/grades', icon: FiAward, roles: ['ADMIN', 'TEACHER', 'STUDENT'] },
    { id: 8, label: 'Fees', href: '/fees', icon: FiDollarSign, roles: ['ADMIN', 'ACCOUNTANT', 'STUDENT'] },
  ];

  const visibleMenuItems = menuItems.filter(item => item.roles.includes(userRole));

  const isActive = (href) => {
    return location.pathname === href;
  };

  return (
    <div className={`sidebar-professional ${isOpen ? 'open' : 'closed'}`}>
      <div className="sidebar-header">
        <h6 className="sidebar-title">Navigation</h6>
      </div>
      <Nav className="sidebar-nav flex-column">
        {visibleMenuItems.map((item) => {
          const Icon = item.icon;
          const active = isActive(item.href);

          return (
            <Nav.Link
              key={item.id}
              as={Link}
              to={item.href}
              className={`sidebar-link ${active ? 'active' : ''}`}
            >
              <div className="sidebar-link-inner">
                <div className="sidebar-link-icon">
                  <Icon size={20} />
                </div>
                <span className="sidebar-link-label">{item.label}</span>
                {active && <FiChevronRight size={16} className="sidebar-link-arrow" />}
              </div>
              {active && <div className="sidebar-link-active-indicator"></div>}
            </Nav.Link>
          );
        })}
      </Nav>
    </div>
  );
}

export default Sidebar;

