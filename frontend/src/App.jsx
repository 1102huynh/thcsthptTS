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
const AdmissionManagement = lazy(() => import('./pages/AdmissionManagement'));
const AdmissionApplyPage = lazy(() => import('./pages/AdmissionApplyPage'));
const AuditLogManagement = lazy(() => import('./pages/AuditLogManagement'));
const SelfServicePortal = lazy(() => import('./pages/SelfServicePortal'));
const ForgotPasswordPage = lazy(() => import('./pages/ForgotPasswordPage'));
const ResetPasswordPage = lazy(() => import('./pages/ResetPasswordPage'));

// Layout
import AppShell from './components/layout/AppShell';
import { AppShellSkeleton, RoutePageSkeleton } from './components/shared/Skeleton';
import ProtectedRoute from './components/auth/ProtectedRoute';

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
    localStorage.removeItem('refreshToken');
  };

  // Wraps a routed page in the role gate (A3). `path` must match the route's
  // own `path` so ProtectedRoute can look up its allow-list in
  // config/navigation.js.
  const guarded = (path, element) => (
    <ProtectedRoute user={user} path={path}>
      {element}
    </ProtectedRoute>
  );

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
              <Route path="/" element={guarded('/', <Dashboard user={user} />)} />
              <Route path="/portal" element={guarded('/portal', <SelfServicePortal />)} />
              <Route path="/staff" element={guarded('/staff', <StaffManagement />)} />
              <Route path="/students" element={guarded('/students', <StudentManagement />)} />
              <Route path="/classes" element={guarded('/classes', <ClassManagement />)} />
              <Route path="/library" element={guarded('/library', <LibraryManagement user={user} />)} />
              <Route path="/attendance" element={guarded('/attendance', <AttendanceManagement />)} />
              <Route path="/grades" element={guarded('/grades', <GradeManagement />)} />
              <Route path="/fees" element={guarded('/fees', <FeeManagement />)} />
              <Route path="/academic-config" element={guarded('/academic-config', <AcademicConfig />)} />
              <Route path="/timetable" element={guarded('/timetable', <TimetableManagement />)} />
              <Route path="/conduct" element={guarded('/conduct', <ConductManagement />)} />
              <Route path="/promotions" element={guarded('/promotions', <PromotionManagement />)} />
              <Route path="/parents" element={guarded('/parents', <ParentManagement />)} />
              <Route path="/notifications" element={guarded('/notifications', <NotificationCenter />)} />
              <Route path="/admissions" element={guarded('/admissions', <AdmissionManagement />)} />
              <Route path="/audit-log" element={guarded('/audit-log', <AuditLogManagement />)} />
              <Route path="*" element={<Navigate to="/" />} />
            </Routes>
          </Suspense>
        </AppShell>
      ) : (
        <Suspense fallback={null}>
          <Routes>
            <Route path="/" element={<LoginPage onLogin={handleLogin} />} />
            {/* Public tuyển sinh đầu cấp form (IMPLEMENTATION_PLAN.md 3.7) -
                no login required, so it only needs to exist in this
                unauthenticated branch; a logged-in user manages
                applications at /admissions instead. */}
            <Route path="/apply" element={<AdmissionApplyPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </Suspense>
      )}
    </Router>
  );
}

export default App;

