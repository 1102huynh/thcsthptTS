import { describe, it, expect } from 'vitest';
import {
  NAV_ITEMS,
  navItemsForRole,
  pageTitleForPath,
  rolesForPath,
  defaultPathForRole,
} from './navigation';

describe('navigation config', () => {
  it('every nav item has a non-empty Vietnamese label and a role list', () => {
    for (const item of NAV_ITEMS) {
      expect(item.label).toBeTruthy();
      // A6: no English labels left. "Management"/"Config" were the old ones.
      expect(item.label).not.toMatch(/Management|Config|Dashboard/);
      expect(Array.isArray(item.roles)).toBe(true);
      expect(item.roles.length).toBeGreaterThan(0);
    }
  });

  it('navItemsForRole returns only items that list the role', () => {
    const teacherItems = navItemsForRole('TEACHER');
    expect(teacherItems.length).toBeGreaterThan(0);
    expect(teacherItems.every((i) => i.roles.includes('TEACHER'))).toBe(true);
    // ADMIN-only pages must not leak to TEACHER
    expect(teacherItems.some((i) => i.href === '/audit-log')).toBe(false);
  });

  it('rolesForPath returns the allow-list for a known route, null otherwise', () => {
    expect(rolesForPath('/staff')).toEqual(['ADMIN', 'PRINCIPAL']);
    expect(rolesForPath('/no-such-route')).toBeNull();
  });

  it('defaultPathForRole is the role’s first menu item', () => {
    expect(defaultPathForRole('ADMIN')).toBe('/');
    // STUDENT/PARENT land on the C3 self-service portal
    expect(defaultPathForRole('PARENT')).toBe('/portal');
    expect(defaultPathForRole('STUDENT')).toBe('/portal');
  });

  it('pageTitleForPath falls back to the dashboard label', () => {
    expect(pageTitleForPath('/staff')).toBe('Quản lý nhân sự');
    expect(pageTitleForPath('/unknown')).toBe('Tổng quan');
  });
});
