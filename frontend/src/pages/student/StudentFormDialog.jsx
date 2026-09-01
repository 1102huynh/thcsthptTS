import React from 'react';
import { z } from 'zod';
import { format } from 'date-fns';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { userService, studentService } from '@/services/dataService';
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
import { STUDENT_STATUS_LABELS, GENDER_LABELS, toOptions } from '@/lib/enumLabels';
import { parseLocalDate } from '@/lib/dates';

const STATUS_OPTIONS = toOptions(STUDENT_STATUS_LABELS);
const GENDER_OPTIONS = toOptions(GENDER_LABELS);

const studentFieldsShape = {
  rollNumber: z.string().min(1, 'Vui lòng nhập số báo danh'),
  admissionNumber: z.string().min(1, 'Vui lòng nhập số nhập học'),
  dateOfBirth: z.date().optional(),
  gender: z.string().optional(),
  bloodGroup: z.string().optional(),
  className: z.string().optional(),
  section: z.string().optional(),
  dateOfAdmission: z.date().optional(),
  status: z.string().min(1, 'Vui lòng chọn trạng thái'),
  fatherName: z.string().optional(),
  fatherPhone: z.string().optional(),
  fatherOccupation: z.string().optional(),
  motherName: z.string().optional(),
  motherPhone: z.string().optional(),
  motherOccupation: z.string().optional(),
  address: z.string().optional(),
  city: z.string().optional(),
  state: z.string().optional(),
  postalCode: z.string().optional(),
  emergencyContactName: z.string().optional(),
  emergencyContactPhone: z.string().optional(),
  emergencyContactRelation: z.string().optional(),
};

const createSchema = z.object({
  username: z.string().min(4, 'Tối thiểu 4 ký tự').max(50),
  email: z.string().email('Email không hợp lệ'),
  password: z.string().min(8, 'Tối thiểu 8 ký tự'),
  firstName: z.string().min(1, 'Vui lòng nhập tên'),
  lastName: z.string().min(1, 'Vui lòng nhập họ'),
  phoneNumber: z.string().optional(),
  ...studentFieldsShape,
});

const editSchema = z.object(studentFieldsShape);

function toStudentPayload(values) {
  return {
    rollNumber: values.rollNumber,
    admissionNumber: values.admissionNumber,
    dateOfBirth: values.dateOfBirth ? format(values.dateOfBirth, 'yyyy-MM-dd') : null,
    gender: values.gender || null,
    bloodGroup: values.bloodGroup || null,
    className: values.className || null,
    section: values.section || null,
    dateOfAdmission: values.dateOfAdmission ? format(values.dateOfAdmission, 'yyyy-MM-dd') : null,
    status: values.status,
    fatherName: values.fatherName || null,
    fatherPhone: values.fatherPhone || null,
    fatherOccupation: values.fatherOccupation || null,
    motherName: values.motherName || null,
    motherPhone: values.motherPhone || null,
    motherOccupation: values.motherOccupation || null,
    address: values.address || null,
    city: values.city || null,
    state: values.state || null,
    postalCode: values.postalCode || null,
    emergencyContactName: values.emergencyContactName || null,
    emergencyContactPhone: values.emergencyContactPhone || null,
    emergencyContactRelation: values.emergencyContactRelation || null,
  };
}

function StudentFieldsFragment({ control }) {
  return (
    <>
      <div className="grid grid-cols-2 gap-4">
        <TextField control={control} name="rollNumber" label="Số báo danh" />
        <TextField control={control} name="admissionNumber" label="Số nhập học" />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <TextField control={control} name="className" label="Lớp" placeholder="10" />
        <TextField control={control} name="section" label="Ban" placeholder="A1" />
      </div>
      <div className="grid grid-cols-3 gap-4">
        <SelectField control={control} name="gender" label="Giới tính" options={GENDER_OPTIONS} />
        <TextField control={control} name="bloodGroup" label="Nhóm máu" placeholder="O+" />
        <SelectField control={control} name="status" label="Trạng thái" options={STATUS_OPTIONS} />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <DateField control={control} name="dateOfBirth" label="Ngày sinh" />
        <DateField control={control} name="dateOfAdmission" label="Ngày nhập học" />
      </div>

      <p className="pt-2 text-sm font-medium text-muted-foreground">Thông tin phụ huynh</p>
      <div className="grid grid-cols-3 gap-4">
        <TextField control={control} name="fatherName" label="Họ tên cha" />
        <TextField control={control} name="fatherPhone" label="SĐT cha" />
        <TextField control={control} name="fatherOccupation" label="Nghề nghiệp cha" />
      </div>
      <div className="grid grid-cols-3 gap-4">
        <TextField control={control} name="motherName" label="Họ tên mẹ" />
        <TextField control={control} name="motherPhone" label="SĐT mẹ" />
        <TextField control={control} name="motherOccupation" label="Nghề nghiệp mẹ" />
      </div>

      <p className="pt-2 text-sm font-medium text-muted-foreground">Địa chỉ</p>
      <TextField control={control} name="address" label="Địa chỉ" />
      <div className="grid grid-cols-3 gap-4">
        <TextField control={control} name="city" label="Thành phố" />
        <TextField control={control} name="state" label="Tỉnh" />
        <TextField control={control} name="postalCode" label="Mã bưu điện" />
      </div>

      <p className="pt-2 text-sm font-medium text-muted-foreground">Liên hệ khẩn cấp</p>
      <div className="grid grid-cols-3 gap-4">
        <TextField control={control} name="emergencyContactName" label="Họ tên" />
        <TextField control={control} name="emergencyContactPhone" label="SĐT" />
        <TextField control={control} name="emergencyContactRelation" label="Quan hệ" placeholder="Cha/Mẹ/..." />
      </div>
    </>
  );
}

function CreateStudentForm({ onSuccess }) {
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
      rollNumber: '',
      admissionNumber: '',
      gender: '',
      bloodGroup: '',
      className: '',
      section: '',
      status: 'ACTIVE',
      fatherName: '',
      fatherPhone: '',
      fatherOccupation: '',
      motherName: '',
      motherPhone: '',
      motherOccupation: '',
      address: '',
      city: '',
      state: '',
      postalCode: '',
      emergencyContactName: '',
      emergencyContactPhone: '',
      emergencyContactRelation: '',
    },
  });

  const mutation = useMutation({
    mutationFn: async (values) => {
      // Same 2-step flow as StaffFormDialog: create the login account first
      // (ADMIN-only endpoint, role fixed to STUDENT - a student account has
      // no reason to be anything else here), then the Student record
      // pointing at that new user.
      const { data: account } = await userService.create({
        username: values.username,
        email: values.email,
        password: values.password,
        firstName: values.firstName,
        lastName: values.lastName,
        phoneNumber: values.phoneNumber || null,
        role: 'STUDENT',
      });
      return studentService.create({ user: { id: account.userId }, ...toStudentPayload(values) });
    },
    onSuccess: () => {
      toast.success('Đã thêm học sinh mới');
      queryClient.invalidateQueries({ queryKey: ['students'] });
      onSuccess();
    },
    onError: (err) => {
      toast.error(err?.response?.data?.message || err?.message || 'Không thể thêm học sinh');
    },
  });

  return (
    <AppForm form={form} onSubmit={(v) => mutation.mutate(v)} className="space-y-4" id="student-form">
      <p className="text-sm font-medium text-muted-foreground">Tài khoản đăng nhập</p>
      <div className="grid grid-cols-2 gap-4">
        <TextField control={form.control} name="username" label="Tên đăng nhập" />
        <TextField control={form.control} name="phoneNumber" label="Số điện thoại" />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <TextField control={form.control} name="firstName" label="Tên" />
        <TextField control={form.control} name="lastName" label="Họ" />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <TextField control={form.control} name="email" label="Email" type="email" />
        <TextField control={form.control} name="password" label="Mật khẩu" type="password" />
      </div>

      <p className="pt-2 text-sm font-medium text-muted-foreground">Thông tin học sinh</p>
      <StudentFieldsFragment control={form.control} />

      <DialogFooter className="pt-2">
        <Button type="submit" form="student-form" disabled={mutation.isPending}>
          {mutation.isPending ? 'Đang lưu...' : 'Thêm học sinh'}
        </Button>
      </DialogFooter>
    </AppForm>
  );
}

function EditStudentForm({ student, onSuccess }) {
  const queryClient = useQueryClient();
  const form = useAppForm({
    schema: editSchema,
    defaultValues: {
      rollNumber: student.rollNumber ?? '',
      admissionNumber: student.admissionNumber ?? '',
      dateOfBirth: parseLocalDate(student.dateOfBirth),
      gender: student.gender ?? '',
      bloodGroup: student.bloodGroup ?? '',
      className: student.className ?? '',
      section: student.section ?? '',
      dateOfAdmission: parseLocalDate(student.dateOfAdmission),
      status: student.status ?? 'ACTIVE',
      fatherName: student.fatherName ?? '',
      fatherPhone: student.fatherPhone ?? '',
      fatherOccupation: student.fatherOccupation ?? '',
      motherName: student.motherName ?? '',
      motherPhone: student.motherPhone ?? '',
      motherOccupation: student.motherOccupation ?? '',
      address: student.address ?? '',
      city: student.city ?? '',
      state: student.state ?? '',
      postalCode: student.postalCode ?? '',
      emergencyContactName: student.emergencyContactName ?? '',
      emergencyContactPhone: student.emergencyContactPhone ?? '',
      emergencyContactRelation: student.emergencyContactRelation ?? '',
    },
  });

  const mutation = useMutation({
    mutationFn: (values) => studentService.update(student.id, toStudentPayload(values)),
    onSuccess: () => {
      toast.success('Đã cập nhật thông tin học sinh');
      queryClient.invalidateQueries({ queryKey: ['students'] });
      onSuccess();
    },
    onError: (err) => {
      toast.error(err?.response?.data?.message || err?.message || 'Không thể cập nhật học sinh');
    },
  });

  return (
    <AppForm form={form} onSubmit={(v) => mutation.mutate(v)} className="space-y-4" id="student-form">
      <StudentFieldsFragment control={form.control} />
      <DialogFooter className="pt-2">
        <Button type="submit" form="student-form" disabled={mutation.isPending}>
          {mutation.isPending ? 'Đang lưu...' : 'Lưu thay đổi'}
        </Button>
      </DialogFooter>
    </AppForm>
  );
}

/** open: boolean, onOpenChange: fn, student: null (create mode) | StudentDTO (edit mode) */
function StudentFormDialog({ open, onOpenChange, student }) {
  const isEdit = Boolean(student);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[85vh] max-w-2xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Sửa thông tin học sinh' : 'Thêm học sinh mới'}</DialogTitle>
          <DialogDescription>
            {isEdit
              ? `Cập nhật hồ sơ của ${student.user?.firstName} ${student.user?.lastName}`
              : 'Tạo tài khoản đăng nhập và hồ sơ học sinh mới'}
          </DialogDescription>
        </DialogHeader>
        {isEdit ? (
          <EditStudentForm student={student} onSuccess={() => onOpenChange(false)} />
        ) : (
          <CreateStudentForm onSuccess={() => onOpenChange(false)} />
        )}
      </DialogContent>
    </Dialog>
  );
}

export default StudentFormDialog;
