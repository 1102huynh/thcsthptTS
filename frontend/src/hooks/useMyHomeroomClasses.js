import { useQuery } from '@tanstack/react-query';
import { staffService, schoolClassService } from '../services/dataService';
import { getCurrentUser } from '../services/authService';

/**
 * "Which class(es) am I GVCN (homeroom teacher) of" - shared by every page
 * scoped to a TEACHER's homeroom class (H.3.1: Student/Attendance/Conduct/
 * Promotion management, Timetable's own-schedule view). Extracted from the
 * inline lookup ConductManagement.jsx originally had (the one page this was
 * already enforced for) once Student/Attendance/Promotion/Timetable needed
 * the identical two-fetch lookup: no backend endpoint returns "my staff
 * profile" or "my homeroom classes" directly, so this fetches the whole
 * staff list once to find the caller's own staffId (matching `s.user?.id`
 * against the logged-in user's id), then the whole class list once to find
 * which ones have that staffId as `classTeacherId`.
 *
 * Fetches unconditionally regardless of role (matching ConductManagement's
 * original behavior) rather than gating on role === 'TEACHER' - an ADMIN
 * account can be linked to a Staff profile too (rare, but ConductManagement
 * always supported it for its own evaluatedBy field), and callers that only
 * care about the TEACHER case simply ignore `homeroomClasses` otherwise.
 */
export function useMyHomeroomClasses() {
  const userId = getCurrentUser()?.userId;

  const staffQuery = useQuery({ queryKey: ['staff-lookup'], queryFn: () => staffService.getAll().then((r) => r.data) });
  const allStaff = staffQuery.data ?? [];
  const myStaffId = allStaff.find((s) => s.user?.id === userId)?.id;

  const classesQuery = useQuery({ queryKey: ['classes'], queryFn: () => schoolClassService.getAll().then((r) => r.data) });
  const allClasses = classesQuery.data ?? [];
  const homeroomClasses = allClasses.filter((c) => c.classTeacherId === myStaffId);

  return {
    myStaffId,
    allStaff,
    allClasses,
    homeroomClasses,
    isLoading: staffQuery.isLoading || classesQuery.isLoading,
    isSuccess: staffQuery.isSuccess && classesQuery.isSuccess,
  };
}
