import React, { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';
import { auditLogService } from '../services/dataService';
import DataTable from '../components/shared/DataTable';
import { Badge } from '../components/ui/badge';
import { AUDIT_ACTION_LABELS } from '../lib/enumLabels';

const PAGE_SIZE_OPTIONS = [10, 20, 50, 100];

const ACTION_BADGE_VARIANT = {
  CREATE: 'default',
  UPDATE: 'secondary',
  DELETE: 'destructive',
  APPROVE: 'default',
  REJECT: 'destructive',
};

/**
 * ADMIN-only audit trail browser (IMPLEMENTATION_PLAN.md 3.9) - true
 * server-side pagination (AuditLogController always paginates, an audit
 * table only ever grows), unlike most *Management pages in this app which
 * fetch everything once and paginate/filter client-side.
 */
function AuditLogManagement() {
  const [pageIndex, setPageIndex] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [entityType, setEntityType] = useState('');

  const auditQuery = useQuery({
    queryKey: ['audit-log', pageIndex, pageSize, entityType],
    queryFn: () =>
      auditLogService
        .search({ entityType: entityType.trim() || undefined, page: pageIndex, size: pageSize })
        .then((r) => r.data),
  });

  const columns = useMemo(
    () => [
      {
        accessorKey: 'occurredAt',
        header: 'Thời gian',
        cell: ({ getValue }) => {
          const v = getValue();
          return v ? format(new Date(v), 'dd/MM/yyyy HH:mm:ss') : '—';
        },
      },
      {
        accessorKey: 'actorName',
        header: 'Người thực hiện',
        cell: ({ getValue }) => getValue() ?? <span className="text-muted-foreground">Hệ thống</span>,
      },
      {
        id: 'action',
        header: 'Hành động',
        cell: ({ row }) => (
          <Badge variant={ACTION_BADGE_VARIANT[row.original.action] ?? 'secondary'}>
            {AUDIT_ACTION_LABELS[row.original.action] ?? row.original.action}
          </Badge>
        ),
      },
      {
        id: 'entity',
        header: 'Đối tượng',
        cell: ({ row }) => (
          <span>
            {row.original.entityType}
            {row.original.entityId != null && <span className="text-muted-foreground"> #{row.original.entityId}</span>}
          </span>
        ),
      },
      {
        accessorKey: 'detailJson',
        header: 'Chi tiết',
        cell: ({ getValue }) => {
          const v = getValue();
          if (!v) return <span className="text-muted-foreground">—</span>;
          return (
            <span className="block max-w-xs truncate font-mono text-xs text-muted-foreground" title={v}>
              {v}
            </span>
          );
        },
      },
    ],
    []
  );

  const page = auditQuery.data;

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold">Nhật ký hệ thống</h1>
        <p className="text-sm text-muted-foreground">Các thao tác nhạy cảm đã ghi nhận (sửa/xóa điểm, xóa học sinh, duyệt tuyển sinh...)</p>
      </div>

      {auditQuery.isError && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive dark:text-red-400">
          Không tải được nhật ký hệ thống.
        </div>
      )}

      <DataTable
        columns={columns}
        data={page?.content ?? []}
        isLoading={auditQuery.isLoading}
        emptyMessage="Không có bản ghi nào."
        pageIndex={pageIndex}
        pageCount={page?.totalPages ?? 0}
        pageSize={pageSize}
        totalCount={page?.totalElements}
        onPageChange={setPageIndex}
        onPageSizeChange={(s) => { setPageSize(s); setPageIndex(0); }}
        pageSizeOptions={PAGE_SIZE_OPTIONS}
        searchValue={entityType}
        onSearchChange={(v) => { setEntityType(v); setPageIndex(0); }}
        searchPlaceholder="Lọc theo loại đối tượng (VD: GradeRecord)..."
      />
    </div>
  );
}

export default AuditLogManagement;
