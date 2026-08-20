package com.schoolmanagement.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Standard error response shape returned by every failed request (400/401/403/404/409/500).")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {
    @Schema(example = "BAD_REQUEST")
    private String status;

    @Schema(description = "Human-readable message — never the raw exception text for 500s, see GlobalExceptionHandler", example = "username: must not be blank")
    private String message;

    @Schema(example = "/api/v1/students")
    private String path;

    private LocalDateTime timestamp;

    @Schema(example = "400")
    private int code;
}

