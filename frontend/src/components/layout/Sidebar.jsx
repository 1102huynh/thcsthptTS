import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { FiBookOpen } from 'react-icons/fi';
import { cn } from '@/lib/utils';
import { navItemsForRole } from '@/config/navigation';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet';

function Brand({ className }) {
  return (
    <div className={cn('flex items-center gap-2 px-4', className)}>
      <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-primary text-primary-foreground">
        <FiBookOpen className="h-5 w-5" />
      </div>
      <span className="truncate text-base font-semibold tracking-tight">
        Quản lý Trường học
      </span>
    </div>
  );
}

function NavList({ userRole, onNavigate }) {
  const location = useLocation();
  const items = navItemsForRole(userRole);

  return (
    <nav className="flex flex-col gap-1 px-3">
      {items.map(({ label, href, icon: Icon }) => {
        const active = location.pathname === href;
        return (
          <Link
            key={href}
            to={href}
            onClick={onNavigate}
            className={cn(
              'group relative flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors',
              active
                ? // dark:text-primary-foreground - --primary was darkened for
                  // AA text contrast against a *light* background (Tuần 5
                  // Ngày 4, buttons/badges), but that same darker blue on
                  // this tinted-dark-navy sidebar background in dark mode
                  // only hit 3.32:1 (axe-core, Tuần 6 Ngày 3-4 QA pass -
                  // light mode alone was checked before, not dark). Swap to
                  // the near-white --primary-foreground for dark mode,
                  // 17.9:1 against this background.
                  'bg-primary/10 text-primary dark:text-primary-foreground'
                : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
            )}
          >
            {active && (
              <span className="absolute inset-y-1 left-0 w-1 rounded-full bg-primary" aria-hidden="true" />
            )}
            <Icon className={cn('h-[18px] w-[18px] shrink-0', active && 'text-primary dark:text-primary-foreground')} />
            <span className="truncate">{label}</span>
          </Link>
        );
      })}
    </nav>
  );
}

/**
 * Desktop (>= lg): fixed, always-visible aside - no collapse, matching most
 * professional admin-dashboard layouts.
 * Mobile (< lg): rendered as a shadcn Sheet drawer instead, opened by the
 * Navbar's menu button (mobileOpen/onMobileOpenChange, backed by
 * useUiStore). Both are always mounted; Tailwind's `hidden lg:flex` on the
 * desktop aside is what actually decides which one is visible.
 */
function Sidebar({ userRole, mobileOpen, onMobileOpenChange }) {
  return (
    <>
      {/* Desktop */}
      <aside className="hidden shrink-0 border-r bg-card lg:flex lg:flex-col lg:py-4">
        <Brand className="mb-6" />
        <NavList userRole={userRole} />
      </aside>

      {/* Mobile */}
      <Sheet open={mobileOpen} onOpenChange={onMobileOpenChange}>
        <SheetContent side="left" className="w-72 p-0">
          <SheetHeader className="border-b py-4 text-left">
            <SheetTitle className="flex items-center gap-2 text-base font-semibold">
              <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-primary text-primary-foreground">
                <FiBookOpen className="h-5 w-5" />
              </span>
              Quản lý Trường học
            </SheetTitle>
          </SheetHeader>
          <div className="py-4">
            <NavList userRole={userRole} onNavigate={() => onMobileOpenChange(false)} />
          </div>
        </SheetContent>
      </Sheet>
    </>
  );
}

export default Sidebar;
