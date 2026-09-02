import React from 'react';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { subjectService } from '@/services/dataService';
import { useAppForm, AppForm } from '@/components/shared/Form';
import { TextField, SelectField } from '@/components/shared/FormFields';
import { toOptions, SUBJECT_CATEGORY_LABELS } from '@/lib/enumLabels';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog';

const categoryOptions = toOptions(SUBJECT_CATEGORY_LABELS);

const schema = z.object({
  code: z.string().min(1, 'Vui lòng nhập mã môn học'),
  name: z.string().min(1, 'Vui lòng nhập tên môn học'),
  category: z.string().min(1, 'Vui lòng chọn loại môn học'),
  gradeLevels: z.string().optional(),
});

function toPayload(values) {
  return {
    code: values.code.trim().toUpperCase(),
    name: values.name.trim(),
    category: values.category,
    gradeLevels: values.gradeLevels?.trim() || null,
  };
}

/** open, onOpenChange, subject: null (create) | SubjectDTO (edit) */
function SubjectFormDialog({ open, onOpenChange, subject }) {
  const isEdit = Boolean(subject);
  const queryClient = useQueryClient();

  const form = useAppForm({
    schema,
    defaultValues: {
      code: subject?.code ?? '',
      name: subject?.name ?? '',
      category: subject?.category ?? '',
      gradeLevels: subject?.gradeLevels ?? '',
    },
  });

  const mutation = useMutation({
    mutationFn: (values) =>
      isEdit ? subjectService.update(subject.id, toPayload(values)) : subjectService.create(toPayload(values)),
    onSuccess: () => {
      toast.success(isEdit ? 'Đã cập nhật môn học' : 'Đã thêm môn học mới');
      queryClient.invalidateQueries({ queryKey: ['subjects'] });
      onOpenChange(false);
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể lưu môn học'),
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Sửa môn học' : 'Thêm môn học mới'}</DialogTitle>
          <DialogDescription>{isEdit ? `Cập nhật môn ${subject.name}` : 'Tạo một môn học mới'}</DialogDescription>
        </DialogHeader>
        <AppForm form={form} onSubmit={(v) => mutation.mutate(v)} className="space-y-4" id="subject-form">
          <div className="grid grid-cols-2 gap-4">
            <TextField control={form.control} name="code" label="Mã môn học" placeholder="TOAN" />
            <SelectField control={form.control} name="category" label="Loại môn học" options={categoryOptions} />
          </div>
          <TextField control={form.control} name="name" label="Tên môn học" placeholder="Toán học" />
          <TextField
            control={form.control}
            name="gradeLevels"
            label="Khối áp dụng"
            placeholder="6,7,8,9,10,11,12"
            description="Danh sách khối lớp, cách nhau bằng dấu phẩy. Để trống nếu áp dụng mọi khối."
          />
          <DialogFooter className="pt-2">
            <Button type="submit" form="subject-form" disabled={mutation.isPending}>
              {mutation.isPending ? 'Đang lưu...' : isEdit ? 'Lưu thay đổi' : 'Thêm môn học'}
            </Button>
          </DialogFooter>
        </AppForm>
      </DialogContent>
    </Dialog>
  );
}

export default SubjectFormDialog;
