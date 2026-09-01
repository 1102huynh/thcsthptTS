import React, { useState } from 'react';
import {
  flexRender,
  getCoreRowModel,
  getSortedRowModel,
  useReactTable,
} from '@tanstack/react-table';
import { FiChevronLeft, FiChevronRight, FiChevronUp, FiChevronDown } from 'react-icons/fi';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { cn } from '@/lib/utils';

const DEFAULT_PAGE_SIZE_OPTIONS = [10, 20, 50, 100];

function SortIcon({ sorted }) {
  if (sorted === 'asc') return <FiChevronUp className="h-3.5 w-3.5" />;
  if (sorted === 'desc') return <FiChevronDown className="h-3.5 w-3.5" />;
  // Neutral/unsorted state: no dedicated "chevrons up-down" glyph in
  // react-icons/fi, so a faint down-chevron stands in as the sort affordance.
  return <FiChevronDown className="h-3.5 w-3.5 opacity-30" />;
}

/**
 * Generic paginated/sortable table (TanStack Table v8 + shadcn Table), per
 * Tuần 2 Ngày 1-2 of the frontend plan. package.json pins
 * @tanstack/react-table to ^8.x on purpose - the plan and shadcn's own
 * DataTable docs both assume v8's `useReactTable`/`getCoreRowModel` hook
 * API; v9 (npm's default `latest` as of writing) replaced it with a
 * store-based API and only keeps v8's shape behind an undocumented
 * `/legacy` subpath, so v8 is the one actually meant here, not a stale
 * choice. Backend list endpoints return Spring
 * Data `Page<T>` (`content`/`number`/`totalPages`/`totalElements`), so
 * pagination here is server-driven by default: pass the current page's
 * `data` plus `pageIndex`/`pageCount`/`onPageChange` straight from that
 * response - see each *Management page's wiring (Tuần 3) for the mapping.
 *
 * Sorting is client-side over just the rows passed in `data` (TanStack's
 * getSortedRowModel) - fine for a single already-paginated page of rows.
 * If a column needs to sort the full server-side dataset instead, handle
 * `onSortingChange` yourself and refetch, same pattern as pagination.
 *
 * Omit `pageCount` (or pass `undefined`) to render without pagination
 * controls - e.g. for a short, non-paginated list.
 */
function DataTable({
  columns,
  data,
  isLoading = false,
  emptyMessage = 'Không có dữ liệu.',
  // Pagination (server-driven; omit pageCount to disable the footer controls)
  pageIndex = 0,
  pageCount,
  pageSize = 10,
  totalCount,
  onPageChange,
  onPageSizeChange,
  pageSizeOptions = DEFAULT_PAGE_SIZE_OPTIONS,
  // Optional toolbar search box - controlled by the caller (server-side
  // search), rendered only when both props are supplied.
  searchValue,
  onSearchChange,
  searchPlaceholder = 'Tìm kiếm...',
  // Sorting - uncontrolled by default (component owns its own state); pass
  // both to control it from the parent instead (server-side sort).
  sorting: controlledSorting,
  onSortingChange,
}) {
  const [internalSorting, setInternalSorting] = useState([]);
  const sorting = controlledSorting ?? internalSorting;
  const setSorting = onSortingChange ?? setInternalSorting;

  const table = useReactTable({
    data,
    columns,
    state: { sorting },
    onSortingChange: (updater) =>
      setSorting(typeof updater === 'function' ? updater(sorting) : updater),
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    // Without this, TanStack's default row id is just the row's position
    // within `data` (0, 1, 2, ...) - React then keys <TableRow> by
    // position instead of identity, which both mis-reconciles rows with
    // any internal state across a refetch/mutation AND produces real
    // "duplicate key" warnings whenever a page's row count changes (caught
    // live on FeeManagement, Tuần 4 Ngày 5, but this affected every page
    // built on DataTable up to now). Every current caller's rows carry a
    // real `id` (Staff/Student/LibraryBook/Fee), so use that instead.
    getRowId: (row) => String(row.id),
  });

  const paginated = typeof pageCount === 'number' && pageCount >= 0;
  const rows = table.getRowModel().rows;

  const rangeStart = totalCount === 0 ? 0 : pageIndex * pageSize + 1;
  const rangeEnd = totalCount != null ? Math.min((pageIndex + 1) * pageSize, totalCount) : undefined;

  return (
    <div className="space-y-3">
      {onSearchChange && (
        <Input
          value={searchValue ?? ''}
          onChange={(e) => onSearchChange(e.target.value)}
          placeholder={searchPlaceholder}
          className="max-w-xs"
        />
      )}

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            {table.getHeaderGroups().map((headerGroup) => (
              <TableRow key={headerGroup.id}>
                {headerGroup.headers.map((header) => {
                  const canSort = header.column.getCanSort();
                  // The row-actions column (every *Management page names it
                  // id: 'actions') gets pinned to the right edge of the
                  // table's own horizontal scroll area. Without this, on a
                  // narrow phone the edit/delete buttons scroll out of view
                  // with no visual hint the row even has more to it - caught
                  // during the Tuần 5 Ngày 3 responsive/mobile audit.
                  const isActions = header.column.id === 'actions';
                  return (
                    <TableHead
                      key={header.id}
                      className={cn(isActions && 'sticky right-0 z-10 bg-background shadow-[-4px_0_4px_-4px_rgba(0,0,0,0.15)]')}
                    >
                      {header.isPlaceholder ? null : canSort ? (
                        <button
                          type="button"
                          onClick={header.column.getToggleSortingHandler()}
                          className="flex items-center gap-1 hover:text-foreground"
                        >
                          {flexRender(header.column.columnDef.header, header.getContext())}
                          <SortIcon sorted={header.column.getIsSorted()} />
                        </button>
                      ) : (
                        flexRender(header.column.columnDef.header, header.getContext())
                      )}
                    </TableHead>
                  );
                })}
              </TableRow>
            ))}
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={columns.length} className="h-24 text-center text-muted-foreground">
                  Đang tải...
                </TableCell>
              </TableRow>
            ) : rows.length === 0 ? (
              <TableRow>
                <TableCell colSpan={columns.length} className="h-24 text-center text-muted-foreground">
                  {emptyMessage}
                </TableCell>
              </TableRow>
            ) : (
              rows.map((row) => (
                <TableRow key={row.id}>
                  {row.getVisibleCells().map((cell) => (
                    <TableCell
                      key={cell.id}
                      className={cn(
                        cell.column.id === 'actions' &&
                          'sticky right-0 z-10 bg-background shadow-[-4px_0_4px_-4px_rgba(0,0,0,0.15)]'
                      )}
                    >
                      {flexRender(cell.column.columnDef.cell, cell.getContext())}
                    </TableCell>
                  ))}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {paginated && (
        <div className="flex flex-col items-center justify-between gap-3 sm:flex-row">
          <div className="text-sm text-muted-foreground">
            {totalCount != null
              ? `Hiển thị ${rangeStart}–${rangeEnd} trong tổng số ${totalCount}`
              : `Trang ${pageIndex + 1} / ${Math.max(pageCount, 1)}`}
          </div>

          <div className="flex items-center gap-4">
            {onPageSizeChange && (
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <span className="hidden sm:inline">Số dòng/trang</span>
                <Select value={String(pageSize)} onValueChange={(v) => onPageSizeChange(Number(v))}>
                  <SelectTrigger className="h-8 w-[70px]">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {pageSizeOptions.map((size) => (
                      <SelectItem key={size} value={String(size)}>
                        {size}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            )}

            <div className="flex items-center gap-1">
              <Button
                variant="outline"
                size="icon"
                className="h-8 w-8"
                disabled={pageIndex <= 0}
                onClick={() => onPageChange?.(pageIndex - 1)}
                aria-label="Trang trước"
              >
                <FiChevronLeft className="h-4 w-4" />
              </Button>
              <span className={cn('px-2 text-sm tabular-nums', totalCount == null && 'hidden')}>
                {pageIndex + 1} / {Math.max(pageCount, 1)}
              </span>
              <Button
                variant="outline"
                size="icon"
                className="h-8 w-8"
                disabled={pageIndex >= pageCount - 1}
                onClick={() => onPageChange?.(pageIndex + 1)}
                aria-label="Trang sau"
              >
                <FiChevronRight className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default DataTable;
