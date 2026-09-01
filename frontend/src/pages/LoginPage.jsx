import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { toast } from 'sonner';
import { FiBookOpen, FiEye, FiEyeOff, FiLoader } from 'react-icons/fi';
import authService from '../services/authService';
import { useAppForm, AppForm } from '../components/shared/Form';
import { TextField } from '../components/shared/FormFields';
import { Button } from '../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../components/ui/card';

const schema = z.object({
  username: z.string().min(1, 'Vui lòng nhập tên đăng nhập'),
  password: z.string().min(1, 'Vui lòng nhập mật khẩu'),
});

/**
 * Rebuilt per Tuần 2 Ngày 5 - Tailwind/shadcn instead of react-bootstrap,
 * using this week's own Form wrapper (Ngày 4) for validation and sonner
 * (this same day) for error feedback instead of a dismissable-but-easy-to-
 * miss inline Alert.
 */
function LoginPage({ onLogin }) {
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();
  const form = useAppForm({
    schema,
    defaultValues: { username: '', password: '' },
  });

  const handleSubmit = async ({ username, password }) => {
    try {
      const userData = await authService.login(username, password);
      if (userData?.accessToken) {
        onLogin(userData);
        navigate('/');
      } else {
        toast.error('Đăng nhập thất bại: không nhận được access token');
      }
    } catch (err) {
      const message =
        (typeof err === 'string' ? err : err?.message) || 'Sai tên đăng nhập hoặc mật khẩu.';
      toast.error(message);
    }
  };

  const submitting = form.formState.isSubmitting;

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-primary/90 via-primary to-purple-700 p-4">
      <Card className="w-full max-w-sm shadow-xl">
        <CardHeader className="items-center space-y-3 text-center">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary text-primary-foreground">
            <FiBookOpen className="h-6 w-6" />
          </div>
          <div>
            <CardTitle className="text-xl">Hệ thống Quản lý Trường học</CardTitle>
            <CardDescription>Đăng nhập vào tài khoản của bạn</CardDescription>
          </div>
        </CardHeader>
        <CardContent>
          <AppForm form={form} onSubmit={handleSubmit} className="space-y-4">
            <TextField
              control={form.control}
              name="username"
              label="Tên đăng nhập"
              placeholder="Nhập tên đăng nhập"
              autoComplete="username"
              disabled={submitting}
            />
            <div className="relative">
              <TextField
                control={form.control}
                name="password"
                label="Mật khẩu"
                type={showPassword ? 'text' : 'password'}
                placeholder="Nhập mật khẩu"
                autoComplete="current-password"
                disabled={submitting}
                className="pr-10"
              />
              <button
                type="button"
                onClick={() => setShowPassword((v) => !v)}
                className="absolute right-3 top-[34px] text-muted-foreground hover:text-foreground"
                aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                tabIndex={-1}
              >
                {showPassword ? <FiEyeOff className="h-4 w-4" /> : <FiEye className="h-4 w-4" />}
              </button>
            </div>

            <Button type="submit" className="w-full" disabled={submitting}>
              {submitting && <FiLoader className="mr-2 h-4 w-4 animate-spin" />}
              {submitting ? 'Đang đăng nhập...' : 'Đăng nhập'}
            </Button>
          </AppForm>
        </CardContent>
      </Card>
    </div>
  );
}

export default LoginPage;
