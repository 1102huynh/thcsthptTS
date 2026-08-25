package com.schoolmanagement.controller;

import com.schoolmanagement.dto.NotificationDTO;
import com.schoolmanagement.dto.NotificationRecipientDTO;
import com.schoolmanagement.entity.Notification;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/notifications")
@AllArgsConstructor
@Tag(name = "Notifications (Sổ liên lạc điện tử)", description = "Created and sent in the same request. APP/EMAIL are live; SMS/ZALO return 501 (pending vendor decision — see IMPLEMENTATION_PLAN.md 3.6).")
public class NotificationController {

    private NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL') or hasRole('TEACHER')")
    @Operation(summary = "Create and send a notification",
            description = "Recipients are resolved from targetType/targetId (STUDENT/CLASS -> the relevant students' parents, ALL_PARENTS -> every parent, STAFF -> that one staff member).")
    public ResponseEntity<NotificationDTO> createAndSend(
            @Valid @RequestBody Notification request, Authentication authentication) {
        User createdBy = (User) authentication.getPrincipal();
        return new ResponseEntity<>(notificationService.createAndSend(request, createdBy), HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @Operation(summary = "Get my notifications",
            description = "Every notification addressed to the calling account (PARENT or any staff role), newest first.")
    public ResponseEntity<List<NotificationRecipientDTO>> getMyNotifications(Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        return new ResponseEntity<>(notificationService.getMyNotifications(requester), HttpStatus.OK);
    }

    @PutMapping("/{recipientId}/read")
    @Operation(summary = "Mark one of my notifications as read",
            description = "recipientId is the id from GET /my, not the notification id. You may only mark your own (403 otherwise).")
    public ResponseEntity<NotificationRecipientDTO> markAsRead(
            @PathVariable Long recipientId, Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        return new ResponseEntity<>(notificationService.markAsRead(recipientId, requester), HttpStatus.OK);
    }
}
