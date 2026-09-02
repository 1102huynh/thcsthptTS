import React, { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { FiPlus, FiTrash2, FiUserPlus } from 'react-icons/fi';
import { userService, parentService } from '../services/dataService';
import CreateParentDialog from './parents/CreateParentDialog';
import AddChildDialog from './parents/AddChildDialog';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Badge } from '../components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import { PARENT_RELATIONSHIP_LABELS } from '../lib/enumLabels';
import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogCancel,
  AlertDialogAction,
} from '../components/ui/alert-dialog';

/**
 * ADMIN-only phụ huynh/con management (Giai đoạn 3.6). ParentController has
 * no "list all relations" endpoint (only per-parent getChildren) - the page
 * is built around "pick one parent, manage their children", not a flat
 * table of every relation in the school.
 */
function ParentManagement() {
  const queryClient = useQueryClient();
  const [parentId, setParentId] = useState('');
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [addChildOpen, setAddChildOpen] = useState(false);
  const [unlinking, setUnlinking] = useState(null);

  const parentsQuery = useQuery({ queryKey: ['users-parent'], queryFn: () => userService.getByRole('PARENT').then((r) => r.data) });
  const selectedParent = parentsQuery.data?.find((p) => String(p.id) === parentId);

  const childrenQuery = useQuery({
    queryKey: ['parent-children', parentId],
    queryFn: () => parentService.getChildren(parentId).then((r) => r.data),
    enabled: Boolean(parentId),
  });

  const unlinkMutation = useMutation({
    mutationFn: (studentId) => parentService.unlinkChild(parentId, studentId),
    onSuccess: () => {
      toast.success('Đã bỏ liên kết học sinh');
      queryClient.invalidateQueries({ queryKey: ['parent-children', parentId] });
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể bỏ liên kết'),
    onSettled: () => setUnlinking(null),
  });

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Phụ huynh</h1>
          <p className="text-sm text-muted-foreground">Liên kết tài khoản phụ huynh với con của họ - dùng cho sổ liên lạc điện tử</p>
        </div>
        <Button onClick={() => setCreateDialogOpen(true)}>
          <FiPlus className="mr-2 h-4 w-4" /> Tạo tài khoản phụ huynh
        </Button>
      </div>

      <Card>
        <CardContent className="p-5">
          <div className="max-w-sm space-y-1.5">
            <label htmlFor="parent-select" className="text-sm font-medium">Chọn phụ huynh</label>
            <Select value={parentId} onValueChange={setParentId}>
              <SelectTrigger id="parent-select">
                <SelectValue placeholder={parentsQuery.isLoading ? 'Đang tải...' : 'Chọn tài khoản phụ huynh'} />
              </SelectTrigger>
              <SelectContent>
                {(parentsQuery.data ?? []).map((p) => (
                  <SelectItem key={p.id} value={String(p.id)}>{p.firstName} {p.lastName} ({p.email})</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          {parentsQuery.isSuccess && parentsQuery.data.length === 0 && (
            <p className="mt-3 text-sm text-muted-foreground">Chưa có tài khoản phụ huynh nào - tạo một tài khoản để bắt đầu.</p>
          )}
        </CardContent>
      </Card>

      {selectedParent && (
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0">
            <div>
              <CardTitle>Con của {selectedParent.firstName} {selectedParent.lastName}</CardTitle>
              <CardDescription>{selectedParent.email}</CardDescription>
            </div>
            <Button onClick={() => setAddChildOpen(true)}>
              <FiUserPlus className="mr-2 h-4 w-4" /> Thêm con
            </Button>
          </CardHeader>
          <CardContent>
            {childrenQuery.isLoading ? (
              <p className="py-4 text-sm text-muted-foreground">Đang tải...</p>
            ) : (childrenQuery.data ?? []).length === 0 ? (
              <p className="py-8 text-center text-sm text-muted-foreground">Chưa liên kết con nào.</p>
            ) : (
              <div className="divide-y rounded-md border">
                {childrenQuery.data.map((rel) => (
                  <div key={rel.id} className="flex items-center justify-between p-3">
                    <div>
                      <p className="font-medium">
                        {rel.studentName} <span className="text-muted-foreground">({rel.rollNumber})</span>
                      </p>
                      <div className="mt-1 flex items-center gap-2">
                        <Badge variant="secondary">{PARENT_RELATIONSHIP_LABELS[rel.relationship] ?? rel.relationship}</Badge>
                        {rel.isPrimaryContact && <Badge>Liên hệ chính</Badge>}
                      </div>
                    </div>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="text-destructive hover:text-destructive"
                      onClick={() => setUnlinking(rel)}
                      aria-label={`Bỏ liên kết ${rel.studentName}`}
                    >
                      <FiTrash2 className="h-4 w-4" />
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      )}

      <CreateParentDialog
        open={createDialogOpen}
        onOpenChange={setCreateDialogOpen}
        onCreated={(userId) => setParentId(String(userId))}
      />
      <AddChildDialog open={addChildOpen} onOpenChange={setAddChildOpen} parentId={parentId} />

      <AlertDialog open={Boolean(unlinking)} onOpenChange={(open) => !open && setUnlinking(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Bỏ liên kết học sinh?</AlertDialogTitle>
            <AlertDialogDescription>
              {selectedParent?.firstName} {selectedParent?.lastName} sẽ không còn xem được thông tin của {unlinking?.studentName} nữa.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Hủy</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={unlinkMutation.isPending}
              onClick={() => unlinkMutation.mutate(unlinking.studentId)}
            >
              {unlinkMutation.isPending ? 'Đang xóa...' : 'Bỏ liên kết'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

export default ParentManagement;
