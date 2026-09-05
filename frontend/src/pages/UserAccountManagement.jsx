import React, { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { FiLock, FiUnlock } from 'react-icons/fi';
import { userService } from '@/services/dataService';
import { getCurrentUser } from '@/services/authService';
import DataTable from '../components/shared/DataTable';
import { Button } from '../components/ui/button';
import { Badge } from '../components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
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
import { ROLE_LABELS } from '@/lib/enumLabels';

// D6 (KE_HOACH_NANG_CAP_V4.md, Giai đoạn D) - trang quản trị tài khoản
// ADMIN: xem mọi tài khoản (mọi vai trò) + khoá/mở đăng nhập. Tách khỏi
// StaffManagement/ParentManagement (nơi tạo tài khoản) và GET /v1/users?role=
// (dùng bởi ParentManagement.jsx cho dropdown chọn phụ huynh, trả mảng
// phẳng không phân trang - không đụng vào endpoint đó).

const PAGE_SIZE_OPTIONS = [10, 20, 50, 100];
const ALL_ROLES = 'ALL'; // Radix <SelectItem> không cho phép value="" nên dùng sentinel này cho "Tất cả vai trò".

function UserAccountManagement() {
  const queryClient = useQueryClient();
  const currentUserId = getCurrentUser()?.userId;

  const [role, setRole] = useState(ALL_ROLES);
  const [q, setQ] = useState('');
  const [pageIndex, setPageIndex] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [lockingUser, setLockingUser] = useState(null);

  const usersQuery = useQuery({
    queryKey: ['users-accounts', role, q, pageIndex, pageSize],
    queryFn: () =>
      userService
        .search({ role: role === ALL_ROLES ? undefined : role, q: q.trim() || undefined, page: pageIndex, size: pageSize })
        .then((r) => r.data),
  });

  const setEnabledMutation = useMutation({
    mutationFn: ({ id, enabled }) => userService.setEnabled(id, enabled),
    onSuccess: (_res, { enabled }) => {
      toast.success(enabled ? 'Đã mở khoá tài khoản' : 'Đã khoá tài khoản');
      queryClient.invalidateQueries({ queryKey: ['users-accounts'] });
    },
    onError: (err) => {
      toast.error(err?.response?.data?.message || err?.message || 'Không thể cập nhật trạng thái tài khoản');
    },
    onSettled: () => setLockingUser(null),
  });

  const columns = useMemo(
    () => [
      { accessorKey: 'username', header: 'Tên đăng nhập' },
      {
        id: 'fullName',
        header: 'Họ tên',
        cell: ({ row }) => `${row.original.firstName ?? ''} ${row.original.lastName ?? ''}`.trim() || '—',
      },
      { accessorKey: 'email', header: 'Email' },
      {
        id: 'role',
        header: 'Vai trò',
        cell: ({ row }) => <Badge variant="secondary">{ROLE_LABELS[row.original.role] ?? row.original.role}</Badge>,
      },
      {
        id: 'enabled',
        header: 'Trạng thái',
        cell: ({ row }) =>
          row.original.enabled ? (
            <Badge variant="default">Đang hoạt động</Badge>
          ) : (
            <Badge variant="destructive">Đã khoá</Badge>
          ),
      },
      {
        id: 'actions',
        header: '',
        cell: ({ row }) => {
          const isSelf = row.original.id === currentUserId;
          return row.original.enabled ? (
            <Button
              variant="outline"
              size="sm"
              disabled={isSelf}
              title={isSelf ? 'Không thể tự khoá tài khoản của chính mình' : undefined}
              onClick={() => setLockingUser(row.original)}
            >
              <FiLock className="mr-1.5 h-3.5 w-3.5" /> Khoá
            </Button>
          ) : (
            <Button
              variant="outline"
              size="sm"
              disabled={setEnabledMutation.isPending}
              onClick={() => setEnabledMutation.mutate({ id: row.original.id, enabled: true })}
            >
              <FiUnlock className="mr-1.5 h-3.5 w-3.5" /> Mở khoá
            </Button>
          );
        },
      },
    ],
    [currentUserId, setEnabledMutation]
  );

  const page = usersQuery.data;

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold">Quản lý tài khoản</h1>
        <p className="text-sm text-muted-foreground">Xem tài khoản mọi vai trò và khoá/mở đăng nhập khi cần</p>
      </div>

      {usersQuery.isError && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive dark:text-red-400">
          Không tải được danh sách tài khoản.
        </div>
      )}

      <div className="max-w-xs space-y-1.5">
        <label className="text-sm font-medium" htmlFor="account-role-filter">
          Vai trò
        </label>
        <Select
          value={role}
          onValueChange={(v) => {
            setRole(v);
            setPageIndex(0);
          }}
        >
          <SelectTrigger id="account-role-filter">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={ALL_ROLES}>Tất cả vai trò</SelectItem>
            {Object.entries(ROLE_LABELS).map(([value, label]) => (
              <SelectItem key={value} value={value}>
                {label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <DataTable
        columns={columns}
        data={page?.content ?? []}
        isLoading={usersQuery.isLoading}
        emptyMessage="Không có tài khoản nào."
        pageIndex={pageIndex}
        pageCount={page?.totalPages ?? 0}
        pageSize={pageSize}
        totalCount={page?.totalElements}
        onPageChange={setPageIndex}
        onPageSizeChange={(s) => {
          setPageSize(s);
          setPageIndex(0);
        }}
        pageSizeOptions={PAGE_SIZE_OPTIONS}
        searchValue={q}
        onSearchChange={(v) => {
          setQ(v);
          setPageIndex(0);
        }}
        searchPlaceholder="Tìm theo tên đăng nhập, họ tên, email..."
      />

      <AlertDialog open={Boolean(lockingUser)} onOpenChange={(open) => !open && setLockingUser(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Khoá tài khoản?</AlertDialogTitle>
            <AlertDialogDescription>
              {lockingUser?.username} ({lockingUser?.firstName} {lockingUser?.lastName}) sẽ không thể đăng nhập nữa cho
              đến khi được mở khoá lại.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Huỷ</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={setEnabledMutation.isPending}
              onClick={() => setEnabledMutation.mutate({ id: lockingUser.id, enabled: false })}
            >
              {setEnabledMutation.isPending ? 'Đang khoá...' : 'Khoá'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

export default UserAccountManagement;
