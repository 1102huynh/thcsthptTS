import React from 'react';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { admissionService } from '@/services/dataService';
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

// Mirrors ApproveAndCreateRequest's own validation - name/DOB/phone are
// pulled from the application itself server-side, not asked again here.
const schema = z.object({
  username: z.string().min(4, 'Tên đăng nhập tối thiểu 4 ký tự').max(50),
  email: z.string().email('Email không hợp lệ'),
  password: z.string().min(8, 'Mật khẩu tối thiểu 8 ký tự'),
  rollNumber: z.string().min(1, 'Vui lòng nhập số báo danh'),
  admissionNumber: z.string().min(1, 'Vui lòng nhập mã hồ sơ nhập học'),
});

/** open, onOpenChange, application: AdmissionApplicationDTO (must be APPROVED, no createdStudentId yet) */
function ApproveAndCreateDialog({ open, onOpenChange, application }) {
  const queryClient = useQueryClient();

  const form = useAppForm({
    schema,
    defaultValues: { username: '', email: '', password: '', rollNumber: '', admissionNumber: '' },
  });

  const mutation = useMutation({
    mutationFn: (values) => admissionService.approveAndCreate(application.id, values),
    onSuccess: (res) => {
      toast.success(`Đã tạo tài khoản học sinh (${res.data.username})`);
      queryClient.invalidateQueries({ queryKey: ['admissions'] });
      onOpenChange(false);
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể tạo tài khoản học sinh'),
  });

  if (!application) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Tạo tài khoản học sinh</DialogTitle>
          <DialogDescription>
            Từ hồ sơ của {application.applicantName} - họ tên/ngày sinh/số điện thoại lấy tự động từ hồ sơ
          </DialogDescription>
        </DialogHeader>
        <AppForm form={form} onSubmit={(v) => mutation.mutate(v)} className="space-y-4" id="approve-create-form">
          <TextField control={form.control} name="username" label="Tên đăng nhập" placeholder={`${application.applicantName?.split(' ').pop()?.toLowerCase() ?? 'hocsinh'}2026`} />
          <TextField control={form.control} name="email" label="Email" type="email" />
          <TextField control={form.control} name="password" label="Mật khẩu ban đầu" type="password" description="Thông báo mật khẩu này cho gia đình học sinh riêng, không qua hệ thống" />
          <div className="grid grid-cols-2 gap-4">
            <TextField control={form.control} name="rollNumber" label="Số báo danh" placeholder="10A015" />
            <TextField control={form.control} name="admissionNumber" label="Mã hồ sơ nhập học" placeholder="ADM2026015" />
          </div>
          <DialogFooter className="pt-2">
            <Button type="submit" form="approve-create-form" disabled={mutation.isPending}>
              {mutation.isPending ? 'Đang tạo...' : 'Tạo tài khoản'}
            </Button>
          </DialogFooter>
        </AppForm>
      </DialogContent>
    </Dialog>
  );
}

export default ApproveAndCreateDialog;
