package com.schoolmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SchoolEventRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    /** Rich-text HTML; sanitized before storage. */
    private String description;

    @Size(max = 500)
    private String coverImageUrl;

    @Size(max = 255)
    private String location;

    @NotNull
    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private Boolean isFeatured;
}
