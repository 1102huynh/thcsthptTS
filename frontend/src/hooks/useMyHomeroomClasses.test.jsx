import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

const svc = { staffGetAll: vi.fn(), classesGetAll: vi.fn() };

vi.mock('@/services/dataService', () => ({
  staffService: { getAll: (...a) => svc.staffGetAll(...a) },
  schoolClassService: { getAll: (...a) => svc.classesGetAll(...a) },
}));

const getCurrentUser = vi.fn();
vi.mock('@/services/authService', () => ({
  getCurrentUser: (...a) => getCurrentUser(...a),
}));

import { useMyHomeroomClasses } from './useMyHomeroomClasses';

function wrapper({ children }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={qc}>{children}</QueryClientProvider>;
}

beforeEach(() => {
  svc.staffGetAll.mockReset();
  svc.classesGetAll.mockReset();
  getCurrentUser.mockReset();
});

describe('useMyHomeroomClasses', () => {
  it('resolves myStaffId and filters classes down to the ones this staff is classTeacher of', async () => {
    getCurrentUser.mockReturnValue({ userId: 42 });
    svc.staffGetAll.mockResolvedValue({
      data: [
        { id: 1, user: { id: 42 } },
        { id: 2, user: { id: 99 } },
      ],
    });
    svc.classesGetAll.mockResolvedValue({
      data: [
        { id: 10, className: '10', section: 'A1', classTeacherId: 1 },
        { id: 11, className: '10', section: 'A2', classTeacherId: 2 },
      ],
    });

    const { result } = renderHook(() => useMyHomeroomClasses(), { wrapper });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.myStaffId).toBe(1);
    expect(result.current.homeroomClasses).toEqual([
      { id: 10, className: '10', section: 'A1', classTeacherId: 1 },
    ]);
    expect(result.current.allClasses).toHaveLength(2);
  });

  it('returns an empty homeroomClasses list when the caller is not GVCN of any class', async () => {
    getCurrentUser.mockReturnValue({ userId: 42 });
    svc.staffGetAll.mockResolvedValue({ data: [{ id: 1, user: { id: 42 } }] });
    svc.classesGetAll.mockResolvedValue({ data: [{ id: 10, classTeacherId: 999 }] });

    const { result } = renderHook(() => useMyHomeroomClasses(), { wrapper });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.homeroomClasses).toEqual([]);
  });

  it('leaves myStaffId undefined when the account has no linked staff profile', async () => {
    getCurrentUser.mockReturnValue({ userId: 42 });
    svc.staffGetAll.mockResolvedValue({ data: [{ id: 1, user: { id: 7 } }] });
    svc.classesGetAll.mockResolvedValue({ data: [{ id: 10, classTeacherId: 1 }] });

    const { result } = renderHook(() => useMyHomeroomClasses(), { wrapper });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.myStaffId).toBeUndefined();
    expect(result.current.homeroomClasses).toEqual([]);
  });
});
