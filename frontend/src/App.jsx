import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
// bootstrap.min.css / App.css are imported from main.jsx now, before
// index.css - see its comment for why the load order matters.

// Pages
import LoginPage from './pages/LoginPage';
import Dashboard from './pages/Dashboard';
import StaffManagement from './pages/StaffManagement';
import StudentManagement from './pages/StudentManagement';
import ClassManagement from './pages/ClassManagement';
import LibraryManagement from './pages/LibraryManagement';
import AttendanceManagement from './pages/AttendanceManagement';
import GradeManagement from './pages/GradeManagement';
import FeeManagement from './pages/FeeManagement';

// Layout
import AppShell from './components/layout/AppShell';
import { AppShellSkeleton } from './components/shared/Skeleton';

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
    // The one true full-page spinner in the app (Bootstrap's
    // .spinner-border, on a bare centered flex div) - replaced with a
    // skeleton shaped like the AppShell it's about to become, per Tuần 5
    // Ngày 5. In practice this frame is near-instant (getCurrentUser()
    // just reads localStorage synchronously) but it's still the literal
    // "spinner toàn trang" the plan calls out.
    return <AppShellSkeleton />;
  }

  return (
    <Router>
      {user ? (
        <AppShell user={user} onLogout={handleLogout}>
          <Routes>
            <Route path="/" element={<Dashboard user={user} />} />
            <Route path="/staff" element={<StaffManagement />} />
            <Route path="/students" element={<StudentManagement />} />
            <Route path="/classes" element={<ClassManagement />} />
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

