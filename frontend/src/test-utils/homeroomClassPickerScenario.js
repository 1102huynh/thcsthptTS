import { it, expect } from 'vitest';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

/**
 * Shared H.3.1 behavior test: a TEACHER's "Lớp" picker must only ever offer
 * the class(es) they are GVCN (homeroom teacher) of, and the page must say
 * so instead of showing an empty/broken picker when they aren't GVCN of any
 * class. AttendanceManagement and PromotionManagement both have this exact
 * picker (and TimetableManagement/StudentManagement need the equivalent
 * "not GVCN of any class" message without a picker at all) - factored out
 * here instead of duplicated per page, which is what tripped SonarCloud's
 * new-code duplication gate the first time this was written per-file.
 *
 * @param {() => void} renderPage
 * @param {import('vitest').Mock} classesService - the mocked schoolClassService.getAll (or equivalent)
 * @param {object} nonHomeroomClass - a class payload where the TEACHER is NOT classTeacherId
 */
export function itScopesClassPickerToHomeroom({ renderPage, classesService, nonHomeroomClass }) {
  it('only offers the class(es) the TEACHER is GVCN of in the class picker', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole('combobox', { name: 'Lớp' }));
    expect(await screen.findByRole('option', { name: /10 - A1/ })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: /10 - A2/ })).not.toBeInTheDocument();
  });

  it('shows a message when the TEACHER is not GVCN of any class', async () => {
    classesService.mockResolvedValue({ data: [nonHomeroomClass] });
    renderPage();

    expect(await screen.findByText(/chưa là giáo viên chủ nhiệm/i)).toBeInTheDocument();
  });
}
