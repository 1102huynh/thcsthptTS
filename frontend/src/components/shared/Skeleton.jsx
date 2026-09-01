import React from 'react';
import { Skeleton } from '@/components/ui/skeleton';
import { TableRow, TableCell } from '@/components/ui/table';
import { Card, CardContent } from '@/components/ui/card';

/**
 * Page-shaped skeleton pieces built on the shadcn Skeleton primitive, per
 * Tuần 5 Ngày 5 of the frontend plan - every place in the app that used to
 * show a bare "Đang tải..." string or a spinner while a query was in
 * flight now shows a shimmering placeholder shaped like the content that's
 * about to replace it, so the layout doesn't jump when data arrives.
 */

/** Rows of shimmering <TableCell>s - drop straight into a <TableBody> in
 * place of real rows while a table's data is loading. Bar widths vary a
 * little (60-95%) so it doesn't look like one uniform gray block. */
export function TableRowsSkeleton({ rows = 5, columns = 4 }) {
  const widths = ['w-3/4', 'w-1/2', 'w-5/6', 'w-2/3', 'w-1/3'];
  return (
    <>
      {Array.from({ length: rows }).map((_, r) => (
        <TableRow key={r}>
          {Array.from({ length: columns }).map((_, c) => (
            <TableCell key={c}>
              <Skeleton className={`h-4 ${widths[(r + c) % widths.length]}`} />
            </TableCell>
          ))}
        </TableRow>
      ))}
    </>
  );
}

/** A row of stat-card placeholders, matching StatCard's own icon-then-two-
 * lines layout (Dashboard.jsx) so the grid doesn't reflow once real
 * numbers land. */
export function StatCardsSkeleton({ count = 5, className = '' }) {
  return (
    <div className={`grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-5 ${className}`}>
      {Array.from({ length: count }).map((_, i) => (
        <Card key={i}>
          <CardContent className="space-y-3 p-5">
            <Skeleton className="h-10 w-10 rounded-lg" />
            <div className="space-y-2">
              <Skeleton className="h-4 w-20" />
              <Skeleton className="h-6 w-14" />
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

/** A block standing in for a chart while its data loads - same fixed
 * height as the real Recharts container it replaces, so the card doesn't
 * change size when the chart mounts. */
export function ChartSkeleton({ heightClass = 'h-64' }) {
  return <Skeleton className={`${heightClass} w-full`} />;
}

/** A short list of "text line + trailing timestamp" rows, matching
 * Dashboard's "Hoạt động gần đây" activity feed shape. */
export function ListRowsSkeleton({ rows = 4 }) {
  return (
    <div className="space-y-4">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="flex items-center justify-between gap-4">
          <Skeleton className="h-4 w-1/2" />
          <Skeleton className="h-3 w-20" />
        </div>
      ))}
    </div>
  );
}

/** Full-page skeleton shaped like AppShell (sidebar strip + topbar +
 * content blocks) - stands in for App.jsx's brief initial auth-check
 * frame, the one true "spinner toàn trang" in the app before this pass. */
export function AppShellSkeleton() {
  return (
    <div className="flex min-h-screen bg-muted" aria-hidden="true">
      <div className="hidden w-64 shrink-0 flex-col gap-4 border-r bg-background p-4 lg:flex">
        <Skeleton className="h-9 w-full" />
        {Array.from({ length: 6 }).map((_, i) => (
          <Skeleton key={i} className="h-8 w-full" />
        ))}
      </div>
      <div className="flex min-w-0 flex-1 flex-col">
        <div className="flex h-16 shrink-0 items-center border-b bg-background px-6">
          <Skeleton className="h-6 w-40" />
        </div>
        <div className="flex-1 space-y-4 p-4 sm:p-6">
          <Skeleton className="h-28 w-full rounded-xl" />
          <StatCardsSkeleton count={5} />
        </div>
      </div>
    </div>
  );
}
