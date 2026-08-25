package com.schoolmanagement.security;

import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.ParentStudentRelationRepository;
import com.schoolmanagement.repository.StudentRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Shared "may this caller see/touch this student's records" check, used by
 * every per-student endpoint across grades (3.3), conduct (3.4), promotions
 * (3.5), and the legacy grades/attendance/fees modules (Phase 1-2, retrofitted
 * in 3.6). Was duplicated as a private method in three services before this
 * extraction (GradeRecordService, ConductRecordService, PromotionService all
 * had their own identical enforceOwnStudentAccess) — consolidated here once a
 * fourth and fifth need (the legacy modules' new PARENT support) made three
 * copies untenable.
 *
 * <p>Only STUDENT and PARENT callers are restricted; every other role
 * (ADMIN/TEACHER/PRINCIPAL/...) is left to whatever {@code @PreAuthorize}
 * already gated at the endpoint — this class only ever narrows, never grants,
 * broader access than the caller's role already gets past @PreAuthorize.
 */
@Component
@AllArgsConstructor
public class StudentAccessGuard {

    private StudentRepository studentRepository;
    private ParentStudentRelationRepository parentStudentRelationRepository;

    public void enforceCanAccessStudent(Long targetStudentId, User requester) {
        if (requester == null) {
            return;
        }

        if (requester.getRole() == Role.STUDENT) {
            Student own = studentRepository.findByUserId(requester.getId())
                    .orElseThrow(() -> new AccessDeniedException("No student profile linked to this account"));
            if (!own.getId().equals(targetStudentId)) {
                throw new AccessDeniedException("Students may only access their own records");
            }
        } else if (requester.getRole() == Role.PARENT) {
            // A missing target id is a 404, not a 403 — this is about whether the
            // *target student* exists, unlike the STUDENT branch above where a
            // missing lookup is about the *caller's own* profile.
            Student target = studentRepository.findById(targetStudentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + targetStudentId));
            boolean isParentOfStudent = parentStudentRelationRepository
                    .existsByParentAndStudent(requester, target);
            if (!isParentOfStudent) {
                throw new AccessDeniedException("Parents may only access their own children's records");
            }
        }
    }
}
