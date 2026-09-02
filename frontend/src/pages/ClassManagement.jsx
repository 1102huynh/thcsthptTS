import React, { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { FiEdit2, FiTrash2, FiPlus, FiUserCheck } from 'react-icons/fi';
import { schoolClassService } from '../services/dataService';
import DataTable from '../components/shared/DataTable';
import ClassFormDialog from './class/ClassFormDialog';
import AssignTeacherDialog from './class/AssignTeacherDialog';
import { Button } from '../components/ui/button';
import { Badge } from '../components/ui/badge';
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

const PAGE_SIZE_OPTIONS = [10, 20, 50];

function ClassManagement() {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState('');
  const [pageIndex, setPageIndex] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingClass, setEditingClass] = useState(null);
  const [assigningClass, setAssigningClass] = useState(null);
  const [deletingClass, setDeletingClass] = useState(null);

  const classesQuery = useQuery({
    queryKey: ['classes'],
    queryFn: () => schoolClassService.getAll().then((r) => r.data),
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => schoolClassService.delete(id),
    onSuccess: () => {
      toast.success('Đã xóa lớp học');
      queryClient.invalidateQueries({ queryKey: ['classes'] });
    },
    onError: (err) => {
      // Backend refuses with 409 if students are still assigned - surface
      // that exact reason rather than a generic failure message.
      toast.error(err?.response?.data?.message || err?.message || 'Không thể xóa lớp học');
    },
    onSettled: () => setDeletingClass(null),
  });

  const allClasses = classesQuery.data ?? [];
  const filtered = useMemo(() => {
    if (!search.trim()) return allClasses;
    const q = search.trim().toLowerCase();
    return allClasses.filter(
      (c) =>
        c.className?.toLowerCase().includes(q) ||
        c.section?.toLowerCase().includes(q) ||
        c.classTeacherName?.toLowerCase().includes(q)
    );
  }, [allClasses, search]);

  const pageCount = Math.max(1, Math.ceil(filtered.length / pageSize));
  const pageRows = filtered.slice(pageIndex * pageSize, (pageIndex + 1) * pageSize);

  const openCreate = () => { setEditingClass(null); setDialogOpen(true); };
  const openEdit = (c) => { setEditingClass(c); setDialogOpen(true); };

  const columns = useMemo(
    () => [
      { id: 'name', header: 'Lớp', accessorFn: (row) => `${row.className} - ${row.section}` },
      { accessorKey: 'academicYear', header: 'Năm học' },
      {
        accessorKey: 'classTeacherName',
        header: 'GVCN',
        cell: ({ getValue }) => getValue() || <span className="text-muted-foreground">Chưa phân công</span>,
      },
      {
        id: 'studentCount',
        header: 'Sĩ số',
        cell: ({ row }) => {
          const { studentCount, capacity } = row.original;
          return (
            <Badge variant={capacity && studentCount >= capacity ? 'destructive' : 'secondary'}>
              {studentCount ?? 0}{capacity ? ` / ${capacity}` : ''}
            </Badge>
          );
        },
      },
      { accessorKey: 'roomNumber', header: 'Phòng', cell: ({ getValue }) => getValue() || '—' },
      {
        id: 'actions',
        header: '',
        cell: ({ row }) => (
          <div className="flex justify-end gap-2">
            <Button variant="ghost" size="icon" onClick={() => setAssigningClass(row.original)} aria-label="Phân công giáo viên">
              <FiUserCheck className="h-4 w-4" />
            </Button>
            <Button variant="ghost" size="icon" onClick={() => openEdit(row.original)} aria-label="Sửa">
              <FiEdit2 className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              className="text-destructive hover:text-destructive"
              onClick={() => setDeletingClass(row.original)}
              aria-label="Xóa"
            >
              <FiTrash2 className="h-4 w-4" />
            </Button>
          </div>
        ),
      },
    ],
    []
  );

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Quản lý lớp học</h1>
          <p className="text-sm text-muted-foreground">Danh sách lớp, sĩ số và giáo viên chủ nhiệm</p>
        </div>
        <Button onClick={openCreate}>
          <FiPlus className="mr-2 h-4 w-4" /> Thêm lớp học
        </Button>
      </div>

      {classesQuery.isError && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive dark:text-red-400">
          Không tải được danh sách lớp học.
        </div>
      )}

      <DataTable
        columns={columns}
        data={pageRows}
        isLoading={classesQuery.isLoading}
        emptyMessage="Không tìm thấy lớp học nào."
        pageIndex={pageIndex}
        pageCount={pageCount}
        pageSize={pageSize}
        totalCount={filtered.length}
        onPageChange={setPageIndex}
        onPageSizeChange={(s) => { setPageSize(s); setPageIndex(0); }}
        pageSizeOptions={PAGE_SIZE_OPTIONS}
        searchValue={search}
        onSearchChange={(v) => { setSearch(v); setPageIndex(0); }}
        searchPlaceholder="Tìm theo lớp, ban, giáo viên chủ nhiệm..."
      />

      {/* key forces a remount when the target record changes - see
          LibraryManagement's BookFormDialog comment for why; prefixed to
          stay unique across sibling dialogs - see FeeManagement's comment
          for why that matters. */}
      <ClassFormDialog key={`edit-${editingClass?.id ?? 'create'}`} open={dialogOpen} onOpenChange={setDialogOpen} schoolClass={editingClass} />
      <AssignTeacherDialog
        key={`assign-${assigningClass?.id ?? 'none'}`}
        open={Boolean(assigningClass)}
        onOpenChange={(open) => !open && setAssigningClass(null)}
        schoolClass={assigningClass}
      />

      <AlertDialog open={Boolean(deletingClass)} onOpenChange={(open) => !open && setDeletingClass(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xóa lớp học?</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn sắp xóa lớp {deletingClass?.className} - {deletingClass?.section}. Hành động này không thể hoàn tác.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Hủy</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={deleteMutation.isPending}
              onClick={() => deleteMutation.mutate(deletingClass.id)}
            >
              {deleteMutation.isPending ? 'Đang xóa...' : 'Xóa'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

export default ClassManagement;
