import React, { useState, Suspense, lazy } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

// Standalone entry pages (no AppShell around them). Eager, not lazy: each
// is small and is a landing target on refresh - lazy-loading them only adds
// a Suspense-fallback flash (the admin-table skeleton, which looks nothing
// like a login card) on every reload of /login, /apply, etc.
import LoginPage from './pages/LoginPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import AdmissionApplyPage from './pages/AdmissionApplyPage';

// Authenticated app pages - lazy per route (Tuần 6 Ngày 2): an admin
// session only downloads the pages it visits.
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
const AuditLogManagement = lazy(() => import('./pages/AuditLogManagement'));
const SelfServicePortal = lazy(() => import('./pages/SelfServicePortal'));
const NewsManagement = lazy(() => import('./pages/NewsManagement'));
const EventManagement = lazy(() => import('./pages/EventManagement'));

// Public portal (KE_HOACH_TRANG_TIN_TUC_CONG_KHAI.md). Eager-imported, NOT
// lazy: a visitor browses these back-to-back, and each is small - lazy just
// adds a Suspense-fallback flash on every menu click and on refresh. As one
// bundle, navigating between them is a plain re-render (no loader).
import PublicLayout from './components/public/PublicLayout';
import PublicHome from './pages/public/PublicHome';
import NewsListPage from './pages/public/NewsListPage';
import NewsDetailPage from './pages/public/NewsDetailPage';
import EventListPage from './pages/public/EventListPage';
import EventDetailPage from './pages/public/EventDetailPage';
import AdmissionsInfoPage from './pages/public/AdmissionsInfoPage';
import AboutPage from './pages/public/AboutPage';
import ContactPage from './pages/public/ContactPage';

// Layout
import AppShell from './components/layout/AppShell';
import { RoutePageSkeleton } from './components/shared/Skeleton';
import ProtectedRoute from './components/auth/ProtectedRoute';

// Services
import { getCurrentUser } from './services/authService';

function readUser() {
  try {
    return getCurrentUser();
  } catch {
    return null;
  }
}

function App() {
  // Resolved synchronously from localStorage on first render - no loading
  // frame, so a refresh lands straight on the right tree (public vs app)
  // instead of flashing the admin-shell skeleton first.
  const [user, setUser] = useState(readUser);

  const handleLogin = (userData) => setUser(userData);

  const handleLogout = () => {
    setUser(null);
    localStorage.removeItem('user');
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  };

  // Role gate (A3) - `path` must match the route's own path.
  const guarded = (path, element) => (
    <ProtectedRoute user={user} path={path}>
      {element}
    </ProtectedRoute>
  );

  return (
    <Router>
      {/* Root fallback is null on purpose: everything that can suspend here
          is either eager (login/apply/portal pages) or lives behind
          AppShell's own inner <Suspense> below. A stray blank frame beats
          flashing an admin-table skeleton over a login card. */}
      <Suspense fallback={null}>
        <Routes>
          {/* ---- Public portal: available to everyone, its own chrome ---- */}
          <Route element={<PublicLayout />}>
            <Route path="/tin-tuc" element={<NewsListPage />} />
            <Route path="/tin-tuc/:slug" element={<NewsDetailPage />} />
            <Route path="/su-kien" element={<EventListPage />} />
            <Route path="/su-kien/:slug" element={<EventDetailPage />} />
            <Route path="/tuyen-sinh" element={<AdmissionsInfoPage />} />
            <Route path="/gioi-thieu" element={<AboutPage />} />
            <Route path="/lien-he" element={<ContactPage />} />
            {/* "/" is the portal home only when NOT logged in - a logged-in
                user's "/" is the dashboard (handled in the authed branch). */}
            {!user && <Route path="/" element={<PublicHome />} />}
          </Route>

          {/* ---- Public, standalone (no portal chrome) ---- */}
          <Route path="/apply" element={<AdmissionApplyPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />

          {user ? (
            <Route
              path="/*"
              element={
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
                      <Route path="/news" element={guarded('/news', <NewsManagement />)} />
                      <Route path="/events" element={guarded('/events', <EventManagement />)} />
                      <Route path="/audit-log" element={guarded('/audit-log', <AuditLogManagement />)} />
                      <Route path="*" element={<Navigate to="/" />} />
                    </Routes>
                  </Suspense>
                </AppShell>
              }
            />
          ) : (
            <>
              <Route path="/login" element={<LoginPage onLogin={handleLogin} />} />
              <Route path="*" element={<Navigate to="/" />} />
            </>
          )}
        </Routes>
      </Suspense>
    </Router>
  );
}

export default App;
