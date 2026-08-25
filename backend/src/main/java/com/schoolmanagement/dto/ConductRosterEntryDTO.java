package com.schoolmanagement.dto;

import com.schoolmanagement.entity.ConductRating;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Schema(description = "One row of the GVCN's bulk conduct-evaluation table for a class/semester — one row per student in the class, whether or not they've been evaluated yet.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConductRosterEntryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long studentId;
    private String studentName;
    private String rollNumber;

    @Schema(description = "null if this student has no conduct record yet for this semester")
    private Long conductRecordId;

    @Schema(description = "null if this student has no conduct record yet for this semester")
    private ConductRating rating;

    private String remarks;
    private Long evaluatedById;
    private String evaluatedByName;
}
