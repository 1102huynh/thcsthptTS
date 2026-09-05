import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { itScopesClassPickerToHomeroom } from '../test-utils/homeroomClassPickerScenario';

// H.3.1 - a TEACHER may only take attendance for the class(es) they are
// GVCN (homeroom teacher) of; AttendanceService 403s any other class
// server-side (see backend/.../AttendanceService.markAttendanceForClass),
// so the class picker here must never even offer a non-homeroom class.
const mockAuth = { role: 'TEACHER', userId: 7 };
vi.mock('../services/authService', () => ({
  getCurrentUser: () => mockAuth,
}));

const svc = {
  classesGetAll: vi.fn(),
  staffGetAll: vi.fn(),
  getByClass: vi.fn(),
  getByDate: vi.fn(),
  getBetweenDates: vi.fn(),
};

vi.mock('../services/dataService', () => ({
  schoolClassService: { getAll: (...a) => svc.classesGetAll(...a) },
  staffService: { getAll: (...a) => svc.staffGetAll(...a) },
  studentService: { getByClass: (...a) => svc.getByClass(...a) },
  attendanceService: {
    getByDate: (...a) => svc.getByDate(...a),
    getBetweenDates: (...a) => svc.getBetweenDates(...a),
    markClass: vi.fn(),
  },
  reportService: { classAttendance: vi.fn() },
}));

import AttendanceManagement from './AttendanceManagement';

function renderPage() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={qc}>
      <AttendanceManagement />
    </QueryClientProvider>
  );
}

beforeEach(() => {
  Object.values(svc).forEach((fn) => fn.mockReset());
  svc.staffGetAll.mockResolvedValue({ data: [{ id: 1, user: { id: 7 } }] });
  svc.classesGetAll.mockResolvedValue({
    data: [
      { id: 10, className: '10', section: 'A1', classTeacherId: 1, studentCount: 2 },
      { id: 11, className: '10', section: 'A2', classTeacherId: 99, studentCount: 3 },
    ],
  });
  svc.getByClass.mockResolvedValue({ data: [] });
  svc.getByDate.mockResolvedValue({ data: [] });
  svc.getBetweenDates.mockResolvedValue({ data: [] });
});

describe('AttendanceManagement - TEACHER homeroom scoping (H.3.1)', () => {
  itScopesClassPickerToHomeroom({
    renderPage,
    classesService: svc.classesGetAll,
    nonHomeroomClass: { id: 11, className: '10', section: 'A2', classTeacherId: 99, studentCount: 3 },
  });

  it('fetches the roster for the auto-selected homeroom class', async () => {
    renderPage();
    await waitFor(() => expect(svc.getByClass).toHaveBeenCalledWith('10', 'A1'));
  });
});
