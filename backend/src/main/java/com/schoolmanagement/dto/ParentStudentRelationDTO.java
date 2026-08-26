package com.schoolmanagement.dto;

import com.schoolmanagement.entity.ParentRelationship;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "Links a PARENT-role account to one of their children.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentStudentRelationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String parentName;
    private Long studentId;
    private String studentName;
    private String rollNumber;
    private ParentRelationship relationship;
    private Boolean isPrimaryContact;
    private LocalDateTime createdAt;
}
