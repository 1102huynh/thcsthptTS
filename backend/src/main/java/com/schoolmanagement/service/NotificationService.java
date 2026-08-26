package com.schoolmanagement.service;

import com.schoolmanagement.dto.NotificationDTO;
import com.schoolmanagement.dto.NotificationRecipientDTO;
import com.schoolmanagement.entity.Notification;
import com.schoolmanagement.entity.NotificationChannel;
import com.schoolmanagement.entity.NotificationRecipient;
import com.schoolmanagement.entity.NotificationStatus;
import com.schoolmanagement.entity.NotificationTargetType;
import com.schoolmanagement.entity.ParentStudentRelation;
import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.NotificationChannelUnavailableException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.NotificationRecipientRepository;
import com.schoolmanagement.repository.NotificationRepository;
import com.schoolmanagement.repository.ParentStudentRelationRepository;
import com.schoolmanagement.repository.SchoolClassRepository;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.schoolmanagement.entity.Role.PARENT;

/**
 * Sổ liên lạc điện tử per IMPLEMENTATION_PLAN.md 3.6 — a Notification is
 * created and sent (recipients resolved, delivery attempted) in the same
 * request; there's no separate "draft, then send later" step.
 *
 * <p>A recipient's {@code send()} returning {@code false} is a normal,
 * expected per-recipient delivery failure (bad address, SMTP hiccup) —
 * recorded on that one {@link NotificationRecipient} row without affecting
 * the others. {@link NotificationChannelUnavailableException} (SMS/ZALO, not
 * implemented yet) is different: the whole channel doesn't exist, so it's
 * allowed to propagate uncaught — the class-level {@code @Transactional}
 * then rolls back everything from this call (no Notification row is left
 * behind for a channel that could never have delivered anything), and
 * GlobalExceptionHandler reports it as 501.
 *
 * <p>Known limitation: recipients are sent to synchronously, one at a time,
 * inside this single request/transaction — fine at this school's scale
 * (tens to a couple hundred parents), but an ALL_PARENTS/large-CLASS EMAIL
 * broadcast would hold the DB connection for as long as every SMTP
 * round-trip takes. A real fix means moving delivery to an async
 * queue/worker, which is a bigger change than this phase's scope.
 */
@Service
@AllArgsConstructor
@Transactional
public class NotificationService {

    private NotificationRepository notificationRepository;
    private NotificationRecipientRepository notificationRecipientRepository;
    private UserRepository userRepository;
    private StudentRepository studentRepository;
    private SchoolClassRepository schoolClassRepository;
    private StaffRepository staffRepository;
    private ParentStudentRelationRepository parentStudentRelationRepository;
    private List<NotificationSender> senders;

    public NotificationDTO createAndSend(Notification request, User createdBy) {
        NotificationSender sender = resolveSender(request.getChannel());
        List<User> recipients = resolveRecipients(request.getTargetType(), request.getTargetId());
        if (recipients.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No recipients resolved for " + request.getTargetType()
                            + (request.getTargetId() != null ? " id " + request.getTargetId() : ""));
        }

        Notification notification = Notification.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .channel(request.getChannel())
                .createdBy(createdBy)
                .sentAt(LocalDateTime.now())
                .status(NotificationStatus.FAILED) // corrected below once delivery is attempted
                .build();
        notification = notificationRepository.save(notification);

        int deliveredCount = 0;
        for (User recipient : recipients) {
            String contact = resolveContact(recipient, request.getChannel());
            boolean delivered = sender.send(contact, request.getTitle(), request.getContent());

            NotificationRecipient row = NotificationRecipient.builder()
                    .notification(notification)
                    .recipient(recipient)
                    .deliveredAt(delivered ? LocalDateTime.now() : null)
                    .failureReason(delivered ? null : "Delivery failed (see server logs for the sender's error)")
                    .build();
            notificationRecipientRepository.save(row);

            if (delivered) {
                deliveredCount++;
            }
        }

        notification.setStatus(deliveredCount == recipients.size() ? NotificationStatus.SENT
                : deliveredCount == 0 ? NotificationStatus.FAILED
                : NotificationStatus.PARTIALLY_SENT);
        notification = notificationRepository.save(notification);

        return mapToDTO(notification, recipients.size(), deliveredCount);
    }

    public List<NotificationRecipientDTO> getMyNotifications(User requester) {
        return notificationRecipientRepository.findByRecipientOrderByCreatedAtDesc(requester)
                .stream()
                .map(this::mapRecipientToDTO)
                .collect(Collectors.toList());
    }

    public NotificationRecipientDTO markAsRead(Long recipientRowId, User requester) {
        NotificationRecipient row = notificationRecipientRepository.findById(recipientRowId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification recipient row not found with id: " + recipientRowId));

        if (!row.getRecipient().getId().equals(requester.getId())) {
            throw new AccessDeniedException("You may only mark your own notifications as read");
        }

        if (row.getReadAt() == null) {
            row.setReadAt(LocalDateTime.now());
            row = notificationRecipientRepository.save(row);
        }

        return mapRecipientToDTO(row);
    }

    private List<User> resolveRecipients(NotificationTargetType targetType, Long targetId) {
        switch (targetType) {
            case ALL_PARENTS:
                return userRepository.findByRole(PARENT);

            case STUDENT: {
                Student student = studentRepository.findById(targetId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + targetId));
                return parentsOf(student);
            }

            case CLASS: {
                SchoolClass schoolClass = schoolClassRepository.findById(targetId)
                        .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + targetId));
                List<Student> roster = studentRepository.findByClassNameAndSection(
                        schoolClass.getClassName(), schoolClass.getSection());
                if (roster.isEmpty()) {
                    return List.of();
                }
                // One batch query for the whole roster instead of one per student.
                Set<User> parents = parentStudentRelationRepository.findByStudentIn(roster)
                        .stream()
                        .map(ParentStudentRelation::getParent)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                return List.copyOf(parents);
            }

            case STAFF: {
                Staff staff = staffRepository.findById(targetId)
                        .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + targetId));
                return List.of(staff.getUser());
            }

            default:
                throw new IllegalArgumentException("Unsupported targetType: " + targetType);
        }
    }

    private List<User> parentsOf(Student student) {
        return parentStudentRelationRepository.findByStudent(student)
                .stream()
                .map(ParentStudentRelation::getParent)
                .distinct()
                .collect(Collectors.toList());
    }

    private String resolveContact(User recipient, NotificationChannel channel) {
        switch (channel) {
            case EMAIL:
                return recipient.getEmail();
            case SMS:
            case ZALO:
                return recipient.getPhoneNumber();
            case APP:
            default:
                return null; // AppNotificationSender ignores this — the DB row itself is the delivery.
        }
    }

    private NotificationSender resolveSender(NotificationChannel channel) {
        return senders.stream()
                .filter(sender -> sender.getChannel() == channel)
                .findFirst()
                .orElseThrow(() -> new NotificationChannelUnavailableException(
                        "No sender registered for channel " + channel));
    }

    private NotificationDTO mapToDTO(Notification notification, int recipientCount, int deliveredCount) {
        User createdBy = notification.getCreatedBy();
        return NotificationDTO.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .targetType(notification.getTargetType())
                .targetId(notification.getTargetId())
                .channel(notification.getChannel())
                .createdById(createdBy.getId())
                .createdByName(createdBy.getFirstName() + " " + createdBy.getLastName())
                .sentAt(notification.getSentAt())
                .status(notification.getStatus())
                .recipientCount(recipientCount)
                .deliveredCount(deliveredCount)
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private NotificationRecipientDTO mapRecipientToDTO(NotificationRecipient row) {
        Notification notification = row.getNotification();
        User createdBy = notification.getCreatedBy();

        return NotificationRecipientDTO.builder()
                .id(row.getId())
                .notificationId(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .channel(notification.getChannel())
                .createdByName(createdBy.getFirstName() + " " + createdBy.getLastName())
                .sentAt(notification.getSentAt())
                .readAt(row.getReadAt())
                .deliveredAt(row.getDeliveredAt())
                .failureReason(row.getFailureReason())
                .build();
    }
}
