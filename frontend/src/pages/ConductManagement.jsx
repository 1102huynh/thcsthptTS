import React, { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { FiSave } from 'react-icons/fi';
import {
  conductService,
  schoolClassService,
  academicYearService,
  semesterService,
  staffService,
} from '../services/dataService';
import { getCurrentUser } from '../services/authService';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import { TableRowsSkeleton } from '../components/shared/Skeleton';
import { CONDUCT_RATING_LABELS, SEMESTER_NAME_LABELS, toOptions } from '../lib/enumLabels';

const RATING_OPTIONS = toOptions(CONDUCT_RATING_LABELS);

function semesterLabel(s) {
  return `${s.academicYearName} - ${SEMESTER_NAME_LABELS[s.name] ?? s.name}`;
}

function ConductManagement() {
  const queryClient = useQueryClient();
  const role = getCurrentUser()?.role;
  const [classId, setClassId] = useState('');
  const [semesterId, setSemesterId] = useState('');
  const [ratingInputs, setRatingInputs] = useState({});
  const [remarksInputs, setRemarksInputs] = useState({});

  const staffQuery = useQuery({ queryKey: ['staff-lookup'], queryFn: () => staffService.getAll().then((r) => r.data) });
  const myStaffId = staffQuery.data?.find((s) => s.user?.id === JSON.parse(localStorage.getItem('user') || '{}').userId)?.id;

  const classesQuery = useQuery({ queryKey: ['classes'], queryFn: () => schoolClassService.getAll().then((r) => r.data) });
  // A TEACHER may only record conduct for the class(es) they are GVCN
  // (homeroom teacher) of - ConductRecordService.enforceHomeroomWriteAccess
  // 403s any other class server-side, so narrowing the picker here avoids
  // setting a teacher up for a guaranteed-403 pick, same reasoning as the
  // nav's own role-scoping comments elsewhere in this app.
  const visibleClasses = useMemo(() => {
    const all = classesQuery.data ?? [];
    if (role !== 'TEACHER') return all;
    return all.filter((c) => c.classTeacherId === myStaffId);
  }, [classesQuery.data, role, myStaffId]);

  useEffect(() => {
    if (!classId && visibleClasses.length) setClassId(String(visibleClasses[0].id));
  }, [visibleClasses, classId]);
  const selectedClass = visibleClasses.find((c) => String(c.id) === classId);

  const academicYearsQuery = useQuery({ queryKey: ['academic-years'], queryFn: () => academicYearService.getAll().then((r) => r.data) });
  const activeYear = academicYearsQuery.data?.find((y) => y.status === 'ACTIVE');

  const semestersQuery = useQuery({
    queryKey: ['semesters-by-year', activeYear?.id],
    queryFn: () => semesterService.getByAcademicYear(activeYear.id).then((r) => r.data),
    enabled: Boolean(activeYear),
  });
  useEffect(() => {
    if (!semesterId && semestersQuery.data?.length) setSemesterId(String(semestersQuery.data[0].id));
  }, [semestersQuery.data, semesterId]);
  const selectedSemester = semestersQuery.data?.find((s) => String(s.id) === semesterId);

  const rosterQuery = useQuery({
    queryKey: ['conduct-roster', classId, semesterId],
    queryFn: () => conductService.getClassSemesterRoster(classId, semesterId).then((r) => r.data),
    enabled: Boolean(classId) && Boolean(semesterId),
  });
  const roster = rosterQuery.data ?? [];

  // Re-seed the input state whenever the roster changes - pre-fill from
  // each entry's existing rating/remarks (null for a not-yet-evaluated
  // student), same pattern as GradeManagement's marksInput/scoreInput.
  useEffect(() => {
    const nextRating = {};
    const nextRemarks = {};
    for (const entry of roster) {
      nextRating[entry.studentId] = entry.rating ?? '';
      nextRemarks[entry.studentId] = entry.remarks ?? '';
    }
    setRatingInputs(nextRating);
    setRemarksInputs(nextRemarks);
  }, [roster]);

  const rosterByStudentId = useMemo(() => new Map(roster.map((e) => [e.studentId, e])), [roster]);

  const saveMutation = useMutation({
    mutationFn: async () => {
      const entries = Object.entries(ratingInputs).filter(([, v]) => v !== '' && v != null);
      const results = await Promise.allSettled(
        entries.map(([studentId, rating]) => {
          const existing = rosterByStudentId.get(Number(studentId));
          const payload = {
            student: { id: Number(studentId) },
            semester: { id: Number(semesterId) },
            rating,
            remarks: remarksInputs[studentId] || null,
            evaluatedBy: { id: existing?.evaluatedById ?? myStaffId },
          };
          return existing?.conductRecordId
            ? conductService.update(existing.conductRecordId, payload)
            : conductService.create(payload);
        })
      );
      const failed = results.filter((r) => r.status === 'rejected');
      return { total: entries.length, failed: failed.length, failures: failed };
    },
    onSuccess: ({ total, failed, failures }) => {
      if (failed > 0) {
        const firstMsg = failures[0]?.reason?.response?.data?.message;
        toast.error(`Lưu ${total - failed}/${total} thành công, ${failed} bị lỗi${firstMsg ? `: ${firstMsg}` : ''}`);
      } else {
        toast.success(`Đã lưu hạnh kiểm cho ${total} học sinh`);
      }
      queryClient.invalidateQueries({ queryKey: ['conduct-roster'] });
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể lưu hạnh kiểm'),
  });

  const filledCount = Object.values(ratingInputs).filter((v) => v !== '' && v != null).length;
  const loading = rosterQuery.isLoading;

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold">Hạnh kiểm</h1>
        <p className="text-sm text-muted-foreground">Đánh giá hạnh kiểm/rèn luyện theo lớp và học kỳ</p>
      </div>

      {!activeYear && academicYearsQuery.isSuccess && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive dark:text-red-400">
          Chưa có năm học đang hoạt động.
        </div>
      )}

      {role === 'TEACHER' && classesQuery.isSuccess && visibleClasses.length === 0 && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive dark:text-red-400">
          Bạn chưa là giáo viên chủ nhiệm của lớp nào nên không có lớp nào để đánh giá hạnh kiểm.
        </div>
      )}

      {!myStaffId && staffQuery.isSuccess && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive dark:text-red-400">
          Tài khoản của bạn chưa được liên kết với hồ sơ nhân sự nên không thể lưu đánh giá mới (vẫn xem được bảng).
        </div>
      )}

      <Card>
        <CardContent className="grid grid-cols-1 gap-4 p-5 sm:grid-cols-2">
          <div className="space-y-1.5">
            <label htmlFor="conduct-class-select" className="text-sm font-medium">Lớp</label>
            <Select value={classId} onValueChange={setClassId}>
              <SelectTrigger id="conduct-class-select">
                <SelectValue placeholder="Chọn lớp" />
              </SelectTrigger>
              <SelectContent>
                {visibleClasses.map((c) => (
                  <SelectItem key={c.id} value={String(c.id)}>{c.className} - {c.section}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1.5">
            <label htmlFor="conduct-semester-select" className="text-sm font-medium">Học kỳ</label>
            <Select value={semesterId} onValueChange={setSemesterId}>
              <SelectTrigger id="conduct-semester-select">
                <SelectValue placeholder={semestersQuery.isLoading ? 'Đang tải...' : 'Chọn học kỳ'} />
              </SelectTrigger>
              <SelectContent>
                {(semestersQuery.data ?? []).map((s) => (
                  <SelectItem key={s.id} value={String(s.id)}>{semesterLabel(s)}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {selectedClass && selectedSemester && (
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0">
            <div>
              <CardTitle>{selectedClass.className} - {selectedClass.section}</CardTitle>
              <CardDescription>
                {filledCount}/{roster.length} đã đánh giá · {semesterLabel(selectedSemester)}
              </CardDescription>
            </div>
            <Button
              onClick={() => saveMutation.mutate()}
              disabled={saveMutation.isPending || loading || filledCount === 0 || !myStaffId}
            >
              <FiSave className="mr-2 h-4 w-4" />
              {saveMutation.isPending ? 'Đang lưu...' : 'Lưu đánh giá'}
            </Button>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="overflow-x-auto rounded-md border">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b bg-muted/50">
                      <th className="p-2 text-left font-medium text-muted-foreground">Số báo danh</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Họ tên</th>
                      <th className="w-40 p-2 text-left font-medium text-muted-foreground">Xếp loại</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Ghi chú</th>
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
                      <th className="p-2 text-left font-medium text-muted-foreground">Số báo danh</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Họ tên</th>
                      <th className="w-40 p-2 text-left font-medium text-muted-foreground">Xếp loại</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Ghi chú</th>
                    </tr>
                  </thead>
                  <tbody>
                    {roster.map((entry) => (
                      <tr key={entry.studentId} className="border-b last:border-0 hover:bg-muted/30">
                        <td className="p-2">{entry.rollNumber}</td>
                        <td className="p-2">{entry.studentName}</td>
                        <td className="p-2">
                          <Select
                            value={ratingInputs[entry.studentId] ?? ''}
                            onValueChange={(v) => setRatingInputs((prev) => ({ ...prev, [entry.studentId]: v }))}
                          >
                            <SelectTrigger className="h-8 w-32" aria-label={`Xếp loại hạnh kiểm ${entry.studentName}`}>
                              <SelectValue placeholder="Chưa đánh giá" />
                            </SelectTrigger>
                            <SelectContent>
                              {RATING_OPTIONS.map((o) => (
                                <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                        </td>
                        <td className="p-2">
                          <Input
                            value={remarksInputs[entry.studentId] ?? ''}
                            onChange={(e) => setRemarksInputs((prev) => ({ ...prev, [entry.studentId]: e.target.value }))}
                            className="h-8"
                            placeholder="Ghi chú (không bắt buộc)"
                            aria-label={`Ghi chú hạnh kiểm ${entry.studentName}`}
                          />
                        </td>
                      </tr>
                    ))}
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

export default ConductManagement;
