import React from 'react';
import { z } from 'zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { schoolClassService, academicYearService } from '@/services/dataService';
import { useAppForm, AppForm } from '@/components/shared/Form';
import { TextField, SelectField } from '@/components/shared/FormFields';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog';

const schema = z.object({
  className: z.string().min(1, 'Vui lòng nhập lớp'),
  section: z.string().min(1, 'Vui lòng nhập ban'),
  academicYear: z.string().min(1, 'Vui lòng chọn năm học'),
  capacity: z.string().optional(),
  roomNumber: z.string().optional(),
});

function toPayload(values) {
  return {
    className: values.className,
    section: values.section,
    academicYear: values.academicYear,
    capacity: values.capacity ? Number(values.capacity) : null,
    roomNumber: values.roomNumber || null,
  };
}

/** open, onOpenChange, schoolClass: null (create) | SchoolClassDTO (edit) */
function ClassFormDialog({ open, onOpenChange, schoolClass }) {
  const isEdit = Boolean(schoolClass);
  const queryClient = useQueryClient();

  const academicYearsQuery = useQuery({
    queryKey: ['academic-years'],
    queryFn: () => academicYearService.getAll().then((r) => r.data),
    enabled: open,
  });
  const activeYear = academicYearsQuery.data?.find((y) => y.status === 'ACTIVE')?.name;
  const yearOptions = (academicYearsQuery.data ?? []).map((y) => ({ value: y.name, label: y.name }));

  const form = useAppForm({
    schema,
    defaultValues: {
      className: schoolClass?.className ?? '',
      section: schoolClass?.section ?? '',
      academicYear: schoolClass?.academicYear ?? activeYear ?? '',
      capacity: schoolClass?.capacity != null ? String(schoolClass.capacity) : '',
      roomNumber: schoolClass?.roomNumber ?? '',
    },
  });

  const mutation = useMutation({
    mutationFn: (values) =>
      isEdit ? schoolClassService.update(schoolClass.id, toPayload(values)) : schoolClassService.create(toPayload(values)),
    onSuccess: () => {
      toast.success(isEdit ? 'Đã cập nhật lớp học' : 'Đã thêm lớp học mới');
      queryClient.invalidateQueries({ queryKey: ['classes'] });
      onOpenChange(false);
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể lưu lớp học'),
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Sửa lớp học' : 'Thêm lớp học mới'}</DialogTitle>
          <DialogDescription>
            {isEdit ? `Cập nhật lớp ${schoolClass.className} - ${schoolClass.section}` : 'Tạo một lớp học mới'}
          </DialogDescription>
        </DialogHeader>
        <AppForm form={form} onSubmit={(v) => mutation.mutate(v)} className="space-y-4" id="class-form">
          <div className="grid grid-cols-2 gap-4">
            <TextField control={form.control} name="className" label="Lớp" placeholder="10" />
            <TextField control={form.control} name="section" label="Ban" placeholder="A" />
          </div>
          <SelectField
            control={form.control}
            name="academicYear"
            label="Năm học"
            options={yearOptions}
            placeholder={academicYearsQuery.isLoading ? 'Đang tải...' : 'Chọn năm học'}
          />
          <div className="grid grid-cols-2 gap-4">
            <TextField control={form.control} name="capacity" label="Sĩ số tối đa" type="number" min="0" />
            <TextField control={form.control} name="roomNumber" label="Phòng học" />
          </div>
          <DialogFooter className="pt-2">
            <Button type="submit" form="class-form" disabled={mutation.isPending}>
              {mutation.isPending ? 'Đang lưu...' : isEdit ? 'Lưu thay đổi' : 'Thêm lớp học'}
            </Button>
          </DialogFooter>
        </AppForm>
      </DialogContent>
    </Dialog>
  );
}

export default ClassFormDialog;
