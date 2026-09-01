import React, { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { FiSave } from 'react-icons/fi';
import {
  gradeService,
  schoolClassService,
  studentService,
  academicYearService,
  staffService,
} from '../services/dataService';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Badge } from '../components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import { TableRowsSkeleton } from '../components/shared/Skeleton';

const SUBJECTS = ['Toán', 'Ngữ văn', 'Tiếng Anh', 'Vật lý', 'Hóa học', 'Sinh học', 'Lịch sử', 'Địa lý', 'GDCD', 'Tin học', 'Thể dục', 'Công nghệ'];
const EXAM_TYPES = ['Miệng', '15 phút', '1 tiết', 'Giữa kỳ', 'Cuối kỳ'];
const DEFAULT_TOTAL_MARKS = 10;

function classKey(c) { return `${c.className}|${c.section}`; }

// Mirrors GradeService.calculateGrade() server-side exactly, for an
// immediate preview - the server recomputes authoritatively on save either
// way, this is just so the table doesn't sit blank until the round-trip.
function letterGrade(percentage) {
  if (percentage == null || Number.isNaN(percentage)) return null;
  if (percentage >= 90) return 'A+';
  if (percentage >= 80) return 'A';
  if (percentage >= 70) return 'B+';
  if (percentage >= 60) return 'B';
  if (percentage >= 50) return 'C';
  if (percentage >= 40) return 'D';
  return 'F';
}

function GradeManagement() {
  const queryClient = useQueryClient();
  const [selectedKey, setSelectedKey] = useState('');
  const [subject, setSubject] = useState(SUBJECTS[0]);
  const [examType, setExamType] = useState(EXAM_TYPES[3]);
  const [totalMarks, setTotalMarks] = useState(DEFAULT_TOTAL_MARKS);
  const [marksInput, setMarksInput] = useState({});

  const classesQuery = useQuery({ queryKey: ['classes'], queryFn: () => schoolClassService.getAll().then((r) => r.data) });
  useEffect(() => {
    if (!selectedKey && classesQuery.data?.length) setSelectedKey(classKey(classesQuery.data[0]));
  }, [classesQuery.data, selectedKey]);
  const selectedClass = classesQuery.data?.find((c) => classKey(c) === selectedKey);

  const academicYearsQuery = useQuery({ queryKey: ['academic-years'], queryFn: () => academicYearService.getAll().then((r) => r.data) });
  const academicYear = academicYearsQuery.data?.find((y) => y.status === 'ACTIVE')?.name;

  const staffQuery = useQuery({ queryKey: ['staff-lookup'], queryFn: () => staffService.getAll().then((r) => r.data) });

  const rosterQuery = useQuery({
    queryKey: ['grade-roster', selectedClass?.className, selectedClass?.section],
    queryFn: () => studentService.getByClass(selectedClass.className, selectedClass.section).then((r) => r.data),
    enabled: Boolean(selectedClass),
  });
  const roster = rosterQuery.data ?? [];
  const rosterIds = useMemo(() => new Set(roster.map((s) => s.id)), [roster]);

  // No "grades by class+subject" endpoint exists (GradeController only has
  // per-student and school-wide-by-year queries) - fetch the whole year's
  // grades (ADMIN/TEACHER only, matches this page's audience) and filter
  // client-side to this roster+subject+examType. Same pattern as
  // AttendanceManagement's getByDate. Fine at this school's data scale;
  // would need a real by-class endpoint at a much larger scale.
  const yearGradesQuery = useQuery({
    queryKey: ['grades-by-year', academicYear],
    queryFn: () => gradeService.getByYear(academicYear).then((r) => r.data),
    enabled: Boolean(academicYear),
  });

  const existingByStudentId = useMemo(() => {
    const map = new Map();
    for (const g of yearGradesQuery.data ?? []) {
      if (rosterIds.has(g.studentId) && g.subject === subject && g.examType === examType) {
        map.set(g.studentId, g);
      }
    }
    return map;
  }, [yearGradesQuery.data, rosterIds, subject, examType]);

  // Re-seed the input state whenever the roster/subject/examType selection
  // changes - pre-fill from existing records, blank otherwise.
  useEffect(() => {
    const next = {};
    for (const s of roster) {
      const existing = existingByStudentId.get(s.id);
      next[s.id] = existing ? String(existing.marksObtained) : '';
    }
    setMarksInput(next);
    const existingTotal = [...existingByStudentId.values()][0]?.totalMarks;
    if (existingTotal) setTotalMarks(existingTotal);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [roster, existingByStudentId]);

  const myStaffId = staffQuery.data?.find((s) => s.user?.id === JSON.parse(localStorage.getItem('user') || '{}').userId)?.id;

  const saveMutation = useMutation({
    mutationFn: async () => {
      const entries = Object.entries(marksInput).filter(([, v]) => v !== '' && v != null);
      const results = await Promise.allSettled(
        entries.map(([studentId, marks]) => {
          const existing = existingByStudentId.get(Number(studentId));
          const payload = existing
            ? {
                // GradeController's PUT deserializes into the raw Grade
                // entity with @Valid, which enforces its full constraint
                // set (student/subject/examType all @NotNull/@NotBlank)
                // even though GradeService.updateGrade only ever reads
                // marksObtained/totalMarks/remarks off this object -
                // omitting the rest 400s with "must not be null/blank".
                // Confirmed via a direct curl repro, not assumed. Sending
                // the record's own unchanged values satisfies validation
                // without actually changing anything the update ignores.
                student: { id: existing.studentId },
                subject: existing.subject,
                examType: existing.examType,
                marksObtained: Number(marks),
                totalMarks: Number(totalMarks),
                remarks: existing.remarks ?? null,
              }
            : {
                student: { id: Number(studentId) },
                subject,
                examType,
                marksObtained: Number(marks),
                totalMarks: Number(totalMarks),
                academicYear,
                ...(myStaffId ? { teacher: { id: myStaffId } } : {}),
              };
          return existing ? gradeService.updateGrade(existing.id, payload) : gradeService.createGrade(payload);
        })
      );
      const failed = results.filter((r) => r.status === 'rejected');
      return { total: entries.length, failed: failed.length };
    },
    onSuccess: ({ total, failed }) => {
      if (failed > 0) {
        toast.error(`Lưu ${total - failed}/${total} điểm thành công, ${failed} bị lỗi`);
      } else {
        toast.success(`Đã lưu điểm cho ${total} học sinh`);
      }
      queryClient.invalidateQueries({ queryKey: ['grades-by-year'] });
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể lưu điểm'),
  });

  const filledCount = Object.values(marksInput).filter((v) => v !== '' && v != null).length;
  const average = useMemo(() => {
    const values = Object.values(marksInput)
      .filter((v) => v !== '' && v != null)
      .map(Number)
      .filter((n) => !Number.isNaN(n));
    if (!values.length) return null;
    return Math.round((values.reduce((a, b) => a + b, 0) / values.length) * 100) / 100;
  }, [marksInput]);

  const loading = rosterQuery.isLoading || yearGradesQuery.isLoading;

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold">Bảng điểm</h1>
        <p className="text-sm text-muted-foreground">Nhập điểm theo lớp và môn học</p>
      </div>

      <Card>
        <CardContent className="grid grid-cols-1 gap-4 p-5 sm:grid-cols-2 lg:grid-cols-4">
          <div className="space-y-1.5">
            <label htmlFor="grade-class-select" className="text-sm font-medium">Lớp</label>
            <Select value={selectedKey} onValueChange={setSelectedKey}>
              <SelectTrigger id="grade-class-select">
                <SelectValue placeholder="Chọn lớp" />
              </SelectTrigger>
              <SelectContent>
                {(classesQuery.data ?? []).map((c) => (
                  <SelectItem key={classKey(c)} value={classKey(c)}>
                    {c.className} - {c.section}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1.5">
            <label htmlFor="grade-subject-select" className="text-sm font-medium">Môn học</label>
            <Select value={subject} onValueChange={setSubject}>
              <SelectTrigger id="grade-subject-select">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {SUBJECTS.map((s) => (
                  <SelectItem key={s} value={s}>{s}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1.5">
            <label htmlFor="grade-examtype-select" className="text-sm font-medium">Loại bài kiểm tra</label>
            <Select value={examType} onValueChange={setExamType}>
              <SelectTrigger id="grade-examtype-select">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {EXAM_TYPES.map((t) => (
                  <SelectItem key={t} value={t}>{t}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1.5">
            <label htmlFor="grade-total-marks" className="text-sm font-medium">Thang điểm</label>
            <Input
              id="grade-total-marks"
              type="number"
              min="1"
              value={totalMarks}
              onChange={(e) => setTotalMarks(e.target.value)}
            />
          </div>
        </CardContent>
      </Card>

      {selectedClass && (
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0">
            <div>
              <CardTitle>
                {selectedClass.className} - {selectedClass.section} · {subject} · {examType}
              </CardTitle>
              <CardDescription>
                {filledCount}/{roster.length} đã nhập điểm
                {average != null && ` · Điểm TB: ${average}`}
                {!academicYear && ' · Chưa có năm học đang hoạt động'}
              </CardDescription>
            </div>
            <Button
              onClick={() => saveMutation.mutate()}
              disabled={saveMutation.isPending || loading || filledCount === 0 || !academicYear}
            >
              <FiSave className="mr-2 h-4 w-4" />
              {saveMutation.isPending ? 'Đang lưu...' : 'Lưu bảng điểm'}
            </Button>
          </CardHeader>
          <CardContent>
            {loading ? (
              // Same header + column shape as the real grade-entry table
              // below (Tuần 5 Ngày 5), not a bare "Đang tải..." string.
              <div className="overflow-x-auto rounded-md border">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b bg-muted/50">
                      <th className="p-2 text-left font-medium text-muted-foreground">Số báo danh</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Họ tên</th>
                      <th className="w-32 p-2 text-left font-medium text-muted-foreground">Điểm</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Xếp loại</th>
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
                      <th className="w-32 p-2 text-left font-medium text-muted-foreground">Điểm</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Xếp loại</th>
                    </tr>
                  </thead>
                  <tbody>
                    {roster.map((s) => {
                      const raw = marksInput[s.id] ?? '';
                      const num = raw === '' ? null : Number(raw);
                      const pct = num != null && !Number.isNaN(num) ? (num / Number(totalMarks || 1)) * 100 : null;
                      const letter = letterGrade(pct);
                      return (
                        <tr key={s.id} className="border-b last:border-0 hover:bg-muted/30">
                          <td className="p-2">{s.rollNumber}</td>
                          <td className="p-2">{s.user?.firstName} {s.user?.lastName}</td>
                          <td className="p-2">
                            <Input
                              type="number"
                              min="0"
                              max={totalMarks || undefined}
                              step="0.1"
                              value={raw}
                              onChange={(e) =>
                                setMarksInput((prev) => ({ ...prev, [s.id]: e.target.value }))
                              }
                              className="h-8 w-24"
                              aria-label={`Điểm ${s.user?.firstName} ${s.user?.lastName}`}
                            />
                          </td>
                          <td className="p-2">
                            {letter ? (
                              <Badge variant={letter === 'F' ? 'destructive' : 'secondary'}>{letter}</Badge>
                            ) : (
                              <span className="text-muted-foreground">—</span>
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

export default GradeManagement;
