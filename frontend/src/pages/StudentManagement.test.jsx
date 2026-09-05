import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

// H.3.1 - GET /v1/students already returns only the TEACHER's own homeroom
// students server-side (StudentService.getAllStudents(User) via
// TeacherHomeroomGuard.filterToHomeroom); this page only adds the "you
// aren't GVCN of any class" message on top of that.
const mockAuth = { role: 'TEACHER', userId: 7 };
vi.mock('../services/authService', () => ({
  getCurrentUser: () => mockAuth,
}));

const svc = { studentsGetAll: vi.fn(), staffGetAll: vi.fn(), classesGetAll: vi.fn() };

vi.mock('../services/dataService', () => ({
  studentService: { getAll: (...a) => svc.studentsGetAll(...a), delete: vi.fn() },
  staffService: { getAll: (...a) => svc.staffGetAll(...a) },
  schoolClassService: { getAll: (...a) => svc.classesGetAll(...a) },
}));

import StudentManagement from './StudentManagement';

function renderPage() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={qc}>
      <StudentManagement />
    </QueryClientProvider>
  );
}

beforeEach(() => {
  Object.values(svc).forEach((fn) => fn.mockReset());
  svc.staffGetAll.mockResolvedValue({ data: [{ id: 1, user: { id: 7 } }] });
});

describe('StudentManagement - TEACHER homeroom scoping (H.3.1)', () => {
  it('shows a message when the TEACHER is not GVCN of any class', async () => {
    svc.classesGetAll.mockResolvedValue({ data: [{ id: 11, classTeacherId: 99 }] });
    svc.studentsGetAll.mockResolvedValue({ data: [] });
    renderPage();

    expect(await screen.findByText(/chưa là giáo viên chủ nhiệm/i)).toBeInTheDocument();
  });

  it('does not show the message when the TEACHER is GVCN of a class, even if it has no students yet', async () => {
    svc.classesGetAll.mockResolvedValue({ data: [{ id: 10, classTeacherId: 1 }] });
    svc.studentsGetAll.mockResolvedValue({ data: [] });
    renderPage();

    await screen.findByText('Không tìm thấy học sinh nào.');
    expect(screen.queryByText(/chưa là giáo viên chủ nhiệm/i)).not.toBeInTheDocument();
  });

  it('renders the (already server-filtered) homeroom roster as-is', async () => {
    svc.classesGetAll.mockResolvedValue({ data: [{ id: 10, classTeacherId: 1 }] });
    svc.studentsGetAll.mockResolvedValue({
      data: [{ id: 1, rollNumber: 'R1', className: '10', section: 'A1', status: 'ACTIVE', user: { firstName: 'A', lastName: 'B' } }],
    });
    renderPage();

    expect(await screen.findByText('R1')).toBeInTheDocument();
  });
});
