import React from 'react';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { gradeConfigService } from '@/services/dataService';
import { useAppForm, AppForm } from '@/components/shared/Form';
import { TextField, SelectField } from '@/components/shared/FormFields';
import { toOptions, GRADE_COMPONENT_TYPE_LABELS } from '@/lib/enumLabels';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog';

const componentTypeOptions = toOptions(GRADE_COMPONENT_TYPE_LABELS);

const schema = z.object({
  componentType: z.string().min(1, 'Vui lòng chọn loại điểm'),
  weight: z.coerce
    .number({ invalid_type_error: 'Vui lòng nhập hệ số' })
    .int('Hệ số phải là số nguyên')
    .positive('Hệ số phải lớn hơn 0'),
  // Matches the backend's own @Pattern("^\\d{4}-\\d{4}$") on
  // GradeComponentConfig.appliesFrom - kept in sync deliberately rather
  // than just letting a malformed value 400 round-trip, since
  // GradeRecordService parses this same string to resolve which weight is
  // in effect for a given semester (see its class doc comment).
  appliesFrom: z
    .string()
    .regex(/^\d{4}-\d{4}$/, 'Định dạng năm học không hợp lệ, ví dụ 2025-2026'),
});

function toPayload(values) {
  return {
    componentType: values.componentType,
    weight: Number(values.weight),
    appliesFrom: values.appliesFrom.trim(),
  };
}

/** open, onOpenChange, config: null (create) | GradeComponentConfigDTO (edit) */
function GradeConfigFormDialog({ open, onOpenChange, config }) {
  const isEdit = Boolean(config);
  const queryClient = useQueryClient();

  const form = useAppForm({
    schema,
    defaultValues: {
      componentType: config?.componentType ?? '',
      weight: config?.weight ?? 1,
      appliesFrom: config?.appliesFrom ?? '',
    },
  });

  const mutation = useMutation({
    mutationFn: (values) =>
      isEdit ? gradeConfigService.update(config.id, toPayload(values)) : gradeConfigService.create(toPayload(values)),
    onSuccess: () => {
      toast.success(isEdit ? 'Đã cập nhật hệ số điểm' : 'Đã thêm hệ số điểm mới');
      queryClient.invalidateQueries({ queryKey: ['grade-configs'] });
      onOpenChange(false);
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể lưu hệ số điểm'),
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Sửa hệ số điểm' : 'Thêm hệ số điểm'}</DialogTitle>
          <DialogDescription>
            {isEdit
              ? `Cập nhật hệ số ${GRADE_COMPONENT_TYPE_LABELS[config.componentType] ?? config.componentType}`
              : 'Hệ số dùng khi tính điểm trung bình môn: Σ(điểm × hệ số) / Σ(hệ số)'}
          </DialogDescription>
        </DialogHeader>
        <AppForm form={form} onSubmit={(v) => mutation.mutate(v)} className="space-y-4" id="grade-config-form">
          <SelectField control={form.control} name="componentType" label="Loại điểm" options={componentTypeOptions} />
          <div className="grid grid-cols-2 gap-4">
            <TextField control={form.control} name="weight" label="Hệ số" type="number" placeholder="1" />
            <TextField
              control={form.control}
              name="appliesFrom"
              label="Áp dụng từ năm học"
              placeholder="2025-2026"
            />
          </div>
          <DialogFooter className="pt-2">
            <Button type="submit" form="grade-config-form" disabled={mutation.isPending}>
              {mutation.isPending ? 'Đang lưu...' : isEdit ? 'Lưu thay đổi' : 'Thêm hệ số'}
            </Button>
          </DialogFooter>
        </AppForm>
      </DialogContent>
    </Dialog>
  );
}

export default GradeConfigFormDialog;
