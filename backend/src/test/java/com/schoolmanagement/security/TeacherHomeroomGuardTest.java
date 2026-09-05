package com.schoolmanagement.security;

import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.SchoolClassRepository;
import com.schoolmanagement.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for the GVCN (homeroom teacher) scoping guard (H.3.1),
 * extracted from ConductRecordService's original enforceHomeroomWriteAccess.
 * Integration-level coverage (real @PreAuthorize + endpoints) lives in each
 * feature's own *IntegrationTest.
 */
@ExtendWith(MockitoExtension.class)
class TeacherHomeroomGuardTest {

    @Mock
    private StaffRepository staffRepository;
    @Mock
    private SchoolClassRepository schoolClassRepository;

    private TeacherHomeroomGuard guard;

    private User adminUser;
    private User teacherUser;
    private Staff teacherStaff;
    private SchoolClass homeroomClass;

    @BeforeEach
    void setUp() {
        guard = new TeacherHomeroomGuard(staffRepository, schoolClassRepository);

        adminUser = User.builder().id(1L).role(Role.ADMIN).build();
        teacherUser = User.builder().id(2L).role(Role.TEACHER).build();
        teacherStaff = Staff.builder().id(10L).build();
        homeroomClass = SchoolClass.builder().id(100L).className("10").section("A1").build();
    }

    @Test
    void enforceHomeroomClassId_adminIsUnrestricted_noRepositoryLookup() {
        guard.enforceHomeroomClassId(999L, adminUser);
        // No exception, and no need to ever resolve a Staff profile for ADMIN.
    }

    @Test
    void enforceHomeroomClassId_nullRequester_noOp() {
        guard.enforceHomeroomClassId(999L, null);
    }

    @Test
    void enforceHomeroomClassId_teacherWithNoStaffProfile_throws403() {
        when(staffRepository.findByUserId(2L)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> guard.enforceHomeroomClassId(100L, teacherUser));
    }

    @Test
    void enforceHomeroomClassId_teacherIsHomeroomOfThatClass_passes() {
        when(staffRepository.findByUserId(2L)).thenReturn(Optional.of(teacherStaff));
        when(schoolClassRepository.findByClassTeacher(teacherStaff)).thenReturn(List.of(homeroomClass));

        guard.enforceHomeroomClassId(100L, teacherUser);
    }

    @Test
    void enforceHomeroomClassId_teacherIsNotHomeroomOfThatClass_throws403() {
        when(staffRepository.findByUserId(2L)).thenReturn(Optional.of(teacherStaff));
        when(schoolClassRepository.findByClassTeacher(teacherStaff)).thenReturn(List.of(homeroomClass));

        assertThrows(AccessDeniedException.class, () -> guard.enforceHomeroomClassId(999L, teacherUser));
    }

    @Test
    void enforceHomeroomClassNameSection_teacherIsHomeroom_passes() {
        when(staffRepository.findByUserId(2L)).thenReturn(Optional.of(teacherStaff));
        when(schoolClassRepository.findByClassTeacher(teacherStaff)).thenReturn(List.of(homeroomClass));

        guard.enforceHomeroomClassNameSection("10", "A1", teacherUser);
    }

    @Test
    void enforceHomeroomClassNameSection_teacherIsNotHomeroom_throws403() {
        when(staffRepository.findByUserId(2L)).thenReturn(Optional.of(teacherStaff));
        when(schoolClassRepository.findByClassTeacher(teacherStaff)).thenReturn(List.of(homeroomClass));

        assertThrows(AccessDeniedException.class,
                () -> guard.enforceHomeroomClassNameSection("10", "A2", teacherUser));
    }

    @Test
    void filterToHomeroom_nonTeacher_returnsListUnchanged() {
        List<Student> students = List.of(
                Student.builder().id(1L).className("10").section("A1").build(),
                Student.builder().id(2L).className("11").section("B2").build());

        assertThat(guard.filterToHomeroom(students, adminUser)).isEqualTo(students);
    }

    @Test
    void filterToHomeroom_teacher_keepsOnlyStudentsInHomeroomClasses() {
        when(staffRepository.findByUserId(2L)).thenReturn(Optional.of(teacherStaff));
        when(schoolClassRepository.findByClassTeacher(teacherStaff)).thenReturn(List.of(homeroomClass));

        Student inHomeroom = Student.builder().id(1L).className("10").section("A1").build();
        Student notInHomeroom = Student.builder().id(2L).className("11").section("B2").build();

        List<Student> result = guard.filterToHomeroom(List.of(inHomeroom, notInHomeroom), teacherUser);

        assertThat(result).containsExactly(inHomeroom);
    }
}
