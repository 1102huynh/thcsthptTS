import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
// bootstrap.min.css / App.css are imported from main.jsx now, before
// index.css - see its comment for why the load order matters.

// Pages
import LoginPage from './pages/LoginPage';
import Dashboard from './pages/Dashboard';
import StaffManagement from './pages/StaffManagement';
import StudentManagement from './pages/StudentManagement';
import LibraryManagement from './pages/LibraryManagement';
import AttendanceManagement from './pages/AttendanceManagement';
import {
  GradeManagement,
  FeeManagement,
} from './pages/ManagementPages';

// Layout
import AppShell from './components/layout/AppShell';

// Services
import { getCurrentUser } from './services/authService';


function App() {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

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
        <AppShell user={user} onLogout={handleLogout}>
          <Routes>
            <Route path="/" element={<Dashboard user={user} />} />
            <Route path="/staff" element={<StaffManagement />} />
            <Route path="/students" element={<StudentManagement />} />
            <Route path="/library" element={<LibraryManagement user={user} />} />
            <Route path="/attendance" element={<AttendanceManagement />} />
            <Route path="/grades" element={<GradeManagement />} />
            <Route path="/fees" element={<FeeManagement />} />
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </AppShell>
      ) : (
        <Routes>
          <Route path="/" element={<LoginPage onLogin={handleLogin} />} />
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      )}
    </Router>
  );
}

export default App;

