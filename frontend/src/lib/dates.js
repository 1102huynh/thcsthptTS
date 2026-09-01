// 'yyyy-MM-dd' -> Date, parsed as local midnight (not UTC) so the day shown
// never shifts depending on the viewer's timezone offset direction.
// Shared by every *FormDialog that pre-fills a DateField from a backend
// LocalDate string (StaffFormDialog, StudentFormDialog, ...).
export function parseLocalDate(value) {
  return value ? new Date(`${value}T00:00:00`) : undefined;
}
