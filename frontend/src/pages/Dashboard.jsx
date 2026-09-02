import React, { useMemo } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { formatDistanceToNow, subDays, format as formatDate } from 'date-fns';
import { vi } from 'date-fns/locale';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
} from 'recharts';
import {
  FiUsers,
  FiBook,
  FiClipboard,
  FiPercent,
  FiDollarSign,
  FiAward,
  FiArrowRight,
  FiCalendar,
} from 'react-icons/fi';
import {
  dashboardService,
  attendanceService,
  feeService,
  academicYearService,
  auditLogService,
} from '../services/dataService';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/card';
import { Badge } from '../components/ui/badge';
import { StatCardsSkeleton, ChartSkeleton, ListRowsSkeleton } from '../components/shared/Skeleton';

const ISO_DATE = 'yyyy-MM-dd';
const CHART_DAYS = 14;

function currencyVND(n) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(
    n ?? 0
  );
}

/**
 * Aggregates the raw AttendanceDTO list (from GET /v1/attendance/between)
 * into one { date, rate } point per day covering the last CHART_DAYS days -
 * days with zero records still get an explicit 0-row point instead of a
 * gap, so the line doesn't visually skip weekends/holidays.
 */
function buildAttendanceChartData(records, startDate, endDate) {
  const byDate = new Map();
  for (const r of records ?? []) {
    const bucket = byDate.get(r.attendanceDate) ?? { total: 0, present: 0 };
    bucket.total += 1;
    if (r.status === 'PRESENT') bucket.present += 1;
    byDate.set(r.attendanceDate, bucket);
  }

  const days = [];
  let cursor = new Date(startDate);
  const end = new Date(endDate);
  while (cursor <= end) {
    const key = formatDate(cursor, ISO_DATE);
    const bucket = byDate.get(key);
    days.push({
      date: key,
      label: formatDate(cursor, 'dd/MM'),
      rate: bucket && bucket.total > 0 ? Math.round((bucket.present / bucket.total) * 1000) / 10 : null,
    });
    cursor = new Date(cursor.getTime() + 86_400_000);
  }
  return days;
}

/** Aggregates FeeDTO[] (from GET /v1/fees/year/{year}) into one row per
 * month (by dueDate), summing paidAmount vs remainingAmount. */
function buildFeeChartData(fees) {
  const byMonth = new Map();
  for (const f of fees ?? []) {
    if (!f.dueDate) continue;
    const monthKey = f.dueDate.slice(0, 7); // yyyy-MM
    const bucket = byMonth.get(monthKey) ?? { paid: 0, remaining: 0 };
    bucket.paid += f.paidAmount ?? 0;
    bucket.remaining += f.remainingAmount ?? 0;
    byMonth.set(monthKey, bucket);
  }
  return [...byMonth.entries()]
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([month, { paid, remaining }]) => ({
      month: formatDate(new Date(`${month}-01`), 'MM/yyyy'),
      'Đã thu': Math.round(paid),
      'Còn nợ': Math.round(remaining),
    }));
}

const AUDIT_ACTION_LABELS = {
  CREATE: 'tạo mới',
  UPDATE: 'cập nhật',
  DELETE: 'xóa',
  APPROVE: 'duyệt',
  REJECT: 'từ chối',
};

function StatCard({ icon: Icon, title, value, iconClassName }) {
  return (
    <Card>
      <CardContent className="space-y-3 p-5">
        <div className={`flex h-10 w-10 items-center justify-center rounded-lg ${iconClassName}`}>
          <Icon className="h-5 w-5" />
        </div>
        <div>
          {/* Stacked (icon above text) rather than side-by-side - at 5
              cards per row, Vietnamese labels like "Chuyên cần (30 ngày)"
              truncated to unreadable fragments ("Ch...") in a side-by-side
              layout with this little width; full card width fixes it. */}
          <p className="text-sm leading-snug text-muted-foreground">{title}</p>
          {/* `truncate` used to live here too, forcing the value onto one
              line - fine for short counts, but the currency-formatted "còn
              nợ" figure still got cut to "135…" even at 5 columns on a full
              1400px desktop (grid-cols-1 on mobile, Tuần 5 Ngày 3, only
              fixed the narrowest case). Wrapping instead of truncating is
              breakpoint-proof: the value is always fully readable, just
              taller when it doesn't fit one line. */}
          <p className="break-words text-xl font-semibold tabular-nums">{value}</p>
        </div>
      </CardContent>
    </Card>
  );
}

function QuickAction({ icon: Icon, label, to, className }) {
  return (
    <Link
      to={to}
      className={`group flex items-center gap-3 rounded-lg p-4 text-white transition-transform hover:-translate-y-0.5 ${className}`}
    >
      <Icon className="h-5 w-5" />
      <span className="flex-1 font-medium">{label}</span>
      <FiArrowRight className="h-4 w-4 opacity-0 transition-opacity group-hover:opacity-100" />
    </Link>
  );
}

function Dashboard({ user }) {
  const isAdmin = user?.role === 'ADMIN';
  // DashboardController's GET /v1/dashboard/stats is ADMIN/PRINCIPAL only -
  // every other role that can land on this page (STUDENT, TEACHER,
  // LIBRARIAN, ACCOUNTANT all reach "/") got an unconditional 403 + console
  // error on every single login, and a permanently-broken stats section,
  // because this query used to fire regardless of role. Caught live while
  // testing LibraryManagement as a STUDENT account, not by inspection.
  const isAdminOrPrincipal = isAdmin || user?.role === 'PRINCIPAL';

  const { startDate, endDate } = useMemo(() => {
    const end = new Date();
    return { startDate: formatDate(subDays(end, CHART_DAYS - 1), ISO_DATE), endDate: formatDate(end, ISO_DATE) };
  }, []);

  const statsQuery = useQuery({
    queryKey: ['dashboard-stats'],
    queryFn: () => dashboardService.getStats().then((r) => r.data),
    enabled: isAdminOrPrincipal,
  });

  // The charts/activity feed below need endpoints only ADMIN can call
  // (AttendanceController's /between, FeeController's /year/{}, and
  // AuditLogController are all ADMIN-only or ADMIN+TEACHER/ACCOUNTANT -
  // PRINCIPAL, who can otherwise view this Dashboard per
  // DashboardController's own @PreAuthorize, can't) - gated with `enabled`
  // so a PRINCIPAL session never fires (and 403s on) these calls.
  const attendanceQuery = useQuery({
    queryKey: ['dashboard-attendance-trend', startDate, endDate],
    queryFn: () => attendanceService.getBetweenDates(startDate, endDate).then((r) => r.data),
    enabled: isAdmin,
  });

  const academicYearsQuery = useQuery({
    queryKey: ['academic-years'],
    queryFn: () => academicYearService.getAll().then((r) => r.data),
    enabled: isAdmin,
  });
  const activeAcademicYear = academicYearsQuery.data?.find((y) => y.status === 'ACTIVE')?.name;

  const feesQuery = useQuery({
    queryKey: ['dashboard-fees-by-year', activeAcademicYear],
    queryFn: () => feeService.getByYear(activeAcademicYear).then((r) => r.data),
    enabled: isAdmin && Boolean(activeAcademicYear),
  });

  const activityQuery = useQuery({
    queryKey: ['dashboard-recent-activity'],
    queryFn: () => auditLogService.getRecent(5).then((r) => r.data.content),
    enabled: isAdmin,
  });

  const attendanceChartData = useMemo(
    () => buildAttendanceChartData(attendanceQuery.data, startDate, endDate),
    [attendanceQuery.data, startDate, endDate]
  );
  const feeChartData = useMemo(() => buildFeeChartData(feesQuery.data), [feesQuery.data]);

  const stats = statsQuery.data;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col justify-between gap-3 rounded-xl bg-gradient-to-r from-primary to-purple-600 p-6 text-primary-foreground sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-bold">
            Chào mừng trở lại, <span className="opacity-90">{user?.firstName}!</span>
          </h1>
          <p className="text-primary-foreground/80">Đây là tình hình trường học hôm nay</p>
        </div>
        <div className="flex items-center gap-2 sm:flex-col sm:items-end">
          <Badge className="border-white/30 bg-white/15 text-white hover:bg-white/15">{user?.role}</Badge>
          <span className="text-sm text-primary-foreground/80">{user?.email}</span>
        </div>
      </div>

      {isAdminOrPrincipal && (
        <>
          {statsQuery.isError && (
            <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive dark:text-red-400">
              Không tải được số liệu thống kê. Vui lòng thử lại sau.
            </div>
          )}

          {statsQuery.isLoading ? (
            <StatCardsSkeleton count={5} />
          ) : (
            /* Stat cards - real values from GET /v1/dashboard/stats.
               1 column below sm: at 2-per-row on a 375px phone, a longer
               value like the currency-formatted "còn nợ" figure had no room
               and truncated to an unreadable "135…" (caught on a mobile
               responsive pass, Tuần 5 Ngày 3) - full card width fixes it,
               same reasoning as the stacked icon/label layout below. */
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-5">
              <StatCard
                icon={FiUsers}
                title="Nhân viên"
                value={stats?.activeStaffCount ?? 0}
                iconClassName="bg-primary/10 text-primary"
              />
              <StatCard
                icon={FiUsers}
                title="Học sinh"
                value={stats?.activeStudentCount ?? 0}
                iconClassName="bg-green-500/10 text-green-600"
              />
              <StatCard
                icon={FiBook}
                title="Sách đang mượn"
                value={stats?.booksBorrowedCount ?? 0}
                iconClassName="bg-blue-500/10 text-blue-600"
              />
              <StatCard
                icon={FiPercent}
                title="Chuyên cần (30 ngày)"
                value={`${(stats?.averageAttendanceRate ?? 0).toFixed(1)}%`}
                iconClassName="bg-amber-500/10 text-amber-600"
              />
              <StatCard
                icon={FiDollarSign}
                title="Học phí còn nợ"
                value={currencyVND(stats?.totalOutstandingFees)}
                iconClassName="bg-rose-500/10 text-rose-600"
              />
            </div>
          )}
        </>
      )}

      {/* Quick actions */}
      <Card>
        <CardHeader>
          <CardTitle>Truy cập nhanh</CardTitle>
          <CardDescription>Các chức năng thường dùng</CardDescription>
        </CardHeader>
        <CardContent className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
          <QuickAction icon={FiUsers} label="Nhân viên" to="/staff" className="bg-gradient-to-br from-indigo-500 to-purple-600" />
          <QuickAction icon={FiUsers} label="Học sinh" to="/students" className="bg-gradient-to-br from-emerald-500 to-teal-600" />
          <QuickAction icon={FiClipboard} label="Điểm danh" to="/attendance" className="bg-gradient-to-br from-sky-500 to-blue-600" />
          <QuickAction icon={FiAward} label="Điểm số" to="/grades" className="bg-gradient-to-br from-amber-500 to-orange-600" />
          <QuickAction icon={FiBook} label="Thư viện" to="/library" className="bg-gradient-to-br from-violet-500 to-fuchsia-600" />
          <QuickAction icon={FiDollarSign} label="Học phí" to="/fees" className="bg-gradient-to-br from-rose-500 to-red-600" />
        </CardContent>
      </Card>

      {isAdmin ? (
        <>
          {/* Charts - real data aggregated client-side from list endpoints
              (no dedicated trend-aggregation endpoint exists on the backend
              yet, see commit message for why this approach was chosen). */}
          <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Tỷ lệ chuyên cần</CardTitle>
                <CardDescription>{CHART_DAYS} ngày gần nhất, toàn trường</CardDescription>
              </CardHeader>
              <CardContent>
                {attendanceQuery.isLoading ? (
                  <ChartSkeleton />
                ) : attendanceChartData.every((d) => d.rate == null) ? (
                  <div className="flex h-64 items-center justify-center text-sm text-muted-foreground">
                    Chưa có dữ liệu điểm danh trong {CHART_DAYS} ngày gần nhất.
                  </div>
                ) : (
                  <ResponsiveContainer width="100%" height={260}>
                    <LineChart data={attendanceChartData}>
                      <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
                      <XAxis dataKey="label" tick={{ fontSize: 12 }} />
                      <YAxis domain={[0, 100]} tick={{ fontSize: 12 }} unit="%" width={40} />
                      <Tooltip
                        formatter={(v) => (v == null ? 'Không có dữ liệu' : `${v}%`)}
                        labelFormatter={(label) => `Ngày ${label}`}
                      />
                      <Line
                        type="monotone"
                        dataKey="rate"
                        name="Chuyên cần"
                        stroke="hsl(var(--primary))"
                        strokeWidth={2}
                        connectNulls
                        dot={{ r: 3 }}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Thu học phí theo tháng</CardTitle>
                <CardDescription>
                  {activeAcademicYear ? `Năm học ${activeAcademicYear}` : 'Theo năm học đang hoạt động'}
                </CardDescription>
              </CardHeader>
              <CardContent>
                {feesQuery.isLoading || academicYearsQuery.isLoading ? (
                  <ChartSkeleton />
                ) : academicYearsQuery.isSuccess && !activeAcademicYear ? (
                  <div className="flex h-64 items-center justify-center px-6 text-center text-sm text-muted-foreground">
                    Chưa thiết lập năm học đang hoạt động (ACTIVE) trong hệ thống.
                  </div>
                ) : feeChartData.length === 0 ? (
                  <div className="flex h-64 items-center justify-center text-sm text-muted-foreground">
                    Chưa có dữ liệu học phí cho năm học này.
                  </div>
                ) : (
                  <ResponsiveContainer width="100%" height={260}>
                    <BarChart data={feeChartData}>
                      <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
                      <XAxis dataKey="month" tick={{ fontSize: 12 }} />
                      <YAxis tick={{ fontSize: 12 }} width={60} tickFormatter={(v) => `${Math.round(v / 1_000_000)}tr`} />
                      <Tooltip formatter={(v) => currencyVND(v)} />
                      <Legend />
                      <Bar dataKey="Đã thu" stackId="a" fill="hsl(var(--primary))" radius={[0, 0, 0, 0]} />
                      <Bar dataKey="Còn nợ" stackId="a" fill="hsl(var(--destructive))" radius={[4, 4, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                )}
              </CardContent>
            </Card>
          </div>

          {/* Recent activity - real audit log entries (ADMIN only) */}
          <Card>
            <CardHeader>
              <CardTitle>Hoạt động gần đây</CardTitle>
              <CardDescription>5 thao tác nhạy cảm gần nhất trong hệ thống</CardDescription>
            </CardHeader>
            <CardContent>
              {activityQuery.isLoading ? (
                <ListRowsSkeleton rows={5} />
              ) : !activityQuery.data?.length ? (
                <p className="py-4 text-center text-sm text-muted-foreground">Chưa có hoạt động nào được ghi nhận.</p>
              ) : (
                <ul className="divide-y">
                  {activityQuery.data.map((entry) => (
                    <li key={entry.id} className="flex items-center justify-between gap-3 py-3 text-sm">
                      <span>
                        <span className="font-medium">{entry.actorName ?? 'Hệ thống'}</span>{' '}
                        {AUDIT_ACTION_LABELS[entry.action] ?? entry.action?.toLowerCase()} {entry.entityType}
                        {entry.entityId != null && ` #${entry.entityId}`}
                      </span>
                      <span className="shrink-0 text-muted-foreground">
                        {formatDistanceToNow(new Date(entry.occurredAt), { addSuffix: true, locale: vi })}
                      </span>
                    </li>
                  ))}
                </ul>
              )}
            </CardContent>
          </Card>
        </>
      ) : (
        <Card>
          <CardContent className="flex items-center gap-3 p-5 text-sm text-muted-foreground">
            <FiCalendar className="h-5 w-5 shrink-0" />
            Biểu đồ chuyên cần, thu học phí và hoạt động gần đây chỉ dành cho tài khoản ADMIN.
          </CardContent>
        </Card>
      )}
    </div>
  );
}

export default Dashboard;
