package com.schoolmanagement.dto;

import com.schoolmanagement.entity.NewsStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsDTO {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime publishedDate;
    private String category;
    private String image;
    private NewsStatus status;
    private Boolean featured;
    private String authorName;
    private Long createdById;
    private String createdByUsername;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

