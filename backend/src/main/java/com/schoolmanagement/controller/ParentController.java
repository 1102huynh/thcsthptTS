package com.schoolmanagement.controller;

import com.schoolmanagement.dto.ParentStudentRelationDTO;
import com.schoolmanagement.entity.ParentRelationship;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.service.ParentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/parents")
@AllArgsConstructor
@Tag(name = "Parents (Phụ huynh)", description = "Links PARENT-role accounts to their children. ADMIN-managed; a PARENT may only view their own children list.")
public class ParentController {

    private ParentService parentService;

    @PostMapping("/{parentId}/children/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Link a parent to a child")
    public ResponseEntity<ParentStudentRelationDTO> linkChild(
            @PathVariable Long parentId,
            @PathVariable Long studentId,
            @RequestParam ParentRelationship relationship,
            @RequestParam(defaultValue = "false") boolean isPrimaryContact) {
        return new ResponseEntity<>(
                parentService.linkChild(parentId, studentId, relationship, isPrimaryContact), HttpStatus.CREATED);
    }

    @DeleteMapping("/{parentId}/children/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unlink a parent from a child")
    public ResponseEntity<Void> unlinkChild(@PathVariable Long parentId, @PathVariable Long studentId) {
        parentService.unlinkChild(parentId, studentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{parentId}/children")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PARENT')")
    @Operation(summary = "List a parent's children",
            description = "A PARENT caller may only list their own children (403 otherwise).")
    public ResponseEntity<List<ParentStudentRelationDTO>> getChildren(
            @PathVariable Long parentId, Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        return new ResponseEntity<>(parentService.getChildren(parentId, requester), HttpStatus.OK);
    }
}
