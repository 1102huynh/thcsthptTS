import React, { Suspense, useEffect, useState } from 'react';
import { Link, NavLink, Outlet, useLocation } from 'react-router-dom';
import { FiMenu, FiX, FiLogIn, FiGrid } from 'react-icons/fi';
import { isAuthenticated } from '../../services/authService';

// Safety-net fallback: the public pages are eager-imported (App.jsx) so this
// normally never renders, but a future lazy child still suspends here inside
// <main> - never at the App root, which would swap header+footer too.
function ContentFallback() {
  return (
    <div className="space-y-4" aria-hidden="true">
      <div className="h-8 w-56 animate-pulse rounded bg-muted" />
      <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i} className="h-64 animate-pulse rounded-xl border bg-muted/50" />
        ))}
      </div>
    </div>
  );
}

// Public-facing chrome for the school portal - completely separate from the
// authenticated app's AppShell. Mobile-first: most parents browse on a
// phone (KE_HOACH_TRANG_TIN_TUC_CONG_KHAI.md §6).

const NAV = [
  { to: '/', label: 'Trang chủ', end: true },
  { to: '/tin-tuc', label: 'Tin tức' },
  { to: '/su-kien', label: 'Sự kiện' },
  { to: '/tuyen-sinh', label: 'Tuyển sinh' },
  { to: '/gioi-thieu', label: 'Giới thiệu' },
  { to: '/lien-he', label: 'Liên hệ' },
];

function navClass({ isActive }) {
  return (
    'rounded-md px-3 py-2 text-sm font-medium transition-colors ' +
    (isActive ? 'bg-primary/10 text-primary' : 'text-foreground/70 hover:bg-muted hover:text-foreground')
  );
}

export default function PublicLayout() {
  const [open, setOpen] = useState(false);
  const location = useLocation();

  // If a valid session is already in localStorage, the portal's CTA points
  // into the app ("/" -> AppShell/dashboard) instead of the login form -
  // there's nothing to log into. (App.jsx also bounces /login -> "/" for a
  // logged-in user, so this is just avoiding a pointless round-trip.)
  const authed = isAuthenticated();
  const cta = authed
    ? { to: '/', label: 'Vào hệ thống', Icon: FiGrid }
    : { to: '/login', label: 'Đăng nhập', Icon: FiLogIn };

  // New page = start from the top (not the previous page's scroll position).
  // Keyed on pathname only, so changing ?category= on /tin-tuc doesn't jump.
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [location.pathname]);

  return (
    <div className="flex min-h-screen flex-col bg-background text-foreground">
      <header className="sticky top-0 z-30 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/80">
        <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4">
          <Link to="/" className="flex items-center gap-2 font-bold">
            <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary text-primary-foreground">T</span>
            <span className="leading-tight">
              Trường THCS &amp; THPT
              <span className="block text-xs font-normal text-muted-foreground">Cổng thông tin</span>
            </span>
          </Link>

          <nav className="hidden items-center gap-1 md:flex">
            {NAV.map((n) => (
              <NavLink key={n.to} to={n.to} end={n.end} className={navClass}>
                {n.label}
              </NavLink>
            ))}
            <Link
              to={cta.to}
              className="ml-2 inline-flex items-center gap-1.5 rounded-md bg-primary px-3 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90"
            >
              <cta.Icon className="h-4 w-4" /> {cta.label}
            </Link>
          </nav>

          <button
            type="button"
            className="rounded-md p-2 text-foreground/70 hover:bg-muted md:hidden"
            aria-label={open ? 'Đóng menu' : 'Mở menu'}
            onClick={() => setOpen((v) => !v)}
          >
            {open ? <FiX className="h-5 w-5" /> : <FiMenu className="h-5 w-5" />}
          </button>
        </div>

        {open && (
          <nav className="space-y-1 border-t px-4 py-3 md:hidden">
            {NAV.map((n) => (
              <NavLink
                key={n.to}
                to={n.to}
                end={n.end}
                className={({ isActive }) =>
                  'block rounded-md px-3 py-2 text-sm font-medium ' +
                  (isActive ? 'bg-primary/10 text-primary' : 'text-foreground/70 hover:bg-muted')
                }
                onClick={() => setOpen(false)}
              >
                {n.label}
              </NavLink>
            ))}
            <Link
              to={cta.to}
              className="mt-1 flex items-center gap-1.5 rounded-md bg-primary px-3 py-2 text-sm font-medium text-primary-foreground"
              onClick={() => setOpen(false)}
            >
              <cta.Icon className="h-4 w-4" /> {cta.label}
            </Link>
          </nav>
        )}
      </header>

      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-8">
        <Suspense fallback={<ContentFallback />}>
          {/* Keyed by pathname: a gentle fade/rise on each page change so
              navigation feels like a transition, not a hard swap. */}
          <div key={location.pathname} className="animate-in fade-in slide-in-from-bottom-1 duration-300">
            <Outlet />
          </div>
        </Suspense>
      </main>

      <footer className="border-t bg-muted/40">
        <div className="mx-auto grid max-w-6xl gap-6 px-4 py-10 sm:grid-cols-3">
          <div>
            <div className="font-semibold">Trường THCS &amp; THPT</div>
            <p className="mt-2 text-sm text-muted-foreground">
              Địa chỉ: (đang cập nhật)
              <br />
              Điện thoại: (đang cập nhật)
              <br />
              Email: (đang cập nhật)
            </p>
          </div>
          <div>
            <div className="font-semibold">Liên kết</div>
            <ul className="mt-2 space-y-1 text-sm text-muted-foreground">
              <li><Link to="/tin-tuc" className="hover:text-foreground">Tin tức</Link></li>
              <li><Link to="/su-kien" className="hover:text-foreground">Sự kiện</Link></li>
              <li><Link to="/tuyen-sinh" className="hover:text-foreground">Tuyển sinh</Link></li>
              <li><Link to="/lien-he" className="hover:text-foreground">Liên hệ</Link></li>
            </ul>
          </div>
          <div>
            <div className="font-semibold">Dành cho nội bộ</div>
            <p className="mt-2 text-sm text-muted-foreground">
              {authed ? (
                <>Bạn đang đăng nhập — <Link to="/" className="text-primary hover:underline">vào hệ thống quản lý</Link>.</>
              ) : (
                <>Cán bộ, giáo viên, học sinh và phụ huynh đăng nhập hệ thống quản lý tại{' '}
                <Link to="/login" className="text-primary hover:underline">trang đăng nhập</Link>.</>
              )}
            </p>
          </div>
        </div>
        <div className="border-t py-4 text-center text-xs text-muted-foreground">
          © {new Date().getFullYear()} Trường THCS &amp; THPT. Bảo lưu mọi quyền.
        </div>
      </footer>
    </div>
  );
}
