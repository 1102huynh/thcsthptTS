import React, { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { toast } from 'sonner';
import { FiDownload } from 'react-icons/fi';
import {
  studentService,
  parentService,
  academicYearService,
  semesterService,
  gradeRecordService,
  attendanceService,
  feeService,
  conductService,
  reportService,
} from '../services/dataService';
import { triggerBlobDownload } from '../lib/download';
import { getCurrentUser } from '../services/authService';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Badge } from '../components/ui/badge';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../components/ui/select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '../components/ui/table';
import { TableRowsSkeleton } from '../components/shared/Skeleton';
import {
  ATTENDANCE_STATUS_LABELS,
  FEE_STATUS_LABELS,
  CONDUCT_RATING_LABELS,
  SEMESTER_NAME_LABELS,
  PARENT_RELATIONSHIP_LABELS,
} from '../lib/enumLabels';

// C3 - one page serving both STUDENT (their own data) and PARENT (each
// linked child, picked from a dropdown). Every endpoint it calls is already
// narrowed server-side by StudentAccessGuard, so a tampered studentId just
// 403s; the child picker is a convenience, not the security boundary.

const TABS = [
  { key: 'grades', label: 'Điểm' },
  { key: 'attendance', label: 'Điểm danh' },
  { key: 'fees', label: 'Học phí' },
  { key: 'conduct', label: 'Hạnh kiểm' },
];

// Attendance statuses that count as "the student was in class" for the
// headline rate. LATE is present-but-tardy; the leave states and ABSENT are
// not. Mirrors how a homeroom teacher reads a chuyên cần column.
const PRESENT_LIKE = new Set(['PRESENT', 'LATE']);

function currencyVND(n) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(n ?? 0);
}

function oneDecimal(n) {
  return n == null ? '—' : Number(n).toFixed(1);
}

function semesterLabel(s) {
  return `${s.academicYearName} · ${SEMESTER_NAME_LABELS[s.name] ?? s.name}`;
}

function SelfServicePortal() {
  const user = getCurrentUser();
  const role = user?.role;
  const isParent = role === 'PARENT';

  // ---- Who are we looking at? ----------------------------------------
  const meQuery = useQuery({
    queryKey: ['portal', 'me'],
    queryFn: () => studentService.getMe().then((r) => r.data),
    enabled: role === 'STUDENT',
    retry: false,
  });

  const childrenQuery = useQuery({
    queryKey: ['portal', 'children', user?.userId],
    queryFn: () => parentService.getChildren(user.userId).then((r) => r.data),
    enabled: isParent && Boolean(user?.userId),
  });

  const subjects = useMemo(() => {
    if (role === 'STUDENT') {
      return meQuery.data
        ? [
            {
              id: meQuery.data.id,
              name: `${meQuery.data.user?.firstName ?? ''} ${meQuery.data.user?.lastName ?? ''}`.trim() || 'Học sinh',
              rollNumber: meQuery.data.rollNumber,
              className: meQuery.data.className,
            },
          ]
        : [];
    }
    return (childrenQuery.data ?? []).map((rel) => ({
      id: rel.studentId,
      name: rel.studentName || 'Học sinh',
      rollNumber: rel.rollNumber,
      relationship: PARENT_RELATIONSHIP_LABELS[rel.relationship] ?? rel.relationship,
    }));
  }, [role, meQuery.data, childrenQuery.data]);

  const [studentId, setStudentId] = useState(null);
  useEffect(() => {
    if (studentId == null && subjects.length) setStudentId(subjects[0].id);
  }, [subjects, studentId]);
  const selected = subjects.find((s) => s.id === studentId);

  const [tab, setTab] = useState('grades');

  // ---- Loading / empty states for the subject resolution ------------
  if (role === 'STUDENT' && meQuery.isError) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Trang của tôi</CardTitle>
          <CardDescription>
            Tài khoản của bạn chưa được liên kết với hồ sơ học sinh nào. Vui lòng liên hệ nhà
            trường.
          </CardDescription>
        </CardHeader>
      </Card>
    );
  }

  if (isParent && childrenQuery.isSuccess && subjects.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Trang của tôi</CardTitle>
          <CardDescription>
            Chưa có học sinh nào được liên kết với tài khoản phụ huynh này. Vui lòng liên hệ nhà
            trường.
          </CardDescription>
        </CardHeader>
      </Card>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">Trang của tôi</h1>
          <p className="text-sm text-muted-foreground">
            {isParent
              ? 'Theo dõi tình hình học tập của con.'
              : 'Điểm, điểm danh, học phí và hạnh kiểm của bạn.'}
          </p>
        </div>

        {isParent && subjects.length > 0 && (
          <div className="w-full sm:w-72">
            <label className="mb-1 block text-sm font-medium">Chọn con</label>
            <Select value={studentId ? String(studentId) : ''} onValueChange={(v) => setStudentId(Number(v))}>
              <SelectTrigger>
                <SelectValue placeholder="Chọn học sinh" />
              </SelectTrigger>
              <SelectContent>
                {subjects.map((s) => (
                  <SelectItem key={s.id} value={String(s.id)}>
                    {s.name}
                    {s.relationship ? ` (${s.relationship})` : ''}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        )}
      </div>

      {selected && (
        <p className="text-sm text-muted-foreground">
          <span className="font-medium text-foreground">{selected.name}</span>
          {selected.rollNumber ? ` · Mã HS: ${selected.rollNumber}` : ''}
          {selected.className ? ` · Lớp: ${selected.className}` : ''}
        </p>
      )}

      <div className="flex flex-wrap gap-2 border-b">
        {TABS.map((t) => (
          <button
            key={t.key}
            type="button"
            onClick={() => setTab(t.key)}
            className={
              'relative -mb-px border-b-2 px-3 py-2 text-sm font-medium transition-colors ' +
              (tab === t.key
                ? 'border-primary text-foreground'
                : 'border-transparent text-muted-foreground hover:text-foreground')
            }
            aria-current={tab === t.key ? 'page' : undefined}
          >
            {t.label}
          </button>
        ))}
      </div>

      {!studentId ? (
        <TableRowsSkeleton rows={5} columns={4} />
      ) : (
        <>
          {tab === 'grades' && <GradesTab studentId={studentId} studentName={selected?.name} />}
          {tab === 'attendance' && <AttendanceTab studentId={studentId} />}
          {tab === 'fees' && <FeesTab studentId={studentId} studentName={selected?.name} />}
          {tab === 'conduct' && <ConductTab studentId={studentId} />}
        </>
      )}
    </div>
  );
}

// ---- Grades -------------------------------------------------------------
function GradesTab({ studentId, studentName }) {
  const yearsQuery = useQuery({
    queryKey: ['portal', 'years'],
    queryFn: () => academicYearService.getAll().then((r) => r.data),
  });
  const years = yearsQuery.data ?? [];

  const [yearId, setYearId] = useState(null);
  useEffect(() => {
    if (yearId == null && years.length) {
      const active = years.find((y) => y.status === 'ACTIVE') ?? years[0];
      setYearId(active.id);
    }
  }, [years, yearId]);

  const selectedYear = years.find((y) => y.id === yearId);

  const downloadTranscript = useMutation({
    mutationFn: () =>
      triggerBlobDownload(
        reportService.studentTranscript(studentId, yearId),
        `hoc-ba-${(studentName ?? 'hoc-sinh').replace(/\s+/g, '-')}-${selectedYear?.name ?? yearId}.pdf`
      ),
    onError: (err) => toast.error(err.message || 'Không thể tải học bạ'),
  });

  const semestersQuery = useQuery({
    queryKey: ['portal', 'semesters', yearId],
    queryFn: () => semesterService.getByAcademicYear(yearId).then((r) => r.data),
    enabled: Boolean(yearId),
  });
  const semesters = semestersQuery.data ?? [];

  const [semesterId, setSemesterId] = useState(null);
  useEffect(() => {
    if (semesters.length) {
      if (semesterId == null || !semesters.some((s) => s.id === semesterId)) {
        setSemesterId(semesters[0].id);
      }
    }
  }, [semesters, semesterId]);

  const summaryQuery = useQuery({
    queryKey: ['portal', 'grade-summary', studentId, semesterId],
    queryFn: () => gradeRecordService.getStudentSemesterSummary(studentId, semesterId).then((r) => r.data),
    enabled: Boolean(studentId) && Boolean(semesterId),
  });

  const yearSummaryQuery = useQuery({
    queryKey: ['portal', 'grade-year-summary', studentId, yearId],
    queryFn: () => gradeRecordService.getStudentYearSummary(studentId, yearId).then((r) => r.data),
    enabled: Boolean(studentId) && Boolean(yearId),
  });

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap gap-3">
        <div className="w-56">
          <label className="mb-1 block text-sm font-medium">Năm học</label>
          <Select value={yearId ? String(yearId) : ''} onValueChange={(v) => setYearId(Number(v))}>
            <SelectTrigger>
              <SelectValue placeholder="Năm học" />
            </SelectTrigger>
            <SelectContent>
              {years.map((y) => (
                <SelectItem key={y.id} value={String(y.id)}>
                  {y.name}
                  {y.status === 'ACTIVE' ? ' (hiện hành)' : ''}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="w-56">
          <label className="mb-1 block text-sm font-medium">Học kỳ</label>
          <Select
            value={semesterId ? String(semesterId) : ''}
            onValueChange={(v) => setSemesterId(Number(v))}
            disabled={!semesters.length}
          >
            <SelectTrigger>
              <SelectValue placeholder="Học kỳ" />
            </SelectTrigger>
            <SelectContent>
              {semesters.map((s) => (
                <SelectItem key={s.id} value={String(s.id)}>
                  {SEMESTER_NAME_LABELS[s.name] ?? s.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="flex items-end">
          <Button
            variant="outline"
            onClick={() => downloadTranscript.mutate()}
            disabled={!yearId || downloadTranscript.isPending}
          >
            <FiDownload className="mr-2 h-4 w-4" />
            {downloadTranscript.isPending ? 'Đang tải...' : 'Tải học bạ (PDF)'}
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Điểm trung bình môn — học kỳ</CardTitle>
          <CardDescription>Σ(điểm × hệ số) / Σ(hệ số) theo từng môn.</CardDescription>
        </CardHeader>
        <CardContent>
          {summaryQuery.isLoading ? (
            <TableRowsSkeleton rows={6} columns={2} />
          ) : (summaryQuery.data ?? []).length === 0 ? (
            <p className="text-sm text-muted-foreground">Chưa có điểm cho học kỳ này.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Môn học</TableHead>
                  <TableHead className="text-right">ĐTB môn</TableHead>
                  <TableHead>Xếp loại</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {summaryQuery.data.map((row) => (
                  <TableRow key={row.subjectId}>
                    <TableCell>{row.subjectName}</TableCell>
                    <TableCell className="text-right font-medium tabular-nums">
                      {oneDecimal(row.average)}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {row.classification ?? '—'}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Tổng kết cả năm</CardTitle>
          <CardDescription>(ĐTB HK1 + ĐTB HK2 × 2) / 3 theo từng môn.</CardDescription>
        </CardHeader>
        <CardContent>
          {yearSummaryQuery.isLoading ? (
            <TableRowsSkeleton rows={6} columns={4} />
          ) : (yearSummaryQuery.data ?? []).length === 0 ? (
            <p className="text-sm text-muted-foreground">Chưa có dữ liệu cả năm.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Môn học</TableHead>
                  <TableHead className="text-right">HK1</TableHead>
                  <TableHead className="text-right">HK2</TableHead>
                  <TableHead className="text-right">Cả năm</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {yearSummaryQuery.data.map((row) => (
                  <TableRow key={row.subjectId}>
                    <TableCell>{row.subjectName}</TableCell>
                    <TableCell className="text-right tabular-nums">{oneDecimal(row.semester1Average)}</TableCell>
                    <TableCell className="text-right tabular-nums">{oneDecimal(row.semester2Average)}</TableCell>
                    <TableCell className="text-right font-medium tabular-nums">{oneDecimal(row.yearAverage)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

// ---- Attendance ------------------------------------------------------
function AttendanceTab({ studentId }) {
  const attendanceQuery = useQuery({
    queryKey: ['portal', 'attendance', studentId],
    queryFn: () => attendanceService.getByStudent(studentId).then((r) => r.data),
    enabled: Boolean(studentId),
  });
  const records = attendanceQuery.data ?? [];

  const stats = useMemo(() => {
    const total = records.length;
    const present = records.filter((r) => PRESENT_LIKE.has(r.status)).length;
    const byStatus = {};
    for (const r of records) byStatus[r.status] = (byStatus[r.status] ?? 0) + 1;
    return {
      total,
      present,
      rate: total ? Math.round((present / total) * 100) : null,
      byStatus,
    };
  }, [records]);

  const recent = useMemo(
    () =>
      [...records]
        .sort((a, b) => String(b.attendanceDate).localeCompare(String(a.attendanceDate)))
        .slice(0, 40),
    [records]
  );

  if (attendanceQuery.isLoading) return <TableRowsSkeleton rows={8} columns={3} />;

  return (
    <div className="space-y-6">
      <div className="grid gap-4 sm:grid-cols-3">
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Tỷ lệ chuyên cần</CardDescription>
            <CardTitle className="text-3xl tabular-nums">
              {stats.rate == null ? '—' : `${stats.rate}%`}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Số buổi có mặt</CardDescription>
            <CardTitle className="text-3xl tabular-nums">
              {stats.present}
              <span className="text-base font-normal text-muted-foreground"> / {stats.total}</span>
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Số buổi vắng</CardDescription>
            <CardTitle className="text-3xl tabular-nums">{stats.byStatus.ABSENT ?? 0}</CardTitle>
          </CardHeader>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Lịch sử điểm danh gần đây</CardTitle>
          <CardDescription>40 bản ghi mới nhất.</CardDescription>
        </CardHeader>
        <CardContent>
          {recent.length === 0 ? (
            <p className="text-sm text-muted-foreground">Chưa có bản ghi điểm danh.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Ngày</TableHead>
                  <TableHead>Trạng thái</TableHead>
                  <TableHead>Ghi chú</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {recent.map((r) => (
                  <TableRow key={r.id}>
                    <TableCell className="tabular-nums">{r.attendanceDate}</TableCell>
                    <TableCell>{ATTENDANCE_STATUS_LABELS[r.status] ?? r.status}</TableCell>
                    <TableCell className="text-muted-foreground">{r.remarks || '—'}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

// ---- Fees ----------------------------------------------------------
function FeesTab({ studentId, studentName }) {
  const feesQuery = useQuery({
    queryKey: ['portal', 'fees', studentId],
    queryFn: () => feeService.getByStudent(studentId).then((r) => r.data),
    enabled: Boolean(studentId),
  });
  const duesQuery = useQuery({
    queryKey: ['portal', 'fee-dues', studentId],
    queryFn: () => feeService.getTotalDues(studentId).then((r) => r.data),
    enabled: Boolean(studentId),
  });
  const fees = feesQuery.data ?? [];

  const downloadReceipt = useMutation({
    mutationFn: (fee) =>
      triggerBlobDownload(
        reportService.feeReceipt(fee.id),
        `bien-lai-${(studentName ?? 'hoc-sinh').replace(/\s+/g, '-')}-${fee.id}.pdf`
      ),
    onError: (err) => toast.error(err.message || 'Không thể tải biên lai'),
  });

  if (feesQuery.isLoading) return <TableRowsSkeleton rows={6} columns={5} />;

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader className="pb-2">
          <CardDescription>Tổng công nợ còn lại</CardDescription>
          <CardTitle className="text-3xl tabular-nums text-rose-600">
            {currencyVND(duesQuery.data ?? 0)}
          </CardTitle>
        </CardHeader>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Các khoản thu</CardTitle>
        </CardHeader>
        <CardContent>
          {fees.length === 0 ? (
            <p className="text-sm text-muted-foreground">Chưa có khoản thu nào.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Khoản thu</TableHead>
                  <TableHead className="text-right">Số tiền</TableHead>
                  <TableHead className="text-right">Đã nộp</TableHead>
                  <TableHead className="text-right">Còn nợ</TableHead>
                  <TableHead>Hạn nộp</TableHead>
                  <TableHead>Trạng thái</TableHead>
                  <TableHead className="text-right">Biên lai</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {fees.map((f) => (
                  <TableRow key={f.id}>
                    <TableCell>{f.feeType}</TableCell>
                    <TableCell className="text-right tabular-nums">{currencyVND(f.amount)}</TableCell>
                    <TableCell className="text-right tabular-nums">{currencyVND(f.paidAmount)}</TableCell>
                    <TableCell className="text-right tabular-nums">{currencyVND(f.remainingAmount)}</TableCell>
                    <TableCell className="tabular-nums">{f.dueDate || '—'}</TableCell>
                    <TableCell>
                      <Badge variant={f.status === 'PAID' ? 'secondary' : f.status === 'OVERDUE' ? 'destructive' : 'outline'}>
                        {FEE_STATUS_LABELS[f.status] ?? f.status}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      {/* The receipt endpoint 400s for a fee with no payment
                          recorded - only offer it once something's been paid. */}
                      {(f.paidAmount ?? 0) > 0 ? (
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => downloadReceipt.mutate(f)}
                          disabled={downloadReceipt.isPending}
                          aria-label={`Tải biên lai ${f.feeType}`}
                        >
                          <FiDownload className="h-4 w-4" />
                        </Button>
                      ) : (
                        <span className="text-muted-foreground">—</span>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

// ---- Conduct -----------------------------------------------------------
function ConductTab({ studentId }) {
  const conductQuery = useQuery({
    queryKey: ['portal', 'conduct', studentId],
    queryFn: () => conductService.getByStudent(studentId).then((r) => r.data),
    enabled: Boolean(studentId),
  });
  const records = conductQuery.data ?? [];

  if (conductQuery.isLoading) return <TableRowsSkeleton rows={4} columns={3} />;

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">Hạnh kiểm theo học kỳ</CardTitle>
      </CardHeader>
      <CardContent>
        {records.length === 0 ? (
          <p className="text-sm text-muted-foreground">Chưa có đánh giá hạnh kiểm.</p>
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Học kỳ</TableHead>
                <TableHead>Xếp loại</TableHead>
                <TableHead>Nhận xét</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {records.map((c) => (
                <TableRow key={c.id}>
                  <TableCell>{c.semesterLabel}</TableCell>
                  <TableCell>{CONDUCT_RATING_LABELS[c.rating] ?? c.rating}</TableCell>
                  <TableCell className="text-muted-foreground">{c.remarks || '—'}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </CardContent>
    </Card>
  );
}

export default SelfServicePortal;
