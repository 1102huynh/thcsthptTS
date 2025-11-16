package com.schoolmanagement.dto;

import com.schoolmanagement.entity.BookCategory;
import com.schoolmanagement.entity.BookStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryBookDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private Integer publicationYear;
    private String edition;
    private BookCategory category;
    private Integer totalCopies;
    private Integer availableCopies;
    private String description;
    private String locationRack;
    private String callNumber;
    private BookStatus status;
    private Double price;
    private LocalDateTime acquisitionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


