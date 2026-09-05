import React from 'react';
import { Button } from '@/components/ui/button';

/**
 * Top-level catch-all so a single component's render/effect error shows a
 * recoverable "Đã có lỗi xảy ra" screen instead of blanking the entire app.
 *
 * KE_HOACH_NANG_CAP_V4.md v4.18, Quyết định 10 — found live while manually
 * clicking through the app: opening NewsManagement's "Bài tin mới" dialog
 * crashed (RichTextEditor.jsx, since fixed) with no error boundary anywhere
 * in the tree, so React unmounted everything past the failure point and the
 * whole app went blank. That specific trigger is fixed, but the underlying
 * gap - ANY future uncaught render/effect error, in any component, would do
 * the same - stayed open until this. Mirrors the backend's
 * GlobalExceptionHandler.handleGeneralException philosophy: log the real
 * error where an operator can act on it, show the user a generic message.
 *
 * Must be a class component - React has no hook equivalent to
 * getDerivedStateFromError/componentDidCatch (error boundaries are the one
 * remaining class-only API).
 */
class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    // eslint-disable-next-line no-console
    console.error('Lỗi không bắt được, ErrorBoundary đã chặn:', error, errorInfo);
  }

  handleReload = () => {
    window.location.reload();
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-background px-4 text-center">
          <h1 className="text-xl font-semibold">Đã có lỗi xảy ra</h1>
          <p className="max-w-sm text-sm text-muted-foreground">
            Ứng dụng gặp sự cố không mong muốn. Vui lòng tải lại trang — nếu vẫn còn lỗi, hãy liên hệ quản trị viên.
          </p>
          <Button onClick={this.handleReload}>Tải lại trang</Button>
        </div>
      );
    }
    return this.props.children;
  }
}

export default ErrorBoundary;
