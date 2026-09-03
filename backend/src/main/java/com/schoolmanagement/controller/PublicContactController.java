package com.schoolmanagement.controller;

import com.schoolmanagement.dto.ContactRequest;
import com.schoolmanagement.service.ContactMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public "Liên hệ" form submission. Rate-limited per IP by
 * {@link com.schoolmanagement.security.ContactRateLimitFilter}.
 */
@RestController
@RequestMapping("/v1/public/contact")
@AllArgsConstructor
@Tag(name = "Public portal", description = "Gửi liên hệ — không cần đăng nhập, có rate-limit")
public class PublicContactController {

    private ContactMessageService contactMessageService;

    @PostMapping
    @Operation(summary = "Gửi tin nhắn liên hệ")
    public ResponseEntity<Map<String, String>> submit(@Valid @RequestBody ContactRequest request) {
        contactMessageService.submit(request);
        return new ResponseEntity<>(Map.of("message", "Đã gửi liên hệ. Nhà trường sẽ phản hồi sớm."),
                HttpStatus.CREATED);
    }
}
