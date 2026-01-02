package com.schoolmanagement.controller;

import com.schoolmanagement.dto.ParentDTO;
import com.schoolmanagement.dto.ParentDashboardDTO;
import com.schoolmanagement.entity.Parent;
import com.schoolmanagement.service.ParentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/parents")
@RequiredArgsConstructor
@Tag(name = "Parent Management", description = "Parent management and portal endpoints")
public class ParentController {

    private final ParentService parentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Create new parent")
    public ResponseEntity<ParentDTO> createParent(@RequestBody Parent parent) {
        ParentDTO createdParent = parentService.createParent(parent);
        return new ResponseEntity<>(createdParent, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL') or hasRole('PARENT')")
    @Operation(summary = "Update parent")
    public ResponseEntity<ParentDTO> updateParent(@PathVariable Long id, @RequestBody Parent parentDetails) {
        ParentDTO updatedParent = parentService.updateParent(id, parentDetails);
        return new ResponseEntity<>(updatedParent, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'PARENT')")
    @Operation(summary = "Get parent by ID")
    public ResponseEntity<ParentDTO> getParentById(@PathVariable Long id) {
        ParentDTO parent = parentService.getParentById(id);
        return new ResponseEntity<>(parent, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER', 'PARENT')")
    @Operation(summary = "Get parent by user ID")
    public ResponseEntity<ParentDTO> getParentByUserId(@PathVariable Long userId) {
        ParentDTO parent = parentService.getParentByUserId(userId);
        return new ResponseEntity<>(parent, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Get all parents")
    public ResponseEntity<List<ParentDTO>> getAllParents() {
        List<ParentDTO> parents = parentService.getAllParents();
        return new ResponseEntity<>(parents, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete parent")
    public ResponseEntity<Void> deleteParent(@PathVariable Long id) {
        parentService.deleteParent(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{parentId}/children/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Add child to parent")
    public ResponseEntity<Void> addChildToParent(@PathVariable Long parentId, @PathVariable Long studentId) {
        parentService.addChildToParent(parentId, studentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{parentId}/children/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Remove child from parent")
    public ResponseEntity<Void> removeChildFromParent(@PathVariable Long parentId, @PathVariable Long studentId) {
        parentService.removeChildFromParent(parentId, studentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/dashboard/user/{userId}")
    @PreAuthorize("hasRole('PARENT')")
    @Operation(summary = "Get parent dashboard")
    public ResponseEntity<ParentDashboardDTO> getParentDashboard(@PathVariable Long userId) {
        ParentDashboardDTO dashboard = parentService.getParentDashboard(userId);
        return new ResponseEntity<>(dashboard, HttpStatus.OK);
    }
}

