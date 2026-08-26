package com.schoolmanagement.service;

import com.schoolmanagement.dto.ParentStudentRelationDTO;
import com.schoolmanagement.entity.ParentRelationship;
import com.schoolmanagement.entity.ParentStudentRelation;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.ParentStudentRelationRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Links PARENT-role {@link User} accounts to the {@link Student}s they're
 * the parent/guardian of — per IMPLEMENTATION_PLAN.md 3.6. ADMIN-managed
 * (linking/unlinking a family relationship is an administrative action, not
 * self-service); a PARENT may only list their own children.
 */
@Service
@AllArgsConstructor
@Transactional
public class ParentService {

    private ParentStudentRelationRepository parentStudentRelationRepository;
    private UserRepository userRepository;
    private StudentRepository studentRepository;

    public ParentStudentRelationDTO linkChild(Long parentId, Long studentId, ParentRelationship relationship, boolean isPrimaryContact) {
        User parent = resolveParentUser(parentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        if (parentStudentRelationRepository.existsByParentAndStudent(parent, student)) {
            throw new DuplicateResourceException(
                    "Parent " + parentId + " is already linked to student " + studentId);
        }

        ParentStudentRelation relation = ParentStudentRelation.builder()
                .parent(parent)
                .student(student)
                .relationship(relationship)
                .isPrimaryContact(isPrimaryContact)
                .build();

        try {
            return mapToDTO(parentStudentRelationRepository.save(relation));
        } catch (DataIntegrityViolationException ex) {
            // Two concurrent requests for the same (parent, student) pair can both
            // pass the exists() check above before either commits; surface that
            // race as 409, not a masked 500.
            throw new DuplicateResourceException(
                    "Parent " + parentId + " is already linked to student " + studentId);
        }
    }

    public void unlinkChild(Long parentId, Long studentId) {
        User parent = resolveParentUser(parentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        ParentStudentRelation relation = parentStudentRelationRepository.findByParentAndStudent(parent, student)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No relation between parent " + parentId + " and student " + studentId));

        parentStudentRelationRepository.delete(relation);
    }

    public List<ParentStudentRelationDTO> getChildren(Long parentId, User requester) {
        if (requester != null && requester.getRole() == Role.PARENT && !requester.getId().equals(parentId)) {
            throw new AccessDeniedException("Parents may only view their own children");
        }

        User parent = resolveParentUser(parentId);
        return parentStudentRelationRepository.findByParent(parent)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private User resolveParentUser(Long parentId) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + parentId));
        if (parent.getRole() != Role.PARENT) {
            throw new IllegalArgumentException("User " + parentId + " is not a PARENT-role account");
        }
        return parent;
    }

    private ParentStudentRelationDTO mapToDTO(ParentStudentRelation relation) {
        User parent = relation.getParent();
        Student student = relation.getStudent();

        return ParentStudentRelationDTO.builder()
                .id(relation.getId())
                .parentId(parent.getId())
                .parentName(parent.getFirstName() + " " + parent.getLastName())
                .studentId(student.getId())
                .studentName(student.getUser() != null
                        ? student.getUser().getFirstName() + " " + student.getUser().getLastName()
                        : null)
                .rollNumber(student.getRollNumber())
                .relationship(relation.getRelationship())
                .isPrimaryContact(relation.getIsPrimaryContact())
                .createdAt(relation.getCreatedAt())
                .build();
    }
}
