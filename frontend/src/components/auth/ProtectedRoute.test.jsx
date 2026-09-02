import React from 'react';
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';

// Renders <ProtectedRoute> for `path` at the given `entry` URL, with a
// distinct marker page at every route so the test can tell which one won.
function renderAt(entry, path, user) {
  return render(
    <MemoryRouter initialEntries={[entry]}>
      <Routes>
        <Route
          path={path}
          element={
            <ProtectedRoute user={user} path={path}>
              <div>PAGE: {path}</div>
            </ProtectedRoute>
          }
        />
        <Route path="/" element={<div>PAGE: dashboard</div>} />
        <Route path="/notifications" element={<div>PAGE: notifications</div>} />
      </Routes>
    </MemoryRouter>
  );
}

describe('ProtectedRoute', () => {
  it('renders the page when the role is in the route allow-list', () => {
    renderAt('/staff', '/staff', { role: 'ADMIN' });
    expect(screen.getByText('PAGE: /staff')).toBeInTheDocument();
  });

  it('redirects to the role default when the role is not allowed', () => {
    // TEACHER is not in NAV_ITEMS' roles for /staff (ADMIN/PRINCIPAL only) -
    // should bounce to the TEACHER default landing page (Dashboard, '/').
    renderAt('/staff', '/staff', { role: 'TEACHER' });
    expect(screen.queryByText('PAGE: /staff')).not.toBeInTheDocument();
    expect(screen.getByText('PAGE: dashboard')).toBeInTheDocument();
  });

  it('sends a PARENT to /notifications (their only menu item) for a disallowed route', () => {
    renderAt('/grades', '/grades', { role: 'PARENT' });
    expect(screen.getByText('PAGE: notifications')).toBeInTheDocument();
  });

  it('leaves a route with no NAV_ITEMS entry open', () => {
    renderAt('/some-unlisted-route', '/some-unlisted-route', { role: 'STUDENT' });
    expect(screen.getByText('PAGE: /some-unlisted-route')).toBeInTheDocument();
  });
});
