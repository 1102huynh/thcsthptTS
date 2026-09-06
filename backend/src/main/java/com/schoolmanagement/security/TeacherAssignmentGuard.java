package com.schoolmanagement.security;

import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.Subject;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.SchoolClassRepository;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.TeachingAssignmentRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * "May this TEACHER record a grade for this student/subject/semester" check
 * for the GVBM (giáo viên bộ môn) scoping rule (KE_HOACH_NANG_CAP_V4.md
 * H.3.1) — a TEACHER may only create/update/delete a {@code GradeRecord} for
 * a (class, subject, semester) combination they hold a {@code
 * TeachingAssignment} for; ADMIN/PRINCIPAL are unrestricted. Mirrors {@link
 * TeacherHomeroomGuard}'s "only narrows, never grants" contract and overall
 * shape, but is a separate class — homeroom (GVCN) and teaching-assignment
 * (GVBM) are different scoping rules over different data, not variants of
 * the same check.
 *
 * <p>{@code GradeRecord} has no direct {@code SchoolClass} FK (only
 * student/subject/semester/teacher), and {@code Student.currentClass} — the
 * real FK — is never populated anywhere in this codebase, so the class is
 * resolved the same way {@code TeacherHomeroomGuard} does: from the
 * student's {@code className}/{@code section} strings. Unlike
 * {@code TeacherHomeroomGuard}, the semester's own academic year is folded
 * into that lookup ({@link SchoolClassRepository#findByClassNameAndSectionAndAcademicYear})
 * so a className/section reused across years can't resolve to the wrong
 * one.
 */
@Component
@AllArgsConstructor
public class TeacherAssignmentGuard {

    private StaffRepository staffRepository;
    private SchoolClassRepository schoolClassRepository;
    private TeachingAssignmentRepository teachingAssignmentRepository;

    /** The Staff profile linked to this caller's account. Callers should only invoke this once they've confirmed the caller's role is TEACHER. */
    public Staff resolveOwnStaff(User requester) {
        return staffRepository.findByUserId(requester.getId())
                .orElseThrow(() -> new AccessDeniedException("No staff profile linked to this account"));
    }

    /**
     * No-op for {@code null}/non-TEACHER; for TEACHER, throws
     * {@link AccessDeniedException} unless they hold a
     * {@code TeachingAssignment} for the student's class, this subject, and
     * this semester. A student with no resolvable class (missing className/
     * section, or no matching {@code SchoolClass} for this semester's
     * academic year) is treated the same as "no assignment" — deny.
     */
    public void enforceHasAssignment(Student student, Subject subject, Semester semester, User requester) {
        if (requester == null || requester.getRole() != Role.TEACHER) {
            return;
        }

        SchoolClass schoolClass = schoolClassRepository
                .findByClassNameAndSectionAndAcademicYear(
                        student.getClassName(), student.getSection(), semester.getAcademicYear().getName())
                .orElseThrow(() -> new AccessDeniedException(
                        "No class found matching this student for the record's academic year"));

        Staff staff = resolveOwnStaff(requester);
        boolean hasAssignment = teachingAssignmentRepository
                .existsByTeacherAndSchoolClassAndSubjectAndSemester(staff, schoolClass, subject, semester);
        if (!hasAssignment) {
            throw new AccessDeniedException(
                    "Only a teacher with a TeachingAssignment for this class/subject/semester may record grades here");
        }
    }
}
