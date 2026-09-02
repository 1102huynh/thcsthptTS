import React, { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { FiEdit2, FiUserPlus, FiPaperclip } from 'react-icons/fi';
import { admissionService } from '../services/dataService';
import DataTable from '../components/shared/DataTable';
import UpdateStatusDialog from './admissions/UpdateStatusDialog';
import ApproveAndCreateDialog from './admissions/ApproveAndCreateDialog';
import DocumentsDialog from '../components/shared/DocumentsDialog';
import { Card, CardContent } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Badge } from '../components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import { toOptions, ADMISSION_STATUS_LABELS } from '../lib/enumLabels';

const statusFilterOptions = [{ value: 'ALL', label: 'Tất cả trạng thái' }, ...toOptions(ADMISSION_STATUS_LABELS)];

const STATUS_BADGE_VARIANT = {
  PENDING: 'secondary',
  REVIEWING: 'secondary',
  APPROVED: 'default',
  REJECTED: 'destructive',
};

function AdmissionManagement() {
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [editingApplication, setEditingApplication] = useState(null);
  const [approvingApplication, setApprovingApplication] = useState(null);
  const [docsForApplication, setDocsForApplication] = useState(null);

  const applicationsQuery = useQuery({
    queryKey: ['admissions', statusFilter],
    queryFn: () => admissionService.getAll(statusFilter === 'ALL' ? undefined : statusFilter).then((r) => r.data),
  });

  const columns = useMemo(
    () => [
      { accessorKey: 'applicantName', header: 'Họ tên học sinh' },
      { accessorKey: 'dateOfBirth', header: 'Ngày sinh' },
      { accessorKey: 'contactPhone', header: 'Số điện thoại' },
      { accessorKey: 'desiredGradeLevel', header: 'Khối' },
      {
        accessorKey: 'priorSchool',
        header: 'Trường cũ',
        cell: ({ getValue }) => getValue() || <span className="text-muted-foreground">—</span>,
      },
      {
        id: 'status',
        header: 'Trạng thái',
        cell: ({ row }) => (
          <Badge variant={STATUS_BADGE_VARIANT[row.original.status] ?? 'secondary'}>
            {ADMISSION_STATUS_LABELS[row.original.status] ?? row.original.status}
          </Badge>
        ),
      },
      {
        accessorKey: 'submittedAt',
        header: 'Ngày nộp',
        cell: ({ getValue }) => (getValue() ? new Date(getValue()).toLocaleDateString('vi-VN') : '—'),
      },
      {
        id: 'actions',
        header: '',
        cell: ({ row }) => {
          const app = row.original;
          return (
            <div className="flex justify-end gap-2">
              {app.status === 'APPROVED' && !app.createdStudentId && (
                <Button variant="ghost" size="icon" onClick={() => setApprovingApplication(app)} aria-label="Tạo tài khoản học sinh">
                  <FiUserPlus className="h-4 w-4" />
                </Button>
              )}
              {app.createdStudentId && (
                <Badge variant="secondary" className="mr-1">Đã tạo HS #{app.createdStudentId}</Badge>
              )}
              <Button variant="ghost" size="icon" onClick={() => setDocsForApplication(app)} aria-label="Tài liệu đính kèm">
                <FiPaperclip className="h-4 w-4" />
              </Button>
              <Button variant="ghost" size="icon" onClick={() => setEditingApplication(app)} aria-label="Cập nhật trạng thái">
                <FiEdit2 className="h-4 w-4" />
              </Button>
            </div>
          );
        },
      },
    ],
    []
  );

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold">Tuyển sinh đầu cấp</h1>
        <p className="text-sm text-muted-foreground">Hồ sơ đăng ký nộp công khai tại /apply - xét duyệt và tạo tài khoản học sinh tại đây</p>
      </div>

      <Card>
        <CardContent className="p-5">
          <div className="max-w-xs space-y-1.5">
            <label htmlFor="admission-status-filter" className="text-sm font-medium">Lọc theo trạng thái</label>
            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger id="admission-status-filter">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {statusFilterOptions.map((o) => (
                  <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {applicationsQuery.isError && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive dark:text-red-400">
          Không tải được danh sách hồ sơ tuyển sinh.
        </div>
      )}

      <DataTable
        columns={columns}
        data={applicationsQuery.data ?? []}
        isLoading={applicationsQuery.isLoading}
        emptyMessage="Chưa có hồ sơ đăng ký nào."
      />

      <UpdateStatusDialog
        key={`status-${editingApplication?.id ?? 'none'}`}
        open={Boolean(editingApplication)}
        onOpenChange={(open) => !open && setEditingApplication(null)}
        application={editingApplication}
      />
      <ApproveAndCreateDialog
        key={`approve-${approvingApplication?.id ?? 'none'}`}
        open={Boolean(approvingApplication)}
        onOpenChange={(open) => !open && setApprovingApplication(null)}
        application={approvingApplication}
      />
      <DocumentsDialog
        key={`docs-${docsForApplication?.id ?? 'none'}`}
        open={Boolean(docsForApplication)}
        onOpenChange={(open) => !open && setDocsForApplication(null)}
        ownerType="ADMISSION_APPLICATION"
        ownerId={docsForApplication?.id}
        ownerLabel={docsForApplication?.applicantName ?? ''}
      />
    </div>
  );
}

export default AdmissionManagement;
