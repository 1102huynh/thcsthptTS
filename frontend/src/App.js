import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';

// Pages
import PrincipalHomePage from './pages/PrincipalHomePage';
import NewsDetailPage from './pages/NewsDetailPage';
import LoginPage from './pages/LoginPage';
import Dashboard from './pages/Dashboard';
import StudentPortal from './pages/StudentPortal';
import StaffManagement from './pages/StaffManagement';
import AdminNewsPage from './pages/AdminNewsPage';
import {
  LibraryManagement,
  AttendanceManagement,
  GradeManagement,
  FeeManagement,
} from './pages/ManagementPages';

// Vietnamese Education System Pages (NEW)
import ClassManagement from './pages/ClassManagement';
import SubjectManagement from './pages/SubjectManagement';
import TeacherAssignmentPage from './pages/TeacherAssignmentPage';
import TimetableManagement from './pages/TimetableManagement';
import ReportsPage from './pages/ReportsPage';
import AcademicYearPage from './pages/AcademicYearPage';
import ExamManagement from './pages/ExamManagement';
import ExamResultsPage from './pages/ExamResultsPage';
import StudentVNManagement from './pages/StudentVNManagement';
import BackendHealthCheck from './pages/BackendHealthCheck';

// Layout
import Navbar from './components/Navbar';
import Sidebar from './components/Sidebar';

// Services
import { getCurrentUser } from './services/authService';


function App() {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);

  useEffect(() => {
    // Check if user is already logged in
    const storedUser = getCurrentUser();
    if (storedUser) {
      setUser(storedUser);
    }
    setIsLoading(false);
  }, []);

  const handleLogin = (userData) => {
    setUser(userData);
  };

  const handleLogout = () => {
    setUser(null);
    localStorage.removeItem('user');
    localStorage.removeItem('accessToken');
  };

  if (isLoading) {
    return (
      <div className="d-flex justify-content-center align-items-center vh-100">
        <div className="spinner-border" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  return (
    <Router>
      {user ? (
        // Authenticated user view with dashboard
        <div className="app-container">
          <Navbar user={user} onLogout={handleLogout} toggleSidebar={() => setIsSidebarOpen(!isSidebarOpen)} />
          <div className="app-body">
            <Sidebar isOpen={isSidebarOpen} userRole={user.role} />
            <div className="main-content">
              <Routes>
                {/* Student Portal */}
                {user.role === 'STUDENT' ? (
                  <>
                    <Route path="/student-portal" element={<StudentPortal user={user} />} />
                    <Route path="/" element={<Navigate to="/student-portal" />} />
                    <Route path="/dashboard" element={<Navigate to="/student-portal" />} />
                  </>
                ) : (
                  <>
                    <Route path="/dashboard" element={<Dashboard user={user} />} />
                    <Route path="/staff" element={<StaffManagement />} />
                    <Route path="/library" element={<LibraryManagement />} />
                    <Route path="/attendance" element={<AttendanceManagement />} />
                    <Route path="/grades" element={<GradeManagement />} />
                    <Route path="/fees" element={<FeeManagement />} />
                    {/* Vietnamese Education System Routes (NEW) */}
                    <Route path="/academic-year" element={<AcademicYearPage />} />
                    <Route path="/classes" element={<ClassManagement />} />
                    <Route path="/exams" element={<ExamManagement />} />
                    <Route path="/exam-results" element={<ExamResultsPage />} />
                    <Route path="/subjects" element={<SubjectManagement />} />
                    <Route path="/students" element={<StudentVNManagement />} />
                    <Route path="/assignments" element={<TeacherAssignmentPage />} />
                    <Route path="/timetable" element={<TimetableManagement />} />
                    <Route path="/reports" element={<ReportsPage />} />
                    <Route path="/health" element={<BackendHealthCheck />} />
                    {(user.role === 'ADMIN' || user.role === 'PRINCIPAL') && (
                      <Route path="/news" element={<AdminNewsPage />} />
                    )}
                    <Route path="/" element={<Navigate to="/dashboard" />} />
                  </>
                )}
                <Route path="*" element={<Navigate to={user.role === 'STUDENT' ? '/student-portal' : '/dashboard'} />} />
              </Routes>
            </div>
          </div>
        </div>
      ) : (
        // Public view with principal's home page and login
        <Routes>
          <Route path="/" element={<PrincipalHomePage />} />
          <Route path="/news/:id" element={<NewsDetailPage />} />
          <Route path="/login" element={<LoginPage onLogin={handleLogin} />} />
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      )}
    </Router>
  );
}

export default App;

