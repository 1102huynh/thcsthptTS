package com.schoolmanagement.controller;

import com.schoolmanagement.dto.AuditLogDTO;
import com.schoolmanagement.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ADMIN-only read access to the audit trail (IMPLEMENTATION_PLAN.md 3.9).
 * Always paginated — unlike most other list endpoints in this app, there is
 * no unpaginated caller to stay backward-compatible with (this is a brand
 * new endpoint) and an audit table only ever grows, so an unbounded
 * "return everything" default isn't offered here.
 */
@RestController
@RequestMapping("/v1/audit-logs")
@AllArgsConstructor
@Tag(name = "Audit Log", description = "Nhật ký các thao tác nhạy cảm (sửa/xóa điểm, xóa học sinh, duyệt tuyển sinh, cấp quyền, đặt lại mật khẩu).")
public class AuditLogController {

    private AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List audit log entries, newest first",
            description = "Optional ?entityType= and ?actorId= filters. page/size default to 0/20.")
    public ResponseEntity<Page<AuditLogDTO>> search(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long actorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must not be negative");
        }
        if (size <= 0 || size > 200) {
            throw new IllegalArgumentException("size must be between 1 and 200");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt"));
        return new ResponseEntity<>(auditLogService.search(entityType, actorId, pageable), HttpStatus.OK);
    }
}
