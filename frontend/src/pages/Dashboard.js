import React, { useState, useEffect } from 'react';
import { Row, Col, Card, Alert, Badge } from 'react-bootstrap';
import {
  FiUsers, FiBook, FiClipboard, FiTrendingUp,
  FiArrowRight, FiCheckCircle, FiClock, FiAward, FiDollarSign,
  FiCalendar, FiPercent
} from 'react-icons/fi';
import { Link } from 'react-router-dom';
import { staffService, studentService, libraryService } from '../services/dataService';
import './Dashboard.css';

function Dashboard({ user }) {
  const [stats, setStats] = useState({
    staffCount: 0,
    studentCount: 0,
    bookCount: 0,
    attendanceRate: 85,
    totalRevenue: 0,
    loading: true,
    error: null,
  });

  const [recentActivity] = useState([
    { id: 1, type: 'student', message: 'New student registered', time: '2 hours ago', icon: '👤' },
    { id: 2, type: 'book', message: 'Book borrowed from library', time: '4 hours ago', icon: '📚' },
    { id: 3, type: 'attendance', message: 'Attendance marked for class 10A', time: '6 hours ago', icon: '✓' },
    { id: 4, type: 'fee', message: 'Fee payment received', time: '1 day ago', icon: '💰' },
  ]);

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      setStats(prev => ({ ...prev, loading: true }));

      const staffRes = await staffService.getAll();
      const studentRes = await studentService.getAll();
      const bookRes = await libraryService.getBooks();

      setStats({
        staffCount: staffRes.data.length || 0,
        studentCount: studentRes.data.length || 0,
        bookCount: bookRes.data.length || 0,
        attendanceRate: 85,
        totalRevenue: 125000,
        loading: false,
        error: null,
      });
    } catch (err) {
      setStats(prev => ({
        ...prev,
        loading: false,
        error: 'Failed to load statistics',
      }));
    }
  };

  const StatCard = ({ icon: Icon, title, count, color, trend = null, trendLabel = null }) => (
    <Card className="stat-card professional">
      <Card.Body>
        <div className="stat-card-header">
          <div className={`stat-icon ${color}`}>
            <Icon size={24} />
          </div>
          {trend && (
            <Badge className={`trend-badge ${trend > 0 ? 'positive' : 'negative'}`}>
              <FiTrendingUp size={14} /> {Math.abs(trend)}%
            </Badge>
          )}
        </div>
        <div className="stat-card-body">
          <h6 className="text-muted mb-1">{title}</h6>
          <h2 className="mb-0">{count}</h2>
          {trendLabel && <p className="trend-label">{trendLabel}</p>}
        </div>
      </Card.Body>
    </Card>
  );

  const QuickActionButton = ({ icon: Icon, label, color, to }) => (
    <Link to={to} className={`quick-action-btn ${color}`}>
      <Icon size={20} />
      <span>{label}</span>
      <FiArrowRight size={16} />
    </Link>
  );

  return (
    <div className="dashboard-container">
      {/* Header Section */}
      <div className="dashboard-header">
        <div className="header-content">
          <div>
            <h1 className="header-title">Welcome back, <span className="highlight">{user?.firstName}!</span></h1>
            <p className="header-subtitle">Here's what's happening in your school today</p>
          </div>
          <div className="header-badge">
            <Badge bg="primary" className="role-badge">{user?.role}</Badge>
            <span className="user-email">{user?.email}</span>
          </div>
        </div>
      </div>

      {stats.error && <Alert variant="danger" className="mt-3">{stats.error}</Alert>}

      {stats.loading ? (
        <div className="text-center py-5">
          <div className="spinner-border" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      ) : (
        <>
          {/* Stats Row */}
          <Row className="stats-row">
            <Col lg={3} md={6} className="mb-3">
              <StatCard
                icon={FiUsers}
                title="Total Staff"
                count={stats.staffCount}
                color="primary"
                trend={12}
                trendLabel="vs last month"
              />
            </Col>
            <Col lg={3} md={6} className="mb-3">
              <StatCard
                icon={FiUsers}
                title="Total Students"
                count={stats.studentCount}
                color="success"
                trend={5}
                trendLabel="vs last month"
              />
            </Col>
            <Col lg={3} md={6} className="mb-3">
              <StatCard
                icon={FiBook}
                title="Library Books"
                count={stats.bookCount}
                color="info"
                trend={-2}
                trendLabel="vs last month"
              />
            </Col>
            <Col lg={3} md={6} className="mb-3">
              <StatCard
                icon={FiPercent}
                title="Attendance Rate"
                count={`${stats.attendanceRate}%`}
                color="warning"
                trend={3}
                trendLabel="vs last month"
              />
            </Col>
          </Row>

          {/* Main Content Row */}
          <Row className="mt-4">
            {/* Quick Actions */}
            <Col lg={7} className="mb-4">
              <Card className="professional-card">
                <Card.Header className="card-header-professional">
                  <div>
                    <Card.Title className="mb-0">Quick Actions</Card.Title>
                    <p className="mb-0 text-muted small">Frequently used features</p>
                  </div>
                </Card.Header>
                <Card.Body>
                  <div className="quick-actions-grid">
                    <QuickActionButton
                      icon={FiUsers}
                      label="Manage Staff"
                      color="primary"
                      to="/staff"
                    />
                    <QuickActionButton
                      icon={FiUsers}
                      label="Manage Students"
                      color="success"
                      to="/students"
                    />
                    <QuickActionButton
                      icon={FiClipboard}
                      label="Attendance"
                      color="info"
                      to="/attendance"
                    />
                    <QuickActionButton
                      icon={FiAward}
                      label="Manage Grades"
                      color="warning"
                      to="/grades"
                    />
                    <QuickActionButton
                      icon={FiBook}
                      label="Library"
                      color="secondary"
                      to="/library"
                    />
                    <QuickActionButton
                      icon={FiDollarSign}
                      label="Manage Fees"
                      color="danger"
                      to="/fees"
                    />
                  </div>
                </Card.Body>
              </Card>
            </Col>

            {/* System Stats */}
            <Col lg={5} className="mb-4">
              <Card className="professional-card">
                <Card.Header className="card-header-professional">
                  <Card.Title className="mb-0">System Overview</Card.Title>
                </Card.Header>
                <Card.Body>
                  <div className="system-stats">
                    <div className="stat-row">
                      <div className="stat-label">
                        <FiCalendar size={18} />
                        <span>Current Academic Year</span>
                      </div>
                      <span className="stat-value">2024-2025</span>
                    </div>
                    <div className="stat-row">
                      <div className="stat-label">
                        <FiCheckCircle size={18} />
                        <span>System Status</span>
                      </div>
                      <Badge bg="success">Operational</Badge>
                    </div>
                    <div className="stat-row">
                      <div className="stat-label">
                        <FiClock size={18} />
                        <span>Last Backup</span>
                      </div>
                      <span className="stat-value">Today 3:00 PM</span>
                    </div>
                    <div className="stat-row">
                      <div className="stat-label">
                        <FiUsers size={18} />
                        <span>Active Users</span>
                      </div>
                      <Badge bg="primary">24</Badge>
                    </div>
                    <div className="stat-row">
                      <div className="stat-label">
                        <FiDollarSign size={18} />
                        <span>Total Revenue</span>
                      </div>
                      <span className="stat-value">₹{stats.totalRevenue.toLocaleString()}</span>
                    </div>
                  </div>
                </Card.Body>
              </Card>
            </Col>
          </Row>

          {/* Recent Activity & Performance */}
          <Row className="mt-4">
            <Col lg={6} className="mb-4">
              <Card className="professional-card">
                <Card.Header className="card-header-professional">
                  <div>
                    <Card.Title className="mb-0">Recent Activity</Card.Title>
                    <p className="mb-0 text-muted small">Latest updates from your system</p>
                  </div>
                </Card.Header>
                <Card.Body className="p-0">
                  <div className="activity-list">
                    {recentActivity.map((activity) => (
                      <div key={activity.id} className="activity-item">
                        <div className="activity-icon">{activity.icon}</div>
                        <div className="activity-content">
                          <p className="mb-0">{activity.message}</p>
                          <small className="text-muted">{activity.time}</small>
                        </div>
                      </div>
                    ))}
                  </div>
                </Card.Body>
              </Card>
            </Col>

            <Col lg={6} className="mb-4">
              <Card className="professional-card">
                <Card.Header className="card-header-professional">
                  <div>
                    <Card.Title className="mb-0">Performance Metrics</Card.Title>
                    <p className="mb-0 text-muted small">This month overview</p>
                  </div>
                </Card.Header>
                <Card.Body>
                  <div className="metrics-container">
                    <div className="metric-item">
                      <div className="metric-header">
                        <span>Student Enrollment</span>
                        <span className="badge-sm success">+15%</span>
                      </div>
                      <div className="progress-bar">
                        <div className="progress-fill" style={{ width: '75%', backgroundColor: '#52c41a' }}></div>
                      </div>
                      <small className="text-muted">{stats.studentCount} students</small>
                    </div>
                    <div className="metric-item">
                      <div className="metric-header">
                        <span>Attendance</span>
                        <span className="badge-sm success">+3%</span>
                      </div>
                      <div className="progress-bar">
                        <div className="progress-fill" style={{ width: '85%', backgroundColor: '#1890ff' }}></div>
                      </div>
                      <small className="text-muted">85% average</small>
                    </div>
                    <div className="metric-item">
                      <div className="metric-header">
                        <span>Fee Collection</span>
                        <span className="badge-sm danger">-5%</span>
                      </div>
                      <div className="progress-bar">
                        <div className="progress-fill" style={{ width: '60%', backgroundColor: '#faad14' }}></div>
                      </div>
                      <small className="text-muted">60% collected</small>
                    </div>
                  </div>
                </Card.Body>
              </Card>
            </Col>
          </Row>
        </>
      )}
    </div>
  );
}

export default Dashboard;

