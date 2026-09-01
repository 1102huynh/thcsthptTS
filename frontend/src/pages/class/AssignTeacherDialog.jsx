import React, { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { schoolClassService, staffService } from '@/services/dataService';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';

/** open, onOpenChange, schoolClass: SchoolClassDTO to assign a homeroom teacher to */
function AssignTeacherDialog({ open, onOpenChange, schoolClass }) {
  const queryClient = useQueryClient();
  const [staffId, setStaffId] = useState('');

  const staffQuery = useQuery({
    queryKey: ['staff'],
    queryFn: () => staffService.getAll().then((r) => r.data),
    enabled: open,
  });
  const teachers = (staffQuery.data ?? []).filter((s) => s.position === 'TEACHER');

  const mutation = useMutation({
    mutationFn: () => schoolClassService.assignTeacher(schoolClass.id, Number(staffId)),
    onSuccess: () => {
      toast.success('Đã phân công giáo viên chủ nhiệm');
      queryClient.invalidateQueries({ queryKey: ['classes'] });
      onOpenChange(false);
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể phân công giáo viên'),
  });

  if (!schoolClass) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Phân công giáo viên chủ nhiệm</DialogTitle>
          <DialogDescription>Lớp {schoolClass.className} - {schoolClass.section}</DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-1.5">
            <label htmlFor="assign-teacher-select" className="text-sm font-medium">Giáo viên</label>
            <Select value={staffId} onValueChange={setStaffId}>
              <SelectTrigger id="assign-teacher-select">
                <SelectValue placeholder={staffQuery.isLoading ? 'Đang tải...' : 'Chọn giáo viên'} />
              </SelectTrigger>
              <SelectContent>
                {teachers.map((t) => (
                  <SelectItem key={t.id} value={String(t.id)}>
                    {t.user?.firstName} {t.user?.lastName} ({t.department || 'Chưa có bộ môn'})
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <DialogFooter>
            <Button onClick={() => mutation.mutate()} disabled={!staffId || mutation.isPending}>
              {mutation.isPending ? 'Đang lưu...' : 'Phân công'}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  );
}

export default AssignTeacherDialog;
