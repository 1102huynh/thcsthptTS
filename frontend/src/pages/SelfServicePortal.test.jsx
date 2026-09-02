import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

// --- mocks -----------------------------------------------------------
const mockAuth = { role: 'STUDENT', userId: 7 };
vi.mock('../services/authService', () => ({
  getCurrentUser: () => mockAuth,
}));

const svc = {
  getMe: vi.fn(),
  getChildren: vi.fn(),
  yearsGetAll: vi.fn(),
  semestersByYear: vi.fn(),
  gradeSemesterSummary: vi.fn(),
  gradeYearSummary: vi.fn(),
  attendanceByStudent: vi.fn(),
  feesByStudent: vi.fn(),
  feeDues: vi.fn(),
  conductByStudent: vi.fn(),
};

vi.mock('../services/dataService', () => ({
  studentService: { getMe: (...a) => svc.getMe(...a) },
  parentService: { getChildren: (...a) => svc.getChildren(...a) },
  academicYearService: { getAll: (...a) => svc.yearsGetAll(...a) },
  semesterService: { getByAcademicYear: (...a) => svc.semestersByYear(...a) },
  gradeRecordService: {
    getStudentSemesterSummary: (...a) => svc.gradeSemesterSummary(...a),
    getStudentYearSummary: (...a) => svc.gradeYearSummary(...a),
  },
  attendanceService: { getByStudent: (...a) => svc.attendanceByStudent(...a) },
  feeService: {
    getByStudent: (...a) => svc.feesByStudent(...a),
    getTotalDues: (...a) => svc.feeDues(...a),
  },
  conductService: { getByStudent: (...a) => svc.conductByStudent(...a) },
}));

import SelfServicePortal from './SelfServicePortal';

function renderPortal() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={qc}>
      <SelfServicePortal />
    </QueryClientProvider>
  );
}

beforeEach(() => {
  Object.values(svc).forEach((fn) => fn.mockReset());
  mockAuth.role = 'STUDENT';
  svc.getMe.mockResolvedValue({
    data: { id: 42, rollNumber: 'HS-042', className: '9A', user: { firstName: 'Lê', lastName: 'Văn C' } },
  });
  svc.yearsGetAll.mockResolvedValue({ data: [{ id: 1, name: '2025-2026', status: 'ACTIVE' }] });
  svc.semestersByYear.mockResolvedValue({ data: [{ id: 11, name: 'HK1', academicYearName: '2025-2026' }] });
  svc.gradeSemesterSummary.mockResolvedValue({
    data: [{ subjectId: 100, subjectName: 'Toán', average: 8.4, classification: null }],
  });
  svc.gradeYearSummary.mockResolvedValue({ data: [] });
  svc.attendanceByStudent.mockResolvedValue({
    data: [
      { id: 1, attendanceDate: '2026-09-01', status: 'PRESENT', remarks: '' },
      { id: 2, attendanceDate: '2026-09-02', status: 'ABSENT', remarks: 'ốm' },
    ],
  });
  svc.feesByStudent.mockResolvedValue({ data: [] });
  svc.feeDues.mockResolvedValue({ data: 0 });
  svc.conductByStudent.mockResolvedValue({ data: [] });
});

describe('SelfServicePortal', () => {
  it('resolves the STUDENT via getMe and shows their identity + grades tab', async () => {
    renderPortal();
    expect(await screen.findByText(/Lê Văn C/)).toBeInTheDocument();
    expect(screen.getByText(/Mã HS: HS-042/)).toBeInTheDocument();
    // Grades tab is the default - its semester-average row renders
    expect(await screen.findByText('Toán')).toBeInTheDocument();
    expect(screen.getByText('8.4')).toBeInTheDocument();
    expect(svc.getChildren).not.toHaveBeenCalled();
  });

  it('switches to the attendance tab and computes the chuyên cần rate', async () => {
    const user = userEvent.setup();
    renderPortal();
    await screen.findByText(/Lê Văn C/);
    await user.click(screen.getByRole('button', { name: 'Điểm danh' }));
    // 1 present of 2 records -> 50%
    expect(await screen.findByText('50%')).toBeInTheDocument();
  });

  it('shows a friendly message when the STUDENT account has no linked profile', async () => {
    svc.getMe.mockRejectedValueOnce(new Error('404'));
    renderPortal();
    expect(
      await screen.findByText(/chưa được liên kết với hồ sơ học sinh/i)
    ).toBeInTheDocument();
  });

  it('for a PARENT, loads children and offers a child picker', async () => {
    mockAuth.role = 'PARENT';
    svc.getChildren.mockResolvedValue({
      data: [
        { studentId: 42, studentName: 'Lê Văn C', rollNumber: 'HS-042', relationship: 'CHA' },
        { studentId: 43, studentName: 'Lê Thị D', rollNumber: 'HS-043', relationship: 'CHA' },
      ],
    });
    renderPortal();
    expect(await screen.findByText('Chọn con')).toBeInTheDocument();
    expect(svc.getMe).not.toHaveBeenCalled();
  });
});
