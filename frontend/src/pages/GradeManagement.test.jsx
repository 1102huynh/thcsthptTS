import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

// H.3.1 (GVBM) - a TEACHER may only enter grades for the class/subject/
// semester combination(s) they hold a TeachingAssignment for;
// GradeRecordService 403s any other combination server-side, so the Lớp/
// Môn học pickers here must never even offer a combo without one.
const mockAuth = { role: 'TEACHER', userId: 7 };
vi.mock('../services/authService', () => ({
  getCurrentUser: () => mockAuth,
}));

const svc = {
  classesGetAll: vi.fn(),
  staffGetAll: vi.fn(),
  subjectsGetAll: vi.fn(),
  assignmentsGetAll: vi.fn(),
  yearsGetAll: vi.fn(),
  semestersByYear: vi.fn(),
  gradeConfigsGetAll: vi.fn(),
  getByClass: vi.fn(),
  getStudentSemesterGrades: vi.fn(),
};

vi.mock('../services/dataService', () => ({
  schoolClassService: { getAll: (...a) => svc.classesGetAll(...a) },
  staffService: { getAll: (...a) => svc.staffGetAll(...a) },
  subjectService: { getAll: (...a) => svc.subjectsGetAll(...a) },
  teachingAssignmentService: { getAll: (...a) => svc.assignmentsGetAll(...a) },
  academicYearService: { getAll: (...a) => svc.yearsGetAll(...a) },
  semesterService: { getByAcademicYear: (...a) => svc.semestersByYear(...a) },
  gradeConfigService: { getAll: (...a) => svc.gradeConfigsGetAll(...a) },
  studentService: { getByClass: (...a) => svc.getByClass(...a) },
  gradeRecordService: {
    getStudentSemesterGrades: (...a) => svc.getStudentSemesterGrades(...a),
    create: vi.fn(),
    update: vi.fn(),
  },
  reportService: { studentTranscript: vi.fn() },
}));

import GradeManagement from './GradeManagement';

function renderPage() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={qc}>
      <GradeManagement />
    </QueryClientProvider>
  );
}

beforeEach(() => {
  Object.values(svc).forEach((fn) => fn.mockReset());
  svc.staffGetAll.mockResolvedValue({ data: [{ id: 1, user: { id: 7 } }] });
  svc.classesGetAll.mockResolvedValue({
    data: [
      { id: 10, className: '10', section: 'A1' },
      { id: 11, className: '10', section: 'A2' },
    ],
  });
  svc.subjectsGetAll.mockResolvedValue({
    data: [
      { id: 100, name: 'Toán' },
      { id: 101, name: 'Ngữ văn' },
    ],
  });
  svc.assignmentsGetAll.mockResolvedValue({
    data: [{ id: 1, teacherId: 1, schoolClassId: 10, subjectId: 100, semesterId: 50 }],
  });
  svc.yearsGetAll.mockResolvedValue({ data: [{ id: 1, name: '2025-2026', status: 'ACTIVE' }] });
  svc.semestersByYear.mockResolvedValue({
    data: [{ id: 50, name: 'HK1', academicYearName: '2025-2026' }],
  });
  svc.gradeConfigsGetAll.mockResolvedValue({ data: [] });
  svc.getByClass.mockResolvedValue({ data: [] });
  svc.getStudentSemesterGrades.mockResolvedValue({ data: [] });
});

describe('GradeManagement - TEACHER teaching-assignment scoping (H.3.1)', () => {
  it('only offers the class(es) the TEACHER has a TeachingAssignment for in the class picker', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('combobox', { name: 'Lớp' }));
    expect(await screen.findByRole('option', { name: /10 - A1/ })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: /10 - A2/ })).not.toBeInTheDocument();
  });

  it('only offers the subject(s) assigned for the selected class/semester in the subject picker', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('combobox', { name: 'Môn học' }));
    expect(await screen.findByRole('option', { name: 'Toán' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'Ngữ văn' })).not.toBeInTheDocument();
  });

  it('shows a message when the TEACHER has no teaching assignment at all', async () => {
    svc.assignmentsGetAll.mockResolvedValue({ data: [] });
    renderPage();

    expect(await screen.findByText(/chưa được phân công giảng dạy lớp nào/i)).toBeInTheDocument();
  });

  it('shows a message when the class has no assigned subject for the selected semester', async () => {
    svc.assignmentsGetAll.mockResolvedValue({
      // Assigned to class 10 but for a different semester than the default (50).
      data: [{ id: 1, teacherId: 1, schoolClassId: 10, subjectId: 100, semesterId: 99 }],
    });
    renderPage();

    expect(await screen.findByText(/chưa được phân công dạy môn nào ở lớp này/i)).toBeInTheDocument();
  });
});
