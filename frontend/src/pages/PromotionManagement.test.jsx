import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

// H.3.1 - a TEACHER may only preview lên lớp for the class(es) they are
// GVCN (homeroom teacher) of; PromotionService 403s any other class
// server-side (see backend/.../PromotionService.previewClassPromotions),
// so the class picker here must never even offer a non-homeroom class.
const mockAuth = { role: 'TEACHER', userId: 7 };
vi.mock('../services/authService', () => ({
  getCurrentUser: () => mockAuth,
}));

const svc = {
  classesGetAll: vi.fn(),
  staffGetAll: vi.fn(),
  yearsGetAll: vi.fn(),
  getClassPreview: vi.fn(),
  getStudentHistory: vi.fn(),
};

vi.mock('../services/dataService', () => ({
  schoolClassService: { getAll: (...a) => svc.classesGetAll(...a) },
  staffService: { getAll: (...a) => svc.staffGetAll(...a) },
  academicYearService: { getAll: (...a) => svc.yearsGetAll(...a) },
  promotionService: {
    getClassPreview: (...a) => svc.getClassPreview(...a),
    getStudentHistory: (...a) => svc.getStudentHistory(...a),
    confirm: vi.fn(),
  },
}));

import PromotionManagement from './PromotionManagement';

function renderPage() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={qc}>
      <PromotionManagement />
    </QueryClientProvider>
  );
}

beforeEach(() => {
  Object.values(svc).forEach((fn) => fn.mockReset());
  svc.staffGetAll.mockResolvedValue({ data: [{ id: 1, user: { id: 7 } }] });
  svc.yearsGetAll.mockResolvedValue({ data: [{ id: 100, name: '2025-2026', status: 'ACTIVE' }] });
  svc.classesGetAll.mockResolvedValue({
    data: [
      { id: 10, className: '10', section: 'A1', academicYear: '2025-2026', classTeacherId: 1 },
      { id: 11, className: '10', section: 'A2', academicYear: '2025-2026', classTeacherId: 99 },
    ],
  });
  svc.getClassPreview.mockResolvedValue({ data: [] });
  svc.getStudentHistory.mockResolvedValue({ data: [] });
});

describe('PromotionManagement - TEACHER homeroom scoping (H.3.1)', () => {
  it('only offers the class(es) the TEACHER is GVCN of in the class picker', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('combobox', { name: 'Lớp' }));
    expect(await screen.findByRole('option', { name: /10 - A1/ })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: /10 - A2/ })).not.toBeInTheDocument();
  });

  it('shows a message when the TEACHER is not GVCN of any class', async () => {
    svc.classesGetAll.mockResolvedValue({
      data: [{ id: 11, className: '10', section: 'A2', academicYear: '2025-2026', classTeacherId: 99 }],
    });
    renderPage();

    expect(await screen.findByText(/chưa là giáo viên chủ nhiệm/i)).toBeInTheDocument();
  });

  it('does not show the "Người quyết định" picker for a TEACHER (read-only preview)', async () => {
    renderPage();
    await screen.findByRole('combobox', { name: 'Lớp' });
    expect(screen.queryByRole('combobox', { name: 'Người quyết định' })).not.toBeInTheDocument();
  });
});
