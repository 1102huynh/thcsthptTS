import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

const svc = {
  search: vi.fn(),
  setEnabled: vi.fn(),
};

vi.mock('@/services/dataService', () => ({
  userService: {
    search: (...a) => svc.search(...a),
    setEnabled: (...a) => svc.setEnabled(...a),
  },
}));

const getCurrentUser = vi.fn();
vi.mock('@/services/authService', () => ({
  getCurrentUser: (...a) => getCurrentUser(...a),
}));

import UserAccountManagement from './UserAccountManagement';

const page = (content) => ({
  data: { content, totalPages: 1, totalElements: content.length },
});

const admin = { id: 1, username: 'admin1', firstName: 'Admin', lastName: 'Root', email: 'admin1@school.com', role: 'ADMIN', enabled: true };
const teacher = { id: 2, username: 'teacher1', firstName: 'Van', lastName: 'Nguyen', email: 'teacher1@school.com', role: 'TEACHER', enabled: true };
const lockedStudent = { id: 3, username: 'student1', firstName: 'Thi', lastName: 'Tran', email: 'student1@school.com', role: 'STUDENT', enabled: false };

function renderPage() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={qc}>
      <UserAccountManagement />
    </QueryClientProvider>
  );
}

beforeEach(() => {
  svc.search.mockReset();
  svc.setEnabled.mockReset();
  getCurrentUser.mockReset();
  getCurrentUser.mockReturnValue({ userId: 1 });
  svc.search.mockResolvedValue(page([admin, teacher, lockedStudent]));
});

describe('UserAccountManagement', () => {
  it('loads and shows every account with its role and status', async () => {
    renderPage();
    expect(await screen.findByText('teacher1')).toBeInTheDocument();
    expect(screen.getByText('student1')).toBeInTheDocument();
    expect(screen.getAllByText('Đang hoạt động')).toHaveLength(2); // admin + teacher
    expect(screen.getByText('Đã khoá')).toBeInTheDocument(); // student
  });

  it('re-queries with the selected role when the filter changes', async () => {
    const user = userEvent.setup();
    renderPage();
    await screen.findByText('teacher1');

    await user.click(screen.getByRole('combobox', { name: 'Vai trò' }));
    await user.click(await screen.findByRole('option', { name: 'Giáo viên' }));

    await waitFor(() =>
      expect(svc.search).toHaveBeenLastCalledWith(expect.objectContaining({ role: 'TEACHER', page: 0 }))
    );
  });

  it('locks an account after the confirmation dialog is accepted', async () => {
    svc.setEnabled.mockResolvedValue({ data: { ...teacher, enabled: false } });
    const user = userEvent.setup();
    renderPage();

    const teacherRow = (await screen.findByText('teacher1')).closest('tr');
    await user.click(within(teacherRow).getByRole('button', { name: /Khoá/ }));

    const dialog = await screen.findByRole('alertdialog');
    await user.click(within(dialog).getByRole('button', { name: 'Khoá' }));

    await waitFor(() => expect(svc.setEnabled).toHaveBeenCalledWith(2, false));
  });

  it('unlocks a locked account directly, without a confirmation dialog', async () => {
    svc.setEnabled.mockResolvedValue({ data: { ...lockedStudent, enabled: true } });
    const user = userEvent.setup();
    renderPage();

    const studentRow = (await screen.findByText('student1')).closest('tr');
    await user.click(within(studentRow).getByRole('button', { name: /Mở khoá/ }));

    await waitFor(() => expect(svc.setEnabled).toHaveBeenCalledWith(3, true));
    expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
  });

  it("disables the lock button on the signed-in admin's own row", async () => {
    renderPage();
    const adminRow = (await screen.findByText('admin1')).closest('tr');
    expect(within(adminRow).getByRole('button', { name: /Khoá/ })).toBeDisabled();
  });
});
