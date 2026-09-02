import React from 'react';
import { z } from 'zod';
import { format } from 'date-fns';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { semesterService, academicYearService } from '@/services/dataService';
import { parseLocalDate } from '@/lib/dates';
import { useAppForm, AppForm } from '@/components/shared/Form';
import { SelectField, DateField } from '@/components/shared/FormFields';
import { toOptions, SEMESTER_NAME_LABELS } from '@/lib/enumLabels';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog';

const nameOptions = toOptions(SEMESTER_NAME_LABELS);

const schema = z
  .object({
    academicYearId: z.string().min(1, 'Vui lòng chọn năm học'),
    name: z.string().min(1, 'Vui lòng chọn học kỳ'),
    startDate: z.date({ required_error: 'Vui lòng chọn ngày bắt đầu' }),
    endDate: z.date({ required_error: 'Vui lòng chọn ngày kết thúc' }),
  })
  .refine((v) => !v.startDate || !v.endDate || v.endDate > v.startDate, {
    message: 'Ngày kết thúc phải sau ngày bắt đầu',
    path: ['endDate'],
  });

function toPayload(values) {
  return {
    academicYear: { id: Number(values.academicYearId) },
    name: values.name,
    startDate: format(values.startDate, 'yyyy-MM-dd'),
    endDate: format(values.endDate, 'yyyy-MM-dd'),
  };
}

/** open, onOpenChange, semester: null (create) | SemesterDTO (edit) */
function SemesterFormDialog({ open, onOpenChange, semester }) {
  const isEdit = Boolean(semester);
  const queryClient = useQueryClient();

  const academicYearsQuery = useQuery({
    queryKey: ['academic-years'],
    queryFn: () => academicYearService.getAll().then((r) => r.data),
    enabled: open,
  });
  const activeYearId = academicYearsQuery.data?.find((y) => y.status === 'ACTIVE')?.id;
  const yearOptions = (academicYearsQuery.data ?? []).map((y) => ({ value: String(y.id), label: y.name }));

  const form = useAppForm({
    schema,
    defaultValues: {
      academicYearId: semester?.academicYearId ? String(semester.academicYearId) : activeYearId ? String(activeYearId) : '',
      name: semester?.name ?? '',
      startDate: parseLocalDate(semester?.startDate),
      endDate: parseLocalDate(semester?.endDate),
    },
  });

  const mutation = useMutation({
    mutationFn: (values) =>
      isEdit ? semesterService.update(semester.id, toPayload(values)) : semesterService.create(toPayload(values)),
    onSuccess: () => {
      toast.success(isEdit ? 'Đã cập nhật học kỳ' : 'Đã thêm học kỳ mới');
      queryClient.invalidateQueries({ queryKey: ['semesters'] });
      onOpenChange(false);
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể lưu học kỳ'),
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Sửa học kỳ' : 'Thêm học kỳ mới'}</DialogTitle>
          <DialogDescription>
            {isEdit ? `Cập nhật ${SEMESTER_NAME_LABELS[semester.name] ?? semester.name}` : 'Tạo một học kỳ mới'}
          </DialogDescription>
        </DialogHeader>
        <AppForm form={form} onSubmit={(v) => mutation.mutate(v)} className="space-y-4" id="semester-form">
          <SelectField
            control={form.control}
            name="academicYearId"
            label="Năm học"
            options={yearOptions}
            placeholder={academicYearsQuery.isLoading ? 'Đang tải...' : 'Chọn năm học'}
          />
          <SelectField control={form.control} name="name" label="Học kỳ" options={nameOptions} />
          <div className="grid grid-cols-2 gap-4">
            <DateField control={form.control} name="startDate" label="Ngày bắt đầu" />
            <DateField control={form.control} name="endDate" label="Ngày kết thúc" />
          </div>
          <DialogFooter className="pt-2">
            <Button type="submit" form="semester-form" disabled={mutation.isPending}>
              {mutation.isPending ? 'Đang lưu...' : isEdit ? 'Lưu thay đổi' : 'Thêm học kỳ'}
            </Button>
          </DialogFooter>
        </AppForm>
      </DialogContent>
    </Dialog>
  );
}

export default SemesterFormDialog;
