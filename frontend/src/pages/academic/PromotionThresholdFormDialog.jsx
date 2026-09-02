import React from 'react';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { promotionThresholdService } from '@/services/dataService';
import { useAppForm, AppForm } from '@/components/shared/Form';
import { TextField, SelectField } from '@/components/shared/FormFields';
import { toOptions, CONDUCT_RATING_LABELS } from '@/lib/enumLabels';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog';

const conductOptions = toOptions(CONDUCT_RATING_LABELS);

const schema = z.object({
  appliesFrom: z
    .string()
    .regex(/^\d{4}-\d{4}$/, 'Định dạng năm học không hợp lệ, ví dụ 2025-2026'),
  minSubjectAverage: z.coerce
    .number({ invalid_type_error: 'Vui lòng nhập điểm TB tối thiểu' })
    .min(0, 'Điểm TB phải từ 0 đến 10')
    .max(10, 'Điểm TB phải từ 0 đến 10'),
  minConduct: z.string().min(1, 'Vui lòng chọn hạnh kiểm tối thiểu'),
  maxAbsenceRate: z.coerce
    .number({ invalid_type_error: 'Vui lòng nhập tỷ lệ nghỉ tối đa' })
    .min(0, 'Tỷ lệ nghỉ phải từ 0 đến 100')
    .max(100, 'Tỷ lệ nghỉ phải từ 0 đến 100'),
});

function toPayload(values) {
  return {
    appliesFrom: values.appliesFrom.trim(),
    minSubjectAverage: Number(values.minSubjectAverage),
    minConduct: values.minConduct,
    maxAbsenceRate: Number(values.maxAbsenceRate),
  };
}

/** open, onOpenChange, threshold: null (create) | PromotionThresholdConfigDTO (edit) */
function PromotionThresholdFormDialog({ open, onOpenChange, threshold }) {
  const isEdit = Boolean(threshold);
  const queryClient = useQueryClient();

  const form = useAppForm({
    schema,
    defaultValues: {
      appliesFrom: threshold?.appliesFrom ?? '',
      minSubjectAverage: threshold?.minSubjectAverage ?? 5,
      minConduct: threshold?.minConduct ?? '',
      maxAbsenceRate: threshold?.maxAbsenceRate ?? 20,
    },
  });

  const mutation = useMutation({
    mutationFn: (values) =>
      isEdit
        ? promotionThresholdService.update(threshold.id, toPayload(values))
        : promotionThresholdService.create(toPayload(values)),
    onSuccess: () => {
      toast.success(isEdit ? 'Đã cập nhật ngưỡng xét lên lớp' : 'Đã thêm ngưỡng xét lên lớp mới');
      queryClient.invalidateQueries({ queryKey: ['promotion-thresholds'] });
      onOpenChange(false);
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể lưu ngưỡng xét lên lớp'),
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Sửa ngưỡng xét lên lớp' : 'Thêm ngưỡng xét lên lớp'}</DialogTitle>
          <DialogDescription>
            Chỉ là gợi ý xét lên lớp tự động, không phải công thức xếp loại học lực chính thức — quyết định cuối
            cùng luôn do con người xác nhận.
          </DialogDescription>
        </DialogHeader>
        <AppForm form={form} onSubmit={(v) => mutation.mutate(v)} className="space-y-4" id="promotion-threshold-form">
          <TextField control={form.control} name="appliesFrom" label="Áp dụng từ năm học" placeholder="2025-2026" />
          <div className="grid grid-cols-2 gap-4">
            <TextField
              control={form.control}
              name="minSubjectAverage"
              label="Điểm TB môn tối thiểu"
              type="number"
              placeholder="5"
              description="Không môn nào được thấp hơn mức này"
            />
            <SelectField control={form.control} name="minConduct" label="Hạnh kiểm tối thiểu" options={conductOptions} />
          </div>
          <TextField
            control={form.control}
            name="maxAbsenceRate"
            label="Tỷ lệ nghỉ tối đa (%)"
            type="number"
            placeholder="20"
          />
          <DialogFooter className="pt-2">
            <Button type="submit" form="promotion-threshold-form" disabled={mutation.isPending}>
              {mutation.isPending ? 'Đang lưu...' : isEdit ? 'Lưu thay đổi' : 'Thêm ngưỡng'}
            </Button>
          </DialogFooter>
        </AppForm>
      </DialogContent>
    </Dialog>
  );
}

export default PromotionThresholdFormDialog;
