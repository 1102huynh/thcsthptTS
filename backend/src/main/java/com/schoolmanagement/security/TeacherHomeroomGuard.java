package com.schoolmanagement.security;

import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.SchoolClassRepository;
import com.schoolmanagement.repository.StaffRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Shared "may this TEACHER touch this class/student" check for the GVCN
 * (homeroom teacher) scoping rule (KE_HOACH_NANG_CAP_V4.md H.3.1) — a
 * TEACHER may only manage students, attendance, conduct, and lên lớp
 * previews for the class(es) they are {@code classTeacher} of; ADMIN/
 * PRINCIPAL are unrestricted. Extracted from the private
 * {@code enforceHomeroomWriteAccess} that used to live only in
 * {@code ConductRecordService} (the one place this was already enforced,
 * for conduct writes) once Student/Attendance/Promotion needed the
 * identical check.
 *
 * <p>Mirrors {@link StudentAccessGuard}'s "only narrows, never grants"
 * contract: assumes {@code @PreAuthorize} already ran and this only
 * tightens further for a TEACHER caller, leaving every other role at
 * whatever {@code @PreAuthorize} already allowed.
 */
@Component
@AllArgsConstructor
public class TeacherHomeroomGuard {

    private StaffRepository staffRepository;
    private SchoolClassRepository schoolClassRepository;

    /**
     * The Staff profile linked to this caller's account. Callers should only
     * invoke this once they've confirmed the caller's role is TEACHER — it
     * always throws if no Staff is linked, regardless of role.
     */
    public Staff resolveOwnStaff(User requester) {
        return staffRepository.findByUserId(requester.getId())
                .orElseThrow(() -> new AccessDeniedException("No staff profile linked to this account"));
    }

    /** Every class this TEACHER is GVCN (homeroom teacher) of. Same caveat as {@link #resolveOwnStaff}. */
    public List<SchoolClass> getHomeroomClasses(User requester) {
        return schoolClassRepository.findByClassTeacher(resolveOwnStaff(requester));
    }

    /**
     * No-op for {@code null}/non-TEACHER (ADMIN/PRINCIPAL unrestricted); for
     * TEACHER, throws {@link AccessDeniedException} unless {@code classId} is
     * one of their homeroom classes. Matches on {@code SchoolClass.getId()}
     * directly — unambiguous across academic years, unlike className/section
     * (see {@link #enforceHomeroomClassNameSection}).
     */
    public void enforceHomeroomClassId(Long classId, User requester) {
        if (requester == null || requester.getRole() != Role.TEACHER) {
            return;
        }
        boolean isHomeroom = getHomeroomClasses(requester).stream()
                .anyMatch(cls -> cls.getId().equals(classId));
        if (!isHomeroom) {
            throw new AccessDeniedException("Only the class's GVCN (homeroom teacher) may access this class");
        }
    }

    /**
     * Same intent as {@link #enforceHomeroomClassId} but for call sites that
     * only have a Student's className/section strings, not a SchoolClass id
     * (Student/Attendance still model class membership that way). className/
     * section isn't unique across academic years, so this can over-match a
     * name/section reused across years — a pre-existing limitation of that
     * convention (see the original comment this was extracted from in
     * ConductRecordService), not new here.
     */
    public void enforceHomeroomClassNameSection(String className, String section, User requester) {
        if (requester == null || requester.getRole() != Role.TEACHER) {
            return;
        }
        boolean isHomeroom = getHomeroomClasses(requester).stream()
                .anyMatch(cls -> cls.getClassName().equals(className) && cls.getSection().equals(section));
        if (!isHomeroom) {
            throw new AccessDeniedException("Only the class's GVCN (homeroom teacher) may access this class");
        }
    }

    /**
     * Returns {@code students} unchanged for {@code null}/non-TEACHER;
     * otherwise keeps only the ones in one of this TEACHER's homeroom
     * classes (matched by className/section, same caveat as above).
     */
    public List<Student> filterToHomeroom(List<Student> students, User requester) {
        if (requester == null || requester.getRole() != Role.TEACHER) {
            return students;
        }
        List<SchoolClass> homeroomClasses = getHomeroomClasses(requester);
        return students.stream()
                .filter(s -> homeroomClasses.stream().anyMatch(cls ->
                        cls.getClassName().equals(s.getClassName()) && cls.getSection().equals(s.getSection())))
                .collect(Collectors.toList());
    }
}
