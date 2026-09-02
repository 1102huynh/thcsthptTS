import React, { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { format, subDays } from 'date-fns';
import { FiCheck, FiSave, FiDownload } from 'react-icons/fi';
import { attendanceService, schoolClassService, studentService, reportService } from '../services/dataService';
import { triggerBlobDownload } from '../lib/download';
import { getCurrentUser } from '../services/authService';
import DatePicker from '../components/shared/DatePicker';
import { TableRowsSkeleton } from '../components/shared/Skeleton';
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

const ISO_DATE = 'yyyy-MM-dd';
const RATE_WINDOW_DAYS = 30;

function classKey(c) { return `${c.className}|${c.section}`; }

function AttendanceManagement() {
  const queryClient = useQueryClient();
  // Mức 2.1 (v4.9): PRINCIPAL reaches this page read-only - attendance GETs
  // now allow PRINCIPAL, but POST /v1/attendance* still 403s them, so the
  // mark/save controls and the roster checkboxes are disabled.
  const readOnly = getCurrentUser()?.role === 'PRINCIPAL';
  const [selectedKey, setSelectedKey] = useState('');
  const [date, setDate] = useState(new Date());
  const [presentIds, setPresentIds] = useState(new Set());

  const dateStr = format(date, ISO_DATE);

  const classesQuery = useQuery({
    queryKey: ['classes'],
    queryFn: () => schoolClassService.getAll().then((r) => r.data),
  });

  useEffect(() => {
    if (!selectedKey && classesQuery.data?.length) {
      setSelectedKey(classKey(classesQuery.data[0]));
    }
  }, [classesQuery.data, selectedKey]);

  const selectedClass = classesQuery.data?.find((c) => classKey(c) === selectedKey);

  const rosterQuery = useQuery({
    queryKey: ['attendance-roster', selectedClass?.className, selectedClass?.section],
    queryFn: () => studentService.getByClass(selectedClass.className, selectedClass.section).then((r) => r.data),
    enabled: Boolean(selectedClass),
  });

  const dayAttendanceQuery = useQuery({
    queryKey: ['attendance-by-date', dateStr],
    queryFn: () => attendanceService.getByDate(dateStr).then((r) => r.data),
    enabled: Boolean(selectedClass),
  });

  const roster = rosterQuery.data ?? [];
  const rosterIds = useMemo(() => new Set(roster.map((s) => s.id)), [roster]);

  // Existing records for this exact class+date, if any (school-wide result
  // filtered down to this roster) - drives both the pre-filled checkboxes
  // and the "already marked, this will replace it" notice.
  const existingForClass = useMemo(
    () => (dayAttendanceQuery.data ?? []).filter((a) => rosterIds.has(a.studentId)),
    [dayAttendanceQuery.data, rosterIds]
  );
  const alreadyMarked = existingForClass.length > 0;

  // Re-seed the checkbox state whenever the roster or the day's existing
  // records change (new class picked, date changed, or a fresh fetch) -
  // default to everyone present when nothing's recorded yet, since
  // unchecking absentees is faster than checking everyone individually.
  useEffect(() => {
    if (!roster.length) {
      setPresentIds(new Set());
      return;
    }
    if (existingForClass.length > 0) {
      setPresentIds(new Set(existingForClass.filter((a) => a.status === 'PRESENT').map((a) => a.studentId)));
    } else {
      setPresentIds(new Set(roster.map((s) => s.id)));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [roster, existingForClass]);

  const rateStart = format(subDays(new Date(), RATE_WINDOW_DAYS - 1), ISO_DATE);
  const rateEnd = format(new Date(), ISO_DATE);
  const rateQuery = useQuery({
    queryKey: ['attendance-rate', selectedClass?.className, selectedClass?.section, rateStart, rateEnd],
    queryFn: () => attendanceService.getBetweenDates(rateStart, rateEnd).then((r) => r.data),
    enabled: Boolean(selectedClass),
  });
  const classRate = useMemo(() => {
    const records = (rateQuery.data ?? []).filter((a) => rosterIds.has(a.studentId));
    if (!records.length) return null;
    const present = records.filter((a) => a.status === 'PRESENT').length;
    return Math.round((present / records.length) * 1000) / 10;
  }, [rateQuery.data, rosterIds]);

  const markMutation = useMutation({
    mutationFn: () =>
      attendanceService.markClass({
        className: selectedClass.className,
        section: selectedClass.section,
        date: dateStr,
        presentStudentIds: [...presentIds],
        status: 'ABSENT',
      }),
    onSuccess: () => {
      toast.success(alreadyMarked ? 'Đã cập nhật điểm danh' : 'Đã lưu điểm danh');
      queryClient.invalidateQueries({ queryKey: ['attendance-by-date', dateStr] });
      queryClient.invalidateQueries({ queryKey: ['attendance-rate'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-attendance-trend'] });
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể lưu điểm danh'),
  });

  const downloadAttendanceMutation = useMutation({
    mutationFn: ({ classId, from, to }) =>
      triggerBlobDownload(
        reportService.classAttendance(classId, from, to),
        `diem-danh-${selectedClass.className}-${selectedClass.section}-${from}_${to}.xlsx`
      ),
    onError: (err) => toast.error(err.message || 'Không thể xuất file điểm danh'),
  });

  const toggle = (studentId) => {
    setPresentIds((prev) => {
      const next = new Set(prev);
      if (next.has(studentId)) next.delete(studentId);
      else next.add(studentId);
      return next;
    });
  };

  const toggleAll = (checked) => {
    setPresentIds(checked ? new Set(roster.map((s) => s.id)) : new Set());
  };

  const loading = rosterQuery.isLoading || dayAttendanceQuery.isLoading;

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold">Điểm danh</h1>
        <p className="text-sm text-muted-foreground">Điểm danh theo lớp và ngày</p>
      </div>

      {readOnly && (
        <div className="rounded-lg border bg-muted/50 px-4 py-2 text-sm text-muted-foreground">
          Chế độ chỉ xem (Hiệu trưởng) — việc điểm danh do giáo viên thực hiện.
        </div>
      )}

      <Card>
        <CardContent className="grid grid-cols-1 gap-4 p-5 sm:grid-cols-2">
          <div className="space-y-1.5">
            {/* Explicit htmlFor/id - a bare <label> next to a control with
                no association silently fails to reach it for screen
                readers (and, empirically, for Playwright's getByLabel) -
                same class of gap fixed in FormFields.jsx's SelectField
                yesterday, recurring here because this filter bar isn't
                inside a react-hook-form context so it can't reuse it. */}
            <label htmlFor="attendance-class-select" className="text-sm font-medium">Lớp</label>
            <Select value={selectedKey} onValueChange={setSelectedKey}>
              <SelectTrigger id="attendance-class-select">
                <SelectValue placeholder="Chọn lớp" />
              </SelectTrigger>
              <SelectContent>
                {(classesQuery.data ?? []).map((c) => (
                  <SelectItem key={classKey(c)} value={classKey(c)}>
                    {c.className} - {c.section} ({c.studentCount ?? 0} học sinh)
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1.5">
            <label htmlFor="attendance-date-picker" className="text-sm font-medium">Ngày</label>
            <DatePicker id="attendance-date-picker" value={date} onChange={(d) => d && setDate(d)} />
          </div>
        </CardContent>
      </Card>

      {selectedClass && (
        <Card>
          <CardHeader className="flex flex-col gap-3 space-y-0 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <CardTitle>
                {selectedClass.className} - {selectedClass.section} · {format(date, 'dd/MM/yyyy')}
              </CardTitle>
              <CardDescription>
                {presentIds.size}/{roster.length} có mặt
                {classRate != null && ` · Chuyên cần ${RATE_WINDOW_DAYS} ngày qua: ${classRate}%`}
              </CardDescription>
            </div>
            <div className="flex items-center gap-2">
              {alreadyMarked && <Badge variant="secondary">Đã điểm danh</Badge>}
              <Button
                variant="outline"
                onClick={() =>
                  downloadAttendanceMutation.mutate({
                    classId: selectedClass.id,
                    from: rateStart,
                    to: rateEnd,
                  })
                }
                disabled={downloadAttendanceMutation.isPending}
              >
                <FiDownload className="mr-2 h-4 w-4" />
                {downloadAttendanceMutation.isPending ? 'Đang tải...' : `Xuất Excel (${RATE_WINDOW_DAYS} ngày qua)`}
              </Button>
              {!readOnly && (
                <Button
                  onClick={() => markMutation.mutate()}
                  disabled={markMutation.isPending || loading || roster.length === 0}
                >
                  <FiSave className="mr-2 h-4 w-4" />
                  {markMutation.isPending ? 'Đang lưu...' : alreadyMarked ? 'Cập nhật điểm danh' : 'Lưu điểm danh'}
                </Button>
              )}
            </div>
          </CardHeader>
          <CardContent>
            {loading ? (
              // Same header + column shape as the real roster table below,
              // body rows swapped for shimmering placeholders (Tuần 5
              // Ngày 5) instead of a bare "Đang tải..." string.
              <div className="overflow-x-auto rounded-md border">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b bg-muted/50">
                      <th className="w-12 p-2" />
                      <th className="p-2 text-left font-medium text-muted-foreground">Số báo danh</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Họ tên</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Trạng thái</th>
                    </tr>
                  </thead>
                  <tbody>
                    <TableRowsSkeleton rows={6} columns={4} />
                  </tbody>
                </table>
              </div>
            ) : roster.length === 0 ? (
              <p className="py-8 text-center text-sm text-muted-foreground">Lớp này chưa có học sinh nào.</p>
            ) : (
              <div className="overflow-x-auto rounded-md border">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b bg-muted/50">
                      <th className="w-12 p-2">
                        <button
                          type="button"
                          onClick={() => toggleAll(presentIds.size < roster.length)}
                          disabled={readOnly}
                          className="flex h-5 w-5 items-center justify-center rounded border border-input hover:bg-accent disabled:opacity-50"
                          aria-label="Chọn/bỏ chọn tất cả"
                        >
                          {presentIds.size === roster.length && <FiCheck className="h-4 w-4" />}
                        </button>
                      </th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Số báo danh</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Họ tên</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Trạng thái</th>
                    </tr>
                  </thead>
                  <tbody>
                    {roster.map((s) => {
                      const present = presentIds.has(s.id);
                      return (
                        <tr key={s.id} className="border-b last:border-0 hover:bg-muted/30">
                          <td className="p-2">
                            <button
                              type="button"
                              onClick={() => toggle(s.id)}
                              disabled={readOnly}
                              className="flex h-5 w-5 items-center justify-center rounded border border-input hover:bg-accent disabled:opacity-50"
                              aria-label={`Điểm danh ${s.user?.firstName} ${s.user?.lastName}`}
                            >
                              {present && <FiCheck className="h-4 w-4" />}
                            </button>
                          </td>
                          <td className="p-2">{s.rollNumber}</td>
                          <td className="p-2">
                            {s.user?.firstName} {s.user?.lastName}
                          </td>
                          <td className="p-2">
                            <Badge variant={present ? 'default' : 'secondary'}>
                              {present ? 'Có mặt' : 'Vắng'}
                            </Badge>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}

export default AttendanceManagement;
