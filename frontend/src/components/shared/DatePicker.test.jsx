import React from 'react';
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import DatePicker from './DatePicker';

describe('DatePicker', () => {
  it('shows the placeholder when no value is selected', () => {
    render(<DatePicker value={undefined} onChange={() => {}} placeholder="Chọn ngày" />);
    expect(screen.getByRole('button')).toHaveTextContent('Chọn ngày');
  });

  it('formats the selected date as dd/MM/yyyy', () => {
    render(<DatePicker value={new Date(2026, 8, 1)} onChange={() => {}} />);
    expect(screen.getByRole('button')).toHaveTextContent('01/09/2026');
  });

  it('opens the calendar when the trigger is clicked', async () => {
    const user = userEvent.setup();
    render(<DatePicker value={undefined} onChange={() => {}} />);
    await user.click(screen.getByRole('button'));
    expect(await screen.findByRole('grid')).toBeInTheDocument();
  });

  it('calls onChange with the picked date and closes the calendar', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    render(<DatePicker value={new Date(2026, 8, 1)} onChange={onChange} />);

    await user.click(screen.getByRole('button'));
    const grid = await screen.findByRole('grid');
    // The clickable day is a <button> inside the gridcell <td>, with a full
    // Vietnamese-localized aria-label (locale={vi}) rather than a bare "15"
    // - matched via regex here since the exact weekday-name prefix isn't
    // worth hardcoding.
    await user.click(screen.getByRole('button', { name: /ngày 15 tháng 09/ }));

    expect(onChange).toHaveBeenCalledTimes(1);
    const pickedDate = onChange.mock.calls[0][0];
    expect(pickedDate.getDate()).toBe(15);
    expect(pickedDate.getMonth()).toBe(8); // September, 0-indexed
    expect(grid).not.toBeInTheDocument(); // popover unmounts its content on close
  });

  it('disables the trigger when disabled is passed', () => {
    render(<DatePicker value={undefined} onChange={() => {}} disabled />);
    expect(screen.getByRole('button')).toBeDisabled();
  });
});
