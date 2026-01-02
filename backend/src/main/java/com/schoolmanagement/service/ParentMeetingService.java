package com.schoolmanagement.service;

import com.schoolmanagement.dto.ParentMeetingDTO;
import com.schoolmanagement.entity.ParentMeeting;
import com.schoolmanagement.entity.ParentMeeting.MeetingStatus;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.ParentMeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ParentMeetingService {

    private final ParentMeetingRepository meetingRepository;

    public ParentMeetingDTO scheduleMeeting(ParentMeeting meeting) {
        ParentMeeting savedMeeting = meetingRepository.save(meeting);
        return mapToDTO(savedMeeting);
    }

    public ParentMeetingDTO updateMeeting(Long id, ParentMeeting meetingDetails) {
        ParentMeeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + id));

        if (meetingDetails.getMeetingDate() != null) {
            meeting.setMeetingDate(meetingDetails.getMeetingDate());
        }
        if (meetingDetails.getPurpose() != null) {
            meeting.setPurpose(meetingDetails.getPurpose());
        }
        if (meetingDetails.getLocation() != null) {
            meeting.setLocation(meetingDetails.getLocation());
        }
        if (meetingDetails.getStatus() != null) {
            meeting.setStatus(meetingDetails.getStatus());
        }
        if (meetingDetails.getNotes() != null) {
            meeting.setNotes(meetingDetails.getNotes());
        }

        ParentMeeting updatedMeeting = meetingRepository.save(meeting);
        return mapToDTO(updatedMeeting);
    }

    public ParentMeetingDTO getMeetingById(Long id) {
        ParentMeeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + id));
        return mapToDTO(meeting);
    }

    public List<ParentMeetingDTO> getMeetingsByParentId(Long parentId) {
        return meetingRepository.findByParentIdOrderByMeetingDateDesc(parentId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ParentMeetingDTO> getMeetingsByTeacherId(Long teacherId) {
        return meetingRepository.findByTeacherIdOrderByMeetingDateDesc(teacherId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ParentMeetingDTO> getUpcomingMeetingsForParent(Long parentId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthLater = now.plusMonths(1);
        return meetingRepository.findUpcomingMeetingsForParent(parentId, now, oneMonthLater).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ParentMeetingDTO confirmMeeting(Long id) {
        ParentMeeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + id));

        meeting.setStatus(MeetingStatus.CONFIRMED);
        ParentMeeting updatedMeeting = meetingRepository.save(meeting);
        return mapToDTO(updatedMeeting);
    }

    public ParentMeetingDTO cancelMeeting(Long id) {
        ParentMeeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + id));

        meeting.setStatus(MeetingStatus.CANCELLED);
        ParentMeeting updatedMeeting = meetingRepository.save(meeting);
        return mapToDTO(updatedMeeting);
    }

    public ParentMeetingDTO completeMeeting(Long id, String notes) {
        ParentMeeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + id));

        meeting.setStatus(MeetingStatus.COMPLETED);
        if (notes != null) {
            meeting.setNotes(notes);
        }
        ParentMeeting updatedMeeting = meetingRepository.save(meeting);
        return mapToDTO(updatedMeeting);
    }

    public void deleteMeeting(Long id) {
        if (!meetingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Meeting not found with id: " + id);
        }
        meetingRepository.deleteById(id);
    }

    private ParentMeetingDTO mapToDTO(ParentMeeting meeting) {
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
}

