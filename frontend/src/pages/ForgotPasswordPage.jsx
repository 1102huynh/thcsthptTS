import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { z } from 'zod';
import { toast } from 'sonner';
import { FiBookOpen, FiCheckCircle, FiArrowLeft } from 'react-icons/fi';
import authService from '../services/authService';
import { useAppForm, AppForm } from '../components/shared/Form';
import { TextField } from '../components/shared/FormFields';
import { Button } from '../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../components/ui/card';

const schema = z.object({
  email: z.string().email('Email không hợp lệ'),
});

/**
 * Public - no login required, per IMPLEMENTATION_PLAN.md 3.9. The backend
 * always responds the same way whether or not the email is registered (see
 * ForgotPasswordRequest's doc comment) - this page shows the same success
 * state either way for the same reason, rather than branching on the
 * response to say "email sent" vs "email not found".
 */
function ForgotPasswordPage() {
  const [submitted, setSubmitted] = useState(false);
  const form = useAppForm({ schema, defaultValues: { email: '' } });

  const handleSubmit = async ({ email }) => {
    try {
      await authService.forgotPassword(email);
      setSubmitted(true);
    } catch (err) {
      const message = err?.response?.data?.message || err?.message || 'Không thể gửi yêu cầu, vui lòng thử lại.';
      toast.error(message);
    }
  };

  const submitting = form.formState.isSubmitting;

  return (
    <main className="flex min-h-screen items-center justify-center bg-gradient-to-br from-primary/90 via-primary to-purple-700 p-4">
      <h1 className="sr-only">Quên mật khẩu</h1>
      <Card className="w-full max-w-sm shadow-xl">
        <CardHeader className="items-center space-y-3 text-center">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary text-primary-foreground">
            <FiBookOpen className="h-6 w-6" />
          </div>
          <div>
            <CardTitle className="text-xl">Quên mật khẩu</CardTitle>
            <CardDescription>Nhập email tài khoản để nhận liên kết đặt lại mật khẩu</CardDescription>
          </div>
        </CardHeader>
        <CardContent>
          {submitted ? (
            <div className="flex flex-col items-center gap-3 py-4 text-center">
              <FiCheckCircle className="h-10 w-10 text-green-600 dark:text-green-400" />
              <p className="text-sm text-muted-foreground">
                Nếu email này đã đăng ký, một liên kết đặt lại mật khẩu đã được gửi tới đó. Liên kết có hiệu lực
                trong 15 phút.
              </p>
            </div>
          ) : (
            <AppForm form={form} onSubmit={handleSubmit} className="space-y-4">
              <TextField
                control={form.control}
                name="email"
                label="Email"
                type="email"
                placeholder="ten@school.com"
                disabled={submitting}
              />
              <Button type="submit" className="w-full" disabled={submitting}>
                {submitting ? 'Đang gửi...' : 'Gửi liên kết đặt lại'}
              </Button>
            </AppForm>
          )}
          <p className="mt-4 flex items-center justify-center gap-1 text-center text-sm text-muted-foreground">
            <Link to="/" className="flex items-center gap-1 underline-offset-2 hover:underline hover:text-foreground">
              <FiArrowLeft className="h-3.5 w-3.5" /> Quay lại đăng nhập
            </Link>
          </p>
        </CardContent>
      </Card>
    </main>
  );
}

export default ForgotPasswordPage;
