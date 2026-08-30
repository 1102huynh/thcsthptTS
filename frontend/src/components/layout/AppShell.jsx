import React, { useEffect } from 'react';
import Sidebar from './Sidebar';
import Navbar from './Navbar';
import useUiStore from '@/stores/uiStore';

// Tailwind's default `lg` breakpoint (1024px) - kept in sync with the
// `lg:flex`/`lg:hidden` classes in Sidebar.jsx.
const LG_BREAKPOINT = '(min-width: 1024px)';

/**
 * Composes the new Tailwind/shadcn Sidebar + Navbar around a routed content
 * area. Desktop (>= lg) gets a fixed, always-visible sidebar; below that the
 * same toggle button (in Navbar) opens the Sidebar's shadcn Sheet drawer
 * instead - see Sidebar.jsx's own comment for why one `sidebarOpen` flag
 * drives both.
 */
function AppShell({ user, onLogout, children }) {
  const sidebarOpen = useUiStore((s) => s.sidebarOpen);
  const setSidebarOpen = useUiStore((s) => s.setSidebarOpen);
  const toggleSidebar = useUiStore((s) => s.toggleSidebar);

  // If the viewport crosses into the desktop breakpoint while the mobile
  // drawer is open (window resize, tablet rotation, external monitor),
  // close it - otherwise the Sheet's full-screen overlay stays mounted and
  // blocks clicks on the now-also-visible fixed desktop sidebar/navbar.
  useEffect(() => {
    const mql = window.matchMedia(LG_BREAKPOINT);
    const handleChange = (e) => {
      if (e.matches) setSidebarOpen(false);
    };
    mql.addEventListener('change', handleChange);
    return () => mql.removeEventListener('change', handleChange);
  }, [setSidebarOpen]);

  return (
    <div className="flex min-h-screen bg-muted/30">
      <Sidebar
        userRole={user?.role}
        mobileOpen={sidebarOpen}
        onMobileOpenChange={setSidebarOpen}
      />
      <div className="flex min-w-0 flex-1 flex-col">
        <Navbar user={user} onLogout={onLogout} onToggleSidebar={toggleSidebar} />
        <main className="flex-1 p-4 sm:p-6">{children}</main>
      </div>
    </div>
  );
}

export default AppShell;
