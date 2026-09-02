import React from 'react';
import { z } from 'zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { teachingAssignmentService, subjectService, staffService } from '@/services/dataService';
import { useAppForm, AppForm } from '@/components/shared/Form';
import { SelectField } from '@/components/shared/FormFields';
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
  subjectId: z.string().min(1, 'Vui lòng chọn môn học'),
  teacherId: z.string().min(1, 'Vui lòng chọn giáo viên'),
});

/** open, onOpenChange, schoolClassId, semesterId, classLabel, assignment: null (create) | TeachingAssignmentDTO (edit) */
function TeachingAssignmentFormDialog({ open, onOpenChange, schoolClassId, semesterId, classLabel, assignment }) {
  const isEdit = Boolean(assignment);
  const queryClient = useQueryClient();

  const subjectsQuery = useQuery({
    queryKey: ['subjects'],
    queryFn: () => subjectService.getAll().then((r) => r.data),
    enabled: open,
  });
  const staffQuery = useQuery({
    queryKey: ['staff'],
    queryFn: () => staffService.getAll().then((r) => r.data),
    enabled: open,
  });
  const subjectOptions = (subjectsQuery.data ?? []).map((s) => ({ value: String(s.id), label: s.name }));
  const teacherOptions = (staffQuery.data ?? [])
    .filter((s) => s.position === 'TEACHER')
    .map((s) => ({ value: String(s.id), label: `${s.user?.firstName ?? ''} ${s.user?.lastName ?? ''}`.trim() }));

  const form = useAppForm({
    schema,
    defaultValues: {
      subjectId: assignment?.subjectId ? String(assignment.subjectId) : '',
      teacherId: assignment?.teacherId ? String(assignment.teacherId) : '',
    },
  });

  const mutation = useMutation({
    mutationFn: (values) => {
      const payload = {
        schoolClass: { id: schoolClassId },
        subject: { id: Number(values.subjectId) },
        teacher: { id: Number(values.teacherId) },
        semester: { id: semesterId },
      };
      return isEdit ? teachingAssignmentService.update(assignment.id, payload) : teachingAssignmentService.create(payload);
    },
    onSuccess: () => {
      toast.success(isEdit ? 'Đã cập nhật phân công giảng dạy' : 'Đã thêm phân công giảng dạy');
      queryClient.invalidateQueries({ queryKey: ['teaching-assignments'] });
      onOpenChange(false);
    },
    onError: (err) =>
      toast.error(err?.response?.data?.message || err?.message || 'Không thể lưu phân công giảng dạy'),
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Sửa phân công giảng dạy' : 'Thêm phân công giảng dạy'}</DialogTitle>
          <DialogDescription>Lớp {classLabel}</DialogDescription>
        </DialogHeader>
        <AppForm form={form} onSubmit={(v) => mutation.mutate(v)} className="space-y-4" id="teaching-assignment-form">
          <SelectField
            control={form.control}
            name="subjectId"
            label="Môn học"
            options={subjectOptions}
            placeholder={subjectsQuery.isLoading ? 'Đang tải...' : 'Chọn môn học'}
          />
          <SelectField
            control={form.control}
            name="teacherId"
            label="Giáo viên"
            options={teacherOptions}
            placeholder={staffQuery.isLoading ? 'Đang tải...' : 'Chọn giáo viên'}
          />
          <DialogFooter className="pt-2">
            <Button type="submit" form="teaching-assignment-form" disabled={mutation.isPending}>
              {mutation.isPending ? 'Đang lưu...' : isEdit ? 'Lưu thay đổi' : 'Thêm phân công'}
            </Button>
          </DialogFooter>
        </AppForm>
      </DialogContent>
    </Dialog>
  );
}

export default TeachingAssignmentFormDialog;
