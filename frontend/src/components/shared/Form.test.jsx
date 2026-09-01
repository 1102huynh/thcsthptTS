import React from 'react';
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { z } from 'zod';
import { useAppForm, AppForm } from './Form';
import { TextField } from './FormFields';
import { Button } from '@/components/ui/button';

const schema = z.object({
  fullName: z.string().min(1, 'Vui lòng nhập họ tên'),
  email: z.string().email('Email không hợp lệ'),
});

function TestForm({ onSubmit, defaultValues }) {
  const form = useAppForm({ schema, defaultValues: defaultValues ?? { fullName: '', email: '' } });
  return (
    <AppForm form={form} onSubmit={onSubmit}>
      <TextField control={form.control} name="fullName" label="Họ tên" />
      <TextField control={form.control} name="email" label="Email" />
      <Button type="submit">Lưu</Button>
    </AppForm>
  );
}

describe('AppForm / useAppForm', () => {
  it('calls onSubmit with the parsed values once every field passes Zod validation', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    render(<TestForm onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText('Họ tên'), 'Nguyễn Văn A');
    await user.type(screen.getByLabelText('Email'), 'a@example.com');
    await user.click(screen.getByRole('button', { name: 'Lưu' }));

    expect(onSubmit).toHaveBeenCalledTimes(1);
    expect(onSubmit.mock.calls[0][0]).toEqual({ fullName: 'Nguyễn Văn A', email: 'a@example.com' });
  });

  it('shows the Zod message and does not call onSubmit when a required field is left blank', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    render(<TestForm onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText('Email'), 'a@example.com');
    await user.click(screen.getByRole('button', { name: 'Lưu' }));

    expect(await screen.findByText('Vui lòng nhập họ tên')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('shows a field-specific message for a value that fails its own validator', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    render(<TestForm onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText('Họ tên'), 'Nguyễn Văn A');
    await user.type(screen.getByLabelText('Email'), 'not-an-email');
    await user.click(screen.getByRole('button', { name: 'Lưu' }));

    expect(await screen.findByText('Email không hợp lệ')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('pre-fills fields from defaultValues', () => {
    render(<TestForm onSubmit={() => {}} defaultValues={{ fullName: 'Có sẵn', email: '' }} />);
    expect(screen.getByLabelText('Họ tên')).toHaveValue('Có sẵn');
  });
});
