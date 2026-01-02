package com.schoolmanagement.service;

import com.schoolmanagement.dto.AnnouncementDTO;
import com.schoolmanagement.entity.Announcement;
import com.schoolmanagement.entity.Announcement.AnnouncementTarget;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementDTO createAnnouncement(Announcement announcement) {
        Announcement savedAnnouncement = announcementRepository.save(announcement);
        return mapToDTO(savedAnnouncement);
    }

    public AnnouncementDTO updateAnnouncement(Long id, Announcement announcementDetails) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + id));

        if (announcementDetails.getTitle() != null) {
            announcement.setTitle(announcementDetails.getTitle());
        }
        if (announcementDetails.getContent() != null) {
            announcement.setContent(announcementDetails.getContent());
        }
        if (announcementDetails.getTargetAudience() != null) {
            announcement.setTargetAudience(announcementDetails.getTargetAudience());
        }
        if (announcementDetails.getPriority() != null) {
            announcement.setPriority(announcementDetails.getPriority());
        }
        if (announcementDetails.getPublished() != null) {
            announcement.setPublished(announcementDetails.getPublished());
        }
        if (announcementDetails.getExpiresAt() != null) {
            announcement.setExpiresAt(announcementDetails.getExpiresAt());
        }

        Announcement updatedAnnouncement = announcementRepository.save(announcement);
        return mapToDTO(updatedAnnouncement);
    }

    public AnnouncementDTO getAnnouncementById(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + id));
        return mapToDTO(announcement);
    }

    public List<AnnouncementDTO> getAllAnnouncements() {
        return announcementRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<AnnouncementDTO> getActiveAnnouncements() {
        return announcementRepository.findActiveAnnouncements(LocalDateTime.now()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<AnnouncementDTO> getActiveAnnouncementsByTarget(AnnouncementTarget target) {
        return announcementRepository.findActiveAnnouncementsByTarget(target, LocalDateTime.now()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<AnnouncementDTO> getAnnouncementsByPublishStatus(Boolean published) {
        return announcementRepository.findByPublishedOrderByCreatedAtDesc(published).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public AnnouncementDTO publishAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + id));

        announcement.setPublished(true);
        announcement.setPublishedAt(LocalDateTime.now());

        Announcement updatedAnnouncement = announcementRepository.save(announcement);
        return mapToDTO(updatedAnnouncement);
    }

    public void deleteAnnouncement(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Announcement not found with id: " + id);
        }
        announcementRepository.deleteById(id);
    }

    private AnnouncementDTO mapToDTO(Announcement announcement) {
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
}

