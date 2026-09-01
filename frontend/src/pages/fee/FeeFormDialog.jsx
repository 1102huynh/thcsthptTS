import React from 'react';
import { z } from 'zod';
import { format } from 'date-fns';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { feeService, studentService } from '@/services/dataService';
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
import { FEE_TYPE_PRESETS, toOptions } from '@/lib/enumLabels';
import { parseLocalDate } from '@/lib/dates';

const FEE_TYPE_OPTIONS = FEE_TYPE_PRESETS.map((t) => ({ value: t, label: t }));

const createSchema = z.object({
  studentId: z.string().min(1, 'Vui lòng chọn học sinh'),
  feeType: z.string().min(1, 'Vui lòng chọn loại phí'),
  amount: z.string().min(1, 'Vui lòng nhập số tiền'),
  dueDate: z.date().optional(),
});

const editSchema = z.object({
  feeType: z.string().min(1, 'Vui lòng chọn loại phí'),
  amount: z.string().min(1, 'Vui lòng nhập số tiền'),
  dueDate: z.date().optional(),
});

function CreateFeeForm({ students, academicYear, onSuccess }) {
  const queryClient = useQueryClient();
  const studentOptions = toOptions(
    Object.fromEntries(students.map((s) => [String(s.id), `${s.rollNumber} - ${s.user?.firstName} ${s.user?.lastName}`]))
  );

  const form = useAppForm({
    schema: createSchema,
    defaultValues: { studentId: '', feeType: FEE_TYPE_PRESETS[0], amount: '' },
  });

  const mutation = useMutation({
    mutationFn: (values) =>
      feeService.createFee({
        student: { id: Number(values.studentId) },
        academicYear,
        feeType: values.feeType,
        amount: Number(values.amount),
        dueDate: values.dueDate ? format(values.dueDate, 'yyyy-MM-dd') : null,
      }),
    onSuccess: () => {
      toast.success('Đã thêm khoản thu');
      queryClient.invalidateQueries({ queryKey: ['fees'] });
      onSuccess();
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể thêm khoản thu'),
  });

  return (
    <AppForm form={form} onSubmit={(v) => mutation.mutate(v)} className="space-y-4" id="fee-form">
      <SelectField control={form.control} name="studentId" label="Học sinh" options={studentOptions} placeholder="Chọn học sinh..." />
      <div className="grid grid-cols-2 gap-4">
        <SelectField control={form.control} name="feeType" label="Loại phí" options={FEE_TYPE_OPTIONS} />
        <TextField control={form.control} name="amount" label="Số tiền (VNĐ)" type="number" min="0" />
      </div>
      <DateField control={form.control} name="dueDate" label="Hạn nộp" />
      <DialogFooter className="pt-2">
        <Button type="submit" form="fee-form" disabled={mutation.isPending}>
          {mutation.isPending ? 'Đang lưu...' : 'Thêm khoản thu'}
        </Button>
      </DialogFooter>
    </AppForm>
  );
}

function EditFeeForm({ fee, onSuccess }) {
  const queryClient = useQueryClient();
  const form = useAppForm({
    schema: editSchema,
    defaultValues: {
      feeType: fee.feeType ?? FEE_TYPE_PRESETS[0],
      amount: fee.amount != null ? String(fee.amount) : '',
      dueDate: parseLocalDate(fee.dueDate),
    },
  });

  const mutation = useMutation({
    mutationFn: (values) =>
      feeService.updateFee(fee.id, {
        // FeeController's PUT deserializes into the raw Fee entity with
        // @Valid (student/academicYear/feeType/amount all required), even
        // though FeeService.updateFee only ever reads feeType/amount/
        // dueDate - same gotcha as GradeController's PUT, see
        // GradeFormDialog-equivalent comment there. Sending the record's
        // own unchanged student/academicYear satisfies validation.
        student: { id: fee.studentId },
        academicYear: fee.academicYear,
        feeType: values.feeType,
        amount: Number(values.amount),
        dueDate: values.dueDate ? format(values.dueDate, 'yyyy-MM-dd') : null,
      }),
    onSuccess: () => {
      toast.success('Đã cập nhật khoản thu');
      queryClient.invalidateQueries({ queryKey: ['fees'] });
      onSuccess();
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể cập nhật khoản thu'),
  });

  return (
    <AppForm form={form} onSubmit={(v) => mutation.mutate(v)} className="space-y-4" id="fee-form">
      <div className="grid grid-cols-2 gap-4">
        <SelectField control={form.control} name="feeType" label="Loại phí" options={FEE_TYPE_OPTIONS} />
        <TextField control={form.control} name="amount" label="Số tiền (VNĐ)" type="number" min="0" />
      </div>
      <DateField control={form.control} name="dueDate" label="Hạn nộp" />
      <DialogFooter className="pt-2">
        <Button type="submit" form="fee-form" disabled={mutation.isPending}>
          {mutation.isPending ? 'Đang lưu...' : 'Lưu thay đổi'}
        </Button>
      </DialogFooter>
    </AppForm>
  );
}

/** open, onOpenChange, fee: null (create) | FeeDTO (edit), academicYear (for create) */
function FeeFormDialog({ open, onOpenChange, fee, academicYear }) {
  const isEdit = Boolean(fee);
  const studentsQuery = useQuery({
    queryKey: ['students'],
    queryFn: () => studentService.getAll().then((r) => r.data),
    enabled: open && !isEdit,
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[85vh] max-w-lg overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Sửa khoản thu' : 'Thêm khoản thu mới'}</DialogTitle>
          <DialogDescription>
            {isEdit ? `Cập nhật "${fee.feeType}" của ${fee.studentName}` : 'Tạo một khoản thu mới cho học sinh'}
          </DialogDescription>
        </DialogHeader>
        {isEdit ? (
          <EditFeeForm fee={fee} onSuccess={() => onOpenChange(false)} />
        ) : studentsQuery.isLoading ? (
          <p className="py-8 text-center text-sm text-muted-foreground">Đang tải danh sách học sinh...</p>
        ) : (
          <CreateFeeForm
            students={studentsQuery.data ?? []}
            academicYear={academicYear}
            onSuccess={() => onOpenChange(false)}
          />
        )}
      </DialogContent>
    </Dialog>
  );
}

export default FeeFormDialog;
