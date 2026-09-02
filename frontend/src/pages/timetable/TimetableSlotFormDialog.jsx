import React from 'react';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { timetableService } from '@/services/dataService';
import { useAppForm, AppForm } from '@/components/shared/Form';
import { TextField, SelectField } from '@/components/shared/FormFields';
import { toOptions, DAY_OF_WEEK_LABELS } from '@/lib/enumLabels';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog';

const dayOptions = toOptions(DAY_OF_WEEK_LABELS);
const periodOptions = Array.from({ length: 10 }, (_, i) => ({ value: String(i + 1), label: `Tiết ${i + 1}` }));

const schema = z.object({
  teachingAssignmentId: z.string().min(1, 'Vui lòng chọn phân công giảng dạy'),
  dayOfWeek: z.string().min(1, 'Vui lòng chọn thứ'),
  period: z.string().min(1, 'Vui lòng chọn tiết'),
  room: z.string().min(1, 'Vui lòng nhập phòng học'),
});

/**
 * open, onOpenChange, assignmentOptions ({value,label}[] - the class's own
 * teaching assignments, "Môn - Giáo viên"), slot: null (create) | TimetableSlotDTO
 * (edit), presetDayOfWeek/presetPeriod: pre-fill for a new slot created by
 * clicking an empty grid cell.
 */
function TimetableSlotFormDialog({
  open,
  onOpenChange,
  assignmentOptions,
  slot,
  presetDayOfWeek,
  presetPeriod,
}) {
  const isEdit = Boolean(slot);
  const queryClient = useQueryClient();

  const form = useAppForm({
    schema,
    defaultValues: {
      teachingAssignmentId: slot?.teachingAssignmentId ? String(slot.teachingAssignmentId) : '',
      dayOfWeek: slot?.dayOfWeek ? String(slot.dayOfWeek) : presetDayOfWeek ? String(presetDayOfWeek) : '',
      period: slot?.period ? String(slot.period) : presetPeriod ? String(presetPeriod) : '',
      room: slot?.room ?? '',
    },
  });

  const mutation = useMutation({
    mutationFn: (values) => {
      const payload = {
        teachingAssignment: { id: Number(values.teachingAssignmentId) },
        dayOfWeek: Number(values.dayOfWeek),
        period: Number(values.period),
        room: values.room.trim(),
      };
      return isEdit ? timetableService.updateSlot(slot.id, payload) : timetableService.createSlot(payload);
    },
    onSuccess: () => {
      toast.success(isEdit ? 'Đã cập nhật tiết học' : 'Đã thêm tiết học');
      queryClient.invalidateQueries({ queryKey: ['timetable'] });
      onOpenChange(false);
    },
    onError: (err) => {
      // Backend refuses with 409 + a specific reason when the teacher, room,
      // or class already has a slot at that day/period this semester
      // (TimetableService.assertNoConflict) - surface it verbatim rather
      // than a generic failure message.
      toast.error(err?.response?.data?.message || err?.message || 'Không thể lưu tiết học');
    },
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Sửa tiết học' : 'Thêm tiết học'}</DialogTitle>
          <DialogDescription>Chọn phân công giảng dạy, thứ, tiết và phòng học</DialogDescription>
        </DialogHeader>
        <AppForm form={form} onSubmit={(v) => mutation.mutate(v)} className="space-y-4" id="timetable-slot-form">
          <SelectField
            control={form.control}
            name="teachingAssignmentId"
            label="Môn học - Giáo viên"
            options={assignmentOptions}
            placeholder={assignmentOptions.length ? 'Chọn phân công giảng dạy' : 'Lớp chưa có phân công giảng dạy'}
          />
          <div className="grid grid-cols-2 gap-4">
            <SelectField control={form.control} name="dayOfWeek" label="Thứ" options={dayOptions} />
            <SelectField control={form.control} name="period" label="Tiết" options={periodOptions} />
          </div>
          <TextField control={form.control} name="room" label="Phòng học" placeholder="P.101" />
          <DialogFooter className="pt-2">
            <Button type="submit" form="timetable-slot-form" disabled={mutation.isPending}>
              {mutation.isPending ? 'Đang lưu...' : isEdit ? 'Lưu thay đổi' : 'Thêm tiết học'}
            </Button>
          </DialogFooter>
        </AppForm>
      </DialogContent>
    </Dialog>
  );
}

export default TimetableSlotFormDialog;
