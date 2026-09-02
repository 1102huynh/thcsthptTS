import React, { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { FiEdit2, FiTrash2, FiPlus } from 'react-icons/fi';
import { subjectService, semesterService } from '../services/dataService';
import DataTable from '../components/shared/DataTable';
import SubjectFormDialog from './academic/SubjectFormDialog';
import SemesterFormDialog from './academic/SemesterFormDialog';
import { Button } from '../components/ui/button';
import { Badge } from '../components/ui/badge';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/card';
import { SUBJECT_CATEGORY_LABELS, SEMESTER_NAME_LABELS } from '../lib/enumLabels';
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

/**
 * Cấu hình học tập - Môn học (Subject) + Học kỳ (Semester) management,
 * grouped on one page since both are small, rarely-changed lookup tables
 * that Giai đoạn 3.2's Teaching Assignment/Timetable page depends on
 * existing first. Neither ever got its own day in the Tuần 1-6 frontend
 * plan (only "academic-config/" as a folder name) - built now as the
 * prerequisite for the Timetable page.
 */
function AcademicConfig() {
  const queryClient = useQueryClient();

  const subjectsQuery = useQuery({
    queryKey: ['subjects'],
    queryFn: () => subjectService.getAll().then((r) => r.data),
  });
  const semestersQuery = useQuery({
    queryKey: ['semesters'],
    queryFn: () => semesterService.getAll().then((r) => r.data),
  });

  // Subjects
  const [subjectDialogOpen, setSubjectDialogOpen] = useState(false);
  const [editingSubject, setEditingSubject] = useState(null);
  const [deletingSubject, setDeletingSubject] = useState(null);
  const deleteSubjectMutation = useMutation({
    mutationFn: (id) => subjectService.delete(id),
    onSuccess: () => {
      toast.success('Đã xóa môn học');
      queryClient.invalidateQueries({ queryKey: ['subjects'] });
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể xóa môn học'),
    onSettled: () => setDeletingSubject(null),
  });

  // Semesters
  const [semesterDialogOpen, setSemesterDialogOpen] = useState(false);
  const [editingSemester, setEditingSemester] = useState(null);
  const [deletingSemester, setDeletingSemester] = useState(null);
  const deleteSemesterMutation = useMutation({
    mutationFn: (id) => semesterService.delete(id),
    onSuccess: () => {
      toast.success('Đã xóa học kỳ');
      queryClient.invalidateQueries({ queryKey: ['semesters'] });
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể xóa học kỳ'),
    onSettled: () => setDeletingSemester(null),
  });

  const subjectColumns = useMemo(
    () => [
      { accessorKey: 'code', header: 'Mã' },
      { accessorKey: 'name', header: 'Tên môn học' },
      {
        id: 'category',
        header: 'Loại',
        cell: ({ row }) => (
          <Badge variant="secondary">{SUBJECT_CATEGORY_LABELS[row.original.category] ?? row.original.category}</Badge>
        ),
      },
      {
        accessorKey: 'gradeLevels',
        header: 'Khối áp dụng',
        cell: ({ getValue }) => getValue() || <span className="text-muted-foreground">Tất cả</span>,
      },
      {
        id: 'actions',
        header: '',
        cell: ({ row }) => (
          <div className="flex justify-end gap-2">
            <Button
              variant="ghost"
              size="icon"
              onClick={() => {
                setEditingSubject(row.original);
                setSubjectDialogOpen(true);
              }}
              aria-label="Sửa"
            >
              <FiEdit2 className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              className="text-destructive hover:text-destructive"
              onClick={() => setDeletingSubject(row.original)}
              aria-label="Xóa"
            >
              <FiTrash2 className="h-4 w-4" />
            </Button>
          </div>
        ),
      },
    ],
    []
  );

  const semesterColumns = useMemo(
    () => [
      {
        id: 'name',
        header: 'Học kỳ',
        cell: ({ row }) => SEMESTER_NAME_LABELS[row.original.name] ?? row.original.name,
      },
      { accessorKey: 'academicYearName', header: 'Năm học' },
      { accessorKey: 'startDate', header: 'Bắt đầu' },
      { accessorKey: 'endDate', header: 'Kết thúc' },
      {
        id: 'actions',
        header: '',
        cell: ({ row }) => (
          <div className="flex justify-end gap-2">
            <Button
              variant="ghost"
              size="icon"
              onClick={() => {
                setEditingSemester(row.original);
                setSemesterDialogOpen(true);
              }}
              aria-label="Sửa"
            >
              <FiEdit2 className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              className="text-destructive hover:text-destructive"
              onClick={() => setDeletingSemester(row.original)}
              aria-label="Xóa"
            >
              <FiTrash2 className="h-4 w-4" />
            </Button>
          </div>
        ),
      },
    ],
    []
  );

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Cấu hình học tập</h1>
        <p className="text-sm text-muted-foreground">Quản lý môn học và học kỳ - dữ liệu nền cho thời khoá biểu</p>
      </div>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0">
          <div>
            <CardTitle>Môn học</CardTitle>
            <CardDescription>Danh sách môn học trong trường</CardDescription>
          </div>
          <Button
            onClick={() => {
              setEditingSubject(null);
              setSubjectDialogOpen(true);
            }}
          >
            <FiPlus className="mr-2 h-4 w-4" /> Thêm môn học
          </Button>
        </CardHeader>
        <CardContent>
          {subjectsQuery.isError && (
            <div className="mb-3 rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive dark:text-red-400">
              Không tải được danh sách môn học.
            </div>
          )}
          <DataTable
            columns={subjectColumns}
            data={subjectsQuery.data ?? []}
            isLoading={subjectsQuery.isLoading}
            emptyMessage="Chưa có môn học nào."
          />
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0">
          <div>
            <CardTitle>Học kỳ</CardTitle>
            <CardDescription>Học kỳ 1 / Học kỳ 2 theo từng năm học</CardDescription>
          </div>
          <Button
            onClick={() => {
              setEditingSemester(null);
              setSemesterDialogOpen(true);
            }}
          >
            <FiPlus className="mr-2 h-4 w-4" /> Thêm học kỳ
          </Button>
        </CardHeader>
        <CardContent>
          {semestersQuery.isError && (
            <div className="mb-3 rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive dark:text-red-400">
              Không tải được danh sách học kỳ.
            </div>
          )}
          <DataTable
            columns={semesterColumns}
            data={semestersQuery.data ?? []}
            isLoading={semestersQuery.isLoading}
            emptyMessage="Chưa có học kỳ nào."
          />
        </CardContent>
      </Card>

      <SubjectFormDialog
        key={`subject-${editingSubject?.id ?? 'create'}`}
        open={subjectDialogOpen}
        onOpenChange={setSubjectDialogOpen}
        subject={editingSubject}
      />
      <SemesterFormDialog
        key={`semester-${editingSemester?.id ?? 'create'}`}
        open={semesterDialogOpen}
        onOpenChange={setSemesterDialogOpen}
        semester={editingSemester}
      />

      <AlertDialog open={Boolean(deletingSubject)} onOpenChange={(open) => !open && setDeletingSubject(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xóa môn học?</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn sắp xóa môn {deletingSubject?.name}. Hành động này không thể hoàn tác.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Hủy</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={deleteSubjectMutation.isPending}
              onClick={() => deleteSubjectMutation.mutate(deletingSubject.id)}
            >
              {deleteSubjectMutation.isPending ? 'Đang xóa...' : 'Xóa'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <AlertDialog open={Boolean(deletingSemester)} onOpenChange={(open) => !open && setDeletingSemester(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xóa học kỳ?</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn sắp xóa {deletingSemester ? SEMESTER_NAME_LABELS[deletingSemester.name] : ''} ({deletingSemester?.academicYearName}).
              Hành động này không thể hoàn tác.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Hủy</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={deleteSemesterMutation.isPending}
              onClick={() => deleteSemesterMutation.mutate(deletingSemester.id)}
            >
              {deleteSemesterMutation.isPending ? 'Đang xóa...' : 'Xóa'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

export default AcademicConfig;
