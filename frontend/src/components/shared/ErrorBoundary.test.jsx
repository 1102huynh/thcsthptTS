import React from 'react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ErrorBoundary from './ErrorBoundary';

// Regression coverage for KE_HOACH_NANG_CAP_V4.md v4.18, Quyết định 10 - App.jsx
// had no error boundary anywhere in the tree, so any uncaught render error (the
// RichTextEditor StrictMode crash that motivated this, since fixed separately)
// blanked the entire app instead of showing a recoverable screen.
function Bomb() {
  throw new Error('boom');
}

describe('ErrorBoundary', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('renders children normally when nothing throws', () => {
    render(
      <ErrorBoundary>
        <p>Nội dung bình thường</p>
      </ErrorBoundary>
    );
    expect(screen.getByText('Nội dung bình thường')).toBeInTheDocument();
  });

  it('shows a recoverable Vietnamese fallback instead of blanking the app when a child throws', () => {
    // React logs the error to the console by default when a boundary catches
    // it - silence that expected noise for this test only.
    vi.spyOn(console, 'error').mockImplementation(() => {});

    render(
      <ErrorBoundary>
        <Bomb />
      </ErrorBoundary>
    );

    expect(screen.getByText('Đã có lỗi xảy ra')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Tải lại trang' })).toBeInTheDocument();
  });

  it('reloads the page when the reload button is clicked', async () => {
    vi.spyOn(console, 'error').mockImplementation(() => {});
    const reload = vi.fn();
    const originalLocation = window.location;
    Object.defineProperty(window, 'location', {
      value: { ...originalLocation, reload },
      writable: true,
      configurable: true,
    });

    const user = userEvent.setup();
    render(
      <ErrorBoundary>
        <Bomb />
      </ErrorBoundary>
    );
    await user.click(screen.getByRole('button', { name: 'Tải lại trang' }));

    expect(reload).toHaveBeenCalledTimes(1);
    Object.defineProperty(window, 'location', { value: originalLocation, writable: true, configurable: true });
  });
});
