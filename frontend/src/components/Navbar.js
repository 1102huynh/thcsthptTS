import React, { useState } from 'react';
import { Navbar, Nav, Container, Dropdown, Badge } from 'react-bootstrap';
import { FiMenu, FiLogOut, FiBell, FiSettings, FiUser } from 'react-icons/fi';
import { Link, useNavigate } from 'react-router-dom';
import './Navbar.css';

function NavbarComponent({ user, onLogout, toggleSidebar }) {
  const navigate = useNavigate();
  const [notifications] = useState(3);

  const handleLogout = () => {
    onLogout();
    navigate('/');
  };

  return (
    <Navbar bg="dark" expand="lg" sticky="top" className="navbar-professional">
      <Container fluid>
        <div className="navbar-left">
          <button
            className="btn-toggle-sidebar"
            onClick={toggleSidebar}
            title="Toggle sidebar"
          >
            <FiMenu size={24} />
          </button>
          <Navbar.Brand as={Link} to="/" className="brand-logo">
            <span className="logo-icon">📚</span>
            <span className="brand-text">Tay Son Secondary & High School</span>
          </Navbar.Brand>
        </div>

        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="navbar-nav-right ms-auto">
            {/* Notifications */}
            <div className="nav-item-wrapper">
              <Dropdown className="notifications-dropdown">
                <Dropdown.Toggle
                  variant="link"
                  className="nav-icon-btn notification-btn"
                  title="Notifications"
                >
                  <FiBell size={20} />
                  {notifications > 0 && (
                    <Badge bg="danger" className="notification-badge">{notifications}</Badge>
                  )}
                </Dropdown.Toggle>
                <Dropdown.Menu align="end" className="notification-menu">
                  <Dropdown.Item disabled>
                    <strong>Notifications</strong>
                  </Dropdown.Item>
                  <Dropdown.Divider />
                  <Dropdown.Item href="#" className="notification-item">
                    <div className="notification-content">
                      <p className="mb-1">👤 New student registered</p>
                      <small className="text-muted">2 minutes ago</small>
                    </div>
                  </Dropdown.Item>
                  <Dropdown.Item href="#" className="notification-item">
                    <div className="notification-content">
                      <p className="mb-1">💰 Payment received</p>
                      <small className="text-muted">1 hour ago</small>
                    </div>
                  </Dropdown.Item>
                  <Dropdown.Item href="#" className="notification-item">
                    <div className="notification-content">
                      <p className="mb-1">📚 Book returned</p>
                      <small className="text-muted">3 hours ago</small>
                    </div>
                  </Dropdown.Item>
                  <Dropdown.Divider />
                  <Dropdown.Item href="#" className="view-all">
                    View all notifications
                  </Dropdown.Item>
                </Dropdown.Menu>
              </Dropdown>
            </div>

            {/* Settings */}
            <div className="nav-item-wrapper">
              <button className="nav-icon-btn" title="Settings">
                <FiSettings size={20} />
              </button>
            </div>

            {/* User Profile */}
            {user && (
              <div className="nav-item-wrapper">
                <Dropdown className="user-dropdown">
                  <Dropdown.Toggle
                    variant="link"
                    className="user-toggle"
                    title={user.firstName}
                  >
                    <div className="user-avatar">
                      {user.firstName.charAt(0)}{user.lastName.charAt(0)}
                    </div>
                    <span className="user-name">{user.firstName} {user.lastName}</span>
                  </Dropdown.Toggle>

                  <Dropdown.Menu align="end" className="user-menu">
                    <Dropdown.Item disabled className="user-info">
                      <FiUser size={16} />
                      <div>
                        <strong>{user.firstName} {user.lastName}</strong>
                        <small>{user.role}</small>
                      </div>
                    </Dropdown.Item>
                    <Dropdown.Divider />
                    <Dropdown.Item href="#" className="menu-item">
                      <FiSettings size={16} /> My Profile
                    </Dropdown.Item>
                    <Dropdown.Item href="#" className="menu-item">
                      <FiSettings size={16} /> Account Settings
                    </Dropdown.Item>
                    <Dropdown.Divider />
                    <Dropdown.Item
                      onClick={handleLogout}
                      className="menu-item logout-item"
                    >
                      <FiLogOut size={16} /> Logout
                    </Dropdown.Item>
                  </Dropdown.Menu>
                </Dropdown>
              </div>
            )}
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
}

export default NavbarComponent;

