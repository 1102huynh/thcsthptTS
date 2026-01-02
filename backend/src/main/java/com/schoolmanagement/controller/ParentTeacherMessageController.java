package com.schoolmanagement.controller;

import com.schoolmanagement.dto.ParentTeacherMessageDTO;
import com.schoolmanagement.entity.ParentTeacherMessage;
import com.schoolmanagement.service.ParentTeacherMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Parent-Teacher Messages", description = "Parent-Teacher communication endpoints")
public class ParentTeacherMessageController {

    private final ParentTeacherMessageService messageService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PARENT', 'TEACHER')")
    @Operation(summary = "Send message")
    public ResponseEntity<ParentTeacherMessageDTO> sendMessage(@RequestBody ParentTeacherMessage message) {
        ParentTeacherMessageDTO sentMessage = messageService.sendMessage(message);
        return new ResponseEntity<>(sentMessage, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PARENT', 'TEACHER', 'ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Get message by ID")
    public ResponseEntity<ParentTeacherMessageDTO> getMessageById(@PathVariable Long id) {
        ParentTeacherMessageDTO message = messageService.getMessageById(id);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @GetMapping("/parent/{parentId}")
    @PreAuthorize("hasAnyRole('PARENT', 'ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Get messages by parent ID")
    public ResponseEntity<List<ParentTeacherMessageDTO>> getMessagesByParentId(@PathVariable Long parentId) {
        List<ParentTeacherMessageDTO> messages = messageService.getMessagesByParentId(parentId);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Get messages by teacher ID")
    public ResponseEntity<List<ParentTeacherMessageDTO>> getMessagesByTeacherId(@PathVariable Long teacherId) {
        List<ParentTeacherMessageDTO> messages = messageService.getMessagesByTeacherId(teacherId);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @GetMapping("/parent/{parentId}/unread")
    @PreAuthorize("hasRole('PARENT')")
    @Operation(summary = "Get unread messages for parent")
    public ResponseEntity<List<ParentTeacherMessageDTO>> getUnreadMessagesByParentId(@PathVariable Long parentId) {
        List<ParentTeacherMessageDTO> messages = messageService.getUnreadMessagesByParentId(parentId);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @GetMapping("/teacher/{teacherId}/unread")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get unread messages for teacher")
    public ResponseEntity<List<ParentTeacherMessageDTO>> getUnreadMessagesByTeacherId(@PathVariable Long teacherId) {
        List<ParentTeacherMessageDTO> messages = messageService.getUnreadMessagesByTeacherId(teacherId);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('PARENT', 'TEACHER')")
    @Operation(summary = "Mark message as read")
    public ResponseEntity<ParentTeacherMessageDTO> markAsRead(@PathVariable Long id) {
        ParentTeacherMessageDTO message = messageService.markAsRead(id);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PARENT', 'TEACHER', 'ADMIN')")
    @Operation(summary = "Delete message")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

