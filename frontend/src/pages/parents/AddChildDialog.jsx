import React, { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { studentService, parentService } from '@/services/dataService';
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
import { toOptions, PARENT_RELATIONSHIP_LABELS } from '@/lib/enumLabels';

const relationshipOptions = toOptions(PARENT_RELATIONSHIP_LABELS);

/** open, onOpenChange, parentId */
function AddChildDialog({ open, onOpenChange, parentId }) {
  const queryClient = useQueryClient();
  const [studentId, setStudentId] = useState('');
  const [relationship, setRelationship] = useState('CHA');
  const [isPrimaryContact, setIsPrimaryContact] = useState(true);

  const studentsQuery = useQuery({
    queryKey: ['students'],
    queryFn: () => studentService.getAll().then((r) => r.data),
    enabled: open,
  });

  const mutation = useMutation({
    mutationFn: () => parentService.linkChild(parentId, Number(studentId), relationship, isPrimaryContact),
    onSuccess: () => {
      toast.success('Đã liên kết học sinh với phụ huynh');
      queryClient.invalidateQueries({ queryKey: ['parent-children', parentId] });
      onOpenChange(false);
      setStudentId('');
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể liên kết học sinh'),
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Thêm con</DialogTitle>
          <DialogDescription>Liên kết một học sinh với tài khoản phụ huynh này</DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-1.5">
            <label htmlFor="add-child-student-select" className="text-sm font-medium">Học sinh</label>
            <Select value={studentId} onValueChange={setStudentId}>
              <SelectTrigger id="add-child-student-select">
                <SelectValue placeholder={studentsQuery.isLoading ? 'Đang tải...' : 'Chọn học sinh'} />
              </SelectTrigger>
              <SelectContent>
                {(studentsQuery.data ?? []).map((s) => (
                  <SelectItem key={s.id} value={String(s.id)}>
                    {s.rollNumber} - {s.user?.firstName} {s.user?.lastName} ({s.className}-{s.section})
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1.5">
            <label htmlFor="add-child-relationship-select" className="text-sm font-medium">Quan hệ</label>
            <Select value={relationship} onValueChange={setRelationship}>
              <SelectTrigger id="add-child-relationship-select">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {relationshipOptions.map((o) => (
                  <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <label className="flex items-center gap-2 text-sm font-medium">
            <input
              type="checkbox"
              checked={isPrimaryContact}
              onChange={(e) => setIsPrimaryContact(e.target.checked)}
              className="h-4 w-4 rounded border-input"
            />
            Là người liên hệ chính
          </label>
          <DialogFooter>
            <Button onClick={() => mutation.mutate()} disabled={!studentId || mutation.isPending}>
              {mutation.isPending ? 'Đang liên kết...' : 'Liên kết'}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  );
}

export default AddChildDialog;
