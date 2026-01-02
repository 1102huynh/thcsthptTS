package com.schoolmanagement.controller;

import com.schoolmanagement.dto.ParentMeetingDTO;
import com.schoolmanagement.entity.ParentMeeting;
import com.schoolmanagement.service.ParentMeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/meetings")
@RequiredArgsConstructor
@Tag(name = "Parent Meetings", description = "Parent-Teacher meeting scheduling")
public class ParentMeetingController {

    private final ParentMeetingService meetingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PARENT', 'TEACHER', 'ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Schedule meeting")
    public ResponseEntity<ParentMeetingDTO> scheduleMeeting(@RequestBody ParentMeeting meeting) {
        ParentMeetingDTO scheduledMeeting = meetingService.scheduleMeeting(meeting);
        return new ResponseEntity<>(scheduledMeeting, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PARENT', 'TEACHER', 'ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Update meeting")
    public ResponseEntity<ParentMeetingDTO> updateMeeting(@PathVariable Long id, @RequestBody ParentMeeting meetingDetails) {
        ParentMeetingDTO updatedMeeting = meetingService.updateMeeting(id, meetingDetails);
        return new ResponseEntity<>(updatedMeeting, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PARENT', 'TEACHER', 'ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Get meeting by ID")
    public ResponseEntity<ParentMeetingDTO> getMeetingById(@PathVariable Long id) {
        ParentMeetingDTO meeting = meetingService.getMeetingById(id);
        return new ResponseEntity<>(meeting, HttpStatus.OK);
    }

    @GetMapping("/parent/{parentId}")
    @PreAuthorize("hasAnyRole('PARENT', 'ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Get meetings by parent ID")
    public ResponseEntity<List<ParentMeetingDTO>> getMeetingsByParentId(@PathVariable Long parentId) {
        List<ParentMeetingDTO> meetings = meetingService.getMeetingsByParentId(parentId);
        return new ResponseEntity<>(meetings, HttpStatus.OK);
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Get meetings by teacher ID")
    public ResponseEntity<List<ParentMeetingDTO>> getMeetingsByTeacherId(@PathVariable Long teacherId) {
        List<ParentMeetingDTO> meetings = meetingService.getMeetingsByTeacherId(teacherId);
        return new ResponseEntity<>(meetings, HttpStatus.OK);
    }

    @GetMapping("/parent/{parentId}/upcoming")
    @PreAuthorize("hasRole('PARENT')")
    @Operation(summary = "Get upcoming meetings for parent")
    public ResponseEntity<List<ParentMeetingDTO>> getUpcomingMeetingsForParent(@PathVariable Long parentId) {
        List<ParentMeetingDTO> meetings = meetingService.getUpcomingMeetingsForParent(parentId);
        return new ResponseEntity<>(meetings, HttpStatus.OK);
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('PARENT', 'TEACHER')")
    @Operation(summary = "Confirm meeting")
    public ResponseEntity<ParentMeetingDTO> confirmMeeting(@PathVariable Long id) {
        ParentMeetingDTO meeting = meetingService.confirmMeeting(id);
        return new ResponseEntity<>(meeting, HttpStatus.OK);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PARENT', 'TEACHER')")
    @Operation(summary = "Cancel meeting")
    public ResponseEntity<ParentMeetingDTO> cancelMeeting(@PathVariable Long id) {
        ParentMeetingDTO meeting = meetingService.cancelMeeting(id);
        return new ResponseEntity<>(meeting, HttpStatus.OK);
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Complete meeting")
    public ResponseEntity<ParentMeetingDTO> completeMeeting(@PathVariable Long id, @RequestParam(required = false) String notes) {
        ParentMeetingDTO meeting = meetingService.completeMeeting(id, notes);
        return new ResponseEntity<>(meeting, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Delete meeting")
    public ResponseEntity<Void> deleteMeeting(@PathVariable Long id) {
        meetingService.deleteMeeting(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

