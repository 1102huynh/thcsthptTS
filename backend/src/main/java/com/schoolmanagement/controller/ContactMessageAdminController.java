package com.schoolmanagement.controller;

import com.schoolmanagement.dto.ContactMessageDTO;
import com.schoolmanagement.service.ContactMessageService;
import com.schoolmanagement.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/v1/contact-messages")
@AllArgsConstructor
@Tag(name = "Public portal", description = "Hộp thư liên hệ — ADMIN/PRINCIPAL")
public class ContactMessageAdminController {

    private ContactMessageService contactMessageService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Danh sách liên hệ (chưa xử lý trước, mới nhất trước)")
    public ResponseEntity<List<ContactMessageDTO>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Pageable pageable = PaginationUtil.toPageable(page, size);
        if (pageable == null) {
            pageable = PageRequest.of(0, 20);
        }
        Page<ContactMessageDTO> result = contactMessageService.list(pageable);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.getTotalElements()))
                .body(result.getContent());
    }

    @PutMapping("/{id}/handled")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
    @Operation(summary = "Đánh dấu đã xử lý / chưa xử lý")
    public ResponseEntity<ContactMessageDTO> setHandled(@PathVariable Long id,
                                                        @RequestParam(defaultValue = "true") boolean handled) {
        return new ResponseEntity<>(contactMessageService.markHandled(id, handled), HttpStatus.OK);
    }
}
