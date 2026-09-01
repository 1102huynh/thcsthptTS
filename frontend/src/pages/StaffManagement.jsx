import React, { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { FiEdit2, FiTrash2, FiPlus } from 'react-icons/fi';
import { staffService } from '../services/dataService';
import DataTable from '../components/shared/DataTable';
import StaffFormDialog from './staff/StaffFormDialog';
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
import { STAFF_POSITION_LABELS, EMPLOYMENT_STATUS_LABELS } from '../lib/enumLabels';

const PAGE_SIZE_OPTIONS = [10, 20, 50];

function StaffManagement() {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState('');
  const [pageIndex, setPageIndex] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingStaff, setEditingStaff] = useState(null);
  const [deletingStaff, setDeletingStaff] = useState(null);

  const staffQuery = useQuery({
    queryKey: ['staff'],
    queryFn: () => staffService.getAll().then((r) => r.data),
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => staffService.delete(id),
    onSuccess: () => {
      toast.success('Đã xóa nhân viên');
      queryClient.invalidateQueries({ queryKey: ['staff'] });
    },
    onError: (err) => {
      toast.error(err?.response?.data?.message || err?.message || 'Không thể xóa nhân viên');
    },
    onSettled: () => setDeletingStaff(null),
  });

  const filtered = useMemo(() => {
    const rows = staffQuery.data ?? [];
    if (!search.trim()) return rows;
    const q = search.trim().toLowerCase();
    return rows.filter((s) => {
      const name = `${s.user?.firstName ?? ''} ${s.user?.lastName ?? ''}`.toLowerCase();
      return (
        name.includes(q) ||
        s.employeeId?.toLowerCase().includes(q) ||
        s.user?.email?.toLowerCase().includes(q) ||
        s.department?.toLowerCase().includes(q)
      );
    });
  }, [staffQuery.data, search]);

  const pageCount = Math.max(1, Math.ceil(filtered.length / pageSize));
  const pageRows = filtered.slice(pageIndex * pageSize, (pageIndex + 1) * pageSize);

  const openCreate = () => {
    setEditingStaff(null);
    setDialogOpen(true);
  };
  const openEdit = (staff) => {
    setEditingStaff(staff);
    setDialogOpen(true);
  };

  const columns = useMemo(
    () => [
      { accessorKey: 'employeeId', header: 'Mã NV' },
      {
        id: 'name',
        header: 'Họ tên',
        accessorFn: (row) => `${row.user?.firstName ?? ''} ${row.user?.lastName ?? ''}`.trim(),
      },
      {
        accessorKey: 'position',
        header: 'Chức vụ',
        cell: ({ getValue }) => STAFF_POSITION_LABELS[getValue()] ?? getValue(),
      },
      { accessorKey: 'department', header: 'Phòng ban', cell: ({ getValue }) => getValue() || '—' },
      { id: 'email', header: 'Email', accessorFn: (row) => row.user?.email ?? '—' },
      {
        accessorKey: 'status',
        header: 'Trạng thái',
        cell: ({ getValue }) => {
          const status = getValue();
          return (
            <Badge variant={status === 'ACTIVE' ? 'default' : 'secondary'}>
              {EMPLOYMENT_STATUS_LABELS[status] ?? status}
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
              onClick={() => setDeletingStaff(row.original)}
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
          <h1 className="text-2xl font-semibold">Quản lý nhân viên</h1>
          <p className="text-sm text-muted-foreground">Danh sách nhân viên, giáo viên trong trường</p>
        </div>
        <Button onClick={openCreate}>
          <FiPlus className="mr-2 h-4 w-4" /> Thêm nhân viên
        </Button>
      </div>

      {staffQuery.isError && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">
          Không tải được danh sách nhân viên.
        </div>
      )}

      <DataTable
        columns={columns}
        data={pageRows}
        isLoading={staffQuery.isLoading}
        emptyMessage="Không tìm thấy nhân viên nào."
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
        searchPlaceholder="Tìm theo tên, mã NV, email, phòng ban..."
      />

      <StaffFormDialog open={dialogOpen} onOpenChange={setDialogOpen} staff={editingStaff} />

      <AlertDialog open={Boolean(deletingStaff)} onOpenChange={(open) => !open && setDeletingStaff(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xóa nhân viên?</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn sắp xóa {deletingStaff?.user?.firstName} {deletingStaff?.user?.lastName} ({deletingStaff?.employeeId}).
              Hành động này không thể hoàn tác.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Hủy</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={deleteMutation.isPending}
              onClick={() => deleteMutation.mutate(deletingStaff.id)}
            >
              {deleteMutation.isPending ? 'Đang xóa...' : 'Xóa'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

export default StaffManagement;
