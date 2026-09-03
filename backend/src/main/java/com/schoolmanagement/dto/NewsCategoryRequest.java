package com.schoolmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewsCategoryRequest {

    @NotBlank
    @Size(max = 150)
    private String name;

    /** Optional; slug is derived from the name and de-duplicated server-side. */
    private Integer displayOrder;
}
