import React, { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { FiEdit2, FiTrash2, FiPlus, FiDollarSign, FiDownload } from 'react-icons/fi';
import { feeService, academicYearService, reportService } from '../services/dataService';
import { triggerBlobDownload } from '../lib/download';
import { getCurrentUser } from '../services/authService';
import DataTable from '../components/shared/DataTable';
import FeeFormDialog from './fee/FeeFormDialog';
import PaymentDialog from './fee/PaymentDialog';
import { Card, CardContent } from '../components/ui/card';
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
import { FEE_STATUS_LABELS } from '../lib/enumLabels';

const PAGE_SIZE_OPTIONS = [10, 20, 50];
const CLOSED_STATUSES = new Set(['PAID', 'EXEMPTED', 'CANCELLED']);

function currencyVND(n) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(n ?? 0);
}

function FeeManagement() {
  const queryClient = useQueryClient();
  // Mức 2.1 (v4.9): PRINCIPAL reaches this page read-only - fee GETs now
  // allow PRINCIPAL for oversight, but create/update/delete/record-payment
  // stay ADMIN/ACCOUNTANT, so those controls are hidden for this role.
  const readOnly = getCurrentUser()?.role === 'PRINCIPAL';
  const [search, setSearch] = useState('');
  const [pageIndex, setPageIndex] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingFee, setEditingFee] = useState(null);
  const [payingFee, setPayingFee] = useState(null);
  const [deletingFee, setDeletingFee] = useState(null);

  const academicYearsQuery = useQuery({
    queryKey: ['academic-years'],
    queryFn: () => academicYearService.getAll().then((r) => r.data),
  });
  const academicYear = academicYearsQuery.data?.find((y) => y.status === 'ACTIVE')?.name;

  const feesQuery = useQuery({
    queryKey: ['fees', academicYear],
    queryFn: () => feeService.getByYear(academicYear).then((r) => r.data),
    enabled: Boolean(academicYear),
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => feeService.deleteFee(id),
    onSuccess: () => {
      toast.success('Đã xóa khoản thu');
      queryClient.invalidateQueries({ queryKey: ['fees'] });
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể xóa khoản thu'),
    onSettled: () => setDeletingFee(null),
  });

  const downloadReceiptMutation = useMutation({
    mutationFn: (fee) =>
      triggerBlobDownload(reportService.feeReceipt(fee.id), `bien-lai-${fee.studentName?.replace(/\s+/g, '-') ?? fee.id}-${fee.id}.pdf`),
    onError: (err) => toast.error(err.message || 'Không thể tải biên lai'),
  });

  const allFees = feesQuery.data ?? [];
  const summary = useMemo(() => {
    const totalCollected = allFees.reduce((sum, f) => sum + (f.paidAmount ?? 0), 0);
    const totalOutstanding = allFees
      .filter((f) => !CLOSED_STATUSES.has(f.status))
      .reduce((sum, f) => sum + (f.remainingAmount ?? f.amount ?? 0), 0);
    return { totalCollected, totalOutstanding };
  }, [allFees]);

  const filtered = useMemo(() => {
    if (!search.trim()) return allFees;
    const q = search.trim().toLowerCase();
    return allFees.filter(
      (f) => f.studentName?.toLowerCase().includes(q) || f.feeType?.toLowerCase().includes(q)
    );
  }, [allFees, search]);

  const pageCount = Math.max(1, Math.ceil(filtered.length / pageSize));
  const pageRows = filtered.slice(pageIndex * pageSize, (pageIndex + 1) * pageSize);

  const openCreate = () => { setEditingFee(null); setDialogOpen(true); };
  const openEdit = (fee) => { setEditingFee(fee); setDialogOpen(true); };

  const columns = useMemo(
    () => [
      { accessorKey: 'studentName', header: 'Học sinh' },
      { accessorKey: 'feeType', header: 'Loại phí' },
      { accessorKey: 'amount', header: 'Số tiền', cell: ({ getValue }) => currencyVND(getValue()) },
      { accessorKey: 'remainingAmount', header: 'Còn nợ', cell: ({ getValue }) => currencyVND(getValue()) },
      { accessorKey: 'dueDate', header: 'Hạn nộp', cell: ({ getValue }) => getValue() || '—' },
      {
        accessorKey: 'status',
        header: 'Trạng thái',
        cell: ({ getValue }) => {
          const status = getValue();
          const variant = status === 'PAID' ? 'default' : status === 'OVERDUE' ? 'destructive' : 'secondary';
          return <Badge variant={variant}>{FEE_STATUS_LABELS[status] ?? status}</Badge>;
        },
      },
      {
        id: 'actions',
        header: '',
        cell: ({ row }) => {
          const fee = row.original;
          const canPay = !CLOSED_STATUSES.has(fee.status);
          return (
            <div className="flex justify-end gap-2">
              {canPay && !readOnly && (
                <Button variant="ghost" size="icon" onClick={() => setPayingFee(fee)} aria-label="Ghi nhận thanh toán">
                  <FiDollarSign className="h-4 w-4" />
                </Button>
              )}
              {fee.paidAmount > 0 && (
                <Button
                  variant="ghost"
                  size="icon"
                  onClick={() => downloadReceiptMutation.mutate(fee)}
                  disabled={downloadReceiptMutation.isPending}
                  aria-label="Tải biên lai"
                >
                  <FiDownload className="h-4 w-4" />
                </Button>
              )}
              {!readOnly && (
                <>
                  <Button variant="ghost" size="icon" onClick={() => openEdit(fee)} aria-label="Sửa">
                    <FiEdit2 className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon"
                    className="text-destructive hover:text-destructive"
                    onClick={() => setDeletingFee(fee)}
                    aria-label="Xóa"
                  >
                    <FiTrash2 className="h-4 w-4" />
                  </Button>
                </>
              )}
            </div>
          );
        },
      },
    ],
    [readOnly]
  );

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Học phí</h1>
          <p className="text-sm text-muted-foreground">Danh sách khoản thu, thanh toán và công nợ</p>
        </div>
        {!readOnly && (
          <Button onClick={openCreate} disabled={!academicYear}>
            <FiPlus className="mr-2 h-4 w-4" /> Thêm khoản thu
          </Button>
        )}
      </div>

      {readOnly && (
        <div className="rounded-lg border bg-muted/50 px-4 py-2 text-sm text-muted-foreground">
          Chế độ chỉ xem (Hiệu trưởng) — việc lập khoản thu và ghi nhận thanh toán do kế toán thực hiện.
        </div>
      )}

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Card>
          <CardContent className="p-5">
            <p className="text-sm text-muted-foreground">Tổng đã thu</p>
            <p className="text-2xl font-semibold tabular-nums text-green-600">{currencyVND(summary.totalCollected)}</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-5">
            <p className="text-sm text-muted-foreground">Tổng công nợ</p>
            <p className="text-2xl font-semibold tabular-nums text-rose-600">{currencyVND(summary.totalOutstanding)}</p>
          </CardContent>
        </Card>
      </div>

      {!academicYear && academicYearsQuery.isSuccess && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive dark:text-red-400">
          Chưa thiết lập năm học đang hoạt động (ACTIVE) trong hệ thống.
        </div>
      )}
      {feesQuery.isError && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive dark:text-red-400">
          Không tải được danh sách khoản thu.
        </div>
      )}

      <DataTable
        columns={columns}
        data={pageRows}
        isLoading={feesQuery.isLoading}
        emptyMessage="Không tìm thấy khoản thu nào."
        pageIndex={pageIndex}
        pageCount={pageCount}
        pageSize={pageSize}
        totalCount={filtered.length}
        onPageChange={setPageIndex}
        onPageSizeChange={(s) => { setPageSize(s); setPageIndex(0); }}
        pageSizeOptions={PAGE_SIZE_OPTIONS}
        searchValue={search}
        onSearchChange={(v) => { setSearch(v); setPageIndex(0); }}
        searchPlaceholder="Tìm theo tên học sinh, loại phí..."
      />

      {/* key forces a remount when the target record changes - see
          LibraryManagement's BookFormDialog comment for why. Prefixed
          ("edit-"/"pay-") because these are two *different* sibling
          components in the same parent - React's duplicate-key check
          applies across all children of a parent regardless of element
          type, and editingFee is never reset to null after its dialog
          closes, so editing fee #248 then opening the payment dialog for
          that same #248 produced two siblings both keyed "248". Caught
          live via a real "Encountered two children with the same key"
          console warning, not by inspection. */}
      <FeeFormDialog
        key={`edit-${editingFee?.id ?? 'create'}`}
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        fee={editingFee}
        academicYear={academicYear}
      />
      <PaymentDialog
        key={`pay-${payingFee?.id ?? 'none'}`}
        open={Boolean(payingFee)}
        onOpenChange={(open) => !open && setPayingFee(null)}
        fee={payingFee}
      />

      <AlertDialog open={Boolean(deletingFee)} onOpenChange={(open) => !open && setDeletingFee(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xóa khoản thu?</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn sắp xóa "{deletingFee?.feeType}" của {deletingFee?.studentName}. Hành động này không thể hoàn tác.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Hủy</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={deleteMutation.isPending}
              onClick={() => deleteMutation.mutate(deletingFee.id)}
            >
              {deleteMutation.isPending ? 'Đang xóa...' : 'Xóa'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

export default FeeManagement;
