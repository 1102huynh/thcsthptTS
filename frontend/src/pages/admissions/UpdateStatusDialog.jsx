import React, { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { admissionService } from '@/services/dataService';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { toOptions, ADMISSION_STATUS_LABELS } from '@/lib/enumLabels';

const statusOptions = toOptions(ADMISSION_STATUS_LABELS);

/** open, onOpenChange, application: AdmissionApplicationDTO */
function UpdateStatusDialog({ open, onOpenChange, application }) {
  const queryClient = useQueryClient();
  const [status, setStatus] = useState(application?.status ?? 'PENDING');
  const [note, setNote] = useState(application?.note ?? '');

  // Re-seed whenever a different application is opened (dialog stays
  // mounted between opens via the parent's `key`, same reasoning as every
  // other FormDialog in this app that reads its target prop into state).
  React.useEffect(() => {
    if (application) {
      setStatus(application.status);
      setNote(application.note ?? '');
    }
  }, [application]);

  const mutation = useMutation({
    mutationFn: () => admissionService.updateStatus(application.id, { status, note: note || null }),
    onSuccess: () => {
      toast.success('Đã cập nhật trạng thái hồ sơ');
      queryClient.invalidateQueries({ queryKey: ['admissions'] });
      onOpenChange(false);
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể cập nhật trạng thái'),
  });

  if (!application) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Cập nhật trạng thái hồ sơ</DialogTitle>
          <DialogDescription>{application.applicantName} - khối {application.desiredGradeLevel}</DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-1.5">
            <label htmlFor="admission-status-select" className="text-sm font-medium">Trạng thái</label>
            <Select value={status} onValueChange={setStatus}>
              <SelectTrigger id="admission-status-select">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {statusOptions.map((o) => (
                  <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1.5">
            <label htmlFor="admission-note" className="text-sm font-medium">Ghi chú (không bắt buộc)</label>
            <Textarea
              id="admission-note"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              rows={3}
              placeholder="VD: Thiếu giấy khai sinh"
            />
          </div>
          <DialogFooter>
            <Button onClick={() => mutation.mutate()} disabled={mutation.isPending}>
              {mutation.isPending ? 'Đang lưu...' : 'Cập nhật'}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  );
}

export default UpdateStatusDialog;
