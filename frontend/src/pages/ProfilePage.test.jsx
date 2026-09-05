import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { toast } from 'sonner';

const svc = {
  getMe: vi.fn(),
  updateMe: vi.fn(),
  changePassword: vi.fn(),
};

vi.mock('@/services/dataService', () => ({
  userService: {
    getMe: (...a) => svc.getMe(...a),
    updateMe: (...a) => svc.updateMe(...a),
    changePassword: (...a) => svc.changePassword(...a),
  },
}));

// ProfilePage doesn't mount its own <Toaster/> (that lives at the App root),
// so toast.success/error calls have nothing to render into in this test tree
// - mock the module and assert on the calls directly instead.
vi.mock('sonner', () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

import ProfilePage from './ProfilePage';

const profile = {
  id: 1,
  username: 'student1',
  email: 'student1@school.com',
  firstName: 'Van',
  lastName: 'Nguyen',
  phoneNumber: '0900000000',
  role: 'STUDENT',
};

function renderPage(props = {}) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={qc}>
      <ProfilePage {...props} />
    </QueryClientProvider>
  );
}

beforeEach(() => {
  Object.values(svc).forEach((fn) => fn.mockReset());
  toast.success.mockReset();
  toast.error.mockReset();
  svc.getMe.mockResolvedValue({ data: { ...profile } });
});

describe('ProfilePage', () => {
  it('loads the profile via getMe and shows the current fields', async () => {
    renderPage();
    expect(await screen.findByDisplayValue('Van')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Nguyen')).toBeInTheDocument();
    expect(screen.getByDisplayValue('student1@school.com')).toBeInTheDocument();
    expect(screen.getByText(/student1/)).toBeInTheDocument();
  });

  it('saves profile changes and reports the update back to the caller', async () => {
    const onUserUpdate = vi.fn();
    svc.updateMe.mockResolvedValue({ data: { ...profile, firstName: 'Thi' } });
    const user = userEvent.setup();
    renderPage({ onUserUpdate });

    const firstNameInput = await screen.findByDisplayValue('Van');
    await user.clear(firstNameInput);
    await user.type(firstNameInput, 'Thi');
    await user.click(screen.getByRole('button', { name: 'Lưu thay đổi' }));

    await waitFor(() =>
      expect(svc.updateMe).toHaveBeenCalledWith(
        expect.objectContaining({ firstName: 'Thi', lastName: 'Nguyen', email: 'student1@school.com' })
      )
    );
    await waitFor(() => expect(onUserUpdate).toHaveBeenCalledWith(expect.objectContaining({ firstName: 'Thi' })));
  });

  it('rejects a mismatched password confirmation before calling the API', async () => {
    const user = userEvent.setup();
    renderPage();
    await screen.findByDisplayValue('Van');

    await user.type(screen.getByLabelText('Mật khẩu hiện tại'), 'OldPassw0rd!');
    await user.type(screen.getByLabelText('Mật khẩu mới'), 'N3wPassw0rd!');
    await user.type(screen.getByLabelText('Nhập lại mật khẩu mới'), 'Mismatch1!');
    await user.click(screen.getByRole('button', { name: 'Đổi mật khẩu' }));

    expect(await screen.findByText('Mật khẩu nhập lại không khớp')).toBeInTheDocument();
    expect(svc.changePassword).not.toHaveBeenCalled();
  });

  it('changes the password when the new password is confirmed correctly', async () => {
    svc.changePassword.mockResolvedValue({ data: { message: 'ok' } });
    const user = userEvent.setup();
    renderPage();
    await screen.findByDisplayValue('Van');

    await user.type(screen.getByLabelText('Mật khẩu hiện tại'), 'OldPassw0rd!');
    await user.type(screen.getByLabelText('Mật khẩu mới'), 'N3wPassw0rd!');
    await user.type(screen.getByLabelText('Nhập lại mật khẩu mới'), 'N3wPassw0rd!');
    await user.click(screen.getByRole('button', { name: 'Đổi mật khẩu' }));

    await waitFor(() =>
      expect(svc.changePassword).toHaveBeenCalledWith({
        currentPassword: 'OldPassw0rd!',
        newPassword: 'N3wPassw0rd!',
      })
    );
  });

  it('shows the server error and does not call onUserUpdate when the profile save fails', async () => {
    const onUserUpdate = vi.fn();
    svc.updateMe.mockRejectedValue({ response: { data: { message: 'Email already exists' } } });
    const user = userEvent.setup();
    renderPage({ onUserUpdate });

    await screen.findByDisplayValue('Van');
    await user.click(screen.getByRole('button', { name: 'Lưu thay đổi' }));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith('Email already exists'));
    expect(onUserUpdate).not.toHaveBeenCalled();
  });

  it('shows the server error when changing the password fails (e.g. wrong current password)', async () => {
    svc.changePassword.mockRejectedValue({ response: { data: { message: 'Mật khẩu hiện tại không đúng' } } });
    const user = userEvent.setup();
    renderPage();
    await screen.findByDisplayValue('Van');

    await user.type(screen.getByLabelText('Mật khẩu hiện tại'), 'WrongPassword!');
    await user.type(screen.getByLabelText('Mật khẩu mới'), 'N3wPassw0rd!');
    await user.type(screen.getByLabelText('Nhập lại mật khẩu mới'), 'N3wPassw0rd!');
    await user.click(screen.getByRole('button', { name: 'Đổi mật khẩu' }));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith('Mật khẩu hiện tại không đúng'));
  });
});
