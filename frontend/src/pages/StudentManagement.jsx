import React, { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { FiEdit2, FiTrash2, FiPlus } from 'react-icons/fi';
import { studentService } from '../services/dataService';
import DataTable from '../components/shared/DataTable';
import StudentFormDialog from './student/StudentFormDialog';
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
import { STUDENT_STATUS_LABELS } from '../lib/enumLabels';

const PAGE_SIZE_OPTIONS = [10, 20, 50];

function StudentManagement() {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState('');
  const [pageIndex, setPageIndex] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingStudent, setEditingStudent] = useState(null);
  const [deletingStudent, setDeletingStudent] = useState(null);

  const studentsQuery = useQuery({
    queryKey: ['students'],
    queryFn: () => studentService.getAll().then((r) => r.data),
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => studentService.delete(id),
    onSuccess: () => {
      toast.success('Đã xóa học sinh');
      queryClient.invalidateQueries({ queryKey: ['students'] });
    },
    onError: (err) => {
      toast.error(err?.response?.data?.message || err?.message || 'Không thể xóa học sinh');
    },
    onSettled: () => setDeletingStudent(null),
  });

  const filtered = useMemo(() => {
    const rows = studentsQuery.data ?? [];
    if (!search.trim()) return rows;
    const q = search.trim().toLowerCase();
    return rows.filter((s) => {
      const name = `${s.user?.firstName ?? ''} ${s.user?.lastName ?? ''}`.toLowerCase();
      return (
        name.includes(q) ||
        s.rollNumber?.toLowerCase().includes(q) ||
        s.admissionNumber?.toLowerCase().includes(q) ||
        s.user?.email?.toLowerCase().includes(q) ||
        s.className?.toLowerCase().includes(q)
      );
    });
  }, [studentsQuery.data, search]);

  const pageCount = Math.max(1, Math.ceil(filtered.length / pageSize));
  const pageRows = filtered.slice(pageIndex * pageSize, (pageIndex + 1) * pageSize);

  const openCreate = () => {
    setEditingStudent(null);
    setDialogOpen(true);
  };
  const openEdit = (student) => {
    setEditingStudent(student);
    setDialogOpen(true);
  };

  const columns = useMemo(
    () => [
      { accessorKey: 'rollNumber', header: 'Số báo danh' },
      {
        id: 'name',
        header: 'Họ tên',
        accessorFn: (row) => `${row.user?.firstName ?? ''} ${row.user?.lastName ?? ''}`.trim(),
      },
      {
        id: 'class',
        header: 'Lớp',
        accessorFn: (row) => [row.className, row.section].filter(Boolean).join(' - ') || '—',
      },
      { id: 'email', header: 'Email', accessorFn: (row) => row.user?.email ?? '—' },
      {
        accessorKey: 'status',
        header: 'Trạng thái',
        cell: ({ getValue }) => {
          const status = getValue();
          return (
            <Badge variant={status === 'ACTIVE' ? 'default' : 'secondary'}>
              {STUDENT_STATUS_LABELS[status] ?? status}
            </Badge>
          );
        },
      },
      {
        id: 'actions',
        header: '',
        cell: ({ row }) => (
          <div className="flex justify-end gap-2">
            <Button variant="ghost" size="icon" onClick={() => openEdit(row.original)} aria-label="Sửa">
              <FiEdit2 className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              className="text-destructive hover:text-destructive"
              onClick={() => setDeletingStudent(row.original)}
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
          <h1 className="text-2xl font-semibold">Quản lý học sinh</h1>
          <p className="text-sm text-muted-foreground">Danh sách học sinh trong trường</p>
        </div>
        <Button onClick={openCreate}>
          <FiPlus className="mr-2 h-4 w-4" /> Thêm học sinh
        </Button>
      </div>

      {studentsQuery.isError && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive dark:text-red-400">
          Không tải được danh sách học sinh.
        </div>
      )}

      <DataTable
        columns={columns}
        data={pageRows}
        isLoading={studentsQuery.isLoading}
        emptyMessage="Không tìm thấy học sinh nào."
        pageIndex={pageIndex}
        pageCount={pageCount}
        pageSize={pageSize}
        totalCount={filtered.length}
        onPageChange={setPageIndex}
        onPageSizeChange={(s) => {
          setPageSize(s);
          setPageIndex(0);
        }}
        pageSizeOptions={PAGE_SIZE_OPTIONS}
        searchValue={search}
        onSearchChange={(v) => {
          setSearch(v);
          setPageIndex(0);
        }}
        searchPlaceholder="Tìm theo tên, số báo danh, email, lớp..."
      />

      {/* key forces a remount whenever the target record changes - see
          LibraryManagement's BookFormDialog comment for why. */}
      <StudentFormDialog
        key={editingStudent?.id ?? 'create'}
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        student={editingStudent}
      />

      <AlertDialog open={Boolean(deletingStudent)} onOpenChange={(open) => !open && setDeletingStudent(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xóa học sinh?</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn sắp xóa {deletingStudent?.user?.firstName} {deletingStudent?.user?.lastName} (
              {deletingStudent?.rollNumber}). Hành động này không thể hoàn tác.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Hủy</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={deleteMutation.isPending}
              onClick={() => deleteMutation.mutate(deletingStudent.id)}
            >
              {deleteMutation.isPending ? 'Đang xóa...' : 'Xóa'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

export default StudentManagement;
