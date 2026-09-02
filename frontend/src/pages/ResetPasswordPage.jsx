import React, { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { z } from 'zod';
import { toast } from 'sonner';
import { FiBookOpen, FiCheckCircle } from 'react-icons/fi';
import authService from '../services/authService';
import { useAppForm, AppForm } from '../components/shared/Form';
import { TextField } from '../components/shared/FormFields';
import { Button } from '../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../components/ui/card';

const schema = z
  .object({
    newPassword: z.string().min(8, 'Mật khẩu tối thiểu 8 ký tự'),
    confirmPassword: z.string().min(1, 'Vui lòng nhập lại mật khẩu'),
  })
  .refine((v) => v.newPassword === v.confirmPassword, {
    message: 'Mật khẩu nhập lại không khớp',
    path: ['confirmPassword'],
  });

/**
 * Public - no login required. Reads the reset token from ?token= (the link
 * FRONTEND_RESET_PASSWORD_URL in the backend's forgot-password email points
 * at, per IMPLEMENTATION_PLAN.md 3.9) rather than as a route param, since
 * that's the literal query-string shape the backend's email builds.
 */
function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const navigate = useNavigate();
  const [submitted, setSubmitted] = useState(false);
  const form = useAppForm({ schema, defaultValues: { newPassword: '', confirmPassword: '' } });

  const handleSubmit = async ({ newPassword }) => {
    try {
      await authService.resetPassword(token, newPassword);
      setSubmitted(true);
    } catch (err) {
      // A used/expired token surfaces here as the backend's own message
      // ("Token không hợp lệ hoặc đã hết hạn" or similar) - shown verbatim
      // rather than a generic failure, so the person knows to request a
      // new link instead of retrying the same one.
      const message = err?.response?.data?.message || err?.message || 'Không thể đặt lại mật khẩu, vui lòng thử lại.';
      toast.error(message);
    }
  };

  const submitting = form.formState.isSubmitting;

  return (
    <main className="flex min-h-screen items-center justify-center bg-gradient-to-br from-primary/90 via-primary to-purple-700 p-4">
      <h1 className="sr-only">Đặt lại mật khẩu</h1>
      <Card className="w-full max-w-sm shadow-xl">
        <CardHeader className="items-center space-y-3 text-center">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary text-primary-foreground">
            <FiBookOpen className="h-6 w-6" />
          </div>
          <div>
            <CardTitle className="text-xl">Đặt lại mật khẩu</CardTitle>
            <CardDescription>Nhập mật khẩu mới cho tài khoản của bạn</CardDescription>
          </div>
        </CardHeader>
        <CardContent>
          {!token ? (
            <div className="space-y-3 text-center">
              <p className="text-sm text-destructive dark:text-red-400">
                Liên kết không hợp lệ — thiếu mã đặt lại mật khẩu.
              </p>
              <Button variant="outline" className="w-full" onClick={() => navigate('/forgot-password')}>
                Yêu cầu liên kết mới
              </Button>
            </div>
          ) : submitted ? (
            <div className="flex flex-col items-center gap-3 py-4 text-center">
              <FiCheckCircle className="h-10 w-10 text-green-600 dark:text-green-400" />
              <p className="text-sm text-muted-foreground">Mật khẩu đã được đặt lại thành công.</p>
              <Button className="w-full" onClick={() => navigate('/')}>
                Đăng nhập ngay
              </Button>
            </div>
          ) : (
            <AppForm form={form} onSubmit={handleSubmit} className="space-y-4">
              <TextField
                control={form.control}
                name="newPassword"
                label="Mật khẩu mới"
                type="password"
                disabled={submitting}
              />
              <TextField
                control={form.control}
                name="confirmPassword"
                label="Nhập lại mật khẩu mới"
                type="password"
                disabled={submitting}
              />
              <Button type="submit" className="w-full" disabled={submitting}>
                {submitting ? 'Đang lưu...' : 'Đặt lại mật khẩu'}
              </Button>
            </AppForm>
          )}
          {!submitted && (
            <p className="mt-4 text-center text-sm text-muted-foreground">
              <Link to="/" className="underline-offset-2 hover:underline hover:text-foreground">
                Quay lại đăng nhập
              </Link>
            </p>
          )}
        </CardContent>
      </Card>
    </main>
  );
}

export default ResetPasswordPage;
