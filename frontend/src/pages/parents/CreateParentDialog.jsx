import React from 'react';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { userService } from '@/services/dataService';
import { useAppForm, AppForm } from '@/components/shared/Form';
import { TextField } from '@/components/shared/FormFields';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog';

// Mirrors CreateUserRequest's own validation (username 4-50, email, password
// min 8) - POST /v1/users 400s on anything short of it, so failing the same
// way client-side gives an immediate, field-level message instead of a
// round-trip.
const schema = z.object({
  username: z.string().min(4, 'Tên đăng nhập tối thiểu 4 ký tự').max(50),
  email: z.string().email('Email không hợp lệ'),
  password: z.string().min(8, 'Mật khẩu tối thiểu 8 ký tự'),
  firstName: z.string().min(1, 'Vui lòng nhập tên'),
  lastName: z.string().min(1, 'Vui lòng nhập họ'),
  phoneNumber: z.string().optional(),
});

function toPayload(values) {
  return { ...values, role: 'PARENT' };
}

/** open, onOpenChange, onCreated(userId) - called with the new account's id after a successful create */
function CreateParentDialog({ open, onOpenChange, onCreated }) {
  const queryClient = useQueryClient();

  const form = useAppForm({
    schema,
    defaultValues: { username: '', email: '', password: '', firstName: '', lastName: '', phoneNumber: '' },
  });

  const mutation = useMutation({
    mutationFn: (values) => userService.create(toPayload(values)),
    onSuccess: (res) => {
      toast.success('Đã tạo tài khoản phụ huynh mới');
      queryClient.invalidateQueries({ queryKey: ['users-parent'] });
      onOpenChange(false);
      onCreated?.(res.data.userId);
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể tạo tài khoản phụ huynh'),
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Tạo tài khoản phụ huynh</DialogTitle>
          <DialogDescription>Tài khoản mới với vai trò Phụ huynh - có thể liên kết nhiều con sau khi tạo</DialogDescription>
        </DialogHeader>
        <AppForm form={form} onSubmit={(v) => mutation.mutate(v)} className="space-y-4" id="create-parent-form">
          <div className="grid grid-cols-2 gap-4">
            <TextField control={form.control} name="firstName" label="Tên" placeholder="Thị B" />
            <TextField control={form.control} name="lastName" label="Họ" placeholder="Trần" />
          </div>
          <TextField control={form.control} name="username" label="Tên đăng nhập" placeholder="tranthib" />
          <TextField control={form.control} name="email" label="Email" type="email" placeholder="tranthib@example.com" />
          <TextField control={form.control} name="password" label="Mật khẩu" type="password" />
          <TextField control={form.control} name="phoneNumber" label="Số điện thoại" placeholder="0912345678" />
          <DialogFooter className="pt-2">
            <Button type="submit" form="create-parent-form" disabled={mutation.isPending}>
              {mutation.isPending ? 'Đang tạo...' : 'Tạo tài khoản'}
            </Button>
          </DialogFooter>
        </AppForm>
      </DialogContent>
    </Dialog>
  );
}

export default CreateParentDialog;
