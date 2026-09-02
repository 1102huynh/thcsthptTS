import React, { useState, useEffect, Suspense, lazy } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

// Pages - lazy-loaded per route (Tuần 6 Ngày 2) so a session only ever
// downloads the page(s) it actually visits instead of one bundle with
// every page (Dashboard's recharts, every *Management page's forms/dialogs,
// ...) upfront. React.lazy()'s default export requirement is why every
// page module still needs `export default` (already true for all of them).
const LoginPage = lazy(() => import('./pages/LoginPage'));
const Dashboard = lazy(() => import('./pages/Dashboard'));
const StaffManagement = lazy(() => import('./pages/StaffManagement'));
const StudentManagement = lazy(() => import('./pages/StudentManagement'));
const ClassManagement = lazy(() => import('./pages/ClassManagement'));
const LibraryManagement = lazy(() => import('./pages/LibraryManagement'));
const AttendanceManagement = lazy(() => import('./pages/AttendanceManagement'));
const GradeManagement = lazy(() => import('./pages/GradeManagement'));
const FeeManagement = lazy(() => import('./pages/FeeManagement'));
const AcademicConfig = lazy(() => import('./pages/AcademicConfig'));
const TimetableManagement = lazy(() => import('./pages/TimetableManagement'));
const ConductManagement = lazy(() => import('./pages/ConductManagement'));
const PromotionManagement = lazy(() => import('./pages/PromotionManagement'));
const ParentManagement = lazy(() => import('./pages/ParentManagement'));
const NotificationCenter = lazy(() => import('./pages/NotificationCenter'));

// Layout
import AppShell from './components/layout/AppShell';
import { AppShellSkeleton, RoutePageSkeleton } from './components/shared/Skeleton';

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
          <Suspense fallback={<RoutePageSkeleton />}>
            <Routes>
              <Route path="/" element={<Dashboard user={user} />} />
              <Route path="/staff" element={<StaffManagement />} />
              <Route path="/students" element={<StudentManagement />} />
              <Route path="/classes" element={<ClassManagement />} />
              <Route path="/library" element={<LibraryManagement user={user} />} />
              <Route path="/attendance" element={<AttendanceManagement />} />
              <Route path="/grades" element={<GradeManagement />} />
              <Route path="/fees" element={<FeeManagement />} />
              <Route path="/academic-config" element={<AcademicConfig />} />
              <Route path="/timetable" element={<TimetableManagement />} />
              <Route path="/conduct" element={<ConductManagement />} />
              <Route path="/promotions" element={<PromotionManagement />} />
              <Route path="/parents" element={<ParentManagement />} />
              <Route path="/notifications" element={<NotificationCenter />} />
              <Route path="*" element={<Navigate to="/" />} />
            </Routes>
          </Suspense>
        </AppShell>
      ) : (
        <Suspense fallback={null}>
          <Routes>
            <Route path="/" element={<LoginPage onLogin={handleLogin} />} />
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </Suspense>
      )}
    </Router>
  );
}

export default App;

