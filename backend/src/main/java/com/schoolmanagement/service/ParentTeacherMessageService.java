package com.schoolmanagement.service;

import com.schoolmanagement.dto.ParentTeacherMessageDTO;
import com.schoolmanagement.entity.*;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ParentTeacherMessageService {

    private final ParentTeacherMessageRepository messageRepository;
    private final ParentRepository parentRepository;
    private final StaffRepository staffRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public ParentTeacherMessageDTO sendMessage(ParentTeacherMessage message) {
        ParentTeacherMessage savedMessage = messageRepository.save(message);
        return mapToDTO(savedMessage);
    }

    public ParentTeacherMessageDTO getMessageById(Long id) {
        ParentTeacherMessage message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + id));
        return mapToDTO(message);
    }

    public List<ParentTeacherMessageDTO> getMessagesByParentId(Long parentId) {
        return messageRepository.findByParentIdOrderByCreatedAtDesc(parentId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ParentTeacherMessageDTO> getMessagesByTeacherId(Long teacherId) {
        return messageRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ParentTeacherMessageDTO> getUnreadMessagesByParentId(Long parentId) {
        return messageRepository.findUnreadMessagesByParentId(parentId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ParentTeacherMessageDTO> getUnreadMessagesByTeacherId(Long teacherId) {
        return messageRepository.findUnreadMessagesByTeacherId(teacherId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ParentTeacherMessageDTO markAsRead(Long messageId) {
        ParentTeacherMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        message.setIsRead(true);
        message.setReadAt(LocalDateTime.now());

        ParentTeacherMessage updatedMessage = messageRepository.save(message);
        return mapToDTO(updatedMessage);
    }

    public void deleteMessage(Long id) {
        if (!messageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Message not found with id: " + id);
        }
        messageRepository.deleteById(id);
    }

    private ParentTeacherMessageDTO mapToDTO(ParentTeacherMessage message) {
        return ParentTeacherMessageDTO.builder()
                .id(message.getId())
                .parentId(message.getParent().getId())
                .parentName(message.getParent().getUser().getFirstName() + " " +
                        message.getParent().getUser().getLastName())
                .teacherId(message.getTeacher().getId())
                .teacherName(message.getTeacher().getUser().getFirstName() + " " +
                        message.getTeacher().getUser().getLastName())
                .studentId(message.getStudent() != null ? message.getStudent().getId() : null)
                .studentName(message.getStudent() != null ?
                        message.getStudent().getUser().getFirstName() + " " +
                        message.getStudent().getUser().getLastName() : null)
                .subject(message.getSubject())
                .message(message.getMessage())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFirstName() + " " + message.getSender().getLastName())
                .isRead(message.getIsRead())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }
}

