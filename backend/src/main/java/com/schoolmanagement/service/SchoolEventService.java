package com.schoolmanagement.service;

import com.schoolmanagement.dto.SchoolEventDTO;
import com.schoolmanagement.dto.SchoolEventRequest;
import com.schoolmanagement.entity.ContentStatus;
import com.schoolmanagement.entity.SchoolEvent;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.SchoolEventRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class SchoolEventService {

    private SchoolEventRepository eventRepository;
    private SlugService slugService;
    private HtmlSanitizerService htmlSanitizer;

    // ---- CMS ----

    @Transactional(readOnly = true)
    public Page<SchoolEventDTO> listForCms(Pageable pageable) {
        return eventRepository.findAllByOrderByStartAtDesc(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public SchoolEventDTO getForCms(Long id) {
        return toDTO(load(id));
    }

    @CacheEvict(cacheNames = {"publicEventsList", "upcomingEvents", "publicHome", "publicSitemap"}, allEntries = true)
    public SchoolEventDTO create(SchoolEventRequest request) {
        SchoolEvent event = SchoolEvent.builder()
                .title(request.getTitle().trim())
                .slug(slugService.uniqueSlug(request.getTitle(), eventRepository::existsBySlug))
                .description(htmlSanitizer.sanitize(request.getDescription()))
                .coverImageUrl(trimToNull(request.getCoverImageUrl()))
                .location(trimToNull(request.getLocation()))
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .status(ContentStatus.DRAFT)
                .isFeatured(Boolean.TRUE.equals(request.getIsFeatured()))
                .build();
        return toDTO(eventRepository.save(event));
    }

    @CacheEvict(cacheNames = {"publicEventsList", "upcomingEvents", "publicHome", "publicSitemap"}, allEntries = true)
    public SchoolEventDTO update(Long id, SchoolEventRequest request) {
        SchoolEvent event = load(id);
        event.setTitle(request.getTitle().trim());
        event.setDescription(htmlSanitizer.sanitize(request.getDescription()));
        event.setCoverImageUrl(trimToNull(request.getCoverImageUrl()));
        event.setLocation(trimToNull(request.getLocation()));
        event.setStartAt(request.getStartAt());
        event.setEndAt(request.getEndAt());
        if (request.getIsFeatured() != null) {
            event.setIsFeatured(request.getIsFeatured());
        }
        return toDTO(eventRepository.save(event));
    }

    @CacheEvict(cacheNames = {"publicEventsList", "upcomingEvents", "publicHome", "publicSitemap"}, allEntries = true)
    public SchoolEventDTO publish(Long id) {
        SchoolEvent event = load(id);
        event.setStatus(ContentStatus.PUBLISHED);
        if (event.getPublishedAt() == null) {
            event.setPublishedAt(LocalDateTime.now());
        }
        return toDTO(eventRepository.save(event));
    }

    @CacheEvict(cacheNames = {"publicEventsList", "upcomingEvents", "publicHome", "publicSitemap"}, allEntries = true)
    public SchoolEventDTO unpublish(Long id) {
        SchoolEvent event = load(id);
        event.setStatus(ContentStatus.ARCHIVED);
        return toDTO(eventRepository.save(event));
    }

    @CacheEvict(cacheNames = {"publicEventsList", "upcomingEvents", "publicHome", "publicSitemap"}, allEntries = true)
    public void delete(Long id) {
        eventRepository.delete(load(id));
    }

    // ---- public ----

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "publicEventsList", key = "#when + ':' + #pageable")
    public Page<SchoolEventDTO> listPublished(String when, Pageable pageable) {
        boolean upcoming = "upcoming".equalsIgnoreCase(when);
        boolean past = "past".equalsIgnoreCase(when);
        return eventRepository.findPublished(LocalDateTime.now(), upcoming, past, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public SchoolEventDTO getPublishedBySlug(String slug) {
        return toDTO(eventRepository.findPublishedBySlug(slug, LocalDateTime.now())
                .orElseThrow(() -> new ResourceNotFoundException("Sự kiện không tồn tại hoặc chưa được đăng")));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "upcomingEvents", key = "#limit")
    public List<SchoolEventDTO> upcoming(int limit) {
        return eventRepository.findUpcoming(LocalDateTime.now(), PageRequest.of(0, limit))
                .stream().map(this::toDTO).toList();
    }

    // ---- helpers ----

    private SchoolEvent load(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School event not found with id: " + id));
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private SchoolEventDTO toDTO(SchoolEvent e) {
        return SchoolEventDTO.builder()
                .id(e.getId()).title(e.getTitle()).slug(e.getSlug())
                .description(e.getDescription()).coverImageUrl(e.getCoverImageUrl())
                .location(e.getLocation()).startAt(e.getStartAt()).endAt(e.getEndAt())
                .status(e.getStatus()).publishedAt(e.getPublishedAt()).isFeatured(e.getIsFeatured())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}
