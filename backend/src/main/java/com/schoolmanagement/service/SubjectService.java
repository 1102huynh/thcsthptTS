package com.schoolmanagement.service;

import com.schoolmanagement.dto.SubjectDTO;
import com.schoolmanagement.entity.Subject;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.SubjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class SubjectService {

    private SubjectRepository subjectRepository;

    public SubjectDTO createSubject(Subject subject) {
        if (subjectRepository.existsByCode(subject.getCode())) {
            throw new DuplicateResourceException("Subject code already exists: " + subject.getCode());
        }
        return mapToDTO(subjectRepository.save(subject));
    }

    public SubjectDTO updateSubject(Long id, Subject details) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

        if (!subject.getCode().equals(details.getCode()) && subjectRepository.existsByCode(details.getCode())) {
            throw new DuplicateResourceException("Subject code already exists: " + details.getCode());
        }

        subject.setCode(details.getCode());
        subject.setName(details.getName());
        subject.setGradeLevels(details.getGradeLevels());
        subject.setCategory(details.getCategory());

        return mapToDTO(subjectRepository.save(subject));
    }

    public SubjectDTO getSubjectById(Long id) {
        return mapToDTO(subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id)));
    }

    public List<SubjectDTO> getAllSubjects() {
        return subjectRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void deleteSubject(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
        subjectRepository.delete(subject);
    }

    private SubjectDTO mapToDTO(Subject subject) {
        return SubjectDTO.builder()
                .id(subject.getId())
                .code(subject.getCode())
                .name(subject.getName())
                .gradeLevels(subject.getGradeLevels())
                .category(subject.getCategory())
                .createdAt(subject.getCreatedAt())
                .updatedAt(subject.getUpdatedAt())
                .build();
    }
}
