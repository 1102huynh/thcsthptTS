package com.schoolmanagement.security;

import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.entity.SemesterName;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.Subject;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.SchoolClassRepository;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.TeachingAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for the GVBM (giáo viên bộ môn) scoping guard (H.3.1).
 * Integration-level coverage (real @PreAuthorize + endpoints) lives in
 * GradeRecordIntegrationTest.
 */
@ExtendWith(MockitoExtension.class)
class TeacherAssignmentGuardTest {

    @Mock
    private StaffRepository staffRepository;
    @Mock
    private SchoolClassRepository schoolClassRepository;
    @Mock
    private TeachingAssignmentRepository teachingAssignmentRepository;

    private TeacherAssignmentGuard guard;

    private User adminUser;
    private User teacherUser;
    private Staff teacherStaff;
    private Student student;
    private Subject subject;
    private Semester semester;
    private SchoolClass schoolClass;

    @BeforeEach
    void setUp() {
        guard = new TeacherAssignmentGuard(staffRepository, schoolClassRepository, teachingAssignmentRepository);

        adminUser = User.builder().id(1L).role(Role.ADMIN).build();
        teacherUser = User.builder().id(2L).role(Role.TEACHER).build();
        teacherStaff = Staff.builder().id(10L).build();
        student = Student.builder().id(100L).className("10").section("A1").build();
        subject = Subject.builder().id(200L).build();
        schoolClass = SchoolClass.builder().id(300L).className("10").section("A1").build();

        com.schoolmanagement.entity.AcademicYear academicYear =
                com.schoolmanagement.entity.AcademicYear.builder().id(1L).name("2025-2026")
                        .startDate(LocalDate.of(2025, 9, 1)).endDate(LocalDate.of(2026, 5, 31)).build();
        semester = Semester.builder().id(400L).name(SemesterName.HK1).academicYear(academicYear).build();
    }

    @Test
    void enforceHasAssignment_adminIsUnrestricted_noRepositoryLookup() {
        guard.enforceHasAssignment(student, subject, semester, adminUser);
        // No exception, and no need to ever resolve class/assignment for ADMIN.
    }

    @Test
    void enforceHasAssignment_nullRequester_noOp() {
        guard.enforceHasAssignment(student, subject, semester, null);
    }

    @Test
    void enforceHasAssignment_teacherWithNoStaffProfile_throws403() {
        when(schoolClassRepository.findByClassNameAndSectionAndAcademicYear("10", "A1", "2025-2026"))
                .thenReturn(Optional.of(schoolClass));
        when(staffRepository.findByUserId(2L)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class,
                () -> guard.enforceHasAssignment(student, subject, semester, teacherUser));
    }

    @Test
    void enforceHasAssignment_noMatchingSchoolClass_throws403() {
        when(schoolClassRepository.findByClassNameAndSectionAndAcademicYear("10", "A1", "2025-2026"))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class,
                () -> guard.enforceHasAssignment(student, subject, semester, teacherUser));
    }

    @Test
    void enforceHasAssignment_teacherHasMatchingAssignment_passes() {
        when(schoolClassRepository.findByClassNameAndSectionAndAcademicYear("10", "A1", "2025-2026"))
                .thenReturn(Optional.of(schoolClass));
        when(staffRepository.findByUserId(2L)).thenReturn(Optional.of(teacherStaff));
        when(teachingAssignmentRepository
                .existsByTeacherAndSchoolClassAndSubjectAndSemester(teacherStaff, schoolClass, subject, semester))
                .thenReturn(true);

        guard.enforceHasAssignment(student, subject, semester, teacherUser);
    }

    @Test
    void enforceHasAssignment_teacherHasNoMatchingAssignment_throws403() {
        when(schoolClassRepository.findByClassNameAndSectionAndAcademicYear("10", "A1", "2025-2026"))
                .thenReturn(Optional.of(schoolClass));
        when(staffRepository.findByUserId(2L)).thenReturn(Optional.of(teacherStaff));
        when(teachingAssignmentRepository
                .existsByTeacherAndSchoolClassAndSubjectAndSemester(teacherStaff, schoolClass, subject, semester))
                .thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> guard.enforceHasAssignment(student, subject, semester, teacherUser));
    }

    // ---- hasAssignmentForClass (subject-agnostic, non-throwing - used by AttendanceService) ----

    @Test
    void hasAssignmentForClass_teacherHasAssignmentForClass_returnsTrue() {
        when(schoolClassRepository.findByClassNameAndSectionAndAcademicYear("10", "A1", "2025-2026"))
                .thenReturn(Optional.of(schoolClass));
        when(staffRepository.findByUserId(2L)).thenReturn(Optional.of(teacherStaff));
        when(teachingAssignmentRepository.existsByTeacherAndSchoolClassAndSemester(teacherStaff, schoolClass, semester))
                .thenReturn(true);

        assertThat(guard.hasAssignmentForClass("10", "A1", semester, teacherUser)).isTrue();
    }

    @Test
    void hasAssignmentForClass_teacherHasNoAssignmentForClass_returnsFalse() {
        when(schoolClassRepository.findByClassNameAndSectionAndAcademicYear("10", "A1", "2025-2026"))
                .thenReturn(Optional.of(schoolClass));
        when(staffRepository.findByUserId(2L)).thenReturn(Optional.of(teacherStaff));
        when(teachingAssignmentRepository.existsByTeacherAndSchoolClassAndSemester(teacherStaff, schoolClass, semester))
                .thenReturn(false);

        assertThat(guard.hasAssignmentForClass("10", "A1", semester, teacherUser)).isFalse();
    }

    @Test
    void hasAssignmentForClass_noMatchingSchoolClass_returnsFalse() {
        when(schoolClassRepository.findByClassNameAndSectionAndAcademicYear("10", "A1", "2025-2026"))
                .thenReturn(Optional.empty());

        assertThat(guard.hasAssignmentForClass("10", "A1", semester, teacherUser)).isFalse();
    }
}
