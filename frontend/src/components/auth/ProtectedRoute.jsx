import React from 'react';
import { Navigate } from 'react-router-dom';
import { rolesForPath, defaultPathForRole } from '@/config/navigation';

/**
 * Route-level role gate (A3 in KE_HOACH_NANG_CAP_V4.md).
 *
 * The sidebar already hides menu items a role can't use (navItemsForRole),
 * but nothing stopped that same user from typing the URL directly - they'd
 * reach a page that just 403s every backend call. This wraps each routed
 * element: if the current user's role isn't in the route's allow-list
 * (config/navigation.js), it redirects to the role's default landing page
 * instead of rendering the page.
 *
 * A route with no NAV_ITEMS entry (rolesForPath -> null) is left open - it's
 * not something the menu gates on, so there's no allow-list to enforce.
 * Backend @PreAuthorize is still the real authority; this is a UX guard.
 */
function ProtectedRoute({ user, path, children }) {
  const allowedRoles = rolesForPath(path);
  const role = user?.role;

  if (allowedRoles && role && !allowedRoles.includes(role)) {
    const fallback = defaultPathForRole(role);
    // Don't redirect a path to itself (role has no menu items at all) -
    // render the page rather than spin in a Navigate loop.
    if (fallback !== path) {
      return <Navigate to={fallback} replace />;
    }
  }

  return children;
}

export default ProtectedRoute;
