import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';

// Pages
import PrincipalHomePage from './pages/PrincipalHomePage';
import LoginPage from './pages/LoginPage';
import Dashboard from './pages/Dashboard';
import StaffManagement from './pages/StaffManagement';
import StudentManagement from './pages/StudentManagement';
import {
  LibraryManagement,
  AttendanceManagement,
  GradeManagement,
  FeeManagement,
} from './pages/ManagementPages';

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
                <Route path="/dashboard" element={<Dashboard user={user} />} />
                <Route path="/staff" element={<StaffManagement />} />
                <Route path="/students" element={<StudentManagement />} />
                <Route path="/library" element={<LibraryManagement />} />
                <Route path="/attendance" element={<AttendanceManagement />} />
                <Route path="/grades" element={<GradeManagement />} />
                <Route path="/fees" element={<FeeManagement />} />
                <Route path="/" element={<Navigate to="/dashboard" />} />
                <Route path="*" element={<Navigate to="/dashboard" />} />
              </Routes>
            </div>
          </div>
        </div>
      ) : (
        // Public view with principal's home page and login
        <Routes>
          <Route path="/" element={<PrincipalHomePage />} />
          <Route path="/login" element={<LoginPage onLogin={handleLogin} />} />
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      )}
    </Router>
  );
}

export default App;

