import React, { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { FiEdit2, FiTrash2, FiPlus } from 'react-icons/fi';
import { schoolClassService, semesterService, teachingAssignmentService, timetableService } from '../services/dataService';
import { getCurrentUser } from '../services/authService';
import { useMyHomeroomClasses } from '../hooks/useMyHomeroomClasses';
import TeachingAssignmentFormDialog from './timetable/TeachingAssignmentFormDialog';
import TimetableSlotFormDialog from './timetable/TimetableSlotFormDialog';
import { Button } from '../components/ui/button';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/card';
import { Label } from '../components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import { DAY_OF_WEEK_LABELS, SEMESTER_NAME_LABELS } from '../lib/enumLabels';
import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogCancel,
  AlertDialogAction,
} from '../components/ui/alert-dialog';

const DAYS = [2, 3, 4, 5, 6, 7];
const PERIODS = Array.from({ length: 10 }, (_, i) => i + 1);

/**
 * Thời khoá biểu - Giai đoạn 3.2 (Tuần 7-8 của plan). Two sections stacked
 * under one class+semester filter, matching the plan's module boundary
 * (Phân công giảng dạy & Thời khoá biểu are one module): "who teaches what
 * to this class this semester" (TeachingAssignment) has to exist before a
 * slot can reference it, so it's managed right above the grid that
 * consumes it rather than as a separate page.
 */
function TimetableManagement() {
  const queryClient = useQueryClient();
  const role = getCurrentUser()?.role;
  // Matches the backend exactly: TeachingAssignmentController/
  // TimetableController restrict every POST/PUT/DELETE to ADMIN/PRINCIPAL,
  // so only they ever see the add/edit/delete controls on this page.
  const canManage = role === 'ADMIN' || role === 'PRINCIPAL';
  // H.3.1 - thời khoá biểu do hiệu trưởng quyết định, TEACHER chỉ được xem
  // lịch dạy của chính mình (tiết mấy, lớp nào) chứ không browse theo lớp
  // bất kỳ nữa: GET /v1/timetable/class/{id} is ADMIN/PRINCIPAL-only now,
  // and GET /v1/timetable/teacher/{id} 403s a TEACHER passing any id but
  // their own - so this page swaps to a fixed "my own schedule" view for
  // TEACHER instead of the class+semester picker.
  const isTeacherView = role === 'TEACHER';

  const [classId, setClassId] = useState('');
  const [semesterId, setSemesterId] = useState('');

  const [assignmentDialogOpen, setAssignmentDialogOpen] = useState(false);
  const [editingAssignment, setEditingAssignment] = useState(null);
  const [deletingAssignment, setDeletingAssignment] = useState(null);

  const [slotDialogOpen, setSlotDialogOpen] = useState(false);
  const [editingSlot, setEditingSlot] = useState(null);
  const [presetCell, setPresetCell] = useState(null); // { dayOfWeek, period }
  const [deletingSlot, setDeletingSlot] = useState(null);

  // Only needed for the "am I linked to a Staff profile" (myStaffId) lookup
  // here - Timetable scoping is "my own teaching schedule", not homeroom, so
  // `homeroomClasses` itself is irrelevant to this page.
  const { myStaffId, isSuccess: myStaffLookupDone } = useMyHomeroomClasses();

  const classesQuery = useQuery({
    queryKey: ['classes'],
    queryFn: () => schoolClassService.getAll().then((r) => r.data),
    enabled: !isTeacherView,
  });
  const semestersQuery = useQuery({
    queryKey: ['semesters'],
    queryFn: () => semesterService.getAll().then((r) => r.data),
  });
  const assignmentsQuery = useQuery({
    queryKey: ['teaching-assignments'],
    queryFn: () => teachingAssignmentService.getAll().then((r) => r.data),
    enabled: !isTeacherView,
  });

  const selectedClass = classesQuery.data?.find((c) => String(c.id) === classId);
  const classLabel = selectedClass ? `${selectedClass.className} - ${selectedClass.section}` : '';

  const classAssignments = useMemo(
    () =>
      (assignmentsQuery.data ?? []).filter(
        (a) => classId && semesterId && String(a.schoolClassId) === classId && String(a.semesterId) === semesterId
      ),
    [assignmentsQuery.data, classId, semesterId]
  );
  const assignmentOptions = classAssignments.map((a) => ({
    value: String(a.id),
    label: `${a.subjectName} - ${a.teacherName}`,
  }));

  const classTimetableQuery = useQuery({
    queryKey: ['timetable', classId, semesterId],
    queryFn: () => timetableService.getByClass(classId, semesterId).then((r) => r.data),
    enabled: !isTeacherView && Boolean(classId && semesterId),
  });
  const teacherTimetableQuery = useQuery({
    queryKey: ['timetable-teacher', myStaffId, semesterId],
    queryFn: () => timetableService.getByTeacher(myStaffId, semesterId).then((r) => r.data),
    enabled: isTeacherView && Boolean(myStaffId && semesterId),
  });
  const timetableQuery = isTeacherView ? teacherTimetableQuery : classTimetableQuery;
  const slotByCell = useMemo(() => {
    const map = new Map();
    for (const slot of timetableQuery.data ?? []) {
      map.set(`${slot.dayOfWeek}-${slot.period}`, slot);
    }
    return map;
  }, [timetableQuery.data]);
  // Whether there's enough picked (class+semester for ADMIN/PRINCIPAL, just
  // semester for a TEACHER viewing their own schedule) to show the grid.
  const scheduleReady = isTeacherView ? Boolean(semesterId) : Boolean(classId && semesterId);

  const deleteAssignmentMutation = useMutation({
    mutationFn: (id) => teachingAssignmentService.delete(id),
    onSuccess: () => {
      toast.success('Đã xóa phân công giảng dạy');
      queryClient.invalidateQueries({ queryKey: ['teaching-assignments'] });
    },
    onError: (err) =>
      // 409 when the assignment still has timetable slots scheduled against
      // it (mirrors deleteTeachingAssignment's own doc comment).
      toast.error(err?.response?.data?.message || err?.message || 'Không thể xóa phân công giảng dạy'),
    onSettled: () => setDeletingAssignment(null),
  });

  const deleteSlotMutation = useMutation({
    mutationFn: (id) => timetableService.deleteSlot(id),
    onSuccess: () => {
      toast.success('Đã xóa tiết học');
      queryClient.invalidateQueries({ queryKey: ['timetable'] });
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể xóa tiết học'),
    onSettled: () => setDeletingSlot(null),
  });

  const openCreateSlot = (dayOfWeek, period) => {
    setEditingSlot(null);
    setPresetCell({ dayOfWeek, period });
    setSlotDialogOpen(true);
  };
  const openEditSlot = (slot) => {
    setEditingSlot(slot);
    setPresetCell(null);
    setSlotDialogOpen(true);
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Thời khoá biểu</h1>
        <p className="text-sm text-muted-foreground">Phân công giảng dạy và lịch học theo lớp</p>
      </div>

      <Card>
        <CardContent className="grid grid-cols-1 gap-4 pt-6 sm:grid-cols-2">
          {!isTeacherView && (
            <div className="space-y-1.5">
              <Label htmlFor="timetable-class-select">Lớp</Label>
              <Select value={classId} onValueChange={setClassId}>
                <SelectTrigger id="timetable-class-select">
                  <SelectValue placeholder={classesQuery.isLoading ? 'Đang tải...' : 'Chọn lớp'} />
                </SelectTrigger>
                <SelectContent>
                  {(classesQuery.data ?? []).map((c) => (
                    <SelectItem key={c.id} value={String(c.id)}>
                      {c.className} - {c.section}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          )}
          <div className="space-y-1.5">
            <Label htmlFor="timetable-semester-select">Học kỳ</Label>
            <Select value={semesterId} onValueChange={setSemesterId}>
              <SelectTrigger id="timetable-semester-select">
                <SelectValue placeholder={semestersQuery.isLoading ? 'Đang tải...' : 'Chọn học kỳ'} />
              </SelectTrigger>
              <SelectContent>
                {(semestersQuery.data ?? []).map((s) => (
                  <SelectItem key={s.id} value={String(s.id)}>
                    {SEMESTER_NAME_LABELS[s.name] ?? s.name} ({s.academicYearName})
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {isTeacherView && myStaffLookupDone && !myStaffId && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive dark:text-red-400">
          Tài khoản của bạn chưa được liên kết với hồ sơ nhân sự nên không thể xem lịch dạy.
        </div>
      )}

      {scheduleReady && (
        <>
          {!isTeacherView && (
            <Card>
              <CardHeader className="flex flex-col gap-3 space-y-0 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <CardTitle>Phân công giảng dạy</CardTitle>
                  <CardDescription>Lớp {classLabel}</CardDescription>
                </div>
                {canManage && (
                  <Button
                    onClick={() => {
                      setEditingAssignment(null);
                      setAssignmentDialogOpen(true);
                    }}
                  >
                    <FiPlus className="mr-2 h-4 w-4" /> Thêm phân công
                  </Button>
                )}
              </CardHeader>
              <CardContent>
                {assignmentsQuery.isLoading ? (
                  <p className="py-4 text-center text-sm text-muted-foreground">Đang tải...</p>
                ) : classAssignments.length === 0 ? (
                  <p className="py-4 text-center text-sm text-muted-foreground">
                    Lớp này chưa có phân công giảng dạy trong học kỳ đã chọn.
                  </p>
                ) : (
                  <div className="divide-y rounded-md border">
                    {classAssignments.map((a) => (
                      <div key={a.id} className="flex items-center justify-between gap-4 px-4 py-2.5">
                        <div>
                          <span className="font-medium">{a.subjectName}</span>
                          <span className="text-muted-foreground"> · {a.teacherName}</span>
                        </div>
                        {canManage && (
                          <div className="flex gap-2">
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() => {
                                setEditingAssignment(a);
                                setAssignmentDialogOpen(true);
                              }}
                              aria-label="Sửa"
                            >
                              <FiEdit2 className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="ghost"
                              size="icon"
                              className="text-destructive hover:text-destructive"
                              onClick={() => setDeletingAssignment(a)}
                              aria-label="Xóa"
                            >
                              <FiTrash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          )}

          <Card>
            <CardHeader>
              <CardTitle>Lịch học trong tuần</CardTitle>
              <CardDescription>{isTeacherView ? 'Lịch dạy của tôi' : `Lớp ${classLabel}`}</CardDescription>
            </CardHeader>
            <CardContent>
              {!isTeacherView && classAssignments.length === 0 ? (
                <p className="py-4 text-center text-sm text-muted-foreground">
                  Thêm phân công giảng dạy ở trên trước khi xếp lịch học.
                </p>
              ) : timetableQuery.isLoading ? (
                <p className="py-8 text-center text-sm text-muted-foreground">Đang tải...</p>
              ) : (
                <div className="overflow-x-auto rounded-md border">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b bg-muted/50">
                        <th className="w-20 p-2 text-left font-medium text-muted-foreground">Tiết</th>
                        {DAYS.map((d) => (
                          <th key={d} className="p-2 text-left font-medium text-muted-foreground">
                            {DAY_OF_WEEK_LABELS[d]}
                          </th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {PERIODS.map((period) => (
                        <React.Fragment key={period}>
                          {period === 6 && (
                            <tr className="border-b bg-muted/30">
                              <td colSpan={DAYS.length + 1} className="px-2 py-1 text-xs font-medium text-muted-foreground">
                                Buổi chiều
                              </td>
                            </tr>
                          )}
                          <tr className="border-b last:border-0">
                            <td className="p-2 align-top font-medium text-muted-foreground">Tiết {period}</td>
                            {DAYS.map((day) => {
                              const slot = slotByCell.get(`${day}-${period}`);
                              return (
                                <td key={day} className="p-1.5 align-top">
                                  {slot ? (
                                    // A <div>, not a <button>, wrapping the
                                    // "edit" and "delete" triggers as sibling
                                    // buttons - a <button> nested inside
                                    // another <button> (the first version of
                                    // this) is invalid HTML that browsers
                                    // and screen readers handle
                                    // unpredictably, caught before this page
                                    // was ever tested live.
                                    <div className="w-full rounded-md border bg-primary/5 p-2 text-xs leading-snug">
                                      {canManage ? (
                                        <button
                                          type="button"
                                          onClick={() => openEditSlot(slot)}
                                          aria-label={`Sửa tiết ${slot.subjectName} thứ ${day} tiết ${period}`}
                                          className="w-full text-left hover:underline"
                                        >
                                          <div className="font-medium">{slot.subjectName}</div>
                                        </button>
                                      ) : (
                                        <div className="font-medium">{slot.subjectName}</div>
                                      )}
                                      <div className="text-muted-foreground">
                                        {isTeacherView ? slot.schoolClassLabel : slot.teacherName}
                                      </div>
                                      <div className="text-muted-foreground">{slot.room}</div>
                                      {canManage && (
                                        <button
                                          type="button"
                                          aria-label={`Xóa tiết ${slot.subjectName} thứ ${day} tiết ${period}`}
                                          onClick={() => setDeletingSlot(slot)}
                                          className="mt-1 inline-flex items-center gap-1 text-destructive hover:underline dark:text-red-400"
                                        >
                                          <FiTrash2 className="h-3 w-3" /> Xóa
                                        </button>
                                      )}
                                    </div>
                                  ) : canManage ? (
                                    <button
                                      type="button"
                                      onClick={() => openCreateSlot(day, period)}
                                      aria-label={`Thêm tiết học thứ ${day} tiết ${period}`}
                                      className="flex h-14 w-full items-center justify-center rounded-md border border-dashed text-muted-foreground hover:border-primary hover:text-primary"
                                    >
                                      <FiPlus className="h-4 w-4" />
                                    </button>
                                  ) : (
                                    <div className="h-14" />
                                  )}
                                </td>
                              );
                            })}
                          </tr>
                        </React.Fragment>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </CardContent>
          </Card>
        </>
      )}

      {canManage && (
        <>
          <TeachingAssignmentFormDialog
            key={`assignment-${editingAssignment?.id ?? 'create'}`}
            open={assignmentDialogOpen}
            onOpenChange={setAssignmentDialogOpen}
            schoolClassId={classId ? Number(classId) : null}
            semesterId={semesterId ? Number(semesterId) : null}
            classLabel={classLabel}
            assignment={editingAssignment}
          />
          <TimetableSlotFormDialog
            key={`slot-${editingSlot?.id ?? 'create'}-${presetCell ? `${presetCell.dayOfWeek}-${presetCell.period}` : 'none'}`}
            open={slotDialogOpen}
            onOpenChange={setSlotDialogOpen}
            assignmentOptions={assignmentOptions}
            slot={editingSlot}
            presetDayOfWeek={presetCell?.dayOfWeek}
            presetPeriod={presetCell?.period}
          />
        </>
      )}

      <AlertDialog open={Boolean(deletingAssignment)} onOpenChange={(open) => !open && setDeletingAssignment(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xóa phân công giảng dạy?</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn sắp xóa phân công {deletingAssignment?.subjectName} - {deletingAssignment?.teacherName}. Hành động
              này không thể hoàn tác.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Hủy</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={deleteAssignmentMutation.isPending}
              onClick={() => deleteAssignmentMutation.mutate(deletingAssignment.id)}
            >
              {deleteAssignmentMutation.isPending ? 'Đang xóa...' : 'Xóa'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <AlertDialog open={Boolean(deletingSlot)} onOpenChange={(open) => !open && setDeletingSlot(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xóa tiết học?</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn sắp xóa tiết {deletingSlot?.subjectName} - {deletingSlot ? DAY_OF_WEEK_LABELS[deletingSlot.dayOfWeek] : ''}, tiết{' '}
              {deletingSlot?.period}. Hành động này không thể hoàn tác.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Hủy</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={deleteSlotMutation.isPending}
              onClick={() => deleteSlotMutation.mutate(deletingSlot.id)}
            >
              {deleteSlotMutation.isPending ? 'Đang xóa...' : 'Xóa'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

export default TimetableManagement;
