import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

// H.3.1 - thời khoá biểu do hiệu trưởng quyết định: TEACHER chỉ được xem
// (không sửa) lịch dạy của chính mình, không browse theo lớp bất kỳ nữa
// (GET /v1/timetable/class/{id} is ADMIN/PRINCIPAL-only now).
const mockAuth = { role: 'TEACHER', userId: 7 };
vi.mock('../services/authService', () => ({
  getCurrentUser: () => mockAuth,
}));

const svc = {
  classesGetAll: vi.fn(),
  staffGetAll: vi.fn(),
  semestersGetAll: vi.fn(),
  assignmentsGetAll: vi.fn(),
  getByClass: vi.fn(),
  getByTeacher: vi.fn(),
};

vi.mock('../services/dataService', () => ({
  schoolClassService: { getAll: (...a) => svc.classesGetAll(...a) },
  staffService: { getAll: (...a) => svc.staffGetAll(...a) },
  semesterService: { getAll: (...a) => svc.semestersGetAll(...a) },
  teachingAssignmentService: { getAll: (...a) => svc.assignmentsGetAll(...a), delete: vi.fn() },
  timetableService: {
    getByClass: (...a) => svc.getByClass(...a),
    getByTeacher: (...a) => svc.getByTeacher(...a),
    createSlot: vi.fn(),
    updateSlot: vi.fn(),
    deleteSlot: vi.fn(),
  },
}));

import TimetableManagement from './TimetableManagement';

function renderPage() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={qc}>
      <TimetableManagement />
    </QueryClientProvider>
  );
}

// TimetableManagement has no auto-select effect for semesterId (unlike
// ConductManagement/AttendanceManagement) - the user always picks it.
async function pickSemester(user) {
  await user.click(await screen.findByRole('combobox', { name: 'Học kỳ' }));
  await user.click(await screen.findByRole('option', { name: /Học kỳ 1/ }));
}

beforeEach(() => {
  Object.values(svc).forEach((fn) => fn.mockReset());
  svc.staffGetAll.mockResolvedValue({ data: [{ id: 1, user: { id: 7 } }] });
  svc.classesGetAll.mockResolvedValue({ data: [] });
  svc.assignmentsGetAll.mockResolvedValue({ data: [] });
  svc.semestersGetAll.mockResolvedValue({
    data: [{ id: 50, name: 'HK1', academicYearName: '2025-2026' }],
  });
  svc.getByClass.mockResolvedValue({ data: [] });
  svc.getByTeacher.mockResolvedValue({
    data: [
      {
        id: 1, dayOfWeek: 2, period: 1, room: 'P.101',
        subjectName: 'Toán', teacherName: 'Integration Teacher',
        schoolClassId: 10, schoolClassLabel: '10-A1',
      },
    ],
  });
});

describe('TimetableManagement - TEACHER own-schedule view (H.3.1)', () => {
  it('does not show a class picker or the Phân công giảng dạy section for a TEACHER', async () => {
    renderPage();
    await screen.findByRole('combobox', { name: 'Học kỳ' });
    expect(screen.queryByRole('combobox', { name: 'Lớp' })).not.toBeInTheDocument();
    expect(screen.queryByText('Phân công giảng dạy')).not.toBeInTheDocument();
  });

  it('fetches the schedule by teacherId (own staffId), not by class', async () => {
    const user = userEvent.setup();
    renderPage();
    await pickSemester(user);

    await waitFor(() => expect(svc.getByTeacher).toHaveBeenCalledWith(1, '50'));
    expect(svc.getByClass).not.toHaveBeenCalled();
  });

  it('shows the class label (not teacher name) on each scheduled cell', async () => {
    const user = userEvent.setup();
    renderPage();
    await pickSemester(user);

    expect(await screen.findByText('Toán')).toBeInTheDocument();
    expect(screen.getByText('10-A1')).toBeInTheDocument();
    expect(screen.queryByText('Integration Teacher')).not.toBeInTheDocument();
  });

  it('never shows edit/delete controls on a scheduled cell (view-only)', async () => {
    const user = userEvent.setup();
    renderPage();
    await pickSemester(user);

    await screen.findByText('Toán');
    expect(screen.queryByRole('button', { name: /Sửa tiết/ })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Xóa tiết/ })).not.toBeInTheDocument();
  });
});
