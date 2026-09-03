package com.schoolmanagement.controller;

import com.schoolmanagement.dto.SchoolEventDTO;
import com.schoolmanagement.dto.SchoolEventRequest;
import com.schoolmanagement.service.SchoolEventService;
import com.schoolmanagement.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/events")
@AllArgsConstructor
@Tag(name = "Events (CMS)", description = "Quản trị sự kiện công khai — ADMIN/PRINCIPAL")
public class SchoolEventAdminController {

    private SchoolEventService eventService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Danh sách sự kiện (gồm cả DRAFT)")
    public ResponseEntity<List<SchoolEventDTO>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Pageable pageable = PaginationUtil.toPageable(page, size);
        if (pageable == null) {
            pageable = PageRequest.of(0, 20);
        }
        Page<SchoolEventDTO> result = eventService.listForCms(pageable);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.getTotalElements()))
                .body(result.getContent());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Chi tiết sự kiện theo id")
    public ResponseEntity<SchoolEventDTO> get(@PathVariable Long id) {
        return new ResponseEntity<>(eventService.getForCms(id), HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Tạo sự kiện (DRAFT)")
    public ResponseEntity<SchoolEventDTO> create(@Valid @RequestBody SchoolEventRequest request) {
        return new ResponseEntity<>(eventService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Sửa sự kiện")
    public ResponseEntity<SchoolEventDTO> update(@PathVariable Long id, @Valid @RequestBody SchoolEventRequest request) {
        return new ResponseEntity<>(eventService.update(id, request), HttpStatus.OK);
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Đăng sự kiện")
    public ResponseEntity<SchoolEventDTO> publish(@PathVariable Long id) {
        return new ResponseEntity<>(eventService.publish(id), HttpStatus.OK);
    }

    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Gỡ sự kiện")
    public ResponseEntity<SchoolEventDTO> unpublish(@PathVariable Long id) {
        return new ResponseEntity<>(eventService.unpublish(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Xoá sự kiện")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
