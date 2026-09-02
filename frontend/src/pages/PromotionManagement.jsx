import React, { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { FiSave } from 'react-icons/fi';
import {
  promotionService,
  schoolClassService,
  academicYearService,
  staffService,
} from '../services/dataService';
import { getCurrentUser } from '../services/authService';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Badge } from '../components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import { TableRowsSkeleton } from '../components/shared/Skeleton';
import { PROMOTION_DECISION_LABELS, CONDUCT_RATING_LABELS, toOptions } from '../lib/enumLabels';

const DECISION_OPTIONS = toOptions(PROMOTION_DECISION_LABELS);

function PromotionManagement() {
  const queryClient = useQueryClient();
  const role = getCurrentUser()?.role;
  // Preview is readable by ADMIN/PRINCIPAL/TEACHER (PromotionController),
  // but only ADMIN/PRINCIPAL may POST /v1/promotions/confirm - TEACHER gets
  // a read-only view of the same table, same canManage-gated-single-page
  // pattern as TimetableManagement.
  const canConfirm = role === 'ADMIN' || role === 'PRINCIPAL';

  const [academicYearId, setAcademicYearId] = useState('');
  const [classId, setClassId] = useState('');
  const [decisionInputs, setDecisionInputs] = useState({});
  const [remarksInputs, setRemarksInputs] = useState({});
  const [decidedById, setDecidedById] = useState('');

  const academicYearsQuery = useQuery({ queryKey: ['academic-years'], queryFn: () => academicYearService.getAll().then((r) => r.data) });
  useEffect(() => {
    if (!academicYearId && academicYearsQuery.data?.length) {
      const active = academicYearsQuery.data.find((y) => y.status === 'ACTIVE');
      setAcademicYearId(String((active ?? academicYearsQuery.data[0]).id));
    }
  }, [academicYearsQuery.data, academicYearId]);
  const selectedYear = academicYearsQuery.data?.find((y) => String(y.id) === academicYearId);

  const classesQuery = useQuery({ queryKey: ['classes'], queryFn: () => schoolClassService.getAll().then((r) => r.data) });
  // A class's own SchoolClass.academicYear (string) must match the selected
  // AcademicYear's name - PromotionService rejects a mismatched pair
  // outright (IllegalArgumentException, since the same className/section
  // can recur across years) - filtering the picker here avoids ever
  // sending that mismatched pair in the first place.
  const visibleClasses = useMemo(
    () => (classesQuery.data ?? []).filter((c) => c.academicYear === selectedYear?.name),
    [classesQuery.data, selectedYear]
  );
  useEffect(() => {
    if (visibleClasses.length && !visibleClasses.some((c) => String(c.id) === classId)) {
      setClassId(String(visibleClasses[0].id));
    }
  }, [visibleClasses, classId]);
  const selectedClass = visibleClasses.find((c) => String(c.id) === classId);

  const staffQuery = useQuery({ queryKey: ['staff-lookup'], queryFn: () => staffService.getAll().then((r) => r.data) });
  const myStaffId = staffQuery.data?.find((s) => s.user?.id === JSON.parse(localStorage.getItem('user') || '{}').userId)?.id;
  useEffect(() => {
    if (!decidedById && myStaffId) setDecidedById(String(myStaffId));
  }, [myStaffId, decidedById]);

  const previewQuery = useQuery({
    queryKey: ['promotion-preview', classId, academicYearId],
    queryFn: () => promotionService.getClassPreview(classId, academicYearId).then((r) => r.data),
    enabled: Boolean(classId) && Boolean(academicYearId),
  });
  const preview = previewQuery.data ?? [];

  // The preview endpoint is a live computation - it has no idea whether a
  // decision was already confirmed for a given student this year. Fetch
  // each student's full history in parallel and keep only this year's
  // entry (if any), same "no bulk endpoint, fetch+filter" pattern as
  // GradeManagement's per-student grade fetch - so an already-decided
  // student pre-fills their actual confirmed decision instead of just the
  // algorithm's suggestion.
  const historyQuery = useQuery({
    queryKey: ['promotion-history', academicYearId, preview.map((p) => p.studentId).join(',')],
    queryFn: async () => {
      const entries = await Promise.all(
        preview.map(async (p) => {
          const res = await promotionService.getStudentHistory(p.studentId);
          const existing = res.data.find((r) => String(r.academicYearId) === academicYearId);
          return [p.studentId, existing ?? null];
        })
      );
      return new Map(entries);
    },
    enabled: preview.length > 0,
  });
  const existingByStudentId = historyQuery.data ?? new Map();

  // Re-seed the input state whenever the preview/history change - pre-fill
  // from an existing confirmed decision if there is one, otherwise from the
  // algorithm's suggestion, blank if neither.
  useEffect(() => {
    if (!historyQuery.data) return;
    const nextDecision = {};
    const nextRemarks = {};
    for (const entry of preview) {
      const existing = existingByStudentId.get(entry.studentId);
      nextDecision[entry.studentId] = existing?.decision ?? entry.suggestedDecision ?? '';
      nextRemarks[entry.studentId] = existing?.remarks ?? '';
    }
    setDecisionInputs(nextDecision);
    setRemarksInputs(nextRemarks);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [historyQuery.data]);

  const confirmMutation = useMutation({
    mutationFn: async () => {
      const entries = Object.entries(decisionInputs).filter(([, v]) => v !== '' && v != null);
      const records = entries.map(([studentId, decision]) => ({
        student: { id: Number(studentId) },
        academicYear: { id: Number(academicYearId) },
        decision,
        remarks: remarksInputs[studentId] || null,
        decidedBy: { id: Number(decidedById) },
      }));
      const res = await promotionService.confirm(records);
      return res.data.length;
    },
    onSuccess: (count) => {
      toast.success(`Đã xác nhận quyết định cho ${count} học sinh`);
      queryClient.invalidateQueries({ queryKey: ['promotion-history'] });
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể xác nhận quyết định'),
  });

  const decidedCount = Object.values(decisionInputs).filter((v) => v !== '' && v != null).length;
  const loading = previewQuery.isLoading || (preview.length > 0 && historyQuery.isLoading);

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold">Xét lên lớp</h1>
        <p className="text-sm text-muted-foreground">
          Đề xuất xét lên lớp/ở lại/tốt nghiệp cuối năm — quyết định cuối cùng luôn do con người xác nhận
        </p>
      </div>

      <Card>
        <CardContent className="grid grid-cols-1 gap-4 p-5 sm:grid-cols-2 lg:grid-cols-3">
          <div className="space-y-1.5">
            <label htmlFor="promotion-year-select" className="text-sm font-medium">Năm học</label>
            <Select value={academicYearId} onValueChange={(v) => { setAcademicYearId(v); setClassId(''); }}>
              <SelectTrigger id="promotion-year-select">
                <SelectValue placeholder={academicYearsQuery.isLoading ? 'Đang tải...' : 'Chọn năm học'} />
              </SelectTrigger>
              <SelectContent>
                {(academicYearsQuery.data ?? []).map((y) => (
                  <SelectItem key={y.id} value={String(y.id)}>{y.name}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1.5">
            <label htmlFor="promotion-class-select" className="text-sm font-medium">Lớp</label>
            <Select value={classId} onValueChange={setClassId}>
              <SelectTrigger id="promotion-class-select">
                <SelectValue placeholder={visibleClasses.length ? 'Chọn lớp' : 'Không có lớp nào cho năm học này'} />
              </SelectTrigger>
              <SelectContent>
                {visibleClasses.map((c) => (
                  <SelectItem key={c.id} value={String(c.id)}>{c.className} - {c.section}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          {canConfirm && (
            <div className="space-y-1.5">
              <label htmlFor="promotion-decidedby-select" className="text-sm font-medium">Người quyết định</label>
              <Select value={decidedById} onValueChange={setDecidedById}>
                <SelectTrigger id="promotion-decidedby-select">
                  <SelectValue placeholder={staffQuery.isLoading ? 'Đang tải...' : 'Chọn người quyết định'} />
                </SelectTrigger>
                <SelectContent>
                  {(staffQuery.data ?? []).map((s) => (
                    <SelectItem key={s.id} value={String(s.id)}>{s.user?.firstName} {s.user?.lastName}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          )}
        </CardContent>
      </Card>

      {canConfirm && !decidedById && staffQuery.isSuccess && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive dark:text-red-400">
          Vui lòng chọn người quyết định trước khi xác nhận.
        </div>
      )}

      {selectedClass && selectedYear && (
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0">
            <div>
              <CardTitle>{selectedClass.className} - {selectedClass.section}</CardTitle>
              <CardDescription>
                {decidedCount}/{preview.length} có quyết định · Năm học {selectedYear.name}
              </CardDescription>
            </div>
            {canConfirm && (
              <Button
                onClick={() => confirmMutation.mutate()}
                disabled={confirmMutation.isPending || loading || decidedCount === 0 || !decidedById}
              >
                <FiSave className="mr-2 h-4 w-4" />
                {confirmMutation.isPending ? 'Đang xác nhận...' : 'Xác nhận quyết định'}
              </Button>
            )}
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="overflow-x-auto rounded-md border">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b bg-muted/50">
                      <th className="p-2 text-left font-medium text-muted-foreground">Số báo danh</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Họ tên</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">TB thấp nhất</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Hạnh kiểm</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Đi học</th>
                      <th className="w-44 p-2 text-left font-medium text-muted-foreground">Quyết định</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Ghi chú</th>
                    </tr>
                  </thead>
                  <tbody>
                    <TableRowsSkeleton rows={6} columns={7} />
                  </tbody>
                </table>
              </div>
            ) : preview.length === 0 ? (
              <p className="py-8 text-center text-sm text-muted-foreground">Lớp này chưa có học sinh nào.</p>
            ) : (
              <div className="overflow-x-auto rounded-md border">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b bg-muted/50">
                      <th className="p-2 text-left font-medium text-muted-foreground">Số báo danh</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Họ tên</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">TB thấp nhất</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Hạnh kiểm</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Đi học</th>
                      <th className="w-44 p-2 text-left font-medium text-muted-foreground">Quyết định</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Ghi chú</th>
                    </tr>
                  </thead>
                  <tbody>
                    {preview.map((entry) => {
                      const existing = existingByStudentId.get(entry.studentId);
                      return (
                        <tr key={entry.studentId} className="border-b last:border-0 hover:bg-muted/30">
                          <td className="p-2">{entry.rollNumber}</td>
                          <td className="p-2">{entry.studentName}</td>
                          <td className="p-2">
                            {entry.lowestSubjectAverage != null ? entry.lowestSubjectAverage : <span className="text-muted-foreground">—</span>}
                          </td>
                          <td className="p-2">
                            {entry.conduct ? CONDUCT_RATING_LABELS[entry.conduct] ?? entry.conduct : <span className="text-muted-foreground">—</span>}
                          </td>
                          <td className="p-2">
                            {entry.attendanceRate != null ? `${entry.attendanceRate}%` : <span className="text-muted-foreground">—</span>}
                          </td>
                          <td className="p-2">
                            {canConfirm ? (
                              <Select
                                value={decisionInputs[entry.studentId] ?? ''}
                                onValueChange={(v) => setDecisionInputs((prev) => ({ ...prev, [entry.studentId]: v }))}
                              >
                                <SelectTrigger className="h-8 w-36" aria-label={`Quyết định cho ${entry.studentName}`}>
                                  <SelectValue placeholder="Chưa quyết định" />
                                </SelectTrigger>
                                <SelectContent>
                                  {DECISION_OPTIONS.map((o) => (
                                    <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>
                                  ))}
                                </SelectContent>
                              </Select>
                            ) : existing?.decision ? (
                              // The already-confirmed decision takes priority over the
                              // live suggestion here (default variant, not "secondary" -
                              // this is a fact, not a suggestion) - a TEACHER's read-only
                              // badge showing the stale O_LAI suggestion while the note
                              // right below it says "Đã xác nhận: Lên lớp" would read as
                              // contradictory. Same priority the editable Select uses for
                              // canConfirm users above.
                              <Badge>{PROMOTION_DECISION_LABELS[existing.decision] ?? existing.decision}</Badge>
                            ) : entry.suggestedDecision ? (
                              <Badge variant="secondary">{PROMOTION_DECISION_LABELS[entry.suggestedDecision]}</Badge>
                            ) : (
                              <span className="text-muted-foreground">—</span>
                            )}
                            {existing && (
                              <p className="mt-1 text-xs text-muted-foreground">
                                Đã xác nhận: {PROMOTION_DECISION_LABELS[existing.decision] ?? existing.decision} bởi {existing.decidedByName}
                              </p>
                            )}
                            {!existing && entry.reasons?.length > 0 && (
                              <p className="mt-1 text-xs text-muted-foreground">{entry.reasons.join('; ')}</p>
                            )}
                          </td>
                          <td className="p-2">
                            {canConfirm ? (
                              <Input
                                value={remarksInputs[entry.studentId] ?? ''}
                                onChange={(e) => setRemarksInputs((prev) => ({ ...prev, [entry.studentId]: e.target.value }))}
                                className="h-8 w-40"
                                placeholder="Ghi chú"
                                aria-label={`Ghi chú xét lên lớp ${entry.studentName}`}
                              />
                            ) : (
                              existing?.remarks || <span className="text-muted-foreground">—</span>
                            )}
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

export default PromotionManagement;
