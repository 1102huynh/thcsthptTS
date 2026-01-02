package com.schoolmanagement.service;

import com.schoolmanagement.dto.*;
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
public class ParentService {

    private final ParentRepository parentRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ParentTeacherMessageRepository messageRepository;
    private final AnnouncementRepository announcementRepository;
    private final ParentMeetingRepository meetingRepository;
    private final AttendanceRepository attendanceRepository;
    private final GradeRepository gradeRepository;

    public ParentDTO createParent(Parent parent) {
        Parent savedParent = parentRepository.save(parent);
        return mapToDTO(savedParent);
    }

    public ParentDTO updateParent(Long id, Parent parentDetails) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + id));

        if (parentDetails.getRelationToStudent() != null) {
            parent.setRelationToStudent(parentDetails.getRelationToStudent());
        }
        if (parentDetails.getOccupation() != null) {
            parent.setOccupation(parentDetails.getOccupation());
        }
        if (parentDetails.getOfficeAddress() != null) {
            parent.setOfficeAddress(parentDetails.getOfficeAddress());
        }
        if (parentDetails.getAnnualIncome() != null) {
            parent.setAnnualIncome(parentDetails.getAnnualIncome());
        }
        if (parentDetails.getNotificationEmailEnabled() != null) {
            parent.setNotificationEmailEnabled(parentDetails.getNotificationEmailEnabled());
        }
        if (parentDetails.getNotificationSmsEnabled() != null) {
            parent.setNotificationSmsEnabled(parentDetails.getNotificationSmsEnabled());
        }

        Parent updatedParent = parentRepository.save(parent);
        return mapToDTO(updatedParent);
    }

    public ParentDTO getParentById(Long id) {
        Parent parent = parentRepository.findByIdWithChildren(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + id));
        return mapToDTO(parent);
    }

    public ParentDTO getParentByUserId(Long userId) {
        Parent parent = parentRepository.findByUserIdWithChildren(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found for user id: " + userId));
        return mapToDTO(parent);
    }

    public List<ParentDTO> getAllParents() {
        return parentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void deleteParent(Long id) {
        if (!parentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Parent not found with id: " + id);
        }
        parentRepository.deleteById(id);
    }

    public void addChildToParent(Long parentId, Long studentId) {
        Parent parent = parentRepository.findByIdWithChildren(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + parentId));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        if (!parent.getChildren().contains(student)) {
            parent.getChildren().add(student);
            parentRepository.save(parent);
        }
    }

    public void removeChildFromParent(Long parentId, Long studentId) {
        Parent parent = parentRepository.findByIdWithChildren(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + parentId));

        parent.getChildren().removeIf(child -> child.getId().equals(studentId));
        parentRepository.save(parent);
    }

    public ParentDashboardDTO getParentDashboard(Long userId) {
        Parent parent = parentRepository.findByUserIdWithChildren(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found for user id: " + userId));

        // Get dashboard stats
        int unreadMessagesCount = messageRepository.findUnreadMessagesByParentId(parent.getId()).size();
        int upcomingMeetingsCount = meetingRepository.findByParentIdAndStatus(
                parent.getId(), ParentMeeting.MeetingStatus.SCHEDULED).size();
        List<AnnouncementDTO> announcements = announcementRepository
                .findActiveAnnouncementsByTarget(Announcement.AnnouncementTarget.PARENTS, LocalDateTime.now())
                .stream()
                .limit(5)
                .map(this::mapAnnouncementToDTO)
                .collect(Collectors.toList());

        ParentDashboardDTO.DashboardStats stats = ParentDashboardDTO.DashboardStats.builder()
                .totalChildren(parent.getChildren().size())
                .unreadMessages(unreadMessagesCount)
                .upcomingMeetings(upcomingMeetingsCount)
                .activeAnnouncements(announcements.size())
                .build();

        // Get upcoming meetings
        List<ParentMeetingDTO> upcomingMeetings = meetingRepository
                .findUpcomingMeetingsForParent(parent.getId(), LocalDateTime.now(), LocalDateTime.now().plusMonths(1))
                .stream()
                .limit(5)
                .map(this::mapMeetingToDTO)
                .collect(Collectors.toList());

        // Get unread messages
        List<ParentTeacherMessageDTO> unreadMessages = messageRepository
                .findUnreadMessagesByParentId(parent.getId())
                .stream()
                .limit(5)
                .map(this::mapMessageToDTO)
                .collect(Collectors.toList());

        return ParentDashboardDTO.builder()
                .parent(mapToDTO(parent))
                .stats(stats)
                .recentAnnouncements(announcements)
                .upcomingMeetings(upcomingMeetings)
                .unreadMessages(unreadMessages)
                .build();
    }

    private ParentDTO mapToDTO(Parent parent) {
        List<ParentDTO.StudentSummaryDTO> children = parent.getChildren() != null ?
                parent.getChildren().stream()
                        .map(student -> ParentDTO.StudentSummaryDTO.builder()
                                .id(student.getId())
                                .rollNumber(student.getRollNumber())
                                .admissionNumber(student.getAdmissionNumber())
                                .firstName(student.getUser().getFirstName())
                                .lastName(student.getUser().getLastName())
                                .className(student.getSchoolClass() != null ?
                                        student.getSchoolClass().getClassName() : student.getClassName())
                                .gradeLevel(student.getGradeLevel() != null ?
                                        student.getGradeLevel().getLevelName() : null)
                                .build())
                        .collect(Collectors.toList()) :
                List.of();

        return ParentDTO.builder()
                .id(parent.getId())
                .userId(parent.getUser().getId())
                .username(parent.getUser().getUsername())
                .email(parent.getUser().getEmail())
                .firstName(parent.getUser().getFirstName())
                .lastName(parent.getUser().getLastName())
                .phoneNumber(parent.getUser().getPhoneNumber())
                .relationToStudent(parent.getRelationToStudent())
                .occupation(parent.getOccupation())
                .officeAddress(parent.getOfficeAddress())
                .annualIncome(parent.getAnnualIncome())
                .children(children)
                .notificationEmailEnabled(parent.getNotificationEmailEnabled())
                .notificationSmsEnabled(parent.getNotificationSmsEnabled())
                .createdAt(parent.getCreatedAt())
                .updatedAt(parent.getUpdatedAt())
                .build();
    }

    private AnnouncementDTO mapAnnouncementToDTO(Announcement announcement) {
        return AnnouncementDTO.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .targetAudience(announcement.getTargetAudience().name())
                .priority(announcement.getPriority().name())
                .createdByName(announcement.getCreatedBy().getFirstName() + " " +
                        announcement.getCreatedBy().getLastName())
                .published(announcement.getPublished())
                .publishedAt(announcement.getPublishedAt())
                .expiresAt(announcement.getExpiresAt())
                .createdAt(announcement.getCreatedAt())
                .updatedAt(announcement.getUpdatedAt())
                .build();
    }

    private ParentMeetingDTO mapMeetingToDTO(ParentMeeting meeting) {
        return ParentMeetingDTO.builder()
                .id(meeting.getId())
                .parentId(meeting.getParent().getId())
                .parentName(meeting.getParent().getUser().getFirstName() + " " +
                        meeting.getParent().getUser().getLastName())
                .teacherId(meeting.getTeacher().getId())
                .teacherName(meeting.getTeacher().getUser().getFirstName() + " " +
                        meeting.getTeacher().getUser().getLastName())
                .studentId(meeting.getStudent() != null ? meeting.getStudent().getId() : null)
                .studentName(meeting.getStudent() != null ?
                        meeting.getStudent().getUser().getFirstName() + " " +
                        meeting.getStudent().getUser().getLastName() : null)
                .meetingDate(meeting.getMeetingDate())
                .purpose(meeting.getPurpose())
                .location(meeting.getLocation())
                .status(meeting.getStatus().name())
                .notes(meeting.getNotes())
                .createdAt(meeting.getCreatedAt())
                .updatedAt(meeting.getUpdatedAt())
                .build();
    }

    private ParentTeacherMessageDTO mapMessageToDTO(ParentTeacherMessage message) {
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

