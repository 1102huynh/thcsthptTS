import React from 'react';
import { z } from 'zod';
import { format } from 'date-fns';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { userService, staffService } from '@/services/dataService';
import { useAppForm, AppForm } from '@/components/shared/Form';
import { TextField, SelectField, DateField } from '@/components/shared/FormFields';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog';
import {
  STAFF_POSITION_LABELS,
  EMPLOYMENT_STATUS_LABELS,
  ROLE_LABELS,
  STAFF_ROLE_OPTIONS,
  toOptions,
} from '@/lib/enumLabels';
import { parseLocalDate } from '@/lib/dates';

const POSITION_OPTIONS = toOptions(STAFF_POSITION_LABELS);
const STATUS_OPTIONS = toOptions(EMPLOYMENT_STATUS_LABELS);
const ROLE_OPTIONS = toOptions(ROLE_LABELS, STAFF_ROLE_OPTIONS);

const staffFieldsShape = {
  employeeId: z.string().min(1, 'Vui lòng nhập mã nhân viên'),
  position: z.string().min(1, 'Vui lòng chọn chức vụ'),
  department: z.string().optional(),
  dateOfBirth: z.date().optional(),
  dateOfJoining: z.date().optional(),
  qualification: z.string().optional(),
  subjectSpecialization: z.string().optional(),
  salary: z.string().optional(),
  status: z.string().min(1, 'Vui lòng chọn trạng thái'),
  address: z.string().optional(),
  city: z.string().optional(),
  state: z.string().optional(),
  postalCode: z.string().optional(),
  emergencyContactName: z.string().optional(),
  emergencyContactPhone: z.string().optional(),
};

const createSchema = z.object({
  username: z.string().min(4, 'Tối thiểu 4 ký tự').max(50),
  email: z.string().email('Email không hợp lệ'),
  password: z.string().min(8, 'Tối thiểu 8 ký tự'),
  firstName: z.string().min(1, 'Vui lòng nhập tên'),
  lastName: z.string().min(1, 'Vui lòng nhập họ'),
  phoneNumber: z.string().optional(),
  role: z.string().min(1, 'Vui lòng chọn vai trò'),
  ...staffFieldsShape,
});

const editSchema = z.object(staffFieldsShape);

function toStaffPayload(values) {
  return {
    employeeId: values.employeeId,
    position: values.position,
    department: values.department || null,
    dateOfBirth: values.dateOfBirth ? format(values.dateOfBirth, 'yyyy-MM-dd') : null,
    dateOfJoining: values.dateOfJoining ? format(values.dateOfJoining, 'yyyy-MM-dd') : null,
    qualification: values.qualification || null,
    subjectSpecialization: values.subjectSpecialization || null,
    salary: values.salary ? Number(values.salary) : null,
    status: values.status,
    address: values.address || null,
    city: values.city || null,
    state: values.state || null,
    postalCode: values.postalCode || null,
    emergencyContactName: values.emergencyContactName || null,
    emergencyContactPhone: values.emergencyContactPhone || null,
  };
}

function StaffFieldsFragment({ control }) {
  return (
    <>
      <div className="grid grid-cols-2 gap-4">
        <TextField control={control} name="employeeId" label="Mã nhân viên" placeholder="NV001" />
        <SelectField control={control} name="position" label="Chức vụ" options={POSITION_OPTIONS} />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <TextField control={control} name="department" label="Phòng ban" />
        <SelectField control={control} name="status" label="Trạng thái" options={STATUS_OPTIONS} />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <DateField control={control} name="dateOfBirth" label="Ngày sinh" />
        <DateField control={control} name="dateOfJoining" label="Ngày vào làm" />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <TextField control={control} name="qualification" label="Trình độ" />
        <TextField control={control} name="subjectSpecialization" label="Chuyên môn" />
      </div>
      <TextField control={control} name="salary" label="Lương (VNĐ)" type="number" min="0" />
      <TextField control={control} name="address" label="Địa chỉ" />
      <div className="grid grid-cols-3 gap-4">
        <TextField control={control} name="city" label="Thành phố" />
        <TextField control={control} name="state" label="Tỉnh" />
        <TextField control={control} name="postalCode" label="Mã bưu điện" />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <TextField control={control} name="emergencyContactName" label="Người liên hệ khẩn cấp" />
        <TextField control={control} name="emergencyContactPhone" label="SĐT liên hệ khẩn cấp" />
      </div>
    </>
  );
}

function CreateStaffForm({ onSuccess }) {
  const queryClient = useQueryClient();
  const form = useAppForm({
    schema: createSchema,
    defaultValues: {
      username: '',
      email: '',
      password: '',
      firstName: '',
      lastName: '',
      phoneNumber: '',
      role: 'TEACHER',
      employeeId: '',
      position: '',
      department: '',
      qualification: '',
      subjectSpecialization: '',
      salary: '',
      status: 'ACTIVE',
      address: '',
      city: '',
      state: '',
      postalCode: '',
      emergencyContactName: '',
      emergencyContactPhone: '',
    },
  });

  const mutation = useMutation({
    mutationFn: async (values) => {
      // 2-step: create the login account first (ADMIN-only endpoint), then
      // the Staff record pointing at that new user - StaffController's
      // createStaff expects an existing user.id, it doesn't create one.
      const { data: account } = await userService.create({
        username: values.username,
        email: values.email,
        password: values.password,
        firstName: values.firstName,
        lastName: values.lastName,
        phoneNumber: values.phoneNumber || null,
        role: values.role,
      });
      return staffService.create({ user: { id: account.userId }, ...toStaffPayload(values) });
    },
    onSuccess: () => {
      toast.success('Đã thêm nhân viên mới');
      queryClient.invalidateQueries({ queryKey: ['staff'] });
      onSuccess();
    },
    onError: (err) => {
      toast.error(err?.response?.data?.message || err?.message || 'Không thể thêm nhân viên');
    },
  });

  return (
    <AppForm form={form} onSubmit={(v) => mutation.mutate(v)} className="space-y-4" id="staff-form">
      <p className="text-sm font-medium text-muted-foreground">Tài khoản đăng nhập</p>
      <div className="grid grid-cols-2 gap-4">
        <TextField control={form.control} name="username" label="Tên đăng nhập" />
        <SelectField control={form.control} name="role" label="Vai trò" options={ROLE_OPTIONS} />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <TextField control={form.control} name="firstName" label="Tên" />
        <TextField control={form.control} name="lastName" label="Họ" />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <TextField control={form.control} name="email" label="Email" type="email" />
        <TextField control={form.control} name="phoneNumber" label="Số điện thoại" />
      </div>
      <TextField control={form.control} name="password" label="Mật khẩu" type="password" />

      <p className="pt-2 text-sm font-medium text-muted-foreground">Thông tin nhân viên</p>
      <StaffFieldsFragment control={form.control} />

      <DialogFooter className="pt-2">
        <Button type="submit" form="staff-form" disabled={mutation.isPending}>
          {mutation.isPending ? 'Đang lưu...' : 'Thêm nhân viên'}
        </Button>
      </DialogFooter>
    </AppForm>
  );
}

function EditStaffForm({ staff, onSuccess }) {
  const queryClient = useQueryClient();
  const form = useAppForm({
    schema: editSchema,
    defaultValues: {
      employeeId: staff.employeeId ?? '',
      position: staff.position ?? '',
      department: staff.department ?? '',
      dateOfBirth: parseLocalDate(staff.dateOfBirth),
      dateOfJoining: parseLocalDate(staff.dateOfJoining),
      qualification: staff.qualification ?? '',
      subjectSpecialization: staff.subjectSpecialization ?? '',
      salary: staff.salary != null ? String(staff.salary) : '',
      status: staff.status ?? 'ACTIVE',
      address: staff.address ?? '',
      city: staff.city ?? '',
      state: staff.state ?? '',
      postalCode: staff.postalCode ?? '',
      emergencyContactName: staff.emergencyContactName ?? '',
      emergencyContactPhone: staff.emergencyContactPhone ?? '',
    },
  });

  const mutation = useMutation({
    mutationFn: (values) => staffService.update(staff.id, toStaffPayload(values)),
    onSuccess: () => {
      toast.success('Đã cập nhật thông tin nhân viên');
      queryClient.invalidateQueries({ queryKey: ['staff'] });
      onSuccess();
    },
    onError: (err) => {
      toast.error(err?.response?.data?.message || err?.message || 'Không thể cập nhật nhân viên');
    },
  });

  return (
    <AppForm form={form} onSubmit={(v) => mutation.mutate(v)} className="space-y-4" id="staff-form">
      <StaffFieldsFragment control={form.control} />
      <DialogFooter className="pt-2">
        <Button type="submit" form="staff-form" disabled={mutation.isPending}>
          {mutation.isPending ? 'Đang lưu...' : 'Lưu thay đổi'}
        </Button>
      </DialogFooter>
    </AppForm>
  );
}

/** open: boolean, onOpenChange: fn, staff: null (create mode) | StaffDTO (edit mode) */
function StaffFormDialog({ open, onOpenChange, staff }) {
  const isEdit = Boolean(staff);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[85vh] max-w-2xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Sửa thông tin nhân viên' : 'Thêm nhân viên mới'}</DialogTitle>
          <DialogDescription>
            {isEdit
              ? `Cập nhật hồ sơ của ${staff.user?.firstName} ${staff.user?.lastName}`
              : 'Tạo tài khoản đăng nhập và hồ sơ nhân viên mới'}
          </DialogDescription>
        </DialogHeader>
        {isEdit ? (
          <EditStaffForm staff={staff} onSuccess={() => onOpenChange(false)} />
        ) : (
          <CreateStaffForm onSuccess={() => onOpenChange(false)} />
        )}
      </DialogContent>
    </Dialog>
  );
}

export default StaffFormDialog;
