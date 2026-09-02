import React, { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { FiSave, FiDownload } from 'react-icons/fi';
import {
  gradeRecordService,
  gradeConfigService,
  schoolClassService,
  studentService,
  academicYearService,
  semesterService,
  subjectService,
  staffService,
  reportService,
} from '../services/dataService';
import { triggerBlobDownload } from '../lib/download';
import { getCurrentUser } from '../services/authService';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import { TableRowsSkeleton } from '../components/shared/Skeleton';
import { GRADE_COMPONENT_TYPE_LABELS, SEMESTER_NAME_LABELS } from '../lib/enumLabels';

const COMPONENT_TYPES = Object.keys(GRADE_COMPONENT_TYPE_LABELS);
// Matches the old page's default (EXAM_TYPES[3] = 'Giữa kỳ') so a teacher
// used to the previous model lands on a familiar default.
const DEFAULT_COMPONENT_TYPE = 'GIUA_KY';

function classKey(c) { return `${c.className}|${c.section}`; }

function extractStartYear(academicYearName) {
  const m = /^(\d{4})-\d{4}$/.exec(academicYearName ?? '');
  return m ? Number(m[1]) : null;
}

// Mirrors GradeRecordService.resolveWeight() server-side exactly: the
// config row for this componentType with the latest appliesFrom <= the
// target academic year.
function resolveWeight(componentType, academicYearName, configs) {
  const target = extractStartYear(academicYearName);
  if (target == null) return null;
  return configs
    .filter((c) => c.componentType === componentType && extractStartYear(c.appliesFrom) <= target)
    .sort((a, b) => extractStartYear(b.appliesFrom) - extractStartYear(a.appliesFrom))[0]?.weight ?? null;
}

// Mirrors GradeRecordService.calculateWeightedAverage(): Σ(score × weight)
// / Σ(weight). Returns null (not a partial guess) if any component type
// present has no matching config - the real backend summary endpoint would
// 404 in that same situation (resolveWeight throws), so a silent partial
// average here would just be wrong, not merely incomplete.
function weightedAverage(records, academicYearName, configs) {
  if (!records.length) return null;
  let weightedSum = 0;
  let weightSum = 0;
  for (const r of records) {
    const weight = resolveWeight(r.componentType, academicYearName, configs);
    if (weight == null) return null;
    weightedSum += r.score * weight;
    weightSum += weight;
  }
  return weightSum > 0 ? Math.round((weightedSum / weightSum) * 100) / 100 : null;
}

function semesterLabel(s) {
  return `${s.academicYearName} - ${SEMESTER_NAME_LABELS[s.name] ?? s.name}`;
}

function GradeManagement() {
  const queryClient = useQueryClient();
  // Mức 2.1 (v4.9): PRINCIPAL reaches this page read-only - grade-record
  // GETs now allow PRINCIPAL, but every write 403s, so the save control is
  // hidden and the score inputs are locked.
  const readOnly = getCurrentUser()?.role === 'PRINCIPAL';
  const [selectedKey, setSelectedKey] = useState('');
  const [semesterId, setSemesterId] = useState('');
  const [subjectId, setSubjectId] = useState('');
  const [componentType, setComponentType] = useState(DEFAULT_COMPONENT_TYPE);
  const [scoreInput, setScoreInput] = useState({});

  const classesQuery = useQuery({ queryKey: ['classes'], queryFn: () => schoolClassService.getAll().then((r) => r.data) });
  useEffect(() => {
    if (!selectedKey && classesQuery.data?.length) setSelectedKey(classKey(classesQuery.data[0]));
  }, [classesQuery.data, selectedKey]);
  const selectedClass = classesQuery.data?.find((c) => classKey(c) === selectedKey);

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

  const subjectsQuery = useQuery({ queryKey: ['subjects'], queryFn: () => subjectService.getAll().then((r) => r.data) });
  useEffect(() => {
    if (!subjectId && subjectsQuery.data?.length) setSubjectId(String(subjectsQuery.data[0].id));
  }, [subjectsQuery.data, subjectId]);

  const gradeConfigsQuery = useQuery({ queryKey: ['grade-configs'], queryFn: () => gradeConfigService.getAll().then((r) => r.data) });

  const staffQuery = useQuery({ queryKey: ['staff-lookup'], queryFn: () => staffService.getAll().then((r) => r.data) });
  const myStaffId = staffQuery.data?.find((s) => s.user?.id === JSON.parse(localStorage.getItem('user') || '{}').userId)?.id;

  const rosterQuery = useQuery({
    queryKey: ['grade-roster', selectedClass?.className, selectedClass?.section],
    queryFn: () => studentService.getByClass(selectedClass.className, selectedClass.section).then((r) => r.data),
    enabled: Boolean(selectedClass),
  });
  const roster = rosterQuery.data ?? [];

  // Per-student grade records for the selected semester (every subject,
  // every component type) - no by-class bulk endpoint exists
  // (GradeRecordController only has per-student queries, see
  // gradeRecordService's comment in dataService.js), so fetch in parallel
  // for the roster, same "no bulk endpoint, fetch+filter" pattern as
  // teachingAssignmentService's getAll elsewhere in this app. Reused for
  // both the editable "Điểm" column (filtered to the
  // selected subject+componentType) and the read-only "TB môn HK" column
  // (all of that subject's component types).
  const rosterGradesQuery = useQuery({
    queryKey: ['grade-records-roster', semesterId, roster.map((s) => s.id).join(',')],
    queryFn: async () => {
      const entries = await Promise.all(
        roster.map(async (s) => {
          const res = await gradeRecordService.getStudentSemesterGrades(s.id, semesterId);
          return [s.id, res.data];
        })
      );
      return new Map(entries);
    },
    enabled: Boolean(semesterId) && roster.length > 0,
  });
  const gradesByStudent = rosterGradesQuery.data ?? new Map();

  const existingByStudentId = useMemo(() => {
    const map = new Map();
    for (const [studentId, records] of gradesByStudent) {
      const match = records.find((r) => String(r.subjectId) === subjectId && r.componentType === componentType);
      if (match) map.set(studentId, match);
    }
    return map;
  }, [gradesByStudent, subjectId, componentType]);

  // Re-seed the input state whenever the roster/subject/componentType
  // selection changes - pre-fill from existing records, blank otherwise.
  useEffect(() => {
    const next = {};
    for (const s of roster) {
      const existing = existingByStudentId.get(s.id);
      next[s.id] = existing ? String(existing.score) : '';
    }
    setScoreInput(next);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [roster, existingByStudentId]);

  const saveMutation = useMutation({
    mutationFn: async () => {
      const entries = Object.entries(scoreInput).filter(([, v]) => v !== '' && v != null);
      const results = await Promise.allSettled(
        entries.map(([studentId, score]) => {
          const existing = existingByStudentId.get(Number(studentId));
          const payload = {
            student: { id: Number(studentId) },
            subject: { id: Number(subjectId) },
            semester: { id: Number(semesterId) },
            componentType,
            score: Number(score),
            teacher: { id: existing?.teacherId ?? myStaffId },
            remarks: existing?.remarks ?? null,
          };
          return existing ? gradeRecordService.update(existing.id, payload) : gradeRecordService.create(payload);
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
      queryClient.invalidateQueries({ queryKey: ['grade-records-roster'] });
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể lưu điểm'),
  });

  const downloadTranscriptMutation = useMutation({
    mutationFn: ({ studentId, studentName }) =>
      triggerBlobDownload(
        reportService.studentTranscript(studentId, selectedSemester.academicYearId),
        `hoc-ba-${studentName.replace(/\s+/g, '-')}-${selectedSemester.academicYearName}.pdf`
      ),
    onError: (err) => toast.error(err.message || 'Không thể tải học bạ'),
  });

  const filledCount = Object.values(scoreInput).filter((v) => v !== '' && v != null).length;
  const configsMissing = Boolean(
    selectedSemester &&
      COMPONENT_TYPES.some((t) => resolveWeight(t, selectedSemester.academicYearName, gradeConfigsQuery.data ?? []) == null)
  );
  const loading = rosterQuery.isLoading || rosterGradesQuery.isLoading;
  const selectedSubjectName = subjectsQuery.data?.find((s) => String(s.id) === subjectId)?.name;

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold">Bảng điểm</h1>
        <p className="text-sm text-muted-foreground">
          Nhập điểm theo lớp, môn học và học kỳ — theo Thông tư 22/2021 (thang điểm 10)
        </p>
      </div>

      {!activeYear && academicYearsQuery.isSuccess && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive dark:text-red-400">
          Chưa có năm học đang hoạt động.
        </div>
      )}

      {readOnly && (
        <div className="rounded-lg border bg-muted/50 px-4 py-2 text-sm text-muted-foreground">
          Chế độ chỉ xem (Hiệu trưởng) — việc nhập/sửa điểm do giáo viên bộ môn thực hiện.
        </div>
      )}

      {!myStaffId && !readOnly && staffQuery.isSuccess && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive dark:text-red-400">
          Tài khoản của bạn chưa được liên kết với hồ sơ nhân sự nên không thể lưu điểm mới (vẫn xem được bảng điểm).
        </div>
      )}

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
            <label htmlFor="grade-semester-select" className="text-sm font-medium">Học kỳ</label>
            <Select value={semesterId} onValueChange={setSemesterId}>
              <SelectTrigger id="grade-semester-select">
                <SelectValue placeholder={semestersQuery.isLoading ? 'Đang tải...' : 'Chọn học kỳ'} />
              </SelectTrigger>
              <SelectContent>
                {(semestersQuery.data ?? []).map((s) => (
                  <SelectItem key={s.id} value={String(s.id)}>{semesterLabel(s)}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1.5">
            <label htmlFor="grade-subject-select" className="text-sm font-medium">Môn học</label>
            <Select value={subjectId} onValueChange={setSubjectId}>
              <SelectTrigger id="grade-subject-select">
                <SelectValue placeholder={subjectsQuery.isLoading ? 'Đang tải...' : 'Chọn môn học'} />
              </SelectTrigger>
              <SelectContent>
                {(subjectsQuery.data ?? []).map((s) => (
                  <SelectItem key={s.id} value={String(s.id)}>{s.name}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1.5">
            <label htmlFor="grade-component-select" className="text-sm font-medium">Loại điểm</label>
            <Select value={componentType} onValueChange={setComponentType}>
              <SelectTrigger id="grade-component-select">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {COMPONENT_TYPES.map((t) => (
                  <SelectItem key={t} value={t}>{GRADE_COMPONENT_TYPE_LABELS[t]}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {configsMissing && (
        <div className="rounded-lg border border-amber-500/30 bg-amber-500/10 px-4 py-3 text-sm text-amber-700 dark:text-amber-400">
          Chưa cấu hình đủ hệ số cho tất cả loại điểm ở năm học {selectedSemester.academicYearName} — cột "TB môn HK"
          sẽ để trống cho tới khi cấu hình đủ 5 loại tại trang Cấu hình học tập.
        </div>
      )}

      {selectedClass && selectedSemester && (
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0">
            <div>
              <CardTitle>
                {selectedClass.className} - {selectedClass.section} · {selectedSubjectName} · {GRADE_COMPONENT_TYPE_LABELS[componentType]}
              </CardTitle>
              <CardDescription>
                {filledCount}/{roster.length} đã nhập điểm · {semesterLabel(selectedSemester)}
              </CardDescription>
            </div>
            {!readOnly && (
              <Button
                onClick={() => saveMutation.mutate()}
                disabled={saveMutation.isPending || loading || filledCount === 0 || !myStaffId}
              >
                <FiSave className="mr-2 h-4 w-4" />
                {saveMutation.isPending ? 'Đang lưu...' : 'Lưu bảng điểm'}
              </Button>
            )}
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
                      <th className="p-2 text-left font-medium text-muted-foreground">TB môn HK</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Học bạ</th>
                    </tr>
                  </thead>
                  <tbody>
                    <TableRowsSkeleton rows={6} columns={5} />
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
                      <th className="p-2 text-left font-medium text-muted-foreground">TB môn HK</th>
                      <th className="p-2 text-left font-medium text-muted-foreground">Học bạ</th>
                    </tr>
                  </thead>
                  <tbody>
                    {roster.map((s) => {
                      const subjectRecords = (gradesByStudent.get(s.id) ?? []).filter((r) => String(r.subjectId) === subjectId);
                      const avg = weightedAverage(subjectRecords, selectedSemester.academicYearName, gradeConfigsQuery.data ?? []);
                      return (
                        <tr key={s.id} className="border-b last:border-0 hover:bg-muted/30">
                          <td className="p-2">{s.rollNumber}</td>
                          <td className="p-2">{s.user?.firstName} {s.user?.lastName}</td>
                          <td className="p-2">
                            <Input
                              type="number"
                              min="0"
                              max="10"
                              step="0.1"
                              value={scoreInput[s.id] ?? ''}
                              onChange={(e) =>
                                setScoreInput((prev) => ({ ...prev, [s.id]: e.target.value }))
                              }
                              className="h-8 w-24"
                              aria-label={`Điểm ${s.user?.firstName} ${s.user?.lastName}`}
                              readOnly={readOnly}
                            />
                          </td>
                          <td className="p-2">
                            {avg != null ? (
                              <span className="font-medium">{avg}</span>
                            ) : (
                              <span className="text-muted-foreground">—</span>
                            )}
                          </td>
                          <td className="p-2">
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() =>
                                downloadTranscriptMutation.mutate({
                                  studentId: s.id,
                                  studentName: `${s.user?.firstName ?? ''} ${s.user?.lastName ?? ''}`.trim() || `hs${s.id}`,
                                })
                              }
                              disabled={downloadTranscriptMutation.isPending}
                              aria-label={`Tải học bạ ${s.user?.firstName} ${s.user?.lastName}`}
                            >
                              <FiDownload className="h-4 w-4" />
                            </Button>
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
