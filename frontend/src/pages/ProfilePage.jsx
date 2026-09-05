import React from 'react';
import { z } from 'zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { userService } from '@/services/dataService';
import { useAppForm, AppForm } from '@/components/shared/Form';
import { TextField } from '@/components/shared/FormFields';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { ROLE_LABELS } from '@/lib/enumLabels';

// Backs Navbar's user-dropdown "Hồ sơ & cài đặt tài khoản" item (previously
// two dead <DropdownMenuItem>s with no onClick and no route at all - see
// App.jsx for the /profile route). One page, one link: profile fields and
// the password change live in separate cards on the same screen instead of
// two menu entries pointing at the same route.

const profileSchema = z.object({
  firstName: z.string().min(1, 'Vui lòng nhập tên'),
  lastName: z.string().min(1, 'Vui lòng nhập họ'),
  email: z.string().email('Email không hợp lệ'),
  phoneNumber: z.string().optional(),
});

const passwordSchema = z
  .object({
    currentPassword: z.string().min(1, 'Vui lòng nhập mật khẩu hiện tại'),
    newPassword: z.string().min(8, 'Mật khẩu mới tối thiểu 8 ký tự'),
    confirmPassword: z.string().min(1, 'Vui lòng nhập lại mật khẩu mới'),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: 'Mật khẩu nhập lại không khớp',
    path: ['confirmPassword'],
  });

function initials(user) {
  return `${user?.firstName?.[0] ?? ''}${user?.lastName?.[0] ?? ''}`.toUpperCase();
}

/** onSaved(updatedUser) - called with the fresh profile after a successful save. */
function ProfileCard({ data, onSaved }) {
  const form = useAppForm({
    schema: profileSchema,
    defaultValues: {
      firstName: data.firstName ?? '',
      lastName: data.lastName ?? '',
      email: data.email ?? '',
      phoneNumber: data.phoneNumber ?? '',
    },
  });

  const mutation = useMutation({
    mutationFn: (values) => userService.updateMe(values),
    onSuccess: (res) => {
      toast.success('Đã cập nhật hồ sơ');
      onSaved(res.data);
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể cập nhật hồ sơ'),
  });

  return (
    <Card>
      <CardHeader className="flex-row items-center gap-4 space-y-0">
        <Avatar className="h-14 w-14">
          <AvatarFallback className="bg-primary text-lg font-semibold text-primary-foreground">
            {initials(data)}
          </AvatarFallback>
        </Avatar>
        <div>
          <CardTitle>Hồ sơ của tôi</CardTitle>
          <CardDescription>
            {data.username} · <Badge variant="secondary">{ROLE_LABELS[data.role] ?? data.role}</Badge>
          </CardDescription>
        </div>
      </CardHeader>
      <AppForm form={form} onSubmit={(v) => mutation.mutate(v)} id="profile-form">
        <CardContent className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <TextField control={form.control} name="firstName" label="Tên" />
            <TextField control={form.control} name="lastName" label="Họ" />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <TextField control={form.control} name="email" label="Email" type="email" />
            <TextField control={form.control} name="phoneNumber" label="Số điện thoại" />
          </div>
        </CardContent>
        <CardFooter>
          <Button type="submit" form="profile-form" disabled={mutation.isPending}>
            {mutation.isPending ? 'Đang lưu...' : 'Lưu thay đổi'}
          </Button>
        </CardFooter>
      </AppForm>
    </Card>
  );
}

function PasswordCard() {
  const form = useAppForm({
    schema: passwordSchema,
    defaultValues: { currentPassword: '', newPassword: '', confirmPassword: '' },
  });

  const mutation = useMutation({
    mutationFn: (values) =>
      userService.changePassword({ currentPassword: values.currentPassword, newPassword: values.newPassword }),
    onSuccess: () => {
      toast.success('Đã đổi mật khẩu');
      form.reset({ currentPassword: '', newPassword: '', confirmPassword: '' });
    },
    onError: (err) =>
      toast.error(err?.response?.data?.message || err?.message || 'Không thể đổi mật khẩu — kiểm tra lại mật khẩu hiện tại'),
  });

  return (
    <Card>
      <CardHeader>
        <CardTitle>Cài đặt tài khoản</CardTitle>
        <CardDescription>Đổi mật khẩu đăng nhập</CardDescription>
      </CardHeader>
      <AppForm form={form} onSubmit={(v) => mutation.mutate(v)} id="password-form">
        <CardContent className="max-w-sm space-y-4">
          <TextField control={form.control} name="currentPassword" label="Mật khẩu hiện tại" type="password" />
          <TextField control={form.control} name="newPassword" label="Mật khẩu mới" type="password" />
          <TextField control={form.control} name="confirmPassword" label="Nhập lại mật khẩu mới" type="password" />
        </CardContent>
        <CardFooter>
          <Button type="submit" form="password-form" disabled={mutation.isPending}>
            {mutation.isPending ? 'Đang đổi...' : 'Đổi mật khẩu'}
          </Button>
        </CardFooter>
      </AppForm>
    </Card>
  );
}

/** onUserUpdate(partialUser) - passed down from App.jsx so a profile save here
 * is reflected immediately in Navbar/localStorage without a full re-login. */
function ProfilePage({ onUserUpdate }) {
  const queryClient = useQueryClient();

  const meQuery = useQuery({ queryKey: ['users', 'me'], queryFn: () => userService.getMe().then((r) => r.data) });

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold">Hồ sơ &amp; cài đặt tài khoản</h1>
        <p className="text-sm text-muted-foreground">Xem và cập nhật thông tin cá nhân, đổi mật khẩu đăng nhập</p>
      </div>
      {meQuery.isLoading ? (
        <p className="py-4 text-sm text-muted-foreground">Đang tải...</p>
      ) : meQuery.isError ? (
        <p className="py-4 text-sm text-destructive">Không thể tải hồ sơ</p>
      ) : (
        <ProfileCard
          data={meQuery.data}
          onSaved={(updated) => {
            queryClient.setQueryData(['users', 'me'], updated);
            onUserUpdate?.(updated);
          }}
        />
      )}
      <PasswordCard />
    </div>
  );
}

export default ProfilePage;
