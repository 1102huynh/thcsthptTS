package com.schoolmanagement.controller;

import com.schoolmanagement.dto.AnnouncementDTO;
import com.schoolmanagement.entity.Announcement;
import com.schoolmanagement.entity.Announcement.AnnouncementTarget;
import com.schoolmanagement.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/announcements")
@RequiredArgsConstructor
@Tag(name = "Announcements", description = "School announcements management")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Create announcement")
    public ResponseEntity<AnnouncementDTO> createAnnouncement(@RequestBody Announcement announcement) {
        AnnouncementDTO createdAnnouncement = announcementService.createAnnouncement(announcement);
        return new ResponseEntity<>(createdAnnouncement, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Update announcement")
    public ResponseEntity<AnnouncementDTO> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcementDetails) {
        AnnouncementDTO updatedAnnouncement = announcementService.updateAnnouncement(id, announcementDetails);
        return new ResponseEntity<>(updatedAnnouncement, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get announcement by ID")
    public ResponseEntity<AnnouncementDTO> getAnnouncementById(@PathVariable Long id) {
        AnnouncementDTO announcement = announcementService.getAnnouncementById(id);
        return new ResponseEntity<>(announcement, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Get all announcements")
    public ResponseEntity<List<AnnouncementDTO>> getAllAnnouncements() {
        List<AnnouncementDTO> announcements = announcementService.getAllAnnouncements();
        return new ResponseEntity<>(announcements, HttpStatus.OK);
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all active announcements")
    public ResponseEntity<List<AnnouncementDTO>> getActiveAnnouncements() {
        List<AnnouncementDTO> announcements = announcementService.getActiveAnnouncements();
        return new ResponseEntity<>(announcements, HttpStatus.OK);
    }

    @GetMapping("/active/{target}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get active announcements by target audience")
    public ResponseEntity<List<AnnouncementDTO>> getActiveAnnouncementsByTarget(@PathVariable String target) {
        AnnouncementTarget targetEnum = AnnouncementTarget.valueOf(target.toUpperCase());
        List<AnnouncementDTO> announcements = announcementService.getActiveAnnouncementsByTarget(targetEnum);
        return new ResponseEntity<>(announcements, HttpStatus.OK);
    }

    @GetMapping("/published/{published}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Get announcements by publish status")
    public ResponseEntity<List<AnnouncementDTO>> getAnnouncementsByPublishStatus(@PathVariable Boolean published) {
        List<AnnouncementDTO> announcements = announcementService.getAnnouncementsByPublishStatus(published);
        return new ResponseEntity<>(announcements, HttpStatus.OK);
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Publish announcement")
    public ResponseEntity<AnnouncementDTO> publishAnnouncement(@PathVariable Long id) {
        AnnouncementDTO announcement = announcementService.publishAnnouncement(id);
        return new ResponseEntity<>(announcement, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Delete announcement")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

